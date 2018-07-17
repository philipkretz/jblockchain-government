package de.pk.jblockchain.node.service;

import java.security.GeneralSecurityException;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import de.pk.jblockchain.common.domain.Block;
import de.pk.jblockchain.common.domain.Transaction;

@Service
public class MiningService implements Runnable {

	private final static Logger LOG = LoggerFactory.getLogger(MiningService.class);

	private final TransactionService transactionService;
	private final NodeService nodeService;
	private final BlockService blockService;

	private AtomicBoolean runMiner = new AtomicBoolean(false);

	@Value("${blockchain.mining.maxTransactionsPerBlock}")
	private int maxTransactionsPerBlock;

	@Value("${blockchain.mining.difficulty}")
	private int miningDifficulty;

	@Autowired
	public MiningService(TransactionService transactionService, NodeService nodeService, BlockService blockService) {
		this.transactionService = transactionService;
		this.nodeService = nodeService;
		this.blockService = blockService;
	}

	/**
	 * Start the miner
	 */
	public void startMiner() {
		if (runMiner.compareAndSet(false, true)) {
			LOG.info("Starting miner");
			Thread thread = new Thread(this);
			thread.start();
		}
	}

	/**
	 * Stop the miner after next iteration
	 */
	public void stopMiner() {
		LOG.info("Stopping miner");
		runMiner.set(false);
	}

	/**
	 * Loop for new blocks until someone signals to stop
	 */
	@Override
	public void run() {
		while (runMiner.get()) {
			try {
				LOG.info("Collecting transactions...");
				Block block = mineBlock();
				if (block != null) {
					// Found block! Append and publish
					LOG.info("Mined block with " + block.getTransactions().size() + " transactions and nonce "
							+ block.getTries());
					blockService.append(block);
					nodeService.broadcastPut("block", block);
				}
			} catch (GeneralSecurityException e) {
				LOG.error("GeneralSecurityException for SCrypt hashing occured: ", e);
			}
		}
		LOG.info("Miner stopped");
	}

	private Block mineBlock() {
		long tries = 0;

		// get previous hash and transactions
		byte[] previousBlockHash = blockService.getLastBlock() != null ? blockService.getLastBlock().getHash() : null;
		List<Transaction> transactions = transactionService.getTransactionPool().stream().limit(maxTransactionsPerBlock)
				.collect(Collectors.toList());
		if (transactions.size() > 0) {
			LOG.info("has Transactions:");
			for (Transaction trans : transactions) {
				LOG.info(trans.getText());
			}
		}

		// sleep if no more transactions left
		if (transactions.isEmpty()) {
			LOG.info("No transactions available, pausing");
			try {
				Thread.sleep(10000);
			} catch (InterruptedException e) {
				LOG.error("Thread interrupted", e);
			}
			return null;
		}

		// try new block until difficulty is sufficient
		while (runMiner.get()) {
			try {
				LOG.info("tries increased to " + tries);
				Block block = new Block(previousBlockHash, transactions, tries);
				if (block.getLeadingZerosCount() >= miningDifficulty) {
					return block;
				}
				tries++;
			} catch (GeneralSecurityException e) {
				LOG.error("GeneralSecurityException for SCrypt hashing occured: ", e);
			}

		}
		return null;
	}

}
