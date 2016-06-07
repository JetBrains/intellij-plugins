package com.jetbrains.lang.dart.psi.impl;

import com.intellij.icons.AllIcons;
import com.intellij.lang.ASTNode;
import com.intellij.navigation.ItemPresentation;
import com.intellij.navigation.NavigationItem;
import com.intellij.psi.PsiElement;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.ui.RowIcon;
import com.intellij.util.PlatformIcons;
import com.jetbrains.lang.dart.psi.DartFunctionDeclarationWithBodyOrNative;
import com.jetbrains.lang.dart.psi.DartMethodDeclaration;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

abstract public class DartFunctionExpressionBase extends DartExpressionImpl {

  public DartFunctionExpressionBase(ASTNode node) {
    super(node);
  }

  @Override
  public Icon getIcon(int flags) {
    Icon icon = AllIcons.Nodes.Function;
    RowIcon baseIcon = new RowIcon(2);
    baseIcon.setIcon(icon, 0);
    Icon visibility = PlatformIcons.PRIVATE_ICON;
    baseIcon.setIcon(visibility, 1);
    return baseIcon;
  }

  @Override
  public ItemPresentation getPresentation() {
    final PsiElement parent = PsiTreeUtil.getParentOfType(this, DartFunctionExpressionBase.class, DartMethodDeclaration.class,
                                                          DartFunctionDeclarationWithBodyOrNative.class);
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
