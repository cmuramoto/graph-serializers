package com.nc.gs.core;

import java.util.Comparator;

@SuppressWarnings("rawtypes")
public interface Instantiator {

	Object allocate();

	Object allocate(Comparator cmp);

	Object allocate(int sz);

	Object allocate(int sz, Comparator cmp);

	Object allocateHollow();

	void copy(Object src, Object dst);

}
