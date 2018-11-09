// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.intellij.coldFusion.UI.editorActions;

import com.intellij.coldFusion.model.psi.CfmlReferenceExpression;
import com.intellij.coldFusion.model.psi.CfmlTag;
import com.intellij.coldFusion.model.psi.impl.CfmlFunctionImpl;
import com.intellij.coldFusion.model.psi.impl.CfmlFunctionParameterImpl;
import com.intellij.coldFusion.model.psi.impl.CfmlTagFunctionImpl;
import com.intellij.lang.cacheBuilder.WordsScanner;
import com.intellij.lang.findUsages.FindUsagesProvider;
import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.NotNull;

/**
 * Created by Lera Nikolaenko
 */
public class CfmllFindUsagesProvider implements FindUsagesProvider {
  @Override
  public WordsScanner getWordsScanner() {
    return null;
  }

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
           ? "reference"
           : (element instanceof CfmlTagFunctionImpl) || element instanceof CfmlFunctionImpl ? "Function Name" : "Argument Name";
  }

  @Override
  @NotNull
  public String getDescriptiveName(@NotNull final PsiElement element) {
    return element instanceof CfmlReferenceExpression
           ? "reference"
           : (element instanceof CfmlTagFunctionImpl) || element instanceof CfmlFunctionImpl ? "function" : "argument";
  }

  @Override
  @NotNull
  public String getNodeText(@NotNull final PsiElement element, final boolean useFullName) {
    return element.getText();
  }
}

