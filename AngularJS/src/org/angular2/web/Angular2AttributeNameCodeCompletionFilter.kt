// Copyright 2000-2021 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.web

import com.intellij.html.webSymbols.WebSymbolsHtmlAdditionalContextProvider
import com.intellij.psi.xml.XmlTag
import com.intellij.xml.util.HtmlUtil
import org.angular2.lang.html.parser.Angular2AttributeNameParser
import org.angular2.lang.html.parser.Angular2AttributeType
import org.angular2.lang.html.parser.Angular2AttributeType.*
import org.angular2.lang.html.psi.PropertyBindingType.ATTRIBUTE
import org.angular2.lang.html.psi.PropertyBindingType.PROPERTY
import org.angular2.web.Angular2WebSymbolsAdditionalContextProvider.Companion.EVENT_ATTR_PREFIX
import java.util.function.Predicate

class Angular2AttributeNameCodeCompletionFilter(tag: XmlTag) : Predicate<String> {

  private val names = mutableSetOf<String>()

  override fun test(name: String): Boolean =
    !names.contains(name)

  init {
    WebSymbolsHtmlAdditionalContextProvider.getStandardHtmlAttributeDescriptors(tag)
      .forEach { if (it.name.startsWith(EVENT_ATTR_PREFIX)) names.add(it.name) }

    tag.attributes.asSequence()
      .map { Angular2AttributeNameParser.parse(it.name, tag) }
      .flatMap {
        when (it.type) {
          BANANA_BOX_BINDING -> buildVariants(it.name, REGULAR, PROPERTY_BINDING, BANANA_BOX_BINDING)
          REGULAR -> buildVariants(it.name, BANANA_BOX_BINDING, PROPERTY_BINDING)
          PROPERTY_BINDING -> {
            it as Angular2AttributeNameParser.PropertyBindingInfo
            if (it.bindingType == ATTRIBUTE || it.bindingType == PROPERTY) {
              buildVariants(it.name, BANANA_BOX_BINDING, REGULAR, PROPERTY_BINDING)
            }
            else emptyList()
          }
          EVENT -> buildVariants(it.name, EVENT)
          TEMPLATE_BINDINGS -> buildVariants(it.name, TEMPLATE_BINDINGS)
          else -> emptyList()
        }
      }
      .forEach(names::add)
  }

  private fun buildVariants(name: String,
                            vararg types: Angular2AttributeType): List<String> {
    val result = ArrayList<String>()
    for (type in types) {
      result.add(type.buildName(name, false))
      type.buildName(name, true)?.let { result.add(it) }
      if (type == PROPERTY_BINDING) {
        result.add(type.buildName("attr.$name", false))
        type.buildName("attr.$name", true)?.let { result.add(it) }
      }
    }
    return result.flatMap {
      sequenceOf(it, HtmlUtil.HTML5_DATA_ATTR_PREFIX + it)
    }
  }
}