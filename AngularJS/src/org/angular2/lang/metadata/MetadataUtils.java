// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.lang.metadata;

import com.intellij.json.psi.JsonObject;
import com.intellij.json.psi.JsonProperty;
import com.intellij.json.psi.JsonStringLiteral;
import com.intellij.json.psi.JsonValue;
import com.intellij.openapi.util.Pair;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.stream.Stream;

import static com.intellij.openapi.util.Pair.pair;
import static com.intellij.util.ObjectUtils.tryCast;

public class MetadataUtils {

  public static @NotNull Stream<JsonProperty> streamObjectProperty(@Nullable JsonProperty property) {
    if (property == null || !(property.getValue() instanceof JsonObject)) {
      return Stream.empty();
    }
    return ((JsonObject)property.getValue()).getPropertyList().stream();
  }

  public static @Nullable Pair<String, String> readStringProperty(@Nullable JsonProperty property) {
    if (property != null && property.getValue() instanceof JsonStringLiteral) {
      return pair(property.getName(), ((JsonStringLiteral)property.getValue()).getValue());
    }
    return null;
  }

  public static @Nullable String readStringPropertyValue(@Nullable JsonProperty property) {
    if (property != null && property.getValue() instanceof JsonStringLiteral) {
      return ((JsonStringLiteral)property.getValue()).getValue();
    }
    return null;
  }

  public static @Nullable <T extends JsonValue> T getPropertyValue(@Nullable JsonProperty property, Class<T> valueClass) {
    return property != null ? tryCast(property.getValue(), valueClass) : null;
  }
}
