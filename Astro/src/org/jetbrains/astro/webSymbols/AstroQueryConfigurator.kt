// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.astro.webSymbols

import com.intellij.lang.javascript.psi.JSElement
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.text.StringUtil
import com.intellij.psi.PsiElement
import com.intellij.psi.css.CssElement
import com.intellij.psi.util.parentOfType
import com.intellij.psi.xml.XmlTag
import com.intellij.webSymbols.WebSymbol.Companion.NAMESPACE_HTML
import com.intellij.webSymbols.WebSymbolQualifiedKind
import com.intellij.webSymbols.WebSymbolsScope
import com.intellij.webSymbols.context.WebSymbolsContext
import com.intellij.webSymbols.query.WebSymbolsQueryConfigurator
import com.intellij.xml.util.HtmlUtil
import org.jetbrains.astro.AstroFramework
import org.jetbrains.astro.lang.AstroFileImpl
import org.jetbrains.astro.webSymbols.scope.AstroAvailableComponentsScope
import org.jetbrains.astro.webSymbols.scope.AstroFrontmatterScope
import org.jetbrains.astro.webSymbols.scope.AstroScriptDefineVarsScope
import org.jetbrains.astro.webSymbols.scope.AstroStyleDefineVarsScope

val ASTRO_COMPONENTS = WebSymbolQualifiedKind(NAMESPACE_HTML, "astro-components")
val ASTRO_COMPONENT_PROPS = WebSymbolQualifiedKind(NAMESPACE_HTML, "props")

val UI_FRAMEWORK_COMPONENTS = WebSymbolQualifiedKind(NAMESPACE_HTML, "ui-framework-components")
val UI_FRAMEWORK_COMPONENT_PROPS = WebSymbolQualifiedKind(NAMESPACE_HTML, "ui-framework-component-props")

val ASTRO_COMMON_DIRECTIVES = WebSymbolQualifiedKind(NAMESPACE_HTML, "astro-common-directives")
val ASTRO_CLIENT_DIRECTIVES = WebSymbolQualifiedKind(NAMESPACE_HTML, "astro-client-directives")
val ASTRO_STYLE_DIRECTIVES = WebSymbolQualifiedKind(NAMESPACE_HTML, "astro-style-directives")
val ASTRO_SCRIPT_STYLE_DIRECTIVES = WebSymbolQualifiedKind(NAMESPACE_HTML, "astro-script-style-directives")

const val PROP_ASTRO_PROXIMITY = "x-astro-proximity"

class AstroQueryConfigurator : WebSymbolsQueryConfigurator {

  override fun getScope(project: Project, location: PsiElement?, context: WebSymbolsContext, allowResolve: Boolean): List<WebSymbolsScope> {
    if (context.framework != AstroFramework.ID || location?.containingFile !is AstroFileImpl) return emptyList()
    return when (location) {
      is CssElement -> calculateCssScopes(location)
      is JSElement -> calculateJsScopes(location)
      else -> calculateDefaultScopes(location)
    }
  }

  private fun calculateCssScopes(location: CssElement): MutableList<WebSymbolsScope> {
    val result = calculateDefaultScopes(location)
    location.parentOfType<XmlTag>()
      ?.takeIf { StringUtil.equalsIgnoreCase(it.name, HtmlUtil.STYLE_TAG_NAME) }
      ?.let { result.add(AstroStyleDefineVarsScope(it)) }
    return result
  }

  private fun calculateJsScopes(location: JSElement): MutableList<WebSymbolsScope> {
    val result = calculateDefaultScopes(location)
    location.parentOfType<XmlTag>()
      ?.takeIf { StringUtil.equalsIgnoreCase(it.name, HtmlUtil.SCRIPT_TAG_NAME) }
      ?.let { result.add(AstroScriptDefineVarsScope(it)) }
    return result
  }

  private fun calculateDefaultScopes(location: PsiElement): MutableList<WebSymbolsScope> =
    mutableListOf(AstroFrontmatterScope(location.containingFile as AstroFileImpl),
                  AstroAvailableComponentsScope(location.project))
}
