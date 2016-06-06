package com.jetbrains.lang.dart.psi.impl;

import com.intellij.lang.ASTNode;
import com.intellij.navigation.ItemPresentation;
import com.intellij.navigation.NavigationItem;
import com.intellij.psi.PsiElement;
import com.intellij.psi.util.PsiTreeUtil;
import com.jetbrains.lang.dart.DartComponentType;
import com.jetbrains.lang.dart.psi.DartFunctionDeclarationWithBodyOrNative;
import com.jetbrains.lang.dart.psi.DartMethodDeclaration;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

public class DartFunctionExpressionBase extends DartExpressionImpl {

  public DartFunctionExpressionBase(ASTNode node) {
    super(node);
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
          DartComponentType type = DartComponentType.typeOf(DartFunctionExpressionBase.this);
          assert type != null;
          return type.getIcon();
        }
      };
    }
    return null;
  }
}
