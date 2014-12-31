package com.nc.gs.tests.serializers.graphs.ic.pvt;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import com.nc.gs.meta.NotNull;
import com.nc.gs.meta.OnlyPayload;

public class SubOptTree {

	@NotNull
	final List<StandardNode> a;

	@NotNull
	private final List<StandardNode> b;

	@NotNull
	@OnlyPayload
	Collection<StandardNode> c;

	@NotNull
	@OnlyPayload
	Collection<StandardNode> d;

	public SubOptTree(Collection<StandardNode> a, Collection<StandardNode> b,
			Collection<StandardNode> c, Collection<StandardNode> d) {
		this.a = (List<StandardNode>) (a instanceof ArrayList ? a
				: new ArrayList<>(a));
		this.b = (List<StandardNode>) (b instanceof LinkedList ? b
				: new LinkedList<>(b));
		this.c = c;
		this.d = d;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		SubOptTree other = (SubOptTree) obj;
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
		if (c == null) {
			if (other.c != null)
				return false;
		} else if (!Arrays.equals(c.toArray(), other.c.toArray()))
			return false;
		if (d == null) {
			if (other.d != null)
				return false;
		} else if (!Arrays.equals(d.toArray(), other.d.toArray()))
			return false;
		return true;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((a == null) ? 0 : a.hashCode());
		result = prime * result + ((b == null) ? 0 : b.hashCode());
		result = prime * result + ((c == null) ? 0 : c.hashCode());
		result = prime * result + ((d == null) ? 0 : d.hashCode());
		return result;
	}

}
