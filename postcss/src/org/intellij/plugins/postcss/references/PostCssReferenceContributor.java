package org.intellij.plugins.postcss.references;

import com.intellij.openapi.util.text.StringUtil;
import com.intellij.patterns.PatternCondition;
import com.intellij.patterns.PsiElementPattern;
import com.intellij.psi.*;
import com.intellij.psi.css.CssPseudoClass;
import com.intellij.psi.css.impl.CssElementTypes;
import com.intellij.psi.util.CachedValueProvider;
import com.intellij.psi.util.CachedValuesManager;
import com.intellij.util.ProcessingContext;
import org.intellij.plugins.postcss.psi.PostCssCustomSelector;
import org.intellij.plugins.postcss.psi.PostCssPsiUtil;
import org.jetbrains.annotations.NotNull;

import static com.intellij.patterns.PlatformPatterns.psiElement;

public class PostCssReferenceContributor extends PsiReferenceContributor {
  public static final PsiElementPattern.Capture<PsiElement> PATTERN = psiElement().with(new PostCssReferencePattern());

  @Override
  public void registerReferenceProviders(@NotNull final PsiReferenceRegistrar registrar) {
    registrar.registerReferenceProvider(PATTERN, new SassScssReferenceProvider(), PsiReferenceRegistrar.HIGHER_PRIORITY);
  }

  private static class SassScssReferenceProvider extends PsiReferenceProvider {
    @NotNull
    public PsiReference[] getReferencesByElement(@NotNull final PsiElement element, @NotNull final ProcessingContext context) {
      return CachedValuesManager.getCachedValue(element, () -> CachedValueProvider.Result.create(getReferences(element), element));
    }

    @NotNull
    private static PsiReference[] getReferences(@NotNull PsiElement element) {
      if (element.getNode().getElementType() == CssElementTypes.CSS_IDENT) {
        PsiElement parent = element.getParent();
        if (parent instanceof CssPseudoClass && StringUtil.startsWith(parent.getText(), ":--")) {
          return new PsiReference[]{new PostCssCustomSelectorReference(element)};
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