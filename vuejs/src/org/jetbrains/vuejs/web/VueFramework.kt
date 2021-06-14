// Copyright 2000-2021 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.vuejs.web

import com.intellij.javascript.web.WebFramework
import com.intellij.javascript.web.lang.html.WebFrameworkHtmlFileType
import com.intellij.javascript.web.symbols.SymbolKind
import com.intellij.javascript.web.symbols.WebSymbol
import com.intellij.javascript.web.symbols.WebSymbolsContainer
import com.intellij.lang.javascript.JSStringUtil
import org.jetbrains.vuejs.VuejsIcons
import org.jetbrains.vuejs.codeInsight.fromAsset
import org.jetbrains.vuejs.lang.html.VueFileType
import javax.swing.Icon

class VueFramework : WebFramework() {

  override val displayName: String = "Vue"
  override val icon: Icon = VuejsIcons.Vue
  override val standaloneFileType: WebFrameworkHtmlFileType = VueFileType.INSTANCE
  override val htmlFileType: WebFrameworkHtmlFileType = VueFileType.INSTANCE

  override fun getCanonicalNames(namespace: WebSymbolsContainer.Namespace,
                                 kind: SymbolKind,
                                 name: String,
                                 forQuery: Boolean): List<String> =
    if (namespace == WebSymbolsContainer.Namespace.HTML) {
      if (forQuery) {
        when (kind) {
          WebSymbol.KIND_HTML_VUE_COMPONENTS ->
            listOf(name, fromAsset(name, true))
          WebSymbol.KIND_HTML_VUE_COMPONENT_PROPS ->
            listOf(fromAsset(name))
          else -> emptyList()
        }
      }
      else {
        when (kind) {
          WebSymbol.KIND_HTML_VUE_COMPONENTS ->
            if (name.contains('-'))
              listOf(name)
            else
              listOf(fromAsset(name, true))
          WebSymbol.KIND_HTML_VUE_COMPONENT_PROPS ->
            listOf(fromAsset(name))
          else -> emptyList()
        }
      }

    }
    else emptyList()

  override fun getNameVariants(namespace: WebSymbolsContainer.Namespace, kind: SymbolKind, name: String): List<String> =
    if (namespace == WebSymbolsContainer.Namespace.HTML) {
      when (kind) {
        WebSymbol.KIND_HTML_VUE_COMPONENTS ->  if (name.contains('-'))
          listOf(name)
        else
          listOf(name, fromAsset(name))
        WebSymbol.KIND_HTML_VUE_COMPONENT_PROPS -> listOf(fromAsset(name))
        else -> emptyList()
      }
    }
    else emptyList()

  companion object {
    val instance get() = get("vue")
  }
}