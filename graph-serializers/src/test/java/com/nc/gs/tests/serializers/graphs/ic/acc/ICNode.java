package com.nc.gs.tests.serializers.graphs.ic.acc;

import com.nc.gs.meta.Hierarchy;

public class ICNode extends Node {

	@Hierarchy(types = { BlackNode.class, SimpleNode.class })
	Node left;

	@Hierarchy(types = { RedNode.class, SimpleNode.class })
	Node right;

	public Node getLeft() {
		return left;
	}

	public Node getRight() {
		return right;
	}

	public void setLeft(Node left) {
		this.left = left;
	}

	public void setRight(Node right) {
		this.right = right;
	}

}
