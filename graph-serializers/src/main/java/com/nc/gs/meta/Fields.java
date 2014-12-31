package com.nc.gs.meta;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.CLASS)
@Target(ElementType.TYPE)
public @interface Fields {

	boolean compressByDefault() default false;

	String[] exclude() default {};

	String[] include() default {};

	boolean welcomesAllNonTransient() default true;

	boolean welcomesTransient() default false;
}
