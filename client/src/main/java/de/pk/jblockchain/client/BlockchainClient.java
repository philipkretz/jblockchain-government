package de.pk.jblockchain.client;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Properties;
import java.util.zip.GZIPInputStream;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionGroup;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.codec.binary.Base64;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import de.pk.jblockchain.common.SignatureUtils;
import de.pk.jblockchain.common.domain.Address;
import de.pk.jblockchain.common.domain.Block;
import de.pk.jblockchain.common.domain.Transaction;

/**
 * Simple class to help building REST calls for jBlockchain. Just run it in
 * command line for instructions on how to use it.
 *
 * Functions include: - Generate Private/Public-Key - Publish a new Address -
 * Publish a new Transaction
 */
public class BlockchainClient {

	private static String serverAddr;

	private static String serverPort;

	private static Boolean sslEnabled;

	private static String storePath;

	public static void main(String args[]) throws Exception {
		Properties props = new Properties();
		// System.out.println(BlockchainClient.class.getResource("/."));
		props.load(BlockchainClient.class.getResourceAsStream("/application.properties"));
		serverAddr = props.getProperty("server.address");
		serverPort = props.getProperty("server.port");
		sslEnabled = Boolean.valueOf(props.getProperty("server.ssl.enabled"));
		storePath = props.getProperty("storage.path");

		CommandLineParser parser = new DefaultParser();
		Options options = getOptions();
		try {
			CommandLine line = parser.parse(options, args);
			executeCommand(line);
		} catch (ParseException e) {
			System.err.println(e.getMessage());
			HelpFormatter formatter = new HelpFormatter();
			formatter.printHelp("BlockchainClient", options, true);
		}
	}

