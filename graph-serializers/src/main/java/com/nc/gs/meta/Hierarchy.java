package com.nc.gs.meta;

public @interface Hierarchy {

	boolean complete() default true;

	Class<?>[] types() default {};

}