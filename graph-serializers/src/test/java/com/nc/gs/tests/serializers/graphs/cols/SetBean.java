package com.nc.gs.tests.serializers.graphs.cols;

import gnu.trove.set.hash.THashSet;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListSet;

import com.nc.gs.meta.Collection;
import com.nc.gs.meta.Hierarchy;
import com.nc.gs.meta.MaybeOpaque;
import com.nc.gs.meta.NotNull;
import com.nc.gs.meta.OnlyPayload;
import com.nc.gs.meta.Shape;

public class SetBean {

	@OnlyPayload
	private Set<Integer> keys;

	@Shape(onlyPayload = true)
	private Set<String> vals;

	@NotNull
	private Set<Integer> hashes;

	@NotNull
	@OnlyPayload
	@Shape(nullable = false, onlyPayload = true)
	private Set<String> hashStrings;

	@Collection(shape = @Shape(nullable = false, onlyPayload = true, hierarchy = @Hierarchy(types = String.class)))
	private Set<Object> polyType;

	@MaybeOpaque
	Set<String> opaqueType;

	@SuppressWarnings("unchecked")
	private Object[] array(@SuppressWarnings("rawtypes") java.util.Collection c) {
		Object[] rv;
		if (c == null) {
			rv = null;
		} else {
			rv = c.toArray(new Object[] { c.size() });
			Arrays.sort(rv);
		}
		return rv;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		SetBean other = (SetBean) obj;
		if (!Arrays.equals(array(hashStrings), array(other.hashStrings)))
			return false;
		if (!Arrays.equals(array(hashes), array(other.hashes)))
			return false;
		if (!Arrays.equals(array(keys), array(other.keys)))
			return false;
		if (!Arrays.equals(array(polyType), array(other.polyType)))
			return false;
		if (!Arrays.equals(array(vals), array(other.vals)))
			return false;
		return true;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + Arrays.hashCode(array(hashStrings));
		result = prime * result + Arrays.hashCode(array(hashes));
		result = prime * result + Arrays.hashCode(array(keys));
		result = prime * result + Arrays.hashCode(array(polyType));
		result = prime * result + Arrays.hashCode(array(vals));
		return result;
	}

	public void setHashes(int[] hashes) {
		this.hashes = new HashSet<>(hashes.length);

		for (int i = 0; i < hashes.length; i++) {
			this.hashes.add(hashes[i]);
		}

	}

	public void setHashStrings(String[] hashStrings) {
		this.hashStrings = new TreeSet<>();

		for (int i = 0; i < hashStrings.length; i++) {
			this.hashStrings.add(hashStrings[i]);
		}
	}

	public void setKeys(int[] keys) {
		this.keys = new ConcurrentSkipListSet<>();

		for (int i = 0; i < keys.length; i++) {
			this.keys.add(keys[i]);
		}
	}

	public void setOpaqueVals(String[] vals, boolean reallyOpaque) {
		this.opaqueType = reallyOpaque ? Collections
				.newSetFromMap(new ConcurrentHashMap<String, Boolean>())
				: new TreeSet<String>();

		for (String string : vals) {
			this.opaqueType.add(string);
		}
	}

	public void setPolyType(Object[] polyType) {
		this.polyType = new THashSet<>();

		for (Object object : polyType) {
			this.polyType.add(object);
		}
	}

	public void setVals(String[] vals) {
		this.vals = new LinkedHashSet<>();

		for (String string : vals) {
			this.vals.add(string);
		}
	}

}