	private static void executeCommand(CommandLine line) throws Exception {
		if (line.hasOption("keypair")) {
			generateKeyPair();
		} else if (line.hasOption("start-miner")) {
			startMiner();
		} else if (line.hasOption("stop-miner")) {
			stopMiner();
		} else if (line.hasOption("publish-address")) {
			String publickey = line.getOptionValue("publickey");
			if (publickey == null) {
				throw new ParseException("publickey is required");
			}
			publishAddress(Paths.get(System.getProperty("user.home") + storePath + publickey));

		} else if (line.hasOption("transaction")) {
			String message = line.getOptionValue("message");
			String sender = line.getOptionValue("sender");
			String privatekey = line.getOptionValue("privatekey");
			if (message == null || sender == null || privatekey == null) {
				throw new ParseException("message, sender and privatekey is required");
			}
			publishTransaction(Paths.get(privatekey), message, Base64.decodeBase64(sender));
		} else if (line.hasOption("add-citizen")) {
			String sender = line.getOptionValue("sender");
			String privatekey = line.getOptionValue("privatekey");
			String firstName = line.getOptionValue("firstName");
			String lastName = line.getOptionValue("lastName");
			DateFormat format = new SimpleDateFormat("YYYYMMdd", Locale.ENGLISH);
			Date birthday = format.parse(line.getOptionValue("birthday"));
			String address = line.getOptionValue("address");
			String mother = line.getOptionValue("mother");
			String father = line.getOptionValue("father");
			addCitizen(Paths.get(System.getProperty("user.home") + storePath + privatekey), Base64.decodeBase64(sender),
					firstName, lastName, birthday, address, mother, father);
		} else if (line.hasOption("marriage")) {
			String sender = line.getOptionValue("sender");
			String privatekey = line.getOptionValue("privatekey");
			String person1 = line.getOptionValue("person1");
			String person2 = line.getOptionValue("person2");
			DateFormat format = new SimpleDateFormat("YYYYMMdd", Locale.ENGLISH);
			Date date = format.parse(line.getOptionValue("date"));
			marriage(Paths.get(System.getProperty("user.home") + storePath + privatekey), Base64.decodeBase64(sender),
					person1, person2, date);
		} else if (line.hasOption("divorce")) {
			String sender = line.getOptionValue("sender");
			String privatekey = line.getOptionValue("privatekey");
			String person1 = line.getOptionValue("person1");
			String person2 = line.getOptionValue("person2");
			divorce(Paths.get(System.getProperty("user.home") + storePath + privatekey), Base64.decodeBase64(sender),
					person1, person2);
		} else if (line.hasOption("declare-death")) {
			String sender = line.getOptionValue("sender");
			String privatekey = line.getOptionValue("privatekey");
			String firstName = line.getOptionValue("firstName");
			String lastName = line.getOptionValue("lastName");
			DateFormat format = new SimpleDateFormat("YYYYMMdd", Locale.ENGLISH);
			Date date = format.parse(line.getOptionValue("date"));
			declareDeath(Paths.get(System.getProperty("user.home") + storePath + privatekey),
					Base64.decodeBase64(sender), firstName, lastName, date);
		} else if (line.hasOption("declare-alive")) {
			String sender = line.getOptionValue("sender");
			String privatekey = line.getOptionValue("privatekey");
			String firstName = line.getOptionValue("firstName");
			String lastName = line.getOptionValue("lastName");
			declareAlive(Paths.get(System.getProperty("user.home") + storePath + privatekey),
					Base64.decodeBase64(sender), firstName, lastName);
		} else if (line.hasOption("add-city")) {
			String sender = line.getOptionValue("sender");
			String privatekey = line.getOptionValue("privatekey");
			String city = line.getOptionValue("city");
			addCity(Paths.get(System.getProperty("user.home") + storePath + privatekey), Base64.decodeBase64(sender),
					city);
		} else if (line.hasOption("add-street")) {
			String sender = line.getOptionValue("sender");
			String privatekey = line.getOptionValue("privatekey");
			String city = line.getOptionValue("city");
			String street = line.getOptionValue("street");
			addStreet(Paths.get(System.getProperty("user.home") + storePath + privatekey), Base64.decodeBase64(sender),
					city, street);
		} else if (line.hasOption("add-house")) {
			String sender = line.getOptionValue("sender");
			String privatekey = line.getOptionValue("privatekey");
			String street = line.getOptionValue("street");
			Integer houseNr = Integer.valueOf(line.getOptionValue("houseNr"));
			addHouse(Paths.get(System.getProperty("user.home") + storePath + privatekey), Base64.decodeBase64(sender),
					street, houseNr);
		} else if (line.hasOption("set-address")) {
			String sender = line.getOptionValue("sender");
			String privatekey = line.getOptionValue("privatekey");
			String firstName = line.getOptionValue("firstName");
			String lastName = line.getOptionValue("lastName");
			String address = line.getOptionValue("address");
			setAddress(Paths.get(System.getProperty("user.home") + storePath + privatekey), Base64.decodeBase64(sender),
					firstName, lastName, address);
		} else if (line.hasOption("show-cities")) {
			showCities();
		} else if (line.hasOption("show-citizens")) {
			String city = line.getOptionValue("city");
			showCitizens(city);
		} else if (line.hasOption("show-citizen-details")) {
			String city = line.getOptionValue("city");
			String name = line.getOptionValue("name");
			showCitizenDetails(city, name);
		} else if (line.hasOption("show-birth-death-rate")) {
			String city = line.getOptionValue("city");
			showBirthDeathRate(city);
		}

		// TODO: extend for additional commands
	}

