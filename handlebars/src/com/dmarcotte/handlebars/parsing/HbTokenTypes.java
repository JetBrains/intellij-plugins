// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.dmarcotte.handlebars.parsing;

import com.intellij.psi.tree.IElementType;
import com.intellij.psi.tree.TokenSet;

public final class HbTokenTypes {

  /**
   * private constructor since this is a constants class
   */
  private HbTokenTypes() {
  }

  public static final IElementType BLOCK_WRAPPER = new HbCompositeElementType("BLOCK_WRAPPER");
    // used to delineate blocks in the PSI tree. The formatter requires this extra structure.
  public static final IElementType OPEN_BLOCK_STACHE = new HbCompositeElementType("OPEN_BLOCK_STACHE");
  public static final IElementType OPEN_PARTIAL_BLOCK_STACHE = new HbCompositeElementType("OPEN_PARTIAL_BLOCK_STACHE");
  public static final IElementType OPEN_INVERSE_BLOCK_STACHE = new HbCompositeElementType("OPEN_INVERSE_BLOCK_STACHE");
  public static final IElementType CLOSE_BLOCK_STACHE = new HbCompositeElementType("CLOSE_BLOCK_STACHE");
  public static final IElementType MUSTACHE = new HbCompositeElementType("MUSTACHE");
  public static final IElementType MUSTACHE_NAME = new HbCompositeElementType("MUSTACHE_NAME");
  public static final IElementType PATH = new HbCompositeElementType("PATH");
  public static final IElementType PARAM = new HbCompositeElementType("PARAM");
  public static final IElementType PARTIAL_STACHE = new HbCompositeElementType("PARTIAL_STACHE");
  public static final IElementType PARTIAL_NAME = new HbCompositeElementType("PARTIAL_NAME");
  public static final IElementType SIMPLE_INVERSE = new HbCompositeElementType("SIMPLE_INVERSE");
  public static final IElementType STATEMENTS = new HbCompositeElementType("STATEMENTS");

  public static final IElementType CONTENT = new HbElementType("CONTENT", "hb.parsing.element.expected.content");
  public static final IElementType OUTER_ELEMENT_TYPE = new HbElementType("HB_FRAGMENT", "hb.parsing.element.expected.outer_element_type");

  public static final IElementType WHITE_SPACE = new HbElementType("WHITE_SPACE", "hb.parsing.element.expected.white_space");
  public static final IElementType COMMENT = new HbElementType("COMMENT", "hb.parsing.element.expected.comment");
  public static final IElementType COMMENT_CONTENT = new HbElementType("COMMENT_CONTENT", "hb.parsing.element.expected.comment.content");
  public static final IElementType COMMENT_OPEN = new HbElementType("COMMENT_OPEN", "hb.parsing.element.expected.comment.open");
  public static final IElementType COMMENT_CLOSE = new HbElementType("COMMENT_CLOSE", "hb.parsing.element.expected.comment.close");
  public static final IElementType UNCLOSED_COMMENT = new HbElementType("UNCLOSED_COMMENT", "");

  public static final IElementType OPEN = new HbElementType("OPEN", "hb.parsing.element.expected.open");
  public static final IElementType OPEN_BLOCK = new HbElementType("OPEN_BLOCK", "hb.parsing.element.expected.open_block");
  public static final IElementType OPEN_PARTIAL = new HbElementType("OPEN_PARTIAL", "hb.parsing.element.expected.open_partial");
  public static final IElementType OPEN_PARTIAL_BLOCK = new HbElementType("OPEN_PARTIAL_BLOCK", "hb.parsing.element.expected.open_partial_block");
  public static final IElementType OPEN_ENDBLOCK = new HbElementType("OPEN_ENDBLOCK", "hb.parsing.element.expected.open_end_block");
  public static final IElementType OPEN_INVERSE = new HbElementType("OPEN_INVERSE", "hb.parsing.element.expected.open_inverse");
  public static final IElementType OPEN_INVERSE_CHAIN = new HbElementType("OPEN_INVERSE_CHAIN", "hb.parsing.element.expected.open_inverse_chain");
  public static final IElementType OPEN_UNESCAPED = new HbElementType("OPEN_UNESCAPED", "hb.parsing.element.expected.open_unescaped");
  public static final IElementType OPEN_SEXPR = new HbElementType("OPEN_SEXPR", "hb.parsing.element.expected.open_sexpr");
  public static final IElementType CLOSE_SEXPR = new HbElementType("CLOSE_SEXPR", "hb.parsing.element.expected.close_sexpr");
  public static final IElementType OPEN_BLOCK_PARAMS = new HbElementType("OPEN_BLOCK_PARAMS", "hb.parsing.element.expected.open_block_params");
  public static final IElementType CLOSE_BLOCK_PARAMS = new HbElementType("CLOSE_BLOCK_PARAMS", "hb.parsing.element.expected.close_block_params");
  public static final IElementType OPEN_RAW_BLOCK = new HbElementType("OPEN_RAW_BLOCK", "hb.parsing.element.expected.open_raw_block");
  public static final IElementType END_RAW_BLOCK = new HbElementType("END_RAW_BLOCK", "hb.parsing.element.expected.end_raw_block");
  public static final IElementType CLOSE_RAW_BLOCK = new HbElementType("CLOSE_RAW_BLOCK", "hb.parsing.element.expected.close_raw_block");
  public static final IElementType EQUALS = new HbElementType("EQUALS", "hb.parsing.element.expected.equals");
  public static final IElementType ID = new HbElementType("ID", "hb.parsing.element.expected.id");
  public static final IElementType DATA_PREFIX = new HbElementType("DATA_PREFIX", "hb.parsing.element.expected.data");
  public static final IElementType DATA = new HbElementType("DATA", "hb.parsing.element.expected.data");
  public static final IElementType SEP = new HbElementType("SEP", "hb.parsing.element.expected.separator");
  public static final IElementType CLOSE = new HbElementType("CLOSE", "hb.parsing.element.expected.close");
  public static final IElementType CLOSE_UNESCAPED = new HbElementType("CLOSE_UNESCAPED", "hb.parsing.element.expected.close.unescaped");
  public static final IElementType ELSE = new HbElementType("ELSE", "");
  public static final IElementType BOOLEAN = new HbElementType("BOOLEAN", "hb.parsing.element.expected.boolean");

  public static final IElementType NULL = new HbElementType("NULL", "hb.parsing.element.expected.null");

  public static final IElementType UNDEFINED = new HbElementType("UNDEFINED", "hb.parsing.element.expected.undefined");
  public static final IElementType NUMBER = new HbElementType("NUMBER", "hb.parsing.element.expected.integer");
  public static final IElementType STRING = new HbElementType("STRING", "hb.parsing.element.expected.string");
  public static final IElementType ESCAPE_CHAR = new HbElementType("ESCAPE_CHAR", "");
  public static final IElementType INVALID = new HbElementType("INVALID", "hb.parsing.element.expected.invalid");
  public static final IElementType HASH = new HbCompositeElementType("HASH");

  public static final TokenSet WHITESPACES = TokenSet.create(WHITE_SPACE);
  public static final TokenSet COMMENTS = TokenSet.create(COMMENT, COMMENT_CONTENT);
  public static final TokenSet STRING_LITERALS = TokenSet.create(STRING);

  public static final TokenSet TAGS = TokenSet.create(OPEN_PARTIAL_BLOCK_STACHE, MUSTACHE, OPEN_INVERSE_BLOCK_STACHE, OPEN_BLOCK_STACHE);
}
