// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.plugins.cucumber.java.steps.reference;

import com.intellij.openapi.util.TextRange;
import com.intellij.pom.PomRenameableTarget;
import com.intellij.pom.PsiDeclaredTarget;
import com.intellij.psi.DelegatePsiTarget;
import com.intellij.psi.JavaPsiFacade;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementFactory;
import com.intellij.psi.PsiIdentifier;
import com.intellij.psi.PsiLiteralExpression;
import com.intellij.psi.PsiManager;
import com.intellij.psi.PsiMethod;
import org.jetbrains.annotations.NotNullByDefault;
import org.jetbrains.annotations.Nullable;

/// Represents a navigation target from a Cucumber `ParameterType` declaration.
///
/// @see org.jetbrains.plugins.cucumber.java.steps.search.CucumberJavaPomDeclarationSearcher CucumberJavaPomDeclarationSearcher
@NotNullByDefault
public final class CucumberJavaParameterPomTarget extends DelegatePsiTarget
  implements PomRenameableTarget<PsiElement>, PsiDeclaredTarget {
  private final String name;

  public CucumberJavaParameterPomTarget(PsiElement element, String name) {
    super(element);
    this.name = name;
  }

  @Override
  public String getName() {
    return name;
  }

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
  public @Nullable PsiElement setName(String newName) {
    PsiElement element = getNavigationElement();
    if (element instanceof PsiLiteralExpression) {
      PsiManager manager = element.getManager();
      PsiElementFactory factory = JavaPsiFacade.getInstance(manager.getProject()).getElementFactory();
      PsiElement newLiteral = factory.createExpressionFromText("\"" + newName + "\"", element);
      return element.replace(newLiteral);
    }
    if (element instanceof PsiIdentifier) {
      if (element.getParent() instanceof PsiMethod method) {
        method.setName(newName);
        return method.getNameIdentifier();
      }
    }
    return null;
  }
}
