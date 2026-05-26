package org.intellij.plugins.postcss.psi;

import com.intellij.psi.css.CssSelectorList;
import org.jetbrains.annotations.Nullable;

public interface PostCssCustomSelectorAtRule extends PostCssOneLineAtRule {

  @Nullable
  PostCssCustomSelector getCustomSelector();

  @Nullable
  CssSelectorList getSelectorList();
}