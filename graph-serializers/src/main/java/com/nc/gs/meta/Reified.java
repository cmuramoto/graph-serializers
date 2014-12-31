package com.nc.gs.meta;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.nc.gs.core.GraphSerializer;

/**
 * Marks a {@link GraphSerializer} implementation as having type static methods
 * to write and/or read an object's payload.
 * 
 * If a class declares a {@link Reified}, then it should provide type public
 * static type-safe methods to read and write the payload of an Object.
 * 
 * The read method is not mandatory, as some classes will consume some of the
 * buffer contents before being instantiated and won't read any data afterwards.
 * 
 * Given an implementation of a {@link GraphSerializer} for a type T, valid
 * signatures for the methods are as follows:
 * 
 * <br/>
 * <br/>
 * 
 * <table border='1px solid black'>
 * <tr>
 * <td><b>allocate</b></td>
 * <td>()T</td>
 * <td>(ByteBuffer)T</td>
 * </tr>
 * <tr>
 * <td><b>write</b></td>
 * <td>(Context,ByteBuffer,T)void</td>
 * <td>(ByteBuffer,T)void</td>
 * </tr>
 * <tr>
 * <td><b>read</b></td>
 * <td>(Context,ByteBuffer,T)void</td>
 * <td>(ByteBuffer,T)void</td>
 * </tr>
 * </table>
 * 
 * <br>
 * <br/>
 * 
 * This annotation will instruct the code generator to use reified instructions
 * rather than usual instructions declared on {@link GraphSerializer}.
 * 
 * @author cmuramoto
 * 
 */
@Retention(RetentionPolicy.CLASS)
@Target(ElementType.TYPE)
public @interface Reified {

	String allocate() default "allocate";

	String read() default "read";

	String write() default "write";

}