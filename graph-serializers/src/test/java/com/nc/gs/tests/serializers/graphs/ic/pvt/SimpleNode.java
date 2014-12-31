package com.nc.gs.tests.serializers.graphs.ic.pvt;

import com.nc.gs.meta.LeafNode;

@LeafNode
public class SimpleNode extends Node {

	private final String value;

	public SimpleNode(String value) {
		super();
		this.value = value;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		SimpleNode other = (SimpleNode) obj;
		if (value == null) {
			if (other.value != null)
				return false;
		} else if (!value.equals(other.value))
			return false;
		return true;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((value == null) ? 0 : value.hashCode());
		return result;
	}

}
