package org.intellij.plugins.postcss.references;

import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementResolveResult;
import com.intellij.psi.PsiPolyVariantReferenceBase;
import com.intellij.psi.ResolveResult;
import com.intellij.psi.css.impl.util.CssUtil;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.stubs.StubIndex;
import com.intellij.util.ArrayUtil;
import com.intellij.util.IncorrectOperationException;
import org.intellij.plugins.postcss.psi.PostCssCustomMedia;
import org.intellij.plugins.postcss.psi.stubs.PostCssCustomMediaIndex;
import org.jetbrains.annotations.NotNull;

public class PostCssCustomMediaReference extends PsiPolyVariantReferenceBase<PsiElement> {
  public PostCssCustomMediaReference(PsiElement psiElement) {
    super(psiElement);
  }

  @NotNull
  @Override
  public ResolveResult[] multiResolve(boolean incompleteCode) {
    GlobalSearchScope scope = CssUtil.getCompletionAndResolvingScopeForElement(myElement);
    String key = StringUtil.trimStart(myElement.getText(), "--");
    return PsiElementResolveResult.createResults(
      StubIndex.getElements(PostCssCustomMediaIndex.KEY, key, myElement.getProject(), scope, PostCssCustomMedia.class));
  }

  @Override
  public PsiElement handleElementRename(String newElementName) throws IncorrectOperationException {
    return super.handleElementRename(StringUtil.startsWith(newElementName, "--") ? newElementName : "--" + newElementName);
  }

  @Override
  public boolean isReferenceTo(PsiElement element) {
    return element instanceof PostCssCustomMedia &&
           StringUtil.trimStart(getCanonicalText(), "--").equals(((PostCssCustomMedia)element).getName());
  }

  @NotNull
  @Override
  public Object[] getVariants() {
    return ArrayUtil.EMPTY_OBJECT_ARRAY;
  }
}