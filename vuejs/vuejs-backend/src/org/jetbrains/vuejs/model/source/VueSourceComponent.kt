// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.vuejs.model.source

import com.intellij.lang.ecmascript6.psi.JSExportAssignment
import com.intellij.lang.javascript.JSElementTypes
import com.intellij.lang.javascript.psi.*
import com.intellij.lang.javascript.psi.ecma6.ES6Decorator
import com.intellij.lang.javascript.psi.ecma6.JSStringTemplateExpression
import com.intellij.lang.javascript.psi.ecma6.TypeScriptTypeParameter
import com.intellij.lang.javascript.psi.ecmal4.JSClass
import com.intellij.lang.javascript.psi.stubs.JSImplicitElement
import com.intellij.lang.javascript.psi.util.JSStubBasedPsiTreeUtil
import com.intellij.lang.javascript.psi.util.stubSafeAttributes
import com.intellij.lang.javascript.psi.util.stubSafeStringValue
import com.intellij.model.Pointer
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.NlsSafe
import com.intellij.openapi.util.TextRange
import com.intellij.platform.backend.navigation.NavigationTarget
import com.intellij.polySymbols.patterns.PolySymbolPattern
import com.intellij.polySymbols.patterns.PolySymbolPatternFactory
import com.intellij.polySymbols.query.PolySymbolWithPattern
import com.intellij.polySymbols.search.PsiSourcedPolySymbol
import com.intellij.polySymbols.utils.PolySymbolDeclaredInPsi
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.StubBasedPsiElement
import com.intellij.psi.createSmartPointer
import com.intellij.psi.util.CachedValueProvider.Result.create
import com.intellij.psi.util.CachedValuesManager
import com.intellij.psi.xml.XmlFile
import com.intellij.psi.xml.XmlTag
import com.intellij.util.asSafely
import com.intellij.xml.util.HtmlUtil.SLOT_TAG_NAME
import org.jetbrains.vuejs.codeInsight.attributes.VueAttributeNameParser
import org.jetbrains.vuejs.codeInsight.attributes.VueAttributeNameParser.*
import org.jetbrains.vuejs.codeInsight.findJSExpression
import org.jetbrains.vuejs.codeInsight.fromAsset
import org.jetbrains.vuejs.codeInsight.getTextIfLiteral
import org.jetbrains.vuejs.codeInsight.getTextLiteralExpression
import org.jetbrains.vuejs.index.VUE_COMPONENTS_INDEX_JS_KEY
import org.jetbrains.vuejs.index.findModule
import org.jetbrains.vuejs.index.findScriptVaporTag
import org.jetbrains.vuejs.index.getVueIndexData
import org.jetbrains.vuejs.lang.html.psi.impl.VueScriptSetupEmbeddedContentImpl
import org.jetbrains.vuejs.model.*
import org.jetbrains.vuejs.model.source.VueComponents.getComponentDecorator
import org.jetbrains.vuejs.model.source.VueComponents.getDescriptorFromDecorator
import org.jetbrains.vuejs.types.VueSourceSlotScopeType
import org.jetbrains.vuejs.web.VueComponentSourceNavigationTarget

