package com.jetbrains.lang.dart.psi.impl;

import com.intellij.icons.AllIcons;
import com.intellij.lang.ASTNode;
import com.intellij.navigation.ItemPresentation;
import com.intellij.navigation.NavigationItem;
import com.intellij.psi.PsiElement;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.ui.RowIcon;
import com.intellij.util.PlatformIcons;
import com.jetbrains.lang.dart.psi.*;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

abstract public class DartFunctionExpressionBase extends DartExpressionImpl {

  private static final RowIcon ICON = new RowIcon(AllIcons.Nodes.Function, PlatformIcons.PRIVATE_ICON);

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
        @Nullable
        @Override
        public String getPresentableText() {
          ItemPresentation container = ((NavigationItem)parent).getPresentation();
          return container == null ? null : "() in " + container.getPresentableText();
        }

        @Nullable
        @Override
        public String getLocationString() {
          ItemPresentation container = ((NavigationItem)parent).getPresentation();
          return container == null ? null : container.getLocationString();
        }

        @Nullable
        @Override
        public Icon getIcon(boolean unused) {
          return DartFunctionExpressionBase.this.getIcon(0);
        }
      };
    }
    return null;
  }
}
