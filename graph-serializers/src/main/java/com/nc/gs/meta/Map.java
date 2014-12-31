package com.nc.gs.meta;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.CLASS)
@Target(ElementType.FIELD)
public @interface Map {

	@SuppressWarnings("rawtypes")
	Class<? extends java.util.Map> concreteImpl() default java.util.Map.class;

	boolean implForReplacement() default false;

	Shape key() default @Shape;

	boolean optimize() default false;

	Shape val() default @Shape;

}
