package org.intellij.plugins.postcss.psi;

import com.intellij.psi.css.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface PostCssNest extends CssAtRule, CssRuleset {

  PostCssNest[] EMPTY_ARRAY = new PostCssNest[0];

  @Override
  @NotNull
  CssSelector[] getSelectors();

  @Override
  @Nullable
  CssSelectorList getSelectorList();

  @Override
  @Nullable
  CssBlock getBlock();

  @Override
  @NotNull
  CssRuleset[] getNestedRulesets();

  @NotNull
  CssDeclaration[] getNestedDeclarations();

  @NotNull
  PostCssNest[] getNestedNests();

}