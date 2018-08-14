// Copyright 2000-2018 JetBrains s.r.o.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
package org.angular2.lang.html.parser;

import com.intellij.lang.*;
import com.intellij.lexer.Lexer;
import com.intellij.psi.tree.ICustomParsingType;
import com.intellij.psi.tree.IElementType;
import com.intellij.util.CharTable;
import org.angular2.lang.html.Angular2HtmlLanguage;
import org.angular2.lang.html.lexer.Angular2HtmlLexer;
import org.jetbrains.annotations.NotNull;

class Angular2ExpansionFormCaseContentTokenType extends IElementType implements ICustomParsingType {

  public static final Angular2ExpansionFormCaseContentTokenType INSTANCE = new Angular2ExpansionFormCaseContentTokenType();

  private Angular2ExpansionFormCaseContentTokenType() {
    super("NG:EXPANSION_FORM_CASE_CONTENT_TOKEN", Angular2HtmlLanguage.INSTANCE);
  }

  @NotNull
  @Override
  public ASTNode parse(@NotNull CharSequence text, @NotNull CharTable table) {
    ParserDefinition parserDef = LanguageParserDefinitions.INSTANCE.forLanguage(Angular2HtmlLanguage.INSTANCE);
    Lexer lexer = new Angular2HtmlLexer(true, null);
    PsiBuilder builder = PsiBuilderFactory.getInstance().createBuilder(parserDef, lexer, text);

    Angular2HtmlParsing htmlParsing = new Angular2HtmlParsing(builder);
    htmlParsing.parseExpansionFormContent();
    return builder.getTreeBuilt();
  }

}