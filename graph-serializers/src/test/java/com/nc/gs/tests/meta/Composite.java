package com.nc.gs.tests.meta;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.nc.gs.meta.Collection;
import com.nc.gs.meta.Hierarchy;
import com.nc.gs.meta.Shape;

public class Composite {

	Map<A, B> map;

	Set<C> set;

	@Collection(concreteImpl = ArrayList.class,
				shape = @Shape(nullable = false,
							   onlyPayload = true,
							   hierarchy = @Hierarchy(types = { D.class },
													  complete = false)),
				optimize = true)
	List<D> list;

}
