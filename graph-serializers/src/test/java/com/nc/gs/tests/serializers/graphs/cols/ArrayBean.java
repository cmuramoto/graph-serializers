package com.nc.gs.tests.serializers.graphs.cols;

import java.util.Arrays;

import com.nc.gs.meta.Collection;
import com.nc.gs.meta.Hierarchy;
import com.nc.gs.meta.NotNull;
import com.nc.gs.meta.OnlyPayload;
import com.nc.gs.meta.Shape;

public class ArrayBean {

	@OnlyPayload
	private int[] keys;

	@Shape(onlyPayload = true)
	private String[] vals;

	@NotNull
	private int[] hashes;

	@NotNull
	@OnlyPayload
	@Shape(nullable = false, onlyPayload = true)
	private String[] hashStrings;

	@Collection(shape = @Shape(nullable = false, onlyPayload = true, hierarchy = @Hierarchy(types = { String.class })))
	private Object[] polyType;

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ArrayBean other = (ArrayBean) obj;
		if (!Arrays.equals(hashStrings, other.hashStrings))
			return false;
		if (!Arrays.equals(hashes, other.hashes))
			return false;
		if (!Arrays.equals(keys, other.keys))
			return false;
		if (!Arrays.equals(polyType, other.polyType))
			return false;
		if (!Arrays.equals(vals, other.vals))
			return false;
		return true;
	}

	public int[] getHashes() {
		return hashes;
	}

	public String[] getHashStrings() {
		return hashStrings;
	}

	public int[] getKeys() {
		return keys;
	}

	public Object[] getPolyType() {
		return polyType;
	}

	public String[] getVals() {
		return vals;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + Arrays.hashCode(hashStrings);
		result = prime * result + Arrays.hashCode(hashes);
		result = prime * result + Arrays.hashCode(keys);
		result = prime * result + Arrays.hashCode(polyType);
		result = prime * result + Arrays.hashCode(vals);
		return result;
	}

	public void setHashes(int[] hashes) {
		this.hashes = hashes;
	}

	public void setHashStrings(String[] hashStrings) {
		this.hashStrings = hashStrings;
	}

	public void setKeys(int[] keys) {
		this.keys = keys;
	}

	public void setPolyType(Object[] polyType) {
		this.polyType = polyType;
	}

	public void setVals(String[] vals) {
		this.vals = vals;
	}

}
