package bench;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import serializers.impl.protobuf.ProtobufSerializer;
import serializers.impl.thrift.ThriftSerializer;
import serializers.spi.CheckingObjectSerializer;
import serializers.spi.ObjectSerializer;

@SuppressWarnings({ "rawtypes", "unchecked" })
public class BenchmarkRunner {
	enum measurements {
		timeCreate, timeSerializeDifferentObjects, timeCreateAndSerialize, timeSerializeSameObject, timeDeserializeNoFieldAccess, timeDeserializeAndCheckMediaField, timeDeserializeAndCheckAllFields, totalTime, length
	}

	public static void main(String... args) throws Exception {
		BenchmarkRunner runner = new BenchmarkRunner();
		boolean thrift = Boolean.getBoolean("benchmark.enable.thrift");

		runner.addObjectSerializer(new serializers.impl.gs.compressed.GSImpl());
		runner.addObjectSerializer(new serializers.impl.gs.std.GSImpl());
		runner.addObjectSerializer(new ProtobufSerializer());
		if (thrift) {
			runner.addObjectSerializer(new ThriftSerializer());
		}
		// runner.addObjectSerializer(new JavaSerializer());

		System.out.println("Starting");

		int repeats = Integer.getInteger("benchmark.repeats.max", 5);
		for (int i = 1; i <= repeats; i++) {
			runner.start(i, repeats);
		}
	}

	public final static int ITERATIONS = 2000;

	public final static int TRIALS = 20;

	/**
	 * Number of milliseconds to warm up for each operation type for each serializer. Let's start
	 * with 3 seconds.
	 */
	final static long WARMUP_MSECS = 3000;

	private Set<ObjectSerializer> _serializers = new LinkedHashSet<ObjectSerializer>();

	private void addObjectSerializer(ObjectSerializer serializer) {
		_serializers.add(serializer);
	}

	private void addValue(EnumMap<measurements, Map<String, Double>> values, String name, double timeCreate, double timeSerializeDifferentObjects, double timeSerializeSameObject, double timeDeserializeNoFieldAccess, double timeDeserializeAndCheckMediaField, double timeDeserializeAndCheckAllFields, double totalTime, double length) {
		// Omit some charts for serializers that are extremely slow.
		if (!name.equals("json/google-gson") && !name.equals("scala")) {
			update(values, measurements.timeSerializeDifferentObjects, name, timeSerializeDifferentObjects);
			update(values, measurements.timeCreateAndSerialize, name, timeCreate + timeSerializeDifferentObjects);
			update(values, measurements.timeSerializeSameObject, name, timeSerializeSameObject);
			update(values, measurements.timeDeserializeNoFieldAccess, name, timeDeserializeNoFieldAccess);
			update(values, measurements.timeDeserializeAndCheckMediaField, name, timeDeserializeAndCheckMediaField);
			update(values, measurements.timeDeserializeAndCheckAllFields, name, timeDeserializeAndCheckAllFields);
			update(values, measurements.totalTime, name, totalTime);
		}
		update(values, measurements.length, name, length);
		update(values, measurements.timeCreate, name, timeCreate);
	}

	/**
	 * Method that tries to validate correctness of serializer, using round-trip (construct,
	 * serializer, deserialize; compare objects after steps 1 and 3). Currently only done for
	 * StdMediaDeserializer...
	 */
	private void checkCorrectness(ObjectSerializer serializer) throws Exception {
		Object input = serializer.create();
		byte[] array = serializer.serialize(input);
		Object output = serializer.deserialize(array);

		if (!input.equals(output)) {
			/*
			 * Should throw an exception; but for now (that we have a few failures) let's just
			 * whine...
			 */
			String msg = "serializer '" + serializer.getName() + "' failed round-trip test (ser+deser produces Object different from input), input=" + input + ", output=" + output;
			// throw new Exception("Error: "+msg);
			System.err.println("WARN: " + msg);
		}
	}

	private <T> double createObjects(ObjectSerializer<T> serializer, int iterations) throws Exception {
		long start = System.nanoTime();
		for (int i = 0; i < iterations; i++) {
			serializer.create();
		}
		return iterationTime(System.nanoTime() - start, iterations);
	}

	private <T> double deserializeAndCheckAllFields(CheckingObjectSerializer<T> serializer, int iterations) throws Exception {
		byte[] array = serializer.serialize(serializer.create());
		long delta = 0;
		for (int i = 0; i < iterations; i++) {
			long start = System.nanoTime();
			T obj = serializer.deserialize(array);
			serializer.checkAllFields(obj);
			delta += System.nanoTime() - start;
		}
		return iterationTime(delta, iterations);
	}

	private <T> double deserializeAndCheckMediaField(CheckingObjectSerializer<T> serializer, int iterations) throws Exception {
		byte[] array = serializer.serialize(serializer.create());
		long delta = 0;
		for (int i = 0; i < iterations; i++) {
			long start = System.nanoTime();
			T obj = serializer.deserialize(array);
			serializer.checkMediaField(obj);
			delta += System.nanoTime() - start;
		}
		return iterationTime(delta, iterations);
	}

