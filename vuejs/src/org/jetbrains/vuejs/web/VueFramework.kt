// Copyright 2000-2021 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.vuejs.web

import com.intellij.javascript.web.WebFramework
import com.intellij.javascript.web.html.WebFrameworkHtmlFileType
import com.intellij.lang.javascript.linter.eslint.service.ESLintLanguageService
import com.intellij.psi.xml.XmlTag
import com.intellij.webSymbols.SymbolKind
import com.intellij.webSymbols.SymbolNamespace
import com.intellij.webSymbols.WebSymbol
import com.intellij.webSymbols.query.WebSymbolNamesProvider
import com.intellij.webSymbols.query.WebSymbolNamesProvider.Target.*
import org.jetbrains.vuejs.VuejsIcons
import org.jetbrains.vuejs.codeInsight.fromAsset
import org.jetbrains.vuejs.codeInsight.toAsset
import org.jetbrains.vuejs.lang.html.VueFileType
import org.jetbrains.vuejs.web.VueWebSymbolsQueryConfigurator.Companion.KIND_VUE_COMPONENTS
import org.jetbrains.vuejs.web.VueWebSymbolsQueryConfigurator.Companion.KIND_VUE_COMPONENT_PROPS
import java.util.function.Predicate
import javax.swing.Icon

class VueFramework : WebFramework() {

  override val displayName: String = "Vue"
  override val icon: Icon = VuejsIcons.Vue
  override val standaloneFileType: WebFrameworkHtmlFileType = VueFileType.INSTANCE
  override val htmlFileType: WebFrameworkHtmlFileType = VueFileType.INSTANCE

  override fun getNames(namespace: SymbolNamespace,
                        kind: SymbolKind,
                        name: String,
                        target: WebSymbolNamesProvider.Target): List<String> =
    when (namespace) {
      WebSymbol.NAMESPACE_HTML -> when (kind) {
        KIND_VUE_COMPONENTS -> when (target) {
          NAMES_QUERY -> listOf(name, fromAsset(name, true))
          NAMES_MAP_STORAGE -> if (name.contains('-'))
            listOf(name)
          else
            listOf(fromAsset(name, true))
          // TODO proposed variant should be taken from code style settings synced from ESLint settings
          CODE_COMPLETION_VARIANTS -> if (name.contains('-'))
            listOf(name)
          else
            listOf(name, fromAsset(name))
        }
        KIND_VUE_COMPONENT_PROPS -> when (target) {
          NAMES_QUERY -> listOf(fromAsset(name))
          NAMES_MAP_STORAGE -> listOf(fromAsset(name))
          CODE_COMPLETION_VARIANTS -> listOf(fromAsset(name))
        }
        else -> emptyList()
      }
      WebSymbol.NAMESPACE_JS -> when (kind) {
        WebSymbol.KIND_JS_EVENTS -> when (target) {
          NAMES_QUERY -> listOf(fromAsset(name), name, fromAsset(name, hyphenBeforeDigit = true))
          NAMES_MAP_STORAGE -> listOf(fromAsset(name, hyphenBeforeDigit = true))
          // TODO proposed variant should be taken from code style settings synced from ESLint settings
          CODE_COMPLETION_VARIANTS -> listOf(fromAsset(name), toAsset(name))
        }
        else -> emptyList()
      }
      else -> emptyList()
    }

  override fun getAttributeNameCodeCompletionFilter(tag: XmlTag): Predicate<String> =
    VueAttributeNameCodeCompletionFilter(tag)

  companion object {
    val instance get() = get("vue")
    const val ID = "vue"
  }
}