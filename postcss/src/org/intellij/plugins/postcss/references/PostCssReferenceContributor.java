package org.intellij.plugins.postcss.references;

import com.intellij.patterns.PatternCondition;
import com.intellij.psi.*;
import com.intellij.psi.css.CssMediaFeature;
import com.intellij.psi.css.CssPseudoClass;
import com.intellij.psi.css.impl.CssElementTypes;
import com.intellij.psi.css.impl.CssTokenImpl;
import com.intellij.psi.css.resolve.CssCustomMixinReference;
import com.intellij.psi.util.CachedValueProvider;
import com.intellij.psi.util.CachedValuesManager;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.ObjectUtils;
import com.intellij.util.ProcessingContext;
import org.intellij.plugins.postcss.psi.PostCssApplyAtRule;
import org.intellij.plugins.postcss.psi.PostCssPsiUtil;
import org.jetbrains.annotations.NotNull;

import static com.intellij.patterns.PlatformPatterns.psiElement;

public class PostCssReferenceContributor extends PsiReferenceContributor {

  @Override
  public void registerReferenceProviders(@NotNull final PsiReferenceRegistrar registrar) {
    registrar.registerReferenceProvider(psiElement().with(new PostCssReferencePattern()), new PostCssReferenceProvider());
  }

  private static class PostCssReferenceProvider extends PsiReferenceProvider {
    @NotNull
    public PsiReference[] getReferencesByElement(@NotNull final PsiElement element, @NotNull final ProcessingContext context) {
      return CachedValuesManager.getCachedValue(element, () -> CachedValueProvider.Result.create(getReferences(element), element));
    }

    @NotNull
    private static PsiReference[] getReferences(@NotNull PsiElement element) {
      if (element.getNode().getElementType() == CssElementTypes.CSS_IDENT) {
        PsiElement parent = element.getParent();
        if (parent instanceof CssPseudoClass && parent.getText().startsWith(":--")) {
          return new PsiReference[]{new PostCssCustomSelectorReference(element)};
        }
        if (parent instanceof CssMediaFeature
            && element.getText().startsWith("--")
            && ObjectUtils.notNull(PsiTreeUtil.getChildrenOfType(parent, CssTokenImpl.class)).length == 1) {
          return new PsiReference[]{new PostCssCustomMediaReference(element)};
        }
        if (parent instanceof PostCssApplyAtRule && element.getText().startsWith("--")) {
          return new PsiReference[]{new CssCustomMixinReference(element)};
        }
      }
      return PsiReference.EMPTY_ARRAY;
    }
  }

  private static class PostCssReferencePattern extends PatternCondition<PsiElement> {
    public PostCssReferencePattern() {
      super("postcss reference");
    }

    @Override
    public boolean accepts(@NotNull PsiElement element, ProcessingContext context) {
      if (!PostCssPsiUtil.isInsidePostCss(element)) return false;
      return element.getNode().getElementType() == CssElementTypes.CSS_IDENT;
    }
  }
}