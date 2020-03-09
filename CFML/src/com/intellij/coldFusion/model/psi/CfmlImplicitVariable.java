// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
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
import com.intellij.util.IncorrectOperationException;
import com.intellij.util.PlatformIcons;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

/**
 * @author vnikolaenko
 */
public class CfmlImplicitVariable extends RenameableFakePsiElement implements CfmlVariable {
  private final PsiComment myComment;
  private final String myName;
  private String myType;
  private final String myText;

  public CfmlImplicitVariable(@NotNull final PsiFile containingFile,
                              final PsiComment comment,
                              @NotNull final String name) {
    super(containingFile);
    myComment = comment;
    myText = name;
    myName = cutScope(myText);
  }

  private String cutScope(String name) {
    final int i = name.indexOf(".");
    if (i != -1 && CfmlScopesInfo.getScopeByString(name.substring(0, i)) != CfmlScopesInfo.DEFAULT_SCOPE) {
      return name.substring(i + 1);
    }
    return name;
  }

  @Override
  @NotNull
  public TextRange getTextRange() {
    return myComment.getTextRange();
  }

  @Override
  @NotNull
  public String getName() {
    return myName;
  }

  @Override
  @NotNull
  public PsiElement getNavigationElement() {
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
  @Nullable
  public PsiType getPsiType() {
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
    return PlatformIcons.VARIABLE_ICON;
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
  @NotNull
  public String getlookUpString() {
    return myText;
  }
}
