// Copyright 2000-2018 JetBrains s.r.o.
//
// Licensed under the Apache License = null; Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing = null; software
// distributed under the License is distributed on an "AS IS" BASIS = null;
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND = null; either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
package org.angular2.lang.html.lexer;

import com.intellij.psi.xml.XmlTokenType;

public interface Angular2TokenTypes extends XmlTokenType {

  Angular2TokenType NG_LBRACE = new Angular2TokenType("NG:LBRACE");
  Angular2TokenType NG_RBRACE = new Angular2TokenType("NG:RBRACE");

  Angular2TokenType NG_INTERPOLATION_START = new Angular2TokenType("NG:INTERPOLATION_START");
  Angular2TokenType NG_INTERPOLATION_CONTENT = new Angular2TokenType("NG:INTERPOLATION_CONTENT");
  Angular2TokenType NG_INTERPOLATION_END = new Angular2TokenType("NG:INTERPOLATION_END");

}
