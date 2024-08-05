// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.

package com.intellij.lang.javascript.uml;

import com.intellij.diagram.DiagramCategory;
import com.intellij.diagram.DiagramRelationshipInfo;
import com.intellij.diagram.DiagramRelationshipManager;
import com.intellij.uml.utils.DiagramBundle;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class FlashUmlRelationshipManager implements DiagramRelationshipManager<Object> {
  private static final DiagramCategory[] CATEGORIES = {new DiagramCategory(DiagramBundle.messagePointer("category.name.dependencies"), null)};

  @Override
  public @Nullable DiagramRelationshipInfo getDependencyInfo(Object e1, Object e2, DiagramCategory category) {
    return null;
  }

  @Override
  public DiagramCategory @NotNull [] getContentCategories() {
    return CATEGORIES;
  }
}
