// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.

package com.intellij.jhipster.uml.model;

import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public final class JdlEntityNodeLink {
  private final JdlEntityNodeLinkType type;
  private final JdlEntityNodeData fromEntity;
  private final JdlEntityNodeData toEntity;

  public JdlEntityNodeLink(@NotNull JdlEntityNodeLinkType type,
                           @NotNull JdlEntityNodeData fromEntity,
                           @NotNull JdlEntityNodeData toEntity) {
    this.type = type;
    this.fromEntity = fromEntity;
    this.toEntity = toEntity;
  }

  public @NotNull JdlEntityNodeLinkType getType() {
    return type;
  }

  public @NotNull JdlEntityNodeData getFromEntity() {
    return fromEntity;
  }

  public @NotNull JdlEntityNodeData getToEntity() {
    return toEntity;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    JdlEntityNodeLink that = (JdlEntityNodeLink)o;
    return type == that.type && fromEntity.equals(that.fromEntity) && toEntity.equals(that.toEntity);
  }

  @Override
  public int hashCode() {
    return Objects.hash(type, fromEntity, toEntity);
  }
}
