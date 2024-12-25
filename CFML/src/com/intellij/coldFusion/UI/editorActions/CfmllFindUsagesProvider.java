// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.intellij.coldFusion.UI.editorActions;

import com.intellij.coldFusion.CfmlBundle;
import com.intellij.coldFusion.model.psi.CfmlReferenceExpression;
import com.intellij.coldFusion.model.psi.CfmlTag;
import com.intellij.coldFusion.model.psi.impl.CfmlFunctionImpl;
import com.intellij.coldFusion.model.psi.impl.CfmlFunctionParameterImpl;
import com.intellij.coldFusion.model.psi.impl.CfmlTagFunctionImpl;
import com.intellij.lang.findUsages.FindUsagesProvider;
import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.NotNull;

/**
 * Created by Lera Nikolaenko
 */
public final class CfmllFindUsagesProvider implements FindUsagesProvider {

  @Override
  public boolean canFindUsagesFor(final @NotNull PsiElement psiElement) {
    return psiElement instanceof CfmlReferenceExpression || (psiElement instanceof CfmlTagFunctionImpl) ||
           (psiElement instanceof CfmlTag && ((CfmlTag)psiElement).getTagName().equalsIgnoreCase("cfargument")) ||
           psiElement instanceof CfmlFunctionImpl || psiElement instanceof CfmlFunctionParameterImpl;
  }

  @Override
  public String getHelpId(final @NotNull PsiElement psiElement) {
    return null;
  }

  @Override
  public @NotNull String getType(final @NotNull PsiElement element) {
    return element instanceof CfmlReferenceExpression
           ? CfmlBundle.message("find.usages.descriptive.name.reference")
           : (element instanceof CfmlTagFunctionImpl) || element instanceof CfmlFunctionImpl
             ? CfmlBundle.message("find.usages.type.function.name")
             : CfmlBundle.message("find.usages.type.argument.name");
  }

  @Override
  public @NotNull String getDescriptiveName(final @NotNull PsiElement element) {
    return element instanceof CfmlReferenceExpression
           ? CfmlBundle.message("find.usages.descriptive.name.reference")
           : (element instanceof CfmlTagFunctionImpl) || element instanceof CfmlFunctionImpl
             ? CfmlBundle.message("find.usages.descriptive.name.function")
             : CfmlBundle.message("find.usages.descriptive.name.argument");
  }

  @Override
  public @NotNull String getNodeText(final @NotNull PsiElement element, final boolean useFullName) {
    return element.getText();
  }
}

