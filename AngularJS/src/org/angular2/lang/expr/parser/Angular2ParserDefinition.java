// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.lang.expr.parser;

import com.intellij.lang.PsiParser;
import com.intellij.lang.javascript.JavascriptParserDefinition;
import com.intellij.lexer.Lexer;
import com.intellij.openapi.project.Project;
import com.intellij.psi.tree.IFileElementType;
import org.angular2.lang.expr.lexer.Angular2Lexer;
import org.jetbrains.annotations.NotNull;

public class Angular2ParserDefinition extends JavascriptParserDefinition {
  @NotNull
  @Override
  public Lexer createLexer(Project project) {
    return new Angular2Lexer();
  }

  @NotNull
  @Override
  public PsiParser createParser(Project project) {
    return new Angular2PsiParser();
  }

  @Override
  public IFileElementType getFileNodeType() {
    return Angular2ElementTypes.FILE;
  }
}
