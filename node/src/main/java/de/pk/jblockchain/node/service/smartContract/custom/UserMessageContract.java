package de.pk.jblockchain.node.service.smartContract.custom;

import de.pk.jblockchain.node.service.smartContract.AbstractSmartContract;

public class UserMessageContract extends AbstractSmartContract {

	@Override
	public String getAbbreviation() {
		return "";
	}

	@Override
	protected Boolean checkSyntax(String message) {
		return true;
	}

	@Override
	protected Boolean checkSemantic(String message) {
		return true;
	}

}
