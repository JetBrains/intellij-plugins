package org.intellij.plugins.postcss.psi.impl;

import com.intellij.css.util.CssPsiUtil;
import com.intellij.navigation.ItemPresentation;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.css.*;
import com.intellij.psi.css.descriptor.CssContextType;
import com.intellij.psi.css.impl.AtRulePresentation;
import com.intellij.psi.css.impl.CssAtRuleImpl;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.ObjectUtils;
import org.intellij.plugins.postcss.PostCssElementTypes;
import org.intellij.plugins.postcss.psi.PostCssNest;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class PostCssNestImpl extends CssAtRuleImpl implements PostCssNest {
  PostCssNestImpl() {
    super(CssContextType.ANY, PostCssElementTypes.POST_CSS_NEST);
  }

  @Override
  public @NotNull ItemPresentation getPresentation() {
    return new AtRulePresentation(this, getPresentableText());
  }

  @Override
  public CssSelector @NotNull [] getSelectors() {
    final CssSelectorList selectorList = getSelectorList();
    return selectorList != null ? selectorList.getSelectors() : CssSelector.EMPTY_ARRAY;
  }

  @Override
  public @Nullable CssSelectorList getSelectorList() {
    return PsiTreeUtil.getChildOfType(this, CssSelectorList.class);
  }

  @Override
  public @Nullable CssBlock getBlock() {
    return PsiTreeUtil.getChildOfType(this, CssBlock.class);
  }

  @Override
  public @NotNull String getPresentableText() {
    return ("nest " + CssPsiUtil.getTokenText(getSelectorList())).trim();
  }

  @Override
  public CssRuleset @NotNull [] getNestedRulesets() {
    final CssRuleset[] rulesets = PsiTreeUtil.getChildrenOfType(getBlock(), CssRuleset.class);
    return ObjectUtils.notNull(rulesets, CssRuleset.EMPTY_ARRAY);
  }

  @Override
  public CssDeclaration @NotNull [] getNestedDeclarations() {
    final CssDeclaration[] declarations = PsiTreeUtil.getChildrenOfType(getBlock(), CssDeclaration.class);
    return ObjectUtils.notNull(declarations, CssDeclaration.EMPTY_ARRAY);
  }

  @Override
  public PostCssNest @NotNull [] getNestedNests() {
    final PostCssNest[] nests = PsiTreeUtil.getChildrenOfType(getBlock(), PostCssNest.class);
    return ObjectUtils.notNull(nests, PostCssNest.EMPTY_ARRAY);
  }

  @Override
  public void accept(@NotNull PsiElementVisitor visitor) {
    if (visitor instanceof PostCssElementVisitor) {
      ((PostCssElementVisitor)visitor).visitPostCssNest(this);
    }
    else {
      visitor.visitElement(this);
    }
  }
}