package com.nc.gs.meta;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a type as being the Leaf of an Hierarchy, so that semantically, the type will be
 * interpreted as if it was a final Class. <br/>
 * <br/>
 * If this annotation is used on a <b>type</b>, then any field declaration of that type will be
 * interpreted as if the type were final. <br/>
 * <br/>
 * <br/>
 * If this annotation is used on a <b>field</b>, then only the field in the context of the class
 * that declares it, will be interpreted as if it were a final class.
 *
 * @author cmuramoto
 */
@Retention(RetentionPolicy.CLASS)
@Target({ ElementType.FIELD, ElementType.TYPE, ElementType.TYPE_USE })
public @interface LeafNode {

}
