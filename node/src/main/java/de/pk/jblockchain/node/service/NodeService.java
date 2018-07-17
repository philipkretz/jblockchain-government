package de.pk.jblockchain.node.service;

import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.URL;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;
import java.util.zip.GZIPOutputStream;

import javax.annotation.PreDestroy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.embedded.EmbeddedServletContainerInitializedEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.ObjectMapper;

import de.pk.jblockchain.common.domain.Node;

@Service
public class NodeService implements ApplicationListener<EmbeddedServletContainerInitializedEvent> {

	@Value("${storage.path}")
	private String storePath;

	private final static Logger LOG = LoggerFactory.getLogger(NodeService.class);

	private final BlockService blockService;
	private final TransactionService transactionService;
	private final AddressService addressService;

	@Value("${server.ssl.enabled}")
	private Boolean sslEnabled;

	@Value("${blockchain.masterNodeAddresses}")
	private String masterNodeAddresses;

	private Node self;
	private Set<Node> knownNodes = new HashSet<>();
	private RestTemplate restTemplate = new RestTemplate(getClientHttpRequestFactory());

	private ClientHttpRequestFactory getClientHttpRequestFactory() {
		int timeout = 5000;
		HttpComponentsClientHttpRequestFactory clientHttpRequestFactory = new HttpComponentsClientHttpRequestFactory();
		clientHttpRequestFactory.setConnectTimeout(timeout);
		return clientHttpRequestFactory;
	}

	@Autowired
	public NodeService(BlockService blockService, TransactionService transactionService,
			AddressService addressService) {
		this.blockService = blockService;
		this.transactionService = transactionService;
		this.addressService = addressService;
	}

	/**
	 * load initial values
	 *
	 * @param nodes
	 */
	public void init(Set<Node> nodes) {
		this.knownNodes = nodes;
	}

	/**
	 * save values
	 *
	 */
	public void save() throws IOException {
		ObjectMapper mapper = new ObjectMapper();
		FileOutputStream fStream = null;
		GZIPOutputStream zStream = null;
		try {
			fStream = new FileOutputStream(System.getProperty("user.home") + this.storePath + "node.json.gz");
			zStream = new GZIPOutputStream(new BufferedOutputStream(fStream));
			mapper.writeValue(zStream, this.knownNodes);

		} finally {
			if (zStream != null) {
				zStream.flush();
				zStream.close();
			}
			if (fStream != null) {
				fStream.flush();
				fStream.close();
			}
			System.out.println("Nodes saved!");
		}
	}

	/**
	 * Initial setup, query master Node for - Other Nodes - All Addresses -
	 * Current Blockchain - Transactions in pool and publish self on all other
	 * Nodes
	 *
	 * @param embeddedServletContainerInitializedEvent
	 *            serverletContainer for port retrieval
	 */
	@Override
	public void onApplicationEvent(EmbeddedServletContainerInitializedEvent embeddedServletContainerInitializedEvent) {
		Node masterNode = getMasterNode();

		self = getSelfNode(embeddedServletContainerInitializedEvent);
		LOG.info("Self address: " + self.getAddress());

		// download data if necessary
		if (self.equals(masterNode)) {
			LOG.info("Running as master node, nothing to init");
		} else {
			knownNodes.add(masterNode);

			// retrieve data
			retrieveKnownNodes(masterNode, restTemplate);
			addressService.retrieveAddresses(masterNode, restTemplate);
			blockService.retrieveBlockchain(masterNode, restTemplate);
			transactionService.retrieveTransactions(masterNode, restTemplate);

			// publish self
			broadcastPut("node", self);
		}
	}

	/**
	 * Logout from every other Node before shutdown
	 */
	@PreDestroy
	public void shutdown() {
		LOG.info("Shutting down");
		broadcastPost("node/remove", self);
		LOG.info(knownNodes.size() + " informed");
	}

	public Set<Node> getKnownNodes() {
		return knownNodes;
	}

	public synchronized void add(Node node) {
		knownNodes.add(node);
		try {
			this.save();
		} catch (IOException e) {
			System.out.println("Unable to save nodes: " + e.getMessage());
		}
	}

	public synchronized void remove(Node node) {
		knownNodes.remove(node);
		try {
			this.save();
		} catch (IOException e) {
			System.out.println("Unable to save nodes: " + e.getMessage());
		}
	}

	/**
	 * Invoke a PUT request on all other Nodes
	 *
	 * @param endpoint
	 *            the endpoint for this request
	 * @param data
	 *            the data to send
	 */
	public void broadcastPut(String endpoint, Object data) {
		knownNodes.parallelStream().forEach(node -> restTemplate.put(node.getAddress() + "/" + endpoint, data));
	}