	private static Options getOptions() {
		OptionGroup actions = new OptionGroup();
		actions.addOption(new Option("sm", "start-miner", false, "start mining server process"));
		actions.addOption(new Option("stm", "stop-miner", false, "stop mining server process"));
		actions.addOption(new Option("k", "keypair", false, "generate private/public key pair"));
		actions.addOption(new Option("a", "publish-address", false, "publish new address"));
		actions.addOption(new Option("t", "transaction", false, "publish new transaction"));
		actions.addOption(new Option("ac", "add-citizen", false, "add a new citizen"));
		actions.addOption(new Option("m", "marriage", false, "declare 2 people as marriaged"));
		actions.addOption(new Option("d", "divorce", false, "declare 2 people as divorced"));
		actions.addOption(new Option("dd", "declare-death", false, "declare death of a citizen"));
		actions.addOption(new Option("da", "declare-alive", false, "declare a citizen alive after death"));
		actions.addOption(new Option("c", "add-city", false, "add a new city"));
		actions.addOption(new Option("st", "add-street", false, "add a new street"));
		actions.addOption(new Option("h", "add-house", false, "add a new house"));
		actions.addOption(new Option("saddr", "set-address", false, "change the address of a citizen"));
		actions.addOption(new Option("sc", "show-cities", false, "show all cities"));
		actions.addOption(new Option("scz", "show-citizens", false, "show all citizen of a city"));
		actions.addOption(new Option("scd", "show-citizen-details", false, "show details of a citizen"));
		actions.addOption(new Option("sbd", "show-birth-death-rate", false, "show birth death rate of a city"));
		actions.setRequired(true);

		Options options = new Options();
		options.addOptionGroup(actions);
		options.addOption(Option.builder("o").longOpt("node").hasArg().argName("Node URL").desc("").build());
		options.addOption(
				Option.builder("n").longOpt("name").hasArg().argName("name for new address").desc("").build());
		options.addOption(
				Option.builder("p").longOpt("publickey").hasArg().argName("path to key file").desc("").build());
		options.addOption(
				Option.builder("v").longOpt("privatekey").hasArg().argName("path to key file").desc("").build());
		options.addOption(Option.builder("m").longOpt("message").hasArg().argName("message to post").desc("").build());
		options.addOption(
				Option.builder("s").longOpt("sender").hasArg().argName("address hash (Base64)").desc("").build());
		options.addOption(Option.builder("fn").longOpt("firstName").hasArg().argName("Firstname").desc("").build());
		options.addOption(Option.builder("ln").longOpt("lastName").hasArg().argName("Lastname").desc("").build());
		options.addOption(Option.builder("bd").longOpt("birthday").hasArg().argName("birthday").desc("").build());
		options.addOption(Option.builder("mt").longOpt("mother").hasArg().argName("mother").desc("").build());
		options.addOption(Option.builder("ft").longOpt("father").hasArg().argName("father").desc("").build());
		options.addOption(Option.builder("addr").longOpt("address").hasArg().argName("address").desc("").build());
		options.addOption(Option.builder("p1").longOpt("person1").hasArg().argName("person 1").desc("").build());
		options.addOption(Option.builder("p2").longOpt("person2").hasArg().argName("person 2").desc("").build());
		options.addOption(Option.builder("dt").longOpt("date").hasArg().argName("date").desc("").build());
		options.addOption(Option.builder("cty").longOpt("city").hasArg().argName("city").desc("").build());
		options.addOption(Option.builder("str").longOpt("street").hasArg().argName("street").desc("").build());
		return options;
	}

	private static void generateKeyPair() throws NoSuchProviderException, NoSuchAlgorithmException, IOException {
		KeyPair keyPair = SignatureUtils.generateKeyPair();
		Files.write(Paths.get(System.getProperty("user.home") + storePath + "key.priv"),
				keyPair.getPrivate().getEncoded());
		Files.write(Paths.get(System.getProperty("user.home") + storePath + "key.pub"),
				keyPair.getPublic().getEncoded());
	}

	private static void publishAddress(Path publicKey) throws IOException {
		String protocol = sslEnabled ? "https" : "http";
		RestTemplate restTemplate = new RestTemplate();
		Address address = new Address(Files.readAllBytes(publicKey));
		restTemplate.put(protocol + "://" + serverAddr + ":" + serverPort + "/address?publish=true", address);
		System.out.println("Hash of new address: " + Base64.encodeBase64String(address.getHash()));
	}

	private static void startMiner() throws Exception {
		String protocol = sslEnabled ? "https" : "http";
		RestTemplate restTemplate = new RestTemplate();
		restTemplate.getForEntity(protocol + "://" + serverAddr + ":" + serverPort + "/block/start-miner",
				String.class);
		System.out.println("Started mining process");
	}

	private static void stopMiner() throws Exception {
		String protocol = sslEnabled ? "https" : "http";
		RestTemplate restTemplate = new RestTemplate();
		restTemplate.getForEntity(protocol + "://" + serverAddr + ":" + serverPort + "/block/stop-miner", String.class);
		System.out.println("Stopped mining process");
	}

