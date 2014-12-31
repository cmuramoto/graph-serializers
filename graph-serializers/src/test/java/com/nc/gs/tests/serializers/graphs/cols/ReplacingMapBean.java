package com.nc.gs.tests.serializers.graphs.cols;

import java.util.HashMap;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

public class ReplacingMapBean {

	@com.nc.gs.meta.Map(concreteImpl = HashMap.class,
						implForReplacement = true,
						optimize = true)
	Map<String, String> optMap;

	@com.nc.gs.meta.Map(concreteImpl = TreeMap.class,
						implForReplacement = true,
						optimize = true)
	SortedMap<String, Object> optPolyMap;

	public Map<String, String> getOptMap() {
		return optMap;
	}

	public SortedMap<String, Object> getOptPolyMap() {
		return optPolyMap;
	}

	public void setOptMap(Map<String, String> map) {
		this.optMap = map;
	}

	public void setOptPolyMap(SortedMap<String, Object> optPolyMap) {
		this.optPolyMap = optPolyMap;
	}

}
