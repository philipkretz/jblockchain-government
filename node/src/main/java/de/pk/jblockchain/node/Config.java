package de.pk.jblockchain.node;

public abstract class Config {

	/**
	 * Address of a Node to use for initialization
	 */
	public static final String[] MASTER_NODE_ADDRESSES = { "http://localhost:21986",
			// TODO: add addresses of fully trustable hosts
			// for masternode backbone network here...
	};

	/**
	 * Minimum number of leading zeros every block hash has to fulfill
	 */
	public static final int DIFFICULTY = 2;

	/**
	 * Maximum number of Transactions a Block can hold
	 */
	public static final int MAX_TRANSACTIONS_PER_BLOCK = 100;

}
