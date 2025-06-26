// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.vuejs.model

import com.intellij.polySymbols.html.attributes.HtmlAttributeSymbolDescriptor
import com.intellij.polySymbols.html.elements.HtmlElementSymbolDescriptor
import com.intellij.polySymbols.js.jsType
import com.intellij.lang.javascript.psi.JSProperty
import com.intellij.lang.javascript.psi.JSType
import com.intellij.lang.javascript.psi.StubSafe
import com.intellij.lang.javascript.psi.ecma6.TypeScriptAsExpression
import com.intellij.lang.javascript.psi.types.JSGenericTypeImpl
import com.intellij.lang.javascript.psi.types.JSNamedType
import com.intellij.polySymbols.PolySymbol
import com.intellij.polySymbols.PolySymbolOrigin
import com.intellij.polySymbols.completion.PolySymbolCodeCompletionItem
import com.intellij.polySymbols.html.HTML_SLOTS
import com.intellij.polySymbols.query.PolySymbolNameMatchQueryParams
import com.intellij.polySymbols.query.PolySymbolQueryExecutorFactory
import com.intellij.polySymbols.query.PolySymbolQueryStack
import com.intellij.polySymbols.utils.match
import com.intellij.psi.PsiElement
import com.intellij.psi.util.parentOfType
import com.intellij.psi.xml.XmlAttribute
import com.intellij.psi.xml.XmlTag
import com.intellij.util.asSafely
import com.intellij.xml.impl.schema.AnyXmlElementDescriptor
import com.intellij.xml.util.HtmlUtil.TEMPLATE_TAG_NAME
import org.jetbrains.vuejs.codeInsight.attributes.VueAttributeNameParser
import org.jetbrains.vuejs.types.asCompleteType
import org.jetbrains.vuejs.web.symbols.VueAnySymbol

const val DEFAULT_SLOT_NAME: String = "default"
const val SLOT_NAME_ATTRIBUTE: String = "name"

const val DEPRECATED_SLOT_ATTRIBUTE: String = "slot"

fun getMatchingAvailableSlots(tag: XmlTag, name: String, newApi: Boolean): List<PolySymbol> =
  processSlots(
    tag = tag,
    newApi = newApi,
    anyMatch = { anySlot.match(name, PolySymbolNameMatchQueryParams.create(PolySymbolQueryExecutorFactory.getInstance(tag.project).create(null)), PolySymbolQueryStack()) },
    process = { runNameMatchQuery(HTML_SLOTS.withName(name)) },
  )

fun getAvailableSlots(tag: XmlTag, expandPatterns: Boolean, newApi: Boolean): List<PolySymbol> =
  processSlots(tag, newApi, { emptyList() }) {
    runListSymbolsQuery(HTML_SLOTS, expandPatterns)
  }

fun getAvailableSlotsCompletions(tag: XmlTag, name: String, position: Int, newApi: Boolean): List<PolySymbolCodeCompletionItem> =
  processSlots(tag, newApi, { emptyList() }) {
    runCodeCompletionQuery(HTML_SLOTS, name, position)
  }

private val anySlot = VueAnySymbol(
  PolySymbolOrigin.empty(),
  HTML_SLOTS,
  "Unknown slot"
)

private fun <T> processSlots(
  tag: XmlTag,
  newApi: Boolean,
  anyMatch: () -> List<T>,
  process: HtmlElementSymbolDescriptor.() -> List<T>,
): List<T> =
  when (val descriptor = if (!newApi || tag.name == TEMPLATE_TAG_NAME) tag.parentTag?.descriptor else tag.descriptor) {
    is HtmlElementSymbolDescriptor -> descriptor.process()
    is AnyXmlElementDescriptor -> anyMatch()
    else -> emptyList()
  }


fun getSlotTypeFromContext(context: PsiElement): JSType? =
  context.parentOfType<XmlAttribute>()
    ?.takeIf { attribute ->
      VueAttributeNameParser.parse(attribute.name, attribute.parent).let {
        it is VueAttributeNameParser.VueDirectiveInfo
        && it.directiveKind == VueAttributeNameParser.VueDirectiveKind.SLOT
      }
    }
    ?.descriptor
    ?.asSafely<HtmlAttributeSymbolDescriptor>()
    ?.symbol
    ?.jsType
    ?.asCompleteType()

/**
 * Get type from an explicitly typed slots property when using Options API.
 *
 * ```
 * slots: Object as SlotsType<{
 *   default: { foo: string; bar: number },
 *   item: { data: number }
 * }>
 * ```
 */
@StubSafe
fun getSlotsTypeFromTypedProperty(property: JSProperty?): JSType? =
  property
    ?.initializerOrStub?.asSafely<TypeScriptAsExpression>()
    ?.type?.jsType?.asSafely<JSGenericTypeImpl>()
    ?.takeIf { JSNamedType.isNamedTypeWithName(it.type, "SlotsType") }
    ?.arguments
    ?.firstOrNull()
