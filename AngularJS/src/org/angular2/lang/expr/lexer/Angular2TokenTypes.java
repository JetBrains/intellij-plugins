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
package org.angular2.lang.expr.lexer;

import com.intellij.lang.javascript.JSTokenTypes;
import com.intellij.psi.tree.TokenSet;

public interface Angular2TokenTypes extends JSTokenTypes {
  Angular2TokenType ESCAPE_SEQUENCE = new Angular2TokenType("NG:ESCAPE_SEQUENCE");
  Angular2TokenType INVALID_ESCAPE_SEQUENCE = new Angular2TokenType("NG:INVALID_ESCAPE_SEQUENCE");

  TokenSet KEYWORDS = TokenSet.create( VAR_KEYWORD, LET_KEYWORD, AS_KEYWORD, NULL_KEYWORD, UNDEFINED_KEYWORD, TRUE_KEYWORD, FALSE_KEYWORD, IF_KEYWORD, ELSE_KEYWORD, THIS_KEYWORD);

}
