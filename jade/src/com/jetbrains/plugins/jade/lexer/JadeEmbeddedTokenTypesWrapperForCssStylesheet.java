package com.jetbrains.plugins.jade.lexer;

import com.intellij.embedding.EmbeddingElementType;
import com.intellij.embedding.MasqueradingLexer;
import com.intellij.embedding.MasqueradingPsiBuilderAdapter;
import com.intellij.lang.*;
import com.intellij.lexer.Lexer;
import com.intellij.openapi.project.Project;
import com.intellij.psi.ParsingDiagnostics;
import com.intellij.psi.PsiElement;
import com.intellij.psi.css.impl.util.CssStylesheetLazyElementType;
import org.jetbrains.annotations.NotNull;

public class JadeEmbeddedTokenTypesWrapperForCssStylesheet extends CssStylesheetLazyElementType implements EmbeddingElementType {

  public JadeEmbeddedTokenTypesWrapperForCssStylesheet(CssStylesheetLazyElementType delegate) {
    super("WRAPPER: " + delegate.toString(), delegate.getLanguage());
  }

  @Override
  protected ASTNode doParseContents(@NotNull ASTNode chameleon, @NotNull PsiElement psi) {
    final Project project = psi.getProject();
    final Language languageForParser = getLanguage();
    final ParserDefinition parserDefinition = LanguageParserDefinitions.INSTANCE.forLanguage(languageForParser);

    final MasqueradingLexer lexer = getLexer(project, parserDefinition);

    final PsiBuilder builder = new MasqueradingPsiBuilderAdapter(project, parserDefinition, lexer, chameleon, chameleon.getChars());
    var startTime = System.nanoTime();
    final PsiParser parser = parserDefinition.createParser(project);
    var result = parser.parse(this, builder).getFirstChildNode();
    ParsingDiagnostics.registerParse(builder, getLanguage(), System.nanoTime() - startTime);
    return result;
  }

  private static MasqueradingLexer getLexer(Project project, ParserDefinition parserDefinition) {
    final Lexer baseLexer = parserDefinition.createLexer(project);
    return new JadeSimpleInterpolationLexer(baseLexer);
  }
}
