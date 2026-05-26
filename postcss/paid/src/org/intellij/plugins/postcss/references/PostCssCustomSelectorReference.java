package org.intellij.plugins.postcss.references;

import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementResolveResult;
import com.intellij.psi.PsiPolyVariantReferenceBase;
import com.intellij.psi.ResolveResult;
import com.intellij.util.IncorrectOperationException;
import org.intellij.plugins.postcss.psi.PostCssCustomSelector;
import org.intellij.plugins.postcss.psi.stubs.PostCssCustomSelectorIndex;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class PostCssCustomSelectorReference extends PsiPolyVariantReferenceBase<PsiElement> {
  public PostCssCustomSelectorReference(PsiElement psiElement) {
    super(psiElement);
  }

  @Override
  public ResolveResult @NotNull [] multiResolve(boolean incompleteCode) {
    String name = getCustomSelectorName();
    return name != null ? PsiElementResolveResult.createResults(PostCssCustomSelectorIndex.getCustomSelectors(name, myElement))
                        : ResolveResult.EMPTY_ARRAY;
  }

  @Override
  public PsiElement handleElementRename(@NotNull String newElementName) throws IncorrectOperationException {
    return super.handleElementRename(StringUtil.startsWith(newElementName, "--") ? newElementName : "--" + newElementName);
  }

  @Override
  public boolean isReferenceTo(@NotNull PsiElement element) {
    String name = getCustomSelectorName();
    return name != null && element instanceof PostCssCustomSelector && name.equals(((PostCssCustomSelector)element).getName());
  }

  private @Nullable String getCustomSelectorName() {
    return StringUtil.startsWith(getCanonicalText(), "--") ? getCanonicalText().substring(2) : null;
  }
}