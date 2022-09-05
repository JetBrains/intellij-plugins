package com.thoughtworks.gauge.language;

import com.intellij.psi.tree.TokenSet;
import com.thoughtworks.gauge.language.token.ConceptTokenTypes;

public final class ConceptTokenSets {
  private ConceptTokenSets() {
  }


  public static final TokenSet COMMENTS = TokenSet.create(ConceptTokenTypes.CONCEPT_COMMENT);
}
