// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.intellij.coldFusion.model.psi.impl;

import com.intellij.coldFusion.UI.CfmlLookUpItemUtil;
import com.intellij.coldFusion.model.info.CfmlFunctionDescription;
import com.intellij.coldFusion.model.psi.*;
import com.intellij.lang.ASTNode;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.PsiType;
import com.intellij.ui.IconManager;
import com.intellij.ui.icons.RowIcon;
import com.intellij.util.PlatformIcons;
import icons.CFMLIcons;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

public class CfmlTagFunctionImpl extends CfmlNamedTagImpl implements CfmlFunction, PlatformIcons {
  public static final String TAG_NAME = "cffunction";

  public CfmlTagFunctionImpl(@NotNull ASTNode node) {
    super(node);
  }

  @Override
  @NotNull
  public String getParametersAsString() {
    return getFunctionInfo().getParametersListPresentableText();
  }

  @Override
  public CfmlParameter @NotNull [] getParameters() {
    return findChildrenByClass(CfmlParameter.class);
  }

  @Override
  @Nullable
  public PsiType getReturnType() {
    final String returnTypeString = CfmlPsiUtil.getPureAttributeValue(this, "returntype");
    return returnTypeString != null ?
           new CfmlComponentType(returnTypeString, getContainingFile(), getProject()) : null;
  }

  @Override
  public Icon getIcon(int flags) {
    String access = CfmlPsiUtil.getPureAttributeValue(this, "access");
    if (access == null) {
      return IconManager.getInstance().getPlatformIcon(com.intellij.ui.PlatformIcons.Method);
    }
    access = StringUtil.toLowerCase(access);

    RowIcon baseIcon = IconManager.getInstance().createRowIcon(2);
    baseIcon.setIcon(IconManager.getInstance().getPlatformIcon(com.intellij.ui.PlatformIcons.Method), 0);
    switch (access) {
      case "private" -> baseIcon.setIcon(IconManager.getInstance().getPlatformIcon(com.intellij.ui.PlatformIcons.Private), 1);
      case "package" -> baseIcon.setIcon(IconManager.getInstance().getPlatformIcon(com.intellij.ui.PlatformIcons.Local), 1);
      case "public" -> baseIcon.setIcon(PUBLIC_ICON, 1);
      case "remote" -> baseIcon.setIcon(CFMLIcons.Remote_access, 1);
    }
    return baseIcon;
  }

  @NotNull
  @Override
  public String getTagName() {
    return TAG_NAME;
  }

  public boolean isTrulyDeclaration() {
    return true;
  }

  @Override
  @NotNull
  public CfmlFunctionDescription getFunctionInfo() {
    return CfmlLookUpItemUtil.getFunctionDescription(this);
  }

  @Override
  public void accept(@NotNull PsiElementVisitor visitor) {
    if (visitor instanceof CfmlRecursiveElementVisitor) {
      ((CfmlRecursiveElementVisitor)visitor).visitCfmlFunction(this);
    }
    else {
      super.accept(visitor);
    }
  }
}
