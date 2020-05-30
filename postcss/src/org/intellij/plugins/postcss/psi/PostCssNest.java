package org.intellij.plugins.postcss.psi;

import com.intellij.psi.css.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface PostCssNest extends CssAtRule, CssRuleset {

  PostCssNest[] EMPTY_ARRAY = new PostCssNest[0];

  @Override
  CssSelector @NotNull [] getSelectors();

  @Override
  @Nullable
  CssSelectorList getSelectorList();

  @Override
  @Nullable
  CssBlock getBlock();

  @Override
  CssRuleset @NotNull [] getNestedRulesets();

  CssDeclaration @NotNull [] getNestedDeclarations();

  PostCssNest @NotNull [] getNestedNests();

}