// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.intellij.coldFusion.UI.highlighting;

import com.intellij.coldFusion.CfmlBundle;
import com.intellij.coldFusion.model.lexer.CfmlLexer;
import com.intellij.coldFusion.model.lexer.CfmlTokenTypes;
import com.intellij.coldFusion.model.lexer.CfscriptTokenTypes;
import com.intellij.coldFusion.model.parsers.CfmlElementTypes;
import com.intellij.ide.highlighter.HtmlFileType;
import com.intellij.ide.highlighter.custom.CustomHighlighterColors;
import com.intellij.lexer.Lexer;
import com.intellij.openapi.editor.DefaultLanguageHighlighterColors;
import com.intellij.openapi.editor.HighlighterColors;
import com.intellij.openapi.editor.XmlHighlighterColors;
import com.intellij.openapi.editor.colors.EditorColorsScheme;
import com.intellij.openapi.editor.colors.TextAttributesKey;
import com.intellij.openapi.editor.ex.util.LayerDescriptor;
import com.intellij.openapi.editor.ex.util.LayeredLexerEditorHighlighter;
import com.intellij.openapi.fileTypes.SyntaxHighlighterBase;
import com.intellij.openapi.fileTypes.SyntaxHighlighterFactory;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.tree.IElementType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

public final class CfmlHighlighter extends LayeredLexerEditorHighlighter {
  public CfmlHighlighter(final @Nullable Project project,
                         final @Nullable VirtualFile virtualFile,
                         final @NotNull EditorColorsScheme colors) {
    super(new CfmlFileHighlighter(project), colors);
    registerLayer(CfmlElementTypes.TEMPLATE_TEXT, new LayerDescriptor(
      SyntaxHighlighterFactory.getSyntaxHighlighter(HtmlFileType.INSTANCE, project, virtualFile), ""));
  }

  static class CfmlFileHighlighter extends SyntaxHighlighterBase {
    private static final Map<IElementType, TextAttributesKey> keys2;
    private final Project myProject;

    CfmlFileHighlighter(Project project) {
      myProject = project;
    }

    @Override
    public @NotNull Lexer getHighlightingLexer() {
      return new CfmlLexer(true, myProject);
    }

    static final TextAttributesKey CFML_ATTRIBUTE = TextAttributesKey.createTextAttributesKey(
      CfmlBundle.cfmlizeMessage("cfml.attribute"),
      CustomHighlighterColors.CUSTOM_KEYWORD2_ATTRIBUTES
    );

    static final TextAttributesKey CFML_COMMENT = TextAttributesKey.createTextAttributesKey(
      CfmlBundle.cfmlizeMessage("cfml.comment"),
      DefaultLanguageHighlighterColors.DOC_COMMENT
    );

    static final TextAttributesKey CFML_TAG_NAME = TextAttributesKey.createTextAttributesKey(
      CfmlBundle.cfmlizeMessage("cfml.tag.name"),
      XmlHighlighterColors.HTML_TAG_NAME
    );

    static final TextAttributesKey CFML_BRACKETS = TextAttributesKey.createTextAttributesKey(
      CfmlBundle.cfmlizeMessage("cfml.bracket"),
      DefaultLanguageHighlighterColors.BRACES
    );

    static final TextAttributesKey CFML_OPERATOR = TextAttributesKey.createTextAttributesKey(
      CfmlBundle.cfmlizeMessage("cfml.operator"),
      DefaultLanguageHighlighterColors.OPERATION_SIGN
    );

    static final TextAttributesKey CFML_STRING = TextAttributesKey.createTextAttributesKey(
      CfmlBundle.cfmlizeMessage("cfml.string"),
      DefaultLanguageHighlighterColors.STRING
    );

    static final TextAttributesKey CFML_NUMBER = TextAttributesKey.createTextAttributesKey(
      CfmlBundle.cfmlizeMessage("cfml.number"),
      DefaultLanguageHighlighterColors.NUMBER
    );

    static final TextAttributesKey CFML_IDENTIFIER = TextAttributesKey.createTextAttributesKey(
      CfmlBundle.cfmlizeMessage("cfml.identifier"),
      DefaultLanguageHighlighterColors.IDENTIFIER
    );

    static final TextAttributesKey CFML_BAD_CHARACTER = TextAttributesKey.createTextAttributesKey(
      CfmlBundle.cfmlizeMessage("cfml.badcharacter"),
      HighlighterColors.BAD_CHARACTER
    );

    static final TextAttributesKey CFML_SHARP = TextAttributesKey.createTextAttributesKey(
      CfmlBundle.cfmlizeMessage("cfml.sharp"),
      CustomHighlighterColors.CUSTOM_KEYWORD2_ATTRIBUTES
    );

    static final TextAttributesKey CFML_KEYWORD = TextAttributesKey.createTextAttributesKey(
      CfmlBundle.cfmlizeMessage("cfml.keyword"),
      DefaultLanguageHighlighterColors.KEYWORD
    );

    static {
      keys2 = new HashMap<>();

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
       //fillMap(keys2, CfscriptTokenTypes.STRING_ELEMENTS, CFML_STRING);
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

    @Override
    public TextAttributesKey @NotNull [] getTokenHighlights(IElementType tokenType) {
      return pack(keys2.get(tokenType));
    }
  }
}