	private static void publishTransaction(Path privateKey, String message, byte[] senderHash) throws Exception {
		String protocol = sslEnabled ? "https" : "http";
		RestTemplate restTemplate = new RestTemplate();
		byte[] signature = SignatureUtils.sign(message.getBytes(), Files.readAllBytes(privateKey));
		Transaction transaction = new Transaction(message, senderHash, signature);
		restTemplate.put(protocol + "://" + serverAddr + ":" + serverPort + "/transaction?publish=true", transaction);
		System.out.println("Hash of new transaction: " + Base64.encodeBase64String(transaction.getHash()));
	}

	/**
	 * addCitizen
	 *
	 * @description add a new citizen
	 * @param privateKey
	 * @param senderHash
	 * @param firstName
	 * @param lastName
	 * @param birthday
	 * @param address
	 * @param mother
	 * @param father
	 * @throws Exception
	 */
	private static void addCitizen(Path privateKey, byte[] senderHash, String firstName, String lastName, Date birthday,
			String address, String mother, String father) throws Exception {
		// TODO: add sex!
		// TODO: a citizen is absolute by his name in the whole blockchain.
		// In a productive environment he should be absolute by his name and
		// additional parameters like birthday, address, father and mother.

		// Format: Lastname|Firstname|Birthday|Address|Mother|Father
		String message = String.format("AD%s|%s|%s|%s|%s|%s", lastName, firstName, birthday, address, mother, father);
		publishTransaction(privateKey, message, senderHash);
	}

	/**
	 * marriage
	 *
	 * @description add a marriage
	 * @param privateKey
	 * @param senderHash
	 * @param person1
	 * @param person2
	 * @param date
	 * @throws Exception
	 */
	private static void marriage(Path privateKey, byte[] senderHash, String person1, String person2, Date date)
			throws Exception {
		String message = String.format("MR%s|%s|%s", person1, person2, date);
		publishTransaction(privateKey, message, senderHash);
	}

	/**
	 * divorce
	 *
	 * @description add a divorce
	 * @param privateKey
	 * @param senderHash
	 * @param person1
	 * @param person2
	 * @throws Exception
	 */
	private static void divorce(Path privateKey, byte[] senderHash, String person1, String person2) throws Exception {
		String message = String.format("DV%s|%s", person1, person2);
		publishTransaction(privateKey, message, senderHash);
	}

	/**
	 * declareDeath
	 *
	 * @description declare death of a citizen
	 * @param privateKey
	 * @param senderHash
	 * @param firstName
	 * @param lastName
	 * @param date
	 * @throws Exception
	 */
	private static void declareDeath(Path privateKey, byte[] senderHash, String firstName, String lastName, Date date)
			throws Exception {
		String message = String.format("DH%s|%s|%s", firstName, lastName, date);
		publishTransaction(privateKey, message, senderHash);
	}

	/**
	 * declareAlive
	 *
	 * @description declare a citizen alive after death
	 * @param privateKey
	 * @param senderHash
	 * @param firstName
	 * @param lastName
	 * @throws Exception
	 */
	private static void declareAlive(Path privateKey, byte[] senderHash, String firstName, String lastName)
			throws Exception {
		String message = String.format("DA%s|%s", firstName, lastName);
		publishTransaction(privateKey, message, senderHash);
	}

	/**
	 * addCity
	 *
	 * @description add a new city
	 * @param privateKey
	 * @param senderHash
	 * @param name
	 * @throws Exception
	 */
	private static void addCity(Path privateKey, byte[] senderHash, String name) throws Exception {
		String message = String.format("AC{%s}", name);
		publishTransaction(privateKey, message, senderHash);
	}

	/**
	 * addStreet
	 *
	 * @description add a new street to a city
	 * @param privateKey
	 * @param senderHash
	 * @param city
	 * @param name
	 * @throws Exception
	 */
	private static void addStreet(Path privateKey, byte[] senderHash, String city, String name) throws Exception {
		String message = String.format("AS%s|%s", city, name);
		publishTransaction(privateKey, message, senderHash);
	}

