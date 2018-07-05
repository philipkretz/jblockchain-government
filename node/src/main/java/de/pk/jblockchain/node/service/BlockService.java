package de.pk.jblockchain.node.service;

import java.io.File;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.ObjectMapper;

import de.pk.jblockchain.common.domain.Block;
import de.pk.jblockchain.common.domain.Node;
import de.pk.jblockchain.node.Config;

@Service
public class BlockService {

	private final static Logger LOG = LoggerFactory.getLogger(BlockService.class);

	private final TransactionService transactionService;

	private List<Block> blockchain = new ArrayList<>();

	@Autowired
	public BlockService(TransactionService transactionService) {
		this.transactionService = transactionService;
	}

	public List<Block> getBlockchain() {
		return blockchain;
	}

	/**
	 * load initial values
	 *
	 * @param List<Block>
	 */
	public void init(List<Block> blockchain) {
		this.blockchain = blockchain;
	}

	/**
	 * save values
	 *
	 */
	public void save() {
		ObjectMapper mapper = new ObjectMapper();
		try {
			mapper.writeValue(new File("/store/blockchain.json"), this.blockchain);
			System.out.println("Blockchain Saved!");
		} catch (IOException e) {
			System.out.println("Unable to save blockchain: " + e.getMessage());
		}
	}

	/**
	 * Determine the last added Block
	 *
	 * @return Last Block in chain
	 */
	public Block getLastBlock() {
		if (blockchain.isEmpty()) {
			return null;
		}
		return blockchain.get(blockchain.size() - 1);
	}

	/**
	 * Append a new Block at the end of chain
	 *
	 * @param block
	 *            Block to append
	 * @return true if verifcation succeeds and Block was appended
	 * @throws GeneralSecurityException
	 */
	public synchronized boolean append(Block block) throws GeneralSecurityException {
		if (verify(block)) {
			blockchain.add(block);
			this.save();

			// remove transactions from pool
			block.getTransactions().forEach(transactionService::remove);
			return true;
		}
		return false;
	}

	/**
	 * Download Blocks from other Node and add them to the blockchain
	 *
	 * @param node
	 *            Node to query
	 * @param restTemplate
	 *            RestTemplate to use
	 */
	public void retrieveBlockchain(Node node, RestTemplate restTemplate) {
		Block[] blocks = restTemplate.getForObject(node.getAddress() + "/block", Block[].class);
		Collections.addAll(blockchain, blocks);
		LOG.info("Retrieved " + blocks.length + " blocks from node " + node.getAddress());
	}

	private boolean verify(Block block) throws GeneralSecurityException {
		// references last block in chain
		if (blockchain.size() > 0) {
			byte[] lastBlockInChainHash = getLastBlock().getHash();
			if (!Arrays.equals(block.getPreviousBlockHash(), lastBlockInChainHash)) {
				return false;
			}
		} else {
			if (block.getPreviousBlockHash() != null) {
				return false;
			}
		}

		// correct hashes
		if (!Arrays.equals(block.getMerkleRoot(), block.calculateMerkleRoot())) {
			return false;
		}
		if (!Arrays.equals(block.getHash(), block.calculateHash())) {
			return false;
		}

		// transaction limit
		if (block.getTransactions().size() > Config.MAX_TRANSACTIONS_PER_BLOCK) {
			return false;
		}

		// all transactions in pool
		if (!transactionService.containsAll(block.getTransactions())) {
			return false;
		}

		// considered difficulty
		return block.getLeadingZerosCount() >= Config.DIFFICULTY;
	}
}
