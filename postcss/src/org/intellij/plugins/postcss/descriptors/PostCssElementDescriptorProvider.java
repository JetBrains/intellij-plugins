package org.intellij.plugins.postcss.descriptors;

import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiPolyVariantReference;
import com.intellij.psi.PsiReference;
import com.intellij.psi.ResolveResult;
import com.intellij.psi.css.CssElementDescriptorProvider;
import com.intellij.psi.css.CssMediaFeatureDescriptor;
import com.intellij.psi.css.CssSimpleSelector;
import com.intellij.psi.css.descriptor.CssPseudoSelectorDescriptor;
import com.intellij.psi.css.descriptor.CssPseudoSelectorDescriptorStub;
import org.intellij.plugins.postcss.psi.PostCssPsiUtil;
import org.intellij.plugins.postcss.references.PostCssCustomMediaReference;
import org.intellij.plugins.postcss.references.PostCssCustomSelectorReference;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Collections;
import java.util.function.Predicate;
import java.util.function.Supplier;

public class PostCssElementDescriptorProvider extends CssElementDescriptorProvider {
  @Override
  public boolean isMyContext(@Nullable PsiElement context) {
    return PostCssPsiUtil.isInsidePostCss(context);
  }

  @NotNull
  @Override
  public PsiElement[] getDeclarationsForSimpleSelector(@NotNull CssSimpleSelector selector) {
    return PsiElement.EMPTY_ARRAY;
  }

  @NotNull
  @Override
  public Collection<? extends CssPseudoSelectorDescriptor> findPseudoSelectorDescriptors(@NotNull String name,
                                                                                         @Nullable PsiElement context) {
    if (context == null || context.getFirstChild() == null) return Collections.emptyList();
    if (context.textContains('&') || PostCssPsiUtil.isInsideRulesetWithNestedRulesets(context)) {
      return Collections.singletonList(new CssPseudoSelectorDescriptorStub(name));
    }
    return getDescriptorsByReferences(context.getFirstChild().getNextSibling(), r -> r instanceof PostCssCustomSelectorReference,
                                      () -> new CssPseudoSelectorDescriptorStub(name));
  }

  @Override
  public boolean isPossibleSelector(@NotNull String selector, @NotNull PsiElement context) {
    return PostCssPsiUtil.isInsideRulesetWithNestedRulesets(context);
  }

  @NotNull
  @Override
  public Collection<? extends CssMediaFeatureDescriptor> findMediaFeatureDescriptors(@NotNull String mediaFeatureName,
                                                                                     @Nullable PsiElement context) {
    if (context == null) return Collections.emptyList();
    return getDescriptorsByReferences(context.getFirstChild(), r -> r instanceof PostCssCustomMediaReference,
                                      () -> new CssMediaFeatureDescriptorStub(mediaFeatureName));
  }

  private static <T> Collection<T> getDescriptorsByReferences(@Nullable final PsiElement element,
                                                              @NotNull final Predicate<PsiReference> refCondition,
                                                              @NotNull final Supplier<T> descriptorGenerator) {
    if (element == null) return Collections.emptyList();
    for (PsiReference ref : element.getReferences()) {
      if (ref instanceof PsiPolyVariantReference && refCondition.test(ref)) {
        ResolveResult[] results = ((PsiPolyVariantReference)ref).multiResolve(false);
        return results.length > 0 ? Collections.singletonList(descriptorGenerator.get()) : Collections.emptyList();
      }
    }
    return Collections.emptyList();
  }
}