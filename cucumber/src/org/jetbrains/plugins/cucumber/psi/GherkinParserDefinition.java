// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.plugins.cucumber.psi;

import com.intellij.lang.ASTNode;
import com.intellij.lang.ParserDefinition;
import com.intellij.lang.PsiParser;
import com.intellij.lexer.Lexer;
import com.intellij.openapi.project.Project;
import com.intellij.psi.FileViewProvider;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.tree.IElementType;
import com.intellij.psi.tree.IFileElementType;
import com.intellij.psi.tree.TokenSet;
import com.intellij.psi.util.PsiUtilCore;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.plugins.cucumber.psi.i18n.JsonGherkinKeywordProvider;
import org.jetbrains.plugins.cucumber.psi.impl.*;

public final class GherkinParserDefinition implements ParserDefinition {
  public static final IFileElementType GHERKIN_FILE = new IFileElementType(GherkinLanguage.INSTANCE);

  @Override
  public @NotNull Lexer createLexer(Project project) {
    return new GherkinLexer(JsonGherkinKeywordProvider.getKeywordProvider(true));
  }

  @Override
  public @NotNull PsiParser createParser(Project project) {
    return new GherkinParser();
  }

  @Override
  public @NotNull IFileElementType getFileNodeType() {
    return GHERKIN_FILE;
  }

  @Override
  public @NotNull TokenSet getCommentTokens() {
    return GherkinTokenTypes.COMMENTS;
  }

  @Override
  public @NotNull TokenSet getStringLiteralElements() {
    return TokenSet.EMPTY;
  }

  @Override
  public @NotNull PsiElement createElement(ASTNode node) {
    if (node.getElementType() == GherkinElementTypes.FEATURE) return new GherkinFeatureImpl(node);
    if (node.getElementType() == GherkinElementTypes.FEATURE_HEADER) return new GherkinFeatureHeaderImpl(node);
    if (node.getElementType() == GherkinElementTypes.SCENARIO) return new GherkinScenarioImpl(node);
    if (node.getElementType() == GherkinElementTypes.STEP) return new GherkinStepImpl(node);
    if (node.getElementType() == GherkinElementTypes.SCENARIO_OUTLINE) return new GherkinScenarioOutlineImpl(node);
    if (node.getElementType() == GherkinElementTypes.RULE) return new GherkinRuleImpl(node);
    if (node.getElementType() == GherkinElementTypes.EXAMPLES_BLOCK) return new GherkinExamplesBlockImpl(node);
    if (node.getElementType() == GherkinElementTypes.TABLE) return new GherkinTableImpl(node);
    if (node.getElementType() == GherkinElementTypes.TABLE_ROW) return new GherkinTableRowImpl(node);
    if (node.getElementType() == GherkinElementTypes.TABLE_CELL) return new GherkinTableCellImpl(node);
    if (node.getElementType() == GherkinElementTypes.TABLE_HEADER_ROW) return new GherkinTableHeaderRowImpl(node);
    if (node.getElementType() == GherkinElementTypes.TAG) return new GherkinTagImpl(node);
    if (node.getElementType() == GherkinElementTypes.STEP_PARAMETER) return new GherkinStepParameterImpl(node);
    if (node.getElementType() == GherkinElementTypes.PYSTRING) return new GherkinPystringImpl(node);
    return PsiUtilCore.NULL_PSI_ELEMENT;
  }

  @Override
  public @NotNull PsiFile createFile(@NotNull FileViewProvider viewProvider) {
    return new GherkinFileImpl(viewProvider);
  }

  @Override
  public @NotNull SpaceRequirements spaceExistenceTypeBetweenTokens(ASTNode left, ASTNode right) {
    // Line break between line comment and other elements
    final IElementType leftElementType = left.getElementType();
    if (leftElementType == GherkinTokenTypes.COMMENT) {
      return SpaceRequirements.MUST_LINE_BREAK;
    }
    if (right.getElementType() == GherkinTokenTypes.EXAMPLES_KEYWORD) {
      return SpaceRequirements.MUST_LINE_BREAK;
    }
    return SpaceRequirements.MAY;
  }
}
