package org.intellij.plugins.postcss.descriptors;

import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiElement;
import com.intellij.psi.css.*;
import com.intellij.psi.css.descriptor.CssPseudoSelectorDescriptor;
import com.intellij.psi.css.descriptor.CssPseudoSelectorDescriptorStub;
import org.intellij.plugins.postcss.psi.*;
import org.intellij.plugins.postcss.psi.stubs.PostCssCustomMediaIndex;
import org.intellij.plugins.postcss.psi.stubs.PostCssCustomSelectorIndex;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Collections;

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
    if (context instanceof CssPseudoClass) {
      if (context.textContains('&') || PostCssPsiUtil.isInsideRulesetWithNestedRulesets(context)) {
        return Collections.singletonList(new CssPseudoSelectorDescriptorStub(name));
      }
      CssPseudoClass pseudoClass = (CssPseudoClass)context;
      PsiElement identifier = pseudoClass.getNameIdentifier();
      if (identifier == null || !StringUtil.startsWith(identifier.getText(), "--")) return Collections.emptyList();
      Collection<PostCssCustomSelector> customSelectors =
        PostCssCustomSelectorIndex.getCustomSelectors(identifier, identifier.getText().substring(2));
      return customSelectors.size() > 0 ? Collections.singletonList(new CssPseudoSelectorDescriptorStub(name)) : Collections.emptyList();
    }
    else if (context instanceof PostCssCustomSelectorAtRule) {
      return Collections.singletonList(new CssPseudoSelectorDescriptorStub(name));
    }
    return Collections.emptyList();
  }

  @Override
  public boolean isPossibleSelector(@NotNull String selector, @NotNull PsiElement context) {
    return PostCssPsiUtil.isInsideRulesetWithNestedRulesets(context);
  }

  @NotNull
  @Override
  public Collection<? extends CssMediaFeatureDescriptor> findMediaFeatureDescriptors(@NotNull String name,
                                                                                     @Nullable PsiElement context) {
    if (context instanceof CssMediaFeature) {
      CssMediaFeature mediaFeature = (CssMediaFeature)context;
      PsiElement identifier = mediaFeature.getNameIdentifier();
      if (identifier == null) return Collections.emptyList();
      boolean isNotTheOnly = identifier.getNextSibling() != null || identifier.getPrevSibling() != null;
      if (isNotTheOnly || !StringUtil.startsWith(identifier.getText(), "--")) return Collections.emptyList();
      Collection<PostCssCustomMedia> customMediaFeatures =
        PostCssCustomMediaIndex.getCustomMediaFeatures(identifier, identifier.getText().substring(2));
      return customMediaFeatures.size() > 0 ? Collections.singletonList(new CssMediaFeatureDescriptorStub(name)) : Collections.emptyList();
    }
    else if (context instanceof PostCssCustomMediaAtRule) {
      return Collections.singletonList(new CssMediaFeatureDescriptorStub(name));
    }
    return Collections.emptyList();
  }
}