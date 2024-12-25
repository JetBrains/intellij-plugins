// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.jetbrains.lang.dart.psi;

import com.intellij.psi.PsiNameIdentifierOwner;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public interface DartComponent extends DartPsiCompositeElement, PsiNameIdentifierOwner {
  @Nullable
  DartComponentName getComponentName();

  boolean isFinal();

  boolean isStatic();

  boolean isPublic();

  boolean isConstructor();

  boolean isGetter();

  boolean isSetter();

  boolean isAbstract();

  boolean isUnitMember();

  boolean isOperator();

  DartMetadata getMetadataByName(final @NotNull String name);

  @NotNull
  List<DartMetadata> getMetadataList();
}
