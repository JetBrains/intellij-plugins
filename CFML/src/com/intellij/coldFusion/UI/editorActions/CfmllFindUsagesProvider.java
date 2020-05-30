// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
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
public class CfmllFindUsagesProvider implements FindUsagesProvider {

  @Override
  public boolean canFindUsagesFor(@NotNull final PsiElement psiElement) {
    return psiElement instanceof CfmlReferenceExpression || (psiElement instanceof CfmlTagFunctionImpl) ||
           (psiElement instanceof CfmlTag && ((CfmlTag)psiElement).getTagName().equalsIgnoreCase("cfargument")) ||
           psiElement instanceof CfmlFunctionImpl || psiElement instanceof CfmlFunctionParameterImpl;
  }

  @Override
  public String getHelpId(@NotNull final PsiElement psiElement) {
    return null;
  }

  @Override
  @NotNull
  public String getType(@NotNull final PsiElement element) {
    return element instanceof CfmlReferenceExpression
           ? CfmlBundle.message("find.usages.descriptive.name.reference")
           : (element instanceof CfmlTagFunctionImpl) || element instanceof CfmlFunctionImpl
             ? CfmlBundle.message("find.usages.type.function.name")
             : CfmlBundle.message("find.usages.type.argument.name");
  }

  @Override
  @NotNull
  public String getDescriptiveName(@NotNull final PsiElement element) {
    return element instanceof CfmlReferenceExpression
           ? CfmlBundle.message("find.usages.descriptive.name.reference")
           : (element instanceof CfmlTagFunctionImpl) || element instanceof CfmlFunctionImpl
             ? CfmlBundle.message("find.usages.descriptive.name.function")
             : CfmlBundle.message("find.usages.descriptive.name.argument");
  }

  @Override
  @NotNull
  public String getNodeText(@NotNull final PsiElement element, final boolean useFullName) {
    return element.getText();
  }
}

