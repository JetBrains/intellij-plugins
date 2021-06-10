// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.vuejs.model

import com.intellij.javascript.web.codeInsight.elements.WebSymbolElementDescriptor
import com.intellij.javascript.web.symbols.WebSymbol
import com.intellij.psi.xml.XmlAttribute
import com.intellij.psi.xml.XmlTag
import com.intellij.xml.util.HtmlUtil.TEMPLATE_TAG_NAME

const val DEFAULT_SLOT_NAME = "default"
const val SLOT_NAME_ATTRIBUTE = "name"
const val SLOT_TAG_NAME: String = "slot"

const val DEPRECATED_SLOT_ATTRIBUTE = "slot"

fun getAvailableSlots(attr: XmlAttribute, newApi: Boolean): List<WebSymbol> =
  getAvailableSlots(attr.parent, newApi)

fun getAvailableSlots(tag: XmlTag, newApi: Boolean): List<WebSymbol> =
  if (!newApi || tag.name == TEMPLATE_TAG_NAME)
    (tag.parentTag?.descriptor as? WebSymbolElementDescriptor)?.getSlots() ?: emptyList()
  else
    (tag.descriptor as? WebSymbolElementDescriptor)?.getSlots()
      ?.filter { it.name == DEFAULT_SLOT_NAME } ?: emptyList()

private fun WebSymbolElementDescriptor.getSlots(): List<WebSymbol> =
  runNameMatchQuery(listOf(WebSymbol.KIND_HTML_SLOTS))
