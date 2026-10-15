// This is a generated file. Not intended for manual editing.
package com.intellij.dts.lang.parser;

import static com.intellij.dts.lang.psi.DtsTypes.*;
import static com.intellij.dts.lang.parser.DtsParserUtil.*;
import static com.intellij.lang.WhitespacesBinders.*;

@java.lang.SuppressWarnings({"SimplifiableIfStatement", "UnusedAssignment"})
public class DtsParser implements com.intellij.lang.PsiParser, com.intellij.lang.LightPsiParser {

  public com.intellij.lang.ASTNode parse(com.intellij.psi.tree.IElementType t, com.intellij.lang.PsiBuilder b) {
    parseLight(t, b);
    return b.getTreeBuilt();
  }

  public void parseLight(com.intellij.psi.tree.IElementType t, com.intellij.lang.PsiBuilder b) {
    boolean r;
    b = adapt_builder_(t, b, this, EXTENDS_SETS_);
    com.intellij.lang.PsiBuilder.Marker m = enter_section_(b, 0, _COLLAPSE_, null);
    r = parse_root_(t, b);
    exit_section_(b, 0, m, t, r, true, TRUE_CONDITION);
  }

  protected boolean parse_root_(com.intellij.psi.tree.IElementType t, com.intellij.lang.PsiBuilder b) {
    return parse_root_(t, b, 0);
  }

  static boolean parse_root_(com.intellij.psi.tree.IElementType t, com.intellij.lang.PsiBuilder b, int l) {
    return file(b, l + 1);
  }

  public static final com.intellij.psi.tree.TokenSet[] EXTENDS_SETS_ = new com.intellij.psi.tree.TokenSet[] {
    create_token_set_(ADD_EXPR, B_AND_EXPR, B_NOT_EXPR, B_OR_EXPR,
      CHAR_EXPR, DIV_EXPR, EQ_EXPR, EXPR,
      GEQ_EXPR, GRT_EXPR, INT_EXPR, LEQ_EXPR,
      LES_EXPR, LSH_EXPR, L_AND_EXPR, L_NOT_EXPR,
      L_OR_EXPR, MOD_EXPR, MUL_EXPR, NEG_EXPR,
      NEQ_EXPR, PAREN_EXPR, PP_MACRO_EXPR, RSH_EXPR,
      SUB_EXPR, TERNARY_EXPR, XOR_EXPR),
  };

