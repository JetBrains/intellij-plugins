/*
 * Copyright 2013 The authors
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.intellij.lang.ognl.highlight;

import com.intellij.lang.ognl.OgnlTypes;
import com.intellij.lang.ognl.lexer.OgnlLexer;
import com.intellij.lexer.LayeredLexer;
import com.intellij.lexer.StringLiteralLexer;
import com.intellij.psi.tree.IElementType;

/**
 * Adds additional syntax highlighting layers.
 * <p/>
 * <ul>
 * <li>string/character literal escape sequences.</li>
 * </ul>
 *
 * @author Yann C&eacute;bron
 */
public class OgnlHighlightingLexer extends LayeredLexer {

  public OgnlHighlightingLexer() {
    super(new OgnlLexer());

    final StringLiteralLexer stringLiteralLexer = new StringLiteralLexer('\"', OgnlTypes.STRING_LITERAL);
    registerSelfStoppingLayer(stringLiteralLexer,
                              new IElementType[]{OgnlTypes.STRING_LITERAL},
                              IElementType.EMPTY_ARRAY);

    final StringLiteralLexer characterLiteralLexer = new StringLiteralLexer('\'', OgnlTypes.CHARACTER_LITERAL);
    registerSelfStoppingLayer(characterLiteralLexer,
                              new IElementType[]{OgnlTypes.CHARACTER_LITERAL},
                              IElementType.EMPTY_ARRAY);
  }
}