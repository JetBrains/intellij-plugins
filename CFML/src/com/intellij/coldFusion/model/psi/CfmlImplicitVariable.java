// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.intellij.coldFusion.model.psi;

import com.intellij.coldFusion.CfmlBundle;
import com.intellij.coldFusion.model.CfmlScopesInfo;
import com.intellij.openapi.util.TextRange;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiComment;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiType;
import com.intellij.psi.impl.RenameableFakePsiElement;
import com.intellij.ui.IconManager;
import com.intellij.util.IncorrectOperationException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

public class CfmlImplicitVariable extends RenameableFakePsiElement implements CfmlVariable {
  private final PsiComment myComment;
  private final String myName;
  private String myType;
  private final String myText;

  public CfmlImplicitVariable(final @NotNull PsiFile containingFile,
                              final PsiComment comment,
                              final @NotNull String name) {
    super(containingFile);
    myComment = comment;
    myText = name;
    myName = cutScope(myText);
  }

  private static String cutScope(String name) {
    final int i = name.indexOf(".");
    if (i != -1 && CfmlScopesInfo.getScopeByString(name.substring(0, i)) != CfmlScopesInfo.DEFAULT_SCOPE) {
      return name.substring(i + 1);
    }
    return name;
  }

  @Override
  public @NotNull TextRange getTextRange() {
    return myComment.getTextRange();
  }

  @Override
  public @NotNull String getName() {
    return myName;
  }

  @Override
  public @NotNull PsiElement getNavigationElement() {
    return myComment;
  }

  @Override
  public PsiElement getParent() {
    return myComment;
  }

  @Override
  public String getTypeName() {
    return CfmlBundle.message("type.name.variable");
  }

  @Override
  public String toString() {
    return "ImplicitVariable " + myName;
  }

  public void setType(final String type) {
    myType = type;
  }

  @Override
  public @Nullable PsiType getPsiType() {
    if (myType == null) {
      return null;
    }
    try {
      if (StringUtil.toLowerCase(myType).equals("javaloader")) {
        return new CfmlJavaLoaderClassType(myComment, getProject());
      }
      return CfmlPsiUtil.getTypeByName(myType, getProject());
    }
    catch (IncorrectOperationException e) {
      return null;
    }
  }

  @Override
  public Icon getIcon() {
    return IconManager.getInstance().getPlatformIcon(com.intellij.ui.PlatformIcons.Variable);
  }

  @Override
  public boolean isTrulyDeclaration() {
    return true;
  }

  @Override
  public PsiElement getNameIdentifier() {
    return getNavigationElement();
  }

  @Override
  public String getText() {
    return myText;
  }

  @Override
  public @NotNull String getlookUpString() {
    return myText;
  }
}
