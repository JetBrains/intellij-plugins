package com.intellij.lang.javascript.linter.jshint.rhino;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.mozilla.javascript.ConsString;
import org.mozilla.javascript.NativeArray;
import org.mozilla.javascript.NativeObject;
import org.mozilla.javascript.ScriptableObject;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class RhinoUtil {

  private RhinoUtil() {}

  public static @NotNull Object toRhinoAnyNotNull(@NotNull Object object) {
    if (object instanceof Map) {
      @SuppressWarnings("unchecked")
      Map<String, Object> map = (Map<String, Object>) object;
      return toRhinoMap(map);
    }
    if (object instanceof Number || object instanceof Boolean || object instanceof String) {
      return object;
    }
    if (object instanceof List) {
      //noinspection unchecked
      return toRhinoArray((List<Object>) object);
    }
    if (object instanceof Object[]) {
      throw new RuntimeException("Java arrays aren't supported, please use " + List.class);
    }
    throw new RuntimeException("Unexpected rhino object " + object);
  }

  public static @Nullable Object toRhinoAny(@Nullable Object object) {
    if (object == null) {
      return null;
    }
    return toRhinoAnyNotNull(object);
  }

  public static NativeArray toRhinoArray(@NotNull List<Object> list) {
    Object[] resArray = new Object[list.size()];
    int i = 0;
    for (Object o : list) {
      Object res = toRhinoAny(o);
      resArray[i] = res;
      i++;
    }
    return new NativeArray(resArray);
  }

  public static @NotNull NativeObject toRhinoMap(@NotNull Map<String, ?> map) {
    NativeObject object = new NativeObject();
    for (Map.Entry<String, ?> entry : map.entrySet()) {
      Object nativeObj = toRhinoAny(entry.getValue());
      object.defineProperty(entry.getKey(), nativeObj, ScriptableObject.READONLY);
    }
    return object;
  }

  public static @Nullable Object toJavaAny(@Nullable Object rhinoObject) {
    if (rhinoObject instanceof NativeObject) {
      return toJavaMap((NativeObject)rhinoObject);
    }
    if (rhinoObject instanceof NativeArray nativeArray) {
      return Arrays.asList(nativeArray.toArray());
    }
    return rhinoObject;
  }

  public static @NotNull Map<Object, Object> toJavaMap(@NotNull NativeObject nativeObject) {
    Map<Object, Object> copy = new HashMap<>();
    for (Map.Entry<Object, Object> entry : nativeObject.entrySet()) {
      Object regularKey = toJavaAny(entry.getKey());
      Object regularValue = toJavaAny(entry.getValue());
      copy.put(regularKey, regularValue);
    }
    return copy;
  }

  public static @Nullable String getStringKey(@NotNull ScriptableObject object, @NotNull String key) {
    Object value = object.get(key);
    String str = null;
    if (value instanceof String) {
      str = (String) value;
    }
    else if (value instanceof ConsString consString) {
      str = consString.toString();
    }
    return str;
  }
}