  /* ********************************************************** */
  // BYTE_LITERAL
  public static boolean byte_$(com.intellij.lang.PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "byte_$")) return false;
    if (!nextTokenIs(b, BYTE_LITERAL)) return false;
    boolean r;
    com.intellij.lang.PsiBuilder.Marker m = enter_section_(b);
    r = consumeToken(b, BYTE_LITERAL);
    exit_section_(b, m, BYTE, r);
    return r;
  }

  /* ********************************************************** */
  // LBRACKET byteArrayContent RBRACKET
  public static boolean byteArray(com.intellij.lang.PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "byteArray")) return false;
    if (!nextTokenIs(b, LBRACKET)) return false;
    boolean r, p;
    com.intellij.lang.PsiBuilder.Marker m = enter_section_(b, l, _NONE_, BYTE_ARRAY, null);
    r = consumeToken(b, LBRACKET);
    p = r; // pin = 1
    r = r && report_error_(b, byteArrayContent(b, l + 1));
    r = p && consumeToken(b, RBRACKET) && r;
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  /* ********************************************************** */
  // byteArrayValue*
  static boolean byteArrayContent(com.intellij.lang.PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "byteArrayContent")) return false;
    com.intellij.lang.PsiBuilder.Marker m = enter_section_(b, l, _NONE_);
    while (true) {
      int c = current_position_(b);
      if (!byteArrayValue(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "byteArrayContent", c)) break;
    }
    exit_section_(b, l, m, true, false, DtsParser::byteArrayRecover);
    return true;
  }

  /* ********************************************************** */
  // !(SEMICOLON | RBRACE | COMMA | RBRACKET | RPAREN)
  static boolean byteArrayRecover(com.intellij.lang.PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "byteArrayRecover")) return false;
    boolean r;
    com.intellij.lang.PsiBuilder.Marker m = enter_section_(b, l, _NOT_);
    r = !byteArrayRecover_0(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  // SEMICOLON | RBRACE | COMMA | RBRACKET | RPAREN
  private static boolean byteArrayRecover_0(com.intellij.lang.PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "byteArrayRecover_0")) return false;
    boolean r;
    r = consumeTokenFast(b, SEMICOLON);
    if (!r) r = consumeTokenFast(b, RBRACE);
    if (!r) r = consumeTokenFast(b, COMMA);
    if (!r) r = consumeTokenFast(b, RBRACKET);
    if (!r) r = consumeTokenFast(b, RPAREN);
    return r;
  }

  /* ********************************************************** */
  // <<value (byte | ppMacro)>>
  static boolean byteArrayValue(com.intellij.lang.PsiBuilder b, int l) {
    return value(b, l + 1, DtsParser::byteArrayValue_0_0);
  }

  // byte | ppMacro
  private static boolean byteArrayValue_0_0(com.intellij.lang.PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "byteArrayValue_0_0")) return false;
    boolean r;
    r = byte_$(b, l + 1);
    if (!r) r = ppMacro(b, l + 1);
    return r;
  }

  /* ********************************************************** */
  // int | char | exprValue | ppMacro
  static boolean cell(com.intellij.lang.PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "cell")) return false;
    boolean r;
    r = int_$(b, l + 1);
    if (!r) r = char_$(b, l + 1);
    if (!r) r = exprValue(b, l + 1);
    if (!r) r = ppMacro(b, l + 1);
    return r;
  }

  /* ********************************************************** */
  // (&BITS cellArrayBits)? LANGL cellArrayContent RANGL
  public static boolean cellArray(com.intellij.lang.PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "cellArray")) return false;
    if (!nextTokenIs(b, "<cell array>", BITS, LANGL)) return false;
    boolean r, p;
    com.intellij.lang.PsiBuilder.Marker m = enter_section_(b, l, _NONE_, CELL_ARRAY, "<cell array>");
    r = cellArray_0(b, l + 1);
    r = r && consumeToken(b, LANGL);
    p = r; // pin = 2
    r = r && report_error_(b, cellArrayContent(b, l + 1));
    r = p && consumeToken(b, RANGL) && r;
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  // (&BITS cellArrayBits)?
  private static boolean cellArray_0(com.intellij.lang.PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "cellArray_0")) return false;
    cellArray_0_0(b, l + 1);
    return true;
  }

  // &BITS cellArrayBits
  private static boolean cellArray_0_0(com.intellij.lang.PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "cellArray_0_0")) return false;
    boolean r;
    com.intellij.lang.PsiBuilder.Marker m = enter_section_(b);
    r = cellArray_0_0_0(b, l + 1);
    r = r && cellArrayBits(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // &BITS
  private static boolean cellArray_0_0_0(com.intellij.lang.PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "cellArray_0_0_0")) return false;
    boolean r;
    com.intellij.lang.PsiBuilder.Marker m = enter_section_(b, l, _AND_);
    r = consumeToken(b, BITS);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  /* ********************************************************** */
  // BITS (int | ppMacro)
  public static boolean cellArrayBits(com.intellij.lang.PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "cellArrayBits")) return false;
    boolean r, p;
    com.intellij.lang.PsiBuilder.Marker m = enter_section_(b, l, _NONE_, CELL_ARRAY_BITS, "<cell array bits>");
    r = consumeToken(b, BITS);
    p = r; // pin = 1
    r = r && cellArrayBits_1(b, l + 1);
    exit_section_(b, l, m, r, p, DtsParser::cellArrayBitsRecover);
    return r || p;
  }

  // int | ppMacro
  private static boolean cellArrayBits_1(com.intellij.lang.PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "cellArrayBits_1")) return false;
    boolean r;
    r = int_$(b, l + 1);
    if (!r) r = ppMacro(b, l + 1);
    return r;
  }

  /* ********************************************************** */
  // !(SEMICOLON | RBRACE | LANGL)
  static boolean cellArrayBitsRecover(com.intellij.lang.PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "cellArrayBitsRecover")) return false;
    boolean r;
    com.intellij.lang.PsiBuilder.Marker m = enter_section_(b, l, _NOT_);
    r = !cellArrayBitsRecover_0(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  // SEMICOLON | RBRACE | LANGL
  private static boolean cellArrayBitsRecover_0(com.intellij.lang.PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "cellArrayBitsRecover_0")) return false;
    boolean r;
    r = consumeTokenFast(b, SEMICOLON);
    if (!r) r = consumeTokenFast(b, RBRACE);
    if (!r) r = consumeTokenFast(b, LANGL);
    return r;
  }

  /* ********************************************************** */
  // cellArrayValue*
  static boolean cellArrayContent(com.intellij.lang.PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "cellArrayContent")) return false;
    com.intellij.lang.PsiBuilder.Marker m = enter_section_(b, l, _NONE_);
    while (true) {
      int c = current_position_(b);
      if (!cellArrayValue(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "cellArrayContent", c)) break;
    }
    exit_section_(b, l, m, true, false, DtsParser::cellArrayRecover);
    return true;
  }

  /* ********************************************************** */
  // !(SEMICOLON | RBRACE | COMMA | RANGL | RPAREN)
  static boolean cellArrayRecover(com.intellij.lang.PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "cellArrayRecover")) return false;
    boolean r;
    com.intellij.lang.PsiBuilder.Marker m = enter_section_(b, l, _NOT_);
    r = !cellArrayRecover_0(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  // SEMICOLON | RBRACE | COMMA | RANGL | RPAREN
  private static boolean cellArrayRecover_0(com.intellij.lang.PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "cellArrayRecover_0")) return false;
    boolean r;
    r = consumeTokenFast(b, SEMICOLON);
    if (!r) r = consumeTokenFast(b, RBRACE);
    if (!r) r = consumeTokenFast(b, COMMA);
    if (!r) r = consumeTokenFast(b, RANGL);
    if (!r) r = consumeTokenFast(b, RPAREN);
    return r;
  }

  /* ********************************************************** */
  // <<value (pHandle | cell)>>
  static boolean cellArrayValue(com.intellij.lang.PsiBuilder b, int l) {
    return value(b, l + 1, DtsParser::cellArrayValue_0_0);
  }

  // pHandle | cell
  private static boolean cellArrayValue_0_0(com.intellij.lang.PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "cellArrayValue_0_0")) return false;
    boolean r;
    r = pHandle(b, l + 1);
    if (!r) r = cell(b, l + 1);
    return r;
  }

  /* ********************************************************** */
  // CHAR_LITERAL
  public static boolean char_$(com.intellij.lang.PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "char_$")) return false;
    if (!nextTokenIs(b, CHAR_LITERAL)) return false;
    boolean r;
    com.intellij.lang.PsiBuilder.Marker m = enter_section_(b);
    r = consumeToken(b, CHAR_LITERAL);
    exit_section_(b, m, CHAR, r);
    return r;
  }

  /* ********************************************************** */
  static Parser compilerDirective_$(Parser _directive, Parser _arg) {
    return (b, l) -> compilerDirective(b, l + 1, _directive, _arg);
  }

  // <<directive>> <<arg>>
  public static boolean compilerDirective(com.intellij.lang.PsiBuilder b, int l, Parser _directive, Parser _arg) {
    if (!recursion_guard_(b, l, "compilerDirective")) return false;
    boolean r, p;
    com.intellij.lang.PsiBuilder.Marker m = enter_section_(b, l, _NONE_, COMPILER_DIRECTIVE, null);
    r = _directive.parse(b, l);
    p = r; // pin = 1
    r = r && _arg.parse(b, l);
    exit_section_(b, l, m, r, p, DtsParser::compilerDirectiveRecover);
    return r || p;
  }

  /* ********************************************************** */
  // <<entry <<compilerDirective <<directive>> <<arg>>>>>>
  static boolean compilerDirectiveEntry(com.intellij.lang.PsiBuilder b, int l, Parser _directive, Parser _arg) {
    return entry(b, l + 1, compilerDirective_$(_directive, _arg));
  }

  /* ********************************************************** */
  // !(SEMICOLON | RBRACE | afterLineBreak)
  static boolean compilerDirectiveRecover(com.intellij.lang.PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "compilerDirectiveRecover")) return false;
    boolean r;
    com.intellij.lang.PsiBuilder.Marker m = enter_section_(b, l, _NOT_);
    r = !compilerDirectiveRecover_0(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  // SEMICOLON | RBRACE | afterLineBreak
  private static boolean compilerDirectiveRecover_0(com.intellij.lang.PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "compilerDirectiveRecover_0")) return false;
    boolean r;
    com.intellij.lang.PsiBuilder.Marker m = enter_section_(b);
    r = consumeTokenFast(b, SEMICOLON);
    if (!r) r = consumeTokenFast(b, RBRACE);
    if (!r) r = parseAfterLineBreak(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // <<entry property>> |
  //         <<entry subNode>> |
  //         <<entry refNode>> |
  //         <<entry rootNode>> |
  //         <<compilerDirectiveEntry V1 ()>> |
  //         <<compilerDirectiveEntry PLUGIN ()>> |
  //         <<compilerDirectiveEntry (LABEL* MEMRESERVE) (cell cell)>> |
  //         <<compilerDirectiveEntry DELETE_NODE (pHandle | NAME)>> |
  //         <<compilerDirectiveEntry DELETE_PROP NAME>> |
  //         <<compilerDirectiveEntry OMIT_NODE pHandle>> |
  //         invalidEntry
  static boolean entries(com.intellij.lang.PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "entries")) return false;
    boolean r;
    com.intellij.lang.PsiBuilder.Marker m = enter_section_(b);
    r = entry(b, l + 1, DtsParser::property);
    if (!r) r = entry(b, l + 1, DtsParser::subNode);
    if (!r) r = entry(b, l + 1, DtsParser::refNode);
    if (!r) r = entry(b, l + 1, DtsParser::rootNode);
    if (!r) r = compilerDirectiveEntry(b, l + 1, V1_parser_, DtsParser::entries_4_1);
    if (!r) r = compilerDirectiveEntry(b, l + 1, PLUGIN_parser_, DtsParser::entries_5_1);
    if (!r) r = compilerDirectiveEntry(b, l + 1, DtsParser::entries_6_0, DtsParser::entries_6_1);
    if (!r) r = compilerDirectiveEntry(b, l + 1, DELETE_NODE_parser_, DtsParser::entries_7_1);
    if (!r) r = compilerDirectiveEntry(b, l + 1, DELETE_PROP_parser_, NAME_parser_);
    if (!r) r = compilerDirectiveEntry(b, l + 1, OMIT_NODE_parser_, DtsParser::pHandle);
    if (!r) r = parseInvalidEntry(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // ()
  private static boolean entries_4_1(com.intellij.lang.PsiBuilder b, int l) {
    return true;
  }

  // ()
  private static boolean entries_5_1(com.intellij.lang.PsiBuilder b, int l) {
    return true;
  }

  // LABEL* MEMRESERVE
  private static boolean entries_6_0(com.intellij.lang.PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "entries_6_0")) return false;
    boolean r;
    com.intellij.lang.PsiBuilder.Marker m = enter_section_(b);
    r = entries_6_0_0(b, l + 1);
    r = r && consumeToken(b, MEMRESERVE);
    exit_section_(b, m, null, r);
    return r;
  }

  // LABEL*
  private static boolean entries_6_0_0(com.intellij.lang.PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "entries_6_0_0")) return false;
    while (true) {
      int c = current_position_(b);
      if (!consumeToken(b, LABEL)) break;
      if (!empty_element_parsed_guard_(b, "entries_6_0_0", c)) break;
    }
    return true;
  }

  // cell cell
  private static boolean entries_6_1(com.intellij.lang.PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "entries_6_1")) return false;
    boolean r;
    com.intellij.lang.PsiBuilder.Marker m = enter_section_(b);
    r = cell(b, l + 1);
    r = r && cell(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // pHandle | NAME
  private static boolean entries_7_1(com.intellij.lang.PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "entries_7_1")) return false;
    boolean r;
    r = pHandle(b, l + 1);
    if (!r) r = consumeToken(b, NAME);
    return r;
  }

  /* ********************************************************** */
  // <<arg>> SEMICOLON
  public static boolean entry(com.intellij.lang.PsiBuilder b, int l, Parser _arg) {
    if (!recursion_guard_(b, l, "entry")) return false;
    boolean r, p;
    com.intellij.lang.PsiBuilder.Marker m = enter_section_(b, l, _NONE_, ENTRY, null);
    r = _arg.parse(b, l);
    p = r; // pin = 1
    r = r && consumeToken(b, SEMICOLON);
    register_hook_(b, RIGHT_BINDER, trailingCommentsBinder);
    register_hook_(b, LEFT_BINDER, leadingCommentsBinder);
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  /* ********************************************************** */
  // LPAREN expr RPAREN
  public static boolean exprValue(com.intellij.lang.PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "exprValue")) return false;
    if (!nextTokenIs(b, LPAREN)) return false;
    boolean r, p;
    com.intellij.lang.PsiBuilder.Marker m = enter_section_(b, l, _NONE_, EXPR_VALUE, null);
    r = consumeToken(b, LPAREN);
    p = r; // pin = 1
    r = r && report_error_(b, expr(b, l + 1, -1));
    r = p && consumeToken(b, RPAREN) && r;
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  /* ********************************************************** */
  // entries*
  static boolean file(com.intellij.lang.PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "file")) return false;
    while (true) {
      int c = current_position_(b);
      if (!entries(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "file", c)) break;
    }
    return true;
  }

  /* ********************************************************** */
  // () INCLUDE INCLUDE_PATH
  public static boolean includeStatement(com.intellij.lang.PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "includeStatement")) return false;
    boolean r, p;
    com.intellij.lang.PsiBuilder.Marker m = enter_section_(b, l, _NONE_, INCLUDE_STATEMENT, "<include statement>");
    r = includeStatement_0(b, l + 1);
    p = r; // pin = 1
    r = r && report_error_(b, consumeTokens(b, -1, INCLUDE, INCLUDE_PATH));
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  // ()
  private static boolean includeStatement_0(com.intellij.lang.PsiBuilder b, int l) {
    return true;
  }

  /* ********************************************************** */
  // INT_LITERAL
  public static boolean int_$(com.intellij.lang.PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "int_$")) return false;
    if (!nextTokenIs(b, INT_LITERAL)) return false;
    boolean r;
    com.intellij.lang.PsiBuilder.Marker m = enter_section_(b);
    r = consumeToken(b, INT_LITERAL);
    exit_section_(b, m, INT, r);
    return r;
  }

  /* ********************************************************** */
  // (!RBRACE entries)+
  public static boolean nodeContent(com.intellij.lang.PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "nodeContent")) return false;
    boolean r;
    com.intellij.lang.PsiBuilder.Marker m = enter_section_(b, l, _NONE_, NODE_CONTENT, "<node content>");
    r = nodeContent_0(b, l + 1);
    while (r) {
      int c = current_position_(b);
      if (!nodeContent_0(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "nodeContent", c)) break;
    }
    register_hook_(b, RIGHT_BINDER, GREEDY_RIGHT_BINDER);
    register_hook_(b, LEFT_BINDER, GREEDY_LEFT_BINDER);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  // !RBRACE entries
  private static boolean nodeContent_0(com.intellij.lang.PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "nodeContent_0")) return false;
    boolean r;
    com.intellij.lang.PsiBuilder.Marker m = enter_section_(b);
    r = nodeContent_0_0(b, l + 1);
    r = r && entries(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // !RBRACE
  private static boolean nodeContent_0_0(com.intellij.lang.PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "nodeContent_0_0")) return false;
    boolean r;
    com.intellij.lang.PsiBuilder.Marker m = enter_section_(b, l, _NOT_);
    r = !consumeToken(b, RBRACE);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  /* ********************************************************** */
  // HANDLE (NAME | pHandlePath)
  public static boolean pHandle(com.intellij.lang.PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "pHandle")) return false;
    if (!nextTokenIs(b, HANDLE)) return false;
    boolean r, p;
    com.intellij.lang.PsiBuilder.Marker m = enter_section_(b, l, _NONE_, P_HANDLE, null);
    r = consumeToken(b, HANDLE);
    p = r; // pin = 1
    r = r && pHandle_1(b, l + 1);
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  // NAME | pHandlePath
  private static boolean pHandle_1(com.intellij.lang.PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "pHandle_1")) return false;
    boolean r;
    r = consumeToken(b, NAME);
    if (!r) r = pHandlePath(b, l + 1);
    return r;
  }

  /* ********************************************************** */
  // LBRACE path RBRACE
  static boolean pHandlePath(com.intellij.lang.PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "pHandlePath")) return false;
    if (!nextTokenIs(b, LBRACE)) return false;
    boolean r, p;
    com.intellij.lang.PsiBuilder.Marker m = enter_section_(b, l, _NONE_);
    r = consumeToken(b, LBRACE);
    p = r; // pin = 1
    r = r && report_error_(b, path(b, l + 1));
    r = p && consumeToken(b, RBRACE) && r;
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  /* ********************************************************** */
  // PATH
  static boolean path(com.intellij.lang.PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "path")) return false;
    boolean r;
    com.intellij.lang.PsiBuilder.Marker m = enter_section_(b, l, _NONE_);
    r = consumeToken(b, PATH);
    exit_section_(b, l, m, r, false, DtsParser::pathRecover);
    return r;
  }

  /* ********************************************************** */
  // !(SEMICOLON | RANGL | RBRACE | afterLineBreak)
  static boolean pathRecover(com.intellij.lang.PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "pathRecover")) return false;
    boolean r;
    com.intellij.lang.PsiBuilder.Marker m = enter_section_(b, l, _NOT_);
    r = !pathRecover_0(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  // SEMICOLON | RANGL | RBRACE | afterLineBreak
  private static boolean pathRecover_0(com.intellij.lang.PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "pathRecover_0")) return false;
    boolean r;
    com.intellij.lang.PsiBuilder.Marker m = enter_section_(b);
    r = consumeTokenFast(b, SEMICOLON);
    if (!r) r = consumeTokenFast(b, RANGL);
    if (!r) r = consumeTokenFast(b, RBRACE);
    if (!r) r = parseAfterLineBreak(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // parserPpMacro
  public static boolean ppMacro(com.intellij.lang.PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ppMacro")) return false;
    boolean r;
    com.intellij.lang.PsiBuilder.Marker m = enter_section_(b, l, _NONE_, PP_MACRO, "<pp macro>");
    r = parsePpMacro(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  /* ********************************************************** */
  // LABEL* propertyName propertyAssignment?
  public static boolean property(com.intellij.lang.PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "property")) return false;
    boolean r;
    com.intellij.lang.PsiBuilder.Marker m = enter_section_(b, l, _NONE_, PROPERTY, "<property>");
    r = property_0(b, l + 1);
    r = r && parsePropertyName(b, l + 1);
    r = r && property_2(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  // LABEL*
  private static boolean property_0(com.intellij.lang.PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "property_0")) return false;
    while (true) {
      int c = current_position_(b);
      if (!consumeToken(b, LABEL)) break;
      if (!empty_element_parsed_guard_(b, "property_0", c)) break;
    }
    return true;
  }

  // propertyAssignment?
  private static boolean property_2(com.intellij.lang.PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "property_2")) return false;
    propertyAssignment(b, l + 1);
    return true;
  }

  /* ********************************************************** */
  // ASSIGN propertyContent
  static boolean propertyAssignment(com.intellij.lang.PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "propertyAssignment")) return false;
    if (!nextTokenIs(b, ASSIGN)) return false;
    boolean r, p;
    com.intellij.lang.PsiBuilder.Marker m = enter_section_(b, l, _NONE_);
    r = consumeToken(b, ASSIGN);
    p = r; // pin = 1
    r = r && propertyContent(b, l + 1);
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  /* ********************************************************** */
  // propertyValue (COMMA propertyValue)*
  public static boolean propertyContent(com.intellij.lang.PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "propertyContent")) return false;
    boolean r;
    com.intellij.lang.PsiBuilder.Marker m = enter_section_(b, l, _NONE_, PROPERTY_CONTENT, "<property content>");
    r = propertyValue(b, l + 1);
    r = r && propertyContent_1(b, l + 1);
    exit_section_(b, l, m, r, false, DtsParser::propertyRecover);
    return r;
  }

  // (COMMA propertyValue)*
  private static boolean propertyContent_1(com.intellij.lang.PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "propertyContent_1")) return false;
    while (true) {
      int c = current_position_(b);
      if (!propertyContent_1_0(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "propertyContent_1", c)) break;
    }
    return true;
  }

  // COMMA propertyValue
  private static boolean propertyContent_1_0(com.intellij.lang.PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "propertyContent_1_0")) return false;
    boolean r;
    com.intellij.lang.PsiBuilder.Marker m = enter_section_(b);
    r = consumeToken(b, COMMA);
    r = r && propertyValue(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // !(SEMICOLON | RBRACE | NAME | afterLineBreak)
  static boolean propertyRecover(com.intellij.lang.PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "propertyRecover")) return false;
    boolean r;
    com.intellij.lang.PsiBuilder.Marker m = enter_section_(b, l, _NOT_);
    r = !propertyRecover_0(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  // SEMICOLON | RBRACE | NAME | afterLineBreak
  private static boolean propertyRecover_0(com.intellij.lang.PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "propertyRecover_0")) return false;
    boolean r;
    com.intellij.lang.PsiBuilder.Marker m = enter_section_(b);
    r = consumeTokenFast(b, SEMICOLON);
    if (!r) r = consumeTokenFast(b, RBRACE);
    if (!r) r = consumeTokenFast(b, NAME);
    if (!r) r = parseAfterLineBreak(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // <<value (string | pHandle | cellArray | byteArray | ppMacro)>>
  static boolean propertyValue(com.intellij.lang.PsiBuilder b, int l) {
    return value(b, l + 1, DtsParser::propertyValue_0_0);
  }

  // string | pHandle | cellArray | byteArray | ppMacro
  private static boolean propertyValue_0_0(com.intellij.lang.PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "propertyValue_0_0")) return false;
    boolean r;
    r = string(b, l + 1);
    if (!r) r = pHandle(b, l + 1);
    if (!r) r = cellArray(b, l + 1);
    if (!r) r = byteArray(b, l + 1);
    if (!r) r = ppMacro(b, l + 1);
    return r;
  }

  /* ********************************************************** */
  // LABEL* pHandle LBRACE nodeContent? RBRACE
  public static boolean refNode(com.intellij.lang.PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "refNode")) return false;
    boolean r, p;
    com.intellij.lang.PsiBuilder.Marker m = enter_section_(b, l, _NONE_, REF_NODE, "<ref node>");
    r = refNode_0(b, l + 1);
    r = r && pHandle(b, l + 1);
    r = r && consumeToken(b, LBRACE);
    p = r; // pin = 3
    r = r && report_error_(b, refNode_3(b, l + 1));
    r = p && consumeToken(b, RBRACE) && r;
    exit_section_(b, l, m, r, p, DtsParser::rootNodeRecover);
    return r || p;
  }

  // LABEL*
  private static boolean refNode_0(com.intellij.lang.PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "refNode_0")) return false;
    while (true) {
      int c = current_position_(b);
      if (!consumeToken(b, LABEL)) break;
      if (!empty_element_parsed_guard_(b, "refNode_0", c)) break;
    }
    return true;
  }

  // nodeContent?
  private static boolean refNode_3(com.intellij.lang.PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "refNode_3")) return false;
    nodeContent(b, l + 1);
    return true;
  }

  /* ********************************************************** */
  // SLASH LBRACE nodeContent? RBRACE
  public static boolean rootNode(com.intellij.lang.PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "rootNode")) return false;
    boolean r, p;
    com.intellij.lang.PsiBuilder.Marker m = enter_section_(b, l, _NONE_, ROOT_NODE, "<root node>");
    r = consumeTokens(b, 2, SLASH, LBRACE);
    p = r; // pin = 2
    r = r && report_error_(b, rootNode_2(b, l + 1));
    r = p && consumeToken(b, RBRACE) && r;
    exit_section_(b, l, m, r, p, DtsParser::rootNodeRecover);
    return r || p;
  }

  // nodeContent?
  private static boolean rootNode_2(com.intellij.lang.PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "rootNode_2")) return false;
    nodeContent(b, l + 1);
    return true;
  }

  /* ********************************************************** */
  // !(SEMICOLON | RBRACE | HANDLE | afterLineBreak)
  static boolean rootNodeRecover(com.intellij.lang.PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "rootNodeRecover")) return false;
    boolean r;
    com.intellij.lang.PsiBuilder.Marker m = enter_section_(b, l, _NOT_);
    r = !rootNodeRecover_0(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  // SEMICOLON | RBRACE | HANDLE | afterLineBreak
  private static boolean rootNodeRecover_0(com.intellij.lang.PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "rootNodeRecover_0")) return false;
    boolean r;
    com.intellij.lang.PsiBuilder.Marker m = enter_section_(b);
    r = consumeTokenFast(b, SEMICOLON);
    if (!r) r = consumeTokenFast(b, RBRACE);
    if (!r) r = consumeTokenFast(b, HANDLE);
    if (!r) r = parseAfterLineBreak(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // STRING_LITERAL
  public static boolean string(com.intellij.lang.PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "string")) return false;
    if (!nextTokenIs(b, STRING_LITERAL)) return false;
    boolean r;
    com.intellij.lang.PsiBuilder.Marker m = enter_section_(b);
    r = consumeToken(b, STRING_LITERAL);
    exit_section_(b, m, STRING, r);
    return r;
  }

  /* ********************************************************** */
  // LABEL* OMIT_NODE? LABEL* NAME LBRACE nodeContent? RBRACE
  public static boolean subNode(com.intellij.lang.PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "subNode")) return false;
    boolean r, p;
    com.intellij.lang.PsiBuilder.Marker m = enter_section_(b, l, _NONE_, SUB_NODE, "<sub node>");
    r = subNode_0(b, l + 1);
    r = r && subNode_1(b, l + 1);
    r = r && subNode_2(b, l + 1);
    r = r && consumeTokens(b, 2, NAME, LBRACE);
    p = r; // pin = 5
    r = r && report_error_(b, subNode_5(b, l + 1));
    r = p && consumeToken(b, RBRACE) && r;
    exit_section_(b, l, m, r, p, DtsParser::subNodeRecover);
    return r || p;
  }

  // LABEL*
  private static boolean subNode_0(com.intellij.lang.PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "subNode_0")) return false;
    while (true) {
      int c = current_position_(b);
      if (!consumeToken(b, LABEL)) break;
      if (!empty_element_parsed_guard_(b, "subNode_0", c)) break;
    }
    return true;
  }

  // OMIT_NODE?
  private static boolean subNode_1(com.intellij.lang.PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "subNode_1")) return false;
    consumeToken(b, OMIT_NODE);
    return true;
  }

  // LABEL*
  private static boolean subNode_2(com.intellij.lang.PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "subNode_2")) return false;
    while (true) {
      int c = current_position_(b);
      if (!consumeToken(b, LABEL)) break;
      if (!empty_element_parsed_guard_(b, "subNode_2", c)) break;
    }
    return true;
  }

  // nodeContent?
  private static boolean subNode_5(com.intellij.lang.PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "subNode_5")) return false;
    nodeContent(b, l + 1);
    return true;
  }

  /* ********************************************************** */
  // !(SEMICOLON | RBRACE | NAME | afterLineBreak)
  static boolean subNodeRecover(com.intellij.lang.PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "subNodeRecover")) return false;
    boolean r;
    com.intellij.lang.PsiBuilder.Marker m = enter_section_(b, l, _NOT_);
    r = !subNodeRecover_0(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  // SEMICOLON | RBRACE | NAME | afterLineBreak
  private static boolean subNodeRecover_0(com.intellij.lang.PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "subNodeRecover_0")) return false;
    boolean r;
    com.intellij.lang.PsiBuilder.Marker m = enter_section_(b);
    r = consumeTokenFast(b, SEMICOLON);
    if (!r) r = consumeTokenFast(b, RBRACE);
    if (!r) r = consumeTokenFast(b, NAME);
    if (!r) r = parseAfterLineBreak(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // LABEL* <<arg>> trailingLabels?
  static boolean value(com.intellij.lang.PsiBuilder b, int l, Parser _arg) {
    if (!recursion_guard_(b, l, "value")) return false;
    boolean r;
    com.intellij.lang.PsiBuilder.Marker m = enter_section_(b);
    r = value_0(b, l + 1);
    r = r && _arg.parse(b, l);
    r = r && value_2(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // LABEL*
  private static boolean value_0(com.intellij.lang.PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "value_0")) return false;
    while (true) {
      int c = current_position_(b);
      if (!consumeToken(b, LABEL)) break;
      if (!empty_element_parsed_guard_(b, "value_0", c)) break;
    }
    return true;
  }

  // trailingLabels?
  private static boolean value_2(com.intellij.lang.PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "value_2")) return false;
    parseTrailingLabels(b, l + 1);
    return true;
  }

  /* ********************************************************** */
  // Expression root: expr
  // Operator priority table:
  // 0: ATOM(ppMacroExpr)
  // 1: BINARY(ternaryExpr)
  // 2: BINARY(lOrExpr)
  // 3: BINARY(lAndExpr)
  // 4: BINARY(bOrExpr)
  // 5: BINARY(xorExpr)
  // 6: BINARY(bAndExpr)
  // 7: BINARY(eqExpr) BINARY(neqExpr)
  // 8: BINARY(lesExpr) BINARY(grtExpr) BINARY(leqExpr) BINARY(geqExpr)
  // 9: BINARY(lshExpr) BINARY(rshExpr)
  // 10: BINARY(addExpr) BINARY(subExpr)
  // 11: BINARY(mulExpr) BINARY(divExpr) BINARY(modExpr)
  // 12: PREFIX(lNotExpr) PREFIX(bNotExpr) PREFIX(negExpr)
  // 13: ATOM(intExpr) ATOM(charExpr) PREFIX(parenExpr)
  public static boolean expr(com.intellij.lang.PsiBuilder b, int l, int g) {
    if (!recursion_guard_(b, l, "expr")) return false;
    addVariant(b, "<expr>");
    boolean r, p;
    com.intellij.lang.PsiBuilder.Marker m = enter_section_(b, l, _NONE_, "<expr>");
    r = ppMacroExpr(b, l + 1);
    if (!r) r = negExpr(b, l + 1);
    if (!r) r = lNotExpr(b, l + 1);
    if (!r) r = bNotExpr(b, l + 1);
    if (!r) r = intExpr(b, l + 1);
    if (!r) r = charExpr(b, l + 1);
    if (!r) r = parenExpr(b, l + 1);
    p = r;
    r = r && expr_0(b, l + 1, g);
    exit_section_(b, l, m, null, r, p, null);
    return r || p;
  }

  public static boolean expr_0(com.intellij.lang.PsiBuilder b, int l, int g) {
    if (!recursion_guard_(b, l, "expr_0")) return false;
    boolean r = true;
    while (true) {
      com.intellij.lang.PsiBuilder.Marker m = enter_section_(b, l, _LEFT_, null);
      if (g < 1 && consumeTokenSmart(b, TERNARY)) {
        r = report_error_(b, expr(b, l, 1));
        r = ternaryExpr_1(b, l + 1) && r;
        exit_section_(b, l, m, TERNARY_EXPR, r, true, null);
      }
      else if (g < 2 && consumeTokenSmart(b, L_OR)) {
        r = expr(b, l, 2);
        exit_section_(b, l, m, L_OR_EXPR, r, true, null);
      }
      else if (g < 3 && consumeTokenSmart(b, L_AND)) {
        r = expr(b, l, 3);
        exit_section_(b, l, m, L_AND_EXPR, r, true, null);
      }
      else if (g < 4 && consumeTokenSmart(b, OR)) {
        r = expr(b, l, 4);
        exit_section_(b, l, m, B_OR_EXPR, r, true, null);
      }
      else if (g < 5 && consumeTokenSmart(b, XOR)) {
        r = expr(b, l, 5);
        exit_section_(b, l, m, XOR_EXPR, r, true, null);
      }
      else if (g < 6 && consumeTokenSmart(b, AND)) {
        r = expr(b, l, 6);
        exit_section_(b, l, m, B_AND_EXPR, r, true, null);
      }
      else if (g < 7 && consumeTokenSmart(b, EQ)) {
        r = expr(b, l, 7);
        exit_section_(b, l, m, EQ_EXPR, r, true, null);
      }
      else if (g < 7 && consumeTokenSmart(b, NEQ)) {
        r = expr(b, l, 7);
        exit_section_(b, l, m, NEQ_EXPR, r, true, null);
      }
      else if (g < 8 && consumeTokenSmart(b, LES)) {
        r = expr(b, l, 8);
        exit_section_(b, l, m, LES_EXPR, r, true, null);
      }
      else if (g < 8 && consumeTokenSmart(b, GRT)) {
        r = expr(b, l, 8);
        exit_section_(b, l, m, GRT_EXPR, r, true, null);
      }
      else if (g < 8 && consumeTokenSmart(b, LEQ)) {
        r = expr(b, l, 8);
        exit_section_(b, l, m, LEQ_EXPR, r, true, null);
      }
      else if (g < 8 && consumeTokenSmart(b, GEQ)) {
        r = expr(b, l, 8);
        exit_section_(b, l, m, GEQ_EXPR, r, true, null);
      }
      else if (g < 9 && consumeTokenSmart(b, LSH)) {
        r = expr(b, l, 9);
        exit_section_(b, l, m, LSH_EXPR, r, true, null);
      }
      else if (g < 9 && consumeTokenSmart(b, RSH)) {
        r = expr(b, l, 9);
        exit_section_(b, l, m, RSH_EXPR, r, true, null);
      }
      else if (g < 10 && consumeTokenSmart(b, ADD)) {
        r = expr(b, l, 10);
        exit_section_(b, l, m, ADD_EXPR, r, true, null);
      }
      else if (g < 10 && consumeTokenSmart(b, SUB)) {
        r = expr(b, l, 10);
        exit_section_(b, l, m, SUB_EXPR, r, true, null);
      }
      else if (g < 11 && consumeTokenSmart(b, MUL)) {
        r = expr(b, l, 11);
        exit_section_(b, l, m, MUL_EXPR, r, true, null);
      }
      else if (g < 11 && consumeTokenSmart(b, DIV)) {
        r = expr(b, l, 11);
        exit_section_(b, l, m, DIV_EXPR, r, true, null);
      }
      else if (g < 11 && consumeTokenSmart(b, MOD)) {
        r = expr(b, l, 11);
        exit_section_(b, l, m, MOD_EXPR, r, true, null);
      }
      else {
        exit_section_(b, l, m, null, false, false, null);
        break;
      }
    }
    return r;
  }

  // ppMacro
  public static boolean ppMacroExpr(com.intellij.lang.PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ppMacroExpr")) return false;
    boolean r;
    com.intellij.lang.PsiBuilder.Marker m = enter_section_(b, l, _NONE_, PP_MACRO_EXPR, "<pp macro expr>");
    r = ppMacro(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  // COLON expr
  private static boolean ternaryExpr_1(com.intellij.lang.PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ternaryExpr_1")) return false;
    boolean r;
    com.intellij.lang.PsiBuilder.Marker m = enter_section_(b);
    r = consumeToken(b, COLON);
    r = r && expr(b, l + 1, -1);
    exit_section_(b, m, null, r);
    return r;
  }

  public static boolean negExpr(com.intellij.lang.PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "negExpr")) return false;
    if (!nextTokenIsSmart(b, SUB)) return false;
    boolean r, p;
    com.intellij.lang.PsiBuilder.Marker m = enter_section_(b, l, _NONE_, null);
    r = consumeTokenSmart(b, SUB);
    p = r;
    r = p && expr(b, l, 12);
    exit_section_(b, l, m, NEG_EXPR, r, p, null);
    return r || p;
  }

  public static boolean lNotExpr(com.intellij.lang.PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "lNotExpr")) return false;
    if (!nextTokenIsSmart(b, L_NOT)) return false;
    boolean r, p;
    com.intellij.lang.PsiBuilder.Marker m = enter_section_(b, l, _NONE_, null);
    r = consumeTokenSmart(b, L_NOT);
    p = r;
    r = p && expr(b, l, 12);
    exit_section_(b, l, m, L_NOT_EXPR, r, p, null);
    return r || p;
  }

  public static boolean bNotExpr(com.intellij.lang.PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "bNotExpr")) return false;
    if (!nextTokenIsSmart(b, NOT)) return false;
    boolean r, p;
    com.intellij.lang.PsiBuilder.Marker m = enter_section_(b, l, _NONE_, null);
    r = consumeTokenSmart(b, NOT);
    p = r;
    r = p && expr(b, l, 12);
    exit_section_(b, l, m, B_NOT_EXPR, r, p, null);
    return r || p;
  }

  // int
  public static boolean intExpr(com.intellij.lang.PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "intExpr")) return false;
    if (!nextTokenIsSmart(b, INT_LITERAL)) return false;
    boolean r;
    com.intellij.lang.PsiBuilder.Marker m = enter_section_(b);
    r = int_$(b, l + 1);
    exit_section_(b, m, INT_EXPR, r);
    return r;
  }

  // char
  public static boolean charExpr(com.intellij.lang.PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "charExpr")) return false;
    if (!nextTokenIsSmart(b, CHAR_LITERAL)) return false;
    boolean r;
    com.intellij.lang.PsiBuilder.Marker m = enter_section_(b);
    r = char_$(b, l + 1);
    exit_section_(b, m, CHAR_EXPR, r);
    return r;
  }

  public static boolean parenExpr(com.intellij.lang.PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "parenExpr")) return false;
    if (!nextTokenIsSmart(b, LPAREN)) return false;
    boolean r, p;
    com.intellij.lang.PsiBuilder.Marker m = enter_section_(b, l, _NONE_, null);
    r = consumeTokenSmart(b, LPAREN);
    p = r;
    r = p && expr(b, l, -1);
    r = p && report_error_(b, consumeToken(b, RPAREN)) && r;
    exit_section_(b, l, m, PAREN_EXPR, r, p, null);
    return r || p;
  }

  static final Parser DELETE_NODE_parser_ = (b, l) -> consumeToken(b, DELETE_NODE);
  static final Parser DELETE_PROP_parser_ = (b, l) -> consumeToken(b, DELETE_PROP);
  static final Parser NAME_parser_ = (b, l) -> consumeToken(b, NAME);
  static final Parser OMIT_NODE_parser_ = (b, l) -> consumeToken(b, OMIT_NODE);
  static final Parser PLUGIN_parser_ = (b, l) -> consumeToken(b, PLUGIN);
  static final Parser V1_parser_ = (b, l) -> consumeToken(b, V1);
}
