// This is a generated file. Not intended for manual editing.
package com.intellij.webassembly.lang.parser;

import com.intellij.lang.PsiBuilder;
import com.intellij.lang.PsiBuilder.Marker;

import com.intellij.lang.parser.GeneratedParserUtilBase;
import com.intellij.psi.tree.IElementType;
import com.intellij.lang.ASTNode;
import com.intellij.lang.PsiParser;
import com.intellij.lang.LightPsiParser;
import com.intellij.webassembly.lang.psi.WebAssemblyTypes;

@SuppressWarnings({"SimplifiableIfStatement", "UnusedAssignment"})
public class WebAssemblyParser implements PsiParser, LightPsiParser {

  public ASTNode parse(IElementType t, PsiBuilder b) {
    parseLight(t, b);
    return b.getTreeBuilt();
  }

  public void parseLight(IElementType t, PsiBuilder b) {
    boolean r;
    b = GeneratedParserUtilBase.adapt_builder_(t, b, this, null);
    Marker m = GeneratedParserUtilBase.enter_section_(b, 0, GeneratedParserUtilBase._COLLAPSE_, null);
    r = parse_root_(t, b);
    GeneratedParserUtilBase.exit_section_(b, 0, m, t, r, true, GeneratedParserUtilBase.TRUE_CONDITION);
  }

  protected boolean parse_root_(IElementType t, PsiBuilder b) {
    return parse_root_(t, b, 0);
  }

  static boolean parse_root_(IElementType t, PsiBuilder b, int l) {
    return webAssemblyFile(b, l + 1);
  }

  /* ********************************************************** */
  // ALIGNEQKEY UNSIGNED
  public static boolean aligneq(PsiBuilder b, int l) {
    if (!GeneratedParserUtilBase.recursion_guard_(b, l, "aligneq")) return false;
    boolean r, p;
    Marker m = GeneratedParserUtilBase.enter_section_(b, l, GeneratedParserUtilBase._NONE_, WebAssemblyTypes.ALIGNEQ, "<aligneq>");
    r = GeneratedParserUtilBase.consumeTokens(b, 1, WebAssemblyTypes.ALIGNEQKEY, WebAssemblyTypes.UNSIGNED);
    p = r; // pin = 1
    GeneratedParserUtilBase.exit_section_(b, l, m, r, p, WebAssemblyParser::aligneq_recover_);
    return r || p;
  }

  /* ********************************************************** */
  // !(instr | RPAR | <<eof>>)
  static boolean aligneq_recover_(PsiBuilder b, int l) {
    if (!GeneratedParserUtilBase.recursion_guard_(b, l, "aligneq_recover_")) return false;
    boolean r;
    Marker m = GeneratedParserUtilBase.enter_section_(b, l, GeneratedParserUtilBase._NOT_);
    r = !aligneq_recover__0(b, l + 1);
    GeneratedParserUtilBase.exit_section_(b, l, m, r, false, null);
    return r;
  }

