package com.nc.gs.serializers.java.lang;

import static com.nc.gs.core.Serializer.readNested;
import static com.nc.gs.core.Serializer.writeNested;
import static symbols.io.abstraction._Tags.Serializer.NULL;
import static symbols.io.abstraction._Tags.Serializer.TYPE_ID;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import com.nc.gs.core.Context;
import com.nc.gs.core.GraphSerializer;
import com.nc.gs.io.Sink;
import com.nc.gs.io.Source;
import com.nc.gs.util.Utils;

public class OpaqueSerializer extends GraphSerializer {

	final Class<?> type;
	final Field[] refs;
	final Field[] prims;

	public OpaqueSerializer(Class<?> type, Set<String> includes) {
		this.type = type;

		List<Field> allRefs = new ArrayList<>();
		List<Field> allPrims = new ArrayList<>();

		Class<?> c = type;

		while (c != null) {
			Field[] fields = c.getDeclaredFields();

			for (Field f : fields) {
				if (!Modifier.isStatic(f.getModifiers()) && (!Modifier.isTransient(f.getModifiers())//
								|| includes != null && includes.contains(f.getName()))) {
					if (f.getType().isPrimitive()) {
						allPrims.add(f);
					} else {
						allRefs.add(f);
					}

					f.setAccessible(true);
				}
			}

			c = c.getSuperclass();
		}

		refs = allRefs.isEmpty() ? null : allRefs.toArray(new Field[allRefs.size()]);
		prims = allPrims.isEmpty() ? null : allPrims.toArray(new Field[allPrims.size()]);
	}

	@Override
	public void inflateData(Context c, Source src, Object o) {
		try {
			Field[] refs = this.refs;
			if (refs != null) {
				for (Field f : refs) {
					Object v = readNested(c, src);
					f.set(o, v);
				}
			}

			Field[] prims = this.prims;
			if (prims != null) {
				for (Field f : prims) {
					switch (Context.typeId(f.getType())) {
					case 1:
						f.setBoolean(o, src.readBoolean());
						break;
					case 2:
						f.setChar(o, src.readChar());
						break;
					case 3:
						f.setByte(o, src.readByte());
						break;
					case 4:
						f.setShort(o, src.readShort());
						break;
					case 5:
						f.setInt(o, src.readInt());
						break;
					case 6:
						f.setFloat(o, src.readFloat());
						break;
					case 7:
						f.setLong(o, src.readLong());
						break;
					case 8:
						f.setDouble(o, src.readDouble());
						break;
					}
				}
			}
		} catch (Exception e) {
			Utils.rethrow(e);
		}
	}

	@Override
	public Object instantiate(Source src) {
		return Utils.allocateInstance(type);
	}

	@Override
	public String toString() {
		return new StringBuilder().//
				append("OpaqueSerializer<").//
				append(type.getName()).//
				append(">").//
				toString();
	}

	@Override
	public void writeData(Context c, Sink dst, Object o) {
		try {
			Field[] refs = this.refs;
			if (refs != null) {
				for (Field f : refs) {
					Object v = f.get(o);
					writeNested(c, dst, v);
				}
			}

			Field[] prims = this.prims;

			if (prims != null) {

				for (Field f : prims) {
					switch (Context.typeId(f.getType())) {
					case 1:
						dst.writeByte(f.getBoolean(o) ? TYPE_ID : NULL);
						break;
					case 2:
						dst.writeChar(f.getChar(o));
						break;
					case 3:
						dst.writeByte(f.getByte(o));
						break;
					case 4:
						dst.writeShort(f.getShort(o));
						break;
					case 5:
						dst.writeInt(f.getInt(o));
						break;
					case 6:
						dst.writeFloat(f.getFloat(o));
						break;
					case 7:
						dst.writeLong(f.getLong(o));
						break;
					case 8:
						dst.writeDouble(f.getDouble(o));
						break;
					}
				}
			}
		} catch (Exception e) {
			Utils.rethrow(e);
		}

	}

}
