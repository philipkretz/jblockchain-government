package de.pk.jblockchain.node.rest;

import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import de.pk.jblockchain.common.domain.Node;
import de.pk.jblockchain.node.service.NodeService;

@RestController
@RequestMapping("node")
public class NodeController {

	private final static Logger LOG = LoggerFactory.getLogger(NodeController.class);

	private final NodeService nodeService;

	@Autowired
	public NodeController(NodeService nodeService) {
		this.nodeService = nodeService;
	}

	/**
	 * Get all Nodes this node knows
	 *
	 * @return JSON list of addresses
	 */
	@RequestMapping()
	Set<Node> getNodes() {
		return nodeService.getKnownNodes();
	}

	/**
	 * Add a new Node
	 *
	 * @param node
	 *            the Node to add
	 */
	@RequestMapping(method = RequestMethod.PUT)
	void addNode(@RequestBody Node node) {
		LOG.info("Add node " + node.getAddress());
		nodeService.add(node);
	}

	/**
	 * Remove a Node
	 *
	 * @param node
	 *            the Node to remove
	 */
	@RequestMapping(path = "remove", method = RequestMethod.POST)
	void removeNode(@RequestBody Node node) {
		LOG.info("Remove node " + node.getAddress());
		nodeService.remove(node);
	}
}