	/**
	 * addHouse
	 *
	 * @description add a new house to a street
	 * @param privateKey
	 * @param senderHash
	 * @param street
	 * @param houseNr
	 * @throws Exception
	 */
	private static void addHouse(Path privateKey, byte[] senderHash, String street, Integer houseNr) throws Exception {
		String message = String.format("AH%s|%s", street, houseNr);
		publishTransaction(privateKey, message, senderHash);
	}

	/**
	 * setAddress
	 *
	 * @description change the address of a citizen
	 * @param privateKey
	 * @param senderHash
	 * @param firstName
	 * @param lastName
	 * @param address
	 * @throws Exception
	 */
	private static void setAddress(Path privateKey, byte[] senderHash, String firstName, String lastName,
			String address) throws Exception {
		String message = String.format("SD%s|%s|%s", firstName, lastName, address);
		publishTransaction(privateKey, message, senderHash);
	}

	/**
	 * showCities
	 *
	 * @description list all cities (TODO: in alphabetical order)
	 * @throws Exception
	 */
	private static void showCities() throws Exception {
		String protocol = sslEnabled ? "https" : "http";
		RestTemplate restTemplate = new RestTemplate();
		ObjectMapper mapper = new ObjectMapper();
		mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
		TypeReference<List<Block>> typeReference = new TypeReference<List<Block>>() {
		};
		String url = protocol + "://" + serverAddr + ":" + serverPort + "/transaction";
		HttpEntity<TypeReference<List<Block>>> request = new HttpEntity<>(typeReference);
		ResponseEntity<Resource> responseEntity = restTemplate.exchange(url, HttpMethod.GET, request, Resource.class);
		InputStream responseInputStream = responseEntity.getBody().getInputStream();
		GZIPInputStream zStream = new GZIPInputStream(responseInputStream);
		List<Block> blocks = mapper.readValue(zStream, typeReference);
		for (Block block : blocks) {
			for (Transaction trans : block.getTransactions()) {
				String msg = trans.getText();
				if (msg.startsWith("AC")) {
					System.out.println(msg.substring(2));
				}
			}
		}
	}

	/**
	 * showCitizens
	 *
	 * @description list all citizens in a city (TODO: in alphabetical order)
	 * @param city
	 * @throws Exception
	 */
	private static void showCitizens(String city) throws Exception {
		String protocol = sslEnabled ? "https" : "http";
		RestTemplate restTemplate = new RestTemplate();
		ObjectMapper mapper = new ObjectMapper();
		mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
		TypeReference<List<Block>> typeReference = new TypeReference<List<Block>>() {
		};
		String url = protocol + "://" + serverAddr + ":" + serverPort + "/transaction";
		HttpEntity<TypeReference<List<Block>>> request = new HttpEntity<>(typeReference);
		ResponseEntity<Resource> responseEntity = restTemplate.exchange(url, HttpMethod.GET, request, Resource.class);
		InputStream responseInputStream = responseEntity.getBody().getInputStream();
		GZIPInputStream zStream = new GZIPInputStream(responseInputStream);
		List<Block> blocks = mapper.readValue(zStream, typeReference);
		int c = 0;
		for (Block block : blocks) {
			for (Transaction trans : block.getTransactions()) {
				String msg = trans.getText();
				if (msg.startsWith("AD")) {
					String[] msgArr = msg.substring(2).split("|");
					if (msgArr[3].contains(city)) {

						// check if already dead
						boolean isDead = false;
						for (Block block2 : blocks) {
							for (Transaction trans2 : block2.getTransactions()) {
								String msg2 = trans2.getText();
								if (msg2.startsWith("DH")) {
									String[] msg2Arr = msg2.substring(2).split("|");
									if (msgArr[0] == msg2Arr[0] && msgArr[1] == msg2Arr[1]) {
										isDead = true;
									}
								} else if (isDead && msg2.startsWith("DA")) {
									String[] msg2Arr = msg2.substring(2).split("|");
									if (msgArr[0] == msg2Arr[0] && msgArr[1] == msg2Arr[1]) {
										isDead = false;
									}
								}
							}
						}

						if (!isDead) {
							c++;
							System.out.println("Citizen #" + c + ":");
							System.out.println("Firstname: " + msgArr[1]);
							System.out.println("Lastname: " + msgArr[0]);
						}
					}
				}
			}
		}
		System.out.println("\nfound " + c + " citizens in " + city + "!");
	}

