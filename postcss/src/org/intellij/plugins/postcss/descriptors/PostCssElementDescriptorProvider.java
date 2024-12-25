package org.intellij.plugins.postcss.descriptors;

import com.intellij.openapi.project.DumbService;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiElement;
import com.intellij.psi.css.*;
import com.intellij.psi.css.descriptor.CssElementDescriptor;
import com.intellij.psi.css.descriptor.CssPseudoSelectorDescriptor;
import com.intellij.psi.css.descriptor.CssPseudoSelectorDescriptorStub;
import com.intellij.psi.css.impl.util.CssUtil;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.stubs.StubIndex;
import org.intellij.plugins.postcss.psi.*;
import org.intellij.plugins.postcss.psi.stubs.PostCssCustomMediaIndex;
import org.intellij.plugins.postcss.psi.stubs.PostCssCustomSelectorIndex;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

public class PostCssElementDescriptorProvider extends CssElementDescriptorProvider {
  @Override
  public boolean isMyContext(@Nullable PsiElement context) {
    return PostCssPsiUtil.isInsidePostCss(context);
  }

  @Override
  public PsiElement @NotNull [] getDeclarationsForSimpleSelector(@NotNull CssSimpleSelector selector) {
    return PsiElement.EMPTY_ARRAY;
  }

  @Override
  public @NotNull Collection<? extends CssPseudoSelectorDescriptor> findPseudoSelectorDescriptors(@NotNull String name,
                                                                                                  @Nullable PsiElement context) {
    if (context instanceof CssPseudoClass) {
      if (context.textContains('&') || PostCssPsiUtil.isInsideRulesetWithNestedRulesets(context)) {
        return Collections.singletonList(new CssPseudoSelectorDescriptorStub(name));
      }
      PsiElement identifier = ((CssPseudoClass)context).getNameIdentifier();
      String selectorName = identifier != null ? identifier.getText() : null;
      if (selectorName == null || !selectorName.startsWith("--")) {
        return Collections.emptyList();
      }
      Collection<PostCssCustomSelector> customSelectors = PostCssCustomSelectorIndex.getCustomSelectors(selectorName.substring(2), context);
      if (customSelectors.isEmpty()) {
        return Collections.emptyList();
      }
      return Collections.singletonList(new CssPseudoSelectorDescriptorStub(name));
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

  @Override
  public @NotNull Collection<? extends CssPseudoSelectorDescriptor> getAllPseudoSelectorDescriptors(final @Nullable PsiElement context) {
    if (context == null || DumbService.getInstance(context.getProject()).isDumb()) {
      return Collections.emptyList();
    }

    final Collection<CssPseudoSelectorDescriptor> result = new ArrayList<>();
    final GlobalSearchScope scope = CssUtil.getCompletionAndResolvingScopeForElement(context);

    final Collection<String> selectorNames = StubIndex.getInstance().getAllKeys(PostCssCustomSelectorIndex.KEY, context.getProject());
    for (String name : selectorNames) {
      if (name.isEmpty()) continue;

      StubIndex.getInstance()
        .processElements(PostCssCustomSelectorIndex.KEY, name, context.getProject(), scope, PostCssCustomSelector.class,
                         selector -> {
                           for (CssElementDescriptor descriptor : selector.getDescriptors()) {
                             if (descriptor instanceof CssPseudoSelectorDescriptor) {
                               result.add((CssPseudoSelectorDescriptor)descriptor);
                             }
                           }
                           return true;
                         });
    }

    return result;
  }

  @Override
  public @NotNull Collection<? extends CssMediaFeatureDescriptor> findMediaFeatureDescriptors(@NotNull String name,
                                                                                              @Nullable PsiElement context) {
    if (context instanceof CssMediaFeature mediaFeature) {
      PsiElement identifier = mediaFeature.getNameIdentifier();
      if (identifier == null) return Collections.emptyList();
      boolean isNotTheOnly = identifier.getNextSibling() != null || identifier.getPrevSibling() != null;
      String featureName = identifier.getText();
      if (isNotTheOnly || !StringUtil.startsWith(featureName, "--")) {
        return Collections.emptyList();
      }
      Collection<PostCssCustomMedia> features = PostCssCustomMediaIndex.getCustomMediaFeatures(featureName.substring(2), identifier);
      if (features.isEmpty()) {
        return Collections.emptyList();
      }
      return Collections.singletonList(new CssMediaFeatureDescriptorStub(name));
    }
    else if (context instanceof PostCssCustomMediaAtRule) {
      return Collections.singletonList(new CssMediaFeatureDescriptorStub(name));
    }
    return Collections.emptyList();
  }
}