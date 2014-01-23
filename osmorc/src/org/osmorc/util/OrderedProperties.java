package org.osmorc.util;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.jps.osmorc.util.JpsOrderedProperties;

import java.util.Map;

public class OrderedProperties extends JpsOrderedProperties {

  @NotNull
  public static OrderedProperties fromMap(@NotNull Map<String,String> map) {
    OrderedProperties result = new OrderedProperties();
    doLoadFromMap(result, map);
    return result;
  }
}
