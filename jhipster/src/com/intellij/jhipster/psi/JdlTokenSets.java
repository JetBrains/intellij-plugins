// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.

package com.intellij.jhipster.psi;

import com.intellij.psi.tree.TokenSet;

public final class JdlTokenSets {
  private JdlTokenSets() {
  }

  public static final TokenSet STRINGS = TokenSet.create(JdlTokenTypes.DOUBLE_QUOTED_STRING);
  public static final TokenSet BRACES = TokenSet.create(JdlTokenTypes.LBRACE, JdlTokenTypes.RBRACE);
  public static final TokenSet COMMENTS = TokenSet.create(JdlTokenTypes.LINE_COMMENT, JdlTokenTypes.BLOCK_COMMENT);
  public static final TokenSet DECLARATIONS = TokenSet.create(JdlTokenTypes.ENTITY, JdlTokenTypes.ENUM, JdlTokenTypes.CONSTANT);

  public static final TokenSet TOP_LEVEL_BLOCKS = TokenSet.create(
    JdlTokenTypes.APPLICATION,
    JdlTokenTypes.ENTITY,
    JdlTokenTypes.ENUM,
    JdlTokenTypes.RELATIONSHIP_GROUP,
    JdlTokenTypes.DEPLOYMENT
  );

  public static final TokenSet BLOCK_KEYWORDS = TokenSet.create(
    JdlTokenTypes.APPLICATION_KEYWORD,
    JdlTokenTypes.CONFIG_KEYWORD,
    JdlTokenTypes.ENTITY_KEYWORD,
    JdlTokenTypes.ENUM_KEYWORD,
    JdlTokenTypes.RELATIONSHIP_KEYWORD,
    JdlTokenTypes.DEPLOYMENT_KEYWORD
  );

  public static final TokenSet BLOCK_IDENTIFIERS = TokenSet.create(
    JdlTokenTypes.ENTITY_ID,
    JdlTokenTypes.ENUM_ID,
    JdlTokenTypes.RELATIONSHIP_TYPE
  );

  public static final TokenSet SPACING_TAIL_ELEMENTS = TokenSet.create(
    JdlTokenTypes.OPTION_NAME,
    JdlTokenTypes.CONFIGURATION_OPTION_NAME,
    JdlTokenTypes.ENTITY_KEYWORD,
    JdlTokenTypes.ENUM_KEYWORD,
    JdlTokenTypes.FIELD_NAME,
    JdlTokenTypes.RELATIONSHIP_KEYWORD
  );
}
