package com.nc.gs.tests.serializers.graphs.ic.pvt;

public abstract class Node {

	private int id;

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!(obj instanceof Node))
			return false;
		Node other = (Node) obj;
		if (getId() != other.getId())
			return false;
		return true;
	}

	public int getId() {
		return id;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + getId();
		return result;
	}

	public void setId(int id) {
		this.id = id;
	}

}
