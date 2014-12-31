package com.nc.gs.tests.serializers.graphs.cols;

import gnu.trove.map.hash.THashMap;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListMap;

import com.nc.gs.meta.Collection;
import com.nc.gs.meta.Hierarchy;
import com.nc.gs.meta.MaybeOpaque;
import com.nc.gs.meta.NotNull;
import com.nc.gs.meta.OnlyPayload;
import com.nc.gs.meta.Shape;

public class MapBean {
	@OnlyPayload
	private java.util.Map<Integer, Integer> keys;

	@Shape(onlyPayload = true)
	private java.util.Map<String, String> vals;

	@NotNull
	private java.util.Map<Integer, Integer> hashes;

	@NotNull
	@OnlyPayload
	@Shape(nullable = false, onlyPayload = true)
	private java.util.Map<String, String> hashStrings;

	@Collection(shape = @Shape(nullable = false, onlyPayload = true, hierarchy = @Hierarchy(types = String.class)))
	private java.util.Map<Object, Object> polyType;

	@MaybeOpaque
	java.util.Map<String, String> opaqueType;

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		MapBean other = (MapBean) obj;
		if (hashStrings == null) {
			if (other.hashStrings != null)
				return false;
		} else if (!hashStrings.equals(other.hashStrings))
			return false;
		if (hashes == null) {
			if (other.hashes != null)
				return false;
		} else if (!hashes.equals(other.hashes))
			return false;
		if (keys == null) {
			if (other.keys != null)
				return false;
		} else if (!keys.equals(other.keys))
			return false;
		if (opaqueType == null) {
			if (other.opaqueType != null)
				return false;
		} else if (!opaqueType.equals(other.opaqueType))
			return false;
		if (polyType == null) {
			if (other.polyType != null)
				return false;
		} else if (!polyType.equals(other.polyType))
			return false;
		if (vals == null) {
			if (other.vals != null)
				return false;
		} else if (!vals.equals(other.vals))
			return false;
		return true;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((hashStrings == null) ? 0 : hashStrings.hashCode());
		result = prime * result + ((hashes == null) ? 0 : hashes.hashCode());
		result = prime * result + ((keys == null) ? 0 : keys.hashCode());
		result = prime * result
				+ ((opaqueType == null) ? 0 : opaqueType.hashCode());
		result = prime * result
				+ ((polyType == null) ? 0 : polyType.hashCode());
		result = prime * result + ((vals == null) ? 0 : vals.hashCode());
		return result;
	}

	public void setHashes(int[] hashes) {
		this.hashes = new HashMap<>(hashes.length);

		for (int i = 0; i < hashes.length; i++) {
			this.hashes.put(hashes[i], hashes[i]);
		}

	}

	public void setHashStrings(String[] hashStrings) {
		this.hashStrings = new TreeMap<>();

		for (int i = 0; i < hashStrings.length; i++) {
			this.hashStrings.put(hashStrings[i], hashStrings[i]);
		}
	}

	public void setKeys(int[] keys) {
		this.keys = new ConcurrentSkipListMap<>();

		for (int i = 0; i < keys.length; i++) {
			this.keys.put(keys[i], keys[i]);
		}
	}

	public void setOpaqueVals(String[] vals, boolean reallyOpaque) {
		this.opaqueType = reallyOpaque ? Collections
				.synchronizedMap(new ConcurrentHashMap<String, String>())
				: new THashMap<String, String>();

		for (String string : vals) {
			this.opaqueType.put(string, string);
		}
	}

	public void setPolyType(Object[] polyType) {
		this.polyType = new THashMap<>();

		for (Object object : polyType) {
			this.polyType.put(object, object);
		}
	}

	public void setVals(String[] vals) {
		this.vals = new LinkedHashMap<>();

		for (String string : vals) {
			this.vals.put(string, string);
		}
	}

}
