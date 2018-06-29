package de.pk.jblockchain.node.rest;

import java.security.GeneralSecurityException;
import java.util.List;

import javax.servlet.http.HttpServletResponse;

import org.apache.commons.codec.binary.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import de.pk.jblockchain.common.domain.Block;
import de.pk.jblockchain.node.service.BlockService;
import de.pk.jblockchain.node.service.MiningService;
import de.pk.jblockchain.node.service.NodeService;

@RestController
@RequestMapping("block")
public class BlockController {

	private static final Logger LOG = LoggerFactory.getLogger(BlockController.class);

	private final BlockService blockService;
	private final NodeService nodeService;
	private final MiningService miningService;

	@Autowired
	public BlockController(BlockService blockService, NodeService nodeService, MiningService miningService) {
		this.blockService = blockService;
		this.nodeService = nodeService;
		this.miningService = miningService;
	}

	/**
	 * Retrieve all Blocks in order of mine date, also known as Blockchain
	 *
	 * @return JSON list of Blocks
	 */
	@RequestMapping
	List<Block> getBlockchain() {
		return blockService.getBlockchain();
	}

	/**
	 * Add a new Block at the end of the Blockchain. It is expected that the
	 * Block is valid, see BlockService.verify(Block) for details.
	 *
	 * @param block
	 *            the Block to add
	 * @param publish
	 *            if true, this Node is going to inform all other Nodes about
	 *            the new Block
	 * @param response
	 *            Status Code 202 if Block accepted, 406 if verification fails
	 */
	@RequestMapping(method = RequestMethod.PUT)
	void addBlock(@RequestBody Block block, @RequestParam(required = false) Boolean publish,
			HttpServletResponse response) {
		LOG.info("Add block " + Base64.encodeBase64String(block.getHash()));
		try {
			boolean success = blockService.append(block);

			if (success) {
				response.setStatus(HttpServletResponse.SC_ACCEPTED);

				if (publish != null && publish) {
					nodeService.broadcastPut("block", block);
				}
			} else {
				response.setStatus(HttpServletResponse.SC_NOT_ACCEPTABLE);
			}
		} catch (GeneralSecurityException e) {
			LOG.error("GeneralSecurityException for SCrypt hashing occured: ", e);
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
		}
	}

	/**
	 * Start mining of Blocks on this Node in a Thread
	 */
	@RequestMapping(path = "start-miner")
	public void startMiner() {
		miningService.startMiner();
	}

	/**
	 * Stop mining of Blocks on this Node
	 */
	@RequestMapping(path = "stop-miner")
	public void stopMiner() {
		miningService.stopMiner();
	}

}
