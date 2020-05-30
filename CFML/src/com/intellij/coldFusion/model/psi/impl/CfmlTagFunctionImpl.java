// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
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
      return METHOD_ICON;
    }
    access = StringUtil.toLowerCase(access);

    RowIcon baseIcon = IconManager.getInstance().createRowIcon(2);
    baseIcon.setIcon(METHOD_ICON, 0);
    if ("private".equals(access)) {
      baseIcon.setIcon(PRIVATE_ICON, 1);
    }
    else if ("package".equals(access)) {
      baseIcon.setIcon(PACKAGE_LOCAL_ICON, 1);
    }
    else if ("public".equals(access)) {
      baseIcon.setIcon(PUBLIC_ICON, 1);
    }
    else if ("remote".equals(access)) {
      baseIcon.setIcon(CFMLIcons.Remote_access, 1);
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
