package de.pk.jblockchain.node.service;

import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;

import java.security.KeyPair;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import de.pk.jblockchain.common.SignatureUtils;
import de.pk.jblockchain.common.domain.Address;
import de.pk.jblockchain.common.domain.Transaction;

@RunWith(SpringRunner.class)
@SpringBootTest
public class MiningServiceTests {

	@Autowired
	private MiningService miningService;
	@Autowired
	private TransactionService transactionService;
	@Autowired
	private AddressService addressService;

	private Address address;
	private KeyPair keyPair;

	@Before
	public void setUp() throws Exception {
		AddressService addressServiceMock = mock(AddressService.class);
		doNothing().when(addressServiceMock).save();
		TransactionService transactionServiceMock = mock(TransactionService.class);
		doNothing().when(transactionServiceMock).save();
		BlockService blockServiceMock = mock(BlockService.class);
		doNothing().when(blockServiceMock).save();
		NodeService nodeServiceMock = mock(NodeService.class);
		doNothing().when(nodeServiceMock).save();

		keyPair = SignatureUtils.generateKeyPair();
		address = new Address(keyPair.getPublic().getEncoded());
		addressService.add(address);
	}

	@Test
	public void startStopMiner() throws Exception {
		final int initalTransactions = 100;
		addTransactions(initalTransactions);

		miningService.startMiner();

		while (transactionService.getTransactionPool().size() == initalTransactions) {
			Thread.sleep(1000);
		}

		miningService.stopMiner();
	}

	private void addTransactions(int count) throws Exception {
		for (int i = 0; i < count; i++) {
			String text = "Demo Transaction " + i;
			byte[] signature = SignatureUtils.sign(text.getBytes(), keyPair.getPrivate().getEncoded());
			Transaction transaction = new Transaction(text, address.getHash(), signature);

			transactionService.add(transaction);
		}
	}

}
