/*
 * @author max
 */
package com.intellij.lang.javascript.dialects;

import com.intellij.lang.PsiBuilder;
import com.intellij.lang.actionscript.parsing.ActionScriptParser;
import com.intellij.lang.javascript.FlexFileElementTypes;
import com.intellij.lang.javascript.JSFlexAdapter;
import com.intellij.lang.javascript.JavascriptParserDefinition;
import com.intellij.lang.javascript.parsing.JavaScriptParser;
import com.intellij.lexer.Lexer;
import com.intellij.openapi.project.Project;
import com.intellij.psi.tree.IFileElementType;
import org.jetbrains.annotations.NotNull;

public class ECMAL4ParserDefinition extends JavascriptParserDefinition {

  @Override
  public @NotNull Lexer createLexer(final Project project) {
    return new JSFlexAdapter(ECMAL4LanguageDialect.DIALECT_OPTION_HOLDER);
  }

  @Override
  public @NotNull IFileElementType getFileNodeType() {
    return FlexFileElementTypes.ECMA4_FILE;
  }

  @Override
  public @NotNull JavaScriptParser createJSParser(@NotNull PsiBuilder builder) {
    return new ActionScriptParser(builder);
  }
}