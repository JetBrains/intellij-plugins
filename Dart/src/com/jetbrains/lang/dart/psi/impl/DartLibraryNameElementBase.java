// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.jetbrains.lang.dart.psi.impl;

import com.intellij.lang.ASTNode;
import com.intellij.navigation.ItemPresentation;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiNameIdentifierOwner;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.IncorrectOperationException;
import com.jetbrains.lang.dart.psi.DartId;
import com.jetbrains.lang.dart.psi.DartLibraryNameElement;
import com.jetbrains.lang.dart.util.DartElementGenerator;
import icons.DartIcons;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

public abstract class DartLibraryNameElementBase extends DartPsiCompositeElementImpl implements PsiNameIdentifierOwner {
  public DartLibraryNameElementBase(@NotNull ASTNode node) {
    super(node);
  }

  @Override
  public PsiElement setName(@NonNls @NotNull String newLibraryName) throws IncorrectOperationException {
    final DartLibraryNameElement libraryNameElementNew =
      DartElementGenerator.createLibraryNameElementFromText(getProject(), newLibraryName);

    if (libraryNameElementNew != null) {
      getNode().replaceAllChildrenToChildrenOf(libraryNameElementNew.getNode());
    }

    return this;
  }

  @Override
  public @NotNull String getName() {
    StringBuilder name = new StringBuilder();
    for (DartId id : PsiTreeUtil.getChildrenOfTypeAsList(this, DartId.class)) {
      if (!name.isEmpty()) {
        name.append('.');
      }
      name.append(id.getText());
    }
    return name.toString();
  }

  @Override
  public @Nullable ItemPresentation getPresentation() {
    return new ItemPresentation() {
      @Override
      public String getPresentableText() {
        return getName();
      }

      @Override
      public Icon getIcon(boolean open) {
        return DartLibraryNameElementBase.this.getIcon(0);
      }
    };
  }

  @Override
  public Icon getIcon(int flags) {
    return DartIcons.Dart_file; // todo better icon?
  }

  @Override
  public PsiElement getNameIdentifier() {
    return this;
  }
}
