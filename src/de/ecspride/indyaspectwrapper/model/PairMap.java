package de.ecspride.indyaspectwrapper.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

/**
 * A Map to store two values for a key. Fixes the lack of the generic Map class
 * only storing one value for a key. Uses a HashMap internally.
 * 
 * @param <K>
 *            the key
 * @param <V1>
 *            value one
 * @param <V2>
 *            value two
 */
public class PairMap<K, V1, V2> {

	Map<K, List<?>> hmap = new HashMap<K, List<?>>();

	/**
	 * Returns the size of the map.
	 * 
	 * @return size of the map
	 */
	public int size() {
		return hmap.size();
	}

	/**
	 * Checks if the map is empty.
	 * 
	 * @return Is the map empty?
	 */
	public boolean isEmpty() {
		return hmap.isEmpty();
	}

	/**
	 * Checks if the map contains the given key.
	 * 
	 * @param key
	 *            the key
	 * @return Is the key in the map?
	 */
	public boolean containsKey(K key) {
		return hmap.containsKey(key);
	}

	/**
	 * Checks if the map contains the given value pair. Gets a list that should
	 * contain two values matching the defined type of the map.
	 * 
	 * @param values
	 *            a list of values
	 * @return Are the values in the map?
	 */
	public boolean containsValues(List<?> values) {
		return hmap.containsValue(values);
	}

	/**
	 * Checks if the map contains the given first value.
	 * 
	 * @param value
	 *            the first value
	 * @return Is the given value in the map as first value?
	 */
	public boolean containsFirstValue(V1 value) {
		for (Entry<K, List<?>> e : hmap.entrySet()) {
			if (e.getValue().get(0).equals(value)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Checks if the map contains the given second value.
	 * 
	 * @param value
	 *            the second value
	 * @return Is the given value in the map as second value?
	 */
	public boolean containsSecondValue(V2 value) {
		for (Entry<K, List<?>> e : hmap.entrySet()) {
			if (e.getValue().get(1).equals(value)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Returns the values for the given key as list.
	 * 
	 * @param key
	 *            the key
	 * @return the values for the key
	 */
	public List<?> get(K key) {
		return hmap.get(key);
	}

	/**
	 * Returns the first value for the given key.
	 * 
	 * @param key
	 *            the key
	 * @return the first value for the key
	 */
	@SuppressWarnings("unchecked")
	public V1 getFirst(K key) {
		return (V1) hmap.get(key).get(0);
	}

	/**
	 * Returns the second value for the given key.
	 * 
	 * @param key
	 *            the key
	 * @return the second value for the key
	 */
	@SuppressWarnings("unchecked")
	public V2 getSecond(K key) {
		return (V2) hmap.get(key).get(1);
	}

	/**
	 * Insert a new key with values. Returns the old entry.
	 * 
	 * @param key
	 *            the key
	 * @param value1
	 *            first value
	 * @param value2
	 *            second value
	 * @return the old entry
	 */
	public Object put(K key, V1 value1, V2 value2) {
		List<Object> list = new ArrayList<>();
		list.add(value1);
		list.add(value2);
		return hmap.put(key, list);
	}

	/**
	 * Removes the entry for the given key.
	 * 
	 * @param key
	 *            the key
	 * @return the removed entry
	 */
	public Object remove(Object key) {
		return hmap.remove(key);
	}

	/**
	 * Put the given PairMap at the end of the PairMap. This should remain
	 * consistent if implementation of putAll changes in Map.
	 * 
	 * @param pairMap
	 *            the PairMap
	 */
	public void putAll(PairMap<K, V1, V2> pairMap) {
		Map<K, List<?>> putMap = new HashMap<K, List<?>>();
		for (Entry<K, List<?>> e : pairMap.entrySet()) {
			putMap.put(e.getKey(), e.getValue());
		}
		hmap.putAll(putMap);

	}

	/**
	 * Clears the PairMap.
	 */
	public void clear() {
		hmap.clear();
	}

	/**
	 * Returns the key set of the PairMap.
	 * 
	 * @return the key set
	 */
	public Set<?> keySet() {
		return hmap.keySet();
	}

	/**
	 * Returns the values of the PairMap as Collection.
	 * 
	 * @return the values
	 */
	public Collection<?> values() {
		return hmap.values();
	}

	/**
	 * Returns the entry set of the PairMap.
	 * 
	 * @return the entry set
	 */
	public Set<Entry<K, List<?>>> entrySet() {
		return hmap.entrySet();
	}

}
