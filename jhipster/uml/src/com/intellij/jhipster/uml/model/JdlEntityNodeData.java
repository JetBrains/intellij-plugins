// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.

package com.intellij.jhipster.uml.model;

import com.intellij.jhipster.JdlIconsMapping;
import org.jetbrains.annotations.NotNull;

import javax.swing.Icon;
import java.util.List;
import java.util.Objects;

public final class JdlEntityNodeData implements JdlNodeData {
  private final String name;
  private final List<JdlEntityNodeField> properties;

  public JdlEntityNodeData(@NotNull String name, @NotNull List<JdlEntityNodeField> properties) {
    this.name = name;
    this.properties = properties;
  }

  @Override
  public @NotNull String getName() {
    return name;
  }

  @Override
  public Icon getIcon() {
    return JdlIconsMapping.getEntityIcon();
  }

  public @NotNull List<JdlEntityNodeField> getProperties() {
    return properties;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    JdlEntityNodeData that = (JdlEntityNodeData)o;
    return name.equals(that.name) && properties.equals(that.properties);
  }

  @Override
  public int hashCode() {
    return Objects.hash(name, properties);
  }
}
