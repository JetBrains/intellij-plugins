/*
 * Copyright 2000-2009 JetBrains s.r.o.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package com.intellij.lang.javascript.uml;

import com.intellij.diagram.DiagramCategory;
import com.intellij.diagram.DiagramRelationshipInfo;
import com.intellij.diagram.DiagramRelationshipManager;
import com.intellij.uml.utils.DiagramBundle;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class FlashUmlRelationshipManager implements DiagramRelationshipManager<Object> {
  private static final DiagramCategory[] CATEGORIES = {new DiagramCategory(DiagramBundle.message("category.name.dependencies"), null)};

  @Override
  @Nullable
  public DiagramRelationshipInfo getDependencyInfo(Object e1, Object e2, DiagramCategory category) {
    return null;
  }

  @Override
  public DiagramCategory @NotNull [] getContentCategories() {
    return CATEGORIES;
  }
}
