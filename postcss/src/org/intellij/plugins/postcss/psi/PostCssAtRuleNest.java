package org.intellij.plugins.postcss.psi;

import com.intellij.psi.css.CssSelectorList;
import org.jetbrains.annotations.Nullable;

public interface PostCssAtRuleNest extends PostCssElement {
  @Nullable
  CssSelectorList getSelectorList();
}
