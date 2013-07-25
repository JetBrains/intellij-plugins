/*
 * Copyright 2000-2013 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.intellij.coldFusion.model.psi;

import com.intellij.coldFusion.model.CfmlScopesInfo;
import com.intellij.openapi.util.TextRange;
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
  private String myText;

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
      return name.substring(i + 1, name.length());
    }
    return name;
  }

  @Override
  public TextRange getTextRange() {
    return myComment.getTextRange();
  }

  @NotNull
  public String getName() {
    return myName;
  }

  public PsiElement getNavigationElement() {
    return myComment;
  }

  public PsiElement getParent() {
    return myComment;
  }

  public String getTypeName() {
    return "Type name variable";
  }

  public String toString() {
    return "ImplicitVariable " + myName;
  }

  public void setType(final String type) {
    myType = type;
  }

  @Nullable
  public PsiType getPsiType() {
    if (myType == null) {
      return null;
    }
    try {
      if (myType.toLowerCase().equals("javaloader")) {
        return new CfmlJavaLoaderClassType(myComment, getProject());
      }
      return CfmlPsiUtil.getTypeByName(myType, getProject());
    }
    catch (IncorrectOperationException e) {
      return null;
    }
  }

  public Icon getIcon() {
    return PlatformIcons.VARIABLE_ICON;
  }

  public boolean isTrulyDeclaration() {
    return true;
  }

  public PsiElement getNameIdentifier() {
    return getNavigationElement();
  }

  @Override
  public String getText() {
    return myText;
  }

  @NotNull
  public String getlookUpString() {
    return myText;
  }
}
