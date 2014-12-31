package com.nc.gs.serializers;

import java.util.Comparator;

import com.nc.gs.core.Context;
import com.nc.gs.core.GraphSerializer;
import com.nc.gs.core.Instantiator;
import com.nc.gs.io.Sink;
import com.nc.gs.io.Source;

public final class SingletonSerializer extends GraphSerializer implements Instantiator {

	final Object instance;

	public SingletonSerializer(Object instance) {
		this.instance = instance;
	}

	@Override
	public Object allocate() {
		return instance;
	}

	@SuppressWarnings("rawtypes")
	@Override
	public Object allocate(Comparator cmp) {
		return instance;
	}

	@Override
	public Object allocate(int sz) {
		return instance;
	}

	@SuppressWarnings("rawtypes")
	@Override
	public Object allocate(int sz, Comparator cmp) {
		return instance;
	}

	@Override
	public Object allocateHollow() {
		return instance;
	}

	@Override
	public void copy(Object src, Object dst) {

	}

	@Override
	public Object instantiate(Source src) {
		return instance;
	}

	@Override
	public void writeData(Context c, Sink dst, Object o) {

	}

}
