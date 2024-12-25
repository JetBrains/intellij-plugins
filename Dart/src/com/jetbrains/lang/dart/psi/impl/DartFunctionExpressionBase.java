// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.jetbrains.lang.dart.psi.impl;

import com.intellij.icons.AllIcons;
import com.intellij.lang.ASTNode;
import com.intellij.navigation.ItemPresentation;
import com.intellij.navigation.NavigationItem;
import com.intellij.psi.PsiElement;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.ui.IconManager;
import com.intellij.ui.icons.RowIcon;
import com.jetbrains.lang.dart.psi.*;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

public abstract class DartFunctionExpressionBase extends DartExpressionImpl {
  private static final RowIcon ICON = IconManager.getInstance().createRowIcon(AllIcons.Nodes.Lambda,
                                                                              IconManager.getInstance().getPlatformIcon(com.intellij.ui.PlatformIcons.Private));

  public DartFunctionExpressionBase(ASTNode node) {
    super(node);
  }

  @Override
  public Icon getIcon(int flags) {
    return ICON;
  }

  @Override
  public ItemPresentation getPresentation() {
    // Hard to believe there is no marker interface to identify a declaration that contains executable code...
    // TODO: Create one! Executable code marker, that is.
    final PsiElement parent = PsiTreeUtil.getParentOfType(this, DartFunctionExpressionBase.class, DartMethodDeclaration.class,
                                                          DartFunctionDeclarationWithBodyOrNative.class,
                                                          DartFactoryConstructorDeclaration.class,
                                                          DartNamedConstructorDeclaration.class, DartFunctionDeclarationWithBody.class,
                                                          DartGetterDeclaration.class, DartSetterDeclaration.class);
    if (parent != null) {
      return new ItemPresentation() {
        @Override
        public @Nullable String getPresentableText() {
          ItemPresentation container = ((NavigationItem)parent).getPresentation();
          return container == null ? null : "() in " + container.getPresentableText();
        }

        @Override
        public @Nullable String getLocationString() {
          ItemPresentation container = ((NavigationItem)parent).getPresentation();
          return container == null ? null : container.getLocationString();
        }

        @Override
        public @Nullable Icon getIcon(boolean unused) {
          return DartFunctionExpressionBase.this.getIcon(0);
        }
      };
    }
    return null;
  }
}
