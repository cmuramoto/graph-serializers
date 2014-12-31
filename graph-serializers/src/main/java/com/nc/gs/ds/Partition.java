package com.nc.gs.ds;

import java.util.AbstractList;
import java.util.List;

public final class Partition<T> extends AbstractList<List<T>> {
	final List<T> list;
	final int size;

	public Partition(List<T> list, int size) {
		this.list = list;
		this.size = size;
	}

	@Override
	public List<T> get(int index) {
		int listSize = size();
		if (index < 0 || index >= listSize) {
			throw new IndexOutOfBoundsException();
		}
		int start = index * size;
		int end = Math.min(start + size, list.size());
		return list.subList(start, end);
	}

	@Override
	public boolean isEmpty() {
		return list.isEmpty();
	}

	@Override
	public int size() {
		int result = list.size() / size;
		if (result * size != list.size()) {
			result++;
		}
		return result;
	}
}