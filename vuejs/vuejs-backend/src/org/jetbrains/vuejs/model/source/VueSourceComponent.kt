// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.vuejs.model.source

import com.intellij.lang.ecmascript6.psi.ES6ExportDefaultAssignment
import com.intellij.lang.ecmascript6.psi.JSExportAssignment
import com.intellij.lang.javascript.JSElementTypes
import com.intellij.lang.javascript.psi.JSArgumentList
import com.intellij.lang.javascript.psi.JSCallExpression
import com.intellij.lang.javascript.psi.JSElement
import com.intellij.lang.javascript.psi.JSExpression
import com.intellij.lang.javascript.psi.JSLiteralExpression
import com.intellij.lang.javascript.psi.JSObjectLiteralExpression
import com.intellij.lang.javascript.psi.JSProperty
import com.intellij.lang.javascript.psi.JSReferenceExpression
import com.intellij.lang.javascript.psi.JSType
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
import com.intellij.openapi.util.io.FileUtilRt
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
import com.intellij.psi.util.contextOfType
import com.intellij.psi.xml.XmlFile
import com.intellij.psi.xml.XmlTag
import com.intellij.util.asSafely
import com.intellij.xml.util.HtmlUtil.SLOT_TAG_NAME
import org.jetbrains.vuejs.codeInsight.attributes.VueAttributeNameParser
import org.jetbrains.vuejs.codeInsight.attributes.VueAttributeNameParser.VueAttributeKind
import org.jetbrains.vuejs.codeInsight.attributes.VueAttributeNameParser.VueDirectiveInfo
import org.jetbrains.vuejs.codeInsight.attributes.VueAttributeNameParser.VueDirectiveKind
import org.jetbrains.vuejs.codeInsight.findJSExpression
import org.jetbrains.vuejs.codeInsight.getTextLiteralExpression
import org.jetbrains.vuejs.codeInsight.toAsset
import org.jetbrains.vuejs.index.VUE_COMPONENTS_INDEX_JS_KEY
import org.jetbrains.vuejs.index.findModule
import org.jetbrains.vuejs.index.findScriptVaporTag
import org.jetbrains.vuejs.index.getVueIndexData
import org.jetbrains.vuejs.lang.html.psi.impl.VueScriptSetupEmbeddedContentImpl
import org.jetbrains.vuejs.model.DEFAULT_SLOT_NAME
import org.jetbrains.vuejs.model.SLOT_NAME_ATTRIBUTE
import org.jetbrains.vuejs.model.VueComponent
import org.jetbrains.vuejs.model.VueFileComponent
import org.jetbrains.vuejs.model.VueLocallyDefinedComponent
import org.jetbrains.vuejs.model.VueMode
import org.jetbrains.vuejs.model.VueNamedComponent
import org.jetbrains.vuejs.model.VuePsiSourcedComponent
import org.jetbrains.vuejs.model.VueSlot
import org.jetbrains.vuejs.model.VueTemplate
import org.jetbrains.vuejs.model.source.VueComponents.getComponentDecorator
import org.jetbrains.vuejs.model.source.VueComponents.getDescriptorFromDecorator
import org.jetbrains.vuejs.types.VueSourceSlotScopeType
import org.jetbrains.vuejs.web.VueComponentSourceNavigationTarget
import org.jetbrains.vuejs.web.symbols.VueAnySlot

