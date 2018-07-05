package de.pk.jblockchain.node.service;

import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;

import java.security.KeyPair;

import org.junit.Assert;
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
public class TransactionServiceTests {

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
	public void addTransaction_valid() throws Exception {
		String text = "Lorem Ipsum";
		byte[] signature = SignatureUtils.sign(text.getBytes(), keyPair.getPrivate().getEncoded());
		Transaction transaction = new Transaction(text, address.getHash(), signature);

		boolean success = transactionService.add(transaction);
		Assert.assertTrue(success);
	}

	@Test
	public void addTransaction_invalidText() throws Exception {
		String text = "Lorem Ipsum";
		byte[] signature = SignatureUtils.sign(text.getBytes(), keyPair.getPrivate().getEncoded());
		Transaction transaction = new Transaction("Fake text!!!", address.getHash(), signature);

		boolean success = transactionService.add(transaction);
		Assert.assertFalse(success);
	}

	@Test
	public void addTransaction_invalidSender() throws Exception {
		Address addressPresident = new Address(SignatureUtils.generateKeyPair().getPublic().getEncoded());
		addressService.add(addressPresident);

		String text = "Lorem Ipsum";
		byte[] signature = SignatureUtils.sign(text.getBytes(), keyPair.getPrivate().getEncoded());
		Transaction transaction = new Transaction(text, addressPresident.getHash(), signature);

		boolean success = transactionService.add(transaction);
		Assert.assertFalse(success);
	}
}
