// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.vuejs.model

import com.intellij.javascript.web.codeInsight.html.attributes.WebSymbolAttributeDescriptor
import com.intellij.javascript.web.codeInsight.html.elements.WebSymbolElementDescriptor
import com.intellij.javascript.web.symbols.WebSymbol
import com.intellij.javascript.web.symbols.WebSymbolCodeCompletionItem
import com.intellij.javascript.web.symbols.WebSymbolsContainer
import com.intellij.lang.javascript.psi.JSType
import com.intellij.psi.PsiElement
import com.intellij.psi.util.parentOfType
import com.intellij.psi.xml.XmlAttribute
import com.intellij.psi.xml.XmlTag
import com.intellij.util.castSafelyTo
import com.intellij.xml.util.HtmlUtil.TEMPLATE_TAG_NAME
import org.jetbrains.vuejs.codeInsight.attributes.VueAttributeNameParser
import org.jetbrains.vuejs.types.asCompleteType

const val DEFAULT_SLOT_NAME = "default"
const val SLOT_NAME_ATTRIBUTE = "name"
const val SLOT_TAG_NAME: String = "slot"

const val DEPRECATED_SLOT_ATTRIBUTE = "slot"

fun getAvailableSlots(tag: XmlTag, name: String?, newApi: Boolean): List<WebSymbol> =
  if (!newApi || tag.name == TEMPLATE_TAG_NAME)
    (tag.parentTag?.descriptor as? WebSymbolElementDescriptor)?.getSlots(name) ?: emptyList()
  else
    (tag.descriptor as? WebSymbolElementDescriptor)?.getSlots(name)
      ?.filter { it.name == DEFAULT_SLOT_NAME } ?: emptyList()

fun getAvailableSlotsCompletions(tag: XmlTag, name: String?, position: Int, newApi: Boolean): List<WebSymbolCodeCompletionItem> =
  if (!newApi || tag.name == TEMPLATE_TAG_NAME)
    (tag.parentTag?.descriptor as? WebSymbolElementDescriptor)?.getSlotsCompletions(name, position) ?: emptyList()
  else
    (tag.descriptor as? WebSymbolElementDescriptor)?.getSlotsCompletions(name, position)
      ?.filter { it.name == DEFAULT_SLOT_NAME } ?: emptyList()

fun getSlotTypeFromContext(context: PsiElement): JSType? =
  context.parentOfType<XmlAttribute>()
    ?.takeIf { attribute ->
      VueAttributeNameParser.parse(attribute.name, attribute.parent).let {
        it is VueAttributeNameParser.VueDirectiveInfo
        && it.directiveKind == VueAttributeNameParser.VueDirectiveKind.SLOT
      }
    }
    ?.descriptor
    ?.castSafelyTo<WebSymbolAttributeDescriptor>()
    ?.symbol
    ?.jsType
    ?.asCompleteType()

private fun WebSymbolElementDescriptor.getSlots(name: String?): List<WebSymbol> =
  runNameMatchQuery(listOfNotNull(WebSymbolsContainer.NAMESPACE_HTML, WebSymbol.KIND_HTML_SLOTS, name))

private fun WebSymbolElementDescriptor.getSlotsCompletions(name: String?, position: Int): List<WebSymbolCodeCompletionItem> =
  runCodeCompletionQuery(listOfNotNull(WebSymbolsContainer.NAMESPACE_HTML, WebSymbol.KIND_HTML_SLOTS, name), position)
