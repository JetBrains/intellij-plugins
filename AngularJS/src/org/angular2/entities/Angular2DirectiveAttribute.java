package org.angular2.entities;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.intellij.lang.javascript.psi.JSType;

/**
 * Represents a constructor parameter decorated with {@code Attribute}.
 * This kind of element is always one-time binding, so there is no need to check.
 */
public interface Angular2DirectiveAttribute extends Angular2Element {
  @NotNull
  String getName();

  @Nullable
  JSType getType();
}
