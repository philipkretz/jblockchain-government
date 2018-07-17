package de.pk.jblockchain.node.service.smartContract.custom;

import de.pk.jblockchain.node.service.smartContract.AbstractSmartContract;

public class CitizenContract extends AbstractSmartContract {

	@Override
	public String getAbbreviation() {
		return "AD";
	}

	@Override
	protected Boolean checkSyntax(String message) {
		return message.substring(getAbbreviation().length())
				.matches("^[A-Za-z- .]+\\|[A-Za-z- .]+\\|[0-9]{8}\\|[A-Za-z0-9- .,]+\\|[A-Za-z- .]+\\|[A-Za-z- .]+$");
	}

	@Override
	protected Boolean checkSemantic(String message) {
		// TODO implement: validate all transactions and check if this message
		// content may be added...
		return true;
	}

}
