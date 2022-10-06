// Copyright 2000-2021 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.vuejs.web

import com.intellij.html.webSymbols.attributes.WebSymbolAttributeDescriptor
import com.intellij.psi.xml.XmlAttribute
import com.intellij.psi.xml.XmlTag
import com.intellij.webSymbols.WebSymbol.Companion.NAMESPACE_HTML
import com.intellij.webSymbols.registry.WebSymbolsRegistryManager
import com.intellij.webSymbols.utils.hideFromCompletion
import org.jetbrains.vuejs.codeInsight.ATTR_ARGUMENT_PREFIX
import org.jetbrains.vuejs.codeInsight.ATTR_DIRECTIVE_PREFIX
import org.jetbrains.vuejs.codeInsight.ATTR_SLOT_SHORTHAND
import org.jetbrains.vuejs.codeInsight.attributes.VueAttributeNameParser
import org.jetbrains.vuejs.web.VueWebSymbolsRegistryExtension.Companion.KIND_VUE_DIRECTIVE_ARGUMENT
import java.util.function.Predicate

class VueAttributeNameCodeCompletionFilter(tag: XmlTag) : Predicate<String> {

  private val names = mutableSetOf<String>()

  init {
    tag.attributes.forEach { addAliases(it) }
  }

  override fun test(name: String): Boolean =
    !names.contains(name)

  private fun addAliases(attr: XmlAttribute) {
    val info = VueAttributeNameParser.parse(attr.name, attr.parent)
    names.add(attr.name)
    if (info is VueAttributeNameParser.VueDirectiveInfo) {
      val descriptor = attr.descriptor as? WebSymbolAttributeDescriptor
      when (info.directiveKind) {
        VueAttributeNameParser.VueDirectiveKind.ON -> return
        VueAttributeNameParser.VueDirectiveKind.BIND -> {
          if (info.arguments != null) {
            names.add(ATTR_ARGUMENT_PREFIX + info.arguments)
            names.add(ATTR_DIRECTIVE_PREFIX + info.name + ATTR_ARGUMENT_PREFIX + info.arguments)
          }
          else {
            return
          }
        }
        VueAttributeNameParser.VueDirectiveKind.SLOT -> {
          names.add(ATTR_SLOT_SHORTHAND.toString())
          names.add(ATTR_DIRECTIVE_PREFIX + info.name)
          names.add(ATTR_DIRECTIVE_PREFIX + info.name + ATTR_ARGUMENT_PREFIX)
        }
        else -> {
          val symbol = descriptor?.symbol
          if (symbol != null
              && WebSymbolsRegistryManager.get(attr)
                .runNameMatchQuery(listOf(NAMESPACE_HTML, KIND_VUE_DIRECTIVE_ARGUMENT), context = listOf(symbol))
                .count { !it.hideFromCompletion } == 0
          ) {
            names.add(ATTR_DIRECTIVE_PREFIX + info.name)
          }
        }
      }
    }
    else {
      names.add(info.name)
    }
  }
}