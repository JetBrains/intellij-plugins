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
package com.intellij.coldFusion.UI.highlighting;

import com.intellij.coldFusion.CfmlBundle;
import com.intellij.coldFusion.model.lexer.CfmlLexer;
import com.intellij.coldFusion.model.lexer.CfmlTokenTypes;
import com.intellij.coldFusion.model.lexer.CfscriptTokenTypes;
import com.intellij.coldFusion.model.parsers.CfmlElementTypes;
import com.intellij.ide.highlighter.custom.CustomHighlighterColors;
import com.intellij.lexer.LayeredLexer;
import com.intellij.lexer.Lexer;
import com.intellij.openapi.editor.DefaultLanguageHighlighterColors;
import com.intellij.openapi.editor.HighlighterColors;
import com.intellij.openapi.editor.XmlHighlighterColors;
import com.intellij.openapi.editor.colors.EditorColorsScheme;
import com.intellij.openapi.editor.colors.TextAttributesKey;
import com.intellij.openapi.editor.ex.util.LayerDescriptor;
import com.intellij.openapi.editor.ex.util.LayeredLexerEditorHighlighter;
import com.intellij.openapi.fileTypes.StdFileTypes;
import com.intellij.openapi.fileTypes.SyntaxHighlighterBase;
import com.intellij.openapi.fileTypes.SyntaxHighlighterFactory;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.tree.IElementType;
import com.intellij.sql.psi.SqlFileType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Lera Nikolaenko
 * Date: 06.10.2008
 */
public class CfmlHighlighter extends LayeredLexerEditorHighlighter {
  public CfmlHighlighter(@Nullable final Project project,
                         @Nullable final VirtualFile virtualFile,
                         @NotNull final EditorColorsScheme colors) {
    super(new CfmlFileHighlighter(project), colors);
    registerLayer(CfmlElementTypes.TEMPLATE_TEXT, new LayerDescriptor(
      SyntaxHighlighterFactory.getSyntaxHighlighter(StdFileTypes.HTML, project, virtualFile), ""));
    registerLayer(CfmlElementTypes.SQL,
                  new LayerDescriptor(SyntaxHighlighterFactory.getSyntaxHighlighter(SqlFileType.INSTANCE, project, virtualFile), ""));
  }

  static class CfmlFileHighlighter extends SyntaxHighlighterBase {
    private static Map<IElementType, TextAttributesKey> keys2;
    private Project myProject;

    CfmlFileHighlighter(Project project) {
      myProject = project;
    }

    @NotNull
    public Lexer getHighlightingLexer() {
      return new LayeredLexer(new CfmlLexer(true, myProject));
    }

    static final TextAttributesKey CFML_ATTRIBUTE = TextAttributesKey.createTextAttributesKey(
      CfmlBundle.message("cfml.attribute"),
      CustomHighlighterColors.CUSTOM_KEYWORD2_ATTRIBUTES
    );

    static final TextAttributesKey CFML_COMMENT = TextAttributesKey.createTextAttributesKey(
      CfmlBundle.message("cfml.comment"),
      DefaultLanguageHighlighterColors.DOC_COMMENT
    );

    static final TextAttributesKey CFML_TAG_NAME = TextAttributesKey.createTextAttributesKey(
      CfmlBundle.message("cfml.tag.name"),
      XmlHighlighterColors.HTML_TAG_NAME
    );

    static final TextAttributesKey CFML_BRACKETS = TextAttributesKey.createTextAttributesKey(
      CfmlBundle.message("cfml.bracket"),
      DefaultLanguageHighlighterColors.BRACES
    );

    static final TextAttributesKey CFML_OPERATOR = TextAttributesKey.createTextAttributesKey(
      CfmlBundle.message("cfml.operator"),
      DefaultLanguageHighlighterColors.OPERATION_SIGN
    );

    static final TextAttributesKey CFML_STRING = TextAttributesKey.createTextAttributesKey(
      "Cfml" + CfmlBundle.message("cfml.string"),
      DefaultLanguageHighlighterColors.STRING
    );

    static final TextAttributesKey CFML_NUMBER = TextAttributesKey.createTextAttributesKey(
      CfmlBundle.message("cfml.number"),
      DefaultLanguageHighlighterColors.NUMBER
    );

    static final TextAttributesKey CFML_IDENTIFIER = TextAttributesKey.createTextAttributesKey(
      CfmlBundle.message("cfml.identifier"),
      DefaultLanguageHighlighterColors.IDENTIFIER
    );

    static final TextAttributesKey CFML_BAD_CHARACTER = TextAttributesKey.createTextAttributesKey(
      CfmlBundle.message("cfml.badcharacter"),
      HighlighterColors.BAD_CHARACTER
    );

    static final TextAttributesKey CFML_SHARP = TextAttributesKey.createTextAttributesKey(
      CfmlBundle.message("cfml.sharp"),
      CustomHighlighterColors.CUSTOM_KEYWORD2_ATTRIBUTES
    );

    static final TextAttributesKey CFML_KEYWORD = TextAttributesKey.createTextAttributesKey(
      CfmlBundle.message("cfml.keyword"),
      DefaultLanguageHighlighterColors.KEYWORD
    );

    static {
      keys2 = new HashMap<IElementType, TextAttributesKey>();

      fillMap(keys2, CfmlTokenTypes.BRACKETS, CFML_BRACKETS);
      fillMap(keys2, CfmlTokenTypes.STRING_ELEMENTS, CFML_STRING);
      // keys2.put(CfmlCompositeElements.TAG, XmlHighlighterColors.HTML_TAG);
      keys2.put(CfmlTokenTypes.ASSIGN, CFML_OPERATOR);
      keys2.put(CfmlTokenTypes.START_EXPRESSION, CFML_SHARP);
      keys2.put(CfmlTokenTypes.CF_TAG_NAME, CFML_TAG_NAME);
      keys2.put(CfmlTokenTypes.ATTRIBUTE, CFML_ATTRIBUTE);
      keys2.put(CfmlTokenTypes.END_EXPRESSION, CFML_SHARP);
      keys2.put(CfmlTokenTypes.COMMENT, CFML_COMMENT);
      keys2.put(CfmlTokenTypes.VAR_ANNOTATION, CFML_COMMENT);
      // for script language
      fillMap(keys2, CfscriptTokenTypes.OPERATIONS, CFML_OPERATOR);
      fillMap(keys2, CfscriptTokenTypes.BRACKETS, CFML_BRACKETS);
      // fillMap(keys2, CfscriptTokenTypes.STRING_ELEMENTS, CFML_STRING);
      fillMap(keys2, CfscriptTokenTypes.WORD_OPERATIONS, CFML_KEYWORD);
      fillMap(keys2, CfscriptTokenTypes.KEYWORDS, CFML_KEYWORD);
      keys2.put(CfscriptTokenTypes.INTEGER, CFML_NUMBER);
      keys2.put(CfscriptTokenTypes.DOUBLE, CFML_NUMBER);
      keys2.put(CfscriptTokenTypes.COMMENT, CFML_COMMENT);
      keys2.put(CfscriptTokenTypes.IDENTIFIER, CFML_IDENTIFIER);
      keys2.put(CfscriptTokenTypes.BAD_CHARACTER, CFML_BAD_CHARACTER);
      keys2.put(CfscriptTokenTypes.OPENSHARP, CFML_SHARP);
      keys2.put(CfscriptTokenTypes.CLOSESHARP, CFML_SHARP);
    }

    @NotNull
    public TextAttributesKey[] getTokenHighlights(IElementType tokenType) {
      return pack(keys2.get(tokenType));
    }
  }
}
