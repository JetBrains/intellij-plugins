// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.

package com.intellij.jhipster.model;

import org.jetbrains.annotations.NotNull;

public final class JdlPrimitiveType implements JdlOptionType {

  public static final JdlOptionType STRING_TYPE = new JdlPrimitiveType("String");
  public static final JdlOptionType STRING_ARRAY_TYPE = new JdlPrimitiveType("String[]");
  public static final JdlOptionType BOOLEAN_TYPE = new JdlPrimitiveType("Boolean");
  public static final JdlOptionType INTEGER_TYPE = new JdlPrimitiveType("Integer");

  public final String name;

  public JdlPrimitiveType(@NotNull String name) {
    this.name = name;
  }

  @Override
  public @NotNull String getName() {
    return name;
  }
}
