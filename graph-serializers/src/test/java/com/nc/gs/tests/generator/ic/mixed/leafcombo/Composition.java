package com.nc.gs.tests.generator.ic.mixed.leafcombo;

import com.nc.gs.meta.InternalNode;
import com.nc.gs.meta.LeafNode;

public class Composition {

	Final a;

	NonFinal b;

	@InternalNode
	Final wrong;

	@InternalNode
	NonFinal override;

	@InternalNode
	String wrongString;

	@LeafNode
	String redundant;

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Composition other = (Composition) obj;
		if (a == null) {
			if (other.a != null)
				return false;
		} else if (!a.equals(other.a))
			return false;
		if (b == null) {
			if (other.b != null)
				return false;
		} else if (!b.equals(other.b))
			return false;
		if (override == null) {
			if (other.override != null)
				return false;
		} else if (!override.equals(other.override))
			return false;
		if (redundant == null) {
			if (other.redundant != null)
				return false;
		} else if (!redundant.equals(other.redundant))
			return false;
		if (wrong == null) {
			if (other.wrong != null)
				return false;
		} else if (!wrong.equals(other.wrong))
			return false;
		if (wrongString == null) {
			if (other.wrongString != null)
				return false;
		} else if (!wrongString.equals(other.wrongString))
			return false;
		return true;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((a == null) ? 0 : a.hashCode());
		result = prime * result + ((b == null) ? 0 : b.hashCode());
		result = prime * result
				+ ((override == null) ? 0 : override.hashCode());
		result = prime * result
				+ ((redundant == null) ? 0 : redundant.hashCode());
		result = prime * result + ((wrong == null) ? 0 : wrong.hashCode());
		result = prime * result
				+ ((wrongString == null) ? 0 : wrongString.hashCode());
		return result;
	}

}