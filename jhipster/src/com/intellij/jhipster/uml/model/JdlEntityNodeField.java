// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.

package com.intellij.jhipster.uml.model;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public final class JdlEntityNodeField {
  private final String name;
  private final String type;
  private final boolean required;

  public JdlEntityNodeField(@NotNull String name, @Nullable String type, boolean required) {
    this.name = name;
    this.type = type;
    this.required = required;
  }

  public @NotNull String getName() {
    return name;
  }

  public @Nullable String getType() {
    return type;
  }

  public boolean isRequired() {
    return required;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    JdlEntityNodeField that = (JdlEntityNodeField)o;
    return required == that.required && name.equals(that.name) && Objects.equals(type, that.type);
  }

  @Override
  public int hashCode() {
    return Objects.hash(name, type, required);
  }
}
