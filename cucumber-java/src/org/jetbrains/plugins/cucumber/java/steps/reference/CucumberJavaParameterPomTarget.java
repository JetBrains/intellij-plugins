// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.plugins.cucumber.java.steps.reference;

import com.intellij.openapi.util.TextRange;
import com.intellij.pom.PomRenameableTarget;
import com.intellij.pom.PsiDeclaredTarget;
import com.intellij.psi.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class CucumberJavaParameterPomTarget extends DelegatePsiTarget implements PomRenameableTarget, PsiDeclaredTarget {
  @NotNull
  private final String myName;

  public CucumberJavaParameterPomTarget(@NotNull PsiElement element, @NotNull String name) {
    super(element);
    myName = name;
  }

  @NotNull
  @Override
  public String getName() {
    return myName;
  }

  @Nullable
  @Override
  public TextRange getNameIdentifierRange() {
    PsiElement element = getNavigationElement();
    if (element instanceof PsiIdentifier) {
      // method name
      return TextRange.create(0, element.getTextLength());
    }
    // string argument
    return TextRange.create(1, element.getTextLength() - 1);
  }

  @Override
  public boolean isWritable() {
    return true;
  }

  @Override
  public Object setName(@NotNull String newName) {
    PsiElement element = getNavigationElement();
    if (element instanceof PsiLiteralExpression) {
      PsiManager manager = element.getManager();
      PsiElementFactory factory = JavaPsiFacade.getInstance(manager.getProject()).getElementFactory();
      PsiElement newLiteral = factory.createExpressionFromText("\"" + newName + "\"", element);
      return element.replace(newLiteral);
    }
    return null;
  }
}
