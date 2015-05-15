package org.intellij.plugins.markdown.lang;

import com.intellij.psi.tree.TokenSet;

public interface MarkdownTokenTypeSets extends MarkdownElementTypes {
  TokenSet HEADER_MARKERS = TokenSet.create(
          MarkdownTokenTypes.ATX_HEADER,
          MarkdownTokenTypes.SETEXT_1,
          MarkdownTokenTypes.SETEXT_2);
  TokenSet HEADER_LEVEL_1_SET = TokenSet.create(ATX_1, SETEXT_1);
  TokenSet HEADER_LEVEL_2_SET = TokenSet.create(ATX_2, SETEXT_2);
  TokenSet HEADER_LEVEL_3_SET = TokenSet.create(ATX_3);
  TokenSet HEADER_LEVEL_4_SET = TokenSet.create(ATX_4);
  TokenSet HEADER_LEVEL_5_SET = TokenSet.create(ATX_5);
  TokenSet HEADER_LEVEL_6_SET = TokenSet.create(ATX_6);

  TokenSet REFERENCE_LINK_SET = TokenSet.create(FULL_REFERENCE_LINK, SHORT_REFERENCE_LINK);

  TokenSet LIST_MARKERS = TokenSet.create(MarkdownTokenTypes.LIST_BULLET, MarkdownTokenTypes.LIST_NUMBER);
}
