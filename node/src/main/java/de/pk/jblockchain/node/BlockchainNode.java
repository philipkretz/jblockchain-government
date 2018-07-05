package de.pk.jblockchain.node;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import de.pk.jblockchain.common.domain.Address;
import de.pk.jblockchain.common.domain.Block;
import de.pk.jblockchain.common.domain.Node;
import de.pk.jblockchain.common.domain.Transaction;
import de.pk.jblockchain.node.service.AddressService;
import de.pk.jblockchain.node.service.BlockService;
import de.pk.jblockchain.node.service.NodeService;
import de.pk.jblockchain.node.service.TransactionService;

@SpringBootApplication
public class BlockchainNode {

	public static void main(String[] args) {
		SpringApplication.run(BlockchainNode.class, args);
	}

	@Bean
	CommandLineRunner runAddressService(AddressService addressService) {
		return args -> {
			// read json
			ObjectMapper mapper = new ObjectMapper();
			TypeReference<Map<String, Address>> typeReference = new TypeReference<Map<String, Address>>() {
			};
			InputStream inputStream = TypeReference.class.getResourceAsStream("/store/address.json");
			try {
				Map<String, Address> addresses = mapper.readValue(inputStream, typeReference);
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
			TypeReference<List<Block>> typeReference = new TypeReference<List<Block>>() {
			};
			InputStream inputStream = TypeReference.class.getResourceAsStream("/store/blockchain.json");
			try {
				List<Block> blocks = mapper.readValue(inputStream, typeReference);
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
			TypeReference<Set<Node>> typeReference = new TypeReference<Set<Node>>() {
			};
			InputStream inputStream = TypeReference.class.getResourceAsStream("/store/node.json");
			try {
				Set<Node> nodes = mapper.readValue(inputStream, typeReference);
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
			TypeReference<Set<Transaction>> typeReference = new TypeReference<Set<Transaction>>() {
			};
			InputStream inputStream = TypeReference.class.getResourceAsStream("/store/transaction.json");
			try {
				Set<Transaction> transactions = mapper.readValue(inputStream, typeReference);
				transactionService.init(transactions);
			} catch (IOException e) {
				System.out.println("Unable to load transactions: " + e.getMessage());
			}
		};
	}
}
