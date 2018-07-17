package de.pk.jblockchain.node.service.smartContract.custom;

import de.pk.jblockchain.node.service.smartContract.AbstractSmartContract;

public class DeclareAliveContract extends AbstractSmartContract {

	@Override
	public String getAbbreviation() {
		return "DA";
	}

	@Override
	protected Boolean checkSyntax(String message) {
		return message.substring(getAbbreviation().length()).matches("^[A-Za-z- .]+\\|[A-Za-z- .]+$");
	}

	@Override
	protected Boolean checkSemantic(String message) {
		// TODO implement: validate all transactions and check if this message
		// content may be added...
		return true;
	}

}
