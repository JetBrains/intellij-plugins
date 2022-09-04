package com.thoughtworks.gauge.language;

import com.intellij.psi.tree.TokenSet;
import com.thoughtworks.gauge.language.token.SpecTokenTypes;

public final class SpecTokenSets {
  public static final TokenSet WHITE_SPACES = TokenSet.WHITE_SPACE;
  public static final TokenSet COMMENTS = TokenSet.create(SpecTokenTypes.SPEC_COMMENT);

  private SpecTokenSets() {
  }
}
