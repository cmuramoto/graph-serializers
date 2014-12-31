package com.nc.gs.tests.serializers.graphs.ic.acc;

public class RedNode extends Node {

	int a;

	long b;

	public RedNode(int a, long b) {
		super();
		this.a = a;
		this.b = b;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		RedNode other = (RedNode) obj;
		if (a != other.a)
			return false;
		if (b != other.b)
			return false;
		return true;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + a;
		result = prime * result + (int) (b ^ (b >>> 32));
		return result;
	}

}
