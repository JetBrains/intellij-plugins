package org.intellij.plugins.postcss.psi.impl;

import com.intellij.navigation.ItemPresentation;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.css.descriptor.CssContextType;
import com.intellij.psi.css.impl.AtRulePresentation;
import com.intellij.psi.css.impl.CssAtRuleImpl;
import com.intellij.psi.util.PsiTreeUtil;
import org.intellij.plugins.postcss.PostCssElementTypes;
import org.intellij.plugins.postcss.psi.PostCssCustomMedia;
import org.intellij.plugins.postcss.psi.PostCssCustomMediaAtRule;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class PostCssCustomMediaAtRuleImpl extends CssAtRuleImpl implements PostCssCustomMediaAtRule {
  PostCssCustomMediaAtRuleImpl() {
    super(CssContextType.ANY, PostCssElementTypes.POST_CSS_CUSTOM_MEDIA_RULE);
  }

  @Override
  public @NotNull ItemPresentation getPresentation() {
    PostCssCustomMedia customMedia = getCustomMedia();
    return new AtRulePresentation(this, customMedia == null ? "custom-media" : "custom-media " + customMedia.getText());
  }

  @Override
  public @Nullable PostCssCustomMedia getCustomMedia() {
    return PsiTreeUtil.getChildOfType(this, PostCssCustomMedia.class);
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