	/**
	 * showCitizenDetails
	 *
	 * @description show all saved details about a citizen in a city
	 * @param city
	 * @param name
	 * @throws Exception
	 */
	private static void showCitizenDetails(String city, String name) throws Exception {
		String protocol = sslEnabled ? "https" : "http";
		RestTemplate restTemplate = new RestTemplate();
		ObjectMapper mapper = new ObjectMapper();
		mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
		TypeReference<List<Block>> typeReference = new TypeReference<List<Block>>() {
		};
		String url = protocol + "://" + serverAddr + ":" + serverPort + "/transaction";
		HttpEntity<TypeReference<List<Block>>> request = new HttpEntity<>(typeReference);
		ResponseEntity<Resource> responseEntity = restTemplate.exchange(url, HttpMethod.GET, request, Resource.class);
		InputStream responseInputStream = responseEntity.getBody().getInputStream();
		GZIPInputStream zStream = new GZIPInputStream(responseInputStream);
		List<Block> blocks = mapper.readValue(zStream, typeReference);
		ArrayList<String> declaredDeaths = new ArrayList<>();
		ArrayList<String> declaredAlive = new ArrayList<>();
		ArrayList<String> addresses = new ArrayList<>();
		ArrayList<String[]> children = new ArrayList<>();
		ArrayList<String[]> marriages = new ArrayList<>();
		ArrayList<String[]> divorces = new ArrayList<>();
		String[] personalDetails = null;

		for (Block block : blocks) {
			for (Transaction trans : block.getTransactions()) {
				String msg = trans.getText();
				if (msg.startsWith("AD")) {
					String[] msgArr = msg.substring(2).split("|");
					if (msgArr[3].contains(city) && msgArr[1] + " " + msgArr[0] == name) {
						personalDetails = msgArr;
					} else if (msgArr[4] == name || msgArr[5] == name) {
						String[] child = { msgArr[1] + " " + msgArr[0], msgArr[2] };
						children.add(child);
					}
				} else if (msg.startsWith("DH")) {
					String[] msgArr = msg.substring(2).split("|");
					if (msgArr[0] + " " + msgArr[1] == name) {
						declaredDeaths.add(msgArr[2]);
					}
				} else if (msg.startsWith("DA")) {
					String[] msgArr = msg.substring(2).split("|");
					if (msgArr[0] + " " + msgArr[1] == name) {
						declaredAlive.add(trans.getTimestamp() + "");
					}
				} else if (msg.startsWith("SD")) {
					String[] msgArr = msg.substring(2).split("|");
					if (msgArr[0] + " " + msgArr[1] == name) {
						addresses.add(msgArr[2]);
					}
				} else if (msg.startsWith("MR")) {
					String[] msgArr = msg.substring(2).split("|");
					if (msgArr[0] == name) {
						String[] marriage = { msgArr[1], msgArr[2] };
						marriages.add(marriage);
					} else if (msgArr[1] == name) {
						String[] marriage = { msgArr[0], msgArr[2] };
						marriages.add(marriage);
					}
				} else if (msg.startsWith("DV")) {
					String[] msgArr = msg.substring(2).split("|");
					if (msgArr[0] == name) {
						String[] divorce = { msgArr[1], trans.getTimestamp() + "" };
						divorces.add(divorce);
					} else if (msgArr[1] == name) {
						String[] divorce = { msgArr[0], trans.getTimestamp() + "" };
						divorces.add(divorce);
					}
				}
			}
		}

		if (personalDetails != null) {
			System.out.println("Personal Details:");
			System.out.println("Firstname: " + personalDetails[1]);
			System.out.println("Lastname: " + personalDetails[0]);
			System.out.println("Birthday: " + personalDetails[2]);
			System.out.println("Address: " + personalDetails[3]);
			System.out.println("Mother: " + personalDetails[4]);
			System.out.println("Father: " + personalDetails[5]);
			System.out.println("\n-----------------------------------\n");
			System.out.println("Addresses:");
			for (String address : addresses) {
				System.out.println(address);
			}
			System.out.println("\n-----------------------------------\n");
			System.out.println("Marriages:");
			for (String[] marriage : marriages) {
				System.out.println(marriage[1] + ": " + marriage[0]);
			}
			System.out.println("\n-----------------------------------\n");
			System.out.println("Divorces:");
			for (String[] divorce : divorces) {
				System.out.println(divorce[1] + ": " + divorce[0]);
			}
			System.out.println("\n-----------------------------------\n");
			System.out.println("Children:");
			for (String[] child : children) {
				System.out.println(child[1] + ": " + child[0]);
			}
			System.out.println("\n-----------------------------------\n");
			System.out.println("Declared Death:");
			for (String death : declaredDeaths) {
				System.out.println(death);
			}
			System.out.println("\n-----------------------------------\n");
			System.out.println("Declared alive:");
			for (String dateAlive : declaredAlive) {
				System.out.println(dateAlive);
			}
		}
	}

