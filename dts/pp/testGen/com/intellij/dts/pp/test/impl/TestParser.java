// This is a generated file. Not intended for manual editing.
package com.intellij.dts.pp.test.impl;

import static com.intellij.dts.pp.test.impl.psi.TestTypes.*;
import static com.intellij.lang.parser.GeneratedParserUtilBase.*;
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
  // TOKEN
  static boolean file(com.intellij.lang.PsiBuilder builder_, int level_) {
    return consumeToken(builder_, TOKEN);
  }

}