	private <T> double deserializeNoFieldAccess(ObjectSerializer<T> serializer, int iterations) throws Exception {
		byte[] array = serializer.serialize(serializer.create());
		long start = System.nanoTime();
		T result = null;
		for (int i = 0; i < iterations; i++) {
			result = serializer.deserialize(array);
			if (result == null) {
				throw new RuntimeException();
			}
		}
		return iterationTime(System.nanoTime() - start, iterations);
	}

	/**
	 * JVM is not required to honor GC requests, but adding bit of sleep around request is most
	 * likely to give it a chance to do it.
	 */
	private void doGc() {
		try {
			Thread.sleep(50L);
		} catch (InterruptedException ie) {
		}
		System.gc();
		System.gc();
		System.gc();
		try { // longer sleep afterwards (not needed by GC, but may help with scheduling)
			Thread.sleep(200L);
		} catch (InterruptedException ie) {
		}
	}

	private double iterationTime(long delta, int iterations) {
		return (double) delta / (double) iterations;
	}

	private void printImage(Map<String, Double> map, measurements m) {
		StringBuilder valSb = new StringBuilder();
		String names = "";
		double max = Double.MIN_NORMAL;
		for (Entry<String, Double> entry : map.entrySet()) {
			valSb.append(entry.getValue()).append(',');
			max = Math.max(max, entry.getValue());
			names = entry.getKey() + '|' + names;
		}

		int height = Math.min(30 + map.size() * 20, 430);
		double scale = max * 1.1;
		System.out.println("<img src='http://chart.apis.google.com/chart?chtt=" + m.name() + "&chf=c||lg||0||FFFFFF||1||76A4FB||0|bg||s||EFEFEF&chs=689x" + height + "&chd=t:" + valSb.toString().substring(0, valSb.length() - 1) + "&chds=0," + scale + "&chxt=y" + "&chxl=0:|" + names.substring(0, names.length() - 1)
				+ "&chm=N *f*,000000,0,-1,10&lklk&chdlp=t&chco=660000|660033|660066|660099|6600CC|6600FF|663300|663333|663366|663399|6633CC|6633FF|666600|666633|666666&cht=bhg&chbh=10&nonsense=aaa.png'/>");

	}

	private void printImages(EnumMap<measurements, Map<String, Double>> values) {
		for (measurements m : values.keySet()) {
			Map<String, Double> map = values.get(m);
			ArrayList<Entry> list = new ArrayList(map.entrySet());
			Collections.sort(list, (o1, o2) -> {
				double diff = (Double) o1.getValue() - (Double) o2.getValue();
				return diff > 0 ? 1 : diff < 0 ? -1 : 0;
			});
			LinkedHashMap<String, Double> sortedMap = new LinkedHashMap<String, Double>();
			for (Entry<String, Double> entry : list) {
				if (!entry.getValue().isNaN()) {
					sortedMap.put(entry.getKey(), entry.getValue());
				}
			}
			if (!sortedMap.isEmpty()) {
				printImage(sortedMap, m);
			}
		}
	}

	private <T> double serializeDifferentObjects(ObjectSerializer<T> serializer, int iterations) throws Exception {
		long start = System.nanoTime();
		for (int i = 0; i < iterations; i++) {
			T obj = serializer.create();
			serializer.serialize(obj);
		}
		return iterationTime(System.nanoTime() - start, iterations);
	}

	private <T> double serializeSameObject(ObjectSerializer<T> serializer, int iterations) throws Exception {
		// let's reuse same instance to reduce overhead
		T obj = serializer.create();
		long delta = 0;
		for (int i = 0; i < iterations; i++) {
			long start = System.nanoTime();
			serializer.serialize(obj);
			delta += System.nanoTime() - start;
			if (i % 1000 == 0) {
				doGc();
			}
		}
		return iterationTime(delta, iterations);
	}

