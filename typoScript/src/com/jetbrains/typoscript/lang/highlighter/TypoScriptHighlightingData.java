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
import com.intellij.openapi.editor.HighlighterColors;
import com.intellij.openapi.editor.SyntaxHighlighterColors;
import com.intellij.openapi.editor.colors.TextAttributesKey;
import com.intellij.openapi.editor.colors.TextAttributesKeyDefaults;


public class TypoScriptHighlightingData {
  public static final TextAttributesKey ONE_LINE_COMMENT =
    TextAttributesKeyDefaults.createTextAttributesKey("TS_ONE_LINE_COMMENT", TextAttributesKeyDefaults
      .getDefaultAttributes(SyntaxHighlighterColors.LINE_COMMENT));
  public static final TextAttributesKey MULTILINE_COMMENT =
    TextAttributesKeyDefaults.createTextAttributesKey("TS_MULTILINE_COMMENT", TextAttributesKeyDefaults
      .getDefaultAttributes(SyntaxHighlighterColors.JAVA_BLOCK_COMMENT));
  public static final TextAttributesKey IGNORED_TEXT =
    TextAttributesKeyDefaults.createTextAttributesKey("TS_IGNORED_TEXT", TextAttributesKeyDefaults
      .getDefaultAttributes(SyntaxHighlighterColors.LINE_COMMENT));
  public static final TextAttributesKey OPERATOR_SIGN =
    TextAttributesKeyDefaults.createTextAttributesKey("TS_OPERATOR_SIGN", TextAttributesKeyDefaults
      .getDefaultAttributes(SyntaxHighlighterColors.OPERATION_SIGN));
  public static final TextAttributesKey STRING_VALUE =
    TextAttributesKeyDefaults
      .createTextAttributesKey("STRING_VALUE", TextAttributesKeyDefaults.getDefaultAttributes(SyntaxHighlighterColors.STRING));
  public static final TextAttributesKey ASSIGNED_VALUE =
    TextAttributesKeyDefaults
      .createTextAttributesKey("ASSIGNED_VALUE", TextAttributesKeyDefaults.getDefaultAttributes(HighlighterColors.TEXT));
  public static final TextAttributesKey OBJECT_PATH_ENTITY =
    TextAttributesKeyDefaults.createTextAttributesKey("TS_OBJECT_PATH_ENTITY", TextAttributesKeyDefaults
      .getDefaultAttributes(SyntaxHighlighterColors.KEYWORD));
  public static final TextAttributesKey OBJECT_PATH_SEPARATOR =
    TextAttributesKeyDefaults.createTextAttributesKey("TS_OBJECT_PATH_SEPARATOR", TextAttributesKeyDefaults
      .getDefaultAttributes(SyntaxHighlighterColors.KEYWORD));
  public static final TextAttributesKey CONDITION =
    TextAttributesKeyDefaults.createTextAttributesKey("TS_CONDITION", TextAttributesKeyDefaults
      .getDefaultAttributes(CustomHighlighterColors.CUSTOM_KEYWORD3_ATTRIBUTES));
  public static final TextAttributesKey INCLUDE_STATEMENT =
    TextAttributesKeyDefaults.createTextAttributesKey("TS_INCLUDE", TextAttributesKeyDefaults
      .getDefaultAttributes(CustomHighlighterColors.CUSTOM_KEYWORD4_ATTRIBUTES));


  public static final TextAttributesKey BAD_CHARACTER =
    TextAttributesKeyDefaults.createTextAttributesKey("TS_BAD_CHARACTER", TextAttributesKeyDefaults
      .getDefaultAttributes(HighlighterColors.BAD_CHARACTER));
}