	/**
	 * showBirthDeathRate
	 *
	 * @description show birth death rate per year in a given city
	 * @param city
	 * @throws Exception
	 */
	private static void showBirthDeathRate(String city) throws Exception {
		String protocol = sslEnabled ? "https" : "http";
		RestTemplate restTemplate = new RestTemplate();
		ObjectMapper mapper = new ObjectMapper();
		mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
		TypeReference<List<Block>> typeReference = new TypeReference<List<Block>>() {
		};
		String url = protocol + "://" + serverAddr + ":" + serverPort + "/transaction";
		HttpEntity<TypeReference<List<Block>>> request = new HttpEntity<>(typeReference);
		ResponseEntity<Resource> responseEntity = restTemplate.exchange(url, HttpMethod.GET, request, Resource.class);
		InputStream responseInputStream = responseEntity.getBody().getInputStream();
		GZIPInputStream zStream = new GZIPInputStream(responseInputStream);
		List<Block> blocks = mapper.readValue(zStream, typeReference);
		HashMap<Integer, Integer[]> yearsMap = new HashMap<>();

		for (Block block : blocks) {
			for (Transaction trans : block.getTransactions()) {
				String msg = trans.getText();
				if (msg.startsWith("AD")) {
					String[] msgArr = msg.substring(2).split("|");
					if (msgArr[3].contains(city)) {
						Integer year = Integer.getInteger(msgArr[2].substring(0, 4));
						if (!yearsMap.containsKey(year)) {
							Integer[] birthDeathRate = { 1, 0 };
							yearsMap.put(year, birthDeathRate);
						} else {
							Integer[] birthDeathRate = yearsMap.get(year);
							birthDeathRate[0]++;
							yearsMap.put(year, birthDeathRate);
						}
					}
				}
				if (msg.startsWith("DH")) {
					for (Block block2 : blocks) {
						for (Transaction trans2 : block.getTransactions()) {
							String msg2 = trans.getText();
							String[] msgArr2 = msg2.substring(2).split("|");
							if (msgArr2[3].contains(city)) {
								String[] msgArr = msg.substring(2).split("|");
								Integer yearOfDeath = Integer.getInteger(msgArr[2].substring(0, 4));
								Integer[] birthDeathRate = yearsMap.get(yearOfDeath);
								birthDeathRate[1]++;
								yearsMap.put(yearOfDeath, birthDeathRate);
							}
						}
					}
				}
			}
		}

		System.out.println("Birth-/Death-rate of " + city + ":");
		System.out.println("-----------------------------------\n");
		for (Integer year : yearsMap.keySet()) {
			Integer[] birthDeathRate = yearsMap.get(year);
			System.out.println(String.format("%i: %i were born, %i died.", year, birthDeathRate[0], birthDeathRate[1]));
		}
	}
}
