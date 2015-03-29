package com.nc.gs.tests.generator.inheritance;

import java.util.HashSet;
import java.util.Set;

import com.nc.gs.meta.Collection;
import com.nc.gs.meta.Hierarchy;

public class Root {

	public static class A extends Root {

	}

	public static class A_0 extends A {

	}

	public static class A_0_0 extends A_0 {

	}

	public static class B extends Root {

	}

	public static class B_0 extends B {

	}

	public static class B_1 extends B {

	}

	public static class C extends Root {

	}

	public static class C_0 extends C {

	}

	public static class C_0_0 extends C_0 {

	}

	public static class C_0_0_0 extends C_0_0 {

	}

	public static class D extends Root {

	}

	int i;

	@Collection(optimize = true, concreteImpl = HashSet.class)
	Set<@Hierarchy(complete = true, types = { Root.class, A.class, B.class, C.class, D.class, A_0.class, A_0_0.class, B_0.class, B_1.class, C_0.class, C_0_0.class, C_0_0_0.class }) Root> s = new HashSet<Root>();

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		Root other = (Root) obj;
		if (i != other.i) {
			return false;
		}
		return true;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = (prime * result) + i;
		return result;
	}

}
