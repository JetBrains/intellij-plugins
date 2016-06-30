package org.intellij.plugins.postcss.psi;

import com.intellij.psi.css.*;
import com.intellij.util.ArrayFactory;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface PostCssNest extends CssAtRule {

  PostCssNest[] EMPTY_ARRAY = new PostCssNest[0];
  ArrayFactory<PostCssNest> ARRAY_FACTORY = count -> count == 0 ? EMPTY_ARRAY : new PostCssNest[count];

  @NotNull
  CssSelector[] getSelectors();
  
  @Nullable
  CssSelectorList getSelectorList();

  @Nullable
  CssBlock getBlock();

  @NotNull
  CssRuleset[] getNestedRulesets();

  @NotNull
  CssDeclaration[] getNestedDeclarations();

  @NotNull
  PostCssNest[] getNestedNests();

}