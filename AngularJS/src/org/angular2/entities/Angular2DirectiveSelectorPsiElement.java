package org.angular2.entities;

import com.intellij.navigation.NavigationItem;
import com.intellij.openapi.util.TextRange;
import com.intellij.pom.PomTarget;
import com.intellij.psi.PsiElement;
import com.intellij.psi.impl.FakePsiElement;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class Angular2DirectiveSelectorPsiElement extends FakePsiElement implements PomTarget, NavigationItem {

  private final PsiElement myParent;
  private final TextRange myRange;
  private final String myName;
  private final boolean myIsElement;

  public Angular2DirectiveSelectorPsiElement(@NotNull PsiElement parent,
                                             @NotNull TextRange range,
                                             @NotNull String name,
                                             boolean isElement) {
    myParent = parent;
    myRange = range;
    myName = name;
    myIsElement = isElement;
  }

  @Override
  @NotNull
  public String getName() {
    return myName;
  }

  @Override
  public PsiElement getParent() {
    return myParent;
  }

  @Override
  public int getTextOffset() {
    return myParent.getTextOffset() + myRange.getStartOffset();
  }

  @Nullable
  @Override
  public TextRange getTextRange() {
    int startOffset = myParent.getTextOffset() + myRange.getStartOffset();
    return new TextRange(startOffset, startOffset + myName.length());
  }

  @Override
  public int getTextLength() {
    return myName.length();
  }

  @Nullable
  @Override
  public String getText() {
    return myName;
  }

  @NotNull
  @Override
  public TextRange getTextRangeInParent() {
    return myRange;
  }

  @Override
  public boolean isEquivalentTo(PsiElement another) {
    if (another instanceof Angular2DirectiveSelectorPsiElement) {
      Angular2DirectiveSelectorPsiElement anotherSelector = (Angular2DirectiveSelectorPsiElement)another;
      return anotherSelector.myIsElement == myIsElement
             && anotherSelector.myName.equals(myName);
    }
    return false;
  }

  @Override
  public String getPresentableText() {
    StringBuilder builder = new StringBuilder();
    if (myIsElement) {
      builder.append("Element");
    }
    else {
      builder.append("Attribute");
    }
    builder.append(" selector: ");
    builder.append(getName());
    return builder.toString();
  }

  @Nullable
  @Override
  public String getLocationString() {
    return myParent.getContainingFile().getName() + ":" + getTextOffset();
  }

  public boolean isElementSelector() {
    return myIsElement;
  }

  public boolean isAttributeSelector() {
    return !myIsElement;
  }
}
