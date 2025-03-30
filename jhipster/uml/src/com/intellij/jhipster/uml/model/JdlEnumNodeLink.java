// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.

package com.intellij.jhipster.uml.model;

import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public final class JdlEnumNodeLink {
  private final JdlEntityNodeData entity;
  private final JdlEnumNodeData enumeration;

  public JdlEnumNodeLink(@NotNull JdlEntityNodeData entity, @NotNull JdlEnumNodeData enumeration) {
    this.entity = entity;
    this.enumeration = enumeration;
  }

  public @NotNull JdlEntityNodeData getEntity() {
    return entity;
  }

  public @NotNull JdlEnumNodeData getEnumeration() {
    return enumeration;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    JdlEnumNodeLink that = (JdlEnumNodeLink)o;
    return entity.equals(that.entity) && enumeration.equals(that.enumeration);
  }

  @Override
  public int hashCode() {
    return Objects.hash(entity, enumeration);
  }
}