abstract class VueSourceComponent<T : PsiElement> private constructor(
  source: T,
  initializer: JSElement?, /* JSObjectLiteralExpression | JSFile */
  clazz: JSClass?,
) : VueSourceContainer<T>(source, initializer, clazz), VueComponent {

  companion object {

    fun create(decorator: ES6Decorator): VueSourceComponent<JSClass>? =
      when (val parentContext = decorator.context?.context) {
        is JSExportAssignment -> parentContext.stubSafeElement as? JSClass
        is JSClass -> parentContext
        else -> null
      }?.let { create(it) }

    fun create(clazz: JSClass): VueSourceComponent<JSClass>? {
      val initializer = getComponentDecorator(clazz)?.let { getDescriptorFromDecorator(it) }
      return VueClassSourceComponent(clazz.takeIf { it.name != null } ?: return null, initializer)
    }

    fun create(initializer: JSObjectLiteralExpression): VueSourceComponent<out PsiElement> {
      val nameLiteral = getTextLiteralExpression(initializer.findProperty(NAME_PROP)?.initializerOrStub)
      return if (nameLiteral != null) {
        VueNamedSourceComponent(nameLiteral, initializer)
      }
      else {
        VueUnnamedSourceComponent(initializer)
      }
    }

    fun create(file: PsiFile): VueFileSourceComponent =
      VueFileSourceComponent(file)

    fun create(call: JSCallExpression): VueComponent? {
      val stub = (call as? StubBasedPsiElement<*>)?.stub
      val initializer: JSObjectLiteralExpression?
      var nameLiteral: JSLiteralExpression?
      if (stub != null) {
        val stubs = stub.childrenStubs
        initializer = stubs.firstOrNull { it.elementType === JSElementTypes.OBJECT_LITERAL_EXPRESSION }?.psi as? JSObjectLiteralExpression
        nameLiteral = stubs.getOrNull(0)?.psi as? JSLiteralExpression
      }
      else {
        val arguments = call.argumentList?.arguments
        initializer = arguments
          ?.firstNotNullOfOrNull { it as? JSObjectLiteralExpression }
        nameLiteral = arguments?.getOrNull(0) as? JSLiteralExpression
      }
      val indexingData = call.indexingData
        ?.implicitElements
        ?.find { it.userString == VUE_COMPONENTS_INDEX_JS_KEY }
        ?.let { getVueIndexData(it) }
      if (nameLiteral == null) {
        nameLiteral = indexingData
          ?.nameQualifiedReference
          ?.let { JSStubBasedPsiTreeUtil.resolveLocallyWithMergedResults(it, call) }
          ?.firstNotNullOfOrNull { getTextLiteralExpression(it) }
      }
      return when {
          initializer == null -> {
              indexingData
                  ?.descriptorQualifiedReference
                  ?.let { JSStubBasedPsiTreeUtil.resolveLocally(it, call) }
                  ?.let { VueComponents.getComponent(it) }
                  ?.let { delegate ->
                      if (nameLiteral != null)
                          VueLocallyDefinedComponent.create(delegate, nameLiteral)
                      else
                          delegate
                  }
          }
          getTextLiteralExpression(nameLiteral) != null -> {
              VueNamedSourceComponent(nameLiteral, initializer)
          }
          else -> {
              VueUnnamedSourceComponent(initializer)
          }
      }
    }

    fun create(element: JSImplicitElement): VueComponent? =
      when (val context = element.context) {
        is JSCallExpression -> create(context)
        is JSProperty -> {
          (context.value as? JSObjectLiteralExpression)
            ?.let { create(it) }
        }
        is ES6Decorator -> create(context)
        else -> throw IllegalStateException("Unexpected implicit element context: ${element.context?.javaClass?.name}")
      }
  }

  override val source: T
    get() = super<VueSourceContainer>.source

  override fun getNavigationTargets(project: Project): Collection<NavigationTarget> =
    listOf(VueComponentSourceNavigationTarget(super<VueSourceContainer>.source))

  override val componentSource: PsiElement
    get() = super<VueSourceContainer>.source

  override val mode: VueMode
    get() {
      val vapor = this@VueSourceComponent.source
        .asSafely<XmlFile>()
        ?.let { findScriptVaporTag(it) } != null

      return if (vapor) VueMode.VAPOR else VueMode.CLASSIC
    }

  override val nameElement: PsiElement?
    get() = initializer
      ?.asSafely<JSObjectLiteralExpression>()
      ?.findProperty(NAME_PROP)
      ?.let { it.literalExpressionInitializer ?: it.value }

  override val slots: List<VueSlot>
    get() {
      val declaredSlots = super.slots
      if (declaredSlots.isNotEmpty()) return declaredSlots

      val template = template ?: return emptyList()
      return CachedValuesManager.getCachedValue(template.source) {
        create(buildSlotsList(template), template.source)
      }
    }

  override fun toString(): String {
    return "VueSourceComponent(${(this as? VueNamedComponent)?.name ?: getTextIfLiteral(nameElement)})"
  }

  abstract override fun createPointer(): Pointer<out VueSourceComponent<*>>

  override val typeParameters: List<TypeScriptTypeParameter>
    get() = (findModule(this@VueSourceComponent.source.containingFile, true) as? VueScriptSetupEmbeddedContentImpl)
              ?.typeParameters?.toList()
            ?: emptyList()

  val delegate: VueComponent?
    get() = null

  private class VueUnnamedSourceComponent(
    override val initializer: JSObjectLiteralExpression,
  ) : VueSourceComponent<JSObjectLiteralExpression>(initializer, initializer, null) {

    override fun createPointer(): Pointer<VueNamedSourceComponent> {
      val sourcePtr = initializer.createSmartPointer()
      return Pointer {
        sourcePtr.dereference()?.let { create(it) } as? VueNamedSourceComponent
      }
    }
  }

  private class VueNamedSourceComponent(
    literal: JSLiteralExpression,
    override val initializer: JSObjectLiteralExpression,
    override val vueProximity: VueModelVisitor.Proximity? = null,
  ) : VueSourceComponent<JSLiteralExpression>(literal, initializer, null), VueNamedComponent, PolySymbolDeclaredInPsi {

    override val name: @NlsSafe String = source.stubSafeStringValue!!

    override val sourceElement: PsiElement
      get() = source

    override val textRangeInSourceElement: TextRange
      get() = TextRange(1, sourceElement.textRange.length - 1)

    override val psiContext: PsiElement?
      get() = super<PolySymbolDeclaredInPsi>.psiContext

    override fun withVueProximity(proximity: VueModelVisitor.Proximity): VueNamedComponent =
      VueNamedSourceComponent(source, initializer, proximity)

    override fun getNavigationTargets(project: Project): Collection<NavigationTarget> =
      super<PolySymbolDeclaredInPsi>.getNavigationTargets(project)

    override fun createPointer(): Pointer<VueNamedSourceComponent> {
      val sourcePtr = initializer.createSmartPointer()
      return Pointer {
        sourcePtr.dereference()?.let { create(it) } as? VueNamedSourceComponent
      }
    }

  }

  class VueFileSourceComponent(
    file: PsiFile,
    override val vueProximity: VueModelVisitor.Proximity? = null,
  ) : VueSourceComponent<PsiFile>(
    file, null, null
  ), VueFileComponent {

    override val name: @NlsSafe String = fromAsset(this@VueFileSourceComponent.source.virtualFile.nameWithoutExtension)

    override fun withVueProximity(proximity: VueModelVisitor.Proximity): VueNamedComponent =
      VueFileSourceComponent(source, proximity)

    override fun getNavigationTargets(project: Project): Collection<NavigationTarget> =
      super<VueSourceComponent>.getNavigationTargets(project)

    override fun createPointer(): Pointer<VueFileSourceComponent> {
      val sourcePtr = source.createSmartPointer()
      return Pointer {
        sourcePtr.dereference()?.let { create(it) }
      }
    }
  }

  private class VueClassSourceComponent(
    clazz: JSClass,
    override val initializer: JSObjectLiteralExpression?,
    override val vueProximity: VueModelVisitor.Proximity? = null,
  ) : VueSourceComponent<JSClass>(clazz, initializer, clazz), VueNamedComponent, PsiSourcedPolySymbol {

    override val name: @NlsSafe String = clazz.name!!

    override fun withVueProximity(proximity: VueModelVisitor.Proximity): VueNamedComponent =
      VueClassSourceComponent(source, initializer, proximity)

    override fun getNavigationTargets(project: Project): Collection<NavigationTarget> =
      super<VueSourceComponent>.getNavigationTargets(project)

    override fun createPointer(): Pointer<VueClassSourceComponent> {
      val sourcePtr = source.createSmartPointer()
      return Pointer {
        sourcePtr.dereference()?.let { create(it) } as? VueClassSourceComponent
      }
    }
  }

}

