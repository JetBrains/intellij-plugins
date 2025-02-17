// Copyright 2000-2025 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.jetbrains.plugins.jade.highlighter;

import com.intellij.application.options.CodeStyle;
import com.intellij.lang.Language;
import com.intellij.lang.css.CSSLanguage;
import com.intellij.lang.html.HTMLLanguage;
import com.intellij.lexer.EmbeddedTokenTypesProvider;
import com.intellij.lexer.Lexer;
import com.intellij.openapi.editor.colors.EditorColorsScheme;
import com.intellij.openapi.editor.ex.util.LayerDescriptor;
import com.intellij.openapi.editor.ex.util.LayeredLexerEditorHighlighter;
import com.intellij.openapi.fileTypes.SyntaxHighlighter;
import com.intellij.openapi.fileTypes.SyntaxHighlighterFactory;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.tree.IElementType;
import com.intellij.psi.xml.XmlElementType;
import com.jetbrains.plugins.jade.js.JavaScriptInJadeLanguageDialect;
import com.jetbrains.plugins.jade.lexer.JadeEmbeddingUtil;
import com.jetbrains.plugins.jade.psi.JadeTokenTypes;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class JadeEditorHighlighter extends LayeredLexerEditorHighlighter {
  public JadeEditorHighlighter(final @Nullable Project project,
                               final @Nullable VirtualFile virtualFile,
                               final @NotNull EditorColorsScheme colors) {
    super(new JadeSyntaxHighlighter(project != null ? CodeStyle.getSettings(project) : CodeStyle.getDefaultSettings()), colors);
    registerHTMLLayer(project, virtualFile);
    registerCSSLayer(project, virtualFile);
    registerJSLayers(project, virtualFile);
    registerEmbeddedTokenTypeProviderLayers(project, virtualFile);
  }

  private void registerEmbeddedTokenTypeProviderLayers(Project project, VirtualFile virtualFile) {
    for (EmbeddedTokenTypesProvider provider : EmbeddedTokenTypesProvider.getProviders()) {
      IElementType embeddedTokenType = provider.getElementType();
      Language language = embeddedTokenType.getLanguage();
      registerLayer(project, virtualFile, language, embeddedTokenType);
      registerLayer(project, virtualFile, language, JadeEmbeddingUtil.getEmbeddedTokenWrapperType(embeddedTokenType));
    }
  }

  private void registerJSLayers(Project project, VirtualFile virtualFile) {
    SyntaxHighlighter jsHighlighter = SyntaxHighlighterFactory.getSyntaxHighlighter(JavaScriptInJadeLanguageDialect.INSTANCE, project, virtualFile);
    LayerDescriptor jsLayer = new LayerDescriptor(jsHighlighter, "\n", JadeHighlighter.JS_BLOCK);
    for (IElementType tokenType : JadeTokenTypes.JS_TOKENS.getTypes()) {
      registerLayer(tokenType, jsLayer);
    }
  }

  private void registerCSSLayer(Project project, VirtualFile virtualFile) {
    registerLayer(project, virtualFile, CSSLanguage.INSTANCE, JadeTokenTypes.STYLE_BLOCK);
    registerLayer(project, virtualFile, CSSLanguage.INSTANCE, JadeEmbeddingUtil.getEmbeddedTokenWrapperType(JadeTokenTypes.STYLE_BLOCK));
  }


  private void registerHTMLLayer(Project project, VirtualFile virtualFile) {
    registerLayer(project, virtualFile, HTMLLanguage.INSTANCE, XmlElementType.HTML_EMBEDDED_CONTENT);
  }

  private void registerLayer(Project project, VirtualFile virtualFile, Language language, IElementType tokenType) {
    SyntaxHighlighter cssHighlighter = SyntaxHighlighterFactory.getSyntaxHighlighter(language, project, virtualFile);
    final LayerDescriptor cssLayer = new LayerDescriptor(cssHighlighter, "\n");
    registerLayer(tokenType, cssLayer);
  }

  @Override
  public @NotNull Lexer getLexer() {
    return super.getLexer();
  }
}
