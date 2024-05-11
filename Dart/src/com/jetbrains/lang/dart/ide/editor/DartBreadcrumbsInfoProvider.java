// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.jetbrains.lang.dart.ide.editor;

import com.intellij.lang.Language;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.psi.PsiElement;
import com.intellij.ui.breadcrumbs.BreadcrumbsProvider;
import com.jetbrains.lang.dart.DartLanguage;
import com.jetbrains.lang.dart.psi.DartComponent;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;


public class DartBreadcrumbsInfoProvider implements BreadcrumbsProvider {
  private static final Logger LOG = Logger.getInstance(DartBreadcrumbsInfoProvider.class);

  @Override
  public Language[] getLanguages() {
    return new Language[]{DartLanguage.INSTANCE};
  }

  @Override
  public boolean acceptElement(@NotNull PsiElement element) {
    return (element instanceof DartComponent && ((DartComponent)element).getName() != null);
  }

  @Override
  public @NotNull String getElementInfo(@NotNull PsiElement element) {
    if (element instanceof DartComponent dartComponent) {
      return Objects.requireNonNullElse(dartComponent.getName(), "");
    }

    LOG.warn("Unexpected element: " + element.getClass().getName());
    return "";
  }
}
