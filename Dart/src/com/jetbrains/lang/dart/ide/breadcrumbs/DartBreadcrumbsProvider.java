// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.

package com.jetbrains.lang.dart.ide.breadcrumbs;

import com.intellij.lang.Language;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiElement;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.ui.breadcrumbs.BreadcrumbsProvider;
import com.jetbrains.lang.dart.DartLanguage;
import com.jetbrains.lang.dart.psi.*;
import org.jetbrains.annotations.NotNull;

public class DartBreadcrumbsProvider implements BreadcrumbsProvider {
  private static final Language[] LANGUAGES = new Language[]{DartLanguage.INSTANCE};

  @Override
  public Language[] getLanguages() {
    return LANGUAGES;
  }

  @Override
  public boolean acceptElement(@NotNull PsiElement e) {
    return e instanceof DartClass ||
           e instanceof DartEnumConstantDeclaration ||
           e instanceof DartFunctionDeclarationWithBodyOrNative || // top level functions
           e instanceof DartFunctionDeclarationWithBody || // local function declarations
           e instanceof DartMethodDeclaration || // constructors are parsed like this
           e instanceof DartFactoryConstructorDeclaration ||
           e instanceof DartFunctionExpression;
  }

  @NotNull
  @Override
  public String getElementInfo(@NotNull PsiElement e) {
    if (e instanceof DartComponent) {
      final String name = ((DartComponent)e).getName();
      if (!StringUtil.isEmpty(name)) {
        //if (e instanceof DartMethodDeclaration) {
        //  final DartClass dartClass = PsiTreeUtil.getParentOfType(e, DartClass.class);
        //  if (dartClass.getConstructors().contains(e)) {
        //    e.getN
        //  }
        //}


        return name;
      }
    }

    if (!(e instanceof DartFunctionExpression)) {
      throw new AssertionError(String.format("Expected a function declaration, got %s", e.getNode().getElementType()));
    }

    final PsiElement parent = e.getParent();
    if (parent instanceof DartNamedArgument || parent instanceof DartArgumentList) {

      String callTargetName = null;
      final PsiElement call = PsiTreeUtil.findFirstParent(e, element -> element instanceof DartCallExpression || element instanceof DartNewExpression);
      if (call instanceof DartCallExpression) {
        final DartExpression callTargetExpression = ((DartCallExpression)call).getExpression();
        if (callTargetExpression instanceof DartReference) {
          final PsiElement callTarget = ((DartReference)callTargetExpression).resolve();
          if (callTarget instanceof DartNamedElement) {
            callTargetName = ((DartNamedElement)callTarget).getName();
          }
        }
      }

      String parameterName = null;
      if (parent instanceof DartNamedArgument) {
        parameterName = ((DartNamedArgument)parent).getParameterReferenceExpression().getText();
      }

      if (callTargetName != null) {
        if (parameterName != null) {
          return callTargetName + "(" + parameterName + ": <closure>)";
        } else {
          return callTargetName + "(<closure>)";
        }
      }
    }

    return "<closure>";
  }
}
