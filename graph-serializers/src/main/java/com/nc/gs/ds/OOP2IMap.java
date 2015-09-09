package com.nc.gs.ds;

import java.util.concurrent.ThreadLocalRandom;

import com.nc.gs.util.Bits;
import com.nc.gs.util.Utils;

// Slightly Modified version of Kryo's IdentityIntMap
public class OOP2IMap<K> {
	// private static final int PRIME1 = 0xbe1f14b1;
	private static final int PRIME2 = 0xb4b82e39;
	private static final int PRIME3 = 0xced1c241;

	public int size;

	K[] keyTable;
	int[] valueTable;
	int capacity, stashSize;

	final int nev;

	private float loadFactor;
	private int hashShift, mask, threshold;
	private int stashCapacity;
	private int pushIterations;

	/**
	 * Creates a new map with an initial capacity of 32 and a load factor of 0.8. This map will hold
	 * 25 items before growing the backing table.
	 */
	public OOP2IMap() {
		this(32, 0.8f, -1);
	}

	/**
	 * Creates a new map with a load factor of 0.8. This map will hold initialCapacity * 0.8 items
	 * before growing the backing table.
	 */
	public OOP2IMap(int initialCapacity) {
		this(initialCapacity, 0.8f, -1);
	}

	/**
	 * Creates a new map with the specified initial capacity and load factor. This map will hold
	 * initialCapacity * loadFactor items before growing the backing table.
	 */
	@SuppressWarnings("unchecked")
	public OOP2IMap(int initialCapacity, float loadFactor, int noEntryVal) {
		if (initialCapacity < 0) {
			throw new IllegalArgumentException("initialCapacity must be >= 0: " + initialCapacity);
		}
		if (capacity > 1 << 30) {
			throw new IllegalArgumentException("initialCapacity is too large: " + initialCapacity);
		}
		capacity = Utils.nextPowerOfTwo(initialCapacity);

		if (loadFactor <= 0) {
			throw new IllegalArgumentException("loadFactor must be > 0: " + loadFactor);
		}
		this.loadFactor = loadFactor;

		threshold = (int) (capacity * loadFactor);
		mask = capacity - 1;
		hashShift = 31 - Integer.numberOfTrailingZeros(capacity);
		stashCapacity = Math.max(3, (int) Math.ceil(Math.log(capacity)) * 2);
		pushIterations = Math.max(Math.min(capacity, 8), (int) Math.sqrt(capacity) / 8);

		keyTable = (K[]) new Object[capacity + stashCapacity];
		valueTable = new int[keyTable.length];
		this.nev = noEntryVal;
	}

	public void clear() {
		// for (int i = capacity + stashSize; i-- > 0;) {
		// keyTable[i] = null;
		// }
		if (size > 0) {
			Bits.clearFast(keyTable);
			size = 0;
		}
		stashSize = 0;
	}

	/**
	 * Clears the map and reduces the size of the backing arrays to be the specified capacity if
	 * they are larger.
	 */
	public void clear(int maximumCapacity) {
		if (capacity <= maximumCapacity) {
			clear();
			return;
		}
		size = 0;
		resize(maximumCapacity);
	}

	public boolean containsKey(K key) {
		final int hashCode = System.identityHashCode(key);
		int index = hashCode & mask;
		final K[] kt = keyTable;
		if (key != kt[index]) {
			index = hash2(hashCode);
			if (key != kt[index]) {
				index = hash3(hashCode);
				if (key != kt[index]) {
					return containsKeyStash(key);
				}
			}
		}
		return true;
	}

