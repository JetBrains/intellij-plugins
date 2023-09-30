// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.

package com.intellij.jhipster.model;

import org.jetbrains.annotations.NotNull;

import java.util.List;

public final class JdlEnumType<E extends Enum<E> & JdlModelEnum> implements JdlOptionType {
  private final List<E> values;

  public JdlEnumType(Class<E> enumClass) {
    this.values = List.of(enumClass.getEnumConstants());
  }

  public List<E> getValues() {
    return values;
  }

  @Override
  public @NotNull String getName() {
    return "Enum";
  }
}
