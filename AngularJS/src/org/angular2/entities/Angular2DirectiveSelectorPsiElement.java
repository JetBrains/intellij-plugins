package org.angular2.entities;

import com.intellij.icons.AllIcons;
import com.intellij.lang.javascript.psi.ecma6.TypeScriptClass;
import com.intellij.navigation.NavigationItem;
import com.intellij.openapi.util.AtomicNotNullLazyValue;
import com.intellij.openapi.util.NotNullFactory;
import com.intellij.openapi.util.TextRange;
import com.intellij.pom.PomTarget;
import com.intellij.psi.PsiElement;
import com.intellij.psi.impl.FakePsiElement;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.search.SearchScope;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

import static org.angular2.Angular2DecoratorUtil.getClassForDecoratorElement;

public class Angular2DirectiveSelectorPsiElement extends FakePsiElement implements PomTarget, NavigationItem {

  private final AtomicNotNullLazyValue<PsiElement> myParent;
  private final TextRange myRange;
  private final String myName;
  private final boolean myIsElement;

  public Angular2DirectiveSelectorPsiElement(@NotNull NotNullFactory<PsiElement> parent,
                                             @NotNull TextRange range,
                                             @NotNull String name,
                                             boolean isElement) {
    myParent = AtomicNotNullLazyValue.createValue(parent);
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
    return myParent.getValue();
  }

  @Override
  public int getTextOffset() {
    return myParent.getValue().getTextOffset() + myRange.getStartOffset();
  }

  @Nullable
  @Override
  public TextRange getTextRange() {
    int startOffset = myParent.getValue().getTextOffset() + myRange.getStartOffset();
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
    return getName();
  }

  @Nullable
  @Override
  public String getLocationString() {
    PsiElement parent = myParent.getValue();
    TypeScriptClass clazz = getClassForDecoratorElement(parent);
    return clazz != null ? "(" + clazz.getName() + ", " + parent.getContainingFile().getName() + ")"
                         : parent.getContainingFile().getName() + ":" + getTextOffset();
  }

  @NotNull
  @Override
  public SearchScope getUseScope() {
    return GlobalSearchScope.projectScope(getProject());
  }

  @Nullable
  @Override
  public Icon getIcon(boolean open) {
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
