package com.nc.gs.meta;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.CLASS)
@Target({ ElementType.FIELD, ElementType.TYPE, ElementType.TYPE_USE })
public @interface Hierarchy {

	boolean complete() default true;

	Class<?>[] types() default {};

}