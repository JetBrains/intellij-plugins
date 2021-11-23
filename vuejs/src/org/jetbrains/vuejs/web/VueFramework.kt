// Copyright 2000-2021 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.vuejs.web

import com.intellij.javascript.web.WebFramework
import com.intellij.javascript.web.lang.html.WebFrameworkHtmlFileType
import com.intellij.javascript.web.symbols.SymbolKind
import com.intellij.javascript.web.symbols.WebSymbolNamesProvider
import com.intellij.javascript.web.symbols.WebSymbolsContainer
import com.intellij.psi.xml.XmlTag
import org.jetbrains.vuejs.VuejsIcons
import org.jetbrains.vuejs.codeInsight.fromAsset
import org.jetbrains.vuejs.lang.html.VueFileType
import org.jetbrains.vuejs.web.VueWebSymbolsAdditionalContextProvider.Companion.KIND_VUE_COMPONENTS
import org.jetbrains.vuejs.web.VueWebSymbolsAdditionalContextProvider.Companion.KIND_VUE_COMPONENT_PROPS
import java.util.function.Predicate
import javax.swing.Icon

class VueFramework : WebFramework() {

  override val displayName: String = "Vue"
  override val icon: Icon = VuejsIcons.Vue
  override val standaloneFileType: WebFrameworkHtmlFileType = VueFileType.INSTANCE
  override val htmlFileType: WebFrameworkHtmlFileType = VueFileType.INSTANCE

  override fun getNames(namespace: WebSymbolsContainer.Namespace,
                        kind: SymbolKind,
                        name: String,
                        target: WebSymbolNamesProvider.Target): List<String> =
    if (namespace == WebSymbolsContainer.Namespace.HTML)
      when (target) {
        WebSymbolNamesProvider.Target.NAMES_QUERY ->
          when (kind) {
            KIND_VUE_COMPONENTS ->
              listOf(name, fromAsset(name, true))
            KIND_VUE_COMPONENT_PROPS ->
              listOf(fromAsset(name))
            else -> emptyList()
          }
        WebSymbolNamesProvider.Target.NAMES_MAP_STORAGE ->
          when (kind) {
            KIND_VUE_COMPONENTS ->
              if (name.contains('-'))
                listOf(name)
              else
                listOf(fromAsset(name, true))
            KIND_VUE_COMPONENT_PROPS ->
              listOf(fromAsset(name))
            else -> emptyList()
          }
        WebSymbolNamesProvider.Target.CODE_COMPLETION_VARIANTS ->
          when (kind) {
            KIND_VUE_COMPONENTS -> if (name.contains('-'))
              listOf(name)
            else
              listOf(name, fromAsset(name))
            KIND_VUE_COMPONENT_PROPS -> listOf(fromAsset(name))
            else -> emptyList()
          }
      }
    else emptyList()

  override fun getAttributeNameCodeCompletionFilter(tag: XmlTag): Predicate<String> =
    VueAttributeNameCodeCompletionFilter(tag)

  companion object {
    val instance get() = get("vue")
    const val ID = "vue"
  }
}