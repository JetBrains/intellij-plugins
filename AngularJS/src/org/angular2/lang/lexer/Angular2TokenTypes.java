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
package org.angular2.lang.lexer;

import com.intellij.lang.javascript.JSTokenTypes;

/**
 * @author Dennis.Ushakov
 */
public interface Angular2TokenTypes extends JSTokenTypes {
  Angular2TokenType ESCAPE_SEQUENCE = new Angular2TokenType("ESCAPE_SEQUENCE");
  Angular2TokenType INVALID_ESCAPE_SEQUENCE = new Angular2TokenType("INVALID_ESCAPE_SEQUENCE");
  Angular2TokenType TRACK_BY_KEYWORD = new Angular2TokenType("TRACK_BY_KEYWORD");
  Angular2TokenType ONE_TIME_BINDING = new Angular2TokenType("ONE_TIME_BINDING");
  Angular2TokenType ELVIS = new Angular2TokenType("ELVIS");// ?.
  Angular2TokenType ASSERT_NOT_NULL = new Angular2TokenType("ASSERT_NOT_NULL");// !.
  Angular2TokenType THEN = new Angular2TokenType("THEN");
}
