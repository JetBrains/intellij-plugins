package org.intellij.plugins.postcss.references;

import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementResolveResult;
import com.intellij.psi.PsiPolyVariantReferenceBase;
import com.intellij.psi.ResolveResult;
import com.intellij.util.IncorrectOperationException;
import org.intellij.plugins.postcss.psi.PostCssCustomMedia;
import org.intellij.plugins.postcss.psi.stubs.PostCssCustomMediaIndex;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class PostCssCustomMediaReference extends PsiPolyVariantReferenceBase<PsiElement> {
  public PostCssCustomMediaReference(PsiElement psiElement) {
    super(psiElement);
  }

  @Override
  public ResolveResult @NotNull [] multiResolve(boolean incompleteCode) {
    String name = getCustomMediaName();
    return name != null ? PsiElementResolveResult.createResults(PostCssCustomMediaIndex.getCustomMediaFeatures(name, myElement))
                        : ResolveResult.EMPTY_ARRAY;
  }

  @Override
  public PsiElement handleElementRename(@NotNull String newElementName) throws IncorrectOperationException {
    return super.handleElementRename(StringUtil.startsWith(newElementName, "--") ? newElementName : "--" + newElementName);
  }

  @Override
  public boolean isReferenceTo(@NotNull PsiElement element) {
    String name = getCustomMediaName();
    return name != null && element instanceof PostCssCustomMedia && name.equals(((PostCssCustomMedia)element).getName());
  }

  private @Nullable String getCustomMediaName() {
    return StringUtil.startsWith(getCanonicalText(), "--") ? getCanonicalText().substring(2) : null;
  }
}