package de.pk.jblockchain.node.service.smartContract;

public abstract class AbstractSmartContract {

	public abstract String getAbbreviation();

	protected abstract Boolean checkSyntax(String message);

	protected abstract Boolean checkSemantic(String message);

	public final Boolean checkContract(String message) {
		return (checkSyntax(message) && checkSemantic(message));
	}
}
