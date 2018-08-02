package com.intellij.javascript.flex.css;

import com.intellij.openapi.util.TextRange;
import com.intellij.psi.*;
import com.intellij.psi.css.CssBundle;
import com.intellij.psi.css.CssElementDescriptorProvider;
import com.intellij.psi.css.CssPropertyDescriptor;
import com.intellij.psi.css.reference.CssReference;
import com.intellij.psi.impl.source.resolve.reference.impl.PsiPolyVariantCachingReference;
import com.intellij.util.IncorrectOperationException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static com.intellij.psi.css.impl.util.table.CssDescriptorsUtil.extractDescriptorsIdsAsArray;

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

  @NotNull
  @Override
  public String getUnresolvedMessagePattern() {
    return CssBundle.message("invalid.css.property.name.message");
  }

  @NotNull
  @Override
  protected ResolveResult[] resolveInner(boolean incompleteCode, @NotNull PsiFile containingFile) {
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

  @NotNull
  @Override
  public PsiElement getElement() {
    return myElement;
  }

  @NotNull
  @Override
  public TextRange getRangeInElement() {
    return new TextRange(myStart, myEnd);
  }

  @Override
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

  @Override
  public PsiElement handleElementRename(String newElementName) throws IncorrectOperationException {
    final ElementManipulator<PsiElement> manipulator = ElementManipulators.getManipulator(myElement);
    assert manipulator != null;
    return manipulator.handleContentChange(myElement, getRangeInElement(), newElementName);
  }

  @Override
  @Nullable
  public PsiElement bindToElement(@NotNull PsiElement element) throws IncorrectOperationException {
    return null;
  }

  @Override
  @NotNull
  public Object[] getVariants() {
    FlexCssElementDescriptorProvider flexDescriptorProvider = CssElementDescriptorProvider.EP_NAME.findExtension(FlexCssElementDescriptorProvider.class);
    return extractDescriptorsIdsAsArray(flexDescriptorProvider.getAllPropertyDescriptors(myElement));
  }

  @Override
  public boolean isReferenceTo(@NotNull PsiElement element) {
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
