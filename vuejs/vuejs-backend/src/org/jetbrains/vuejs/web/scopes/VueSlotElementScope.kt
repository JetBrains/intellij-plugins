// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.vuejs.web.scopes

import com.intellij.lang.javascript.psi.JSExpression
import com.intellij.lang.javascript.psi.resolve.JSResolveUtil
import com.intellij.lang.javascript.psi.types.JSStringLiteralTypeImpl
import com.intellij.lang.javascript.psi.util.stubSafeAttributes
import com.intellij.model.Pointer
import com.intellij.polySymbols.PolySymbol
import com.intellij.polySymbols.PolySymbolKind
import com.intellij.polySymbols.PolySymbolOrigin
import com.intellij.polySymbols.html.HTML_ATTRIBUTES
import com.intellij.polySymbols.html.HTML_SLOTS
import com.intellij.polySymbols.html.PolySymbolHtmlAttributeValue
import com.intellij.polySymbols.html.PolySymbolHtmlAttributeValue.Kind.EXPRESSION
import com.intellij.polySymbols.html.PolySymbolHtmlAttributeValue.Type.OF_MATCH
import com.intellij.polySymbols.js.JS_PROPERTIES
import com.intellij.polySymbols.patterns.ComplexPatternOptions
import com.intellij.polySymbols.patterns.PolySymbolPattern
import com.intellij.polySymbols.patterns.PolySymbolPatternFactory.createComplexPattern
import com.intellij.polySymbols.patterns.PolySymbolPatternFactory.createSymbolReferencePlaceholder
import com.intellij.polySymbols.patterns.PolySymbolPatternReferenceResolver
import com.intellij.polySymbols.patterns.PolySymbolPatternReferenceResolver.Reference
import com.intellij.polySymbols.query.PolySymbolNameConversionRules
import com.intellij.polySymbols.query.PolySymbolWithPattern
import com.intellij.polySymbols.utils.PolySymbolScopeWithCache
import com.intellij.psi.createSmartPointer
import com.intellij.psi.util.PsiModificationTracker
import com.intellij.psi.xml.XmlTag
import com.intellij.util.asSafely
import com.intellij.xml.util.HtmlUtil.SLOT_TAG_NAME
import org.jetbrains.vuejs.codeInsight.attributes.VueAttributeNameParser
import org.jetbrains.vuejs.codeInsight.findJSExpression
import org.jetbrains.vuejs.codeInsight.fromAsset
import org.jetbrains.vuejs.codeInsight.toAsset
import org.jetbrains.vuejs.model.DEFAULT_SLOT_NAME
import org.jetbrains.vuejs.model.SLOT_NAME_ATTRIBUTE
import org.jetbrains.vuejs.model.VueModelManager
import org.jetbrains.vuejs.model.VueModelVisitor
import org.jetbrains.vuejs.web.VUE_COMPONENTS
import org.jetbrains.vuejs.web.VUE_SPECIAL_PROPERTIES
import org.jetbrains.vuejs.web.asPolySymbol
import org.jetbrains.vuejs.web.symbols.VueSymbol

private const val SLOT_LOCAL_COMPONENT = "\$local"

class VueSlotElementScope(tag: XmlTag) : PolySymbolScopeWithCache<XmlTag, Unit>(tag.project, tag, Unit) {

  override fun provides(kind: PolySymbolKind): Boolean =
    kind == HTML_ATTRIBUTES
    || kind == VUE_COMPONENTS

  override fun initialize(consumer: (PolySymbol) -> Unit, cacheDependencies: MutableSet<Any>) {
    VueModelManager.findEnclosingContainer(dataHolder)
      .asPolySymbol(SLOT_LOCAL_COMPONENT, VueModelVisitor.Proximity.LOCAL)
      ?.let(consumer)

    consumer(VueSlotPropertiesSymbol(getSlotName()))

    cacheDependencies.add(PsiModificationTracker.MODIFICATION_COUNT)
  }

  private fun getSlotName(): String? {
    for (attr in dataHolder.stubSafeAttributes) {
      val info = VueAttributeNameParser.parse(attr.name, SLOT_TAG_NAME)

      if (info.kind == VueAttributeNameParser.VueAttributeKind.SLOT_NAME) {
        return attr.value
      }
      else if ((info as? VueAttributeNameParser.VueDirectiveInfo)?.directiveKind == VueAttributeNameParser.VueDirectiveKind.BIND
               && info.arguments == SLOT_NAME_ATTRIBUTE) {
        return attr.valueElement
          ?.findJSExpression<JSExpression>()
          ?.let { JSResolveUtil.getExpressionJSType(it)?.substitute() }
          ?.asSafely<JSStringLiteralTypeImpl>()
          ?.literal
      }
    }

    return DEFAULT_SLOT_NAME
  }

  override fun createPointer(): Pointer<VueSlotElementScope> {
    val componentPointer = dataHolder.createSmartPointer()
    return Pointer {
      componentPointer.dereference()?.let { VueSlotElementScope(it) }
    }
  }

  private class VueSlotPropertiesSymbol(slotName: String?) : PolySymbolWithPattern, VueSymbol {

    override val kind: PolySymbolKind
      get() = HTML_ATTRIBUTES

    override val name: String
      get() = "Vue Slot Properties"

    override val attributeValue: PolySymbolHtmlAttributeValue =
      PolySymbolHtmlAttributeValue.create(kind = EXPRESSION, type = OF_MATCH)

    override val pattern: PolySymbolPattern =
      createComplexPattern(
        ComplexPatternOptions(symbolsResolver = PolySymbolPatternReferenceResolver(
          if (slotName != null)
            Reference(
              location = listOf(
                VUE_COMPONENTS.withName(SLOT_LOCAL_COMPONENT),
                HTML_SLOTS.withName(slotName),
              ),
              kind = JS_PROPERTIES,
              nameConversionRules = listOf(
                PolySymbolNameConversionRules.create(JS_PROPERTIES) {
                  listOf(fromAsset(it), toAsset(it))
                }
              )
            )
          else
            Reference(kind = VUE_SPECIAL_PROPERTIES)
        )), false,
        createSymbolReferencePlaceholder()
      )

    override val origin: PolySymbolOrigin = PolySymbolOrigin.empty()

    override fun createPointer(): Pointer<out PolySymbol> =
      Pointer.hardPointer(this)
  }

}