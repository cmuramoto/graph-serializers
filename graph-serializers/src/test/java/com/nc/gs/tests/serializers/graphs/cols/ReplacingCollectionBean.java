package com.nc.gs.tests.serializers.graphs.cols;

import java.util.LinkedList;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

public class ReplacingCollectionBean {

	@com.nc.gs.meta.Collection(concreteImpl = LinkedList.class,
							   implForReplacement = true,
							   optimize = true)
	List<String> optList;

	@com.nc.gs.meta.Collection(concreteImpl = TreeSet.class,
							   implForReplacement = true,
							   optimize = true)
	SortedSet<String> optPolySet;

	public List<String> getOptList() {
		return optList;
	}

	public SortedSet<String> getOptPolySet() {
		return optPolySet;
	}

	public void setOptList(List<String> optMap) {
		this.optList = optMap;
	}

	public void setOptPolySet(SortedSet<String> optPolyMap) {
		this.optPolySet = optPolyMap;
	}

}