private fun buildSlotsList(template: VueTemplate<*>): List<VueSlot> {
  val result = mutableListOf<VueSlot>()
  template.safeVisitTags { tag ->
    if (tag.name == SLOT_TAG_NAME) {
      tag.stubSafeAttributes
        .asSequence()
        .filter { !it.value.isNullOrBlank() }
        .firstNotNullOfOrNull { attr ->
          VueAttributeNameParser.parse(attr.name, SLOT_TAG_NAME).let { info ->
            if (info.kind == VueAttributeKind.SLOT_NAME) {
              attr.value?.let { name -> VueSourceSlot(name, tag) }
            }
            else if ((info as? VueDirectiveInfo)?.directiveKind == VueDirectiveKind.BIND
                     && info.arguments == SLOT_NAME_ATTRIBUTE) {
              attr.valueElement
                ?.findJSExpression<JSExpression>()
                ?.let { getSlotNameRegex(it) }
                ?.let { VueSourceRegexSlot(it, tag) }
            }
            else null
          }
        }
        .let {
          result.add(it ?: VueSourceSlot(DEFAULT_SLOT_NAME, tag))
        }
    }
  }
  return result
}

private fun getSlotNameRegex(expr: JSExpression): String {
  if (expr is JSStringTemplateExpression) {
    var lastIndex = 1
    val exprText = expr.text
    val result = StringBuilder()
    for (range in expr.stringRanges) {
      if (lastIndex < range.startOffset) {
        result.append(".*")
      }
      result.append(Regex.escape(range.substring(exprText)))
      lastIndex = range.endOffset
    }
    if (lastIndex < exprText.length - 1) {
      result.append(".*")
    }
    return result.toString()
  }
  return ".*"
}

