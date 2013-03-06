/*
 * Copyright 2000-2012 JetBrains s.r.o.
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
package com.jetbrains.typoscript.lang.highlighter;

import com.intellij.ide.highlighter.custom.CustomHighlighterColors;
import com.intellij.openapi.editor.DefaultLanguageHighlighterColors;
import com.intellij.openapi.editor.HighlighterColors;
import com.intellij.openapi.editor.colors.TextAttributesKey;


public class TypoScriptHighlightingData {
  public static final TextAttributesKey ONE_LINE_COMMENT =
    TextAttributesKey.createTextAttributesKey("TS_ONE_LINE_COMMENT", DefaultLanguageHighlighterColors.LINE_COMMENT);
  public static final TextAttributesKey MULTILINE_COMMENT =
    TextAttributesKey.createTextAttributesKey("TS_MULTILINE_COMMENT", DefaultLanguageHighlighterColors.BLOCK_COMMENT);
  public static final TextAttributesKey IGNORED_TEXT =
    TextAttributesKey.createTextAttributesKey("TS_IGNORED_TEXT", DefaultLanguageHighlighterColors.LINE_COMMENT);
  public static final TextAttributesKey OPERATOR_SIGN =
    TextAttributesKey.createTextAttributesKey("TS_OPERATOR_SIGN", DefaultLanguageHighlighterColors.OPERATION_SIGN);
  public static final TextAttributesKey STRING_VALUE =
    TextAttributesKey.createTextAttributesKey("STRING_VALUE", DefaultLanguageHighlighterColors.STRING);
  public static final TextAttributesKey ASSIGNED_VALUE =
    TextAttributesKey.createTextAttributesKey("ASSIGNED_VALUE", HighlighterColors.TEXT);
  public static final TextAttributesKey OBJECT_PATH_ENTITY =
    TextAttributesKey.createTextAttributesKey("TS_OBJECT_PATH_ENTITY", DefaultLanguageHighlighterColors.KEYWORD);
  public static final TextAttributesKey OBJECT_PATH_SEPARATOR =
    TextAttributesKey.createTextAttributesKey("TS_OBJECT_PATH_SEPARATOR", DefaultLanguageHighlighterColors.KEYWORD);
  public static final TextAttributesKey CONDITION =
    TextAttributesKey.createTextAttributesKey("TS_CONDITION", CustomHighlighterColors.CUSTOM_KEYWORD3_ATTRIBUTES);
  public static final TextAttributesKey INCLUDE_STATEMENT =
    TextAttributesKey.createTextAttributesKey("TS_INCLUDE", CustomHighlighterColors.CUSTOM_KEYWORD4_ATTRIBUTES);


  public static final TextAttributesKey BAD_CHARACTER =
    TextAttributesKey.createTextAttributesKey("TS_BAD_CHARACTER", HighlighterColors.BAD_CHARACTER);
}
