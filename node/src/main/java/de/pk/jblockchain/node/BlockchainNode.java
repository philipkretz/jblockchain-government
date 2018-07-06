package de.pk.jblockchain.node;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.zip.GZIPInputStream;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import de.pk.jblockchain.common.domain.Address;
import de.pk.jblockchain.common.domain.Block;
import de.pk.jblockchain.common.domain.Node;
import de.pk.jblockchain.common.domain.Transaction;
import de.pk.jblockchain.node.service.AddressService;
import de.pk.jblockchain.node.service.BlockService;
import de.pk.jblockchain.node.service.MiningService;
import de.pk.jblockchain.node.service.NodeService;
import de.pk.jblockchain.node.service.TransactionService;

@SpringBootApplication
public class BlockchainNode {

	@Value("${storage.path}")
	private String storePath;

	@Autowired
	private static MiningService miningService;

	public static void main(String[] args) {
		SpringApplication.run(BlockchainNode.class, args);
		miningService.startMiner();
		System.out.println("Mining process started!");
	}

	private void createDirectories() throws IOException {
		java.nio.file.Path p = Files.createDirectories(Paths.get(System.getProperty("user.home") + this.storePath));
		if (!p.toFile().exists()) {
			System.out.println("Could not create path " + p.toString());
		}
	}

	@Bean
	CommandLineRunner runAddressService(AddressService addressService) {
		return args -> {
			// read json
			ObjectMapper mapper = new ObjectMapper();
			mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
			TypeReference<Map<String, Address>> typeReference = new TypeReference<Map<String, Address>>() {
			};
			try {
				createDirectories();
				System.out.println("Rootpath=" + BlockchainNode.class.getResource(this.storePath + "."));
				FileInputStream fStream = new FileInputStream(
						System.getProperty("user.home") + this.storePath + "address.json.gz");
				GZIPInputStream zStream = new GZIPInputStream(new BufferedInputStream(fStream));
				Map<String, Address> addresses = mapper.readValue(zStream, typeReference);
				addressService.init(addresses);
			} catch (IOException e) {
				System.out.println("Unable to load addresses: " + e.getMessage());
			}
		};
	}

	@Bean
	CommandLineRunner runBlockService(BlockService blockService) {
		return args -> {
			// read json
			ObjectMapper mapper = new ObjectMapper();
			mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
			TypeReference<List<Block>> typeReference = new TypeReference<List<Block>>() {
			};
			try {
				createDirectories();
				FileInputStream fStream = new FileInputStream(
						System.getProperty("user.home") + this.storePath + "blockchain.json.gz");
				GZIPInputStream zStream = new GZIPInputStream(new BufferedInputStream(fStream));
				List<Block> blocks = mapper.readValue(zStream, typeReference);
				blockService.init(blocks);
			} catch (IOException e) {
				System.out.println("Unable to load blocks: " + e.getMessage());
			}
		};
	}

	@Bean
	CommandLineRunner runNodeService(NodeService nodeService) {
		return args -> {
			// read json
			ObjectMapper mapper = new ObjectMapper();
			mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
			TypeReference<Set<Node>> typeReference = new TypeReference<Set<Node>>() {
			};
			try {
				createDirectories();
				FileInputStream fStream = new FileInputStream(
						System.getProperty("user.home") + this.storePath + "node.json.gz");
				GZIPInputStream zStream = new GZIPInputStream(new BufferedInputStream(fStream));
				Set<Node> nodes = mapper.readValue(zStream, typeReference);
				nodeService.init(nodes);
			} catch (IOException e) {
				System.out.println("Unable to load nodes: " + e.getMessage());
			}
		};
	}

	@Bean
	CommandLineRunner runTransactionService(TransactionService transactionService) {
		return args -> {
			// read json
			ObjectMapper mapper = new ObjectMapper();
			mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
			TypeReference<Set<Transaction>> typeReference = new TypeReference<Set<Transaction>>() {
			};
			try {
				createDirectories();
				FileInputStream fStream = new FileInputStream(
						System.getProperty("user.home") + this.storePath + "transaction.json.gz");
				GZIPInputStream zStream = new GZIPInputStream(new BufferedInputStream(fStream));
				Set<Transaction> transactions = mapper.readValue(zStream, typeReference);
				transactionService.init(transactions);
			} catch (IOException e) {
				System.out.println("Unable to load transactions: " + e.getMessage());
			}
		};
	}
}
