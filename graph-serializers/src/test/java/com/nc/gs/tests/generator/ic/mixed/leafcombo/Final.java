package com.nc.gs.tests.generator.ic.mixed.leafcombo;

public final class Final {

	int tag;

	public Final(int tag) {
		super();
		this.tag = tag;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Final other = (Final) obj;
		if (tag != other.tag)
			return false;
		return true;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + tag;
		return result;
	}

}
