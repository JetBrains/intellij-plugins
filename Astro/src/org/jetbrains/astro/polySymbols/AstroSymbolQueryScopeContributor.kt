// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.astro.polySymbols

import com.intellij.lang.javascript.psi.JSElement
import com.intellij.openapi.util.text.StringUtil
import com.intellij.polySymbols.PolySymbolProperty
import com.intellij.polySymbols.PolySymbolQualifiedKind
import com.intellij.polySymbols.html.NAMESPACE_HTML
import com.intellij.polySymbols.query.PolySymbolQueryScopeContributor
import com.intellij.polySymbols.query.PolySymbolQueryScopeProviderRegistrar
import com.intellij.psi.css.CssElement
import com.intellij.psi.util.parentOfType
import com.intellij.psi.xml.XmlTag
import com.intellij.xml.util.HtmlUtil
import org.jetbrains.astro.AstroFramework
import org.jetbrains.astro.lang.AstroFileImpl
import org.jetbrains.astro.polySymbols.scope.AstroAvailableComponentsScope
import org.jetbrains.astro.polySymbols.scope.AstroFrontmatterScope
import org.jetbrains.astro.polySymbols.scope.AstroScriptDefineVarsScope
import org.jetbrains.astro.polySymbols.scope.AstroStyleDefineVarsScope

val ASTRO_COMPONENTS: PolySymbolQualifiedKind = PolySymbolQualifiedKind[NAMESPACE_HTML, "astro-components"]
val ASTRO_COMPONENT_PROPS: PolySymbolQualifiedKind = PolySymbolQualifiedKind[NAMESPACE_HTML, "props"]

val UI_FRAMEWORK_COMPONENTS: PolySymbolQualifiedKind = PolySymbolQualifiedKind[NAMESPACE_HTML, "ui-framework-components"]
val UI_FRAMEWORK_COMPONENT_PROPS: PolySymbolQualifiedKind = PolySymbolQualifiedKind[NAMESPACE_HTML, "ui-framework-component-props"]

val ASTRO_COMMON_DIRECTIVES: PolySymbolQualifiedKind = PolySymbolQualifiedKind[NAMESPACE_HTML, "astro-common-directives"]
val ASTRO_CLIENT_DIRECTIVES: PolySymbolQualifiedKind = PolySymbolQualifiedKind[NAMESPACE_HTML, "astro-client-directives"]
val ASTRO_STYLE_DIRECTIVES: PolySymbolQualifiedKind = PolySymbolQualifiedKind[NAMESPACE_HTML, "astro-style-directives"]
val ASTRO_SCRIPT_STYLE_DIRECTIVES: PolySymbolQualifiedKind = PolySymbolQualifiedKind[NAMESPACE_HTML, "astro-script-style-directives"]

val PROP_ASTRO_PROXIMITY: PolySymbolProperty<AstroProximity> = PolySymbolProperty["x-astro-proximity"]

class AstroSymbolQueryScopeContributor : PolySymbolQueryScopeContributor {
  override fun registerProviders(registrar: PolySymbolQueryScopeProviderRegistrar) {
    registrar
      .inFile(AstroFileImpl::class.java)
      .inContext { it.framework == AstroFramework.ID }
      .apply {
        // Default scopes
        forAnyPsiLocation()
          .contributeScopeProvider {
            mutableListOf(AstroFrontmatterScope(it.containingFile as AstroFileImpl),
                          AstroAvailableComponentsScope(it.project))
          }

        // AstroStyleDefineVarsScope
        forPsiLocation(CssElement::class.java)
          .contributeScopeProvider { location ->
            location.parentOfType<XmlTag>()
              ?.takeIf { StringUtil.equalsIgnoreCase(it.name, HtmlUtil.STYLE_TAG_NAME) }
              ?.let { listOf(AstroStyleDefineVarsScope(it)) }
            ?: emptyList()
          }

        // AstroScriptDefineVarsScope
        forPsiLocation(JSElement::class.java)
          .contributeScopeProvider { location ->
            location.parentOfType<XmlTag>()
              ?.takeIf { StringUtil.equalsIgnoreCase(it.name, HtmlUtil.SCRIPT_TAG_NAME) }
              ?.let { listOf(AstroScriptDefineVarsScope(it)) }
            ?: emptyList()
          }
      }
  }
}
