package com.nc.gs.tests.generator.ic.mixed.leafcombo;

import com.nc.gs.meta.LeafNode;

@LeafNode
public class NonFinal {

	class Child extends NonFinal {

		int j;

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			Child other = (Child) obj;
			// if (!getOuterType().equals(other.getOuterType()))
			// return false;
			if (j != other.j || i != other.i)
				return false;
			return true;
		}

		private NonFinal getOuterType() {
			return NonFinal.this;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + getOuterType().hashCode();
			result = prime * result + j;
			return result;
		}

		Child with(int i, int j) {
			this.i = i;
			this.j = j;

			return this;
		}

	}

	int i;

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		NonFinal other = (NonFinal) obj;
		if (i != other.i)
			return false;
		return true;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + i;
		return result;
	}

	NonFinal with(int i) {
		this.i = i;
		return this;
	}

}
