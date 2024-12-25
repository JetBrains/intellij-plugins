// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.intellij.plugins.drools.lang.psi.impl;

import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.lang.ASTNode;
import com.intellij.openapi.util.TextRange;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.plugins.drools.lang.psi.*;
import com.intellij.plugins.drools.lang.psi.util.DroolsResolveUtil;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementResolveResult;
import com.intellij.psi.PsiReference;
import com.intellij.psi.ResolveResult;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.ui.IconManager;
import com.intellij.ui.PlatformIcons;
import com.intellij.util.IncorrectOperationException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashSet;
import java.util.Set;

public abstract class DroolsWindowReferenceImpl extends DroolsPsiCompositeElementImpl implements DroolsWindowId, DroolsWindowReference {
  public DroolsWindowReferenceImpl(@NotNull ASTNode node) {
    super(node);
  }

  @Override
  public @NotNull PsiElement getElement() {
    return this;
  }

  @Override
  public PsiReference getReference() {
    return this;
  }

  @Override
  public @NotNull TextRange getRangeInElement() {
    final TextRange textRange = getTextRange();
    return new TextRange(0, textRange.getEndOffset() - textRange.getStartOffset());
  }

  @Override
  public @NotNull String getCanonicalText() {
    return getText();
  }

  @Override
  public PsiElement handleElementRename(@NotNull String newElementName) throws IncorrectOperationException {
    return this;
  }

  @Override
  public PsiElement bindToElement(@NotNull PsiElement element) throws IncorrectOperationException {
    return element;
  }

  @Override
  public boolean isReferenceTo(@NotNull PsiElement element) {
    return element.equals(resolve());
  }

  @Override
  public Object @NotNull [] getVariants() {
    Set<LookupElementBuilder> items = new HashSet<>();
    DroolsFile droolsFile = PsiTreeUtil.getContextOfType(getElement(), DroolsFile.class);
    if (droolsFile != null) {
      DroolsDeclareStatement[] declarations = droolsFile.getDeclarations();
      for (DroolsDeclareStatement declaration : declarations) {
        @Nullable DroolsWindowDeclaration windowDeclaration = declaration.getWindowDeclaration();
        if (windowDeclaration != null) {
          final String windowDeclarationName = windowDeclaration.getSimpleName().getText();
          if (StringUtil.isNotEmpty(windowDeclarationName)) {
           items.add(LookupElementBuilder.create(windowDeclarationName).withIcon(
                IconManager.getInstance().getPlatformIcon(PlatformIcons.Variable)));
          }
        }
      }
    }

    return items.toArray();
  }

  @Override
  public boolean isSoft() {
    return false;
  }

  @Override
  public PsiElement resolve() {
    final ResolveResult[] resolveResults = multiResolve(true);

    return resolveResults.length == 0  ? null : DroolsResolveUtil.chooseDroolsTypeResult(resolveResults);
  }

  @Override
  public ResolveResult @NotNull [] multiResolve(boolean incompleteCode) {
    final String windowId = getText();
    if (StringUtil.isNotEmpty(windowId)) {
      DroolsFile droolsFile = PsiTreeUtil.getContextOfType(getElement(), DroolsFile.class);
      if (droolsFile != null) {
        DroolsDeclareStatement[] declarations = droolsFile.getDeclarations();
        for (DroolsDeclareStatement declaration : declarations) {
          @Nullable DroolsWindowDeclaration windowDeclaration = declaration.getWindowDeclaration();
          if (windowDeclaration != null  && windowId.equals(windowDeclaration.getSimpleName().getText())) {
            return new ResolveResult[] {new PsiElementResolveResult(windowDeclaration)};
          }
        }
      }
    }
    return ResolveResult.EMPTY_ARRAY;
  }

}
