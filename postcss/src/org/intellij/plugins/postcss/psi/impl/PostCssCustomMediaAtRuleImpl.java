package org.intellij.plugins.postcss.psi.impl;

import com.intellij.navigation.ItemPresentation;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.css.descriptor.CssContextType;
import com.intellij.psi.css.impl.AtRulePresentation;
import com.intellij.psi.css.impl.CssAtRuleImpl;
import com.intellij.psi.css.impl.CssElementTypes;
import com.intellij.psi.css.impl.CssTokenImpl;
import com.intellij.psi.util.PsiTreeUtil;
import org.intellij.plugins.postcss.PostCssElementTypes;
import org.intellij.plugins.postcss.psi.PostCssCustomMediaAtRule;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class PostCssCustomMediaAtRuleImpl extends CssAtRuleImpl implements PostCssCustomMediaAtRule {
  PostCssCustomMediaAtRuleImpl() {
    super(CssContextType.ANY, PostCssElementTypes.POST_CSS_CUSTOM_MEDIA_RULE);
  }

  @NotNull
  @Override
  public ItemPresentation getPresentation() {
    CssTokenImpl customMedia = getCustomMedia();
    return new AtRulePresentation(this, customMedia == null ? "custom-media" : "custom-media " + customMedia.getText());
  }

  @Nullable
  @Override
  public CssTokenImpl getCustomMedia() {
    CssTokenImpl customMedia = PsiTreeUtil.getNextSiblingOfType(getFirstChild(), CssTokenImpl.class);
    return customMedia != null && customMedia.getNode().getElementType() == CssElementTypes.CSS_IDENT ? customMedia : null;
  }

  @Override
  public void accept(@NotNull PsiElementVisitor visitor) {
    if (visitor instanceof PostCssElementVisitor) {
      ((PostCssElementVisitor)visitor).visitPostCssCustomMediaAtRule(this);
    }
    else {
      visitor.visitElement(this);
    }
  }
}