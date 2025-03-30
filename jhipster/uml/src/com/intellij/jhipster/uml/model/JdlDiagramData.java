// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.

package com.intellij.jhipster.uml.model;

import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Objects;

public final class JdlDiagramData {

  private final Collection<JdlEntityNodeData> entities;
  private final Collection<JdlEnumNodeData> enums;
  private final Collection<JdlEntityNodeLink> entityLinks;
  private final Collection<JdlEnumNodeLink> enumLinks;

  public JdlDiagramData(@NotNull Collection<JdlEntityNodeData> entities,
                        @NotNull Collection<JdlEnumNodeData> enums,
                        @NotNull Collection<JdlEntityNodeLink> entityLinks,
                        @NotNull Collection<JdlEnumNodeLink> enumLinks) {
    this.entities = entities;
    this.enums = enums;
    this.entityLinks = entityLinks;
    this.enumLinks = enumLinks;
  }

  public @NotNull Collection<JdlEntityNodeData> getEntities() {
    return entities;
  }

  public @NotNull Collection<JdlEnumNodeData> getEnums() {
    return enums;
  }

  public @NotNull Collection<JdlEntityNodeLink> getEntityLinks() {
    return entityLinks;
  }

  public @NotNull Collection<JdlEnumNodeLink> getEnumLinks() {
    return enumLinks;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    JdlDiagramData that = (JdlDiagramData)o;
    return entities.equals(that.entities) &&
           enums.equals(that.enums) &&
           entityLinks.equals(that.entityLinks) &&
           enumLinks.equals(that.enumLinks);
  }

  @Override
  public int hashCode() {
    return Objects.hash(entities, enums, entityLinks, enumLinks);
  }
}
