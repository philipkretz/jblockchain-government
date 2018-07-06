package de.pk.jblockchain.client;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Properties;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionGroup;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.codec.binary.Base64;
import org.springframework.web.client.RestTemplate;

import de.pk.jblockchain.common.SignatureUtils;
import de.pk.jblockchain.common.domain.Address;
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

	public static void main(String args[]) throws Exception {
		Properties props = new Properties();
		// System.out.println(BlockchainClient.class.getResource("/."));
		props.load(BlockchainClient.class.getResourceAsStream("/application.properties"));
		serverAddr = props.getProperty("server.address");
		serverPort = props.getProperty("server.port");
		sslEnabled = Boolean.valueOf(props.getProperty("server.ssl.enabled"));

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
			publishAddress(Paths.get(publickey));

		} else if (line.hasOption("transaction")) {
			String message = line.getOptionValue("message");
			String sender = line.getOptionValue("sender");
			String privatekey = line.getOptionValue("privatekey");
			if (message == null || sender == null || privatekey == null) {
				throw new ParseException("message, sender and privatekey is required");
			}
			publishTransaction(Paths.get(privatekey), message, Base64.decodeBase64(sender));
		} else if (line.hasOption("add-citizen")) {
			// TODO: implementieren
			String sender = line.getOptionValue("sender");
			String privatekey = line.getOptionValue("privatekey");
			String firstName = line.getOptionValue("firstName");
			String lastName = line.getOptionValue("lastName");
			DateFormat format = new SimpleDateFormat("YYYYMMdd", Locale.ENGLISH);
			Date birthday = format.parse(line.getOptionValue("birthday"));
			String address = line.getOptionValue("address");
			String mother = line.getOptionValue("mother");
			String father = line.getOptionValue("father");
			addCitizen(Paths.get(privatekey), Base64.decodeBase64(sender), firstName, lastName, birthday, address,
					mother, father);
		} else if (line.hasOption("marriage")) {
			// TODO: implementieren
			String sender = line.getOptionValue("sender");
			String privatekey = line.getOptionValue("privatekey");
			String person1 = line.getOptionValue("person1");
			String person2 = line.getOptionValue("person2");
			DateFormat format = new SimpleDateFormat("YYYYMMdd", Locale.ENGLISH);
			Date date = format.parse(line.getOptionValue("date"));
			marriage(Paths.get(privatekey), Base64.decodeBase64(sender), person1, person2, date);
		} else if (line.hasOption("divorce")) {
			// TODO: implementieren
			String sender = line.getOptionValue("sender");
			String privatekey = line.getOptionValue("privatekey");
			String person1 = line.getOptionValue("person1");
			String person2 = line.getOptionValue("person2");
			divorce(Paths.get(privatekey), Base64.decodeBase64(sender), person1, person2);
		} else if (line.hasOption("declare-death")) {
			// TODO: implementieren
			String sender = line.getOptionValue("sender");
			String privatekey = line.getOptionValue("privatekey");
			String firstName = line.getOptionValue("firstName");
			String lastName = line.getOptionValue("lastName");
			DateFormat format = new SimpleDateFormat("YYYYMMdd", Locale.ENGLISH);
			Date date = format.parse(line.getOptionValue("date"));
			declareDeath(Paths.get(privatekey), Base64.decodeBase64(sender), firstName, lastName, date);
		} else if (line.hasOption("declare-alive")) {
			// TODO: implementieren
			String sender = line.getOptionValue("sender");
			String privatekey = line.getOptionValue("privatekey");
			String firstName = line.getOptionValue("firstName");
			String lastName = line.getOptionValue("lastName");
			declareAlive(Paths.get(privatekey), Base64.decodeBase64(sender), firstName, lastName);
		} else if (line.hasOption("add-city")) {
			// TODO: implementieren
			String sender = line.getOptionValue("sender");
			String privatekey = line.getOptionValue("privatekey");
			String city = line.getOptionValue("city");
			addCity(Paths.get(privatekey), Base64.decodeBase64(sender), city);
		} else if (line.hasOption("add-street")) {
			// TODO: implementieren
			String sender = line.getOptionValue("sender");
			String privatekey = line.getOptionValue("privatekey");
			String city = line.getOptionValue("city");
			String street = line.getOptionValue("street");
			addStreet(Paths.get(privatekey), Base64.decodeBase64(sender), city, street);
		} else if (line.hasOption("add-house")) {
			// TODO: implementieren
			String sender = line.getOptionValue("sender");
			String privatekey = line.getOptionValue("privatekey");
			String street = line.getOptionValue("street");
			Integer houseNr = Integer.valueOf(line.getOptionValue("houseNr"));
			addHouse(Paths.get(privatekey), Base64.decodeBase64(sender), street, houseNr);
		} else if (line.hasOption("set-address")) {
			// TODO: implementieren
			String sender = line.getOptionValue("sender");
			String privatekey = line.getOptionValue("privatekey");
			String firstName = line.getOptionValue("firstName");
			String lastName = line.getOptionValue("lastName");
			String address = line.getOptionValue("address");
			setAddress(Paths.get(privatekey), Base64.decodeBase64(sender), firstName, lastName, address);
		} else if (line.hasOption("show-cities")) {
			// TODO: implementieren
			showCities();
		} else if (line.hasOption("show-citizens")) {
			// TODO: implementieren
			String city = line.getOptionValue("city");
			showCitizens(city);
		} else if (line.hasOption("show-citizen-details")) {
			// TODO: implementieren
			String city = line.getOptionValue("city");
			String name = line.getOptionValue("name");
			showCitizenDetails(city, name);
		} else if (line.hasOption("show-birth-death-rate")) {
			// TODO: implementieren
			String city = line.getOptionValue("city");
			showBirthDeathRate(city);
		}

		// TODO: für zusätzliche Kommandos erweitern
	}

	private static Options getOptions() {
		OptionGroup actions = new OptionGroup();
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
		Files.write(Paths.get("key.priv"), keyPair.getPrivate().getEncoded());
		Files.write(Paths.get("key.pub"), keyPair.getPublic().getEncoded());
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
		restTemplate.getForEntity(protocol + "://" + serverAddr + ":" + serverPort + "/start-miner", String.class);
		System.out.println("Started mining process");
	}

	private static void stopMiner() throws Exception {
		String protocol = sslEnabled ? "https" : "http";
		RestTemplate restTemplate = new RestTemplate();
		restTemplate.getForEntity(protocol + "://" + serverAddr + ":" + serverPort + "/stop-miner", String.class);
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

	// TODO: nach Aufgabenstellung die Methode publishTransaction mit eigenem
	// Inhalt für message erweitern.
	// Kommandos verknüpfen und aufrufen, wie es oben schon teilweise
	// implementiert wurde.

	// TODO: einen neuen Bürger der Blockchain hinzufügen
	private static void addCitizen(Path privateKey, byte[] senderHash, String firstName, String lastName, Date birthday,
			String address, String mother, String father) throws Exception {

		// Format: Nachname|Vorname|Geburtstag|Adresse|Mutter|Vater
		String message = "AD{lastName}|{firstName}|{birthday}|{address}|{mother}|{father}";
		publishTransaction(privateKey, message, senderHash);
	}

	// TODO: eine Eheschließung eintragen
	private static void marriage(Path privateKey, byte[] senderHash, String person1, String person2, Date date)
			throws Exception {
		String message = "MR{person1}|{person2}|{date}";
		publishTransaction(privateKey, message, senderHash);
	}

	// TODO: eine Scheidung vermerken
	private static void divorce(Path privateKey, byte[] senderHash, String person1, String person2) throws Exception {
		String message = "DV{person1}|{person2}";
		publishTransaction(privateKey, message, senderHash);
	}

	// TODO: den Tod eines Bürgers erklären
	private static void declareDeath(Path privateKey, byte[] senderHash, String firstName, String lastName, Date date)
			throws Exception {
		String message = "DH{firstName}|{lastName}|{date}";
		publishTransaction(privateKey, message, senderHash);
	}

	// TODO: dem Tod eines Bürgers widersprechen
	private static void declareAlive(Path privateKey, byte[] senderHash, String firstName, String lastName)
			throws Exception {
		String message = "DA{firstName}|{lastName}";
		publishTransaction(privateKey, message, senderHash);
	}

	// TODO: eine Stadt hinzufügen
	private static void addCity(Path privateKey, byte[] senderHash, String name) throws Exception {
		String message = "AC{name}";
		publishTransaction(privateKey, message, senderHash);
	}

	// TODO: eine Straße hinzufügen
	private static void addStreet(Path privateKey, byte[] senderHash, String city, String name) throws Exception {
		String message = "AS{city}|{name}";
		publishTransaction(privateKey, message, senderHash);
	}

	// TODO: ein Haus zu einer Straße hinzufügen
	private static void addHouse(Path privateKey, byte[] senderHash, String street, Integer houseNr) throws Exception {
		String message = "AH{street}|{houseNr}";
		publishTransaction(privateKey, message, senderHash);
	}

	// TODO: Hauptwohnsitz eines Bürgers ändern
	private static void setAddress(Path privateKey, byte[] senderHash, String firstName, String lastName,
			String address) throws Exception {
		String message = "SD{firstName}|{lastName}|{address}";
		publishTransaction(privateKey, message, senderHash);
	}

	// TODO: alle Bürger einer Stadt alphabetisch sortiert auflisten
	private static void showCities() throws Exception {

	}

	// TODO: alle Bürger einer Stadt alphabetisch sortiert auflisten
	private static void showCitizens(String city) throws Exception {

	}

	// TODO: alle gespeicherten Details über einen Bürger ausgeben
	private static void showCitizenDetails(String city, String name) throws Exception {

	}

	// Geburten- und Sterberate nach Jahr darstellen
	private static void showBirthDeathRate(String city) throws Exception {

	}
}
