// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.

package com.intellij.jhipster.uml;

import com.intellij.diagram.AbstractDiagramNodeContentManager;
import com.intellij.diagram.DiagramBuilder;
import com.intellij.diagram.DiagramCategory;
import com.intellij.icons.AllIcons;
import com.intellij.jhipster.JdlBundle;
import com.intellij.jhipster.uml.model.JdlEntityNodeField;
import com.intellij.jhipster.uml.model.JdlEnumNodeItem;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

final class JdlUmlCategoryManager extends AbstractDiagramNodeContentManager {
  private static final @NotNull DiagramCategory FIELDS = new DiagramCategory(() -> JdlBundle.message("fields"), AllIcons.Nodes.Field, true, false);
  private static final @NotNull DiagramCategory ENUM_VALUES = new DiagramCategory(() -> JdlBundle.message("enum.values"), AllIcons.Nodes.Enum, true, false);

  private static final DiagramCategory[] CATEGORIES = new DiagramCategory[]{ENUM_VALUES, FIELDS};

  @Override
  public DiagramCategory @NotNull [] getContentCategories() {
    return CATEGORIES;
  }

  @Override
  public boolean isInCategory(@Nullable Object nodeElement, @Nullable Object item,
                              @NotNull DiagramCategory category, @Nullable DiagramBuilder builder) {
    if (nodeElement instanceof JdlEntityNodeField) {
      return category == FIELDS;
    }
    if (nodeElement instanceof JdlEnumNodeItem) {
      return category == ENUM_VALUES;
    }
    return super.isInCategory(nodeElement, item, category, builder);
  }
}
