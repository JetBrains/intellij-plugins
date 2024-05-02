// This is a generated file. Not intended for manual editing.
package com.intellij.dts.pp.test.impl;

import static com.intellij.dts.pp.test.impl.psi.TestTypes.*;
import static com.intellij.dts.pp.test.impl.TestParserUtil.*;
import static com.intellij.lang.WhitespacesBinders.*;

@java.lang.SuppressWarnings({"SimplifiableIfStatement", "UnusedAssignment"})
public class TestParser implements com.intellij.lang.PsiParser, com.intellij.lang.LightPsiParser {

  public com.intellij.lang.ASTNode parse(com.intellij.psi.tree.IElementType root_, com.intellij.lang.PsiBuilder builder_) {
    parseLight(root_, builder_);
    return builder_.getTreeBuilt();
  }

  public void parseLight(com.intellij.psi.tree.IElementType root_, com.intellij.lang.PsiBuilder builder_) {
    boolean result_;
    builder_ = adapt_builder_(root_, builder_, this, null);
    com.intellij.lang.PsiBuilder.Marker marker_ = enter_section_(builder_, 0, _COLLAPSE_, null);
    result_ = parse_root_(root_, builder_);
    exit_section_(builder_, 0, marker_, root_, result_, true, TRUE_CONDITION);
  }

  protected boolean parse_root_(com.intellij.psi.tree.IElementType root_, com.intellij.lang.PsiBuilder builder_) {
    return parse_root_(root_, builder_, 0);
  }

  static boolean parse_root_(com.intellij.psi.tree.IElementType root_, com.intellij.lang.PsiBuilder builder_, int level_) {
    return file(builder_, level_ + 1);
  }

  /* ********************************************************** */
  // sentence*
  static boolean file(com.intellij.lang.PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "file")) return false;
    while (true) {
      int pos_ = current_position_(builder_);
      if (!sentence(builder_, level_ + 1)) break;
      if (!empty_element_parsed_guard_(builder_, "file", pos_)) break;
    }
    return true;
  }

  /* ********************************************************** */
  // QUOTE_START WORD* QUOTE_END
  public static boolean quote(com.intellij.lang.PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "quote")) return false;
    if (!nextTokenIs(builder_, QUOTE_START)) return false;
    boolean result_;
    com.intellij.lang.PsiBuilder.Marker marker_ = enter_section_(builder_);
    result_ = consumeToken(builder_, QUOTE_START);
    result_ = result_ && quote_1(builder_, level_ + 1);
    result_ = result_ && consumeToken(builder_, QUOTE_END);
    exit_section_(builder_, marker_, QUOTE, result_);
    return result_;
  }

  // WORD*
  private static boolean quote_1(com.intellij.lang.PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "quote_1")) return false;
    while (true) {
      int pos_ = current_position_(builder_);
      if (!consumeToken(builder_, WORD)) break;
      if (!empty_element_parsed_guard_(builder_, "quote_1", pos_)) break;
    }
    return true;
  }

  /* ********************************************************** */
  // (WORD | quote)+ DOT
  public static boolean sentence(com.intellij.lang.PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "sentence")) return false;
    if (!nextTokenIs(builder_, "<sentence>", QUOTE_START, WORD)) return false;
    boolean result_;
    com.intellij.lang.PsiBuilder.Marker marker_ = enter_section_(builder_, level_, _NONE_, SENTENCE, "<sentence>");
    result_ = sentence_0(builder_, level_ + 1);
    result_ = result_ && consumeToken(builder_, DOT);
    exit_section_(builder_, level_, marker_, result_, false, null);
    return result_;
  }

  // (WORD | quote)+
  private static boolean sentence_0(com.intellij.lang.PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "sentence_0")) return false;
    boolean result_;
    com.intellij.lang.PsiBuilder.Marker marker_ = enter_section_(builder_);
    result_ = sentence_0_0(builder_, level_ + 1);
    while (result_) {
      int pos_ = current_position_(builder_);
      if (!sentence_0_0(builder_, level_ + 1)) break;
      if (!empty_element_parsed_guard_(builder_, "sentence_0", pos_)) break;
    }
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  // WORD | quote
  private static boolean sentence_0_0(com.intellij.lang.PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "sentence_0_0")) return false;
    boolean result_;
    result_ = consumeToken(builder_, WORD);
    if (!result_) result_ = quote(builder_, level_ + 1);
    return result_;
  }

}
