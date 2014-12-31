package com.nc.gs.core;

import java.lang.reflect.Modifier;
import java.lang.reflect.Proxy;
import java.util.Collection;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

import com.nc.gs.log.Log;
import com.nc.gs.serializers.java.lang.ArraySerializer;
import com.nc.gs.serializers.java.lang.EnumSerializer;
import com.nc.gs.serializers.java.lang.LeafTypeArraySerializer;
import com.nc.gs.serializers.java.lang.OpaqueSerializer;
import com.nc.gs.serializers.java.lang.reflect.ProxySerializer;
import com.nc.gs.serializers.java.util.CollectionSerializer;
import com.nc.gs.serializers.java.util.EnumMapSerializer;
import com.nc.gs.serializers.java.util.EnumSetSerializer;
import com.nc.gs.serializers.java.util.MapSerializer;
import com.nc.gs.serializers.java.util.SetSerializer;

/**
 * A simple abstraction that enables the resolution of serializers prior to falling back to code
 * generation.
 *
 * @author cmuramoto
 */
public abstract class SerializerLookup implements Comparable<SerializerLookup> {

	public static class Basic extends SerializerLookup {

		public Basic(int order) {
			super(order);
		}

		@Override
		public GraphSerializer lookup(Class<?> clazz) {
			GraphSerializer rv;
			if (clazz.isArray()) {
				Class<?> compType = clazz.getComponentType();

				if (compType.isArray()) {
					rv = ArraySerializer.NULL_WITH_REFS;
				} else if (compType.isPrimitive()) {
					rv = ArraySerializer.NULL_WITH_REFS;
				} else if (Modifier.isFinal(compType.getModifiers())) {
					rv = new LeafTypeArraySerializer(compType, true, false);
				} else {
					rv = ArraySerializer.NULL_WITH_REFS;
				}
			} else if (clazz.isEnum()) {
				rv = new EnumSerializer(clazz);
			} else if (Proxy.isProxyClass(clazz)) {
				rv = new ProxySerializer(clazz);
			} else if (Set.class.isAssignableFrom(clazz)) {
				if (EnumSet.class.isAssignableFrom(clazz)) {
					rv = EnumSetSerializer.basic();
				} else {
					rv = SetSerializer.basic();
				}
			} else if (Map.class.isAssignableFrom(clazz)) {
				if (EnumMap.class.isAssignableFrom(clazz)) {
					rv = EnumMapSerializer.basic();
				} else {
					rv = MapSerializer.basic();
				}

			} else if (Collection.class.isAssignableFrom(clazz)) {
				rv = CollectionSerializer.WITH_REFS;
			} else {
				rv = next == null ? null : next.lookup(clazz);
			}

			if (rv == null && clazz.getClassLoader() == null) {
				Log.warn("Creating Opaque Serializer for Bootstrap %s. For better performance and correctness, please provide a custom implementation.", clazz);
				rv = new OpaqueSerializer(clazz, null);
			}

			return rv;
		}
	}

	final int order;

	SerializerLookup next;

	public SerializerLookup(int order) {
		this.order = order;
	}

	@Override
	public int compareTo(SerializerLookup o) {
		return Integer.compare(order, o.order);
	}

	public abstract GraphSerializer lookup(Class<?> clazz);

}