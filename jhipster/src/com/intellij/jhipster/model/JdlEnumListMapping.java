// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.

package com.intellij.jhipster.model;

import org.jetbrains.annotations.NotNull;

public final class JdlEnumListMapping extends JdlOptionMapping {
  public <T extends Enum<T> & JdlModelEnum> JdlEnumListMapping(@NotNull String name, @NotNull Class<T> enumClass) {
    super(name, new JdlEnumListType<>(enumClass));
  }

  public <T extends Enum<T> & JdlModelEnum> JdlEnumListMapping(@NotNull String name, @NotNull Class<T> enumClass, @NotNull T defaultValue) {
    super(name, new JdlEnumListType<>(enumClass), defaultValue.getId());
  }
}
