package com.intellij.coldFusion.model.lexer;

import com.intellij.coldFusion.model.CfmlLanguage;
import com.intellij.coldFusion.model.info.CfmlLangInfo;
import com.intellij.lexer.FlexAdapter;
import com.intellij.lexer.FlexLexer;
import com.intellij.lexer.MergingLexerAdapter;
import com.intellij.openapi.project.Project;
import com.intellij.psi.tree.TokenSet;

/**
 * Created by Lera Nikolaenko
 * Date: 20.11.2008
 */
public class CfscriptLexer extends MergingLexerAdapter {

  public CfscriptLexer(Project project) {
    super(new FlexAdapter(getFlexLexer(project)), TokenSet.EMPTY);
  }

  private static FlexLexer getFlexLexer(Project project) {
    final String projectLangLevel = project == null ? CfmlLanguage.CF10 : CfmlLangInfo.getInstance(project).getLanguageLevel();
    return projectLangLevel.equals(CfmlLanguage.CF9) || projectLangLevel.equals(CfmlLanguage.CF10)
           ? new _CfscriptLexer(project)
           : new _CfscriptLexer8(project);
  }
}