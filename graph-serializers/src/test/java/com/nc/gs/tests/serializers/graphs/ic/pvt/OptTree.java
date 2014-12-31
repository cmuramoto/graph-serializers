package com.nc.gs.tests.serializers.graphs.ic.pvt;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.LinkedTransferQueue;

import com.nc.gs.meta.Collection;
import com.nc.gs.meta.Hierarchy;
import com.nc.gs.meta.NotNull;
import com.nc.gs.meta.OnlyPayload;
import com.nc.gs.meta.Shape;

public class OptTree {

	@Collection(concreteImpl = ArrayList.class,
				shape = @Shape(nullable = false,
							   hierarchy = @Hierarchy(types = ICNode.class)),
				optimize = true)
	@NotNull
	final List<ICNode> a;

	@Collection(concreteImpl = LinkedList.class,
				shape = @Shape(nullable = false,
							   hierarchy = @Hierarchy(types = ICNode.class)),
				optimize = true)
	@NotNull
	private final List<ICNode> b;

	@Collection(concreteImpl = ConcurrentLinkedQueue.class,
				shape = @Shape(nullable = false,
							   hierarchy = @Hierarchy(types = ICNode.class,
													  complete = true)),
				optimize = true)
	@NotNull
	@OnlyPayload
	java.util.Collection<ICNode> c;

	@Collection(concreteImpl = LinkedTransferQueue.class,
				shape = @Shape(nullable = false,
							   hierarchy = @Hierarchy(types = ICNode.class)),
				optimize = true)
	@NotNull
	@OnlyPayload
	java.util.Collection<ICNode> d;

	public OptTree(java.util.Collection<ICNode> a, java.util.Collection<ICNode> b, java.util.Collection<ICNode> c, java.util.Collection<ICNode> d) {
		this.a = (List<ICNode>) (a instanceof ArrayList ? a : new ArrayList<>(a));
		this.b = (List<ICNode>) (b instanceof LinkedList ? b : new LinkedList<>(b));
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
		OptTree other = (OptTree) obj;
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
