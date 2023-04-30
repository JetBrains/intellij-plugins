// This is a generated file. Not intended for manual editing.
package com.intellij.dts.lang.parser;

import static com.intellij.dts.lang.psi.DtsTypes.*;
import static com.intellij.dts.lang.parser.DtsParserUtil.*;

@java.lang.SuppressWarnings({"SimplifiableIfStatement", "UnusedAssignment"})
public class DtsParser implements com.intellij.lang.PsiParser, com.intellij.lang.LightPsiParser {

  public com.intellij.lang.ASTNode parse(com.intellij.psi.tree.IElementType root_, com.intellij.lang.PsiBuilder builder_) {
    parseLight(root_, builder_);
    return builder_.getTreeBuilt();
  }

  public void parseLight(com.intellij.psi.tree.IElementType root_, com.intellij.lang.PsiBuilder builder_) {
    boolean result_;
    builder_ = adapt_builder_(root_, builder_, this, EXTENDS_SETS_);
    com.intellij.lang.PsiBuilder.Marker marker_ = enter_section_(builder_, 0, _COLLAPSE_, null);
    result_ = parse_root_(root_, builder_);
    exit_section_(builder_, 0, marker_, root_, result_, true, TRUE_CONDITION);
  }

  protected boolean parse_root_(com.intellij.psi.tree.IElementType root_, com.intellij.lang.PsiBuilder builder_) {
    return parse_root_(root_, builder_, 0);
  }

