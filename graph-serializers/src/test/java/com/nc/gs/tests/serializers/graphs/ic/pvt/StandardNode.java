package com.nc.gs.tests.serializers.graphs.ic.pvt;

public class StandardNode extends Node {

	private Node left;

	private Node right;

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		StandardNode other = (StandardNode) obj;
		if (getLeft() == null) {
			if (other.getLeft() != null)
				return false;
		} else if (!getLeft().equals(other.getLeft()))
			return false;
		if (getRight() == null) {
			if (other.getRight() != null)
				return false;
		} else if (!getRight().equals(other.getRight()))
			return false;
		return true;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((getLeft() == null) ? 0 : getLeft().hashCode());
		result = prime * result + ((getRight() == null) ? 0 : getRight().hashCode());
		return result;
	}

	public Node getLeft() {
		return left;
	}

	public void setLeft(Node left) {
		this.left = left;
	}

	public Node getRight() {
		return right;
	}

	public void setRight(Node right) {
		this.right = right;
	}

}
