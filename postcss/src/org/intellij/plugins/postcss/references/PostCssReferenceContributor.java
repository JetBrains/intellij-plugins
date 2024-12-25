package org.intellij.plugins.postcss.references;

import com.intellij.patterns.PatternCondition;
import com.intellij.psi.*;
import com.intellij.psi.css.CssMediaFeature;
import com.intellij.psi.css.CssPseudoClass;
import com.intellij.psi.css.impl.CssElementTypes;
import com.intellij.psi.css.impl.CssTokenImpl;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.ProcessingContext;
import org.intellij.plugins.postcss.psi.PostCssPsiUtil;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

import static com.intellij.patterns.PlatformPatterns.psiElement;

public class PostCssReferenceContributor extends PsiReferenceContributor {

  @Override
  public void registerReferenceProviders(final @NotNull PsiReferenceRegistrar registrar) {
    registrar.registerReferenceProvider(psiElement().with(new PostCssReferencePattern()), new PostCssReferenceProvider());
  }

  private static class PostCssReferenceProvider extends PsiReferenceProvider {
    @Override
    public PsiReference @NotNull [] getReferencesByElement(final @NotNull PsiElement element, final @NotNull ProcessingContext context) {
      PsiElement parent = element.getParent();
      if (parent instanceof CssPseudoClass && parent.getText().startsWith(":--")) {
        return new PsiReference[]{new PostCssCustomSelectorReference(element)};
      }
      if (parent instanceof CssMediaFeature
          && element.getText().startsWith("--")
          && Objects.requireNonNull(PsiTreeUtil.getChildrenOfType(parent, CssTokenImpl.class)).length == 1) {
        return new PsiReference[]{new PostCssCustomMediaReference(element)};
      }
      return PsiReference.EMPTY_ARRAY;
    }
  }

  private static class PostCssReferencePattern extends PatternCondition<PsiElement> {
    PostCssReferencePattern() {
      super("postcss reference");
    }

    @Override
    public boolean accepts(@NotNull PsiElement element, ProcessingContext context) {
      return element.getNode().getElementType() == CssElementTypes.CSS_IDENT && PostCssPsiUtil.isInsidePostCss(element);
    }
  }
}