package com.intellij.javascript.flex.css;

import com.intellij.openapi.util.TextRange;
import com.intellij.psi.*;
import com.intellij.psi.css.CssBundle;
import com.intellij.psi.css.CssElementDescriptorProvider;
import com.intellij.psi.css.CssPropertyDescriptor;
import com.intellij.psi.css.impl.util.references.CssReference;
import com.intellij.psi.impl.source.resolve.reference.impl.PsiPolyVariantCachingReference;
import com.intellij.util.IncorrectOperationException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Created by IntelliJ IDEA.
 * User: Eugene.Kudelevsky
 * Date: Mar 7, 2010
 * Time: 7:41:54 PM
 * To change this template use File | Settings | File Templates.
 */
public class CssPropertyValueReference extends PsiPolyVariantCachingReference implements CssReference {
  private final PsiElement myElement;
  private final int myStart;
  private final int myEnd;

  public CssPropertyValueReference(@NotNull PsiElement element) {
    myElement = element;
    String text = element.getText();
    if (FlexCssUtil.inQuotes(text)) {
      myStart = 1;
      myEnd = text.length() - 1;
    }
    else {
      myStart = 0;
      myEnd = 0;
    }
  }

  public String getUnresolvedMessage() {
    return CssBundle.message("invalid.css.property.name.message");
  }

  @NotNull
  @Override
  protected ResolveResult[] resolveInner(boolean incompleteCode) {
    String value = myElement.getText();
    if (FlexCssUtil.inQuotes(value)) {
      FlexCssElementDescriptorProvider provider =
        CssElementDescriptorProvider.EP_NAME.findExtension(FlexCssElementDescriptorProvider.class);
      CssPropertyDescriptor descriptor = provider.getPropertyDescriptor(value.substring(1, value.length() - 1), myElement);
      if (descriptor != null) {
        return PsiElementResolveResult.createResults(descriptor.getDeclarations(myElement));
      }
    }
    return ResolveResult.EMPTY_ARRAY;
  }

  public PsiElement getElement() {
    return myElement;
  }

  public TextRange getRangeInElement() {
    return new TextRange(myStart, myEnd);
  }

  @NotNull
  public String getCanonicalText() {
    String text = myElement.getText();
    if (FlexCssUtil.inQuotes(text)) {
      return text.substring(1, text.length() - 1);
    }
    else {
      return "";
    }
  }

  public PsiElement handleElementRename(String newElementName) throws IncorrectOperationException {
    final ElementManipulator<PsiElement> manipulator = ElementManipulators.getManipulator(myElement);
    assert manipulator != null;
    return manipulator.handleContentChange(myElement, getRangeInElement(), newElementName);
  }

  @Nullable
  public PsiElement bindToElement(@NotNull PsiElement element) throws IncorrectOperationException {
    return null;
  }

  @NotNull
  public Object[] getVariants() {
    return CssElementDescriptorProvider.EP_NAME.findExtension(FlexCssElementDescriptorProvider.class).getPropertyNames(myElement);
  }

  @Override
  public boolean isReferenceTo(PsiElement element) {
    ResolveResult[] results = multiResolve(false);
    for (ResolveResult result : results) {
      if (myElement.getManager().areElementsEquivalent(result.getElement(), element)) {
        return true;
      }
    }
    return false;
  }

  @Override
  public boolean isSoft() {
    return true;
  }
}
