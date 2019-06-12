package org.angular2.entities;

import com.intellij.lang.javascript.psi.JSType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface Angular2DirectiveAttribute extends Angular2Element {
  @NotNull
  String getName();

  @Nullable
  JSType getType();
}
