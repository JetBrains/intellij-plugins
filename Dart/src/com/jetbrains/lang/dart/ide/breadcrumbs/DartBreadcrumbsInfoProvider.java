// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.jetbrains.lang.dart.ide.breadcrumbs;

import com.intellij.lang.Language;
import com.intellij.navigation.NavigationItem;
import com.intellij.psi.PsiElement;
import com.intellij.ui.breadcrumbs.BreadcrumbsProvider;
import com.jetbrains.lang.dart.DartLanguage;
import com.jetbrains.lang.dart.psi.*;
import org.jetbrains.annotations.NotNull;

public class DartBreadcrumbsInfoProvider implements BreadcrumbsProvider {
  private static final Language[] ourLanguages = {DartLanguage.INSTANCE};

  @Override
  public Language[] getLanguages() {
    return ourLanguages;
  }

  @Override
  public boolean acceptElement(@NotNull PsiElement e) {
    return e instanceof DartClass ||
           e instanceof DartGetterDeclaration ||
           e instanceof DartSetterDeclaration ||
           e instanceof DartMethodDeclaration ||
           e instanceof DartFunctionDeclarationWithBody ||
           e instanceof DartFunctionDeclarationWithBodyOrNative;
  }

  @NotNull
  @Override
  public String getElementInfo(@NotNull PsiElement e) {
    return getPresentableText(e);
  }

  @NotNull
  private static String getPresentableText(@NotNull final PsiElement e) {
    String name = "";
    if (e instanceof NavigationItem) {
      name = ((NavigationItem)e).getName();
    }
    String prefix = "";
    if (e instanceof DartGetterDeclaration) {
      prefix = "get ";
    }
    else if (e instanceof DartSetterDeclaration) {
      prefix = "set ";
    }
    String suffix = "";
    if (e instanceof DartSetterDeclaration ||
        e instanceof DartMethodDeclaration ||
        e instanceof DartFunctionDeclarationWithBody ||
        e instanceof DartFunctionDeclarationWithBodyOrNative) {
      suffix = "()";
    }
    return prefix + name + suffix;
  }
}