  // instr | RPAR | <<eof>>
  private static boolean aligneq_recover__0(PsiBuilder b, int l) {
    if (!GeneratedParserUtilBase.recursion_guard_(b, l, "aligneq_recover__0")) return false;
    boolean r;
    Marker m = GeneratedParserUtilBase.enter_section_(b);
    r = instr(b, l + 1);
    if (!r) r = GeneratedParserUtilBase.consumeToken(b, WebAssemblyTypes.RPAR);
    if (!r) r = GeneratedParserUtilBase.eof(b, l + 1);
    GeneratedParserUtilBase.exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // blockinstr_block_ | blockinstr_loop_ | blockinstr_if_
  public static boolean blockinstr(PsiBuilder b, int l) {
    if (!GeneratedParserUtilBase.recursion_guard_(b, l, "blockinstr")) return false;
    boolean r;
    Marker m = GeneratedParserUtilBase.enter_section_(b, l, GeneratedParserUtilBase._NONE_, WebAssemblyTypes.BLOCKINSTR, "<blockinstr>");
    r = blockinstr_block_(b, l + 1);
    if (!r) r = blockinstr_loop_(b, l + 1);
    if (!r) r = blockinstr_if_(b, l + 1);
    GeneratedParserUtilBase.exit_section_(b, l, m, r, false, null);
    return r;
  }

  /* ********************************************************** */
  // BLOCKKEY blockinstr_block_aux_ ENDKEY IDENTIFIER?
  static boolean blockinstr_block_(PsiBuilder b, int l) {
    if (!GeneratedParserUtilBase.recursion_guard_(b, l, "blockinstr_block_")) return false;
    if (!GeneratedParserUtilBase.nextTokenIs(b, WebAssemblyTypes.BLOCKKEY)) return false;
    boolean r, p;
    Marker m = GeneratedParserUtilBase.enter_section_(b, l, GeneratedParserUtilBase._NONE_);
    r = GeneratedParserUtilBase.consumeToken(b, WebAssemblyTypes.BLOCKKEY);
    p = r; // pin = 1
    r = r && GeneratedParserUtilBase.report_error_(b, blockinstr_block_aux_(b, l + 1));
    r = p && GeneratedParserUtilBase.report_error_(b, GeneratedParserUtilBase.consumeToken(b, WebAssemblyTypes.ENDKEY)) && r;
    r = p && blockinstr_block__3(b, l + 1) && r;
    GeneratedParserUtilBase.exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  // IDENTIFIER?
  private static boolean blockinstr_block__3(PsiBuilder b, int l) {
    if (!GeneratedParserUtilBase.recursion_guard_(b, l, "blockinstr_block__3")) return false;
    GeneratedParserUtilBase.consumeToken(b, WebAssemblyTypes.IDENTIFIER);
    return true;
  }

  /* ********************************************************** */
  // IDENTIFIER? blocktype? instr*
  static boolean blockinstr_block_aux_(PsiBuilder b, int l) {
    if (!GeneratedParserUtilBase.recursion_guard_(b, l, "blockinstr_block_aux_")) return false;
    boolean r;
    Marker m = GeneratedParserUtilBase.enter_section_(b, l, GeneratedParserUtilBase._NONE_);
    r = blockinstr_block_aux__0(b, l + 1);
    r = r && blockinstr_block_aux__1(b, l + 1);
    r = r && blockinstr_block_aux__2(b, l + 1);
    GeneratedParserUtilBase.exit_section_(b, l, m, r, false, WebAssemblyParser::blockinstr_recover_);
    return r;
  }

  // IDENTIFIER?
  private static boolean blockinstr_block_aux__0(PsiBuilder b, int l) {
    if (!GeneratedParserUtilBase.recursion_guard_(b, l, "blockinstr_block_aux__0")) return false;
    GeneratedParserUtilBase.consumeToken(b, WebAssemblyTypes.IDENTIFIER);
    return true;
  }

  // blocktype?
  private static boolean blockinstr_block_aux__1(PsiBuilder b, int l) {
    if (!GeneratedParserUtilBase.recursion_guard_(b, l, "blockinstr_block_aux__1")) return false;
    blocktype(b, l + 1);
    return true;
  }

  // instr*
  private static boolean blockinstr_block_aux__2(PsiBuilder b, int l) {
    if (!GeneratedParserUtilBase.recursion_guard_(b, l, "blockinstr_block_aux__2")) return false;
    while (true) {
      int c = GeneratedParserUtilBase.current_position_(b);
      if (!instr(b, l + 1)) break;
      if (!GeneratedParserUtilBase.empty_element_parsed_guard_(b, "blockinstr_block_aux__2", c)) break;
    }
    return true;
  }

  /* ********************************************************** */
  // foldeinstr* IFKEY blockinstr_if_aux_ ENDKEY IDENTIFIER?
  static boolean blockinstr_if_(PsiBuilder b, int l) {
    if (!GeneratedParserUtilBase.recursion_guard_(b, l, "blockinstr_if_")) return false;
    if (!GeneratedParserUtilBase.nextTokenIs(b, "", WebAssemblyTypes.IFKEY, WebAssemblyTypes.LPAR)) return false;
    boolean r, p;
    Marker m = GeneratedParserUtilBase.enter_section_(b, l, GeneratedParserUtilBase._NONE_);
    r = blockinstr_if__0(b, l + 1);
    r = r && GeneratedParserUtilBase.consumeToken(b, WebAssemblyTypes.IFKEY);
    p = r; // pin = 2
    r = r && GeneratedParserUtilBase.report_error_(b, blockinstr_if_aux_(b, l + 1));
    r = p && GeneratedParserUtilBase.report_error_(b, GeneratedParserUtilBase.consumeToken(b, WebAssemblyTypes.ENDKEY)) && r;
    r = p && blockinstr_if__4(b, l + 1) && r;
    GeneratedParserUtilBase.exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  // foldeinstr*
  private static boolean blockinstr_if__0(PsiBuilder b, int l) {
    if (!GeneratedParserUtilBase.recursion_guard_(b, l, "blockinstr_if__0")) return false;
    while (true) {
      int c = GeneratedParserUtilBase.current_position_(b);
      if (!foldeinstr(b, l + 1)) break;
      if (!GeneratedParserUtilBase.empty_element_parsed_guard_(b, "blockinstr_if__0", c)) break;
    }
    return true;
  }

  // IDENTIFIER?
  private static boolean blockinstr_if__4(PsiBuilder b, int l) {
    if (!GeneratedParserUtilBase.recursion_guard_(b, l, "blockinstr_if__4")) return false;
    GeneratedParserUtilBase.consumeToken(b, WebAssemblyTypes.IDENTIFIER);
    return true;
  }

  /* ********************************************************** */
  // IDENTIFIER? blocktype? instr* (ELSEKEY IDENTIFIER? instr*)?
  static boolean blockinstr_if_aux_(PsiBuilder b, int l) {
    if (!GeneratedParserUtilBase.recursion_guard_(b, l, "blockinstr_if_aux_")) return false;
    boolean r;
    Marker m = GeneratedParserUtilBase.enter_section_(b, l, GeneratedParserUtilBase._NONE_);
    r = blockinstr_if_aux__0(b, l + 1);
    r = r && blockinstr_if_aux__1(b, l + 1);
    r = r && blockinstr_if_aux__2(b, l + 1);
    r = r && blockinstr_if_aux__3(b, l + 1);
    GeneratedParserUtilBase.exit_section_(b, l, m, r, false, WebAssemblyParser::blockinstr_recover_);
    return r;
  }

  // IDENTIFIER?
  private static boolean blockinstr_if_aux__0(PsiBuilder b, int l) {
    if (!GeneratedParserUtilBase.recursion_guard_(b, l, "blockinstr_if_aux__0")) return false;
    GeneratedParserUtilBase.consumeToken(b, WebAssemblyTypes.IDENTIFIER);
    return true;
  }

  // blocktype?
  private static boolean blockinstr_if_aux__1(PsiBuilder b, int l) {
    if (!GeneratedParserUtilBase.recursion_guard_(b, l, "blockinstr_if_aux__1")) return false;
    blocktype(b, l + 1);
    return true;
  }

  // instr*
  private static boolean blockinstr_if_aux__2(PsiBuilder b, int l) {
    if (!GeneratedParserUtilBase.recursion_guard_(b, l, "blockinstr_if_aux__2")) return false;
    while (true) {
      int c = GeneratedParserUtilBase.current_position_(b);
      if (!instr(b, l + 1)) break;
      if (!GeneratedParserUtilBase.empty_element_parsed_guard_(b, "blockinstr_if_aux__2", c)) break;
    }
    return true;
  }

  // (ELSEKEY IDENTIFIER? instr*)?
  private static boolean blockinstr_if_aux__3(PsiBuilder b, int l) {
    if (!GeneratedParserUtilBase.recursion_guard_(b, l, "blockinstr_if_aux__3")) return false;
    blockinstr_if_aux__3_0(b, l + 1);
    return true;
  }

  // ELSEKEY IDENTIFIER? instr*
  private static boolean blockinstr_if_aux__3_0(PsiBuilder b, int l) {
    if (!GeneratedParserUtilBase.recursion_guard_(b, l, "blockinstr_if_aux__3_0")) return false;
    boolean r;
    Marker m = GeneratedParserUtilBase.enter_section_(b);
    r = GeneratedParserUtilBase.consumeToken(b, WebAssemblyTypes.ELSEKEY);
    r = r && blockinstr_if_aux__3_0_1(b, l + 1);
    r = r && blockinstr_if_aux__3_0_2(b, l + 1);
    GeneratedParserUtilBase.exit_section_(b, m, null, r);
    return r;
  }

  // IDENTIFIER?
  private static boolean blockinstr_if_aux__3_0_1(PsiBuilder b, int l) {
    if (!GeneratedParserUtilBase.recursion_guard_(b, l, "blockinstr_if_aux__3_0_1")) return false;
    GeneratedParserUtilBase.consumeToken(b, WebAssemblyTypes.IDENTIFIER);
    return true;
  }

  // instr*
  private static boolean blockinstr_if_aux__3_0_2(PsiBuilder b, int l) {
    if (!GeneratedParserUtilBase.recursion_guard_(b, l, "blockinstr_if_aux__3_0_2")) return false;
    while (true) {
      int c = GeneratedParserUtilBase.current_position_(b);
      if (!instr(b, l + 1)) break;
      if (!GeneratedParserUtilBase.empty_element_parsed_guard_(b, "blockinstr_if_aux__3_0_2", c)) break;
    }
    return true;
  }

  /* ********************************************************** */
  // LOOPKEY blockinstr_loop_aux_ ENDKEY IDENTIFIER?
  static boolean blockinstr_loop_(PsiBuilder b, int l) {
    if (!GeneratedParserUtilBase.recursion_guard_(b, l, "blockinstr_loop_")) return false;
    if (!GeneratedParserUtilBase.nextTokenIs(b, WebAssemblyTypes.LOOPKEY)) return false;
    boolean r, p;
    Marker m = GeneratedParserUtilBase.enter_section_(b, l, GeneratedParserUtilBase._NONE_);
    r = GeneratedParserUtilBase.consumeToken(b, WebAssemblyTypes.LOOPKEY);
    p = r; // pin = 1
    r = r && GeneratedParserUtilBase.report_error_(b, blockinstr_loop_aux_(b, l + 1));
    r = p && GeneratedParserUtilBase.report_error_(b, GeneratedParserUtilBase.consumeToken(b, WebAssemblyTypes.ENDKEY)) && r;
    r = p && blockinstr_loop__3(b, l + 1) && r;
    GeneratedParserUtilBase.exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  // IDENTIFIER?
  private static boolean blockinstr_loop__3(PsiBuilder b, int l) {
    if (!GeneratedParserUtilBase.recursion_guard_(b, l, "blockinstr_loop__3")) return false;
    GeneratedParserUtilBase.consumeToken(b, WebAssemblyTypes.IDENTIFIER);
    return true;
  }

  /* ********************************************************** */
  // IDENTIFIER? blocktype? instr*
  static boolean blockinstr_loop_aux_(PsiBuilder b, int l) {
    if (!GeneratedParserUtilBase.recursion_guard_(b, l, "blockinstr_loop_aux_")) return false;
    boolean r;
    Marker m = GeneratedParserUtilBase.enter_section_(b, l, GeneratedParserUtilBase._NONE_);
    r = blockinstr_loop_aux__0(b, l + 1);
    r = r && blockinstr_loop_aux__1(b, l + 1);
    r = r && blockinstr_loop_aux__2(b, l + 1);
    GeneratedParserUtilBase.exit_section_(b, l, m, r, false, WebAssemblyParser::blockinstr_recover_);
    return r;
  }

  // IDENTIFIER?
  private static boolean blockinstr_loop_aux__0(PsiBuilder b, int l) {
    if (!GeneratedParserUtilBase.recursion_guard_(b, l, "blockinstr_loop_aux__0")) return false;
    GeneratedParserUtilBase.consumeToken(b, WebAssemblyTypes.IDENTIFIER);
    return true;
  }

  // blocktype?
  private static boolean blockinstr_loop_aux__1(PsiBuilder b, int l) {
    if (!GeneratedParserUtilBase.recursion_guard_(b, l, "blockinstr_loop_aux__1")) return false;
    blocktype(b, l + 1);
    return true;
  }

  // instr*
  private static boolean blockinstr_loop_aux__2(PsiBuilder b, int l) {
    if (!GeneratedParserUtilBase.recursion_guard_(b, l, "blockinstr_loop_aux__2")) return false;
    while (true) {
      int c = GeneratedParserUtilBase.current_position_(b);
      if (!instr(b, l + 1)) break;
      if (!GeneratedParserUtilBase.empty_element_parsed_guard_(b, "blockinstr_loop_aux__2", c)) break;
    }
    return true;
  }

  /* ********************************************************** */
  // !(ENDKEY | <<eof>>)
  static boolean blockinstr_recover_(PsiBuilder b, int l) {
    if (!GeneratedParserUtilBase.recursion_guard_(b, l, "blockinstr_recover_")) return false;
    boolean r;
    Marker m = GeneratedParserUtilBase.enter_section_(b, l, GeneratedParserUtilBase._NOT_);
    r = !blockinstr_recover__0(b, l + 1);
    GeneratedParserUtilBase.exit_section_(b, l, m, r, false, null);
    return r;
  }

  // ENDKEY | <<eof>>
  private static boolean blockinstr_recover__0(PsiBuilder b, int l) {
    if (!GeneratedParserUtilBase.recursion_guard_(b, l, "blockinstr_recover__0")) return false;
    boolean r;
    Marker m = GeneratedParserUtilBase.enter_section_(b);
    r = GeneratedParserUtilBase.consumeToken(b, WebAssemblyTypes.ENDKEY);
    if (!r) r = GeneratedParserUtilBase.eof(b, l + 1);
    GeneratedParserUtilBase.exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // result | typeuse
  public static boolean blocktype(PsiBuilder b, int l) {
    if (!GeneratedParserUtilBase.recursion_guard_(b, l, "blocktype")) return false;
    if (!GeneratedParserUtilBase.nextTokenIs(b, WebAssemblyTypes.LPAR)) return false;
    boolean r;
    Marker m = GeneratedParserUtilBase.enter_section_(b);
    r = result(b, l + 1);
    if (!r) r = typeuse(b, l + 1);
    GeneratedParserUtilBase.exit_section_(b, m, WebAssemblyTypes.BLOCKTYPE, r);
    return r;
  }

  /* ********************************************************** */
  // CALLINDIRECTINSTR idx? typeuse?
  public static boolean call_indirect_instr(PsiBuilder b, int l) {
    if (!GeneratedParserUtilBase.recursion_guard_(b, l, "call_indirect_instr")) return false;
    if (!GeneratedParserUtilBase.nextTokenIs(b, WebAssemblyTypes.CALLINDIRECTINSTR)) return false;
    boolean r, p;
    Marker m = GeneratedParserUtilBase.enter_section_(b, l, GeneratedParserUtilBase._NONE_, WebAssemblyTypes.CALL_INDIRECT_INSTR, null);
    r = GeneratedParserUtilBase.consumeToken(b, WebAssemblyTypes.CALLINDIRECTINSTR);
    p = r; // pin = 1
    r = r && GeneratedParserUtilBase.report_error_(b, call_indirect_instr_1(b, l + 1));
    r = p && call_indirect_instr_2(b, l + 1) && r;
    GeneratedParserUtilBase.exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  // idx?
  private static boolean call_indirect_instr_1(PsiBuilder b, int l) {
    if (!GeneratedParserUtilBase.recursion_guard_(b, l, "call_indirect_instr_1")) return false;
    idx(b, l + 1);
    return true;
  }

  // typeuse?
  private static boolean call_indirect_instr_2(PsiBuilder b, int l) {
    if (!GeneratedParserUtilBase.recursion_guard_(b, l, "call_indirect_instr_2")) return false;
    typeuse(b, l + 1);
    return true;
  }

  /* ********************************************************** */
  // CALLINSTR idx
  public static boolean call_instr(PsiBuilder b, int l) {
    if (!GeneratedParserUtilBase.recursion_guard_(b, l, "call_instr")) return false;
    if (!GeneratedParserUtilBase.nextTokenIs(b, WebAssemblyTypes.CALLINSTR)) return false;
    boolean r, p;
    Marker m = GeneratedParserUtilBase.enter_section_(b, l, GeneratedParserUtilBase._NONE_, WebAssemblyTypes.CALL_INSTR, null);
    r = GeneratedParserUtilBase.consumeToken(b, WebAssemblyTypes.CALLINSTR);
    p = r; // pin = 1
    r = r && idx(b, l + 1);
    GeneratedParserUtilBase.exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  /* ********************************************************** */
  // LINE_COMMENT | BLOCK_COMMENT
  public static boolean comment(PsiBuilder b, int l) {
    if (!GeneratedParserUtilBase.recursion_guard_(b, l, "comment")) return false;
    if (!GeneratedParserUtilBase.nextTokenIs(b, "<comment>", WebAssemblyTypes.BLOCK_COMMENT, WebAssemblyTypes.LINE_COMMENT)) return false;
    boolean r;
    Marker m = GeneratedParserUtilBase.enter_section_(b, l, GeneratedParserUtilBase._NONE_, WebAssemblyTypes.COMMENT, "<comment>");
    r = GeneratedParserUtilBase.consumeToken(b, WebAssemblyTypes.LINE_COMMENT);
    if (!r) r = GeneratedParserUtilBase.consumeToken(b, WebAssemblyTypes.BLOCK_COMMENT);
    GeneratedParserUtilBase.exit_section_(b, l, m, r, false, null);
    return r;
  }

  /* ********************************************************** */
  // LPAR data_aux_ RPAR
  public static boolean data(PsiBuilder b, int l) {
    if (!GeneratedParserUtilBase.recursion_guard_(b, l, "data")) return false;
    if (!GeneratedParserUtilBase.nextTokenIs(b, WebAssemblyTypes.LPAR)) return false;
    boolean r, p;
    Marker m = GeneratedParserUtilBase.enter_section_(b, l, GeneratedParserUtilBase._NONE_, WebAssemblyTypes.DATA, null);
    r = GeneratedParserUtilBase.consumeToken(b, WebAssemblyTypes.LPAR);
    r = r && data_aux_(b, l + 1);
    p = r; // pin = 2
    r = r && GeneratedParserUtilBase.consumeToken(b, WebAssemblyTypes.RPAR);
    GeneratedParserUtilBase.exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  /* ********************************************************** */
  // DATAKEY IDENTIFIER? (memuse_? (instr | LPAR OFFSETKEY instr* RPAR))? STRING*
  static boolean data_aux_(PsiBuilder b, int l) {
    if (!GeneratedParserUtilBase.recursion_guard_(b, l, "data_aux_")) return false;
    boolean r, p;
    Marker m = GeneratedParserUtilBase.enter_section_(b, l, GeneratedParserUtilBase._NONE_);
    r = GeneratedParserUtilBase.consumeToken(b, WebAssemblyTypes.DATAKEY);
    p = r; // pin = 1
    r = r && GeneratedParserUtilBase.report_error_(b, data_aux__1(b, l + 1));
    r = p && GeneratedParserUtilBase.report_error_(b, data_aux__2(b, l + 1)) && r;
    r = p && data_aux__3(b, l + 1) && r;
    GeneratedParserUtilBase.exit_section_(b, l, m, r, p, WebAssemblyParser::item_recover_);
    return r || p;
  }

  // IDENTIFIER?
  private static boolean data_aux__1(PsiBuilder b, int l) {
    if (!GeneratedParserUtilBase.recursion_guard_(b, l, "data_aux__1")) return false;
    GeneratedParserUtilBase.consumeToken(b, WebAssemblyTypes.IDENTIFIER);
    return true;
  }

  // (memuse_? (instr | LPAR OFFSETKEY instr* RPAR))?
  private static boolean data_aux__2(PsiBuilder b, int l) {
    if (!GeneratedParserUtilBase.recursion_guard_(b, l, "data_aux__2")) return false;
    data_aux__2_0(b, l + 1);
    return true;
  }

  // memuse_? (instr | LPAR OFFSETKEY instr* RPAR)
  private static boolean data_aux__2_0(PsiBuilder b, int l) {
    if (!GeneratedParserUtilBase.recursion_guard_(b, l, "data_aux__2_0")) return false;
    boolean r;
    Marker m = GeneratedParserUtilBase.enter_section_(b);
    r = data_aux__2_0_0(b, l + 1);
    r = r && data_aux__2_0_1(b, l + 1);
    GeneratedParserUtilBase.exit_section_(b, m, null, r);
    return r;
  }

  // memuse_?
  private static boolean data_aux__2_0_0(PsiBuilder b, int l) {
    if (!GeneratedParserUtilBase.recursion_guard_(b, l, "data_aux__2_0_0")) return false;
    memuse_(b, l + 1);
    return true;
  }

  // instr | LPAR OFFSETKEY instr* RPAR
  private static boolean data_aux__2_0_1(PsiBuilder b, int l) {
    if (!GeneratedParserUtilBase.recursion_guard_(b, l, "data_aux__2_0_1")) return false;
    boolean r;
    Marker m = GeneratedParserUtilBase.enter_section_(b);
    r = instr(b, l + 1);
    if (!r) r = data_aux__2_0_1_1(b, l + 1);
    GeneratedParserUtilBase.exit_section_(b, m, null, r);
    return r;
  }

  // LPAR OFFSETKEY instr* RPAR
  private static boolean data_aux__2_0_1_1(PsiBuilder b, int l) {
    if (!GeneratedParserUtilBase.recursion_guard_(b, l, "data_aux__2_0_1_1")) return false;
    boolean r;
    Marker m = GeneratedParserUtilBase.enter_section_(b);
    r = GeneratedParserUtilBase.consumeTokens(b, 0, WebAssemblyTypes.LPAR, WebAssemblyTypes.OFFSETKEY);
    r = r && data_aux__2_0_1_1_2(b, l + 1);
    r = r && GeneratedParserUtilBase.consumeToken(b, WebAssemblyTypes.RPAR);
    GeneratedParserUtilBase.exit_section_(b, m, null, r);
    return r;
  }

  // instr*
  private static boolean data_aux__2_0_1_1_2(PsiBuilder b, int l) {
    if (!GeneratedParserUtilBase.recursion_guard_(b, l, "data_aux__2_0_1_1_2")) return false;
    while (true) {
      int c = GeneratedParserUtilBase.current_position_(b);
      if (!instr(b, l + 1)) break;
      if (!GeneratedParserUtilBase.empty_element_parsed_guard_(b, "data_aux__2_0_1_1_2", c)) break;
    }
    return true;
  }

  // STRING*
  private static boolean data_aux__3(PsiBuilder b, int l) {
    if (!GeneratedParserUtilBase.recursion_guard_(b, l, "data_aux__3")) return false;
    while (true) {
      int c = GeneratedParserUtilBase.current_position_(b);
      if (!GeneratedParserUtilBase.consumeToken(b, WebAssemblyTypes.STRING)) break;
      if (!GeneratedParserUtilBase.empty_element_parsed_guard_(b, "data_aux__3", c)) break;
    }
    return true;
  }

  /* ********************************************************** */
  // LPAR elem_aux_ RPAR
  public static boolean elem(PsiBuilder b, int l) {
    if (!GeneratedParserUtilBase.recursion_guard_(b, l, "elem")) return false;
    if (!GeneratedParserUtilBase.nextTokenIs(b, WebAssemblyTypes.LPAR)) return false;
    boolean r, p;
    Marker m = GeneratedParserUtilBase.enter_section_(b, l, GeneratedParserUtilBase._NONE_, WebAssemblyTypes.ELEM, null);
    r = GeneratedParserUtilBase.consumeToken(b, WebAssemblyTypes.LPAR);
    r = r && elem_aux_(b, l + 1);
    p = r; // pin = 2
    r = r && GeneratedParserUtilBase.consumeToken(b, WebAssemblyTypes.RPAR);
    GeneratedParserUtilBase.exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  /* ********************************************************** */
  // ELEMKEY IDENTIFIER? (
  //                          (LPAR TABLEKEY idx RPAR)? (instr | LPAR OFFSETKEY instr* RPAR)
  //                        | DECLAREKEY
  //                       )? elemlist
  static boolean elem_aux_(PsiBuilder b, int l) {
    if (!GeneratedParserUtilBase.recursion_guard_(b, l, "elem_aux_")) return false;
    boolean r, p;
    Marker m = GeneratedParserUtilBase.enter_section_(b, l, GeneratedParserUtilBase._NONE_);
    r = GeneratedParserUtilBase.consumeToken(b, WebAssemblyTypes.ELEMKEY);
    p = r; // pin = 1
    r = r && GeneratedParserUtilBase.report_error_(b, elem_aux__1(b, l + 1));
    r = p && GeneratedParserUtilBase.report_error_(b, elem_aux__2(b, l + 1)) && r;
    r = p && elemlist(b, l + 1) && r;
    GeneratedParserUtilBase.exit_section_(b, l, m, r, p, WebAssemblyParser::item_recover_);
    return r || p;
  }

  // IDENTIFIER?
  private static boolean elem_aux__1(PsiBuilder b, int l) {
    if (!GeneratedParserUtilBase.recursion_guard_(b, l, "elem_aux__1")) return false;
    GeneratedParserUtilBase.consumeToken(b, WebAssemblyTypes.IDENTIFIER);
    return true;
  }

  // (
  //                          (LPAR TABLEKEY idx RPAR)? (instr | LPAR OFFSETKEY instr* RPAR)
  //                        | DECLAREKEY
  //                       )?
  private static boolean elem_aux__2(PsiBuilder b, int l) {
    if (!GeneratedParserUtilBase.recursion_guard_(b, l, "elem_aux__2")) return false;
    elem_aux__2_0(b, l + 1);
    return true;
  }

  // (LPAR TABLEKEY idx RPAR)? (instr | LPAR OFFSETKEY instr* RPAR)
  //                        | DECLAREKEY
  private static boolean elem_aux__2_0(PsiBuilder b, int l) {
    if (!GeneratedParserUtilBase.recursion_guard_(b, l, "elem_aux__2_0")) return false;
    boolean r;
    Marker m = GeneratedParserUtilBase.enter_section_(b);
    r = elem_aux__2_0_0(b, l + 1);
    if (!r) r = GeneratedParserUtilBase.consumeToken(b, WebAssemblyTypes.DECLAREKEY);
    GeneratedParserUtilBase.exit_section_(b, m, null, r);
    return r;
  }

  // (LPAR TABLEKEY idx RPAR)? (instr | LPAR OFFSETKEY instr* RPAR)
  private static boolean elem_aux__2_0_0(PsiBuilder b, int l) {
    if (!GeneratedParserUtilBase.recursion_guard_(b, l, "elem_aux__2_0_0")) return false;
    boolean r;
    Marker m = GeneratedParserUtilBase.enter_section_(b);
    r = elem_aux__2_0_0_0(b, l + 1);
    r = r && elem_aux__2_0_0_1(b, l + 1);
    GeneratedParserUtilBase.exit_section_(b, m, null, r);
    return r;
  }

  // (LPAR TABLEKEY idx RPAR)?
  private static boolean elem_aux__2_0_0_0(PsiBuilder b, int l) {
    if (!GeneratedParserUtilBase.recursion_guard_(b, l, "elem_aux__2_0_0_0")) return false;
    elem_aux__2_0_0_0_0(b, l + 1);
    return true;
  }

  // LPAR TABLEKEY idx RPAR
  private static boolean elem_aux__2_0_0_0_0(PsiBuilder b, int l) {
    if (!GeneratedParserUtilBase.recursion_guard_(b, l, "elem_aux__2_0_0_0_0")) return false;
    boolean r;
    Marker m = GeneratedParserUtilBase.enter_section_(b);
    r = GeneratedParserUtilBase.consumeTokens(b, 0, WebAssemblyTypes.LPAR, WebAssemblyTypes.TABLEKEY);
    r = r && idx(b, l + 1);
    r = r && GeneratedParserUtilBase.consumeToken(b, WebAssemblyTypes.RPAR);
    GeneratedParserUtilBase.exit_section_(b, m, null, r);
    return r;
  }

  // instr | LPAR OFFSETKEY instr* RPAR
  private static boolean elem_aux__2_0_0_1(PsiBuilder b, int l) {
    if (!GeneratedParserUtilBase.recursion_guard_(b, l, "elem_aux__2_0_0_1")) return false;
    boolean r;
    Marker m = GeneratedParserUtilBase.enter_section_(b);
    r = instr(b, l + 1);
    if (!r) r = elem_aux__2_0_0_1_1(b, l + 1);
    GeneratedParserUtilBase.exit_section_(b, m, null, r);
    return r;
  }

  // LPAR OFFSETKEY instr* RPAR
  private static boolean elem_aux__2_0_0_1_1(PsiBuilder b, int l) {
    if (!GeneratedParserUtilBase.recursion_guard_(b, l, "elem_aux__2_0_0_1_1")) return false;
    boolean r;
    Marker m = GeneratedParserUtilBase.enter_section_(b);
    r = GeneratedParserUtilBase.consumeTokens(b, 0, WebAssemblyTypes.LPAR, WebAssemblyTypes.OFFSETKEY);
    r = r && elem_aux__2_0_0_1_1_2(b, l + 1);
    r = r && GeneratedParserUtilBase.consumeToken(b, WebAssemblyTypes.RPAR);
    GeneratedParserUtilBase.exit_section_(b, m, null, r);
    return r;
  }

  // instr*
  private static boolean elem_aux__2_0_0_1_1_2(PsiBuilder b, int l) {
    if (!GeneratedParserUtilBase.recursion_guard_(b, l, "elem_aux__2_0_0_1_1_2")) return false;
    while (true) {
      int c = GeneratedParserUtilBase.current_position_(b);
      if (!instr(b, l + 1)) break;
      if (!GeneratedParserUtilBase.empty_element_parsed_guard_(b, "elem_aux__2_0_0_1_1_2", c)) break;
    }
    return true;
  }

  /* ********************************************************** */
  // ELEMDROPINSTR idx
  public static boolean elem_drop_instr(PsiBuilder b, int l) {
    if (!GeneratedParserUtilBase.recursion_guard_(b, l, "elem_drop_instr")) return false;
    if (!GeneratedParserUtilBase.nextTokenIs(b, WebAssemblyTypes.ELEMDROPINSTR)) return false;
    boolean r, p;
    Marker m = GeneratedParserUtilBase.enter_section_(b, l, GeneratedParserUtilBase._NONE_, WebAssemblyTypes.ELEM_DROP_INSTR, null);
    r = GeneratedParserUtilBase.consumeToken(b, WebAssemblyTypes.ELEMDROPINSTR);
    p = r; // pin = 1
    r = r && idx(b, l + 1);
    GeneratedParserUtilBase.exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  /* ********************************************************** */
  // REFTYPE (instr | LPAR ITEMKEY instr* RPAR)* | FUNCKEY? idx*
  public static boolean elemlist(PsiBuilder b, int l) {
    if (!GeneratedParserUtilBase.recursion_guard_(b, l, "elemlist")) return false;
    boolean r;
    Marker m = GeneratedParserUtilBase.enter_section_(b, l, GeneratedParserUtilBase._NONE_, WebAssemblyTypes.ELEMLIST, "<elemlist>");
    r = elemlist_0(b, l + 1);
    if (!r) r = elemlist_1(b, l + 1);
    GeneratedParserUtilBase.exit_section_(b, l, m, r, false, null);
    return r;
  }

  // REFTYPE (instr | LPAR ITEMKEY instr* RPAR)*
  private static boolean elemlist_0(PsiBuilder b, int l) {
    if (!GeneratedParserUtilBase.recursion_guard_(b, l, "elemlist_0")) return false;
    boolean r;
    Marker m = GeneratedParserUtilBase.enter_section_(b);
    r = GeneratedParserUtilBase.consumeToken(b, WebAssemblyTypes.REFTYPE);
    r = r && elemlist_0_1(b, l + 1);
    GeneratedParserUtilBase.exit_section_(b, m, null, r);
    return r;
  }

  // (instr | LPAR ITEMKEY instr* RPAR)*
  private static boolean elemlist_0_1(PsiBuilder b, int l) {
    if (!GeneratedParserUtilBase.recursion_guard_(b, l, "elemlist_0_1")) return false;
    while (true) {
      int c = GeneratedParserUtilBase.current_position_(b);
      if (!elemlist_0_1_0(b, l + 1)) break;
      if (!GeneratedParserUtilBase.empty_element_parsed_guard_(b, "elemlist_0_1", c)) break;
    }
    return true;
  }

  // instr | LPAR ITEMKEY instr* RPAR
  private static boolean elemlist_0_1_0(PsiBuilder b, int l) {
    if (!GeneratedParserUtilBase.recursion_guard_(b, l, "elemlist_0_1_0")) return false;
    boolean r;
    Marker m = GeneratedParserUtilBase.enter_section_(b);
    r = instr(b, l + 1);
    if (!r) r = elemlist_0_1_0_1(b, l + 1);
    GeneratedParserUtilBase.exit_section_(b, m, null, r);
    return r;
  }

  // LPAR ITEMKEY instr* RPAR
  private static boolean elemlist_0_1_0_1(PsiBuilder b, int l) {
    if (!GeneratedParserUtilBase.recursion_guard_(b, l, "elemlist_0_1_0_1")) return false;
    boolean r;
    Marker m = GeneratedParserUtilBase.enter_section_(b);
    r = GeneratedParserUtilBase.consumeTokens(b, 0, WebAssemblyTypes.LPAR, WebAssemblyTypes.ITEMKEY);
    r = r && elemlist_0_1_0_1_2(b, l + 1);
    r = r && GeneratedParserUtilBase.consumeToken(b, WebAssemblyTypes.RPAR);
    GeneratedParserUtilBase.exit_section_(b, m, null, r);
    return r;
  }

  // instr*
  private static boolean elemlist_0_1_0_1_2(PsiBuilder b, int l) {
    if (!GeneratedParserUtilBase.recursion_guard_(b, l, "elemlist_0_1_0_1_2")) return false;
    while (true) {
      int c = GeneratedParserUtilBase.current_position_(b);
      if (!instr(b, l + 1)) break;
      if (!GeneratedParserUtilBase.empty_element_parsed_guard_(b, "elemlist_0_1_0_1_2", c)) break;
    }
    return true;
  }

  // FUNCKEY? idx*
  private static boolean elemlist_1(PsiBuilder b, int l) {
    if (!GeneratedParserUtilBase.recursion_guard_(b, l, "elemlist_1")) return false;
    boolean r;
    Marker m = GeneratedParserUtilBase.enter_section_(b);
    r = elemlist_1_0(b, l + 1);
    r = r && elemlist_1_1(b, l + 1);
    GeneratedParserUtilBase.exit_section_(b, m, null, r);
    return r;
  }

  // FUNCKEY?
  private static boolean elemlist_1_0(PsiBuilder b, int l) {
    if (!GeneratedParserUtilBase.recursion_guard_(b, l, "elemlist_1_0")) return false;
    GeneratedParserUtilBase.consumeToken(b, WebAssemblyTypes.FUNCKEY);
    return true;
  }

  // idx*
  private static boolean elemlist_1_1(PsiBuilder b, int l) {
    if (!GeneratedParserUtilBase.recursion_guard_(b, l, "elemlist_1_1")) return false;
    while (true) {
      int c = GeneratedParserUtilBase.current_position_(b);
      if (!idx(b, l + 1)) break;
      if (!GeneratedParserUtilBase.empty_element_parsed_guard_(b, "elemlist_1_1", c)) break;
    }
    return true;
  }

  /* ********************************************************** */
  // LPAR export_aux_ RPAR
  public static boolean export(PsiBuilder b, int l) {
    if (!GeneratedParserUtilBase.recursion_guard_(b, l, "export")) return false;
    if (!GeneratedParserUtilBase.nextTokenIs(b, WebAssemblyTypes.LPAR)) return false;
    boolean r, p;
    Marker m = GeneratedParserUtilBase.enter_section_(b, l, GeneratedParserUtilBase._NONE_, WebAssemblyTypes.EXPORT, null);
    r = GeneratedParserUtilBase.consumeToken(b, WebAssemblyTypes.LPAR);
    r = r && export_aux_(b, l + 1);
    p = r; // pin = 2
    r = r && GeneratedParserUtilBase.consumeToken(b, WebAssemblyTypes.RPAR);
    GeneratedParserUtilBase.exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  /* ********************************************************** */
  // EXPORTKEY string_aux_ exportdesc
  static boolean export_aux_(PsiBuilder b, int l) {
    if (!GeneratedParserUtilBase.recursion_guard_(b, l, "export_aux_")) return false;
    boolean r, p;
    Marker m = GeneratedParserUtilBase.enter_section_(b, l, GeneratedParserUtilBase._NONE_);
    r = GeneratedParserUtilBase.consumeToken(b, WebAssemblyTypes.EXPORTKEY);
    p = r; // pin = 1
    r = r && GeneratedParserUtilBase.report_error_(b, string_aux_(b, l + 1));
    r = p && exportdesc(b, l + 1) && r;
    GeneratedParserUtilBase.exit_section_(b, l, m, r, p, WebAssemblyParser::item_recover_);
    return r || p;
  }

  /* ********************************************************** */
  // exportdesc_func_ | exportdesc_table_ | exportdesc_memory_ | exportdesc_global_
  public static boolean exportdesc(PsiBuilder b, int l) {
    if (!GeneratedParserUtilBase.recursion_guard_(b, l, "exportdesc")) return false;
    if (!GeneratedParserUtilBase.nextTokenIs(b, WebAssemblyTypes.LPAR)) return false;
    boolean r;
    Marker m = GeneratedParserUtilBase.enter_section_(b);
    r = exportdesc_func_(b, l + 1);
    if (!r) r = exportdesc_table_(b, l + 1);
    if (!r) r = exportdesc_memory_(b, l + 1);
    if (!r) r = exportdesc_global_(b, l + 1);
    GeneratedParserUtilBase.exit_section_(b, m, WebAssemblyTypes.EXPORTDESC, r);
    return r;
  }

  /* ********************************************************** */
  // LPAR exportdesc_func_aux_ RPAR
  static boolean exportdesc_func_(PsiBuilder b, int l) {
    if (!GeneratedParserUtilBase.recursion_guard_(b, l, "exportdesc_func_")) return false;
    if (!GeneratedParserUtilBase.nextTokenIs(b, WebAssemblyTypes.LPAR)) return false;
    boolean r, p;
    Marker m = GeneratedParserUtilBase.enter_section_(b, l, GeneratedParserUtilBase._NONE_);
    r = GeneratedParserUtilBase.consumeToken(b, WebAssemblyTypes.LPAR);
    r = r && exportdesc_func_aux_(b, l + 1);
    p = r; // pin = 2
    r = r && GeneratedParserUtilBase.consumeToken(b, WebAssemblyTypes.RPAR);
    GeneratedParserUtilBase.exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  /* ********************************************************** */
  // FUNCKEY idx
  static boolean exportdesc_func_aux_(PsiBuilder b, int l) {
    if (!GeneratedParserUtilBase.recursion_guard_(b, l, "exportdesc_func_aux_")) return false;
    boolean r, p;
    Marker m = GeneratedParserUtilBase.enter_section_(b, l, GeneratedParserUtilBase._NONE_);
    r = GeneratedParserUtilBase.consumeToken(b, WebAssemblyTypes.FUNCKEY);
    p = r; // pin = 1
    r = r && idx(b, l + 1);
    GeneratedParserUtilBase.exit_section_(b, l, m, r, p, WebAssemblyParser::item_recover_);
    return r || p;
  }

  /* ********************************************************** */
  // LPAR exportdesc_global_aux_ RPAR
  static boolean exportdesc_global_(PsiBuilder b, int l) {
    if (!GeneratedParserUtilBase.recursion_guard_(b, l, "exportdesc_global_")) return false;
    if (!GeneratedParserUtilBase.nextTokenIs(b, WebAssemblyTypes.LPAR)) return false;
    boolean r, p;
    Marker m = GeneratedParserUtilBase.enter_section_(b, l, GeneratedParserUtilBase._NONE_);
    r = GeneratedParserUtilBase.consumeToken(b, WebAssemblyTypes.LPAR);
    r = r && exportdesc_global_aux_(b, l + 1);
    p = r; // pin = 2
    r = r && GeneratedParserUtilBase.consumeToken(b, WebAssemblyTypes.RPAR);
    GeneratedParserUtilBase.exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  /* ********************************************************** */
  // GLOBALKEY idx
  static boolean exportdesc_global_aux_(PsiBuilder b, int l) {
    if (!GeneratedParserUtilBase.recursion_guard_(b, l, "exportdesc_global_aux_")) return false;
    boolean r, p;
    Marker m = GeneratedParserUtilBase.enter_section_(b, l, GeneratedParserUtilBase._NONE_);
    r = GeneratedParserUtilBase.consumeToken(b, WebAssemblyTypes.GLOBALKEY);
    p = r; // pin = 1
    r = r && idx(b, l + 1);
    GeneratedParserUtilBase.exit_section_(b, l, m, r, p, WebAssemblyParser::item_recover_);
    return r || p;
  }

  /* ********************************************************** */
  // LPAR exportdesc_memory_aux_ RPAR
  static boolean exportdesc_memory_(PsiBuilder b, int l) {
    if (!GeneratedParserUtilBase.recursion_guard_(b, l, "exportdesc_memory_")) return false;
    if (!GeneratedParserUtilBase.nextTokenIs(b, WebAssemblyTypes.LPAR)) return false;
    boolean r, p;
    Marker m = GeneratedParserUtilBase.enter_section_(b, l, GeneratedParserUtilBase._NONE_);
    r = GeneratedParserUtilBase.consumeToken(b, WebAssemblyTypes.LPAR);
    r = r && exportdesc_memory_aux_(b, l + 1);
    p = r; // pin = 2
    r = r && GeneratedParserUtilBase.consumeToken(b, WebAssemblyTypes.RPAR);
    GeneratedParserUtilBase.exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  /* ********************************************************** */
  // MEMORYKEY idx
  static boolean exportdesc_memory_aux_(PsiBuilder b, int l) {
    if (!GeneratedParserUtilBase.recursion_guard_(b, l, "exportdesc_memory_aux_")) return false;
    boolean r, p;
    Marker m = GeneratedParserUtilBase.enter_section_(b, l, GeneratedParserUtilBase._NONE_);
    r = GeneratedParserUtilBase.consumeToken(b, WebAssemblyTypes.MEMORYKEY);
    p = r; // pin = 1
    r = r && idx(b, l + 1);
    GeneratedParserUtilBase.exit_section_(b, l, m, r, p, WebAssemblyParser::item_recover_);
    return r || p;
  }

  /* ********************************************************** */
  // LPAR exportdesc_table_aux_ RPAR
  static boolean exportdesc_table_(PsiBuilder b, int l) {
    if (!GeneratedParserUtilBase.recursion_guard_(b, l, "exportdesc_table_")) return false;
    if (!GeneratedParserUtilBase.nextTokenIs(b, WebAssemblyTypes.LPAR)) return false;
    boolean r, p;
    Marker m = GeneratedParserUtilBase.enter_section_(b, l, GeneratedParserUtilBase._NONE_);
    r = GeneratedParserUtilBase.consumeToken(b, WebAssemblyTypes.LPAR);
    r = r && exportdesc_table_aux_(b, l + 1);
    p = r; // pin = 2
    r = r && GeneratedParserUtilBase.consumeToken(b, WebAssemblyTypes.RPAR);
    GeneratedParserUtilBase.exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  /* ********************************************************** */
  // TABLEKEY idx
  static boolean exportdesc_table_aux_(PsiBuilder b, int l) {
    if (!GeneratedParserUtilBase.recursion_guard_(b, l, "exportdesc_table_aux_")) return false;
    boolean r, p;
    Marker m = GeneratedParserUtilBase.enter_section_(b, l, GeneratedParserUtilBase._NONE_);
    r = GeneratedParserUtilBase.consumeToken(b, WebAssemblyTypes.TABLEKEY);
    p = r; // pin = 1
    r = r && idx(b, l + 1);
    GeneratedParserUtilBase.exit_section_(b, l, m, r, p, WebAssemblyParser::item_recover_);
    return r || p;
  }

  /* ********************************************************** */
  // foldeinstr_plaininstr_ | foldeinstr_block_ | foldeinstr_loop_ | foldeinstr_if_
  public static boolean foldeinstr(PsiBuilder b, int l) {
    if (!GeneratedParserUtilBase.recursion_guard_(b, l, "foldeinstr")) return false;
    if (!GeneratedParserUtilBase.nextTokenIs(b, WebAssemblyTypes.LPAR)) return false;
    boolean r;
    Marker m = GeneratedParserUtilBase.enter_section_(b);
    r = foldeinstr_plaininstr_(b, l + 1);
    if (!r) r = foldeinstr_block_(b, l + 1);
    if (!r) r = foldeinstr_loop_(b, l + 1);
    if (!r) r = foldeinstr_if_(b, l + 1);
    GeneratedParserUtilBase.exit_section_(b, m, WebAssemblyTypes.FOLDEINSTR, r);
    return r;
  }

  /* ********************************************************** */
  // LPAR foldeinstr_block_aux_ RPAR IDENTIFIER?
  static boolean foldeinstr_block_(PsiBuilder b, int l) {
    if (!GeneratedParserUtilBase.recursion_guard_(b, l, "foldeinstr_block_")) return false;
    if (!GeneratedParserUtilBase.nextTokenIs(b, WebAssemblyTypes.LPAR)) return false;
    boolean r, p;
    Marker m = GeneratedParserUtilBase.enter_section_(b, l, GeneratedParserUtilBase._NONE_);
    r = GeneratedParserUtilBase.consumeToken(b, WebAssemblyTypes.LPAR);
    r = r && foldeinstr_block_aux_(b, l + 1);
    p = r; // pin = 2
    r = r && GeneratedParserUtilBase.report_error_(b, GeneratedParserUtilBase.consumeToken(b, WebAssemblyTypes.RPAR));
    r = p && foldeinstr_block__3(b, l + 1) && r;
    GeneratedParserUtilBase.exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  // IDENTIFIER?
  private static boolean foldeinstr_block__3(PsiBuilder b, int l) {
    if (!GeneratedParserUtilBase.recursion_guard_(b, l, "foldeinstr_block__3")) return false;
    GeneratedParserUtilBase.consumeToken(b, WebAssemblyTypes.IDENTIFIER);
    return true;
  }

  /* ********************************************************** */
  // BLOCKKEY IDENTIFIER? blocktype? instr*
  static boolean foldeinstr_block_aux_(PsiBuilder b, int l) {
    if (!GeneratedParserUtilBase.recursion_guard_(b, l, "foldeinstr_block_aux_")) return false;
    boolean r, p;
    Marker m = GeneratedParserUtilBase.enter_section_(b, l, GeneratedParserUtilBase._NONE_);
    r = GeneratedParserUtilBase.consumeToken(b, WebAssemblyTypes.BLOCKKEY);
    p = r; // pin = 1
    r = r && GeneratedParserUtilBase.report_error_(b, foldeinstr_block_aux__1(b, l + 1));
    r = p && GeneratedParserUtilBase.report_error_(b, foldeinstr_block_aux__2(b, l + 1)) && r;
    r = p && foldeinstr_block_aux__3(b, l + 1) && r;
    GeneratedParserUtilBase.exit_section_(b, l, m, r, p, WebAssemblyParser::item_recover_);
    return r || p;
  }

  // IDENTIFIER?
  private static boolean foldeinstr_block_aux__1(PsiBuilder b, int l) {
    if (!GeneratedParserUtilBase.recursion_guard_(b, l, "foldeinstr_block_aux__1")) return false;
    GeneratedParserUtilBase.consumeToken(b, WebAssemblyTypes.IDENTIFIER);
    return true;
  }

  // blocktype?
  private static boolean foldeinstr_block_aux__2(PsiBuilder b, int l) {
    if (!GeneratedParserUtilBase.recursion_guard_(b, l, "foldeinstr_block_aux__2")) return false;
    blocktype(b, l + 1);
    return true;
  }

  // instr*
  private static boolean foldeinstr_block_aux__3(PsiBuilder b, int l) {
    if (!GeneratedParserUtilBase.recursion_guard_(b, l, "foldeinstr_block_aux__3")) return false;
    while (true) {
      int c = GeneratedParserUtilBase.current_position_(b);
      if (!instr(b, l + 1)) break;
      if (!GeneratedParserUtilBase.empty_element_parsed_guard_(b, "foldeinstr_block_aux__3", c)) break;
    }
    return true;
  }

  /* ********************************************************** */
  // LPAR foldeinstr_if_aux_ RPAR
  static boolean foldeinstr_if_(PsiBuilder b, int l) {
    if (!GeneratedParserUtilBase.recursion_guard_(b, l, "foldeinstr_if_")) return false;
    if (!GeneratedParserUtilBase.nextTokenIs(b, WebAssemblyTypes.LPAR)) return false;
    boolean r, p;
    Marker m = GeneratedParserUtilBase.enter_section_(b, l, GeneratedParserUtilBase._NONE_);
    r = GeneratedParserUtilBase.consumeToken(b, WebAssemblyTypes.LPAR);
    r = r && foldeinstr_if_aux_(b, l + 1);
    p = r; // pin = 2
    r = r && GeneratedParserUtilBase.consumeToken(b, WebAssemblyTypes.RPAR);
    GeneratedParserUtilBase.exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  /* ********************************************************** */
  // IFKEY IDENTIFIER? blocktype? foldeinstr* foldeinstr_if_then_ foldeinstr_if_else_
  static boolean foldeinstr_if_aux_(PsiBuilder b, int l) {
    if (!GeneratedParserUtilBase.recursion_guard_(b, l, "foldeinstr_if_aux_")) return false;
    boolean r, p;
    Marker m = GeneratedParserUtilBase.enter_section_(b, l, GeneratedParserUtilBase._NONE_);
    r = GeneratedParserUtilBase.consumeToken(b, WebAssemblyTypes.IFKEY);
    p = r; // pin = 1
    r = r && GeneratedParserUtilBase.report_error_(b, foldeinstr_if_aux__1(b, l + 1));
    r = p && GeneratedParserUtilBase.report_error_(b, foldeinstr_if_aux__2(b, l + 1)) && r;
    r = p && GeneratedParserUtilBase.report_error_(b, foldeinstr_if_aux__3(b, l + 1)) && r;
    r = p && GeneratedParserUtilBase.report_error_(b, foldeinstr_if_then_(b, l + 1)) && r;
    r = p && foldeinstr_if_else_(b, l + 1) && r;
    GeneratedParserUtilBase.exit_section_(b, l, m, r, p, WebAssemblyParser::item_recover_);
    return r || p;
  }

  // IDENTIFIER?
  private static boolean foldeinstr_if_aux__1(PsiBuilder b, int l) {
    if (!GeneratedParserUtilBase.recursion_guard_(b, l, "foldeinstr_if_aux__1")) return false;
    GeneratedParserUtilBase.consumeToken(b, WebAssemblyTypes.IDENTIFIER);
    return true;
  }

  // blocktype?
  private static boolean foldeinstr_if_aux__2(PsiBuilder b, int l) {
    if (!GeneratedParserUtilBase.recursion_guard_(b, l, "foldeinstr_if_aux__2")) return false;
    blocktype(b, l + 1);
    return true;
  }

  // foldeinstr*
  private static boolean foldeinstr_if_aux__3(PsiBuilder b, int l) {
    if (!GeneratedParserUtilBase.recursion_guard_(b, l, "foldeinstr_if_aux__3")) return false;
    while (true) {
      int c = GeneratedParserUtilBase.current_position_(b);
      if (!foldeinstr(b, l + 1)) break;
      if (!GeneratedParserUtilBase.empty_element_parsed_guard_(b, "foldeinstr_if_aux__3", c)) break;
    }
    return true;
  }

  /* ********************************************************** */
  // LPAR foldeinstr_if_else_aux_ RPAR
  static boolean foldeinstr_if_else_(PsiBuilder b, int l) {
    if (!GeneratedParserUtilBase.recursion_guard_(b, l, "foldeinstr_if_else_")) return false;
    if (!GeneratedParserUtilBase.nextTokenIs(b, WebAssemblyTypes.LPAR)) return false;
    boolean r, p;
    Marker m = GeneratedParserUtilBase.enter_section_(b, l, GeneratedParserUtilBase._NONE_);
    r = GeneratedParserUtilBase.consumeToken(b, WebAssemblyTypes.LPAR);
    r = r && foldeinstr_if_else_aux_(b, l + 1);
    p = r; // pin = 2
    r = r && GeneratedParserUtilBase.consumeToken(b, WebAssemblyTypes.RPAR);
    GeneratedParserUtilBase.exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  /* ********************************************************** */
  // ELSEKEY instr*
  static boolean foldeinstr_if_else_aux_(PsiBuilder b, int l) {
    if (!GeneratedParserUtilBase.recursion_guard_(b, l, "foldeinstr_if_else_aux_")) return false;
    boolean r, p;
    Marker m = GeneratedParserUtilBase.enter_section_(b, l, GeneratedParserUtilBase._NONE_);
    r = GeneratedParserUtilBase.consumeToken(b, WebAssemblyTypes.ELSEKEY);
    p = r; // pin = 1
    r = r && foldeinstr_if_else_aux__1(b, l + 1);
    GeneratedParserUtilBase.exit_section_(b, l, m, r, p, WebAssemblyParser::item_recover_);
    return r || p;
  }

  // instr*
  private static boolean foldeinstr_if_else_aux__1(PsiBuilder b, int l) {
    if (!GeneratedParserUtilBase.recursion_guard_(b, l, "foldeinstr_if_else_aux__1")) return false;
    while (true) {
      int c = GeneratedParserUtilBase.current_position_(b);
      if (!instr(b, l + 1)) break;
      if (!GeneratedParserUtilBase.empty_element_parsed_guard_(b, "foldeinstr_if_else_aux__1", c)) break;
    }
    return true;
  }

  /* ********************************************************** */
  // LPAR foldeinstr_if_then_aux_ RPAR
  static boolean foldeinstr_if_then_(PsiBuilder b, int l) {
    if (!GeneratedParserUtilBase.recursion_guard_(b, l, "foldeinstr_if_then_")) return false;
    if (!GeneratedParserUtilBase.nextTokenIs(b, WebAssemblyTypes.LPAR)) return false;
    boolean r, p;
    Marker m = GeneratedParserUtilBase.enter_section_(b, l, GeneratedParserUtilBase._NONE_);
    r = GeneratedParserUtilBase.consumeToken(b, WebAssemblyTypes.LPAR);
    r = r && foldeinstr_if_then_aux_(b, l + 1);
    p = r; // pin = 2
    r = r && GeneratedParserUtilBase.consumeToken(b, WebAssemblyTypes.RPAR);
    GeneratedParserUtilBase.exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  /* ********************************************************** */
  // THENKEY instr*
  static boolean foldeinstr_if_then_aux_(PsiBuilder b, int l) {
    if (!GeneratedParserUtilBase.recursion_guard_(b, l, "foldeinstr_if_then_aux_")) return false;
    boolean r, p;
    Marker m = GeneratedParserUtilBase.enter_section_(b, l, GeneratedParserUtilBase._NONE_);
    r = GeneratedParserUtilBase.consumeToken(b, WebAssemblyTypes.THENKEY);
    p = r; // pin = 1
    r = r && foldeinstr_if_then_aux__1(b, l + 1);
    GeneratedParserUtilBase.exit_section_(b, l, m, r, p, WebAssemblyParser::item_recover_);
    return r || p;
  }

  // instr*
  private static boolean foldeinstr_if_then_aux__1(PsiBuilder b, int l) {
    if (!GeneratedParserUtilBase.recursion_guard_(b, l, "foldeinstr_if_then_aux__1")) return false;
    while (true) {
      int c = GeneratedParserUtilBase.current_position_(b);
      if (!instr(b, l + 1)) break;
      if (!GeneratedParserUtilBase.empty_element_parsed_guard_(b, "foldeinstr_if_then_aux__1", c)) break;
    }
    return true;
  }

  /* ********************************************************** */
  // LPAR foldeinstr_loop_aux_ RPAR IDENTIFIER?
  static boolean foldeinstr_loop_(PsiBuilder b, int l) {
    if (!GeneratedParserUtilBase.recursion_guard_(b, l, "foldeinstr_loop_")) return false;
    if (!GeneratedParserUtilBase.nextTokenIs(b, WebAssemblyTypes.LPAR)) return false;
    boolean r, p;
    Marker m = GeneratedParserUtilBase.enter_section_(b, l, GeneratedParserUtilBase._NONE_);
    r = GeneratedParserUtilBase.consumeToken(b, WebAssemblyTypes.LPAR);
    r = r && foldeinstr_loop_aux_(b, l + 1);
    p = r; // pin = 2
    r = r && GeneratedParserUtilBase.report_error_(b, GeneratedParserUtilBase.consumeToken(b, WebAssemblyTypes.RPAR));
    r = p && foldeinstr_loop__3(b, l + 1) && r;
    GeneratedParserUtilBase.exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  // IDENTIFIER?
  private static boolean foldeinstr_loop__3(PsiBuilder b, int l) {
    if (!GeneratedParserUtilBase.recursion_guard_(b, l, "foldeinstr_loop__3")) return false;
    GeneratedParserUtilBase.consumeToken(b, WebAssemblyTypes.IDENTIFIER);
    return true;
  }

  /* ********************************************************** */
  // LOOPKEY IDENTIFIER? blocktype? instr*
  static boolean foldeinstr_loop_aux_(PsiBuilder b, int l) {
    if (!GeneratedParserUtilBase.recursion_guard_(b, l, "foldeinstr_loop_aux_")) return false;
    boolean r, p;
    Marker m = GeneratedParserUtilBase.enter_section_(b, l, GeneratedParserUtilBase._NONE_);
    r = GeneratedParserUtilBase.consumeToken(b, WebAssemblyTypes.LOOPKEY);
    p = r; // pin = 1
    r = r && GeneratedParserUtilBase.report_error_(b, foldeinstr_loop_aux__1(b, l + 1));
    r = p && GeneratedParserUtilBase.report_error_(b, foldeinstr_loop_aux__2(b, l + 1)) && r;
    r = p && foldeinstr_loop_aux__3(b, l + 1) && r;
    GeneratedParserUtilBase.exit_section_(b, l, m, r, p, WebAssemblyParser::item_recover_);
    return r || p;
  }

  // IDENTIFIER?
  private static boolean foldeinstr_loop_aux__1(PsiBuilder b, int l) {
    if (!GeneratedParserUtilBase.recursion_guard_(b, l, "foldeinstr_loop_aux__1")) return false;
    GeneratedParserUtilBase.consumeToken(b, WebAssemblyTypes.IDENTIFIER);
    return true;
  }

  // blocktype?
  private static boolean foldeinstr_loop_aux__2(PsiBuilder b, int l) {
    if (!GeneratedParserUtilBase.recursion_guard_(b, l, "foldeinstr_loop_aux__2")) return false;
    blocktype(b, l + 1);
    return true;
  }

  // instr*
  private static boolean foldeinstr_loop_aux__3(PsiBuilder b, int l) {
    if (!GeneratedParserUtilBase.recursion_guard_(b, l, "foldeinstr_loop_aux__3")) return false;
    while (true) {
      int c = GeneratedParserUtilBase.current_position_(b);
      if (!instr(b, l + 1)) break;
      if (!GeneratedParserUtilBase.empty_element_parsed_guard_(b, "foldeinstr_loop_aux__3", c)) break;
    }
    return true;
  }

  /* ********************************************************** */
  // LPAR foldeinstr_plaininstr_aux_ RPAR
  static boolean foldeinstr_plaininstr_(PsiBuilder b, int l) {
    if (!GeneratedParserUtilBase.recursion_guard_(b, l, "foldeinstr_plaininstr_")) return false;
    if (!GeneratedParserUtilBase.nextTokenIs(b, WebAssemblyTypes.LPAR)) return false;
    boolean r, p;
    Marker m = GeneratedParserUtilBase.enter_section_(b, l, GeneratedParserUtilBase._NONE_);
    r = GeneratedParserUtilBase.consumeToken(b, WebAssemblyTypes.LPAR);
    r = r && foldeinstr_plaininstr_aux_(b, l + 1);
    p = r; // pin = 2
    r = r && GeneratedParserUtilBase.consumeToken(b, WebAssemblyTypes.RPAR);
    GeneratedParserUtilBase.exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  /* ********************************************************** */
  // plaininstr foldeinstr*
  static boolean foldeinstr_plaininstr_aux_(PsiBuilder b, int l) {
    if (!GeneratedParserUtilBase.recursion_guard_(b, l, "foldeinstr_plaininstr_aux_")) return false;
    boolean r, p;
    Marker m = GeneratedParserUtilBase.enter_section_(b, l, GeneratedParserUtilBase._NONE_);
    r = plaininstr(b, l + 1);
    p = r; // pin = 1
    r = r && foldeinstr_plaininstr_aux__1(b, l + 1);
    GeneratedParserUtilBase.exit_section_(b, l, m, r, p, WebAssemblyParser::item_recover_);
    return r || p;
  }

  // foldeinstr*
  private static boolean foldeinstr_plaininstr_aux__1(PsiBuilder b, int l) {
    if (!GeneratedParserUtilBase.recursion_guard_(b, l, "foldeinstr_plaininstr_aux__1")) return false;
    while (true) {
      int c = GeneratedParserUtilBase.current_position_(b);
      if (!foldeinstr(b, l + 1)) break;
      if (!GeneratedParserUtilBase.empty_element_parsed_guard_(b, "foldeinstr_plaininstr_aux__1", c)) break;
    }
    return true;
  }

  /* ********************************************************** */
  // LPAR func_aux_ RPAR
  public static boolean func(PsiBuilder b, int l) {
    if (!GeneratedParserUtilBase.recursion_guard_(b, l, "func")) return false;
    if (!GeneratedParserUtilBase.nextTokenIs(b, WebAssemblyTypes.LPAR)) return false;
    boolean r, p;
    Marker m = GeneratedParserUtilBase.enter_section_(b, l, GeneratedParserUtilBase._NONE_, WebAssemblyTypes.FUNC, null);
    r = GeneratedParserUtilBase.consumeToken(b, WebAssemblyTypes.LPAR);
    r = r && func_aux_(b, l + 1);
    p = r; // pin = 2
    r = r && GeneratedParserUtilBase.consumeToken(b, WebAssemblyTypes.RPAR);
    GeneratedParserUtilBase.exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  /* ********************************************************** */
  // FUNCKEY func_aux_ident_? (
  //                          inline_import typeuse?
  //                        | (inline_export (inline_export | inline_import)?)? typeuse? local* instr*
  //                       )
  static boolean func_aux_(PsiBuilder b, int l) {
    if (!GeneratedParserUtilBase.recursion_guard_(b, l, "func_aux_")) return false;
    boolean r, p;
    Marker m = GeneratedParserUtilBase.enter_section_(b, l, GeneratedParserUtilBase._NONE_);
    r = GeneratedParserUtilBase.consumeToken(b, WebAssemblyTypes.FUNCKEY);
    p = r; // pin = 1
    r = r && GeneratedParserUtilBase.report_error_(b, func_aux__1(b, l + 1));
    r = p && func_aux__2(b, l + 1) && r;
    GeneratedParserUtilBase.exit_section_(b, l, m, r, p, WebAssemblyParser::item_recover_);
    return r || p;
  }

  // func_aux_ident_?
  private static boolean func_aux__1(PsiBuilder b, int l) {
    if (!GeneratedParserUtilBase.recursion_guard_(b, l, "func_aux__1")) return false;
    func_aux_ident_(b, l + 1);
    return true;
  }

  // inline_import typeuse?
  //                        | (inline_export (inline_export | inline_import)?)? typeuse? local* instr*
  private static boolean func_aux__2(PsiBuilder b, int l) {
    if (!GeneratedParserUtilBase.recursion_guard_(b, l, "func_aux__2")) return false;
    boolean r;
    Marker m = GeneratedParserUtilBase.enter_section_(b);
    r = func_aux__2_0(b, l + 1);
    if (!r) r = func_aux__2_1(b, l + 1);
    GeneratedParserUtilBase.exit_section_(b, m, null, r);
    return r;
  }

  // inline_import typeuse?
  private static boolean func_aux__2_0(PsiBuilder b, int l) {
    if (!GeneratedParserUtilBase.recursion_guard_(b, l, "func_aux__2_0")) return false;
    boolean r;
    Marker m = GeneratedParserUtilBase.enter_section_(b);
    r = inline_import(b, l + 1);
    r = r && func_aux__2_0_1(b, l + 1);
    GeneratedParserUtilBase.exit_section_(b, m, null, r);
    return r;
  }

  // typeuse?
  private static boolean func_aux__2_0_1(PsiBuilder b, int l) {
    if (!GeneratedParserUtilBase.recursion_guard_(b, l, "func_aux__2_0_1")) return false;
    typeuse(b, l + 1);
    return true;
  }

  // (inline_export (inline_export | inline_import)?)? typeuse? local* instr*
  private static boolean func_aux__2_1(PsiBuilder b, int l) {
    if (!GeneratedParserUtilBase.recursion_guard_(b, l, "func_aux__2_1")) return false;
    boolean r;
    Marker m = GeneratedParserUtilBase.enter_section_(b);
    r = func_aux__2_1_0(b, l + 1);
    r = r && func_aux__2_1_1(b, l + 1);
    r = r && func_aux__2_1_2(b, l + 1);
    r = r && func_aux__2_1_3(b, l + 1);
    GeneratedParserUtilBase.exit_section_(b, m, null, r);
    return r;
  }

  // (inline_export (inline_export | inline_import)?)?
  private static boolean func_aux__2_1_0(PsiBuilder b, int l) {
    if (!GeneratedParserUtilBase.recursion_guard_(b, l, "func_aux__2_1_0")) return false;
    func_aux__2_1_0_0(b, l + 1);
    return true;
  }

  // inline_export (inline_export | inline_import)?
  private static boolean func_aux__2_1_0_0(PsiBuilder b, int l) {
    if (!GeneratedParserUtilBase.recursion_guard_(b, l, "func_aux__2_1_0_0")) return false;
    boolean r;
    Marker m = GeneratedParserUtilBase.enter_section_(b);
    r = inline_export(b, l + 1);
    r = r && func_aux__2_1_0_0_1(b, l + 1);
    GeneratedParserUtilBase.exit_section_(b, m, null, r);
    return r;
  }

  // (inline_export | inline_import)?
  private static boolean func_aux__2_1_0_0_1(PsiBuilder b, int l) {
    if (!GeneratedParserUtilBase.recursion_guard_(b, l, "func_aux__2_1_0_0_1")) return false;
    func_aux__2_1_0_0_1_0(b, l + 1);
    return true;
  }

  // inline_export | inline_import
  private static boolean func_aux__2_1_0_0_1_0(PsiBuilder b, int l) {
    if (!GeneratedParserUtilBase.recursion_guard_(b, l, "func_aux__2_1_0_0_1_0")) return false;
    boolean r;
    r = inline_export(b, l + 1);
    if (!r) r = inline_import(b, l + 1);
    return r;
  }

  // typeuse?
  private static boolean func_aux__2_1_1(PsiBuilder b, int l) {
    if (!GeneratedParserUtilBase.recursion_guard_(b, l, "func_aux__2_1_1")) return false;
    typeuse(b, l + 1);
    return true;
  }

  // local*
  private static boolean func_aux__2_1_2(PsiBuilder b, int l) {
    if (!GeneratedParserUtilBase.recursion_guard_(b, l, "func_aux__2_1_2")) return false;
    while (true) {
      int c = GeneratedParserUtilBase.current_position_(b);
      if (!local(b, l + 1)) break;
      if (!GeneratedParserUtilBase.empty_element_parsed_guard_(b, "func_aux__2_1_2", c)) break;
    }
    return true;
  }

  // instr*
  private static boolean func_aux__2_1_3(PsiBuilder b, int l) {
    if (!GeneratedParserUtilBase.recursion_guard_(b, l, "func_aux__2_1_3")) return false;
    while (true) {
      int c = GeneratedParserUtilBase.current_position_(b);
      if (!instr(b, l + 1)) break;
      if (!GeneratedParserUtilBase.empty_element_parsed_guard_(b, "func_aux__2_1_3", c)) break;
    }
    return true;
  }

  /* ********************************************************** */
  // IDENTIFIER
  static boolean func_aux_ident_(PsiBuilder b, int l) {
    if (!GeneratedParserUtilBase.recursion_guard_(b, l, "func_aux_ident_")) return false;
    boolean r;
    Marker m = GeneratedParserUtilBase.enter_section_(b, l, GeneratedParserUtilBase._NONE_);
    r = GeneratedParserUtilBase.consumeToken(b, WebAssemblyTypes.IDENTIFIER);
    GeneratedParserUtilBase.exit_section_(b, l, m, r, false, WebAssemblyParser::func_aux_ident_recover_);
    return r;
  }

  /* ********************************************************** */
  // !(LPAR | RPAR | instr_key_ | <<eof>>)
  static boolean func_aux_ident_recover_(PsiBuilder b, int l) {
    if (!GeneratedParserUtilBase.recursion_guard_(b, l, "func_aux_ident_recover_")) return false;
    boolean r;
    Marker m = GeneratedParserUtilBase.enter_section_(b, l, GeneratedParserUtilBase._NOT_);
    r = !func_aux_ident_recover__0(b, l + 1);
    GeneratedParserUtilBase.exit_section_(b, l, m, r, false, null);
    return r;
  }

  // LPAR | RPAR | instr_key_ | <<eof>>
  private static boolean func_aux_ident_recover__0(PsiBuilder b, int l) {
    if (!GeneratedParserUtilBase.recursion_guard_(b, l, "func_aux_ident_recover__0")) return false;
    boolean r;
    Marker m = GeneratedParserUtilBase.enter_section_(b);
    r = GeneratedParserUtilBase.consumeToken(b, WebAssemblyTypes.LPAR);
    if (!r) r = GeneratedParserUtilBase.consumeToken(b, WebAssemblyTypes.RPAR);
    if (!r) r = instr_key_(b, l + 1);
    if (!r) r = GeneratedParserUtilBase.eof(b, l + 1);
    GeneratedParserUtilBase.exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // LPAR FUNCKEY param* result* RPAR
  public static boolean functype(PsiBuilder b, int l) {
    if (!GeneratedParserUtilBase.recursion_guard_(b, l, "functype")) return false;
    if (!GeneratedParserUtilBase.nextTokenIs(b, WebAssemblyTypes.LPAR)) return false;
    boolean r, p;
    Marker m = GeneratedParserUtilBase.enter_section_(b, l, GeneratedParserUtilBase._NONE_, WebAssemblyTypes.FUNCTYPE, null);
    r = GeneratedParserUtilBase.consumeTokens(b, 2, WebAssemblyTypes.LPAR, WebAssemblyTypes.FUNCKEY);
    p = r; // pin = 2
    r = r && GeneratedParserUtilBase.report_error_(b, functype_2(b, l + 1));
    r = p && GeneratedParserUtilBase.report_error_(b, functype_3(b, l + 1)) && r;
    r = p && GeneratedParserUtilBase.consumeToken(b, WebAssemblyTypes.RPAR) && r;
    GeneratedParserUtilBase.exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  // param*
  private static boolean functype_2(PsiBuilder b, int l) {
    if (!GeneratedParserUtilBase.recursion_guard_(b, l, "functype_2")) return false;
    while (true) {
      int c = GeneratedParserUtilBase.current_position_(b);
      if (!param(b, l + 1)) break;
      if (!GeneratedParserUtilBase.empty_element_parsed_guard_(b, "functype_2", c)) break;
    }
    return true;
  }

  // result*
  private static boolean functype_3(PsiBuilder b, int l) {
    if (!GeneratedParserUtilBase.recursion_guard_(b, l, "functype_3")) return false;
    while (true) {
      int c = GeneratedParserUtilBase.current_position_(b);
      if (!result(b, l + 1)) break;
      if (!GeneratedParserUtilBase.empty_element_parsed_guard_(b, "functype_3", c)) break;
    }
    return true;
  }

  /* ********************************************************** */
  // LPAR global_aux_ RPAR
  public static boolean global(PsiBuilder b, int l) {
    if (!GeneratedParserUtilBase.recursion_guard_(b, l, "global")) return false;
    if (!GeneratedParserUtilBase.nextTokenIs(b, WebAssemblyTypes.LPAR)) return false;
    boolean r, p;
    Marker m = GeneratedParserUtilBase.enter_section_(b, l, GeneratedParserUtilBase._NONE_, WebAssemblyTypes.GLOBAL, null);
    r = GeneratedParserUtilBase.consumeToken(b, WebAssemblyTypes.LPAR);
    r = r && global_aux_(b, l + 1);
    p = r; // pin = 2
    r = r && GeneratedParserUtilBase.consumeToken(b, WebAssemblyTypes.RPAR);
    GeneratedParserUtilBase.exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  /* ********************************************************** */
  // GLOBALKEY IDENTIFIER? (
  //                            inline_import globaltype
  //                          | (inline_export (inline_import | inline_export)?)? globaltype instr*
  //                         )
  static boolean global_aux_(PsiBuilder b, int l) {
    if (!GeneratedParserUtilBase.recursion_guard_(b, l, "global_aux_")) return false;
    boolean r, p;
    Marker m = GeneratedParserUtilBase.enter_section_(b, l, GeneratedParserUtilBase._NONE_);
    r = GeneratedParserUtilBase.consumeToken(b, WebAssemblyTypes.GLOBALKEY);
    p = r; // pin = 1
    r = r && GeneratedParserUtilBase.report_error_(b, global_aux__1(b, l + 1));
    r = p && global_aux__2(b, l + 1) && r;
    GeneratedParserUtilBase.exit_section_(b, l, m, r, p, WebAssemblyParser::item_recover_);
    return r || p;
  }

  // IDENTIFIER?
  private static boolean global_aux__1(PsiBuilder b, int l) {
    if (!GeneratedParserUtilBase.recursion_guard_(b, l, "global_aux__1")) return false;
    GeneratedParserUtilBase.consumeToken(b, WebAssemblyTypes.IDENTIFIER);
    return true;
  }

  // inline_import globaltype
  //                          | (inline_export (inline_import | inline_export)?)? globaltype instr*
  private static boolean global_aux__2(PsiBuilder b, int l) {
    if (!GeneratedParserUtilBase.recursion_guard_(b, l, "global_aux__2")) return false;
    boolean r;
    Marker m = GeneratedParserUtilBase.enter_section_(b);
    r = global_aux__2_0(b, l + 1);
    if (!r) r = global_aux__2_1(b, l + 1);
    GeneratedParserUtilBase.exit_section_(b, m, null, r);
    return r;
  }

  // inline_import globaltype
  private static boolean global_aux__2_0(PsiBuilder b, int l) {
    if (!GeneratedParserUtilBase.recursion_guard_(b, l, "global_aux__2_0")) return false;
    boolean r;
    Marker m = GeneratedParserUtilBase.enter_section_(b);
    r = inline_import(b, l + 1);
    r = r && globaltype(b, l + 1);
    GeneratedParserUtilBase.exit_section_(b, m, null, r);
    return r;
  }

  // (inline_export (inline_import | inline_export)?)? globaltype instr*
  private static boolean global_aux__2_1(PsiBuilder b, int l) {
    if (!GeneratedParserUtilBase.recursion_guard_(b, l, "global_aux__2_1")) return false;
    boolean r;
    Marker m = GeneratedParserUtilBase.enter_section_(b);
    r = global_aux__2_1_0(b, l + 1);
    r = r && globaltype(b, l + 1);
    r = r && global_aux__2_1_2(b, l + 1);
    GeneratedParserUtilBase.exit_section_(b, m, null, r);
    return r;
  }

  // (inline_export (inline_import | inline_export)?)?
  private static boolean global_aux__2_1_0(PsiBuilder b, int l) {
    if (!GeneratedParserUtilBase.recursion_guard_(b, l, "global_aux__2_1_0")) return false;
    global_aux__2_1_0_0(b, l + 1);
    return true;
  }

  // inline_export (inline_import | inline_export)?
  private static boolean global_aux__2_1_0_0(PsiBuilder b, int l) {
    if (!GeneratedParserUtilBase.recursion_guard_(b, l, "global_aux__2_1_0_0")) return false;
    boolean r;
    Marker m = GeneratedParserUtilBase.enter_section_(b);
    r = inline_export(b, l + 1);
    r = r && global_aux__2_1_0_0_1(b, l + 1);
    GeneratedParserUtilBase.exit_section_(b, m, null, r);
    return r;
  }

  // (inline_import | inline_export)?
  private static boolean global_aux__2_1_0_0_1(PsiBuilder b, int l) {
    if (!GeneratedParserUtilBase.recursion_guard_(b, l, "global_aux__2_1_0_0_1")) return false;
    global_aux__2_1_0_0_1_0(b, l + 1);
    return true;
  }

  // inline_import | inline_export
  private static boolean global_aux__2_1_0_0_1_0(PsiBuilder b, int l) {
    if (!GeneratedParserUtilBase.recursion_guard_(b, l, "global_aux__2_1_0_0_1_0")) return false;
    boolean r;
    r = inline_import(b, l + 1);
    if (!r) r = inline_export(b, l + 1);
    return r;
  }

  // instr*
  private static boolean global_aux__2_1_2(PsiBuilder b, int l) {
    if (!GeneratedParserUtilBase.recursion_guard_(b, l, "global_aux__2_1_2")) return false;
    while (true) {
      int c = GeneratedParserUtilBase.current_position_(b);
      if (!instr(b, l + 1)) break;
      if (!GeneratedParserUtilBase.empty_element_parsed_guard_(b, "global_aux__2_1_2", c)) break;
    }
    return true;
  }

  /* ********************************************************** */
  // GLOBALINSTR idx
  public static boolean global_instr(PsiBuilder b, int l) {
    if (!GeneratedParserUtilBase.recursion_guard_(b, l, "global_instr")) return false;
    if (!GeneratedParserUtilBase.nextTokenIs(b, WebAssemblyTypes.GLOBALINSTR)) return false;
    boolean r, p;
    Marker m = GeneratedParserUtilBase.enter_section_(b, l, GeneratedParserUtilBase._NONE_, WebAssemblyTypes.GLOBAL_INSTR, null);
    r = GeneratedParserUtilBase.consumeToken(b, WebAssemblyTypes.GLOBALINSTR);
    p = r; // pin = 1
    r = r && idx(b, l + 1);
    GeneratedParserUtilBase.exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  /* ********************************************************** */
  // valtype | globaltype_mut_
  public static boolean globaltype(PsiBuilder b, int l) {
    if (!GeneratedParserUtilBase.recursion_guard_(b, l, "globaltype")) return false;
    boolean r;
    Marker m = GeneratedParserUtilBase.enter_section_(b, l, GeneratedParserUtilBase._NONE_, WebAssemblyTypes.GLOBALTYPE, "<globaltype>");
    r = valtype(b, l + 1);
    if (!r) r = globaltype_mut_(b, l + 1);
    GeneratedParserUtilBase.exit_section_(b, l, m, r, false, null);
    return r;
  }

  /* ********************************************************** */
  // LPAR MUTKEY valtype RPAR
  static boolean globaltype_mut_(PsiBuilder b, int l) {
    if (!GeneratedParserUtilBase.recursion_guard_(b, l, "globaltype_mut_")) return false;
    if (!GeneratedParserUtilBase.nextTokenIs(b, WebAssemblyTypes.LPAR)) return false;
    boolean r, p;
    Marker m = GeneratedParserUtilBase.enter_section_(b, l, GeneratedParserUtilBase._NONE_);
    r = GeneratedParserUtilBase.consumeTokens(b, 2, WebAssemblyTypes.LPAR, WebAssemblyTypes.MUTKEY);
    p = r; // pin = 2
    r = r && GeneratedParserUtilBase.report_error_(b, valtype(b, l + 1));
    r = p && GeneratedParserUtilBase.consumeToken(b, WebAssemblyTypes.RPAR) && r;
    GeneratedParserUtilBase.exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  /* ********************************************************** */
  // UNSIGNED | IDENTIFIER
  public static boolean idx(PsiBuilder b, int l) {
    if (!GeneratedParserUtilBase.recursion_guard_(b, l, "idx")) return false;
    if (!GeneratedParserUtilBase.nextTokenIs(b, "<idx>", WebAssemblyTypes.IDENTIFIER, WebAssemblyTypes.UNSIGNED)) return false;
    boolean r;
    Marker m = GeneratedParserUtilBase.enter_section_(b, l, GeneratedParserUtilBase._NONE_, WebAssemblyTypes.IDX, "<idx>");
    r = GeneratedParserUtilBase.consumeToken(b, WebAssemblyTypes.UNSIGNED);
    if (!r) r = GeneratedParserUtilBase.consumeToken(b, WebAssemblyTypes.IDENTIFIER);
    GeneratedParserUtilBase.exit_section_(b, l, m, r, false, null);
    return r;
  }

  /* ********************************************************** */
  // LPAR import_aux_ RPAR
  public static boolean import_$(PsiBuilder b, int l) {
    if (!GeneratedParserUtilBase.recursion_guard_(b, l, "import_$")) return false;
    if (!GeneratedParserUtilBase.nextTokenIs(b, WebAssemblyTypes.LPAR)) return false;
    boolean r, p;
    Marker m = GeneratedParserUtilBase.enter_section_(b, l, GeneratedParserUtilBase._NONE_, WebAssemblyTypes.IMPORT, null);
    r = GeneratedParserUtilBase.consumeToken(b, WebAssemblyTypes.LPAR);
    r = r && import_aux_(b, l + 1);
    p = r; // pin = 2
    r = r && GeneratedParserUtilBase.consumeToken(b, WebAssemblyTypes.RPAR);
    GeneratedParserUtilBase.exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  /* ********************************************************** */
  // IMPORTKEY string_fir_aux_ string_aux_ importdesc
  static boolean import_aux_(PsiBuilder b, int l) {
    if (!GeneratedParserUtilBase.recursion_guard_(b, l, "import_aux_")) return false;
    boolean r, p;
    Marker m = GeneratedParserUtilBase.enter_section_(b, l, GeneratedParserUtilBase._NONE_);
    r = GeneratedParserUtilBase.consumeToken(b, WebAssemblyTypes.IMPORTKEY);
    p = r; // pin = 1
    r = r && GeneratedParserUtilBase.report_error_(b, string_fir_aux_(b, l + 1));
    r = p && GeneratedParserUtilBase.report_error_(b, string_aux_(b, l + 1)) && r;
    r = p && importdesc(b, l + 1) && r;
    GeneratedParserUtilBase.exit_section_(b, l, m, r, p, WebAssemblyParser::item_recover_);
    return r || p;
  }

  /* ********************************************************** */
  // importdesc_func_ | importdesc_table_ | importdesc_memory_ | importdesc_global_
  public static boolean importdesc(PsiBuilder b, int l) {
    if (!GeneratedParserUtilBase.recursion_guard_(b, l, "importdesc")) return false;
    if (!GeneratedParserUtilBase.nextTokenIs(b, WebAssemblyTypes.LPAR)) return false;
    boolean r;
    Marker m = GeneratedParserUtilBase.enter_section_(b);
    r = importdesc_func_(b, l + 1);
    if (!r) r = importdesc_table_(b, l + 1);
    if (!r) r = importdesc_memory_(b, l + 1);
    if (!r) r = importdesc_global_(b, l + 1);
    GeneratedParserUtilBase.exit_section_(b, m, WebAssemblyTypes.IMPORTDESC, r);
    return r;
  }

  /* ********************************************************** */
  // LPAR importdesc_func_aux_ RPAR
  static boolean importdesc_func_(PsiBuilder b, int l) {
    if (!GeneratedParserUtilBase.recursion_guard_(b, l, "importdesc_func_")) return false;
    if (!GeneratedParserUtilBase.nextTokenIs(b, WebAssemblyTypes.LPAR)) return false;
    boolean r, p;
    Marker m = GeneratedParserUtilBase.enter_section_(b, l, GeneratedParserUtilBase._NONE_);
    r = GeneratedParserUtilBase.consumeToken(b, WebAssemblyTypes.LPAR);
    r = r && importdesc_func_aux_(b, l + 1);
    p = r; // pin = 2
    r = r && GeneratedParserUtilBase.consumeToken(b, WebAssemblyTypes.RPAR);
    GeneratedParserUtilBase.exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  /* ********************************************************** */
  // FUNCKEY IDENTIFIER? typeuse?
  static boolean importdesc_func_aux_(PsiBuilder b, int l) {
    if (!GeneratedParserUtilBase.recursion_guard_(b, l, "importdesc_func_aux_")) return false;
    boolean r, p;
    Marker m = GeneratedParserUtilBase.enter_section_(b, l, GeneratedParserUtilBase._NONE_);
    r = GeneratedParserUtilBase.consumeToken(b, WebAssemblyTypes.FUNCKEY);
    p = r; // pin = 1
    r = r && GeneratedParserUtilBase.report_error_(b, importdesc_func_aux__1(b, l + 1));
    r = p && importdesc_func_aux__2(b, l + 1) && r;
    GeneratedParserUtilBase.exit_section_(b, l, m, r, p, WebAssemblyParser::item_recover_);
    return r || p;
  }

  // IDENTIFIER?
  private static boolean importdesc_func_aux__1(PsiBuilder b, int l) {
    if (!GeneratedParserUtilBase.recursion_guard_(b, l, "importdesc_func_aux__1")) return false;
    GeneratedParserUtilBase.consumeToken(b, WebAssemblyTypes.IDENTIFIER);
    return true;
  }

  // typeuse?
  private static boolean importdesc_func_aux__2(PsiBuilder b, int l) {
    if (!GeneratedParserUtilBase.recursion_guard_(b, l, "importdesc_func_aux__2")) return false;
    typeuse(b, l + 1);
    return true;
  }

  /* ********************************************************** */
  // LPAR importdesc_global_aux_ RPAR
  static boolean importdesc_global_(PsiBuilder b, int l) {
    if (!GeneratedParserUtilBase.recursion_guard_(b, l, "importdesc_global_")) return false;
    if (!GeneratedParserUtilBase.nextTokenIs(b, WebAssemblyTypes.LPAR)) return false;
    boolean r, p;
    Marker m = GeneratedParserUtilBase.enter_section_(b, l, GeneratedParserUtilBase._NONE_);
    r = GeneratedParserUtilBase.consumeToken(b, WebAssemblyTypes.LPAR);
    r = r && importdesc_global_aux_(b, l + 1);
    p = r; // pin = 2
    r = r && GeneratedParserUtilBase.consumeToken(b, WebAssemblyTypes.RPAR);
    GeneratedParserUtilBase.exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  /* ********************************************************** */
  // GLOBALKEY IDENTIFIER? globaltype
  static boolean importdesc_global_aux_(PsiBuilder b, int l) {
    if (!GeneratedParserUtilBase.recursion_guard_(b, l, "importdesc_global_aux_")) return false;
    boolean r, p;
    Marker m = GeneratedParserUtilBase.enter_section_(b, l, GeneratedParserUtilBase._NONE_);
    r = GeneratedParserUtilBase.consumeToken(b, WebAssemblyTypes.GLOBALKEY);
    p = r; // pin = 1
    r = r && GeneratedParserUtilBase.report_error_(b, importdesc_global_aux__1(b, l + 1));
    r = p && globaltype(b, l + 1) && r;
    GeneratedParserUtilBase.exit_section_(b, l, m, r, p, WebAssemblyParser::item_recover_);
    return r || p;
  }

  // IDENTIFIER?
  private static boolean importdesc_global_aux__1(PsiBuilder b, int l) {
    if (!GeneratedParserUtilBase.recursion_guard_(b, l, "importdesc_global_aux__1")) return false;
    GeneratedParserUtilBase.consumeToken(b, WebAssemblyTypes.IDENTIFIER);
    return true;
  }

  /* ********************************************************** */
  // LPAR importdesc_memory_aux_ RPAR
  static boolean importdesc_memory_(PsiBuilder b, int l) {
    if (!GeneratedParserUtilBase.recursion_guard_(b, l, "importdesc_memory_")) return false;
    if (!GeneratedParserUtilBase.nextTokenIs(b, WebAssemblyTypes.LPAR)) return false;
    boolean r, p;
    Marker m = GeneratedParserUtilBase.enter_section_(b, l, GeneratedParserUtilBase._NONE_);
    r = GeneratedParserUtilBase.consumeToken(b, WebAssemblyTypes.LPAR);
    r = r && importdesc_memory_aux_(b, l + 1);
    p = r; // pin = 2
    r = r && GeneratedParserUtilBase.consumeToken(b, WebAssemblyTypes.RPAR);
    GeneratedParserUtilBase.exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  /* ********************************************************** */
  // MEMORYKEY IDENTIFIER? memtype
  static boolean importdesc_memory_aux_(PsiBuilder b, int l) {
    if (!GeneratedParserUtilBase.recursion_guard_(b, l, "importdesc_memory_aux_")) return false;
    boolean r, p;
    Marker m = GeneratedParserUtilBase.enter_section_(b, l, GeneratedParserUtilBase._NONE_);
    r = GeneratedParserUtilBase.consumeToken(b, WebAssemblyTypes.MEMORYKEY);
    p = r; // pin = 1
    r = r && GeneratedParserUtilBase.report_error_(b, importdesc_memory_aux__1(b, l + 1));
    r = p && memtype(b, l + 1) && r;
    GeneratedParserUtilBase.exit_section_(b, l, m, r, p, WebAssemblyParser::item_recover_);
    return r || p;
  }

  // IDENTIFIER?
  private static boolean importdesc_memory_aux__1(PsiBuilder b, int l) {
    if (!GeneratedParserUtilBase.recursion_guard_(b, l, "importdesc_memory_aux__1")) return false;
    GeneratedParserUtilBase.consumeToken(b, WebAssemblyTypes.IDENTIFIER);
    return true;
  }

  /* ********************************************************** */
  // LPAR importdesc_table_aux_ RPAR
  static boolean importdesc_table_(PsiBuilder b, int l) {
    if (!GeneratedParserUtilBase.recursion_guard_(b, l, "importdesc_table_")) return false;
    if (!GeneratedParserUtilBase.nextTokenIs(b, WebAssemblyTypes.LPAR)) return false;
    boolean r, p;
    Marker m = GeneratedParserUtilBase.enter_section_(b, l, GeneratedParserUtilBase._NONE_);
    r = GeneratedParserUtilBase.consumeToken(b, WebAssemblyTypes.LPAR);
    r = r && importdesc_table_aux_(b, l + 1);
    p = r; // pin = 2
    r = r && GeneratedParserUtilBase.consumeToken(b, WebAssemblyTypes.RPAR);
    GeneratedParserUtilBase.exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  /* ********************************************************** */
  // TABLEKEY IDENTIFIER? tabletype
  static boolean importdesc_table_aux_(PsiBuilder b, int l) {
    if (!GeneratedParserUtilBase.recursion_guard_(b, l, "importdesc_table_aux_")) return false;
    boolean r, p;
    Marker m = GeneratedParserUtilBase.enter_section_(b, l, GeneratedParserUtilBase._NONE_);
    r = GeneratedParserUtilBase.consumeToken(b, WebAssemblyTypes.TABLEKEY);
    p = r; // pin = 1
    r = r && GeneratedParserUtilBase.report_error_(b, importdesc_table_aux__1(b, l + 1));
    r = p && tabletype(b, l + 1) && r;
    GeneratedParserUtilBase.exit_section_(b, l, m, r, p, WebAssemblyParser::item_recover_);
    return r || p;
  }

  // IDENTIFIER?
  private static boolean importdesc_table_aux__1(PsiBuilder b, int l) {
    if (!GeneratedParserUtilBase.recursion_guard_(b, l, "importdesc_table_aux__1")) return false;
    GeneratedParserUtilBase.consumeToken(b, WebAssemblyTypes.IDENTIFIER);
    return true;
  }

  /* ********************************************************** */
  // LPAR inline_data_aux_ RPAR
  public static boolean inline_data(PsiBuilder b, int l) {
    if (!GeneratedParserUtilBase.recursion_guard_(b, l, "inline_data")) return false;
    if (!GeneratedParserUtilBase.nextTokenIs(b, WebAssemblyTypes.LPAR)) return false;
    boolean r, p;
    Marker m = GeneratedParserUtilBase.enter_section_(b, l, GeneratedParserUtilBase._NONE_, WebAssemblyTypes.INLINE_DATA, null);
    r = GeneratedParserUtilBase.consumeToken(b, WebAssemblyTypes.LPAR);
    r = r && inline_data_aux_(b, l + 1);
    p = r; // pin = 2
    r = r && GeneratedParserUtilBase.consumeToken(b, WebAssemblyTypes.RPAR);
    GeneratedParserUtilBase.exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  /* ********************************************************** */
  // DATAKEY STRING*
  static boolean inline_data_aux_(PsiBuilder b, int l) {
    if (!GeneratedParserUtilBase.recursion_guard_(b, l, "inline_data_aux_")) return false;
    boolean r, p;
    Marker m = GeneratedParserUtilBase.enter_section_(b, l, GeneratedParserUtilBase._NONE_);
    r = GeneratedParserUtilBase.consumeToken(b, WebAssemblyTypes.DATAKEY);
    p = r; // pin = 1
    r = r && inline_data_aux__1(b, l + 1);
    GeneratedParserUtilBase.exit_section_(b, l, m, r, p, WebAssemblyParser::item_recover_);
    return r || p;
  }

  // STRING*
  private static boolean inline_data_aux__1(PsiBuilder b, int l) {
    if (!GeneratedParserUtilBase.recursion_guard_(b, l, "inline_data_aux__1")) return false;
    while (true) {
      int c = GeneratedParserUtilBase.current_position_(b);
      if (!GeneratedParserUtilBase.consumeToken(b, WebAssemblyTypes.STRING)) break;
      if (!GeneratedParserUtilBase.empty_element_parsed_guard_(b, "inline_data_aux__1", c)) break;
    }
    return true;
  }

  /* ********************************************************** */
  // REFTYPE LPAR inline_elem_aux_ RPAR
  public static boolean inline_elem(PsiBuilder b, int l) {
    if (!GeneratedParserUtilBase.recursion_guard_(b, l, "inline_elem")) return false;
    if (!GeneratedParserUtilBase.nextTokenIs(b, WebAssemblyTypes.REFTYPE)) return false;
    boolean r, p;
    Marker m = GeneratedParserUtilBase.enter_section_(b, l, GeneratedParserUtilBase._NONE_, WebAssemblyTypes.INLINE_ELEM, null);
    r = GeneratedParserUtilBase.consumeTokens(b, 0, WebAssemblyTypes.REFTYPE, WebAssemblyTypes.LPAR);
    r = r && inline_elem_aux_(b, l + 1);
    p = r; // pin = 3
    r = r && GeneratedParserUtilBase.consumeToken(b, WebAssemblyTypes.RPAR);
    GeneratedParserUtilBase.exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  /* ********************************************************** */
  // ELEMKEY (instr+ | elemlist)?
  static boolean inline_elem_aux_(PsiBuilder b, int l) {
    if (!GeneratedParserUtilBase.recursion_guard_(b, l, "inline_elem_aux_")) return false;
    boolean r, p;
    Marker m = GeneratedParserUtilBase.enter_section_(b, l, GeneratedParserUtilBase._NONE_);
    r = GeneratedParserUtilBase.consumeToken(b, WebAssemblyTypes.ELEMKEY);
    p = r; // pin = 1
    r = r && inline_elem_aux__1(b, l + 1);
    GeneratedParserUtilBase.exit_section_(b, l, m, r, p, WebAssemblyParser::item_recover_);
    return r || p;
  }

  // (instr+ | elemlist)?
  private static boolean inline_elem_aux__1(PsiBuilder b, int l) {
    if (!GeneratedParserUtilBase.recursion_guard_(b, l, "inline_elem_aux__1")) return false;
    inline_elem_aux__1_0(b, l + 1);
    return true;
  }

  // instr+ | elemlist
  private static boolean inline_elem_aux__1_0(PsiBuilder b, int l) {
    if (!GeneratedParserUtilBase.recursion_guard_(b, l, "inline_elem_aux__1_0")) return false;
    boolean r;
    Marker m = GeneratedParserUtilBase.enter_section_(b);
    r = inline_elem_aux__1_0_0(b, l + 1);
    if (!r) r = elemlist(b, l + 1);
    GeneratedParserUtilBase.exit_section_(b, m, null, r);
    return r;
  }

  // instr+
  private static boolean inline_elem_aux__1_0_0(PsiBuilder b, int l) {
    if (!GeneratedParserUtilBase.recursion_guard_(b, l, "inline_elem_aux__1_0_0")) return false;
    boolean r;
    Marker m = GeneratedParserUtilBase.enter_section_(b);
    r = instr(b, l + 1);
    while (r) {
      int c = GeneratedParserUtilBase.current_position_(b);
      if (!instr(b, l + 1)) break;
      if (!GeneratedParserUtilBase.empty_element_parsed_guard_(b, "inline_elem_aux__1_0_0", c)) break;
    }
    GeneratedParserUtilBase.exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // LPAR EXPORTKEY STRING RPAR
  public static boolean inline_export(PsiBuilder b, int l) {
    if (!GeneratedParserUtilBase.recursion_guard_(b, l, "inline_export")) return false;
    if (!GeneratedParserUtilBase.nextTokenIs(b, WebAssemblyTypes.LPAR)) return false;
    boolean r, p;
    Marker m = GeneratedParserUtilBase.enter_section_(b, l, GeneratedParserUtilBase._NONE_, WebAssemblyTypes.INLINE_EXPORT, null);
    r = GeneratedParserUtilBase.consumeTokens(b, 2, WebAssemblyTypes.LPAR, WebAssemblyTypes.EXPORTKEY, WebAssemblyTypes.STRING, WebAssemblyTypes.RPAR);
    p = r; // pin = 2
    GeneratedParserUtilBase.exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  /* ********************************************************** */
  // LPAR IMPORTKEY STRING STRING RPAR
  public static boolean inline_import(PsiBuilder b, int l) {
    if (!GeneratedParserUtilBase.recursion_guard_(b, l, "inline_import")) return false;
    if (!GeneratedParserUtilBase.nextTokenIs(b, WebAssemblyTypes.LPAR)) return false;
    boolean r;
    Marker m = GeneratedParserUtilBase.enter_section_(b);
    r = GeneratedParserUtilBase.consumeTokens(b, 0, WebAssemblyTypes.LPAR, WebAssemblyTypes.IMPORTKEY, WebAssemblyTypes.STRING, WebAssemblyTypes.STRING, WebAssemblyTypes.RPAR);
    GeneratedParserUtilBase.exit_section_(b, m, WebAssemblyTypes.INLINE_IMPORT, r);
    return r;
  }

  /* ********************************************************** */
  // foldeinstr | plaininstr | blockinstr
  public static boolean instr(PsiBuilder b, int l) {
    if (!GeneratedParserUtilBase.recursion_guard_(b, l, "instr")) return false;
    boolean r;
    Marker m = GeneratedParserUtilBase.enter_section_(b, l, GeneratedParserUtilBase._NONE_, WebAssemblyTypes.INSTR, "<instr>");
    r = foldeinstr(b, l + 1);
    if (!r) r = plaininstr(b, l + 1);
    if (!r) r = blockinstr(b, l + 1);
    GeneratedParserUtilBase.exit_section_(b, l, m, r, false, null);
    return r;
  }

  /* ********************************************************** */
  // BLOCKKEY | LOOPKEY | IFKEY
  //                      | CONTROLINSTR | CONTROLINSTR_IDX | CALLINSTR | BRTABLEINSTR | CALLINDIRECTINSTR
  //                      | REFISNULLINST | REFNULLINSTR | REFFUNCINSTR
  //                      | PARAMETRICINSTR
  //                      | LOCALINSTR | GLOBALINSTR
  //                      | TABLEINSTR_IDX | TABLECOPYINSTR | TABLEINITINSTR | ELEMDROPINSTR
  //                      | MEMORYINSTR | MEMORYINSTR_IDX | MEMORYINSTR_MEMARG
  //                      | ICONST | FCONST | NUMERICINSTR
  static boolean instr_key_(PsiBuilder b, int l) {
    if (!GeneratedParserUtilBase.recursion_guard_(b, l, "instr_key_")) return false;
    boolean r;
    r = GeneratedParserUtilBase.consumeToken(b, WebAssemblyTypes.BLOCKKEY);
    if (!r) r = GeneratedParserUtilBase.consumeToken(b, WebAssemblyTypes.LOOPKEY);
    if (!r) r = GeneratedParserUtilBase.consumeToken(b, WebAssemblyTypes.IFKEY);
    if (!r) r = GeneratedParserUtilBase.consumeToken(b, WebAssemblyTypes.CONTROLINSTR);
    if (!r) r = GeneratedParserUtilBase.consumeToken(b, WebAssemblyTypes.CONTROLINSTR_IDX);
    if (!r) r = GeneratedParserUtilBase.consumeToken(b, WebAssemblyTypes.CALLINSTR);
    if (!r) r = GeneratedParserUtilBase.consumeToken(b, WebAssemblyTypes.BRTABLEINSTR);
    if (!r) r = GeneratedParserUtilBase.consumeToken(b, WebAssemblyTypes.CALLINDIRECTINSTR);
    if (!r) r = GeneratedParserUtilBase.consumeToken(b, WebAssemblyTypes.REFISNULLINST);
    if (!r) r = GeneratedParserUtilBase.consumeToken(b, WebAssemblyTypes.REFNULLINSTR);
    if (!r) r = GeneratedParserUtilBase.consumeToken(b, WebAssemblyTypes.REFFUNCINSTR);
    if (!r) r = GeneratedParserUtilBase.consumeToken(b, WebAssemblyTypes.PARAMETRICINSTR);
    if (!r) r = GeneratedParserUtilBase.consumeToken(b, WebAssemblyTypes.LOCALINSTR);
    if (!r) r = GeneratedParserUtilBase.consumeToken(b, WebAssemblyTypes.GLOBALINSTR);
    if (!r) r = GeneratedParserUtilBase.consumeToken(b, WebAssemblyTypes.TABLEINSTR_IDX);
    if (!r) r = GeneratedParserUtilBase.consumeToken(b, WebAssemblyTypes.TABLECOPYINSTR);
    if (!r) r = GeneratedParserUtilBase.consumeToken(b, WebAssemblyTypes.TABLEINITINSTR);
    if (!r) r = GeneratedParserUtilBase.consumeToken(b, WebAssemblyTypes.ELEMDROPINSTR);
    if (!r) r = GeneratedParserUtilBase.consumeToken(b, WebAssemblyTypes.MEMORYINSTR);
    if (!r) r = GeneratedParserUtilBase.consumeToken(b, WebAssemblyTypes.MEMORYINSTR_IDX);
    if (!r) r = GeneratedParserUtilBase.consumeToken(b, WebAssemblyTypes.MEMORYINSTR_MEMARG);
    if (!r) r = GeneratedParserUtilBase.consumeToken(b, WebAssemblyTypes.ICONST);
    if (!r) r = GeneratedParserUtilBase.consumeToken(b, WebAssemblyTypes.FCONST);
    if (!r) r = GeneratedParserUtilBase.consumeToken(b, WebAssemblyTypes.NUMERICINSTR);
    return r;
  }

  /* ********************************************************** */
  // module | modulefield
  static boolean item_(PsiBuilder b, int l) {
    if (!GeneratedParserUtilBase.recursion_guard_(b, l, "item_")) return false;
    if (!GeneratedParserUtilBase.nextTokenIs(b, WebAssemblyTypes.LPAR)) return false;
    boolean r;
    r = module(b, l + 1);
    if (!r) r = modulefield(b, l + 1);
    return r;
  }

  /* ********************************************************** */
  // !(RPAR | <<eof>>)
  static boolean item_recover_(PsiBuilder b, int l) {
    if (!GeneratedParserUtilBase.recursion_guard_(b, l, "item_recover_")) return false;
    boolean r;
    Marker m = GeneratedParserUtilBase.enter_section_(b, l, GeneratedParserUtilBase._NOT_);
    r = !item_recover__0(b, l + 1);
    GeneratedParserUtilBase.exit_section_(b, l, m, r, false, null);
    return r;
  }

  // RPAR | <<eof>>
  private static boolean item_recover__0(PsiBuilder b, int l) {
    if (!GeneratedParserUtilBase.recursion_guard_(b, l, "item_recover__0")) return false;
    boolean r;
    Marker m = GeneratedParserUtilBase.enter_section_(b);
    r = GeneratedParserUtilBase.consumeToken(b, WebAssemblyTypes.RPAR);
    if (!r) r = GeneratedParserUtilBase.eof(b, l + 1);
    GeneratedParserUtilBase.exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // BAD_TOKEN
  public static boolean lexer_tokens(PsiBuilder b, int l) {
    if (!GeneratedParserUtilBase.recursion_guard_(b, l, "lexer_tokens")) return false;
    if (!GeneratedParserUtilBase.nextTokenIs(b, WebAssemblyTypes.BAD_TOKEN)) return false;
    boolean r;
    Marker m = GeneratedParserUtilBase.enter_section_(b);
    r = GeneratedParserUtilBase.consumeToken(b, WebAssemblyTypes.BAD_TOKEN);
    GeneratedParserUtilBase.exit_section_(b, m, WebAssemblyTypes.LEXER_TOKENS, r);
    return r;
  }

  /* ********************************************************** */
  // LPAR local_aux_ RPAR
  public static boolean local(PsiBuilder b, int l) {
    if (!GeneratedParserUtilBase.recursion_guard_(b, l, "local")) return false;
    if (!GeneratedParserUtilBase.nextTokenIs(b, WebAssemblyTypes.LPAR)) return false;
    boolean r, p;
    Marker m = GeneratedParserUtilBase.enter_section_(b, l, GeneratedParserUtilBase._NONE_, WebAssemblyTypes.LOCAL, null);
    r = GeneratedParserUtilBase.consumeToken(b, WebAssemblyTypes.LPAR);
    r = r && local_aux_(b, l + 1);
    p = r; // pin = 2
    r = r && GeneratedParserUtilBase.consumeToken(b, WebAssemblyTypes.RPAR);
    GeneratedParserUtilBase.exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  /* ********************************************************** */
  // LOCALKEY (IDENTIFIER valtype | valtype*)
  static boolean local_aux_(PsiBuilder b, int l) {
    if (!GeneratedParserUtilBase.recursion_guard_(b, l, "local_aux_")) return false;
    boolean r, p;
    Marker m = GeneratedParserUtilBase.enter_section_(b, l, GeneratedParserUtilBase._NONE_);
    r = GeneratedParserUtilBase.consumeToken(b, WebAssemblyTypes.LOCALKEY);
    p = r; // pin = 1
    r = r && local_aux__1(b, l + 1);
    GeneratedParserUtilBase.exit_section_(b, l, m, r, p, WebAssemblyParser::item_recover_);
    return r || p;
  }

  // IDENTIFIER valtype | valtype*
  private static boolean local_aux__1(PsiBuilder b, int l) {
    if (!GeneratedParserUtilBase.recursion_guard_(b, l, "local_aux__1")) return false;
    boolean r;
    Marker m = GeneratedParserUtilBase.enter_section_(b);
    r = local_aux__1_0(b, l + 1);
    if (!r) r = local_aux__1_1(b, l + 1);
    GeneratedParserUtilBase.exit_section_(b, m, null, r);
    return r;
  }

  // IDENTIFIER valtype
  private static boolean local_aux__1_0(PsiBuilder b, int l) {
    if (!GeneratedParserUtilBase.recursion_guard_(b, l, "local_aux__1_0")) return false;
    boolean r;
    Marker m = GeneratedParserUtilBase.enter_section_(b);
    r = GeneratedParserUtilBase.consumeToken(b, WebAssemblyTypes.IDENTIFIER);
    r = r && valtype(b, l + 1);
    GeneratedParserUtilBase.exit_section_(b, m, null, r);
    return r;
  }

  // valtype*
  private static boolean local_aux__1_1(PsiBuilder b, int l) {
    if (!GeneratedParserUtilBase.recursion_guard_(b, l, "local_aux__1_1")) return false;
    while (true) {
      int c = GeneratedParserUtilBase.current_position_(b);
      if (!valtype(b, l + 1)) break;
      if (!GeneratedParserUtilBase.empty_element_parsed_guard_(b, "local_aux__1_1", c)) break;
    }
    return true;
  }

  /* ********************************************************** */
  // LOCALINSTR idx
  public static boolean local_instr(PsiBuilder b, int l) {
    if (!GeneratedParserUtilBase.recursion_guard_(b, l, "local_instr")) return false;
    if (!GeneratedParserUtilBase.nextTokenIs(b, WebAssemblyTypes.LOCALINSTR)) return false;
    boolean r, p;
    Marker m = GeneratedParserUtilBase.enter_section_(b, l, GeneratedParserUtilBase._NONE_, WebAssemblyTypes.LOCAL_INSTR, null);
    r = GeneratedParserUtilBase.consumeToken(b, WebAssemblyTypes.LOCALINSTR);
    p = r; // pin = 1
    r = r && idx(b, l + 1);
    GeneratedParserUtilBase.exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  /* ********************************************************** */
  // LPAR memaux_ RPAR
  public static boolean mem(PsiBuilder b, int l) {
    if (!GeneratedParserUtilBase.recursion_guard_(b, l, "mem")) return false;
    if (!GeneratedParserUtilBase.nextTokenIs(b, WebAssemblyTypes.LPAR)) return false;
    boolean r, p;
    Marker m = GeneratedParserUtilBase.enter_section_(b, l, GeneratedParserUtilBase._NONE_, WebAssemblyTypes.MEM, null);
    r = GeneratedParserUtilBase.consumeToken(b, WebAssemblyTypes.LPAR);
    r = r && memaux_(b, l + 1);
    p = r; // pin = 2
    r = r && GeneratedParserUtilBase.consumeToken(b, WebAssemblyTypes.RPAR);
    GeneratedParserUtilBase.exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  /* ********************************************************** */
  // MEMORYKEY IDENTIFIER? (
  //                        inline_data
  //                      | inline_import? memtype
  //                      | inline_export (inline_import | inline_export | inline_data)* memtype
  //                     )
  static boolean memaux_(PsiBuilder b, int l) {
    if (!GeneratedParserUtilBase.recursion_guard_(b, l, "memaux_")) return false;
    boolean r, p;
    Marker m = GeneratedParserUtilBase.enter_section_(b, l, GeneratedParserUtilBase._NONE_);
    r = GeneratedParserUtilBase.consumeToken(b, WebAssemblyTypes.MEMORYKEY);
    p = r; // pin = 1
    r = r && GeneratedParserUtilBase.report_error_(b, memaux__1(b, l + 1));
    r = p && memaux__2(b, l + 1) && r;
    GeneratedParserUtilBase.exit_section_(b, l, m, r, p, WebAssemblyParser::item_recover_);
    return r || p;
  }

  // IDENTIFIER?
  private static boolean memaux__1(PsiBuilder b, int l) {
    if (!GeneratedParserUtilBase.recursion_guard_(b, l, "memaux__1")) return false;
    GeneratedParserUtilBase.consumeToken(b, WebAssemblyTypes.IDENTIFIER);
    return true;
  }

  // inline_data
  //                      | inline_import? memtype
  //                      | inline_export (inline_import | inline_export | inline_data)* memtype
  private static boolean memaux__2(PsiBuilder b, int l) {
    if (!GeneratedParserUtilBase.recursion_guard_(b, l, "memaux__2")) return false;
    boolean r;
    Marker m = GeneratedParserUtilBase.enter_section_(b);
    r = inline_data(b, l + 1);
    if (!r) r = memaux__2_1(b, l + 1);
    if (!r) r = memaux__2_2(b, l + 1);
    GeneratedParserUtilBase.exit_section_(b, m, null, r);
    return r;
  }

  // inline_import? memtype
  private static boolean memaux__2_1(PsiBuilder b, int l) {
    if (!GeneratedParserUtilBase.recursion_guard_(b, l, "memaux__2_1")) return false;
    boolean r;
    Marker m = GeneratedParserUtilBase.enter_section_(b);
    r = memaux__2_1_0(b, l + 1);
    r = r && memtype(b, l + 1);
    GeneratedParserUtilBase.exit_section_(b, m, null, r);
    return r;
  }

  // inline_import?
  private static boolean memaux__2_1_0(PsiBuilder b, int l) {
    if (!GeneratedParserUtilBase.recursion_guard_(b, l, "memaux__2_1_0")) return false;
    inline_import(b, l + 1);
    return true;
  }

  // inline_export (inline_import | inline_export | inline_data)* memtype
  private static boolean memaux__2_2(PsiBuilder b, int l) {
    if (!GeneratedParserUtilBase.recursion_guard_(b, l, "memaux__2_2")) return false;
    boolean r;
    Marker m = GeneratedParserUtilBase.enter_section_(b);
    r = inline_export(b, l + 1);
    r = r && memaux__2_2_1(b, l + 1);
    r = r && memtype(b, l + 1);
    GeneratedParserUtilBase.exit_section_(b, m, null, r);
    return r;
  }

  // (inline_import | inline_export | inline_data)*
  private static boolean memaux__2_2_1(PsiBuilder b, int l) {
    if (!GeneratedParserUtilBase.recursion_guard_(b, l, "memaux__2_2_1")) return false;
    while (true) {
      int c = GeneratedParserUtilBase.current_position_(b);
      if (!memaux__2_2_1_0(b, l + 1)) break;
      if (!GeneratedParserUtilBase.empty_element_parsed_guard_(b, "memaux__2_2_1", c)) break;
    }
    return true;
  }

  // inline_import | inline_export | inline_data
  private static boolean memaux__2_2_1_0(PsiBuilder b, int l) {
    if (!GeneratedParserUtilBase.recursion_guard_(b, l, "memaux__2_2_1_0")) return false;
    boolean r;
    r = inline_import(b, l + 1);
    if (!r) r = inline_export(b, l + 1);
    if (!r) r = inline_data(b, l + 1);
    return r;
  }

  /* ********************************************************** */
  // MEMORYINSTR_IDX idx
  public static boolean memory_idx_instr(PsiBuilder b, int l) {
    if (!GeneratedParserUtilBase.recursion_guard_(b, l, "memory_idx_instr")) return false;
    if (!GeneratedParserUtilBase.nextTokenIs(b, WebAssemblyTypes.MEMORYINSTR_IDX)) return false;
    boolean r, p;
    Marker m = GeneratedParserUtilBase.enter_section_(b, l, GeneratedParserUtilBase._NONE_, WebAssemblyTypes.MEMORY_IDX_INSTR, null);
    r = GeneratedParserUtilBase.consumeToken(b, WebAssemblyTypes.MEMORYINSTR_IDX);
    p = r; // pin = 1
    r = r && idx(b, l + 1);
    GeneratedParserUtilBase.exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  /* ********************************************************** */
  // UNSIGNED UNSIGNED?
  public static boolean memtype(PsiBuilder b, int l) {
    if (!GeneratedParserUtilBase.recursion_guard_(b, l, "memtype")) return false;
    if (!GeneratedParserUtilBase.nextTokenIs(b, WebAssemblyTypes.UNSIGNED)) return false;
    boolean r;
    Marker m = GeneratedParserUtilBase.enter_section_(b);
    r = GeneratedParserUtilBase.consumeToken(b, WebAssemblyTypes.UNSIGNED);
    r = r && memtype_1(b, l + 1);
    GeneratedParserUtilBase.exit_section_(b, m, WebAssemblyTypes.MEMTYPE, r);
    return r;
  }

  // UNSIGNED?
  private static boolean memtype_1(PsiBuilder b, int l) {
    if (!GeneratedParserUtilBase.recursion_guard_(b, l, "memtype_1")) return false;
    GeneratedParserUtilBase.consumeToken(b, WebAssemblyTypes.UNSIGNED);
    return true;
  }

  /* ********************************************************** */
  // LPAR MEMORYKEY idx RPAR
  static boolean memuse_(PsiBuilder b, int l) {
    if (!GeneratedParserUtilBase.recursion_guard_(b, l, "memuse_")) return false;
    if (!GeneratedParserUtilBase.nextTokenIs(b, WebAssemblyTypes.LPAR)) return false;
    boolean r, p;
    Marker m = GeneratedParserUtilBase.enter_section_(b, l, GeneratedParserUtilBase._NONE_);
    r = GeneratedParserUtilBase.consumeTokens(b, 2, WebAssemblyTypes.LPAR, WebAssemblyTypes.MEMORYKEY);
    p = r; // pin = 2
    r = r && GeneratedParserUtilBase.report_error_(b, idx(b, l + 1));
    r = p && GeneratedParserUtilBase.consumeToken(b, WebAssemblyTypes.RPAR) && r;
    GeneratedParserUtilBase.exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  /* ********************************************************** */
  // LPAR module_aux_ RPAR
  public static boolean module(PsiBuilder b, int l) {
    if (!GeneratedParserUtilBase.recursion_guard_(b, l, "module")) return false;
    if (!GeneratedParserUtilBase.nextTokenIs(b, WebAssemblyTypes.LPAR)) return false;
    boolean r, p;
    Marker m = GeneratedParserUtilBase.enter_section_(b, l, GeneratedParserUtilBase._NONE_, WebAssemblyTypes.MODULE, null);
    r = GeneratedParserUtilBase.consumeToken(b, WebAssemblyTypes.LPAR);
    r = r && module_aux_(b, l + 1);
    p = r; // pin = 2
    r = r && GeneratedParserUtilBase.consumeToken(b, WebAssemblyTypes.RPAR);
    GeneratedParserUtilBase.exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  /* ********************************************************** */
  // MODULEKEY IDENTIFIER? modulefield*
  static boolean module_aux_(PsiBuilder b, int l) {
    if (!GeneratedParserUtilBase.recursion_guard_(b, l, "module_aux_")) return false;
    boolean r, p;
    Marker m = GeneratedParserUtilBase.enter_section_(b, l, GeneratedParserUtilBase._NONE_);
    r = GeneratedParserUtilBase.consumeToken(b, WebAssemblyTypes.MODULEKEY);
    p = r; // pin = 1
    r = r && GeneratedParserUtilBase.report_error_(b, module_aux__1(b, l + 1));
    r = p && module_aux__2(b, l + 1) && r;
    GeneratedParserUtilBase.exit_section_(b, l, m, r, p, WebAssemblyParser::item_recover_);
    return r || p;
  }

  // IDENTIFIER?
  private static boolean module_aux__1(PsiBuilder b, int l) {
    if (!GeneratedParserUtilBase.recursion_guard_(b, l, "module_aux__1")) return false;
    GeneratedParserUtilBase.consumeToken(b, WebAssemblyTypes.IDENTIFIER);
    return true;
  }

  // modulefield*
  private static boolean module_aux__2(PsiBuilder b, int l) {
    if (!GeneratedParserUtilBase.recursion_guard_(b, l, "module_aux__2")) return false;
    while (true) {
      int c = GeneratedParserUtilBase.current_position_(b);
      if (!modulefield(b, l + 1)) break;
      if (!GeneratedParserUtilBase.empty_element_parsed_guard_(b, "module_aux__2", c)) break;
    }
    return true;
  }

  /* ********************************************************** */
  // type | import | func | table | mem | global | export | start | elem | data
  public static boolean modulefield(PsiBuilder b, int l) {
    if (!GeneratedParserUtilBase.recursion_guard_(b, l, "modulefield")) return false;
    if (!GeneratedParserUtilBase.nextTokenIs(b, WebAssemblyTypes.LPAR)) return false;
    boolean r;
    Marker m = GeneratedParserUtilBase.enter_section_(b);
    r = type(b, l + 1);
    if (!r) r = import_$(b, l + 1);
    if (!r) r = func(b, l + 1);
    if (!r) r = table(b, l + 1);
    if (!r) r = mem(b, l + 1);
    if (!r) r = global(b, l + 1);
    if (!r) r = export(b, l + 1);
    if (!r) r = start(b, l + 1);
    if (!r) r = elem(b, l + 1);
    if (!r) r = data(b, l + 1);
    GeneratedParserUtilBase.exit_section_(b, m, WebAssemblyTypes.MODULEFIELD, r);
    return r;
  }

  /* ********************************************************** */
  // OFFSETEQKEY UNSIGNED
  public static boolean offseteq(PsiBuilder b, int l) {
    if (!GeneratedParserUtilBase.recursion_guard_(b, l, "offseteq")) return false;
    boolean r, p;
    Marker m = GeneratedParserUtilBase.enter_section_(b, l, GeneratedParserUtilBase._NONE_, WebAssemblyTypes.OFFSETEQ, "<offseteq>");
    r = GeneratedParserUtilBase.consumeTokens(b, 1, WebAssemblyTypes.OFFSETEQKEY, WebAssemblyTypes.UNSIGNED);
    p = r; // pin = 1
    GeneratedParserUtilBase.exit_section_(b, l, m, r, p, WebAssemblyParser::offseteq_recover_);
    return r || p;
  }

  /* ********************************************************** */
  // !(aligneq | instr | RPAR | <<eof>>)
  static boolean offseteq_recover_(PsiBuilder b, int l) {
    if (!GeneratedParserUtilBase.recursion_guard_(b, l, "offseteq_recover_")) return false;
    boolean r;
    Marker m = GeneratedParserUtilBase.enter_section_(b, l, GeneratedParserUtilBase._NOT_);
    r = !offseteq_recover__0(b, l + 1);
    GeneratedParserUtilBase.exit_section_(b, l, m, r, false, null);
    return r;
  }

  // aligneq | instr | RPAR | <<eof>>
  private static boolean offseteq_recover__0(PsiBuilder b, int l) {
    if (!GeneratedParserUtilBase.recursion_guard_(b, l, "offseteq_recover__0")) return false;
    boolean r;
    Marker m = GeneratedParserUtilBase.enter_section_(b);
    r = aligneq(b, l + 1);
    if (!r) r = instr(b, l + 1);
    if (!r) r = GeneratedParserUtilBase.consumeToken(b, WebAssemblyTypes.RPAR);
    if (!r) r = GeneratedParserUtilBase.eof(b, l + 1);
    GeneratedParserUtilBase.exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // LPAR param_aux_ RPAR
  public static boolean param(PsiBuilder b, int l) {
    if (!GeneratedParserUtilBase.recursion_guard_(b, l, "param")) return false;
    if (!GeneratedParserUtilBase.nextTokenIs(b, WebAssemblyTypes.LPAR)) return false;
    boolean r, p;
    Marker m = GeneratedParserUtilBase.enter_section_(b, l, GeneratedParserUtilBase._NONE_, WebAssemblyTypes.PARAM, null);
    r = GeneratedParserUtilBase.consumeToken(b, WebAssemblyTypes.LPAR);
    r = r && param_aux_(b, l + 1);
    p = r; // pin = 2
    r = r && GeneratedParserUtilBase.consumeToken(b, WebAssemblyTypes.RPAR);
    GeneratedParserUtilBase.exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  /* ********************************************************** */
  // PARAMKEY (IDENTIFIER valtype | valtype*)
  static boolean param_aux_(PsiBuilder b, int l) {
    if (!GeneratedParserUtilBase.recursion_guard_(b, l, "param_aux_")) return false;
    boolean r, p;
    Marker m = GeneratedParserUtilBase.enter_section_(b, l, GeneratedParserUtilBase._NONE_);
    r = GeneratedParserUtilBase.consumeToken(b, WebAssemblyTypes.PARAMKEY);
    p = r; // pin = 1
    r = r && param_aux__1(b, l + 1);
    GeneratedParserUtilBase.exit_section_(b, l, m, r, p, WebAssemblyParser::item_recover_);
    return r || p;
  }

  // IDENTIFIER valtype | valtype*
  private static boolean param_aux__1(PsiBuilder b, int l) {
    if (!GeneratedParserUtilBase.recursion_guard_(b, l, "param_aux__1")) return false;
    boolean r;
    Marker m = GeneratedParserUtilBase.enter_section_(b);
    r = param_aux__1_0(b, l + 1);
    if (!r) r = param_aux__1_1(b, l + 1);
    GeneratedParserUtilBase.exit_section_(b, m, null, r);
    return r;
  }

  // IDENTIFIER valtype
  private static boolean param_aux__1_0(PsiBuilder b, int l) {
    if (!GeneratedParserUtilBase.recursion_guard_(b, l, "param_aux__1_0")) return false;
    boolean r;
    Marker m = GeneratedParserUtilBase.enter_section_(b);
    r = GeneratedParserUtilBase.consumeToken(b, WebAssemblyTypes.IDENTIFIER);
    r = r && valtype(b, l + 1);
    GeneratedParserUtilBase.exit_section_(b, m, null, r);
    return r;
  }

  // valtype*
  private static boolean param_aux__1_1(PsiBuilder b, int l) {
    if (!GeneratedParserUtilBase.recursion_guard_(b, l, "param_aux__1_1")) return false;
    while (true) {
      int c = GeneratedParserUtilBase.current_position_(b);
      if (!valtype(b, l + 1)) break;
      if (!GeneratedParserUtilBase.empty_element_parsed_guard_(b, "param_aux__1_1", c)) break;
    }
    return true;
  }

  /* ********************************************************** */
  // CONTROLINSTR | CONTROLINSTR_IDX idx | call_instr | BRTABLEINSTR idx+ | call_indirect_instr
  //              // reference
  //              | REFISNULLINST | REFNULLINSTR (FUNCKEY | EXTERNKEY) | ref_func_instr
  //              // parametric
  //              | PARAMETRICINSTR
  //              // variable
  //              | local_instr | global_instr
  //              // table
  //              | table_idx_instr | table_copy_instr | table_init_instr | elem_drop_instr
  //              // memory
  //              | MEMORYINSTR | memory_idx_instr | MEMORYINSTR_MEMARG offseteq? aligneq?
  //              // numeric
  //              | ICONST (UNSIGNED | SIGNED) | FCONST (FLOAT | UNSIGNED | SIGNED) | NUMERICINSTR
  public static boolean plaininstr(PsiBuilder b, int l) {
    if (!GeneratedParserUtilBase.recursion_guard_(b, l, "plaininstr")) return false;
    boolean r;
    Marker m = GeneratedParserUtilBase.enter_section_(b, l, GeneratedParserUtilBase._NONE_, WebAssemblyTypes.PLAININSTR, "<plaininstr>");
    r = GeneratedParserUtilBase.consumeToken(b, WebAssemblyTypes.CONTROLINSTR);
    if (!r) r = plaininstr_1(b, l + 1);
    if (!r) r = call_instr(b, l + 1);
    if (!r) r = plaininstr_3(b, l + 1);
    if (!r) r = call_indirect_instr(b, l + 1);
    if (!r) r = GeneratedParserUtilBase.consumeToken(b, WebAssemblyTypes.REFISNULLINST);
    if (!r) r = plaininstr_6(b, l + 1);
    if (!r) r = ref_func_instr(b, l + 1);
    if (!r) r = GeneratedParserUtilBase.consumeToken(b, WebAssemblyTypes.PARAMETRICINSTR);
    if (!r) r = local_instr(b, l + 1);
    if (!r) r = global_instr(b, l + 1);
    if (!r) r = table_idx_instr(b, l + 1);
    if (!r) r = table_copy_instr(b, l + 1);
    if (!r) r = table_init_instr(b, l + 1);
    if (!r) r = elem_drop_instr(b, l + 1);
    if (!r) r = GeneratedParserUtilBase.consumeToken(b, WebAssemblyTypes.MEMORYINSTR);
    if (!r) r = memory_idx_instr(b, l + 1);
    if (!r) r = plaininstr_17(b, l + 1);
    if (!r) r = plaininstr_18(b, l + 1);
    if (!r) r = plaininstr_19(b, l + 1);
    if (!r) r = GeneratedParserUtilBase.consumeToken(b, WebAssemblyTypes.NUMERICINSTR);
    GeneratedParserUtilBase.exit_section_(b, l, m, r, false, null);
    return r;
  }

  // CONTROLINSTR_IDX idx
  private static boolean plaininstr_1(PsiBuilder b, int l) {
    if (!GeneratedParserUtilBase.recursion_guard_(b, l, "plaininstr_1")) return false;
    boolean r;
    Marker m = GeneratedParserUtilBase.enter_section_(b);
    r = GeneratedParserUtilBase.consumeToken(b, WebAssemblyTypes.CONTROLINSTR_IDX);
    r = r && idx(b, l + 1);
    GeneratedParserUtilBase.exit_section_(b, m, null, r);
    return r;
  }

  // BRTABLEINSTR idx+
  private static boolean plaininstr_3(PsiBuilder b, int l) {
    if (!GeneratedParserUtilBase.recursion_guard_(b, l, "plaininstr_3")) return false;
    boolean r;
    Marker m = GeneratedParserUtilBase.enter_section_(b);
    r = GeneratedParserUtilBase.consumeToken(b, WebAssemblyTypes.BRTABLEINSTR);
    r = r && plaininstr_3_1(b, l + 1);
    GeneratedParserUtilBase.exit_section_(b, m, null, r);
    return r;
  }

  // idx+
  private static boolean plaininstr_3_1(PsiBuilder b, int l) {
    if (!GeneratedParserUtilBase.recursion_guard_(b, l, "plaininstr_3_1")) return false;
    boolean r;
    Marker m = GeneratedParserUtilBase.enter_section_(b);
    r = idx(b, l + 1);
    while (r) {
      int c = GeneratedParserUtilBase.current_position_(b);
      if (!idx(b, l + 1)) break;
      if (!GeneratedParserUtilBase.empty_element_parsed_guard_(b, "plaininstr_3_1", c)) break;
    }
    GeneratedParserUtilBase.exit_section_(b, m, null, r);
    return r;
  }

  // REFNULLINSTR (FUNCKEY | EXTERNKEY)
  private static boolean plaininstr_6(PsiBuilder b, int l) {
    if (!GeneratedParserUtilBase.recursion_guard_(b, l, "plaininstr_6")) return false;
    boolean r;
    Marker m = GeneratedParserUtilBase.enter_section_(b);
    r = GeneratedParserUtilBase.consumeToken(b, WebAssemblyTypes.REFNULLINSTR);
    r = r && plaininstr_6_1(b, l + 1);
    GeneratedParserUtilBase.exit_section_(b, m, null, r);
    return r;
  }

  // FUNCKEY | EXTERNKEY
  private static boolean plaininstr_6_1(PsiBuilder b, int l) {
    if (!GeneratedParserUtilBase.recursion_guard_(b, l, "plaininstr_6_1")) return false;
    boolean r;
    r = GeneratedParserUtilBase.consumeToken(b, WebAssemblyTypes.FUNCKEY);
    if (!r) r = GeneratedParserUtilBase.consumeToken(b, WebAssemblyTypes.EXTERNKEY);
    return r;
  }

  // MEMORYINSTR_MEMARG offseteq? aligneq?
  private static boolean plaininstr_17(PsiBuilder b, int l) {
    if (!GeneratedParserUtilBase.recursion_guard_(b, l, "plaininstr_17")) return false;
    boolean r;
    Marker m = GeneratedParserUtilBase.enter_section_(b);
    r = GeneratedParserUtilBase.consumeToken(b, WebAssemblyTypes.MEMORYINSTR_MEMARG);
    r = r && plaininstr_17_1(b, l + 1);
    r = r && plaininstr_17_2(b, l + 1);
    GeneratedParserUtilBase.exit_section_(b, m, null, r);
    return r;
  }

  // offseteq?
  private static boolean plaininstr_17_1(PsiBuilder b, int l) {
    if (!GeneratedParserUtilBase.recursion_guard_(b, l, "plaininstr_17_1")) return false;
    offseteq(b, l + 1);
    return true;
  }

  // aligneq?
  private static boolean plaininstr_17_2(PsiBuilder b, int l) {
    if (!GeneratedParserUtilBase.recursion_guard_(b, l, "plaininstr_17_2")) return false;
    aligneq(b, l + 1);
    return true;
  }

  // ICONST (UNSIGNED | SIGNED)
  private static boolean plaininstr_18(PsiBuilder b, int l) {
    if (!GeneratedParserUtilBase.recursion_guard_(b, l, "plaininstr_18")) return false;
    boolean r;
    Marker m = GeneratedParserUtilBase.enter_section_(b);
    r = GeneratedParserUtilBase.consumeToken(b, WebAssemblyTypes.ICONST);
    r = r && plaininstr_18_1(b, l + 1);
    GeneratedParserUtilBase.exit_section_(b, m, null, r);
    return r;
  }

  // UNSIGNED | SIGNED
  private static boolean plaininstr_18_1(PsiBuilder b, int l) {
    if (!GeneratedParserUtilBase.recursion_guard_(b, l, "plaininstr_18_1")) return false;
    boolean r;
    r = GeneratedParserUtilBase.consumeToken(b, WebAssemblyTypes.UNSIGNED);
    if (!r) r = GeneratedParserUtilBase.consumeToken(b, WebAssemblyTypes.SIGNED);
    return r;
  }

  // FCONST (FLOAT | UNSIGNED | SIGNED)
  private static boolean plaininstr_19(PsiBuilder b, int l) {
    if (!GeneratedParserUtilBase.recursion_guard_(b, l, "plaininstr_19")) return false;
    boolean r;
    Marker m = GeneratedParserUtilBase.enter_section_(b);
    r = GeneratedParserUtilBase.consumeToken(b, WebAssemblyTypes.FCONST);
    r = r && plaininstr_19_1(b, l + 1);
    GeneratedParserUtilBase.exit_section_(b, m, null, r);
    return r;
  }

  // FLOAT | UNSIGNED | SIGNED
  private static boolean plaininstr_19_1(PsiBuilder b, int l) {
    if (!GeneratedParserUtilBase.recursion_guard_(b, l, "plaininstr_19_1")) return false;
    boolean r;
    r = GeneratedParserUtilBase.consumeToken(b, WebAssemblyTypes.FLOAT);
    if (!r) r = GeneratedParserUtilBase.consumeToken(b, WebAssemblyTypes.UNSIGNED);
    if (!r) r = GeneratedParserUtilBase.consumeToken(b, WebAssemblyTypes.SIGNED);
    return r;
  }

  /* ********************************************************** */
  // REFFUNCINSTR idx
  public static boolean ref_func_instr(PsiBuilder b, int l) {
    if (!GeneratedParserUtilBase.recursion_guard_(b, l, "ref_func_instr")) return false;
    if (!GeneratedParserUtilBase.nextTokenIs(b, WebAssemblyTypes.REFFUNCINSTR)) return false;
    boolean r, p;
    Marker m = GeneratedParserUtilBase.enter_section_(b, l, GeneratedParserUtilBase._NONE_, WebAssemblyTypes.REF_FUNC_INSTR, null);
    r = GeneratedParserUtilBase.consumeToken(b, WebAssemblyTypes.REFFUNCINSTR);
    p = r; // pin = 1
    r = r && idx(b, l + 1);
    GeneratedParserUtilBase.exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  /* ********************************************************** */
  // LPAR result_aux_ RPAR
  public static boolean result(PsiBuilder b, int l) {
    if (!GeneratedParserUtilBase.recursion_guard_(b, l, "result")) return false;
    if (!GeneratedParserUtilBase.nextTokenIs(b, WebAssemblyTypes.LPAR)) return false;
    boolean r, p;
    Marker m = GeneratedParserUtilBase.enter_section_(b, l, GeneratedParserUtilBase._NONE_, WebAssemblyTypes.RESULT, null);
    r = GeneratedParserUtilBase.consumeToken(b, WebAssemblyTypes.LPAR);
    r = r && result_aux_(b, l + 1);
    p = r; // pin = 2
    r = r && GeneratedParserUtilBase.consumeToken(b, WebAssemblyTypes.RPAR);
    GeneratedParserUtilBase.exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  /* ********************************************************** */
  // RESULTKEY valtype*
  static boolean result_aux_(PsiBuilder b, int l) {
    if (!GeneratedParserUtilBase.recursion_guard_(b, l, "result_aux_")) return false;
    boolean r, p;
    Marker m = GeneratedParserUtilBase.enter_section_(b, l, GeneratedParserUtilBase._NONE_);
    r = GeneratedParserUtilBase.consumeToken(b, WebAssemblyTypes.RESULTKEY);
    p = r; // pin = 1
    r = r && result_aux__1(b, l + 1);
    GeneratedParserUtilBase.exit_section_(b, l, m, r, p, WebAssemblyParser::item_recover_);
    return r || p;
  }

  // valtype*
  private static boolean result_aux__1(PsiBuilder b, int l) {
    if (!GeneratedParserUtilBase.recursion_guard_(b, l, "result_aux__1")) return false;
    while (true) {
      int c = GeneratedParserUtilBase.current_position_(b);
      if (!valtype(b, l + 1)) break;
      if (!GeneratedParserUtilBase.empty_element_parsed_guard_(b, "result_aux__1", c)) break;
    }
    return true;
  }

  /* ********************************************************** */
  // LPAR start_aux_ RPAR
  public static boolean start(PsiBuilder b, int l) {
    if (!GeneratedParserUtilBase.recursion_guard_(b, l, "start")) return false;
    if (!GeneratedParserUtilBase.nextTokenIs(b, WebAssemblyTypes.LPAR)) return false;
    boolean r, p;
    Marker m = GeneratedParserUtilBase.enter_section_(b, l, GeneratedParserUtilBase._NONE_, WebAssemblyTypes.START, null);
    r = GeneratedParserUtilBase.consumeToken(b, WebAssemblyTypes.LPAR);
    r = r && start_aux_(b, l + 1);
    p = r; // pin = 2
    r = r && GeneratedParserUtilBase.consumeToken(b, WebAssemblyTypes.RPAR);
    GeneratedParserUtilBase.exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  /* ********************************************************** */
  // STARTKEY idx
  static boolean start_aux_(PsiBuilder b, int l) {
    if (!GeneratedParserUtilBase.recursion_guard_(b, l, "start_aux_")) return false;
    boolean r, p;
    Marker m = GeneratedParserUtilBase.enter_section_(b, l, GeneratedParserUtilBase._NONE_);
    r = GeneratedParserUtilBase.consumeToken(b, WebAssemblyTypes.STARTKEY);
    p = r; // pin = 1
    r = r && idx(b, l + 1);
    GeneratedParserUtilBase.exit_section_(b, l, m, r, p, WebAssemblyParser::item_recover_);
    return r || p;
  }

  /* ********************************************************** */
  // STRING
  static boolean string_aux_(PsiBuilder b, int l) {
    if (!GeneratedParserUtilBase.recursion_guard_(b, l, "string_aux_")) return false;
    boolean r;
    Marker m = GeneratedParserUtilBase.enter_section_(b, l, GeneratedParserUtilBase._NONE_);
    r = GeneratedParserUtilBase.consumeToken(b, WebAssemblyTypes.STRING);
    GeneratedParserUtilBase.exit_section_(b, l, m, r, false, WebAssemblyParser::string_recover_);
    return r;
  }

  /* ********************************************************** */
  // STRING
  static boolean string_fir_aux_(PsiBuilder b, int l) {
    if (!GeneratedParserUtilBase.recursion_guard_(b, l, "string_fir_aux_")) return false;
    boolean r;
    Marker m = GeneratedParserUtilBase.enter_section_(b, l, GeneratedParserUtilBase._NONE_);
    r = GeneratedParserUtilBase.consumeToken(b, WebAssemblyTypes.STRING);
    GeneratedParserUtilBase.exit_section_(b, l, m, r, false, WebAssemblyParser::string_fir_recover_);
    return r;
  }

  /* ********************************************************** */
  // !(STRING | LPAR | RPAR | <<eof>>)
  static boolean string_fir_recover_(PsiBuilder b, int l) {
    if (!GeneratedParserUtilBase.recursion_guard_(b, l, "string_fir_recover_")) return false;
    boolean r;
    Marker m = GeneratedParserUtilBase.enter_section_(b, l, GeneratedParserUtilBase._NOT_);
    r = !string_fir_recover__0(b, l + 1);
    GeneratedParserUtilBase.exit_section_(b, l, m, r, false, null);
    return r;
  }

  // STRING | LPAR | RPAR | <<eof>>
  private static boolean string_fir_recover__0(PsiBuilder b, int l) {
    if (!GeneratedParserUtilBase.recursion_guard_(b, l, "string_fir_recover__0")) return false;
    boolean r;
    Marker m = GeneratedParserUtilBase.enter_section_(b);
    r = GeneratedParserUtilBase.consumeToken(b, WebAssemblyTypes.STRING);
    if (!r) r = GeneratedParserUtilBase.consumeToken(b, WebAssemblyTypes.LPAR);
    if (!r) r = GeneratedParserUtilBase.consumeToken(b, WebAssemblyTypes.RPAR);
    if (!r) r = GeneratedParserUtilBase.eof(b, l + 1);
    GeneratedParserUtilBase.exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // !(LPAR | RPAR | <<eof>>)
  static boolean string_recover_(PsiBuilder b, int l) {
    if (!GeneratedParserUtilBase.recursion_guard_(b, l, "string_recover_")) return false;
    boolean r;
    Marker m = GeneratedParserUtilBase.enter_section_(b, l, GeneratedParserUtilBase._NOT_);
    r = !string_recover__0(b, l + 1);
    GeneratedParserUtilBase.exit_section_(b, l, m, r, false, null);
    return r;
  }

  // LPAR | RPAR | <<eof>>
  private static boolean string_recover__0(PsiBuilder b, int l) {
    if (!GeneratedParserUtilBase.recursion_guard_(b, l, "string_recover__0")) return false;
    boolean r;
    Marker m = GeneratedParserUtilBase.enter_section_(b);
    r = GeneratedParserUtilBase.consumeToken(b, WebAssemblyTypes.LPAR);
    if (!r) r = GeneratedParserUtilBase.consumeToken(b, WebAssemblyTypes.RPAR);
    if (!r) r = GeneratedParserUtilBase.eof(b, l + 1);
    GeneratedParserUtilBase.exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // LPAR table_aux_ RPAR
  public static boolean table(PsiBuilder b, int l) {
    if (!GeneratedParserUtilBase.recursion_guard_(b, l, "table")) return false;
    if (!GeneratedParserUtilBase.nextTokenIs(b, WebAssemblyTypes.LPAR)) return false;
    boolean r, p;
    Marker m = GeneratedParserUtilBase.enter_section_(b, l, GeneratedParserUtilBase._NONE_, WebAssemblyTypes.TABLE, null);
    r = GeneratedParserUtilBase.consumeToken(b, WebAssemblyTypes.LPAR);
    r = r && table_aux_(b, l + 1);
    p = r; // pin = 2
    r = r && GeneratedParserUtilBase.consumeToken(b, WebAssemblyTypes.RPAR);
    GeneratedParserUtilBase.exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  /* ********************************************************** */
  // TABLEKEY table_aux_ident_? (
  //                           inline_elem
  //                         | inline_import? tabletype
  //                         | inline_export (inline_import | inline_export | inline_elem)?
  //                        )
  static boolean table_aux_(PsiBuilder b, int l) {
    if (!GeneratedParserUtilBase.recursion_guard_(b, l, "table_aux_")) return false;
    boolean r, p;
    Marker m = GeneratedParserUtilBase.enter_section_(b, l, GeneratedParserUtilBase._NONE_);
    r = GeneratedParserUtilBase.consumeToken(b, WebAssemblyTypes.TABLEKEY);
    p = r; // pin = 1
    r = r && GeneratedParserUtilBase.report_error_(b, table_aux__1(b, l + 1));
    r = p && table_aux__2(b, l + 1) && r;
    GeneratedParserUtilBase.exit_section_(b, l, m, r, p, WebAssemblyParser::item_recover_);
    return r || p;
  }

  // table_aux_ident_?
  private static boolean table_aux__1(PsiBuilder b, int l) {
    if (!GeneratedParserUtilBase.recursion_guard_(b, l, "table_aux__1")) return false;
    table_aux_ident_(b, l + 1);
    return true;
  }

  // inline_elem
  //                         | inline_import? tabletype
  //                         | inline_export (inline_import | inline_export | inline_elem)?
  private static boolean table_aux__2(PsiBuilder b, int l) {
    if (!GeneratedParserUtilBase.recursion_guard_(b, l, "table_aux__2")) return false;
    boolean r;
    Marker m = GeneratedParserUtilBase.enter_section_(b);
    r = inline_elem(b, l + 1);
    if (!r) r = table_aux__2_1(b, l + 1);
    if (!r) r = table_aux__2_2(b, l + 1);
    GeneratedParserUtilBase.exit_section_(b, m, null, r);
    return r;
  }

  // inline_import? tabletype
  private static boolean table_aux__2_1(PsiBuilder b, int l) {
    if (!GeneratedParserUtilBase.recursion_guard_(b, l, "table_aux__2_1")) return false;
    boolean r;
    Marker m = GeneratedParserUtilBase.enter_section_(b);
    r = table_aux__2_1_0(b, l + 1);
    r = r && tabletype(b, l + 1);
    GeneratedParserUtilBase.exit_section_(b, m, null, r);
    return r;
  }

  // inline_import?
  private static boolean table_aux__2_1_0(PsiBuilder b, int l) {
    if (!GeneratedParserUtilBase.recursion_guard_(b, l, "table_aux__2_1_0")) return false;
    inline_import(b, l + 1);
    return true;
  }

  // inline_export (inline_import | inline_export | inline_elem)?
  private static boolean table_aux__2_2(PsiBuilder b, int l) {
    if (!GeneratedParserUtilBase.recursion_guard_(b, l, "table_aux__2_2")) return false;
    boolean r;
    Marker m = GeneratedParserUtilBase.enter_section_(b);
    r = inline_export(b, l + 1);
    r = r && table_aux__2_2_1(b, l + 1);
    GeneratedParserUtilBase.exit_section_(b, m, null, r);
    return r;
  }

  // (inline_import | inline_export | inline_elem)?
  private static boolean table_aux__2_2_1(PsiBuilder b, int l) {
    if (!GeneratedParserUtilBase.recursion_guard_(b, l, "table_aux__2_2_1")) return false;
    table_aux__2_2_1_0(b, l + 1);
    return true;
  }

  // inline_import | inline_export | inline_elem
  private static boolean table_aux__2_2_1_0(PsiBuilder b, int l) {
    if (!GeneratedParserUtilBase.recursion_guard_(b, l, "table_aux__2_2_1_0")) return false;
    boolean r;
    r = inline_import(b, l + 1);
    if (!r) r = inline_export(b, l + 1);
    if (!r) r = inline_elem(b, l + 1);
    return r;
  }

  /* ********************************************************** */
  // IDENTIFIER
  static boolean table_aux_ident_(PsiBuilder b, int l) {
    if (!GeneratedParserUtilBase.recursion_guard_(b, l, "table_aux_ident_")) return false;
    boolean r;
    Marker m = GeneratedParserUtilBase.enter_section_(b, l, GeneratedParserUtilBase._NONE_);
    r = GeneratedParserUtilBase.consumeToken(b, WebAssemblyTypes.IDENTIFIER);
    GeneratedParserUtilBase.exit_section_(b, l, m, r, false, WebAssemblyParser::table_aux_ident_recover_);
    return r;
  }

  /* ********************************************************** */
  // !(LPAR | RPAR | REFTYPE | memtype | <<eof>>)
  static boolean table_aux_ident_recover_(PsiBuilder b, int l) {
    if (!GeneratedParserUtilBase.recursion_guard_(b, l, "table_aux_ident_recover_")) return false;
    boolean r;
    Marker m = GeneratedParserUtilBase.enter_section_(b, l, GeneratedParserUtilBase._NOT_);
    r = !table_aux_ident_recover__0(b, l + 1);
    GeneratedParserUtilBase.exit_section_(b, l, m, r, false, null);
    return r;
  }

  // LPAR | RPAR | REFTYPE | memtype | <<eof>>
  private static boolean table_aux_ident_recover__0(PsiBuilder b, int l) {
    if (!GeneratedParserUtilBase.recursion_guard_(b, l, "table_aux_ident_recover__0")) return false;
    boolean r;
    Marker m = GeneratedParserUtilBase.enter_section_(b);
    r = GeneratedParserUtilBase.consumeToken(b, WebAssemblyTypes.LPAR);
    if (!r) r = GeneratedParserUtilBase.consumeToken(b, WebAssemblyTypes.RPAR);
    if (!r) r = GeneratedParserUtilBase.consumeToken(b, WebAssemblyTypes.REFTYPE);
    if (!r) r = memtype(b, l + 1);
    if (!r) r = GeneratedParserUtilBase.eof(b, l + 1);
    GeneratedParserUtilBase.exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // TABLECOPYINSTR idx? idx?
  public static boolean table_copy_instr(PsiBuilder b, int l) {
    if (!GeneratedParserUtilBase.recursion_guard_(b, l, "table_copy_instr")) return false;
    if (!GeneratedParserUtilBase.nextTokenIs(b, WebAssemblyTypes.TABLECOPYINSTR)) return false;
    boolean r, p;
    Marker m = GeneratedParserUtilBase.enter_section_(b, l, GeneratedParserUtilBase._NONE_, WebAssemblyTypes.TABLE_COPY_INSTR, null);
    r = GeneratedParserUtilBase.consumeToken(b, WebAssemblyTypes.TABLECOPYINSTR);
    p = r; // pin = 1
    r = r && GeneratedParserUtilBase.report_error_(b, table_copy_instr_1(b, l + 1));
    r = p && table_copy_instr_2(b, l + 1) && r;
    GeneratedParserUtilBase.exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  // idx?
  private static boolean table_copy_instr_1(PsiBuilder b, int l) {
    if (!GeneratedParserUtilBase.recursion_guard_(b, l, "table_copy_instr_1")) return false;
    idx(b, l + 1);
    return true;
  }

  // idx?
  private static boolean table_copy_instr_2(PsiBuilder b, int l) {
    if (!GeneratedParserUtilBase.recursion_guard_(b, l, "table_copy_instr_2")) return false;
    idx(b, l + 1);
    return true;
  }

  /* ********************************************************** */
  // TABLEINSTR_IDX idx?
  public static boolean table_idx_instr(PsiBuilder b, int l) {
    if (!GeneratedParserUtilBase.recursion_guard_(b, l, "table_idx_instr")) return false;
    if (!GeneratedParserUtilBase.nextTokenIs(b, WebAssemblyTypes.TABLEINSTR_IDX)) return false;
    boolean r, p;
    Marker m = GeneratedParserUtilBase.enter_section_(b, l, GeneratedParserUtilBase._NONE_, WebAssemblyTypes.TABLE_IDX_INSTR, null);
    r = GeneratedParserUtilBase.consumeToken(b, WebAssemblyTypes.TABLEINSTR_IDX);
    p = r; // pin = 1
    r = r && table_idx_instr_1(b, l + 1);
    GeneratedParserUtilBase.exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  // idx?
  private static boolean table_idx_instr_1(PsiBuilder b, int l) {
    if (!GeneratedParserUtilBase.recursion_guard_(b, l, "table_idx_instr_1")) return false;
    idx(b, l + 1);
    return true;
  }

  /* ********************************************************** */
  // TABLEINITINSTR idx idx?
  public static boolean table_init_instr(PsiBuilder b, int l) {
    if (!GeneratedParserUtilBase.recursion_guard_(b, l, "table_init_instr")) return false;
    if (!GeneratedParserUtilBase.nextTokenIs(b, WebAssemblyTypes.TABLEINITINSTR)) return false;
    boolean r, p;
    Marker m = GeneratedParserUtilBase.enter_section_(b, l, GeneratedParserUtilBase._NONE_, WebAssemblyTypes.TABLE_INIT_INSTR, null);
    r = GeneratedParserUtilBase.consumeToken(b, WebAssemblyTypes.TABLEINITINSTR);
    p = r; // pin = 1
    r = r && GeneratedParserUtilBase.report_error_(b, idx(b, l + 1));
    r = p && table_init_instr_2(b, l + 1) && r;
    GeneratedParserUtilBase.exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  // idx?
  private static boolean table_init_instr_2(PsiBuilder b, int l) {
    if (!GeneratedParserUtilBase.recursion_guard_(b, l, "table_init_instr_2")) return false;
    idx(b, l + 1);
    return true;
  }

  /* ********************************************************** */
  // memtype REFTYPE
  public static boolean tabletype(PsiBuilder b, int l) {
    if (!GeneratedParserUtilBase.recursion_guard_(b, l, "tabletype")) return false;
    if (!GeneratedParserUtilBase.nextTokenIs(b, WebAssemblyTypes.UNSIGNED)) return false;
    boolean r;
    Marker m = GeneratedParserUtilBase.enter_section_(b);
    r = memtype(b, l + 1);
    r = r && GeneratedParserUtilBase.consumeToken(b, WebAssemblyTypes.REFTYPE);
    GeneratedParserUtilBase.exit_section_(b, m, WebAssemblyTypes.TABLETYPE, r);
    return r;
  }

  /* ********************************************************** */
  // LPAR type_aux_ RPAR
  public static boolean type(PsiBuilder b, int l) {
    if (!GeneratedParserUtilBase.recursion_guard_(b, l, "type")) return false;
    if (!GeneratedParserUtilBase.nextTokenIs(b, WebAssemblyTypes.LPAR)) return false;
    boolean r, p;
    Marker m = GeneratedParserUtilBase.enter_section_(b, l, GeneratedParserUtilBase._NONE_, WebAssemblyTypes.TYPE, null);
    r = GeneratedParserUtilBase.consumeToken(b, WebAssemblyTypes.LPAR);
    r = r && type_aux_(b, l + 1);
    p = r; // pin = 2
    r = r && GeneratedParserUtilBase.consumeToken(b, WebAssemblyTypes.RPAR);
    GeneratedParserUtilBase.exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  /* ********************************************************** */
  // TYPEKEY IDENTIFIER? functype
  static boolean type_aux_(PsiBuilder b, int l) {
    if (!GeneratedParserUtilBase.recursion_guard_(b, l, "type_aux_")) return false;
    boolean r, p;
    Marker m = GeneratedParserUtilBase.enter_section_(b, l, GeneratedParserUtilBase._NONE_);
    r = GeneratedParserUtilBase.consumeToken(b, WebAssemblyTypes.TYPEKEY);
    p = r; // pin = 1
    r = r && GeneratedParserUtilBase.report_error_(b, type_aux__1(b, l + 1));
    r = p && functype(b, l + 1) && r;
    GeneratedParserUtilBase.exit_section_(b, l, m, r, p, WebAssemblyParser::item_recover_);
    return r || p;
  }

  // IDENTIFIER?
  private static boolean type_aux__1(PsiBuilder b, int l) {
    if (!GeneratedParserUtilBase.recursion_guard_(b, l, "type_aux__1")) return false;
    GeneratedParserUtilBase.consumeToken(b, WebAssemblyTypes.IDENTIFIER);
    return true;
  }

  /* ********************************************************** */
  // typeuse_typeref param+ result+
  //           | typeuse_typeref param+
  //           | typeuse_typeref        result+
  //           | typeuse_typeref
  //           |                 param+ result+
  //           |                 param+
  //           |                        result+
  public static boolean typeuse(PsiBuilder b, int l) {
    if (!GeneratedParserUtilBase.recursion_guard_(b, l, "typeuse")) return false;
    if (!GeneratedParserUtilBase.nextTokenIs(b, WebAssemblyTypes.LPAR)) return false;
    boolean r;
    Marker m = GeneratedParserUtilBase.enter_section_(b);
    r = typeuse_0(b, l + 1);
    if (!r) r = typeuse_1(b, l + 1);
    if (!r) r = typeuse_2(b, l + 1);
    if (!r) r = typeuse_typeref(b, l + 1);
    if (!r) r = typeuse_4(b, l + 1);
    if (!r) r = typeuse_5(b, l + 1);
    if (!r) r = typeuse_6(b, l + 1);
    GeneratedParserUtilBase.exit_section_(b, m, WebAssemblyTypes.TYPEUSE, r);
    return r;
  }

  // typeuse_typeref param+ result+
  private static boolean typeuse_0(PsiBuilder b, int l) {
    if (!GeneratedParserUtilBase.recursion_guard_(b, l, "typeuse_0")) return false;
    boolean r;
    Marker m = GeneratedParserUtilBase.enter_section_(b);
    r = typeuse_typeref(b, l + 1);
    r = r && typeuse_0_1(b, l + 1);
    r = r && typeuse_0_2(b, l + 1);
    GeneratedParserUtilBase.exit_section_(b, m, null, r);
    return r;
  }

  // param+
  private static boolean typeuse_0_1(PsiBuilder b, int l) {
    if (!GeneratedParserUtilBase.recursion_guard_(b, l, "typeuse_0_1")) return false;
    boolean r;
    Marker m = GeneratedParserUtilBase.enter_section_(b);
    r = param(b, l + 1);
    while (r) {
      int c = GeneratedParserUtilBase.current_position_(b);
      if (!param(b, l + 1)) break;
      if (!GeneratedParserUtilBase.empty_element_parsed_guard_(b, "typeuse_0_1", c)) break;
    }
    GeneratedParserUtilBase.exit_section_(b, m, null, r);
    return r;
  }

  // result+
  private static boolean typeuse_0_2(PsiBuilder b, int l) {
    if (!GeneratedParserUtilBase.recursion_guard_(b, l, "typeuse_0_2")) return false;
    boolean r;
    Marker m = GeneratedParserUtilBase.enter_section_(b);
    r = result(b, l + 1);
    while (r) {
      int c = GeneratedParserUtilBase.current_position_(b);
      if (!result(b, l + 1)) break;
      if (!GeneratedParserUtilBase.empty_element_parsed_guard_(b, "typeuse_0_2", c)) break;
    }
    GeneratedParserUtilBase.exit_section_(b, m, null, r);
    return r;
  }

  // typeuse_typeref param+
  private static boolean typeuse_1(PsiBuilder b, int l) {
    if (!GeneratedParserUtilBase.recursion_guard_(b, l, "typeuse_1")) return false;
    boolean r;
    Marker m = GeneratedParserUtilBase.enter_section_(b);
    r = typeuse_typeref(b, l + 1);
    r = r && typeuse_1_1(b, l + 1);
    GeneratedParserUtilBase.exit_section_(b, m, null, r);
    return r;
  }

  // param+
  private static boolean typeuse_1_1(PsiBuilder b, int l) {
    if (!GeneratedParserUtilBase.recursion_guard_(b, l, "typeuse_1_1")) return false;
    boolean r;
    Marker m = GeneratedParserUtilBase.enter_section_(b);
    r = param(b, l + 1);
    while (r) {
      int c = GeneratedParserUtilBase.current_position_(b);
      if (!param(b, l + 1)) break;
      if (!GeneratedParserUtilBase.empty_element_parsed_guard_(b, "typeuse_1_1", c)) break;
    }
    GeneratedParserUtilBase.exit_section_(b, m, null, r);
    return r;
  }

  // typeuse_typeref        result+
  private static boolean typeuse_2(PsiBuilder b, int l) {
    if (!GeneratedParserUtilBase.recursion_guard_(b, l, "typeuse_2")) return false;
    boolean r;
    Marker m = GeneratedParserUtilBase.enter_section_(b);
    r = typeuse_typeref(b, l + 1);
    r = r && typeuse_2_1(b, l + 1);
    GeneratedParserUtilBase.exit_section_(b, m, null, r);
    return r;
  }

  // result+
  private static boolean typeuse_2_1(PsiBuilder b, int l) {
    if (!GeneratedParserUtilBase.recursion_guard_(b, l, "typeuse_2_1")) return false;
    boolean r;
    Marker m = GeneratedParserUtilBase.enter_section_(b);
    r = result(b, l + 1);
    while (r) {
      int c = GeneratedParserUtilBase.current_position_(b);
      if (!result(b, l + 1)) break;
      if (!GeneratedParserUtilBase.empty_element_parsed_guard_(b, "typeuse_2_1", c)) break;
    }
    GeneratedParserUtilBase.exit_section_(b, m, null, r);
    return r;
  }

  // param+ result+
  private static boolean typeuse_4(PsiBuilder b, int l) {
    if (!GeneratedParserUtilBase.recursion_guard_(b, l, "typeuse_4")) return false;
    boolean r;
    Marker m = GeneratedParserUtilBase.enter_section_(b);
    r = typeuse_4_0(b, l + 1);
    r = r && typeuse_4_1(b, l + 1);
    GeneratedParserUtilBase.exit_section_(b, m, null, r);
    return r;
  }

  // param+
  private static boolean typeuse_4_0(PsiBuilder b, int l) {
    if (!GeneratedParserUtilBase.recursion_guard_(b, l, "typeuse_4_0")) return false;
    boolean r;
    Marker m = GeneratedParserUtilBase.enter_section_(b);
    r = param(b, l + 1);
    while (r) {
      int c = GeneratedParserUtilBase.current_position_(b);
      if (!param(b, l + 1)) break;
      if (!GeneratedParserUtilBase.empty_element_parsed_guard_(b, "typeuse_4_0", c)) break;
    }
    GeneratedParserUtilBase.exit_section_(b, m, null, r);
    return r;
  }

  // result+
  private static boolean typeuse_4_1(PsiBuilder b, int l) {
    if (!GeneratedParserUtilBase.recursion_guard_(b, l, "typeuse_4_1")) return false;
    boolean r;
    Marker m = GeneratedParserUtilBase.enter_section_(b);
    r = result(b, l + 1);
    while (r) {
      int c = GeneratedParserUtilBase.current_position_(b);
      if (!result(b, l + 1)) break;
      if (!GeneratedParserUtilBase.empty_element_parsed_guard_(b, "typeuse_4_1", c)) break;
    }
    GeneratedParserUtilBase.exit_section_(b, m, null, r);
    return r;
  }

  // param+
  private static boolean typeuse_5(PsiBuilder b, int l) {
    if (!GeneratedParserUtilBase.recursion_guard_(b, l, "typeuse_5")) return false;
    boolean r;
    Marker m = GeneratedParserUtilBase.enter_section_(b);
    r = param(b, l + 1);
    while (r) {
      int c = GeneratedParserUtilBase.current_position_(b);
      if (!param(b, l + 1)) break;
      if (!GeneratedParserUtilBase.empty_element_parsed_guard_(b, "typeuse_5", c)) break;
    }
    GeneratedParserUtilBase.exit_section_(b, m, null, r);
    return r;
  }

  // result+
  private static boolean typeuse_6(PsiBuilder b, int l) {
    if (!GeneratedParserUtilBase.recursion_guard_(b, l, "typeuse_6")) return false;
    boolean r;
    Marker m = GeneratedParserUtilBase.enter_section_(b);
    r = result(b, l + 1);
    while (r) {
      int c = GeneratedParserUtilBase.current_position_(b);
      if (!result(b, l + 1)) break;
      if (!GeneratedParserUtilBase.empty_element_parsed_guard_(b, "typeuse_6", c)) break;
    }
    GeneratedParserUtilBase.exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // LPAR TYPEKEY idx RPAR
  public static boolean typeuse_typeref(PsiBuilder b, int l) {
    if (!GeneratedParserUtilBase.recursion_guard_(b, l, "typeuse_typeref")) return false;
    if (!GeneratedParserUtilBase.nextTokenIs(b, WebAssemblyTypes.LPAR)) return false;
    boolean r, p;
    Marker m = GeneratedParserUtilBase.enter_section_(b, l, GeneratedParserUtilBase._NONE_, WebAssemblyTypes.TYPEUSE_TYPEREF, null);
    r = GeneratedParserUtilBase.consumeTokens(b, 2, WebAssemblyTypes.LPAR, WebAssemblyTypes.TYPEKEY);
    p = r; // pin = 2
    r = r && GeneratedParserUtilBase.report_error_(b, idx(b, l + 1));
    r = p && GeneratedParserUtilBase.consumeToken(b, WebAssemblyTypes.RPAR) && r;
    GeneratedParserUtilBase.exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  /* ********************************************************** */
  // NUMTYPE | REFTYPE
  public static boolean valtype(PsiBuilder b, int l) {
    if (!GeneratedParserUtilBase.recursion_guard_(b, l, "valtype")) return false;
    if (!GeneratedParserUtilBase.nextTokenIs(b, "<valtype>", WebAssemblyTypes.NUMTYPE, WebAssemblyTypes.REFTYPE)) return false;
    boolean r;
    Marker m = GeneratedParserUtilBase.enter_section_(b, l, GeneratedParserUtilBase._NONE_, WebAssemblyTypes.VALTYPE, "<valtype>");
    r = GeneratedParserUtilBase.consumeToken(b, WebAssemblyTypes.NUMTYPE);
    if (!r) r = GeneratedParserUtilBase.consumeToken(b, WebAssemblyTypes.REFTYPE);
    GeneratedParserUtilBase.exit_section_(b, l, m, r, false, null);
    return r;
  }

  /* ********************************************************** */
  // item_*
  static boolean webAssemblyFile(PsiBuilder b, int l) {
    if (!GeneratedParserUtilBase.recursion_guard_(b, l, "webAssemblyFile")) return false;
    while (true) {
      int c = GeneratedParserUtilBase.current_position_(b);
      if (!item_(b, l + 1)) break;
      if (!GeneratedParserUtilBase.empty_element_parsed_guard_(b, "webAssemblyFile", c)) break;
    }
    return true;
  }

}
