package com.nc.gs.meta;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.CLASS)
@Target(ElementType.FIELD)
public @interface Shape {

	Hierarchy hierarchy() default @Hierarchy;

	boolean nullable() default true;

	boolean onlyPayload() default false;
}