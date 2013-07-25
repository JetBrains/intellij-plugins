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
