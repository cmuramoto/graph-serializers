package com.nc.gs.tests.generator.interned;

public class InternedBean {

	int i;

	String j;

	public InternedBean() {
		super();
	}

	public InternedBean(int i, String j) {
		super();
		this.i = i;
		this.j = j;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		final InternedBean other = (InternedBean) obj;
		if (i != other.i) {
			return false;
		}
		if (j == null) {
			if (other.j != null) {
				return false;
			}
		} else if (!j.equals(other.j)) {
			return false;
		}
		return true;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + i;
		result = prime * result + (j == null ? 0 : j.hashCode());
		return result;
	}

}
