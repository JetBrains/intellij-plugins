/*
 * Copyright 2000-2013 JetBrains s.r.o.
 *
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
package com.intellij.coldFusion.model.lexer;

import com.intellij.coldFusion.model.psi.CfmlElementType;
import com.intellij.psi.tree.IElementType;
import com.intellij.psi.tree.TokenSet;

/**
 * Created by Lera Nikolaenko
 * Date: 30.09.2008
 */
public interface CfmlTokenTypes {
  IElementType START_EXPRESSION = new CfmlElementType("START_EXPRESSION");
  IElementType END_EXPRESSION = new CfmlElementType("END_EXPRESSION");
  IElementType VAR_ANNOTATION = new CfmlElementType("VAR_ANNOTATION");

  IElementType COMMENT = new CfmlElementType("COMMENT");
  IElementType OPENER = new CfmlElementType("OPENER"); // < / {TAG}
  IElementType CLOSER = new CfmlElementType("CLOSER"); // {TAG} / >
  IElementType LSLASH_ANGLEBRACKET = new CfmlElementType("LSLASH_ANGLEBRACKET"); // </
  IElementType R_ANGLEBRACKET = new CfmlElementType("R_ANGLEBRACKET"); // >
  IElementType BAD_CHARACTER = new CfmlElementType("BAD_CHARACTER");
  IElementType CF_TAG_NAME = new CfmlElementType("CF_TAG_NAME");
  IElementType SCRIPT_EXPRESSION = new CfmlElementType("SCRIPT_EXPRESSION");
  IElementType WHITE_SPACE = new CfmlElementType("WHITE_SPACE");
  IElementType ATTRIBUTE = new CfmlElementType("ATTRIBUTE");

  IElementType ASSIGN = new CfmlElementType("ASSIGN");
  IElementType STRING_TEXT = new CfmlElementType("STRING_TEXT");
  IElementType SINGLE_QUOTE = new CfmlElementType("SINGLE_QUOTE");
  IElementType DOUBLE_QUOTE = new CfmlElementType("DOUBLE_QUOTE");
  IElementType SINGLE_QUOTE_CLOSER = new CfmlElementType("SINGLE_QUOTE_CLOSER");
  IElementType DOUBLE_QUOTE_CLOSER = new CfmlElementType("DOUBLE_QUOTE_CLOSER");

  TokenSet STRING_ELEMENTS = TokenSet.create(STRING_TEXT, SINGLE_QUOTE,
                                             DOUBLE_QUOTE, SINGLE_QUOTE_CLOSER, DOUBLE_QUOTE_CLOSER
  );

  TokenSet BRACKETS = TokenSet.create(
    OPENER,
    CLOSER,
    R_ANGLEBRACKET,
    LSLASH_ANGLEBRACKET
  );
  TokenSet tsCOMMENTS = TokenSet.create(COMMENT, CfscriptTokenTypes.COMMENT);
}