abstract class VueSourceComponent<T : PsiElement> private constructor(
  source: T,
  initializer: JSElement?, /* JSObjectLiteralExpression | JSFile */
  clazz: JSClass?,
) : VueSourceContainer<T>(source, initializer, clazz), VueComponent {

  companion object {

    fun create(decorator: ES6Decorator): VueSourceComponent<*>? =
      when (val parentContext = decorator.context?.context) {
        is JSExportAssignment -> parentContext.stubSafeElement as? JSClass
        is JSClass -> parentContext
        else -> null
      }?.let { create(it) }

    fun create(clazz: JSClass): VueSourceComponent<*>? {
      val initializer = getDescriptorFromDecorator(getComponentDecorator(clazz) ?: return null)
      val stringLiteral = getTextLiteralExpression(initializer?.findProperty(NAME_PROP)?.initializerOrStub)
      val isExportedClass = clazz.context is ES6ExportDefaultAssignment
      return when {
        stringLiteral != null -> VueStringLiteralNamedClassSourceComponent(clazz, stringLiteral, initializer)
        isExportedClass -> VueFileSourceComponent(clazz, initializer)
        clazz.name != null -> VueNamedClassSourceComponent(clazz, initializer)
        else -> VueUnnamedClassSourceComponent(clazz, initializer)
      }
    }

    fun create(initializer: JSObjectLiteralExpression): VueSourceComponent<out PsiElement> {
      val nameLiteral = getTextLiteralExpression(initializer.findProperty(NAME_PROP)?.initializerOrStub)
      return if (nameLiteral != null) {
        VueNamedSourceComponent(nameLiteral, initializer)
      }
      else {
        val result = VueUnnamedSourceComponent(initializer)
        if (result.elementToImport is PsiFile)
          VueFileSourceComponent(initializer)
        else result
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
          create(initializer)
        }
      }
    }

    fun create(element: JSImplicitElement): VueComponent? =
      when (val context = element.context) {
        is JSCallExpression -> create(context)
        is JSProperty -> {
          if (context.isShorthanded) {
            val delegate = VueComponents.getComponent(context.value.asSafely<JSReferenceExpression>())
                           ?: VueUnresolvedComponent(context.value)
            VueLocallyDefinedComponent.create(delegate, context)
          }
          else
            (context.context as? JSObjectLiteralExpression ?: context.initializerOrStub as? JSObjectLiteralExpression)
              ?.let { create(it) }
        }
        is ES6Decorator -> create(context)
        else -> throw IllegalStateException("Unexpected implicit element context: ${element.context?.javaClass?.name}")
      }
  }

  override val elementToImport: PsiElement?
    get() = when (source) {
      is JSObjectLiteralExpression ->
        when (val context = source.context) {
          is ES6ExportDefaultAssignment -> source.containingFile
          is JSArgumentList, is JSCallExpression ->
            context.contextOfType<JSCallExpression>(true)
              ?.takeIf { it.context is ES6ExportDefaultAssignment || VueComponents.isDefineOptionsCall(it) }
              ?.containingFile
          else -> null
        }
      is JSClass ->
        source.context.asSafely<ES6ExportDefaultAssignment>()?.containingFile
      else ->
        source.asSafely<PsiFile>()
    }

  override val mode: VueMode
    get() {
      val vapor = this@VueSourceComponent.source
        .asSafely<XmlFile>()
        ?.let { findScriptVaporTag(it) } != null

      return if (vapor) VueMode.VAPOR else VueMode.CLASSIC
    }

  override val slots: List<VueSlot>
    get() {
      val declaredSlots = super.slots
      if (declaredSlots.isNotEmpty()) return declaredSlots

      val template = template ?: return listOf(VueAnySlot)
      return CachedValuesManager.getCachedValue(template.source) {
        create(buildSlotsList(template), template.source)
      }
    }

  override fun toString(): String {
    return "VueSourceComponent(${(this as? VueNamedComponent)?.name})"
  }

  override fun getNavigationTargets(project: Project): Collection<NavigationTarget> =
    listOf(VueComponentSourceNavigationTarget(
      clazz ?: initializer ?: source
    ))

  abstract override fun createPointer(): Pointer<out VueSourceComponent<*>>

  override val typeParameters: List<TypeScriptTypeParameter>
    get() = (findModule(this@VueSourceComponent.source.containingFile, true) as? VueScriptSetupEmbeddedContentImpl)
              ?.typeParameters?.toList()
            ?: emptyList()

  private class VueUnnamedSourceComponent(
    override val initializer: JSObjectLiteralExpression,
  ) : VueSourceComponent<JSObjectLiteralExpression>(initializer, initializer, null) {

    override fun createPointer(): Pointer<VueUnnamedSourceComponent> {
      val sourcePtr = initializer.createSmartPointer()
      return Pointer {
        sourcePtr.dereference()?.let { create(it) } as? VueUnnamedSourceComponent
      }
    }
  }

  private class VueNamedSourceComponent(
    literal: JSLiteralExpression,
    override val initializer: JSObjectLiteralExpression,
  ) : VueSourceComponent<JSObjectLiteralExpression>(initializer, initializer, null),
      VueNamedComponent, PolySymbolDeclaredInPsi {

    override val name: @NlsSafe String = toAsset(literal.stubSafeStringValue!!, true)

    override val sourceElement: JSLiteralExpression = literal

    override val textRangeInSourceElement: TextRange
      get() = TextRange(1, sourceElement.textRange.length - 1)

    override val psiContext: PsiElement?
      get() = super<PolySymbolDeclaredInPsi>.psiContext

    override fun getNavigationTargets(project: Project): Collection<NavigationTarget> =
      super<VueSourceComponent>.getNavigationTargets(project)

    override fun createPointer(): Pointer<VueNamedSourceComponent> {
      val sourceElementPtr = sourceElement.createSmartPointer()
      val sourcePtr = initializer.createSmartPointer()
      return Pointer {
        val sourceElement = sourceElementPtr.dereference() ?: return@Pointer null
        val source = sourcePtr.dereference() ?: return@Pointer null
        VueNamedSourceComponent(sourceElement, source)
      }
    }

  }

  class VueFileSourceComponent private constructor(
    file: PsiFile,
    override val initializer: JSObjectLiteralExpression?,
    clazz: JSClass?,
  ) : VueSourceComponent<PsiFile>(
    file, initializer, clazz
  ), VueFileComponent {

    constructor(file: PsiFile)
      : this(file, null, null)

    constructor(initializer: JSObjectLiteralExpression)
      : this(initializer.containingFile, initializer, null)

    constructor(clazz: JSClass, initializer: JSObjectLiteralExpression?)
      : this(clazz.containingFile, initializer, clazz)

    override val name: @NlsSafe String =
      toAsset(FileUtilRt.getNameWithoutExtension(this@VueFileSourceComponent.source.name), true)

    override fun getNavigationTargets(project: Project): Collection<NavigationTarget> =
      super<VueSourceComponent>.getNavigationTargets(project)

    override fun createPointer(): Pointer<VueFileSourceComponent> {
      val sourcePtr = source.createSmartPointer()
      val initializerPtr = initializer?.createSmartPointer()
      val clazzPtr = clazz?.createSmartPointer()
      return Pointer {
        when {
          clazzPtr != null ->
            clazzPtr.dereference()?.let { create(it) } as? VueFileSourceComponent
          initializerPtr != null ->
            initializerPtr.dereference()?.let { create(it) } as? VueFileSourceComponent
          else ->
            sourcePtr.dereference()?.let { create(it) }
        }
      }
    }
  }

  private class VueNamedClassSourceComponent(
    clazz: JSClass,
    override val initializer: JSObjectLiteralExpression?,
  ) : VueSourceComponent<JSClass>(clazz, initializer, clazz), VuePsiSourcedComponent {

    override val name: @NlsSafe String = clazz.name!!

    override fun getNavigationTargets(project: Project): Collection<NavigationTarget> =
      super<VueSourceComponent>.getNavigationTargets(project)

    override fun createPointer(): Pointer<VueNamedClassSourceComponent> {
      val sourcePtr = source.createSmartPointer()
      return Pointer {
        sourcePtr.dereference()?.let { create(it) } as? VueNamedClassSourceComponent
      }
    }
  }

  private class VueStringLiteralNamedClassSourceComponent(
    clazz: JSClass,
    literal: JSLiteralExpression,
    override val initializer: JSObjectLiteralExpression,
  ) : VueSourceComponent<JSClass>(clazz, initializer, clazz), VueNamedComponent, PolySymbolDeclaredInPsi {

    override val name: @NlsSafe String = toAsset(literal.stubSafeStringValue!!, true)

    override val sourceElement: JSLiteralExpression = literal

    override val textRangeInSourceElement: TextRange
      get() = TextRange(1, sourceElement.textRange.length - 1)

    override val psiContext: PsiElement?
      get() = super<PolySymbolDeclaredInPsi>.psiContext

    override fun getNavigationTargets(project: Project): Collection<NavigationTarget> =
      super<VueSourceComponent>.getNavigationTargets(project)

    override fun createPointer(): Pointer<VueStringLiteralNamedClassSourceComponent> {
      val sourcePtr = source.createSmartPointer()
      return Pointer {
        sourcePtr.dereference()?.let { create(it) } as? VueStringLiteralNamedClassSourceComponent
      }
    }

  }

  private class VueUnnamedClassSourceComponent(
    clazz: JSClass,
    override val initializer: JSObjectLiteralExpression?,
  ) : VueSourceComponent<JSClass>(clazz, initializer, clazz) {

    override fun createPointer(): Pointer<VueUnnamedClassSourceComponent> {
      val sourcePtr = source.createSmartPointer()
      return Pointer {
        sourcePtr.dereference()?.let { create(it) } as? VueUnnamedClassSourceComponent
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

private data class VueSourceRegexSlot(
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
}

private data class VueSourceSlot(
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
}