	/**
	 * Invoke a POST request on all other Nodes
	 *
	 * @param endpoint
	 *            the endpoint for this request
	 * @param data
	 *            the data to send
	 */
	public void broadcastPost(String endpoint, Object data) {
		knownNodes.parallelStream()
				.forEach(node -> restTemplate.postForLocation(node.getAddress() + "/" + endpoint, data));
	}

	/**
	 * Download Nodes from other Node and them to known Nodes
	 *
	 * @param node
	 *            Node to query
	 * @param restTemplate
	 *            RestTemplate to use
	 */
	public void retrieveKnownNodes(Node node, RestTemplate restTemplate) {
		Node[] nodes = restTemplate.getForObject(node.getAddress() + "/node", Node[].class);
		Collections.addAll(knownNodes, nodes);
		LOG.info("Retrieved " + nodes.length + " nodes from node " + node.getAddress());
	}

	private Node getSelfNode(EmbeddedServletContainerInitializedEvent embeddedServletContainerInitializedEvent) {
		String host = "127.0.0.1";
		int port = embeddedServletContainerInitializedEvent.getEmbeddedServletContainer().getPort();

		try {
			// discover own ip address in public or local network
			String result = "";
			String externalIP = "";
			String protocol = this.sslEnabled ? "https" : "http";
			try {
				externalIP = restTemplate.getForObject("http://checkip.amazonaws.com/", String.class);
				externalIP = externalIP.substring(0, externalIP.length() - 1);
				try {
					result = restTemplate.getForObject("{protocol}://{externalIP}:{port}/address/check-loopback",
							String.class, protocol, externalIP, port);
				} catch (Exception ex) {
					LOG.warn("External ip address " + externalIP + " is not reachable at port " + port, ex);
				}
			} catch (Exception ex) {
				LOG.warn("Could not establish a network connection to discover external ip address.", ex);
			}

			if (result.equals("connected")) {
				// discover external ip and try loopback
				host = externalIP;

				LOG.info("Found own external ip address " + externalIP);
			} else {
				// discover addresses from network interfaces
				String hostTest;

				LOG.info("discover private IP address from network interfaces");
				try {
					Enumeration<NetworkInterface> e = NetworkInterface.getNetworkInterfaces();
					while (e.hasMoreElements()) {
						NetworkInterface n = e.nextElement();
						Enumeration<InetAddress> ee = n.getInetAddresses();
						while (ee.hasMoreElements()) {
							InetAddress i = ee.nextElement();
							String hostAddr = i.getHostAddress();
							LOG.info("hostAddr=" + hostAddr);
							if (hostAddr.equals("127.0.0.1") || hostAddr.substring(0, 3).equals("10.")
									|| (hostAddr.substring(0, 4).equals("172.") && hostAddr.substring(6, 7).equals(".")
											&& (Integer.valueOf(hostAddr.substring(4, 6)) >= 16
													&& Integer.valueOf(hostAddr.substring(4, 6)) <= 31))
									|| hostAddr.substring(0, 8).equals("192.168.")) {
								hostTest = hostAddr;

								LOG.info("Found local ip address " + hostTest);
							} else {
								hostTest = hostAddr;

								LOG.info("Found own external ip address " + hostTest);
							}
							try {
								result = restTemplate.getForObject(
										"{protocol}://{hostTest}:{port}/address/check-loopback", String.class, protocol,
										hostTest, port);
								if (result.equals("connected")) {
									host = hostTest;
									break;
								}
							} catch (Exception ex) {
								LOG.warn("ip address " + hostTest + " is not reachable at port " + port, ex);
							}
						}
					}
				} catch (SocketException ex) {
					LOG.error(
							"SocketException: could not access computer's network interfaces. Check your security privilegues.",
							ex);
					return new Node();
				}
			}

			return new Node(new URL(protocol, host, port, ""));
		} catch (MalformedURLException e) {
			LOG.error("Invalid self URL", e);
			return new Node();
		}
	}

	private Node getMasterNode() {
		try {
			// Improvement: randomize master node addresses for secure backbone
			// network

			String[] masterNodeAddresses = this.masterNodeAddresses.split(",");

			return new Node(
					new URL(masterNodeAddresses[(int) Math.floor(Math.random() * masterNodeAddresses.length)].trim()));
		} catch (MalformedURLException e) {
			LOG.error("Invalid master node URL", e);
			return new Node();
		}
	}

}
