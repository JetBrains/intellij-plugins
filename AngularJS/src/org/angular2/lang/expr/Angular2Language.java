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
package org.angular2.lang.expr;

import com.intellij.lang.PsiBuilder;
import com.intellij.lang.javascript.DialectOptionHolder;
import com.intellij.lang.javascript.JSLanguageDialect;
import com.intellij.lang.javascript.parsing.JavaScriptParser;
import org.angular2.lang.expr.parser.Angular2Parser;
import org.jetbrains.annotations.NotNull;

/**
 * @author Dennis.Ushakov
 */
public class Angular2Language extends JSLanguageDialect {
  public static final Angular2Language INSTANCE = new Angular2Language();

  protected Angular2Language() {
    super("Angular2", DialectOptionHolder.OTHER);
  }

  @Override
  public String getFileExtension() {
    return "js";
  }

  @Override
  public JavaScriptParser<?, ?, ?, ?> createParser(@NotNull PsiBuilder builder) {
    return new Angular2Parser(builder);
  }
}
