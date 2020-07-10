package org.angular2.entities;

import com.intellij.icons.AllIcons;
import com.intellij.lang.javascript.psi.ecma6.TypeScriptClass;
import com.intellij.navigation.NavigationItem;
import com.intellij.openapi.util.TextRange;
import com.intellij.pom.PsiDeclaredTarget;
import com.intellij.psi.PsiElement;
import com.intellij.psi.impl.FakePsiElement;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.search.SearchScope;
import com.intellij.util.IncorrectOperationException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

import static org.angular2.Angular2DecoratorUtil.getClassForDecoratorElement;

public class Angular2DirectiveSelectorPsiElement extends FakePsiElement implements PsiDeclaredTarget, NavigationItem {

  private final Angular2DirectiveSelectorImpl myParent;
  private final TextRange myRange;
  private final String myName;
  private final boolean myIsElement;

  public Angular2DirectiveSelectorPsiElement(@NotNull Angular2DirectiveSelectorImpl parent,
                                             @NotNull TextRange range,
                                             @NotNull String name,
                                             boolean isElement) {
    myParent = parent;
    myRange = range;
    myName = name;
    myIsElement = isElement;
  }

  @Override
  public @NotNull String getName() {
    return myName;
  }

  @Override
  public PsiElement setName(@NotNull String name) throws IncorrectOperationException {
    if (myRange.getLength() > 0) {
      myParent.replaceText(myRange, name);
    }
    return this;
  }

  @Override
  public PsiElement getParent() {
    return myParent.getPsiParent();
  }

  @Override
  public int getTextOffset() {
    return myParent.getPsiParent().getTextOffset() + myRange.getStartOffset();
  }

  @Override
  public @Nullable TextRange getTextRange() {
    int startOffset = myParent.getPsiParent().getTextOffset() + myRange.getStartOffset();
    return new TextRange(startOffset, startOffset + myName.length());
  }

  @Override
  public int getTextLength() {
    return myName.length();
  }

  @Override
  public @Nullable String getText() {
    return myName;
  }

  @Override
  public @NotNull TextRange getTextRangeInParent() {
    return myRange;
  }

  @Override
  public @Nullable TextRange getNameIdentifierRange() {
    return new TextRange(0, myRange.getLength());
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
    return getName();
  }

  @Override
  public @Nullable String getLocationString() {
    PsiElement parent = myParent.getPsiParent();
    TypeScriptClass clazz = getClassForDecoratorElement(parent);
    return clazz != null ? "(" + clazz.getName() + ", " + parent.getContainingFile().getName() + ")"
                         : parent.getContainingFile().getName() + ":" + getTextOffset();
  }

  @Override
  public @NotNull SearchScope getUseScope() {
    return GlobalSearchScope.projectScope(getProject());
  }

  @Override
  public @Nullable Icon getIcon(boolean open) {
    return isElementSelector() ? AllIcons.Nodes.Tag : AllIcons.Nodes.ObjectTypeAttribute;
  }

  public boolean isElementSelector() {
    return myIsElement;
  }

  public boolean isAttributeSelector() {
    return !myIsElement;
  }

  @Override
  public String toString() {
    return (myIsElement ? "ElementDirectiveSelector" : "AttributeDirectiveSelector") + "<" + myName + ">";
  }
}
