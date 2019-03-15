// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.lang.expr.lexer;

import com.intellij.lang.javascript.JSTokenTypes;
import com.intellij.psi.tree.IElementType;
import com.intellij.psi.tree.TokenSet;

public interface Angular2TokenTypes extends JSTokenTypes {
  IElementType ESCAPE_SEQUENCE = new Angular2TokenType("NG:ESCAPE_SEQUENCE");
  IElementType INVALID_ESCAPE_SEQUENCE = new Angular2TokenType("NG:INVALID_ESCAPE_SEQUENCE");
  IElementType XML_CHAR_ENTITY_REF = new Angular2TokenType("NG:XML_CHAR_ENTITY_REF");

  TokenSet KEYWORDS = TokenSet.create(VAR_KEYWORD, LET_KEYWORD, AS_KEYWORD, NULL_KEYWORD,
                                      UNDEFINED_KEYWORD, TRUE_KEYWORD, FALSE_KEYWORD, IF_KEYWORD,
                                      ELSE_KEYWORD, THIS_KEYWORD);

  TokenSet STRING_PART_SPECIAL_SEQ = TokenSet.create(ESCAPE_SEQUENCE, INVALID_ESCAPE_SEQUENCE, XML_CHAR_ENTITY_REF);
}
