package com.nc.gs.meta;

public @interface Collection {

	@SuppressWarnings("rawtypes")
	Class<? extends java.util.Collection> concreteImpl() default java.util.Collection.class;

	boolean implForReplacement() default false;

	boolean optimize() default false;

	Shape shape() default @Shape;
}