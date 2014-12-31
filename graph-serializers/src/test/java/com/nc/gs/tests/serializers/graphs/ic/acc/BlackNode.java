package com.nc.gs.tests.serializers.graphs.ic.acc;

public class BlackNode extends Node {

	public enum Amplitude {
		Low, High;
	}

	String value;

	Amplitude a;

	public BlackNode(String value, Amplitude a) {
		super();
		this.value = value;
		this.a = a;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		BlackNode other = (BlackNode) obj;
		if (a != other.a)
			return false;
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
		result = prime * result + ((a == null) ? 0 : a.hashCode());
		result = prime * result + ((value == null) ? 0 : value.hashCode());
		return result;
	}

}
