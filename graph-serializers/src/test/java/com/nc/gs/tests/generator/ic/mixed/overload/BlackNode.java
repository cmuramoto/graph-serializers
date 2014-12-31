package com.nc.gs.tests.generator.ic.mixed.overload;

import com.nc.gs.meta.Hierarchy;
import com.nc.gs.meta.OnlyPayload;

public class BlackNode extends Node {

	@Hierarchy(types = { Node.class, RedNode.class, BlackNode.class })
	Node predecessor;

	@Hierarchy(types = { Node.class, RedNode.class, BlackNode.class })
	Node successor;

	@Hierarchy(types = { Node.class, RedNode.class })
	Node left;

	@Hierarchy(types = { Node.class, RedNode.class })
	Node right;

	@Hierarchy(types = { Node.class, RedNode.class, BlackNode.class })
	Node master;

	@OnlyPayload
	@Hierarchy(types = { Node.class, RedNode.class, BlackNode.class })
	Node slave;

}
