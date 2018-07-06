package de.pk.jblockchain.node.service;

import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.GZIPOutputStream;

import org.apache.commons.codec.binary.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.ObjectMapper;

import de.pk.jblockchain.common.domain.Address;
import de.pk.jblockchain.common.domain.Node;

@Service
public class AddressService {

	@Value("${storage.path}")
	private String storePath;

	private final static Logger LOG = LoggerFactory.getLogger(AddressService.class);

	/**
	 * Mapping of Address hash -> Address object
	 */
	private Map<String, Address> addresses = new HashMap<>();

	/**
	 * Get a specific Address
	 *
	 * @param hash
	 *            hash of Address
	 * @return Matching Address for hash
	 */
	public Address getByHash(byte[] hash) {
		return addresses.get(Base64.encodeBase64String(hash));
	}

	/**
	 * Return all Addresses from map
	 *
	 * @return Collection of Addresses
	 */
	public Collection<Address> getAll() {
		return addresses.values();
	}

	/**
	 * load initial values
	 *
	 * @param Map<String,
	 *            Address>
	 */
	public void init(Map<String, Address> addresses) {
		this.addresses = addresses;
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
			fStream = new FileOutputStream(System.getProperty("user.home") + this.storePath + "address.json.gz");
			zStream = new GZIPOutputStream(new BufferedOutputStream(fStream));
			mapper.writeValue(zStream, this.addresses);

		} finally {
			if (zStream != null) {
				zStream.flush();
				zStream.close();
			}
			if (fStream != null) {
				fStream.flush();
				fStream.close();
			}
			System.out.println("Addresses saved!");
		}
	}

	/**
	 * Add a new Address to the map
	 *
	 * @param address
	 *            Address to add
	 */
	public synchronized void add(Address address) {
		addresses.put(Base64.encodeBase64String(address.getHash()), address);
		try {
			this.save();
		} catch (IOException e) {
			System.out.println("Unable to save addresses: " + e.getMessage());
		}
	}

	/**
	 * Download Addresses from other Node and them to the map
	 *
	 * @param node
	 *            Node to query
	 * @param restTemplate
	 *            RestTemplate to use
	 */
	public void retrieveAddresses(Node node, RestTemplate restTemplate) {
		Address[] addresses = restTemplate.getForObject(node.getAddress() + "/address", Address[].class);
		Arrays.asList(addresses).forEach(this::add);
		LOG.info("Retrieved " + addresses.length + " addresses from node " + node.getAddress());
	}
}
