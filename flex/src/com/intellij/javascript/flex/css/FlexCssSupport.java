// Copyright 2000-2025 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.intellij.javascript.flex.css;

import com.intellij.openapi.extensions.ExtensionPointName;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReference;
import com.intellij.psi.PsiReferenceProvider;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.awt.Color;
import java.util.List;

/**
 * Optional CSS integration for the Flash/Flex plugin.
 *
 * <p>The implementation ({@code FlexCssSupportImpl}) lives in the {@code intellij.flex.css} content module and is
 * loaded only when the CSS plugin is available, so the Flash/Flex plugin does not hard-require CSS. When CSS is
 * absent the dispatcher methods degrade gracefully (no CSS references, resolves, color conversion, etc.).
 */
public interface FlexCssSupport {
  ExtensionPointName<FlexCssSupport> EP_NAME = new ExtensionPointName<>("com.intellij.flex.cssSupport");

  /**
   * Returns the (kebab-to-camelCase) name of a CSS class marker element, or {@code null} when the element is not a
   * CSS class marker. Used by ActionScript resolve to expose CSS class selectors as ActionScript names.
   */
  @Nullable String cssClassMarkerName(@NotNull PsiElement element);

  /**
   * Whether the given element is a CSS string literal.
   */
  boolean isCssString(@Nullable PsiElement element);

  /**
   * Converts an AWT color to the canonical CSS hex string (e.g. for the MXML color picker).
   */
  @NotNull String toCssHexString(@NotNull Color color);

  /**
   * Creates a reference for a Flex CSS property-name value (e.g. {@code setStyle("...")}).
   */
  @NotNull PsiReference createCssPropertyValueReference(@NotNull PsiElement element);

  /**
   * Creates a reference for a Flex CSS class (style-name) value.
   */
  @NotNull PsiReference createCssClassValueReference(@NotNull PsiElement element);

  /**
   * The CSS class-or-id reference provider used for MXML {@code styleName} attributes.
   */
  @NotNull PsiReferenceProvider cssClassOrIdReferenceProvider();

  /**
   * Collects UML dependencies declared through {@code ClassReference(...)} inside CSS injected into MXML.
   *
   * @param injectedRoot               the root PSI of the injected fragment (may be any language; non-CSS is ignored)
   * @param classReferenceFunctionName the name of the CSS function that declares class references
   */
  @NotNull List<InjectedCssClassDependency> collectInjectedCssClassDependencies(@NotNull PsiElement injectedRoot,
                                                                                @NotNull String classReferenceFunctionName);

  /**
   * Returns the registered CSS support, or {@code null} when the CSS plugin (and thus the {@code intellij.flex.css}
   * content module) is not available. Safe to call from lightweight tests where the extension point is not registered.
   */
  static @Nullable FlexCssSupport getInstance() {
    List<FlexCssSupport> extensions = EP_NAME.getExtensionsIfPointIsRegistered();
    return extensions.isEmpty() ? null : extensions.getFirst();
  }

  /**
   * A single UML dependency collected from injected CSS: the CSS declaration property name, the anchor element to
   * attach the dependency to, and the references discovered on the CSS class value.
   */
  record InjectedCssClassDependency(@NotNull String dependencyName, @NotNull PsiElement anchor,
                                    PsiReference @NotNull [] references) {
  }
}
