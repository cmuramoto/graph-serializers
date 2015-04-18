package com.nc.gs.tests.generator.typeannon;

import java.util.Map;

import com.nc.gs.meta.Hierarchy;
import com.nc.gs.meta.LeafNode;
import com.nc.gs.meta.NotNull;
import com.nc.gs.meta.OnlyPayload;
import com.nc.gs.meta.Optimize;

public class TheType {

	static class Bar extends Foo {

	}

	static class Baz extends Foo {

	}

	static class Car extends Roo {

	}

	static class Foo {

	}

	static class Roo {

	}

	@Optimize
	Map<@NotNull Long, @LeafNode @OnlyPayload Car> mapA;

	@Optimize
	Map<@NotNull String, @OnlyPayload @NotNull @Hierarchy(complete = true, types = { Foo.class, Bar.class }) Bar> mapB;

	@Optimize
	Map<@NotNull String, @OnlyPayload @NotNull @Hierarchy(complete = true, types = { Baz.class, Bar.class }) Foo> mapC;

}