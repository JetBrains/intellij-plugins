package org.osmorc.util;

import org.jetbrains.annotations.NotNull;

import java.util.*;

/**
 * Implementation of {@link Properties} which has a defined order of elements. Properties will be returned in the same order in which they are inserted.
 */
@SuppressWarnings({"unchecked"})
public class OrderedProperties extends Properties {

  private final LinkedHashSet keys = new LinkedHashSet();

  /**
   * Creates an OrderedProperties instance from a map of String -> String.
   * @param map the map
   * @return the OrderedProperties instance.
   */
  @NotNull
  public static OrderedProperties fromMap(@NotNull Map<String,String> map) {
    OrderedProperties result = new OrderedProperties();
    for (Map.Entry<String, String> entry : map.entrySet()) {
      result.setProperty(entry.getKey(), entry.getValue());
    }
    return result;
  }

  /**
   * Creates a map that represents the contents of this OrderedProperties instance. The returned map provides the same key iteration
   * order as this OrderedProperties instance.
   * 
   * @return the created map.
   */
  @NotNull
  public Map<String,String> toMap() {
    LinkedHashMap<String,String> result = new LinkedHashMap<String, String>(size());
    for (String name : stringPropertyNames()) {
      result.put(name, getProperty(name));
    }
    return result;
  }
  
  public synchronized Enumeration<Object> keys() {
    return Collections.enumeration(keys);
  }


  @Override
  public Set<String> stringPropertyNames() {
    return Collections.unmodifiableSet(keys);
  }

  @Override
  public Enumeration<?> propertyNames() {
    return Collections.enumeration(keys);
  }

  public synchronized Object put(Object key, Object value) {
    keys.add(key);
    //noinspection UseOfPropertiesAsHashtable
    return super.put(key, value);
  }

  @Override
  public synchronized Object remove(Object key) {
    keys.remove(key);
    return super.remove(key);
  }

  @Override
  public synchronized void clear() {
    keys.clear();
    super.clear();
  }
}
