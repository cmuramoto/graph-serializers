package com.nc.gs.meta;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.nc.gs.core.GraphSerializer;

@Retention(RetentionPolicy.CLASS)
@Target({ ElementType.FIELD, ElementType.TYPE })
public @interface Serialized {

	Class<? extends GraphSerializer> with();

}
