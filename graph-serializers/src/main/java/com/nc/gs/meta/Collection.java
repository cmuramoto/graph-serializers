package com.nc.gs.meta;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.CLASS)
@Target(ElementType.FIELD)
public @interface Collection {

	@SuppressWarnings("rawtypes")
	Class<? extends java.util.Collection> concreteImpl() default java.util.Collection.class;

	boolean implForReplacement() default false;

	boolean optimize() default false;

	Shape shape() default @Shape;
}