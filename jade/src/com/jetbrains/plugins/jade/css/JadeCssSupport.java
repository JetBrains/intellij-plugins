// Copyright 2000-2025 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.jetbrains.plugins.jade.css;

import com.intellij.lang.Language;
import com.intellij.openapi.extensions.ExtensionPointName;
import com.intellij.psi.tree.IElementType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * Optional CSS integration for the Pug (Jade) plugin.
 *
 * <p>The implementation ({@code JadeCssSupportImpl}) lives in the {@code intellij.jade.css} content module and is
 * loaded only when the CSS plugin is available, so the Pug plugin does not hard-require CSS. When CSS is absent the
 * dispatcher methods below degrade gracefully and embedded {@code style} blocks are handled as plain regions.
 */
public interface JadeCssSupport {
  ExtensionPointName<JadeCssSupport> EP_NAME = new ExtensionPointName<>("com.intellij.jade.cssSupport");

  /**
   * Wraps an embedded {@code style}-block token type as a CSS lazy stylesheet element type, or returns {@code null}
   * when the token is not a CSS style block that this support knows how to embed.
   */
  @Nullable IElementType createEmbeddedCssWrapper(@NotNull IElementType token);

  /**
   * The language used to highlight and format embedded {@code style} blocks (i.e. CSS).
   */
  @NotNull Language getStyleBlockLanguage();

  /**
   * Whether the given element type is a CSS stylesheet wrapper produced by {@link #createEmbeddedCssWrapper}.
   */
  boolean isCssStyleBlockWrapper(@NotNull IElementType type);

  /**
   * Returns the registered CSS support, or {@code null} when the CSS plugin (and thus the {@code intellij.jade.css}
   * content module) is not available. Safe to call from lightweight tests where the extension point is not registered.
   */
  static @Nullable JadeCssSupport getInstance() {
    List<JadeCssSupport> extensions = EP_NAME.getExtensionsIfPointIsRegistered();
    return extensions.isEmpty() ? null : extensions.getFirst();
  }
}
