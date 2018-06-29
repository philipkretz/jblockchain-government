package de.pk.jblockchain.node.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import de.pk.jblockchain.common.SignatureUtils;
import de.pk.jblockchain.common.domain.Address;
import de.pk.jblockchain.common.domain.Block;
import de.pk.jblockchain.common.domain.Transaction;

@RunWith(SpringRunner.class)
@SpringBootTest
public class BlockServiceTests {
	private final static Logger LOG = LoggerFactory.getLogger(BlockServiceTests.class);

	private final static byte[] fixedSignature = new byte[] { 48, 44, 2, 20, 89, 48, -114, -49, 36, 65, 116, -5, 88, 6,
			-38, -110, -30, -73, 59, -53, 19, -49, 122, 90, 2, 20, 111, 38, 55, -120, -125, 17, -66, -8, -121, 85, 31,
			-82, -80, -31, -33, 116, 121, -90, 123, -113 };

	@Autowired
	private BlockService blockService;
	@Autowired
	private TransactionService transactionService;
	@Autowired
	private AddressService addressService;

	private Address address;
	private byte[] privateKey;

	@Before
	public void setUp() throws Exception {
		privateKey = IOUtils
				.toByteArray(Thread.currentThread().getContextClassLoader().getResourceAsStream("key.priv"));
		byte[] publicKey = IOUtils
				.toByteArray(Thread.currentThread().getContextClassLoader().getResourceAsStream("key.pub"));
		address = new Address(publicKey);
		addressService.add(address);
	}

	@Test
	public void addBlock_validHash() throws Exception {
		long trials = 8908926L;
		Block block = new Block(null, Collections.singletonList(generateStableTransaction()), trials);
		block.setTimestamp(42);
		block.setHash(block.calculateHash());
		Assert.assertTrue(blockService.append(block));
	}

	@Test
	public void addBlock_invalidHash() throws Exception {
		Block block = new Block(null, generateTransactions(1), 42);
		boolean success = blockService.append(block);
		Assert.assertFalse(success);
	}

	@Test
	public void addBlock_invalidLimitExceeded() throws Exception {
		Block block = new Block(null, generateTransactions(6), 42);
		boolean success = blockService.append(block);
		Assert.assertFalse(success);
	}

	@Ignore
	@Test
	public void addBlock_generateBlock() throws Exception {
		List<Transaction> transactions = Collections.singletonList(generateStableTransaction());
		boolean success = false;
		long nonce = 8908926L;
		while (!success) {
			Block block = new Block(null, transactions, nonce);
			block.setTimestamp(42); // need stable hash
			block.setHash(block.calculateHash());
			success = blockService.append(block);
			nonce++;
		}
	}

	private List<Transaction> generateTransactions(int count) throws Exception {
		List<Transaction> transactions = new ArrayList<>();
		for (int i = 0; i < count; i++) {
			String text = "Hello " + i;
			byte[] signature = SignatureUtils.sign(text.getBytes(), privateKey);
			Transaction transaction = new Transaction(text, address.getHash(), signature);

			transactionService.add(transaction);
			transactions.add(transaction);
		}
		return transactions;
	}

	private Transaction generateStableTransaction() {
		String text = "Hello 0";
		Transaction transaction = new Transaction(text, address.getHash(), fixedSignature);
		transaction.setTimestamp(42); // need stable hash
		transaction.setHash(transaction.calculateHash());

		transactionService.add(transaction);
		return transaction;
	}

}
