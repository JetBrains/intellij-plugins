package com.dmarcotte.handlebars.parsing;

import com.dmarcotte.handlebars.HbLanguage;
import com.intellij.psi.tree.IElementType;
import com.intellij.psi.tree.IFileElementType;
import com.intellij.psi.tree.TokenSet;

public class HbTokenTypes {

  /**
   * private constructor since this is a constants class
   */
  private HbTokenTypes() {
  }

  public static final IElementType BLOCK_WRAPPER = new HbCompositeElementType("BLOCK_WRAPPER");
    // used to delineate blocks in the PSI tree. The formatter requires this extra structure.
  public static final IElementType OPEN_BLOCK_STACHE = new HbCompositeElementType("OPEN_BLOCK_STACHE");
  public static final IElementType OPEN_INVERSE_BLOCK_STACHE = new HbCompositeElementType("OPEN_INVERSE_BLOCK_STACHE");
  public static final IElementType CLOSE_BLOCK_STACHE = new HbCompositeElementType("CLOSE_BLOCK_STACHE");
  public static final IElementType MUSTACHE = new HbCompositeElementType("MUSTACHE");
  public static final IElementType PATH = new HbCompositeElementType("PATH");
  public static final IElementType PARAM = new HbCompositeElementType("PARAM");
  public static final IElementType PARTIAL_STACHE = new HbCompositeElementType("PARTIAL_STACHE");
  public static final IElementType SIMPLE_INVERSE = new HbCompositeElementType("SIMPLE_INVERSE");
  public static final IElementType STATEMENTS = new HbCompositeElementType("STATEMENTS");

  public static final IElementType CONTENT = new HbElementType("CONTENT", "hb.parsing.element.expected.content");
  public static final IElementType OUTER_ELEMENT_TYPE = new HbElementType("HB_FRAGMENT", "hb.parsing.element.expected.outer_element_type");

  public static final IElementType WHITE_SPACE = new HbElementType("WHITE_SPACE", "hb.parsing.element.expected.white_space");
  public static final IElementType COMMENT = new HbElementType("COMMENT", "hb.parsing.element.expected.comment");
  public static final IElementType UNCLOSED_COMMENT = new HbElementType("UNCLOSED_COMMENT", "");

  public static final IElementType OPEN = new HbElementType("OPEN", "hb.parsing.element.expected.open");
  public static final IElementType OPEN_BLOCK = new HbElementType("OPEN_BLOCK", "hb.parsing.element.expected.open_block");
  public static final IElementType OPEN_PARTIAL = new HbElementType("OPEN_PARTIAL", "hb.parsing.element.expected.open_partial");
  public static final IElementType OPEN_ENDBLOCK = new HbElementType("OPEN_ENDBLOCK", "hb.parsing.element.expected.open_end_block");
  public static final IElementType OPEN_INVERSE = new HbElementType("OPEN_INVERSE", "hb.parsing.element.expected.open_inverse");
  public static final IElementType OPEN_UNESCAPED = new HbElementType("OPEN_UNESCAPED", "hb.parsing.element.expected.open_unescaped");
  public static final IElementType EQUALS = new HbElementType("EQUALS", "hb.parsing.element.expected.equals");
  public static final IElementType ID = new HbElementType("ID", "hb.parsing.element.expected.id");
  public static final IElementType PARTIAL_NAME = new HbElementType("PARTIAL_NAME", "hb.parsing.element.expected.partial.name");
  public static final IElementType DATA_PREFIX = new HbElementType("DATA_PREFIX", "hb.parsing.element.expected.data");
  public static final IElementType DATA = new HbElementType("DATA", "hb.parsing.element.expected.data");
  public static final IElementType SEP = new HbElementType("SEP", "hb.parsing.element.expected.separator");
  public static final IElementType CLOSE = new HbElementType("CLOSE", "hb.parsing.element.expected.close");
  public static final IElementType ELSE = new HbElementType("ELSE", "");
  public static final IElementType BOOLEAN = new HbElementType("BOOLEAN", "hb.parsing.element.expected.boolean");
  public static final IElementType INTEGER = new HbElementType("INTEGER", "hb.parsing.element.expected.integer");
  public static final IElementType STRING = new HbElementType("STRING", "hb.parsing.element.expected.string");
  public static final IElementType ESCAPE_CHAR = new HbElementType("ESCAPE_CHAR", "");
  public static final IElementType INVALID = new HbElementType("INVALID", "hb.parsing.element.expected.invalid");

  public static final IFileElementType FILE = new IFileElementType("FILE", HbLanguage.INSTANCE);

  public static final TokenSet WHITESPACES = TokenSet.create(WHITE_SPACE);
  public static final TokenSet COMMENTS = TokenSet.create(COMMENT);
  public static final TokenSet STRING_LITERALS = TokenSet.create(STRING);
}
