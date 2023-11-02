// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.

package com.intellij.jhipster.uml.model;

import com.intellij.openapi.util.NlsSafe;

import java.util.Objects;

public final class JdlEnumNodeItem {
  private final String name;

  public JdlEnumNodeItem(String name) {
    this.name = name;
  }

  public @NlsSafe String getName() {
    return name;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    JdlEnumNodeItem that = (JdlEnumNodeItem)o;
    return Objects.equals(name, that.name);
  }

  @Override
  public int hashCode() {
    return Objects.hash(name);
  }
}
