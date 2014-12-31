package com.nc.gs.tests.generator.ic.mixed.overload;

import com.nc.gs.meta.Hierarchy;
import com.nc.gs.meta.NotNull;
import com.nc.gs.meta.OnlyPayload;

public class Node {

	@NotNull
	@Hierarchy(types = { Node.class, RedNode.class, BlackNode.class },
			   complete = false)
	Node predecessor;

	@NotNull
	@Hierarchy(types = { Node.class, RedNode.class, BlackNode.class })
	Node successor;

	@NotNull
	@Hierarchy(types = { RedNode.class, BlueNode.class })
	Node left;

	@NotNull
	@Hierarchy(types = { BlackNode.class })
	Node right;

	@NotNull
	@OnlyPayload
	@Hierarchy(types = { Node.class, RedNode.class, BlackNode.class })
	Node master;

	@NotNull
	@OnlyPayload
	@Hierarchy(types = { Node.class, RedNode.class, BlackNode.class })
	Node slave;

}
