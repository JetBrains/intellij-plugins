/*
 * Copyright 2019 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.intellij.protobuf.ide.views;

import com.intellij.lang.Language;
import com.intellij.protobuf.lang.PbLanguage;
import com.intellij.protobuf.lang.psi.*;
import com.intellij.psi.PsiElement;
import com.intellij.psi.util.QualifiedName;
import com.intellij.ui.breadcrumbs.BreadcrumbsProvider;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/** Generates the breadcrumb bar entries for protobuf files. */
public class PbBreadcrumbsInfoProvider implements BreadcrumbsProvider {
  private static final Language[] LANGUAGES = {PbLanguage.INSTANCE};

  @Override
  public Language[] getLanguages() {
    return LANGUAGES;
  }

  @Override
  public boolean acceptElement(@NotNull PsiElement e) {
    return e instanceof PbNamedElement && ((PbNamedElement) e).getName() != null;
  }

  @Override
  public @NotNull String getElementInfo(@NotNull PsiElement e) {
    String name = ((PbNamedElement) e).getName();
    if (name == null) {
      return getTooltipPrefix(e);
    }
    return name;
  }

  @Override
  public @Nullable String getElementTooltip(@NotNull PsiElement e) {
    QualifiedName name = ((PbNamedElement) e).getQualifiedName();
    return name != null ? String.format("%s <b>%s</b>", getTooltipPrefix(e), name) : null;
  }

  private static String getTooltipPrefix(@NotNull PsiElement element) {
    if (element instanceof PbMessageDefinition) {
      return "message";
    }
    if (element instanceof PbEnumDefinition) {
      return "enum";
    }
    if (element instanceof PbEnumValue) {
      return "enum value";
    }
    if (element instanceof PbGroupDefinition) {
      return "group";
    }
    if (element instanceof PbMapField) {
      return "map";
    }
    if (element instanceof PbField) {
      return "field";
    }
    if (element instanceof PbOneofDefinition) {
      return "oneof";
    }
    if (element instanceof PbServiceDefinition) {
      return "service";
    }
    if (element instanceof PbServiceMethod) {
      return "rpc";
    }
    return "element";
  }
}
