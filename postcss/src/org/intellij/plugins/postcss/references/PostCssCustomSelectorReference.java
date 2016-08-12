package org.intellij.plugins.postcss.references;

import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementResolveResult;
import com.intellij.psi.PsiPolyVariantReferenceBase;
import com.intellij.psi.ResolveResult;
import com.intellij.util.ArrayUtil;
import com.intellij.util.IncorrectOperationException;
import org.intellij.plugins.postcss.psi.PostCssCustomSelector;
import org.intellij.plugins.postcss.psi.stubs.PostCssCustomSelectorIndex;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class PostCssCustomSelectorReference extends PsiPolyVariantReferenceBase<PsiElement> {
  public PostCssCustomSelectorReference(PsiElement psiElement) {
    super(psiElement);
  }

  @NotNull
  @Override
  public ResolveResult[] multiResolve(boolean incompleteCode) {
    String key = getElementName();
    if (key == null) return ResolveResult.EMPTY_ARRAY;
    return PsiElementResolveResult.createResults(PostCssCustomSelectorIndex.getCustomSelectors(myElement, key));
  }

  @Override
  public PsiElement handleElementRename(String newElementName) throws IncorrectOperationException {
    return super.handleElementRename(StringUtil.startsWith(newElementName, "--") ? newElementName : "--" + newElementName);
  }

  @Override
  public boolean isReferenceTo(PsiElement element) {
    String name = getElementName();
    return name != null && element instanceof PostCssCustomSelector && name.equals(((PostCssCustomSelector)element).getName());
  }

  @Nullable
  private String getElementName() {
    return StringUtil.startsWith(getCanonicalText(), "--") ? getCanonicalText().substring(2) : null;
  }

  @NotNull
  @Override
  public Object[] getVariants() {
    return ArrayUtil.EMPTY_OBJECT_ARRAY;
  }
}