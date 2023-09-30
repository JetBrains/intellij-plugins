// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.

package com.intellij.jhipster.model;

import com.intellij.util.containers.ContainerUtil;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public final class JdlEnumMapping extends JdlOptionMapping {
  public <T extends Enum<T> & JdlModelEnum> JdlEnumMapping(@NotNull String name, @NotNull Class<T> enumClass) {
    super(name, new JdlEnumType<>(enumClass));
  }

  public <T extends Enum<T> & JdlModelEnum> JdlEnumMapping(@NotNull String name, @NotNull Class<T> enumClass, @NotNull T defaultValue) {
    super(name, new JdlEnumType<>(enumClass), defaultValue.getId());
  }

  @Override
  public @NotNull JdlEnumType<?> getPropertyType() {
    return (JdlEnumType<?>)super.getPropertyType();
  }

  public List<String> getOptions() {
    return ContainerUtil.map(getPropertyType().getValues(), JdlModelEnum::getId);
  }
}