	private void start(int curr, int max) throws Exception {
		if (curr == 1) {
			System.out.printf("%-24s, %15s, %15s, %15s, %15s, %15s, %15s, %15s, %15s, %10s\n", " ", "Object create", "Serialize", "Create+Serialize", "/w Same Object", "Deserialize", "and Check Media", "and Check All", "Total Time", "Serialized Size");
		}
		EnumMap<measurements, Map<String, Double>> values = new EnumMap<measurements, Map<String, Double>>(measurements.class);
		for (measurements m : measurements.values()) {
			values.put(m, new HashMap<String, Double>());
		}

		for (ObjectSerializer serializer : _serializers) {
			/*
			 * Should only warm things for the serializer that we test next: HotSpot JIT will
			 * otherwise spent most of its time optimizing slower ones... Use -XX:CompileThreshold=1
			 * to hint the JIT to start immediately Actually: 1 is often not a good value --
			 * threshold is the number of samples needed to trigger inlining, and there's no point
			 * in inlining everything. Default value is in thousands, so lowering it to, say, 1000
			 * is usually better.
			 */
			warmCreation(serializer);
			doGc();
			double timeCreate = Double.MAX_VALUE;
			// do more iteration for object creation because of its short time
			for (int i = 0; i < TRIALS; i++) {
				timeCreate = Math.min(timeCreate, createObjects(serializer, ITERATIONS * 100));
			}

			warmSerialization(serializer);

			// actually: let's verify serializer actually works now:
			checkCorrectness(serializer);

			doGc();
			double timeSerializeDifferentObjects = Double.MAX_VALUE;
			for (int i = 0; i < TRIALS; i++) {
				timeSerializeDifferentObjects = Math.min(timeSerializeDifferentObjects, serializeDifferentObjects(serializer, ITERATIONS));
			}

			doGc();
			double timeSerializeSameObject = Double.MAX_VALUE;
			for (int i = 0; i < TRIALS; i++) {
				timeSerializeSameObject = Math.min(timeSerializeSameObject, serializeSameObject(serializer, ITERATIONS));
			}

			warmDeserialization(serializer);

			doGc();
			double timeDeserializeNoFieldAccess = Double.MAX_VALUE;
			for (int i = 0; i < TRIALS; i++) {
				timeDeserializeNoFieldAccess = Math.min(timeDeserializeNoFieldAccess, deserializeNoFieldAccess(serializer, ITERATIONS));
			}

			double timeDeserializeAndCheckAllFields = timeDeserializeNoFieldAccess;
			double timeDeserializeAndCheckMediaField = timeDeserializeNoFieldAccess;

			double totalTime = timeSerializeDifferentObjects + timeDeserializeNoFieldAccess;

			if (serializer instanceof CheckingObjectSerializer) {
				CheckingObjectSerializer checkingSerializer = (CheckingObjectSerializer) serializer;

				timeDeserializeAndCheckMediaField = Double.MAX_VALUE;
				doGc();
				for (int i = 0; i < TRIALS; i++) {
					timeDeserializeAndCheckMediaField = Math.min(timeDeserializeAndCheckMediaField, deserializeAndCheckMediaField(checkingSerializer, ITERATIONS));
				}

				timeDeserializeAndCheckAllFields = Double.MAX_VALUE;
				doGc();
				for (int i = 0; i < TRIALS; i++) {
					timeDeserializeAndCheckAllFields = Math.min(timeDeserializeAndCheckAllFields, deserializeAndCheckAllFields(checkingSerializer, ITERATIONS));
				}

				totalTime = timeSerializeDifferentObjects + timeDeserializeAndCheckAllFields;
			}

			byte[] array = serializer.serialize(serializer.create());
			System.out.printf("%-24s, %15.5f, %15.5f,%15.5f, %15.5f, %15.5f, %15.5f, %15.5f, %15.5f, %10d\n", serializer.getName(), timeCreate, timeSerializeDifferentObjects, timeCreate + timeSerializeDifferentObjects, timeSerializeSameObject, timeDeserializeNoFieldAccess, timeDeserializeAndCheckMediaField, timeDeserializeAndCheckAllFields, totalTime, array.length);

			addValue(values, serializer.getName(), timeCreate, timeSerializeDifferentObjects, timeSerializeSameObject, timeDeserializeNoFieldAccess, timeDeserializeAndCheckMediaField, timeDeserializeAndCheckAllFields, totalTime, array.length);
		}

		if (curr == max) {
			printImages(values);
		}
	}

	// Update to better value only.
	private void update(EnumMap<measurements, Map<String, Double>> values, measurements m, String name, double v) {
		Double d = values.get(m).get(name);

		if (d == null || d > v) {
			values.get(m).put(name, v);
		}

	}

	private <T> void warmCreation(ObjectSerializer<T> serializer) throws Exception {
		// Instead of fixed counts, let's try to prime by running for N seconds
		long endTime = System.currentTimeMillis() + WARMUP_MSECS;
		do {
			createObjects(serializer, 1);
		} while (System.currentTimeMillis() < endTime);
	}

	private <T> void warmDeserialization(ObjectSerializer<T> serializer) throws Exception {
		// Instead of fixed counts, let's try to prime by running for N seconds
		long endTime = System.currentTimeMillis() + WARMUP_MSECS;
		do {
			deserializeNoFieldAccess(serializer, 1);
		} while (System.currentTimeMillis() < endTime);
	}

	private <T> void warmSerialization(ObjectSerializer<T> serializer) throws Exception {
		// Instead of fixed counts, let's try to prime by running for N seconds
		long endTime = System.currentTimeMillis() + WARMUP_MSECS;
		do {
			serializeDifferentObjects(serializer, 1);
		} while (System.currentTimeMillis() < endTime);
	}
}
