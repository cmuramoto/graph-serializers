package com.nc.gs.tests.serializers.java.util.sorted;

public class A implements Comparable<A> {

	String name;

	@Override
	public int compareTo(final A o) {
		return name.compareTo(o.name);
	}

	@Override
	public String toString() {
		return "A [name=" + name + "]";
	}

}