private class VueSourceRegexSlot(
  private val regex: String,
  override val source: XmlTag,
) : VueSlot, PolySymbolWithPattern, PsiSourcedPolySymbol {

  override val name: String
    get() = "Dynamic slot"

  override val pattern: PolySymbolPattern =
    PolySymbolPatternFactory.createRegExMatch(regex, true)

  override val type: JSType
    get() = VueSourceSlotScopeType(source, "$name /$pattern/")

  override fun createPointer(): Pointer<VueSourceRegexSlot> {
    val regex = regex
    val sourcePtr = source.createSmartPointer()
    return Pointer {
      val source = sourcePtr.dereference() ?: return@Pointer null
      VueSourceRegexSlot(regex, source)
    }
  }

  override fun equals(other: Any?): Boolean =
    other === this ||
    other is VueSourceRegexSlot
    && other.regex == regex
    && other.source == source

  override fun hashCode(): Int {
    var result = regex.hashCode()
    result = 31 * result + source.hashCode()
    return result
  }

}

private class VueSourceSlot(
  override val name: String,
  override val source: XmlTag,
) : VueSlot, PsiSourcedPolySymbol {
  override val type: JSType
    get() = VueSourceSlotScopeType(source, name)

  override fun createPointer(): Pointer<VueSourceSlot> {
    val name = name
    val sourcePtr = source.createSmartPointer()
    return Pointer {
      val source = sourcePtr.dereference() ?: return@Pointer null
      VueSourceSlot(name, source)
    }
  }

  override fun equals(other: Any?): Boolean =
    other === this ||
    other is VueSourceSlot
    && other.name == name
    && other.source == source

  override fun hashCode(): Int {
    var result = name.hashCode()
    result = 31 * result + source.hashCode()
    return result
  }

}
