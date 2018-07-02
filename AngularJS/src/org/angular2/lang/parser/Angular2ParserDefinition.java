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
package org.angular2.lang.parser;

import com.intellij.lang.PsiParser;
import com.intellij.lang.javascript.JavascriptParserDefinition;
import com.intellij.lexer.Lexer;
import com.intellij.openapi.project.Project;
import com.intellij.psi.tree.IFileElementType;
import org.angular2.lang.lexer.Angular2Lexer;
import org.jetbrains.annotations.NotNull;

/**
 * @author Dennis.Ushakov
 */
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
