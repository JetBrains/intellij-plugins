// Copyright 2000-2025 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.intellij.lang.javascript.flex;

import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public final class PredefinedImportSet extends ScopedImportSet {
  private final Map<String,Object> storage = new HashMap<>();

  public PredefinedImportSet(@NonNls String ... strings) {
    for(String s: strings) {
      doAppendToMap(storage, s);
    }
  }

  public PredefinedImportSet(Collection<String> strings) {
    for(String s: strings) {
      doAppendToMap(storage, s);
    }
  }

  @Override
  protected @NotNull Map<String, Object> getUpToDateMap(@NotNull PsiElement scope) {
    return storage;
  }
}
