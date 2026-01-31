// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.

package com.intellij.jhipster.uml.model;

import com.intellij.jhipster.JdlIconsMapping;
import com.intellij.util.containers.ContainerUtil;
import org.jetbrains.annotations.NotNull;

import javax.swing.Icon;
import java.util.List;
import java.util.Objects;

public final class JdlEnumNodeData implements JdlNodeData {
  private final String name;
  private final List<JdlEnumNodeItem> options;

  public JdlEnumNodeData(@NotNull String name, @NotNull List<@NotNull String> options) {
    this.name = name;
    this.options = ContainerUtil.map(options, JdlEnumNodeItem::new);
  }

  @Override
  public @NotNull String getName() {
    return name;
  }

  @Override
  public Icon getIcon() {
    return JdlIconsMapping.getEnumIcon();
  }

  public @NotNull List<@NotNull JdlEnumNodeItem> getOptions() {
    return options;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    JdlEnumNodeData that = (JdlEnumNodeData)o;
    return name.equals(that.name) && options.equals(that.options);
  }

  @Override
  public int hashCode() {
    return Objects.hash(name, options);
  }
}
