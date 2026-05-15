package com.intellij.lang.javascript.linter.jshint;

import com.google.common.collect.ImmutableMap;
import com.intellij.util.containers.ComparatorUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * @author Sergey Simonchik
 */
public final class JSHintOptionsState {

  private final Map<String, Object> myValueByOptionMap;

  private JSHintOptionsState(@NotNull Map<String, Object> valueByOptionMap) {
    myValueByOptionMap = ImmutableMap.copyOf(valueByOptionMap);
  }

  public @NotNull Set<String> getOptionKeys() {
    return myValueByOptionMap.keySet();
  }

  public @NotNull Map<String, Object> getValueByOptionMap() {
    return myValueByOptionMap;
  }

  public @Nullable Object getValue(@NotNull String optionKey) {
    Object value = myValueByOptionMap.get(optionKey);
    if (value == null) {
      JSHintOption option = JSHintOption.findByName(optionKey);
      if (option != null) {
        return option.getDefaultValue();
      }
    }
    return value;
  }

  public @Nullable Object getValue(@NotNull JSHintOption option) {
    return getValue(option.getKey());
  }

  public static class Builder {
    private final Map<String, Object> myValueByOptionMap;

    public Builder() {
      myValueByOptionMap = new HashMap<>();
    }

    /**
     * @param option jshint option name or "-W{warning number}" or "+W{warning number}"
     * @param value option value
     * @return this builder
     */
    public Builder put(@NotNull JSHintOption option, @Nullable Object value) {
      put(option.getKey(), value);
      return this;
    }

    /**
     * @param key jshint key name or "-W{warning number}" or "+W{warning number}"
     * @param value key value
     * @return this builder
     */
    public Builder put(@NotNull String key, @Nullable Object value) {
      String newKey = key;
      JSHintOption option = JSHintOption.findByName(key);
      if (option != null) {
        newKey = option.getKey();
      }
      if (value == null) {
        myValueByOptionMap.remove(newKey);
      }
      else {
        myValueByOptionMap.put(newKey, value);
      }
      return this;
    }

    public @NotNull JSHintOptionsState build() {
      return new JSHintOptionsState(myValueByOptionMap);
    }
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    JSHintOptionsState that = (JSHintOptionsState)o;
    for (JSHintOption option : JSHintOption.values()) {
      Object thisValue = getValue(option);
      Object thatValue = that.getValue(option);
      if (!ComparatorUtil.equalsNullable(thisValue, thatValue)) {
        return false;
      }
    }
    return true;
  }

  @Override
  public int hashCode() {
    return myValueByOptionMap.hashCode();
  }

  @Override
  public String toString() {
    return myValueByOptionMap.toString();
  }

}
