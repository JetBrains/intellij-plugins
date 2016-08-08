package org.intellij.plugins.postcss.descriptors;

import com.intellij.css.util.CssPsiUtil;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReference;
import com.intellij.psi.ResolveResult;
import com.intellij.psi.css.CssElementDescriptorProvider;
import com.intellij.psi.css.CssMediaFeatureDescriptor;
import com.intellij.psi.css.CssRuleset;
import com.intellij.psi.css.CssSimpleSelector;
import com.intellij.psi.css.descriptor.CssPseudoSelectorDescriptor;
import com.intellij.psi.css.descriptor.CssPseudoSelectorDescriptorStub;
import com.intellij.psi.util.PsiTreeUtil;
import org.intellij.plugins.postcss.PostCssLanguage;
import org.intellij.plugins.postcss.references.PostCssCustomMediaReference;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Collections;

public class PostCssElementDescriptorProvider extends CssElementDescriptorProvider {
  @Override
  public boolean isMyContext(@Nullable PsiElement context) {
    return PostCssLanguage.INSTANCE.is(CssPsiUtil.getStylesheetLanguage(context));
  }

  @NotNull
  @Override
  public PsiElement[] getDeclarationsForSimpleSelector(@NotNull CssSimpleSelector selector) {
    return PsiElement.EMPTY_ARRAY;
  }

  @NotNull
  @Override
  public Collection<? extends CssPseudoSelectorDescriptor> findPseudoSelectorDescriptors(@NotNull String name) {
    if (!StringUtil.startsWith(name, "--") && !StringUtil.containsChar(name, '&')) return Collections.emptyList();
    return Collections.singletonList(new CssPseudoSelectorDescriptorStub(name));
  }

  @Override
  public boolean isPossibleSelector(@NotNull String selector, @NotNull PsiElement context) {
    CssRuleset ruleset = PsiTreeUtil.getParentOfType(context, CssRuleset.class);
    return ruleset != null && PsiTreeUtil.findChildOfAnyType(ruleset.getBlock(), false, CssRuleset.class) != null;
  }

  @NotNull
  @Override
  public Collection<? extends CssMediaFeatureDescriptor> findMediaFeatureDescriptors(@NotNull String mediaFeatureName,
                                                                                     @Nullable PsiElement context) {
    if (context == null) return Collections.emptyList();
    PsiElement child = context.getFirstChild();
    if (child == null || child.getNextSibling() != null || !StringUtil.startsWith(mediaFeatureName, "--")) return Collections.emptyList();
    for (PsiReference ref : child.getReferences()) {
      if (ref instanceof PostCssCustomMediaReference) {
        ResolveResult[] results = ((PostCssCustomMediaReference)ref).multiResolve(false);
        return results.length > 0
               ? Collections.singletonList(new CssMediaFeatureDescriptorStub(mediaFeatureName))
               : Collections.emptyList();
      }
    }
    return Collections.emptyList();
  }
}