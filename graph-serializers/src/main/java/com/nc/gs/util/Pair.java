package com.nc.gs.util;

public final class Pair<K, V> {

	public static <T, U> Pair<T, U> of(T t, U u) {
		return new Pair<>(t, u);
	}

	public K k;

	public V v;

	public Pair() {
	}

	public Pair(K k, V v) {
		this.k = k;
		this.v = v;
	}

}
