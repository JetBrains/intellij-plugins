// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.

package com.intellij.jhipster.psi;

import com.intellij.extapi.psi.ASTWrapperPsiElement;
import com.intellij.jhipster.JdlIconsMapping;
import com.intellij.lang.ASTNode;
import com.intellij.navigation.ItemPresentation;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.impl.source.tree.LeafElement;
import com.intellij.psi.search.LocalSearchScope;
import com.intellij.psi.search.SearchScope;
import com.intellij.util.IncorrectOperationException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

public abstract class JdlEnumMixin extends ASTWrapperPsiElement implements JdlEnum {
  public JdlEnumMixin(@NotNull ASTNode node) {
    super(node);
  }

  @Override
  public @NotNull String getName() {
    JdlEnumId enumId = this.getEnumId();
    if (enumId == null) return "";

    return enumId.getText();
  }

  @Override
  public PsiElement setName(@NotNull String name) throws IncorrectOperationException {
    if (getEnumId() != null) {
      ASTNode node = getEnumId().getNode();
      ((LeafElement)node.getFirstChildNode()).replaceWithText(name);
    }
    return this;
  }

  @Override
  public @Nullable PsiElement getNameIdentifier() {
    return getEnumId();
  }

  @Override
  public @NotNull SearchScope getUseScope() {
    PsiFile containingFile = getContainingFile();
    if (containingFile != null) {
      return new LocalSearchScope(containingFile);
    }
    return super.getUseScope();
  }

  @Override
  public int getTextOffset() {
    PsiElement name = getNameIdentifier();
    return name != null ? name.getTextOffset() : super.getTextOffset();
  }

  @Override
  protected @Nullable Icon getElementIcon(int flags) {
    return JdlIconsMapping.getEnumIcon();
  }

  @Override
  public @NotNull ItemPresentation getPresentation() {
    return new ItemPresentation() {
      @Override
      public @NotNull String getPresentableText() {
        return getName();
      }

      @Override
      public @NotNull Icon getIcon(boolean unused) {
        return JdlIconsMapping.getEnumIcon();
      }
    };
  }
}