	private boolean containsKeyStash(K key) {
		final K[] keyTable = this.keyTable;
		for (int i = capacity, n = i + stashSize; i < n; i++) {
			if (key == keyTable[i]) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Returns true if the specified value is in the map. Note this traverses the entire map and
	 * compares every value, which may be an expensive operation.
	 */
	public boolean containsValue(int value) {
		final int[] valueTable = this.valueTable;
		for (int i = capacity + stashSize; i-- > 0;) {
			if (valueTable[i] == value) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Increases the size of the backing array to acommodate the specified number of additional
	 * items. Useful before adding many items to avoid multiple backing array resizes.
	 */
	public void ensureCapacity(int additionalCapacity) {
		final int sizeNeeded = size + additionalCapacity;
		if (sizeNeeded >= threshold) {
			resize(Utils.nextPowerOfTwo((int) (sizeNeeded / loadFactor)));
		}
	}

	/**
	 * Returns the key for the specified value, or null if it is not in the map. Note this traverses
	 * the entire map and compares every value, which may be an expensive operation.
	 */
	public K findKey(int value) {
		final int[] valueTable = this.valueTable;
		for (int i = capacity + stashSize; i-- > 0;) {
			if (valueTable[i] == value) {
				return keyTable[i];
			}
		}
		return null;
	}

	public final int get(Object key) {
		return get(key, nev);
	}

	/**
	 * @param defaultValue
	 *            Returned if the key was not associated with a value.
	 */
	public int get(Object key, int defaultValue) {
		final int hashCode = System.identityHashCode(key);
		int index = hashCode & mask;
		if (key != keyTable[index]) {
			index = hash2(hashCode);
			if (key != keyTable[index]) {
				index = hash3(hashCode);
				if (key != keyTable[index]) {
					return getStash(key, defaultValue);
				}
			}
		}
		return valueTable[index];
	}

	/**
	 * Returns the key's current value and increments the stored value. If the key is not in the
	 * map, defaultValue + increment is put into the map.
	 */
	public int getAndIncrement(K key, int defaultValue, int increment) {
		final int hashCode = System.identityHashCode(key);
		int index = hashCode & mask;
		if (key != keyTable[index]) {
			index = hash2(hashCode);
			if (key != keyTable[index]) {
				index = hash3(hashCode);
				if (key != keyTable[index]) {
					return getAndIncrementStash(key, defaultValue, increment);
				}
			}
		}
		final int value = valueTable[index];
		valueTable[index] = value + increment;
		return value;
	}

	private int getAndIncrementStash(K key, int defaultValue, int increment) {
		final K[] keyTable = this.keyTable;
		for (int i = capacity, n = i + stashSize; i < n; i++) {
			if (key == keyTable[i]) {
				final int value = valueTable[i];
				valueTable[i] = value + increment;
				return value;
			}
		}
		put(key, defaultValue + increment);
		return defaultValue;
	}

	private int getStash(Object key, int defaultValue) {
		final K[] keyTable = this.keyTable;
		for (int i = capacity, n = i + stashSize; i < n; i++) {
			if (key == keyTable[i]) {
				return valueTable[i];
			}
		}
		return defaultValue;
	}

	private int hash2(int h) {
		h *= PRIME2;
		return (h ^ h >>> hashShift) & mask;
	}

	private int hash3(int h) {
		h *= PRIME3;
		return (h ^ h >>> hashShift) & mask;
	}

	private void push(K insertKey, int insertValue, int index1, K key1, int index2, K key2, int index3, K key3) {
		final K[] keyTable = this.keyTable;
		final int[] valueTable = this.valueTable;
		final int mask = this.mask;

		// Push keys until an empty bucket is found.
		K evictedKey;
		int evictedValue;
		int i = 0;
		final int pushIterations = this.pushIterations;
		final ThreadLocalRandom random = ThreadLocalRandom.current();
		do {
			// Replace the key and value for one of the hashes.
			switch (random.nextInt(3)) {
			case 0:
				evictedKey = key1;
				evictedValue = valueTable[index1];
				keyTable[index1] = insertKey;
				valueTable[index1] = insertValue;
				break;
			case 1:
				evictedKey = key2;
				evictedValue = valueTable[index2];
				keyTable[index2] = insertKey;
				valueTable[index2] = insertValue;
				break;
			default:
				evictedKey = key3;
				evictedValue = valueTable[index3];
				keyTable[index3] = insertKey;
				valueTable[index3] = insertValue;
				break;
			}

			// If the evicted key hashes to an empty bucket, put it there and stop.
			final int hashCode = System.identityHashCode(evictedKey);
			index1 = hashCode & mask;
			key1 = keyTable[index1];
			if (key1 == null) {
				keyTable[index1] = evictedKey;
				valueTable[index1] = evictedValue;
				if (size++ >= threshold) {
					resize(capacity << 1);
				}
				return;
			}

			index2 = hash2(hashCode);
			key2 = keyTable[index2];
			if (key2 == null) {
				keyTable[index2] = evictedKey;
				valueTable[index2] = evictedValue;
				if (size++ >= threshold) {
					resize(capacity << 1);
				}
				return;
			}

			index3 = hash3(hashCode);
			key3 = keyTable[index3];
			if (key3 == null) {
				keyTable[index3] = evictedKey;
				valueTable[index3] = evictedValue;
				if (size++ >= threshold) {
					resize(capacity << 1);
				}
				return;
			}

			if (++i == pushIterations) {
				break;
			}

			insertKey = evictedKey;
			insertValue = evictedValue;
		} while (true);

		putStash(evictedKey, evictedValue);
	}

	public void put(K key, int value) {
		if (key == null) {
			throw new NullPointerException("key cannot be null.");
		}
		final K[] kt = this.keyTable;
		final int[] vt = valueTable;

		// Check for existing keys.
		final int hashCode = System.identityHashCode(key);
		final int index1 = hashCode & mask;
		final K key1 = kt[index1];
		if (key == key1) {
			vt[index1] = value;
			return;
		}

		final int index2 = hash2(hashCode);
		final K key2 = kt[index2];
		if (key == key2) {
			vt[index2] = value;
			return;
		}

		final int index3 = hash3(hashCode);
		final K key3 = kt[index3];
		if (key == key3) {
			vt[index3] = value;
			return;
		}

		// Update key in the stash.
		for (int i = capacity, n = i + stashSize; i < n; i++) {
			if (kt[i] == key) {
				vt[i] = value;
				return;
			}
		}

		// Check for empty buckets.
		if (key1 == null) {
			kt[index1] = key;
			vt[index1] = value;
			if (size++ >= threshold) {
				resize(capacity << 1);
			}
			return;
		}

		if (key2 == null) {
			kt[index2] = key;
			vt[index2] = value;
			if (size++ >= threshold) {
				resize(capacity << 1);
			}
			return;
		}

		if (key3 == null) {
			kt[index3] = key;
			vt[index3] = value;
			if (size++ >= threshold) {
				resize(capacity << 1);
			}
			return;
		}

		push(key, value, index1, key1, index2, key2, index3, key3);
	}

	public int putIfAbsent(K key, int value) {
		if (key == null) {
			throw new NullPointerException("key cannot be null.");
		}

		final K[] kt = this.keyTable;
		final int[] vt = valueTable;

		// Check for existing keys.
		final int hashCode = System.identityHashCode(key);
		final int index1 = hashCode & mask;
		final K key1 = kt[index1];
		if (key == key1) {
			return vt[index1];
		}

		final int index2 = hash2(hashCode);
		final K key2 = kt[index2];
		if (key == key2) {
			return vt[index2];
		}

		final int index3 = hash3(hashCode);
		final K key3 = kt[index3];
		if (key == key3) {
			return vt[index3];
		}

		// Update key in the stash.
		for (int i = capacity, n = i + stashSize; i < n; i++) {
			if (kt[i] == key) {
				return vt[i];
			}
		}

		// Check for empty buckets.
		if (key1 == null) {
			kt[index1] = key;
			vt[index1] = value;
			if (size++ >= threshold) {
				resize(capacity << 1);
			}
			return nev;
		}

		if (key2 == null) {
			kt[index2] = key;
			vt[index2] = value;
			if (size++ >= threshold) {
				resize(capacity << 1);
			}
			return nev;
		}

		if (key3 == null) {
			kt[index3] = key;
			vt[index3] = value;
			if (size++ >= threshold) {
				resize(capacity << 1);
			}
			return nev;
		}

		push(key, value, index1, key1, index2, key2, index3, key3);

		return nev;
	}

	/** Skips checks for existing keys. */
	private void putResize(K key, int value) {
		// Check for empty buckets.
		final int hashCode = System.identityHashCode(key);
		final int index1 = hashCode & mask;
		final K key1 = keyTable[index1];
		if (key1 == null) {
			keyTable[index1] = key;
			valueTable[index1] = value;
			if (size++ >= threshold) {
				resize(capacity << 1);
			}
			return;
		}

		final int index2 = hash2(hashCode);
		final K key2 = keyTable[index2];
		if (key2 == null) {
			keyTable[index2] = key;
			valueTable[index2] = value;
			if (size++ >= threshold) {
				resize(capacity << 1);
			}
			return;
		}

		final int index3 = hash3(hashCode);
		final K key3 = keyTable[index3];
		if (key3 == null) {
			keyTable[index3] = key;
			valueTable[index3] = value;
			if (size++ >= threshold) {
				resize(capacity << 1);
			}
			return;
		}

		push(key, value, index1, key1, index2, key2, index3, key3);
	}

	private void putStash(K key, int value) {
		if (stashSize == stashCapacity) {
			// Too many pushes occurred and the stash is full, increase the table size.
			resize(capacity << 1);
			put(key, value);
			return;
		}
		// Store key in the stash.
		final int index = capacity + stashSize;
		keyTable[index] = key;
		valueTable[index] = value;
		stashSize++;
		size++;
	}

	public int remove(K key, int defaultValue) {
		final int hashCode = System.identityHashCode(key);
		int index = hashCode & mask;
		if (key == keyTable[index]) {
			keyTable[index] = null;
			final int oldValue = valueTable[index];
			size--;
			return oldValue;
		}

		index = hash2(hashCode);
		if (key == keyTable[index]) {
			keyTable[index] = null;
			final int oldValue = valueTable[index];
			size--;
			return oldValue;
		}

		index = hash3(hashCode);
		if (key == keyTable[index]) {
			keyTable[index] = null;
			final int oldValue = valueTable[index];
			size--;
			return oldValue;
		}

		return removeStash(key, defaultValue);
	}

	int removeStash(K key, int defaultValue) {
		final K[] keyTable = this.keyTable;
		for (int i = capacity, n = i + stashSize; i < n; i++) {
			if (key == keyTable[i]) {
				final int oldValue = valueTable[i];
				removeStashIndex(i);
				size--;
				return oldValue;
			}
		}
		return defaultValue;
	}

	void removeStashIndex(int index) {
		// If the removed location was not last, move the last tuple to the removed location.
		stashSize--;
		final int lastIndex = capacity + stashSize;
		if (index < lastIndex) {
			keyTable[index] = keyTable[lastIndex];
			valueTable[index] = valueTable[lastIndex];
		}
	}

	@SuppressWarnings("unchecked")
	private void resize(int newSize) {
		final int oldEndIndex = capacity + stashSize;

		capacity = newSize;
		threshold = (int) (newSize * loadFactor);
		mask = newSize - 1;
		hashShift = 31 - Integer.numberOfTrailingZeros(newSize);
		stashCapacity = Math.max(3, (int) Math.ceil(Math.log(newSize)) * 2);
		pushIterations = Math.max(Math.min(newSize, 8), (int) Math.sqrt(newSize) / 8);

		final K[] oldKeyTable = keyTable;
		final int[] oldValueTable = valueTable;

		keyTable = (K[]) new Object[newSize + stashCapacity];
		valueTable = new int[newSize + stashCapacity];

		final int oldSize = size;
		size = 0;
		stashSize = 0;
		if (oldSize > 0) {
			for (int i = 0; i < oldEndIndex; i++) {
				final K key = oldKeyTable[i];
				if (key != null) {
					putResize(key, oldValueTable[i]);
				}
			}
		}
	}

	/**
	 * Reduces the size of the backing arrays to be the specified capacity or less. If the capacity
	 * is already less, nothing is done. If the map contains more items than the specified capacity,
	 * nothing is done.
	 */
	public void shrink(int maximumCapacity) {
		if (maximumCapacity < 0) {
			throw new IllegalArgumentException("maximumCapacity must be >= 0: " + maximumCapacity);
		}
		if (size > maximumCapacity) {
			maximumCapacity = size;
		}
		if (capacity <= maximumCapacity) {
			return;
		}
		maximumCapacity = Utils.nextPowerOfTwo(maximumCapacity);
		resize(maximumCapacity);
	}

	public int size() {
		return size;
	}

	@Override
	public String toString() {
		if (size == 0) {
			return "{}";
		}
		final StringBuilder buffer = new StringBuilder(32);
		buffer.append('{');
		final K[] keyTable = this.keyTable;
		final int[] valueTable = this.valueTable;
		int i = keyTable.length;

		while (i-- > 0) {
			final K key = keyTable[i];
			if (key == null) {
				continue;
			}
			buffer.append(key);
			buffer.append('=');
			buffer.append(valueTable[i]);
			break;
		}

		while (i-- > 0) {
			final K key = keyTable[i];
			if (key == null) {
				continue;
			}
			buffer.append(", ");
			buffer.append(key);
			buffer.append('=');
			buffer.append(valueTable[i]);
		}
		buffer.append('}');

		return buffer.toString();
	}

}