  static boolean parse_root_(com.intellij.psi.tree.IElementType root_, com.intellij.lang.PsiBuilder builder_, int level_) {
    return dtsFile(builder_, level_ + 1);
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
  // LBRAC byteArrayContent RBRAC
  public static boolean byteArray(com.intellij.lang.PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "byteArray")) return false;
    if (!nextTokenIs(builder_, LBRAC)) return false;
    boolean result_, pinned_;
    com.intellij.lang.PsiBuilder.Marker marker_ = enter_section_(builder_, level_, _NONE_, BYTE_ARRAY, null);
    result_ = consumeToken(builder_, LBRAC);
    pinned_ = result_; // pin = 1
    result_ = result_ && report_error_(builder_, byteArrayContent(builder_, level_ + 1));
    result_ = pinned_ && consumeToken(builder_, RBRAC) && result_;
    exit_section_(builder_, level_, marker_, result_, pinned_, null);
    return result_ || pinned_;
  }

  /* ********************************************************** */
  // (LABEL* (BYTE | ppMacro) LABEL*)*
  static boolean byteArrayContent(com.intellij.lang.PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "byteArrayContent")) return false;
    com.intellij.lang.PsiBuilder.Marker marker_ = enter_section_(builder_, level_, _NONE_);
    while (true) {
      int pos_ = current_position_(builder_);
      if (!byteArrayContent_0(builder_, level_ + 1)) break;
      if (!empty_element_parsed_guard_(builder_, "byteArrayContent", pos_)) break;
    }
    exit_section_(builder_, level_, marker_, true, false, DtsParser::byteArrayRecover);
    return true;
  }

  // LABEL* (BYTE | ppMacro) LABEL*
  private static boolean byteArrayContent_0(com.intellij.lang.PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "byteArrayContent_0")) return false;
    boolean result_;
    com.intellij.lang.PsiBuilder.Marker marker_ = enter_section_(builder_);
    result_ = byteArrayContent_0_0(builder_, level_ + 1);
    result_ = result_ && byteArrayContent_0_1(builder_, level_ + 1);
    result_ = result_ && byteArrayContent_0_2(builder_, level_ + 1);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  // LABEL*
  private static boolean byteArrayContent_0_0(com.intellij.lang.PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "byteArrayContent_0_0")) return false;
    while (true) {
      int pos_ = current_position_(builder_);
      if (!consumeToken(builder_, LABEL)) break;
      if (!empty_element_parsed_guard_(builder_, "byteArrayContent_0_0", pos_)) break;
    }
    return true;
  }

  // BYTE | ppMacro
  private static boolean byteArrayContent_0_1(com.intellij.lang.PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "byteArrayContent_0_1")) return false;
    boolean result_;
    result_ = consumeToken(builder_, BYTE);
    if (!result_) result_ = ppMacro(builder_, level_ + 1);
    return result_;
  }

  // LABEL*
  private static boolean byteArrayContent_0_2(com.intellij.lang.PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "byteArrayContent_0_2")) return false;
    while (true) {
      int pos_ = current_position_(builder_);
      if (!consumeToken(builder_, LABEL)) break;
      if (!empty_element_parsed_guard_(builder_, "byteArrayContent_0_2", pos_)) break;
    }
    return true;
  }

  /* ********************************************************** */
  // !(SEMICOLON | RBRACE | COMMA | RBRAC | RPAREN)
  static boolean byteArrayRecover(com.intellij.lang.PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "byteArrayRecover")) return false;
    boolean result_;
    com.intellij.lang.PsiBuilder.Marker marker_ = enter_section_(builder_, level_, _NOT_);
    result_ = !byteArrayRecover_0(builder_, level_ + 1);
    exit_section_(builder_, level_, marker_, result_, false, null);
    return result_;
  }

  // SEMICOLON | RBRACE | COMMA | RBRAC | RPAREN
  private static boolean byteArrayRecover_0(com.intellij.lang.PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "byteArrayRecover_0")) return false;
    boolean result_;
    result_ = consumeToken(builder_, SEMICOLON);
    if (!result_) result_ = consumeToken(builder_, RBRACE);
    if (!result_) result_ = consumeToken(builder_, COMMA);
    if (!result_) result_ = consumeToken(builder_, RBRAC);
    if (!result_) result_ = consumeToken(builder_, RPAREN);
    return result_;
  }

  /* ********************************************************** */
  // INT | char | LPAREN expr RPAREN | ppMacro
  public static boolean cell(com.intellij.lang.PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "cell")) return false;
    boolean result_;
    com.intellij.lang.PsiBuilder.Marker marker_ = enter_section_(builder_, level_, _NONE_, CELL, "<cell>");
    result_ = consumeToken(builder_, INT);
    if (!result_) result_ = char_$(builder_, level_ + 1);
    if (!result_) result_ = cell_2(builder_, level_ + 1);
    if (!result_) result_ = ppMacro(builder_, level_ + 1);
    exit_section_(builder_, level_, marker_, result_, false, null);
    return result_;
  }

  // LPAREN expr RPAREN
  private static boolean cell_2(com.intellij.lang.PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "cell_2")) return false;
    boolean result_;
    com.intellij.lang.PsiBuilder.Marker marker_ = enter_section_(builder_);
    result_ = consumeToken(builder_, LPAREN);
    result_ = result_ && expr(builder_, level_ + 1, -1);
    result_ = result_ && consumeToken(builder_, RPAREN);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  /* ********************************************************** */
  // cellArrayBits? LANGL cellArrayContent RANGL
  public static boolean cellArray(com.intellij.lang.PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "cellArray")) return false;
    if (!nextTokenIs(builder_, "<cell array>", BITS, LANGL)) return false;
    boolean result_, pinned_;
    com.intellij.lang.PsiBuilder.Marker marker_ = enter_section_(builder_, level_, _NONE_, CELL_ARRAY, "<cell array>");
    result_ = cellArray_0(builder_, level_ + 1);
    result_ = result_ && consumeToken(builder_, LANGL);
    pinned_ = result_; // pin = 2
    result_ = result_ && report_error_(builder_, cellArrayContent(builder_, level_ + 1));
    result_ = pinned_ && consumeToken(builder_, RANGL) && result_;
    exit_section_(builder_, level_, marker_, result_, pinned_, null);
    return result_ || pinned_;
  }

  // cellArrayBits?
  private static boolean cellArray_0(com.intellij.lang.PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "cellArray_0")) return false;
    cellArrayBits(builder_, level_ + 1);
    return true;
  }

  /* ********************************************************** */
  // BITS (INT | ppMacro)
  public static boolean cellArrayBits(com.intellij.lang.PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "cellArrayBits")) return false;
    boolean result_, pinned_;
    com.intellij.lang.PsiBuilder.Marker marker_ = enter_section_(builder_, level_, _NONE_, CELL_ARRAY_BITS, "<cell array bits>");
    result_ = consumeToken(builder_, BITS);
    pinned_ = result_; // pin = 1
    result_ = result_ && cellArrayBits_1(builder_, level_ + 1);
    exit_section_(builder_, level_, marker_, result_, pinned_, DtsParser::cellArrayBitsRecover);
    return result_ || pinned_;
  }

  // INT | ppMacro
  private static boolean cellArrayBits_1(com.intellij.lang.PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "cellArrayBits_1")) return false;
    boolean result_;
    result_ = consumeToken(builder_, INT);
    if (!result_) result_ = ppMacro(builder_, level_ + 1);
    return result_;
  }

  /* ********************************************************** */
  // !(SEMICOLON | RBRACE | LANGL)
  static boolean cellArrayBitsRecover(com.intellij.lang.PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "cellArrayBitsRecover")) return false;
    boolean result_;
    com.intellij.lang.PsiBuilder.Marker marker_ = enter_section_(builder_, level_, _NOT_);
    result_ = !cellArrayBitsRecover_0(builder_, level_ + 1);
    exit_section_(builder_, level_, marker_, result_, false, null);
    return result_;
  }

  // SEMICOLON | RBRACE | LANGL
  private static boolean cellArrayBitsRecover_0(com.intellij.lang.PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "cellArrayBitsRecover_0")) return false;
    boolean result_;
    result_ = consumeToken(builder_, SEMICOLON);
    if (!result_) result_ = consumeToken(builder_, RBRACE);
    if (!result_) result_ = consumeToken(builder_, LANGL);
    return result_;
  }

  /* ********************************************************** */
  // (LABEL* (pHandle | cell) LABEL*)*
  static boolean cellArrayContent(com.intellij.lang.PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "cellArrayContent")) return false;
    com.intellij.lang.PsiBuilder.Marker marker_ = enter_section_(builder_, level_, _NONE_);
    while (true) {
      int pos_ = current_position_(builder_);
      if (!cellArrayContent_0(builder_, level_ + 1)) break;
      if (!empty_element_parsed_guard_(builder_, "cellArrayContent", pos_)) break;
    }
    exit_section_(builder_, level_, marker_, true, false, DtsParser::cellArrayRecover);
    return true;
  }

  // LABEL* (pHandle | cell) LABEL*
  private static boolean cellArrayContent_0(com.intellij.lang.PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "cellArrayContent_0")) return false;
    boolean result_;
    com.intellij.lang.PsiBuilder.Marker marker_ = enter_section_(builder_);
    result_ = cellArrayContent_0_0(builder_, level_ + 1);
    result_ = result_ && cellArrayContent_0_1(builder_, level_ + 1);
    result_ = result_ && cellArrayContent_0_2(builder_, level_ + 1);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  // LABEL*
  private static boolean cellArrayContent_0_0(com.intellij.lang.PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "cellArrayContent_0_0")) return false;
    while (true) {
      int pos_ = current_position_(builder_);
      if (!consumeToken(builder_, LABEL)) break;
      if (!empty_element_parsed_guard_(builder_, "cellArrayContent_0_0", pos_)) break;
    }
    return true;
  }

  // pHandle | cell
  private static boolean cellArrayContent_0_1(com.intellij.lang.PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "cellArrayContent_0_1")) return false;
    boolean result_;
    result_ = pHandle(builder_, level_ + 1);
    if (!result_) result_ = cell(builder_, level_ + 1);
    return result_;
  }

  // LABEL*
  private static boolean cellArrayContent_0_2(com.intellij.lang.PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "cellArrayContent_0_2")) return false;
    while (true) {
      int pos_ = current_position_(builder_);
      if (!consumeToken(builder_, LABEL)) break;
      if (!empty_element_parsed_guard_(builder_, "cellArrayContent_0_2", pos_)) break;
    }
    return true;
  }

  /* ********************************************************** */
  // !(SEMICOLON | RBRACE | COMMA | RANGL | RPAREN)
  static boolean cellArrayRecover(com.intellij.lang.PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "cellArrayRecover")) return false;
    boolean result_;
    com.intellij.lang.PsiBuilder.Marker marker_ = enter_section_(builder_, level_, _NOT_);
    result_ = !cellArrayRecover_0(builder_, level_ + 1);
    exit_section_(builder_, level_, marker_, result_, false, null);
    return result_;
  }

  // SEMICOLON | RBRACE | COMMA | RANGL | RPAREN
  private static boolean cellArrayRecover_0(com.intellij.lang.PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "cellArrayRecover_0")) return false;
    boolean result_;
    result_ = consumeToken(builder_, SEMICOLON);
    if (!result_) result_ = consumeToken(builder_, RBRACE);
    if (!result_) result_ = consumeToken(builder_, COMMA);
    if (!result_) result_ = consumeToken(builder_, RANGL);
    if (!result_) result_ = consumeToken(builder_, RPAREN);
    return result_;
  }

  /* ********************************************************** */
  // SQUOTE CHAR_VALUE* SQUOTE
  public static boolean char_$(com.intellij.lang.PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "char_$")) return false;
    if (!nextTokenIs(builder_, SQUOTE)) return false;
    boolean result_, pinned_;
    com.intellij.lang.PsiBuilder.Marker marker_ = enter_section_(builder_, level_, _NONE_, CHAR, null);
    result_ = consumeToken(builder_, SQUOTE);
    pinned_ = result_; // pin = 1
    result_ = result_ && report_error_(builder_, char_1(builder_, level_ + 1));
    result_ = pinned_ && consumeToken(builder_, SQUOTE) && result_;
    exit_section_(builder_, level_, marker_, result_, pinned_, null);
    return result_ || pinned_;
  }

  // CHAR_VALUE*
  private static boolean char_1(com.intellij.lang.PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "char_1")) return false;
    while (true) {
      int pos_ = current_position_(builder_);
      if (!consumeToken(builder_, CHAR_VALUE)) break;
      if (!empty_element_parsed_guard_(builder_, "char_1", pos_)) break;
    }
    return true;
  }

  /* ********************************************************** */
  static Parser compilerDirective_$(Parser directive, Parser arg) {
    return (builder_, level_) -> compilerDirective(builder_, level_ + 1, directive, arg);
  }

  // <<directive>> <<arg>>
  public static boolean compilerDirective(com.intellij.lang.PsiBuilder builder_, int level_, Parser directive, Parser arg) {
    if (!recursion_guard_(builder_, level_, "compilerDirective")) return false;
    boolean result_, pinned_;
    com.intellij.lang.PsiBuilder.Marker marker_ = enter_section_(builder_, level_, _NONE_, COMPILER_DIRECTIVE, null);
    result_ = directive.parse(builder_, level_);
    pinned_ = result_; // pin = 1
    result_ = result_ && arg.parse(builder_, level_);
    exit_section_(builder_, level_, marker_, result_, pinned_, DtsParser::compilerDirectiveRecover);
    return result_ || pinned_;
  }

  /* ********************************************************** */
  // <<entry <<compilerDirective <<directive>> <<arg>>>>>>
  static boolean compilerDirectiveEntry(com.intellij.lang.PsiBuilder builder_, int level_, Parser directive, Parser arg) {
    return entry(builder_, level_ + 1, compilerDirective_$(directive, arg));
  }

  /* ********************************************************** */
  // !(SEMICOLON)
  static boolean compilerDirectiveRecover(com.intellij.lang.PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "compilerDirectiveRecover")) return false;
    boolean result_;
    com.intellij.lang.PsiBuilder.Marker marker_ = enter_section_(builder_, level_, _NOT_);
    result_ = !consumeToken(builder_, SEMICOLON);
    exit_section_(builder_, level_, marker_, result_, false, null);
    return result_;
  }

  /* ********************************************************** */
  // (
  //     <<entry rootNode>> |
  //     <<compilerDirectiveEntry V1 ()>> |
  //     <<compilerDirectiveEntry PLUGIN ()>> |
  //     <<compilerDirectiveEntry (LABEL* MEMRESERVE) (cell cell)>> |
  //     <<compilerDirectiveEntry OMIT_NODE pHandle>> |
  //     <<compilerDirectiveEntry DELETE_NODE pHandle>> |
  //     invalidEntry
  // )*
  public static boolean content(com.intellij.lang.PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "content")) return false;
    com.intellij.lang.PsiBuilder.Marker marker_ = enter_section_(builder_, level_, _NONE_, CONTENT, "<content>");
    while (true) {
      int pos_ = current_position_(builder_);
      if (!content_0(builder_, level_ + 1)) break;
      if (!empty_element_parsed_guard_(builder_, "content", pos_)) break;
    }
    exit_section_(builder_, level_, marker_, true, false, null);
    return true;
  }

  // <<entry rootNode>> |
  //     <<compilerDirectiveEntry V1 ()>> |
  //     <<compilerDirectiveEntry PLUGIN ()>> |
  //     <<compilerDirectiveEntry (LABEL* MEMRESERVE) (cell cell)>> |
  //     <<compilerDirectiveEntry OMIT_NODE pHandle>> |
  //     <<compilerDirectiveEntry DELETE_NODE pHandle>> |
  //     invalidEntry
  private static boolean content_0(com.intellij.lang.PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "content_0")) return false;
    boolean result_;
    com.intellij.lang.PsiBuilder.Marker marker_ = enter_section_(builder_);
    result_ = entry(builder_, level_ + 1, DtsParser::rootNode);
    if (!result_) result_ = compilerDirectiveEntry(builder_, level_ + 1, V1_parser_, DtsParser::content_0_1_1);
    if (!result_) result_ = compilerDirectiveEntry(builder_, level_ + 1, PLUGIN_parser_, DtsParser::content_0_2_1);
    if (!result_) result_ = compilerDirectiveEntry(builder_, level_ + 1, DtsParser::content_0_3_0, DtsParser::content_0_3_1);
    if (!result_) result_ = compilerDirectiveEntry(builder_, level_ + 1, OMIT_NODE_parser_, DtsParser::pHandle);
    if (!result_) result_ = compilerDirectiveEntry(builder_, level_ + 1, DELETE_NODE_parser_, DtsParser::pHandle);
    if (!result_) result_ = parseInvalidEntry(builder_, level_ + 1);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  // ()
  private static boolean content_0_1_1(com.intellij.lang.PsiBuilder builder_, int level_) {
    return true;
  }

  // ()
  private static boolean content_0_2_1(com.intellij.lang.PsiBuilder builder_, int level_) {
    return true;
  }

  // LABEL* MEMRESERVE
  private static boolean content_0_3_0(com.intellij.lang.PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "content_0_3_0")) return false;
    boolean result_;
    com.intellij.lang.PsiBuilder.Marker marker_ = enter_section_(builder_);
    result_ = content_0_3_0_0(builder_, level_ + 1);
    result_ = result_ && consumeToken(builder_, MEMRESERVE);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  // LABEL*
  private static boolean content_0_3_0_0(com.intellij.lang.PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "content_0_3_0_0")) return false;
    while (true) {
      int pos_ = current_position_(builder_);
      if (!consumeToken(builder_, LABEL)) break;
      if (!empty_element_parsed_guard_(builder_, "content_0_3_0_0", pos_)) break;
    }
    return true;
  }

  // cell cell
  private static boolean content_0_3_1(com.intellij.lang.PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "content_0_3_1")) return false;
    boolean result_;
    com.intellij.lang.PsiBuilder.Marker marker_ = enter_section_(builder_);
    result_ = cell(builder_, level_ + 1);
    result_ = result_ && cell(builder_, level_ + 1);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  /* ********************************************************** */
  // nodeContent | content
  static boolean dtsFile(com.intellij.lang.PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "dtsFile")) return false;
    boolean result_;
    result_ = nodeContent(builder_, level_ + 1);
    if (!result_) result_ = content(builder_, level_ + 1);
    return result_;
  }

  /* ********************************************************** */
  // <<arg>> SEMICOLON
  public static boolean entry(com.intellij.lang.PsiBuilder builder_, int level_, Parser arg) {
    if (!recursion_guard_(builder_, level_, "entry")) return false;
    boolean result_, pinned_;
    com.intellij.lang.PsiBuilder.Marker marker_ = enter_section_(builder_, level_, _NONE_, ENTRY, null);
    result_ = arg.parse(builder_, level_);
    pinned_ = result_; // pin = 1
    result_ = result_ && consumeToken(builder_, SEMICOLON);
    exit_section_(builder_, level_, marker_, result_, pinned_, null);
    return result_ || pinned_;
  }

  /* ********************************************************** */
  // (
  //     <<compilerDirectiveEntry DELETE_PROP NAME>> |
  //     // only pin on delete and valid name
  //     <<compilerDirectiveEntry (DELETE_NODE NAME) ()>> |
  //     <<entry subNode>> |
  //     <<entry property>>
  // )+
  public static boolean nodeContent(com.intellij.lang.PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "nodeContent")) return false;
    boolean result_;
    com.intellij.lang.PsiBuilder.Marker marker_ = enter_section_(builder_, level_, _NONE_, NODE_CONTENT, "<node content>");
    result_ = nodeContent_0(builder_, level_ + 1);
    while (result_) {
      int pos_ = current_position_(builder_);
      if (!nodeContent_0(builder_, level_ + 1)) break;
      if (!empty_element_parsed_guard_(builder_, "nodeContent", pos_)) break;
    }
    exit_section_(builder_, level_, marker_, result_, false, null);
    return result_;
  }

  // <<compilerDirectiveEntry DELETE_PROP NAME>> |
  //     // only pin on delete and valid name
  //     <<compilerDirectiveEntry (DELETE_NODE NAME) ()>> |
  //     <<entry subNode>> |
  //     <<entry property>>
  private static boolean nodeContent_0(com.intellij.lang.PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "nodeContent_0")) return false;
    boolean result_;
    com.intellij.lang.PsiBuilder.Marker marker_ = enter_section_(builder_);
    result_ = compilerDirectiveEntry(builder_, level_ + 1, DELETE_PROP_parser_, NAME_parser_);
    if (!result_) result_ = compilerDirectiveEntry(builder_, level_ + 1, DtsParser::nodeContent_0_1_0, DtsParser::nodeContent_0_1_1);
    if (!result_) result_ = entry(builder_, level_ + 1, DtsParser::subNode);
    if (!result_) result_ = entry(builder_, level_ + 1, DtsParser::property);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  // DELETE_NODE NAME
  private static boolean nodeContent_0_1_0(com.intellij.lang.PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "nodeContent_0_1_0")) return false;
    boolean result_;
    com.intellij.lang.PsiBuilder.Marker marker_ = enter_section_(builder_);
    result_ = consumeTokens(builder_, 0, DELETE_NODE, NAME);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  // ()
  private static boolean nodeContent_0_1_1(com.intellij.lang.PsiBuilder builder_, int level_) {
    return true;
  }

  /* ********************************************************** */
  // !(SEMICOLON | NAME)
  static boolean nodeRecover(com.intellij.lang.PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "nodeRecover")) return false;
    boolean result_;
    com.intellij.lang.PsiBuilder.Marker marker_ = enter_section_(builder_, level_, _NOT_);
    result_ = !nodeRecover_0(builder_, level_ + 1);
    exit_section_(builder_, level_, marker_, result_, false, null);
    return result_;
  }

  // SEMICOLON | NAME
  private static boolean nodeRecover_0(com.intellij.lang.PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "nodeRecover_0")) return false;
    boolean result_;
    result_ = consumeToken(builder_, SEMICOLON);
    if (!result_) result_ = consumeToken(builder_, NAME);
    return result_;
  }

  /* ********************************************************** */
  // HANDLE (NAME | pHandlePath)
  public static boolean pHandle(com.intellij.lang.PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "pHandle")) return false;
    if (!nextTokenIs(builder_, HANDLE)) return false;
    boolean result_, pinned_;
    com.intellij.lang.PsiBuilder.Marker marker_ = enter_section_(builder_, level_, _NONE_, P_HANDLE, null);
    result_ = consumeToken(builder_, HANDLE);
    pinned_ = result_; // pin = 1
    result_ = result_ && pHandle_1(builder_, level_ + 1);
    exit_section_(builder_, level_, marker_, result_, pinned_, null);
    return result_ || pinned_;
  }

  // NAME | pHandlePath
  private static boolean pHandle_1(com.intellij.lang.PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "pHandle_1")) return false;
    boolean result_;
    result_ = consumeToken(builder_, NAME);
    if (!result_) result_ = pHandlePath(builder_, level_ + 1);
    return result_;
  }

  /* ********************************************************** */
  // LBRACE path RBRACE
  static boolean pHandlePath(com.intellij.lang.PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "pHandlePath")) return false;
    if (!nextTokenIs(builder_, LBRACE)) return false;
    boolean result_, pinned_;
    com.intellij.lang.PsiBuilder.Marker marker_ = enter_section_(builder_, level_, _NONE_);
    result_ = consumeToken(builder_, LBRACE);
    pinned_ = result_; // pin = 1
    result_ = result_ && report_error_(builder_, path(builder_, level_ + 1));
    result_ = pinned_ && consumeToken(builder_, RBRACE) && result_;
    exit_section_(builder_, level_, marker_, result_, pinned_, null);
    return result_ || pinned_;
  }

  /* ********************************************************** */
  // PATH
  static boolean path(com.intellij.lang.PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "path")) return false;
    boolean result_;
    com.intellij.lang.PsiBuilder.Marker marker_ = enter_section_(builder_, level_, _NONE_);
    result_ = consumeToken(builder_, PATH);
    exit_section_(builder_, level_, marker_, result_, false, DtsParser::pathRecover);
    return result_;
  }

  /* ********************************************************** */
  // !(SEMICOLON | RANGL | RBRACE)
  static boolean pathRecover(com.intellij.lang.PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "pathRecover")) return false;
    boolean result_;
    com.intellij.lang.PsiBuilder.Marker marker_ = enter_section_(builder_, level_, _NOT_);
    result_ = !pathRecover_0(builder_, level_ + 1);
    exit_section_(builder_, level_, marker_, result_, false, null);
    return result_;
  }

  // SEMICOLON | RANGL | RBRACE
  private static boolean pathRecover_0(com.intellij.lang.PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "pathRecover_0")) return false;
    boolean result_;
    result_ = consumeToken(builder_, SEMICOLON);
    if (!result_) result_ = consumeToken(builder_, RANGL);
    if (!result_) result_ = consumeToken(builder_, RBRACE);
    return result_;
  }

  /* ********************************************************** */
  // parserPpMacro
  public static boolean ppMacro(com.intellij.lang.PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "ppMacro")) return false;
    boolean result_;
    com.intellij.lang.PsiBuilder.Marker marker_ = enter_section_(builder_, level_, _NONE_, PP_MACRO, "<pp macro>");
    result_ = parsePpMacro(builder_, level_ + 1);
    exit_section_(builder_, level_, marker_, result_, false, null);
    return result_;
  }

  /* ********************************************************** */
  // LABEL* NAME propertyAssignment?
  public static boolean property(com.intellij.lang.PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "property")) return false;
    if (!nextTokenIs(builder_, "<property>", LABEL, NAME)) return false;
    boolean result_;
    com.intellij.lang.PsiBuilder.Marker marker_ = enter_section_(builder_, level_, _NONE_, PROPERTY, "<property>");
    result_ = property_0(builder_, level_ + 1);
    result_ = result_ && consumeToken(builder_, NAME);
    result_ = result_ && property_2(builder_, level_ + 1);
    exit_section_(builder_, level_, marker_, result_, false, null);
    return result_;
  }

  // LABEL*
  private static boolean property_0(com.intellij.lang.PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "property_0")) return false;
    while (true) {
      int pos_ = current_position_(builder_);
      if (!consumeToken(builder_, LABEL)) break;
      if (!empty_element_parsed_guard_(builder_, "property_0", pos_)) break;
    }
    return true;
  }

  // propertyAssignment?
  private static boolean property_2(com.intellij.lang.PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "property_2")) return false;
    propertyAssignment(builder_, level_ + 1);
    return true;
  }

  /* ********************************************************** */
  // ASSIGN LABEL* propertyValue LABEL* (COMMA LABEL* propertyValue LABEL*)*
  static boolean propertyAssignment(com.intellij.lang.PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "propertyAssignment")) return false;
    boolean result_, pinned_;
    com.intellij.lang.PsiBuilder.Marker marker_ = enter_section_(builder_, level_, _NONE_);
    result_ = consumeToken(builder_, ASSIGN);
    pinned_ = result_; // pin = 1
    result_ = result_ && report_error_(builder_, propertyAssignment_1(builder_, level_ + 1));
    result_ = pinned_ && report_error_(builder_, propertyValue(builder_, level_ + 1)) && result_;
    result_ = pinned_ && report_error_(builder_, propertyAssignment_3(builder_, level_ + 1)) && result_;
    result_ = pinned_ && propertyAssignment_4(builder_, level_ + 1) && result_;
    exit_section_(builder_, level_, marker_, result_, pinned_, DtsParser::propertyRecover);
    return result_ || pinned_;
  }

  // LABEL*
  private static boolean propertyAssignment_1(com.intellij.lang.PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "propertyAssignment_1")) return false;
    while (true) {
      int pos_ = current_position_(builder_);
      if (!consumeToken(builder_, LABEL)) break;
      if (!empty_element_parsed_guard_(builder_, "propertyAssignment_1", pos_)) break;
    }
    return true;
  }

  // LABEL*
  private static boolean propertyAssignment_3(com.intellij.lang.PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "propertyAssignment_3")) return false;
    while (true) {
      int pos_ = current_position_(builder_);
      if (!consumeToken(builder_, LABEL)) break;
      if (!empty_element_parsed_guard_(builder_, "propertyAssignment_3", pos_)) break;
    }
    return true;
  }

  // (COMMA LABEL* propertyValue LABEL*)*
  private static boolean propertyAssignment_4(com.intellij.lang.PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "propertyAssignment_4")) return false;
    while (true) {
      int pos_ = current_position_(builder_);
      if (!propertyAssignment_4_0(builder_, level_ + 1)) break;
      if (!empty_element_parsed_guard_(builder_, "propertyAssignment_4", pos_)) break;
    }
    return true;
  }

  // COMMA LABEL* propertyValue LABEL*
  private static boolean propertyAssignment_4_0(com.intellij.lang.PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "propertyAssignment_4_0")) return false;
    boolean result_;
    com.intellij.lang.PsiBuilder.Marker marker_ = enter_section_(builder_);
    result_ = consumeToken(builder_, COMMA);
    result_ = result_ && propertyAssignment_4_0_1(builder_, level_ + 1);
    result_ = result_ && propertyValue(builder_, level_ + 1);
    result_ = result_ && propertyAssignment_4_0_3(builder_, level_ + 1);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  // LABEL*
  private static boolean propertyAssignment_4_0_1(com.intellij.lang.PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "propertyAssignment_4_0_1")) return false;
    while (true) {
      int pos_ = current_position_(builder_);
      if (!consumeToken(builder_, LABEL)) break;
      if (!empty_element_parsed_guard_(builder_, "propertyAssignment_4_0_1", pos_)) break;
    }
    return true;
  }

  // LABEL*
  private static boolean propertyAssignment_4_0_3(com.intellij.lang.PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "propertyAssignment_4_0_3")) return false;
    while (true) {
      int pos_ = current_position_(builder_);
      if (!consumeToken(builder_, LABEL)) break;
      if (!empty_element_parsed_guard_(builder_, "propertyAssignment_4_0_3", pos_)) break;
    }
    return true;
  }

  /* ********************************************************** */
  // !(SEMICOLON | RBRACE | NAME)
  static boolean propertyRecover(com.intellij.lang.PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "propertyRecover")) return false;
    boolean result_;
    com.intellij.lang.PsiBuilder.Marker marker_ = enter_section_(builder_, level_, _NOT_);
    result_ = !propertyRecover_0(builder_, level_ + 1);
    exit_section_(builder_, level_, marker_, result_, false, null);
    return result_;
  }

  // SEMICOLON | RBRACE | NAME
  private static boolean propertyRecover_0(com.intellij.lang.PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "propertyRecover_0")) return false;
    boolean result_;
    result_ = consumeToken(builder_, SEMICOLON);
    if (!result_) result_ = consumeToken(builder_, RBRACE);
    if (!result_) result_ = consumeToken(builder_, NAME);
    return result_;
  }

  /* ********************************************************** */
  // string | pHandle | cellArray | byteArray | ppMacro
  static boolean propertyValue(com.intellij.lang.PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "propertyValue")) return false;
    boolean result_;
    result_ = string(builder_, level_ + 1);
    if (!result_) result_ = pHandle(builder_, level_ + 1);
    if (!result_) result_ = cellArray(builder_, level_ + 1);
    if (!result_) result_ = byteArray(builder_, level_ + 1);
    if (!result_) result_ = ppMacro(builder_, level_ + 1);
    return result_;
  }

  /* ********************************************************** */
  // LABEL* (SLASH | pHandle) LBRACE nodeContent? RBRACE
  public static boolean rootNode(com.intellij.lang.PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "rootNode")) return false;
    boolean result_, pinned_;
    com.intellij.lang.PsiBuilder.Marker marker_ = enter_section_(builder_, level_, _NONE_, ROOT_NODE, "<root node>");
    result_ = rootNode_0(builder_, level_ + 1);
    result_ = result_ && rootNode_1(builder_, level_ + 1);
    result_ = result_ && consumeToken(builder_, LBRACE);
    pinned_ = result_; // pin = 3
    result_ = result_ && report_error_(builder_, rootNode_3(builder_, level_ + 1));
    result_ = pinned_ && consumeToken(builder_, RBRACE) && result_;
    exit_section_(builder_, level_, marker_, result_, pinned_, DtsParser::rootNodeRecover);
    return result_ || pinned_;
  }

  // LABEL*
  private static boolean rootNode_0(com.intellij.lang.PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "rootNode_0")) return false;
    while (true) {
      int pos_ = current_position_(builder_);
      if (!consumeToken(builder_, LABEL)) break;
      if (!empty_element_parsed_guard_(builder_, "rootNode_0", pos_)) break;
    }
    return true;
  }

  // SLASH | pHandle
  private static boolean rootNode_1(com.intellij.lang.PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "rootNode_1")) return false;
    boolean result_;
    result_ = consumeToken(builder_, SLASH);
    if (!result_) result_ = pHandle(builder_, level_ + 1);
    return result_;
  }

  // nodeContent?
  private static boolean rootNode_3(com.intellij.lang.PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "rootNode_3")) return false;
    nodeContent(builder_, level_ + 1);
    return true;
  }

  /* ********************************************************** */
  // !(SEMICOLON)
  static boolean rootNodeRecover(com.intellij.lang.PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "rootNodeRecover")) return false;
    boolean result_;
    com.intellij.lang.PsiBuilder.Marker marker_ = enter_section_(builder_, level_, _NOT_);
    result_ = !consumeToken(builder_, SEMICOLON);
    exit_section_(builder_, level_, marker_, result_, false, null);
    return result_;
  }

  /* ********************************************************** */
  // DQUOTE STRING_VALUE? DQUOTE
  public static boolean string(com.intellij.lang.PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "string")) return false;
    if (!nextTokenIs(builder_, DQUOTE)) return false;
    boolean result_, pinned_;
    com.intellij.lang.PsiBuilder.Marker marker_ = enter_section_(builder_, level_, _NONE_, STRING, null);
    result_ = consumeToken(builder_, DQUOTE);
    pinned_ = result_; // pin = 1
    result_ = result_ && report_error_(builder_, string_1(builder_, level_ + 1));
    result_ = pinned_ && consumeToken(builder_, DQUOTE) && result_;
    exit_section_(builder_, level_, marker_, result_, pinned_, null);
    return result_ || pinned_;
  }

  // STRING_VALUE?
  private static boolean string_1(com.intellij.lang.PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "string_1")) return false;
    consumeToken(builder_, STRING_VALUE);
    return true;
  }

  /* ********************************************************** */
  // LABEL* OMIT_NODE? LABEL* NAME LBRACE nodeContent? RBRACE
  public static boolean subNode(com.intellij.lang.PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "subNode")) return false;
    boolean result_, pinned_;
    com.intellij.lang.PsiBuilder.Marker marker_ = enter_section_(builder_, level_, _NONE_, SUB_NODE, "<sub node>");
    result_ = subNode_0(builder_, level_ + 1);
    result_ = result_ && subNode_1(builder_, level_ + 1);
    result_ = result_ && subNode_2(builder_, level_ + 1);
    result_ = result_ && consumeTokens(builder_, 2, NAME, LBRACE);
    pinned_ = result_; // pin = 5
    result_ = result_ && report_error_(builder_, subNode_5(builder_, level_ + 1));
    result_ = pinned_ && consumeToken(builder_, RBRACE) && result_;
    exit_section_(builder_, level_, marker_, result_, pinned_, DtsParser::nodeRecover);
    return result_ || pinned_;
  }

  // LABEL*
  private static boolean subNode_0(com.intellij.lang.PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "subNode_0")) return false;
    while (true) {
      int pos_ = current_position_(builder_);
      if (!consumeToken(builder_, LABEL)) break;
      if (!empty_element_parsed_guard_(builder_, "subNode_0", pos_)) break;
    }
    return true;
  }

  // OMIT_NODE?
  private static boolean subNode_1(com.intellij.lang.PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "subNode_1")) return false;
    consumeToken(builder_, OMIT_NODE);
    return true;
  }

  // LABEL*
  private static boolean subNode_2(com.intellij.lang.PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "subNode_2")) return false;
    while (true) {
      int pos_ = current_position_(builder_);
      if (!consumeToken(builder_, LABEL)) break;
      if (!empty_element_parsed_guard_(builder_, "subNode_2", pos_)) break;
    }
    return true;
  }

  // nodeContent?
  private static boolean subNode_5(com.intellij.lang.PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "subNode_5")) return false;
    nodeContent(builder_, level_ + 1);
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
  public static boolean expr(com.intellij.lang.PsiBuilder builder_, int level_, int priority_) {
    if (!recursion_guard_(builder_, level_, "expr")) return false;
    addVariant(builder_, "<expr>");
    boolean result_, pinned_;
    com.intellij.lang.PsiBuilder.Marker marker_ = enter_section_(builder_, level_, _NONE_, "<expr>");
    result_ = ppMacroExpr(builder_, level_ + 1);
    if (!result_) result_ = negExpr(builder_, level_ + 1);
    if (!result_) result_ = lNotExpr(builder_, level_ + 1);
    if (!result_) result_ = bNotExpr(builder_, level_ + 1);
    if (!result_) result_ = intExpr(builder_, level_ + 1);
    if (!result_) result_ = charExpr(builder_, level_ + 1);
    if (!result_) result_ = parenExpr(builder_, level_ + 1);
    pinned_ = result_;
    result_ = result_ && expr_0(builder_, level_ + 1, priority_);
    exit_section_(builder_, level_, marker_, null, result_, pinned_, null);
    return result_ || pinned_;
  }

  public static boolean expr_0(com.intellij.lang.PsiBuilder builder_, int level_, int priority_) {
    if (!recursion_guard_(builder_, level_, "expr_0")) return false;
    boolean result_ = true;
    while (true) {
      com.intellij.lang.PsiBuilder.Marker marker_ = enter_section_(builder_, level_, _LEFT_, null);
      if (priority_ < 1 && consumeTokenSmart(builder_, TERNARY)) {
        result_ = report_error_(builder_, expr(builder_, level_, 1));
        result_ = ternaryExpr_1(builder_, level_ + 1) && result_;
        exit_section_(builder_, level_, marker_, TERNARY_EXPR, result_, true, null);
      }
      else if (priority_ < 2 && consumeTokenSmart(builder_, L_OR)) {
        result_ = expr(builder_, level_, 2);
        exit_section_(builder_, level_, marker_, L_OR_EXPR, result_, true, null);
      }
      else if (priority_ < 3 && consumeTokenSmart(builder_, L_AND)) {
        result_ = expr(builder_, level_, 3);
        exit_section_(builder_, level_, marker_, L_AND_EXPR, result_, true, null);
      }
      else if (priority_ < 4 && consumeTokenSmart(builder_, OR)) {
        result_ = expr(builder_, level_, 4);
        exit_section_(builder_, level_, marker_, B_OR_EXPR, result_, true, null);
      }
      else if (priority_ < 5 && consumeTokenSmart(builder_, XOR)) {
        result_ = expr(builder_, level_, 5);
        exit_section_(builder_, level_, marker_, XOR_EXPR, result_, true, null);
      }
      else if (priority_ < 6 && consumeTokenSmart(builder_, AND)) {
        result_ = expr(builder_, level_, 6);
        exit_section_(builder_, level_, marker_, B_AND_EXPR, result_, true, null);
      }
      else if (priority_ < 7 && consumeTokenSmart(builder_, EQ)) {
        result_ = expr(builder_, level_, 7);
        exit_section_(builder_, level_, marker_, EQ_EXPR, result_, true, null);
      }
      else if (priority_ < 7 && consumeTokenSmart(builder_, NEQ)) {
        result_ = expr(builder_, level_, 7);
        exit_section_(builder_, level_, marker_, NEQ_EXPR, result_, true, null);
      }
      else if (priority_ < 8 && consumeTokenSmart(builder_, LES)) {
        result_ = expr(builder_, level_, 8);
        exit_section_(builder_, level_, marker_, LES_EXPR, result_, true, null);
      }
      else if (priority_ < 8 && consumeTokenSmart(builder_, GRT)) {
        result_ = expr(builder_, level_, 8);
        exit_section_(builder_, level_, marker_, GRT_EXPR, result_, true, null);
      }
      else if (priority_ < 8 && consumeTokenSmart(builder_, LEQ)) {
        result_ = expr(builder_, level_, 8);
        exit_section_(builder_, level_, marker_, LEQ_EXPR, result_, true, null);
      }
      else if (priority_ < 8 && consumeTokenSmart(builder_, GEQ)) {
        result_ = expr(builder_, level_, 8);
        exit_section_(builder_, level_, marker_, GEQ_EXPR, result_, true, null);
      }
      else if (priority_ < 9 && consumeTokenSmart(builder_, LSH)) {
        result_ = expr(builder_, level_, 9);
        exit_section_(builder_, level_, marker_, LSH_EXPR, result_, true, null);
      }
      else if (priority_ < 9 && consumeTokenSmart(builder_, RSH)) {
        result_ = expr(builder_, level_, 9);
        exit_section_(builder_, level_, marker_, RSH_EXPR, result_, true, null);
      }
      else if (priority_ < 10 && consumeTokenSmart(builder_, ADD)) {
        result_ = expr(builder_, level_, 10);
        exit_section_(builder_, level_, marker_, ADD_EXPR, result_, true, null);
      }
      else if (priority_ < 10 && consumeTokenSmart(builder_, SUB)) {
        result_ = expr(builder_, level_, 10);
        exit_section_(builder_, level_, marker_, SUB_EXPR, result_, true, null);
      }
      else if (priority_ < 11 && consumeTokenSmart(builder_, MUL)) {
        result_ = expr(builder_, level_, 11);
        exit_section_(builder_, level_, marker_, MUL_EXPR, result_, true, null);
      }
      else if (priority_ < 11 && consumeTokenSmart(builder_, DIV)) {
        result_ = expr(builder_, level_, 11);
        exit_section_(builder_, level_, marker_, DIV_EXPR, result_, true, null);
      }
      else if (priority_ < 11 && consumeTokenSmart(builder_, MOD)) {
        result_ = expr(builder_, level_, 11);
        exit_section_(builder_, level_, marker_, MOD_EXPR, result_, true, null);
      }
      else {
        exit_section_(builder_, level_, marker_, null, false, false, null);
        break;
      }
    }
    return result_;
  }

  // ppMacro
  public static boolean ppMacroExpr(com.intellij.lang.PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "ppMacroExpr")) return false;
    boolean result_;
    com.intellij.lang.PsiBuilder.Marker marker_ = enter_section_(builder_, level_, _NONE_, PP_MACRO_EXPR, "<pp macro expr>");
    result_ = ppMacro(builder_, level_ + 1);
    exit_section_(builder_, level_, marker_, result_, false, null);
    return result_;
  }

  // COLON expr
  private static boolean ternaryExpr_1(com.intellij.lang.PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "ternaryExpr_1")) return false;
    boolean result_;
    com.intellij.lang.PsiBuilder.Marker marker_ = enter_section_(builder_);
    result_ = consumeToken(builder_, COLON);
    result_ = result_ && expr(builder_, level_ + 1, -1);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  public static boolean negExpr(com.intellij.lang.PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "negExpr")) return false;
    if (!nextTokenIsSmart(builder_, SUB)) return false;
    boolean result_, pinned_;
    com.intellij.lang.PsiBuilder.Marker marker_ = enter_section_(builder_, level_, _NONE_, null);
    result_ = consumeTokenSmart(builder_, SUB);
    pinned_ = result_;
    result_ = pinned_ && expr(builder_, level_, 12);
    exit_section_(builder_, level_, marker_, NEG_EXPR, result_, pinned_, null);
    return result_ || pinned_;
  }

  public static boolean lNotExpr(com.intellij.lang.PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "lNotExpr")) return false;
    if (!nextTokenIsSmart(builder_, L_NOT)) return false;
    boolean result_, pinned_;
    com.intellij.lang.PsiBuilder.Marker marker_ = enter_section_(builder_, level_, _NONE_, null);
    result_ = consumeTokenSmart(builder_, L_NOT);
    pinned_ = result_;
    result_ = pinned_ && expr(builder_, level_, 12);
    exit_section_(builder_, level_, marker_, L_NOT_EXPR, result_, pinned_, null);
    return result_ || pinned_;
  }

  public static boolean bNotExpr(com.intellij.lang.PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "bNotExpr")) return false;
    if (!nextTokenIsSmart(builder_, NOT)) return false;
    boolean result_, pinned_;
    com.intellij.lang.PsiBuilder.Marker marker_ = enter_section_(builder_, level_, _NONE_, null);
    result_ = consumeTokenSmart(builder_, NOT);
    pinned_ = result_;
    result_ = pinned_ && expr(builder_, level_, 12);
    exit_section_(builder_, level_, marker_, B_NOT_EXPR, result_, pinned_, null);
    return result_ || pinned_;
  }

  // INT
  public static boolean intExpr(com.intellij.lang.PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "intExpr")) return false;
    if (!nextTokenIsSmart(builder_, INT)) return false;
    boolean result_;
    com.intellij.lang.PsiBuilder.Marker marker_ = enter_section_(builder_);
    result_ = consumeTokenSmart(builder_, INT);
    exit_section_(builder_, marker_, INT_EXPR, result_);
    return result_;
  }

  // char
  public static boolean charExpr(com.intellij.lang.PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "charExpr")) return false;
    if (!nextTokenIsSmart(builder_, SQUOTE)) return false;
    boolean result_;
    com.intellij.lang.PsiBuilder.Marker marker_ = enter_section_(builder_);
    result_ = char_$(builder_, level_ + 1);
    exit_section_(builder_, marker_, CHAR_EXPR, result_);
    return result_;
  }

  public static boolean parenExpr(com.intellij.lang.PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "parenExpr")) return false;
    if (!nextTokenIsSmart(builder_, LPAREN)) return false;
    boolean result_, pinned_;
    com.intellij.lang.PsiBuilder.Marker marker_ = enter_section_(builder_, level_, _NONE_, null);
    result_ = consumeTokenSmart(builder_, LPAREN);
    pinned_ = result_;
    result_ = pinned_ && expr(builder_, level_, -1);
    result_ = pinned_ && report_error_(builder_, consumeToken(builder_, RPAREN)) && result_;
    exit_section_(builder_, level_, marker_, PAREN_EXPR, result_, pinned_, null);
    return result_ || pinned_;
  }

  static final Parser DELETE_NODE_parser_ = (builder_, level_) -> consumeToken(builder_, DELETE_NODE);
  static final Parser DELETE_PROP_parser_ = (builder_, level_) -> consumeToken(builder_, DELETE_PROP);
  static final Parser NAME_parser_ = (builder_, level_) -> consumeToken(builder_, NAME);
  static final Parser OMIT_NODE_parser_ = (builder_, level_) -> consumeToken(builder_, OMIT_NODE);
  static final Parser PLUGIN_parser_ = (builder_, level_) -> consumeToken(builder_, PLUGIN);
  static final Parser V1_parser_ = (builder_, level_) -> consumeToken(builder_, V1);
}
