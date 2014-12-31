package com.nc.gs.core;

public final class TypeHandle implements Comparable<TypeHandle> {

	Object h;

	public TypeHandle(Class<?> type) {
		h = type;
	}

	public TypeHandle(Class<?>[] types) {
		h = types;
	}

	@Override
	public int compareTo(TypeHandle r) {
		return 0;
	}

}
