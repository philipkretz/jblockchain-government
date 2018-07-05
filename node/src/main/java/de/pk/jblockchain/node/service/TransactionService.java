package de.pk.jblockchain.node.service;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.codec.binary.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.ObjectMapper;

import de.pk.jblockchain.common.SignatureUtils;
import de.pk.jblockchain.common.domain.Address;
import de.pk.jblockchain.common.domain.Node;
import de.pk.jblockchain.common.domain.Transaction;

@Service
public class TransactionService {

	private final static Logger LOG = LoggerFactory.getLogger(TransactionService.class);

	private final AddressService addressService;

	/**
	 * Pool of Transactions which are not included in a Block yet.
	 */
	private Set<Transaction> transactionPool = new HashSet<>();

	@Autowired
	public TransactionService(AddressService addressService) {
		this.addressService = addressService;
	}

	public Set<Transaction> getTransactionPool() {
		return transactionPool;
	}

	/**
	 * load initial values
	 *
	 * @param Set<Transaction>
	 */
	public void init(Set<Transaction> transactions) {
		this.transactionPool = transactions;
	}

	/**
	 * save values
	 *
	 */
	public void save() {
		ObjectMapper mapper = new ObjectMapper();
		try {
			mapper.writeValue(new File("/store/transaction.json"), this.transactionPool);
			System.out.println("Transactions Saved!");
		} catch (IOException e) {
			System.out.println("Unable to save transactions: " + e.getMessage());
		}
	}

	/**
	 * Add a new Transaction to the pool
	 *
	 * @param transaction
	 *            Transaction to add
	 * @return true if verification succeeds and Transaction was added
	 */
	public synchronized boolean add(Transaction transaction) {
		if (verify(transaction)) {
			transactionPool.add(transaction);
			this.save();
			return true;
		}
		return false;
	}

	/**
	 * Remove Transaction from pool
	 *
	 * @param transaction
	 *            Transaction to remove
	 */
	public void remove(Transaction transaction) {
		transactionPool.remove(transaction);
		this.save();
	}

	/**
	 * Does the pool contain all given Transactions?
	 *
	 * @param transactions
	 *            Collection of Transactions to check
	 * @return true if all Transactions are member of the pool
	 */
	public boolean containsAll(Collection<Transaction> transactions) {
		return transactionPool.containsAll(transactions);
	}

	private boolean verify(Transaction transaction) {
		// correct signature
		Address sender = addressService.getByHash(transaction.getSenderHash());
		if (sender == null) {
			LOG.warn("Unknown address " + Base64.encodeBase64String(transaction.getSenderHash()));
			return false;
		}

		try {
			if (!SignatureUtils.verify(transaction.getSignableData(), transaction.getSignature(),
					sender.getPublicKey())) {
				LOG.warn("Invalid signature");
				return false;
			}
		} catch (Exception e) {
			LOG.error("Error while verification", e);
			return false;
		}

		// correct hash
		if (!Arrays.equals(transaction.getHash(), transaction.calculateHash())) {
			LOG.warn("Invalid hash");
			return false;
		}

		return true;
	}

	/**
	 * Download Transactions from other Node and them to the pool
	 *
	 * @param node
	 *            Node to query
	 * @param restTemplate
	 *            RestTemplate to use
	 */
	public void retrieveTransactions(Node node, RestTemplate restTemplate) {
		Transaction[] transactions = restTemplate.getForObject(node.getAddress() + "/transaction", Transaction[].class);
		Collections.addAll(transactionPool, transactions);
		LOG.info("Retrieved " + transactions.length + " transactions from node " + node.getAddress());
	}
}
