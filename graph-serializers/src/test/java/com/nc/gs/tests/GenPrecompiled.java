package com.nc.gs.tests;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.io.UncheckedIOException;
import java.math.BigInteger;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.IdentityHashMap;
import java.util.Map;

import javax.xml.ws.Holder;

import org.junit.Test;
import org.objectweb.asm.Type;

import symbols.io.abstraction._SerializerFactory;

import com.nc.gs.core.GraphSerializer;
import com.nc.gs.generator.GenerationStrategy;
import com.nc.gs.generator.Reifier;
import com.nc.gs.io.Sink;
import com.nc.gs.serializers.java.lang.compressed.CharacterSerializer;
import com.nc.gs.serializers.java.lang.compressed.DoubleSerializer;
import com.nc.gs.serializers.java.lang.compressed.FloatSerializer;
import com.nc.gs.serializers.java.lang.compressed.IntegerSerializer;
import com.nc.gs.serializers.java.lang.compressed.LongSerializer;
import com.nc.gs.serializers.java.lang.compressed.ShortSerializer;
import com.nc.gs.serializers.java.lang.compressed.StringSerializer;
import com.nc.gs.serializers.javax.xml.ws.HolderSerializer;
import com.nc.gs.util.Utils;

public class GenPrecompiled {

	public static byte[] read(File file) {
		try (RandomAccessFile raf = new RandomAccessFile(file, "r")) {
			int len = (int) raf.length();

			byte[] b = new byte[len];

			if (raf.read(b) != len) {
				throw new IllegalStateException();
			}

			return b;
		} catch (IOException e) {
			throw new UncheckedIOException(e);
		}
	}

	@Test
	public void run() throws IOException {

		File base = new File("src/main/resources/precompiled");

		Arrays.stream(Utils.IO_BASE.listFiles()).forEach(f -> f.delete());

		Map<Class<?>, GraphSerializer> comp = new IdentityHashMap<>();
		comp.put(String.class, new StringSerializer());
		comp.put(Double.class, new DoubleSerializer());
		comp.put(Long.class, new LongSerializer());
		comp.put(Integer.class, new IntegerSerializer());
		comp.put(Float.class, new FloatSerializer());
		comp.put(Short.class, new ShortSerializer());
		comp.put(Character.class, new CharacterSerializer());

		Map<Class<?>, GraphSerializer> std = new IdentityHashMap<>();
		std.put(String.class, new com.nc.gs.serializers.java.lang.StringSerializer());
		std.put(Byte.class, new com.nc.gs.serializers.java.lang.ByteSerializer());
		std.put(Boolean.class, new com.nc.gs.serializers.java.lang.BooleanSerializer());
		std.put(Double.class, new com.nc.gs.serializers.java.lang.DoubleSerializer());
		std.put(Long.class, new com.nc.gs.serializers.java.lang.LongSerializer());
		std.put(Integer.class, new com.nc.gs.serializers.java.lang.IntegerSerializer());
		std.put(Float.class, new com.nc.gs.serializers.java.lang.FloatSerializer());
		std.put(Short.class, new com.nc.gs.serializers.java.lang.ShortSerializer());
		std.put(Character.class, new com.nc.gs.serializers.java.lang.CharacterSerializer());
		std.put(Class.class, new com.nc.gs.serializers.java.lang.TypeSerializer());
		std.put(Date.class, new com.nc.gs.serializers.java.util.DateSerializer());
		std.put(BigInteger.class, new com.nc.gs.serializers.java.math.BigIntegerSerializer());
		std.put(GregorianCalendar.class, new com.nc.gs.serializers.java.util.GregorianCalendarSerializer());
		std.put(Holder.class, new HolderSerializer());

		try (Sink s = new Sink(16 * 1024)) {

			s.writeVarInt(comp.size() + std.size());

			comp.forEach((k, v) -> {
				String targetName = GenerationStrategy.prefixForSerializer(Type.getInternalName(k)) + "_C" + _SerializerFactory.genClassSuffix;
				try {
					Reifier.reify(k, v.getClass(), targetName);

					String srcName = targetName.replace('/', '.') + ".class";
					// String dstName = k.getName() + ".bin";
					File f = new File(Utils.IO_BASE, srcName);
					byte[] bytes = Files.readAllBytes(f.toPath());
					s.writeBoolean(true);
					s.writeUTF(k.getName());
					s.writeVarInt(bytes.length);
					s.writeBytes(bytes);
				} catch (Exception e) {
					throw new RuntimeException(e);
				}
			});

			std.forEach((k, v) -> {
				String targetName = GenerationStrategy.prefixForSerializer(Type.getInternalName(k)) + _SerializerFactory.genClassSuffix;

				try {
					Reifier.reify(k, v.getClass(), targetName);

					String srcName = targetName.replace('/', '.') + ".class";
					// String dstName = k.getName() + ".bin";
					File f = new File(Utils.IO_BASE, srcName);
					// Files.copy(f.toPath(), base.toPath().resolve(dstName),
					// StandardCopyOption.REPLACE_EXISTING);
					byte[] bytes = Files.readAllBytes(f.toPath());
					s.writeBoolean(false);
					s.writeUTF(k.getName());
					s.writeVarInt(bytes.length);
					s.writeBytes(bytes);

				} catch (Exception e) {
					throw new RuntimeException(e);
				}
			});

			byte[] blob = s.toByteArray();

			Files.write(base.toPath().resolve("reified-blob.bin"), blob, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
		}
	}
}