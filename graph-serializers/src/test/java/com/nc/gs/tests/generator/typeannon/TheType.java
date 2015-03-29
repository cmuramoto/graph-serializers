package com.nc.gs.tests.generator.typeannon;

import java.util.Map;

import com.nc.gs.meta.Hierarchy;
import com.nc.gs.meta.LeafNode;
import com.nc.gs.meta.NotNull;
import com.nc.gs.meta.OnlyPayload;
import com.nc.gs.meta.Optimize;

public class TheType {

	static class Foo {

	}
	
	static class Roo {

	}

	static class Bar extends Foo {

	}

	static class Car extends Roo {
		
	}

	static class Baz extends Bar {

	}

	@Optimize
	Map<@NotNull Long, @LeafNode @OnlyPayload Car> mapA; 

	@Optimize
	Map<@NotNull String,@OnlyPayload @NotNull @Hierarchy(complete = true, types = { Foo.class,
			Bar.class }) Bar> mapB;
	
	@Optimize
	Map<@NotNull String,@OnlyPayload @NotNull @Hierarchy(complete = true, types = { Bar.class,
			Baz.class }) Foo> mapC;

}
