// Copyright 2000-2025 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.intellij.jade.css;

import com.intellij.lang.Language;
import com.intellij.lang.css.CSSLanguage;
import com.intellij.psi.css.impl.CssAdvancedElementTypes;
import com.intellij.psi.css.impl.util.CssStylesheetLazyElementType;
import com.intellij.psi.tree.IElementType;
import com.jetbrains.plugins.jade.css.JadeCssSupport;
import com.jetbrains.plugins.jade.psi.JadeTokenTypes;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * CSS-backed implementation of {@link JadeCssSupport}, contributed by the {@code intellij.jade.css} content module.
 * It is instantiated by the platform through the content-module descriptor when the CSS plugin is available.
 * The class is public so lightweight parser/lexer tests can register it explicitly (see {@code JadeBaseParsingTestCase}).
 */
public final class JadeCssSupportImpl implements JadeCssSupport {
  @Override
  public @Nullable IElementType createEmbeddedCssWrapper(@NotNull IElementType token) {
    CssStylesheetLazyElementType cssType;
    if (token instanceof CssStylesheetLazyElementType) {
      cssType = (CssStylesheetLazyElementType)token;
    }
    else if (token == JadeTokenTypes.STYLE_BLOCK) {
      cssType = CssAdvancedElementTypes.CSS_LAZY_STYLESHEET;
    }
    else {
      return null;
    }
    return new JadeEmbeddedTokenTypesWrapperForCssStylesheet(cssType);
  }

  @Override
  public @NotNull Language getStyleBlockLanguage() {
    return CSSLanguage.INSTANCE;
  }

  @Override
  public boolean isCssStyleBlockWrapper(@NotNull IElementType type) {
    return type instanceof JadeEmbeddedTokenTypesWrapperForCssStylesheet;
  }
}
