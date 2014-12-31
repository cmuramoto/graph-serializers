package com.nc.gs.tests.generator.ic.mixed.samples;

import com.nc.gs.meta.Hierarchy;
import com.nc.gs.meta.NotNull;
import com.nc.gs.meta.OnlyPayload;

@SuppressWarnings({ "all" })
public class Tree {

	@Hierarchy(types = { Leaf.class, Root.class, Trunk.class })
	Node a;

	@NotNull
	@Hierarchy(types = { Leaf.class, Root.class, Trunk.class })
	Node b;

	@OnlyPayload
	@Hierarchy(types = { Leaf.class, Root.class, Trunk.class })
	Node c;

	@NotNull
	@OnlyPayload
	@Hierarchy(types = { Leaf.class, Root.class, Trunk.class })
	Node d;

	@Hierarchy(types = { Leaf.class, Root.class, Trunk.class })
	Root e;

	@NotNull
	@Hierarchy(types = { Leaf.class, Root.class, Trunk.class })
	Root f;

	@OnlyPayload
	@Hierarchy(types = { Leaf.class, Root.class, Trunk.class })
	Root g;

	@NotNull
	@OnlyPayload
	@Hierarchy(types = { Leaf.class, Root.class, Trunk.class })
	Root h;

	@Hierarchy(types = { Prims.class })
	Prims p;

}
