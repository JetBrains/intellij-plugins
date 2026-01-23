// Copyright 2000-2021 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.vuejs.model.source

import com.intellij.lang.ecmascript6.psi.ES6ImportSpecifier
import com.intellij.lang.javascript.JSElementTypes
import com.intellij.lang.javascript.JSStringUtil.unquoteWithoutUnescapingStringLiteralValue
import com.intellij.lang.javascript.evaluation.JSCodeBasedTypeFactory
import com.intellij.lang.javascript.psi.*
import com.intellij.lang.javascript.psi.ecma6.JSTypeDeclaration
import com.intellij.lang.javascript.psi.ecmal4.JSClass
import com.intellij.lang.javascript.psi.impl.JSStubElementImpl
import com.intellij.lang.javascript.psi.resolve.JSEvaluateContext
import com.intellij.lang.javascript.psi.resolve.JSResolveUtil
import com.intellij.lang.javascript.psi.stubs.JSImplicitElement
import com.intellij.lang.javascript.psi.types.JSAnyType
import com.intellij.lang.javascript.psi.types.JSParameterTypeDecoratorImpl
import com.intellij.lang.javascript.psi.types.JSStringLiteralTypeImpl
import com.intellij.lang.javascript.psi.types.JSTupleType
import com.intellij.lang.javascript.psi.util.JSStubBasedPsiTreeUtil
import com.intellij.lang.javascript.psi.util.stubSafeCallArguments
import com.intellij.lang.javascript.psi.util.stubSafeChildren
import com.intellij.lang.typescript.TypeScriptElementTypes
import com.intellij.model.Pointer
import com.intellij.model.Symbol
import com.intellij.openapi.util.NlsSafe
import com.intellij.polySymbols.search.PolySymbolSearchTarget
import com.intellij.polySymbols.search.PsiSourcedPolySymbol
import com.intellij.psi.PsiElement
import com.intellij.psi.createSmartPointer
import com.intellij.psi.util.CachedValueProvider
import com.intellij.psi.util.CachedValuesManager
import com.intellij.psi.util.PsiModificationTracker
import com.intellij.psi.xml.XmlFile
import com.intellij.psi.xml.XmlTag
import com.intellij.util.asSafely
import org.jetbrains.vuejs.codeInsight.*
import org.jetbrains.vuejs.index.findModule
import org.jetbrains.vuejs.index.getFunctionNameFromVueIndex
import org.jetbrains.vuejs.index.isScriptVaporTag
import org.jetbrains.vuejs.model.*
import org.jetbrains.vuejs.types.VueSourceModelPropType
import org.jetbrains.vuejs.types.optionalIf

class VueScriptSetupInfoProvider : VueContainerInfoProvider {

  override fun getInfo(descriptor: VueSourceEntityDescriptor): VueContainerInfoProvider.VueContainerInfo? {
    return descriptor.source
      .takeIf { it is JSObjectLiteralExpression || it is XmlFile }
      ?.let { findModule(it, true) }
      ?.let { module ->
        CachedValuesManager.getCachedValue(module) {
          CachedValueProvider.Result.create(VueScriptSetupInfo(module), PsiModificationTracker.MODIFICATION_COUNT)
        }
      }
  }

  class VueScriptSetupInfo(val module: JSExecutionScope) : VueContainerInfoProvider.VueContainerInfo {

    override val components: Map<String, VueNamedComponent>
      get() = structure.components

    override val directives: Map<String, VueDirective>
      get() = structure.directives

    override val props: List<VueInputProperty>
      get() = structure.props

    override val emits: List<VueEmitCall>
      get() = structure.emits

    override val slots: List<VueSlot>
      get() = structure.slots

    override val computed: List<VueComputedProperty>
      get() = structure.rawBindings.filterIsInstance<VueComputedProperty>()

    override val data: List<VueDataProperty>
      get() = structure.rawBindings.filterIsInstance<VueDataProperty>()

    override val methods: List<VueMethod>
      get() = structure.rawBindings.filterIsInstance<VueMethod>()

    override val provides: List<VueProvide>
      get() = injectionCalls.filterIsInstance<VueProvide>()

    override val injects: List<VueInject>
      get() = injectionCalls.filterIsInstance<VueInject>()

    private val structure: VueScriptSetupStructure
      get() = CachedValuesManager.getCachedValue(module) {
        CachedValueProvider.Result.create(analyzeModule(module), PsiModificationTracker.MODIFICATION_COUNT)
      }

    private val injectionCalls: List<VueSymbol>
      get() = CachedValuesManager.getCachedValue(module) {
        CachedValueProvider.Result.create(getInjectionCalls(module), PsiModificationTracker.MODIFICATION_COUNT)
      }

    private fun getVueMode(module: JSExecutionScope): VueMode {
      val isVapor = module.context
        .asSafely<XmlTag>()
        .isScriptVaporTag()

      return if (isVapor) VueMode.VAPOR else VueMode.CLASSIC
    }

    private fun analyzeModule(module: JSExecutionScope): VueScriptSetupStructure {
      val mode = getVueMode(module)
      val components = mutableMapOf<String, VueNamedComponent>()
      val directives = mutableMapOf<String, VueDirective>()

      JSStubBasedPsiTreeUtil.processDeclarationsInScope(
        module,
        { element, _ ->
          val name = (element as? JSPsiNamedElementBase)?.let { if (it is ES6ImportSpecifier) it.declaredName else it.name }
          if (name != null && isScriptSetupLocalDirectiveName(name)) {
            directives[name.substring(1)] = VueScriptSetupLocalDirective(name, element, mode)
          }
          else if (name != null && element !is JSClass) {
            (VueComponents.getComponent(element) ?: VueUnresolvedComponent(element))
              .let { VueLocallyDefinedComponent.create(it, element) }
              ?.let { components[name] = it }
          }
          true
        },
        false
      )

      VueModelManager.findEnclosingComponent(module)
        ?.let { VueLocallyDefinedComponent.create(it, module.containingFile) }
        ?.let { component -> components.putIfAbsent(component.name, component) }

      var props: List<VueInputProperty> = emptyList()
      var emits: List<VueEmitCall> = emptyList()
      var slots: List<VueSlot> = emptyList()
      var rawBindings: List<VueSymbol> = emptyList()
      val modelDecls: MutableMap<String, VueScriptSetupModelDecl> = mutableMapOf()

      module.getStubSafeDefineCalls().forEach { call ->
        when (getFunctionNameFromVueIndex(call)) {
          DEFINE_PROPS_FUN -> {
            val defaults = when (val parent = call.context) {
              is JSDestructuringElement -> {
                // parent.target could be an JSDestructuringArray, but Vue does not support it
                val objectDestructure = parent.target as? JSDestructuringObject
                objectDestructure?.properties?.mapNotNull { destructuringProperty ->
                  val element = destructuringProperty.destructuringElement
                  if (!destructuringProperty.isRest && element is JSVariable && element.hasOwnInitializer()) {
                    element.name
                  }
                  else {
                    null // e.g. aliased prop without default value
                  }
                } ?: listOf()
              }
              else -> listOf()
            }
            props = analyzeDefineProps(call, defaults)
          }
          WITH_DEFAULTS_FUN -> {
            val definePropsCall = call.getInnerDefineProps()
            if (definePropsCall != null) {
              // Vue can throw Error: [@vue/compiler-sfc] The 2nd argument of withDefaults must be an object literal.
              // Let's be more forgiving here and try to interpret props without defaults
              val withDefaultsObjectLiteral = call.stubSafeCallArguments.getOrNull(1) as? JSObjectLiteralExpression
              val defaults = withDefaultsObjectLiteral?.properties?.mapNotNull { it.name } ?: listOf()

              props = analyzeDefineProps(definePropsCall, defaults)
            }
          }
          DEFINE_EMITS_FUN -> {
            emits = analyzeDefineEmits(call)
          }
          DEFINE_EXPOSE_FUN -> {
            rawBindings =
              call.stubSafeCallArguments
                .getOrNull(0)
                ?.let { argument ->
                  VueCompositionInfoHelper.createRawBindings(call, argument) {
                    JSCodeBasedTypeFactory.getPsiBasedType(it, JSEvaluateContext(it.containingFile))
                  }
                }
              ?: emptyList()
          }
          DEFINE_MODEL_FUN -> VueScriptSetupModelDecl.create(call)?.let { modelDecls[it.name] = it }
          DEFINE_SLOTS_FUN -> {
            slots = analyzeDefineSlots(call)
          }
        }
      }

      if (modelDecls.isNotEmpty()) {
        val modelProps: MutableList<VueInputProperty> = mutableListOf()
        val modelEmits: MutableList<VueEmitCall> = mutableListOf()

        modelDecls.values.forEach {
          modelProps.add(VueScriptSetupModelInputProperty(it))
          modelEmits.add(VueScriptSetupModelEvent(it))
        }

        props = props + modelProps
        emits = emits + modelEmits
      }

      return VueScriptSetupStructure(components, directives, props, emits, slots, rawBindings)
    }

    override fun equals(other: Any?): Boolean =
      (other as? VueScriptSetupInfo)?.module == module

    override fun hashCode(): Int = module.hashCode()
  }

  private data class VueScriptSetupStructure(
    val components: Map<String, VueNamedComponent>,
    val directives: Map<String, VueDirective>,
    val props: List<VueInputProperty>,
    val emits: List<VueEmitCall>,
    val slots: List<VueSlot>,
    val rawBindings: List<VueSymbol>,
  )

}

private val STUB_SAFE_DEFINE_METHOD_NAMES: Set<String> = setOf(
  DEFINE_PROPS_FUN,
  DEFINE_EMITS_FUN,
  DEFINE_SLOTS_FUN,
  DEFINE_EXPOSE_FUN,
  WITH_DEFAULTS_FUN,
  DEFINE_MODEL_FUN,
  INJECT_FUN,
  PROVIDE_FUN,
  DEFINE_OPTIONS_FUN,
)

fun JSExecutionScope.getStubSafeDefineCalls(): Sequence<JSCallExpression> {
  (this as? JSStubElementImpl<*>)?.stub?.let { moduleStub ->
    return moduleStub.childrenStubs.asSequence().flatMap { stub ->
      when (val psi = stub.psi) {
        is JSCallExpression -> sequenceOf(psi)
        is JSStatement -> {
          stub.childrenStubs.asSequence()
            .filter {
              it.elementType == JSElementTypes.VARIABLE ||
              it.elementType == TypeScriptElementTypes.TYPESCRIPT_VARIABLE ||
              it.elementType == JSElementTypes.DESTRUCTURING_ELEMENT
            }
            .flatMap { it.childrenStubs.asSequence() }
            .filter { it.elementType == JSElementTypes.CALL_EXPRESSION }
            .mapNotNull { it.psi as? JSCallExpression }
        }
        else -> emptySequence()
      }
    }
  }

  return children.asSequence()
    .filterIsInstance<JSStatement>()
    .flatMap { it.children.asSequence() }
    .map {
      when (it) {
        is JSVariable -> it.initializer
        is JSDestructuringElement -> it.initializer
        else -> it
      }
    }
    .filterIsInstance<JSCallExpression>()
    .filter { call -> (call.methodExpression as? JSReferenceExpression)?.referenceName in STUB_SAFE_DEFINE_METHOD_NAMES }
}

private fun getLiteralValue(literal: JSLiteralExpression): String? =
  literal.significantValue
    ?.let { unquoteWithoutUnescapingStringLiteralValue(it) }
    ?.takeIf { it.isNotBlank() }

private fun getInjectionCalls(module: JSExecutionScope): List<VueSymbol> {
  val symbols: MutableList<VueSymbol> = mutableListOf()

  module.getStubSafeDefineCalls().forEach { call ->
    when (getFunctionNameFromVueIndex(call)) {
      PROVIDE_FUN -> analyzeProvide(call)?.let { symbols.add(it) }
      INJECT_FUN -> analyzeInject(call)?.let { symbols.add(it) }
    }
  }

  return symbols
}

private fun analyzeDefineProps(call: JSCallExpression, defaults: List<@NlsSafe String>): List<VueInputProperty> {
  val typeArgs = call.typeArguments
  val arguments = call.stubSafeCallArguments
  val props: List<VueInputProperty>

  // scriptSetup type declaration
  if (typeArgs.size == 1) {
    val typeArg = typeArgs[0]
    val recordType = typeArg.jsType.asRecordType()

    props = recordType.properties
      .map { VueScriptSetupInputProperty(typeArg, it, defaults.contains(it.memberName)) }
      .toList()
  }
  // scriptSetup runtime declaration
  else if (arguments.size == 1) {
    when (val arg = arguments[0]) {
      is JSObjectLiteralExpression -> {
        props = collectMembers(arg)
          .mapNotNull { (name, property) ->
            VueDefaultContainerInfoProvider.VueSourceInputProperty.create(name, property, defaults.contains(name))
          }
      }
      is JSArrayLiteralExpression -> {
        props = getStringLiteralsFromInitializerArray(arg)
          .mapNotNull { literal ->
            val name = getTextIfLiteral(literal) ?: ""
            VueDefaultContainerInfoProvider.VueSourceInputProperty.create(name, literal, defaults.contains(name))
          }
      }
      else -> {
        props = emptyList()
      }
    }
  }
  else {
    props = emptyList()
  }

  return props
}

private fun analyzeDefineEmits(call: JSCallExpression): List<VueEmitCall> {
  val options = call.stubSafeCallArguments.getOrNull(0)

  if (options is JSArrayLiteralExpression) {
    return options.stubSafeChildren.mapNotNull { literal ->
      VueScriptSetupLiteralBasedEvent.create(literal)
    }
  }

  val typeArgument = call.typeArguments
    .firstOrNull()
  val typeArgumentType = typeArgument
    ?.jsType
    ?.asRecordType()

  if (typeArgumentType != null && typeArgumentType.hasProperties()) {
    return typeArgumentType.properties.map {
      VueScriptSetupPropertyContractEvent(typeArgument, it)
    }
  }

  val emitType = JSResolveUtil.getElementJSType(call)?.asRecordType()
                 ?: return emptyList()

  val eventSources =
    options.asSafely<JSObjectLiteralExpression>()
      ?.let { JSResolveUtil.getElementJSType(it) }
      ?.asRecordType()
      ?.properties
      ?.associate { it.memberName to it.memberSource.singleElement }
    ?: emptyMap()

  return emitType.callSignatures
    .mapNotNull { callSignature ->
      callSignature
        .functionType
        .parameters.getOrNull(0)
        ?.inferredType
        ?.asSafely<JSStringLiteralTypeImpl>()
        ?.let {
          val name = unquoteWithoutUnescapingStringLiteralValue(it.valueAsString)
          val source = eventSources[name] ?: it.sourceElement
          VueScriptSetupCallSignatureEvent(call, name, source, callSignature.functionType)
        }
    }
}

private fun analyzeDefineSlots(call: JSCallExpression): List<VueSlot> =
  call.takeIf { JSStubBasedPsiTreeUtil.isStubBased(it) }
    ?.typeArguments
    ?.singleOrNull()?.let { typeDeclaration ->
      typeDeclaration.jsType.asRecordType().typeMembers
        .asSequence()
        .filterIsInstance<JSRecordType.PropertySignature>()
        .map {
          VueScriptSetupSlot(typeDeclaration, it)
        }
    }
    ?.toList()
  ?: emptyList()

private fun JSCallExpression.getInnerDefineProps(): JSCallExpression? =
  stubSafeCallArguments
    .getOrNull(0)
    .asSafely<JSCallExpression>()
    ?.takeIf { getFunctionNameFromVueIndex(it) == DEFINE_PROPS_FUN }

private class VueScriptSetupInputProperty(
  private val sourceType: JSTypeDeclaration,
  private val propertySignature: JSRecordType.PropertySignature,
  private val hasOuterDefault: Boolean,
) : VueInputProperty, PsiSourcedPolySymbol {

  override val name: String
    get() = propertySignature.memberName

  override val source: PsiElement? = propertySignature.memberSource.singleElement

  override val required: Boolean
    get() {
      // modifying required by hasOuterDefault is controversial, Vue compiler will still raise a warning in the dev mode,
      // but for editors, it makes more sense to treat such prop as optional.
      if (hasOuterDefault) return false
      return !propertySignature.isOptional
    }

  override val type: JSType?
    get() = propertySignature.jsType?.optionalIf(propertySignature.isOptional)

  override fun equals(other: Any?): Boolean =
    other === this
    || other is VueScriptSetupInputProperty
    && other.sourceType == sourceType
    && other.propertySignature.memberName == propertySignature.memberName

  override fun hashCode(): Int {
    var result = sourceType.hashCode()
    result = 31 * result + propertySignature.memberName.hashCode()
    return result
  }

  override fun createPointer(): Pointer<out VueScriptSetupInputProperty> {
    val sourcePtr = sourceType.createSmartPointer()
    val name = propertySignature.memberName
    val hasOuterDefault = hasOuterDefault
    return Pointer {
      val source = sourcePtr.element ?: return@Pointer null
      source.jsType.asRecordType().findPropertySignature(name)
        ?.let { VueScriptSetupInputProperty(sourceType, it, hasOuterDefault) }
    }
  }

  override fun toString(): String {
    return "VueScriptSetupInputProperty(name='$name', required=$required, jsType=$type)"
  }

}

private class VueScriptSetupLiteralBasedEvent(
  override val name: String,
  override val source: PsiElement,
) : VueEmitCall, PsiSourcedPolySymbol {
  companion object {
    fun create(literal: PsiElement): VueScriptSetupLiteralBasedEvent? =
      (literal as? JSLiteralExpression)
        ?.significantValue
        ?.let { VueScriptSetupLiteralBasedEvent(unquoteWithoutUnescapingStringLiteralValue(it), literal) }
  }

  override val searchTarget: PolySymbolSearchTarget
    get() = PolySymbolSearchTarget.create(this)

  override fun createPointer(): Pointer<VueScriptSetupLiteralBasedEvent> {
    val sourcePtr = source.createSmartPointer()
    return Pointer { sourcePtr.element?.let { create(it) } }
  }

  override fun equals(other: Any?): Boolean =
    other === this
    || other is VueScriptSetupLiteralBasedEvent
    && other.source == source

  override fun hashCode(): Int =
    source.hashCode()
}

private class VueScriptSetupCallSignatureEvent(
  private val call: JSCallExpression,
  override val name: String,
  override val source: PsiElement?,
  private val eventSignature: JSFunctionType,
) : VueEmitCall, PsiSourcedPolySymbol {
  override val params: List<JSParameterTypeDecorator>
    get() = eventSignature.parameters.drop(1)

  override val hasStrictSignature: Boolean
    get() = true

  override fun equals(other: Any?): Boolean =
    other === this
    || other is VueScriptSetupCallSignatureEvent
    && other.call == call
    && other.name == name

  override fun hashCode(): Int {
    var result = call.hashCode()
    result = 31 * result + name.hashCode()
    return result
  }

  override fun createPointer(): Pointer<VueScriptSetupCallSignatureEvent> {
    val callPtr = call.createSmartPointer()
    val name = name
    return Pointer {
      callPtr.dereference()
        ?.let { analyzeDefineEmits(it) }
        ?.firstNotNullOfOrNull { emit ->
          (emit as? VueScriptSetupCallSignatureEvent)?.takeIf { it.name == name }
        }
    }
  }
}

private class VueScriptSetupPropertyContractEvent(
  private val typeArgument: JSTypeDeclaration,
  private val signature: JSRecordType.PropertySignature,
) : VueEmitCall, PsiSourcedPolySymbol {
  override val name: String
    get() = signature.memberName

  override val source: PsiElement?
    get() = signature.memberSource.singleElement

  private val parametersType: JSType?
    get() = signature.jsType

  override val params: List<JSParameterTypeDecorator> by lazy {
    val parametersType = parametersType
    if (parametersType !is JSTupleType)
      return@lazy emptyList()

    val firstOptional = parametersType.firstOptional
    fun isOptional(index: Int): Boolean =
      firstOptional != -1 && index >= firstOptional

    parametersType.types.mapIndexed { index, type ->
      JSParameterTypeDecoratorImpl(
        parametersType.getNameByIndex(index),
        type,
        isOptional(index),
        false,
        type != null,
      )
    }
  }

  override val hasStrictSignature: Boolean
    get() = true

  override fun equals(other: Any?): Boolean =
    other === this
    || other is VueScriptSetupPropertyContractEvent
    && other.typeArgument == typeArgument
    && other.signature.memberName == signature.memberName

  override fun hashCode(): Int {
    var result = typeArgument.hashCode()
    result = 31 * result + signature.memberName.hashCode()
    return result
  }

  override fun createPointer(): Pointer<VueScriptSetupPropertyContractEvent> {
    val typeArgumentPtr = typeArgument.createSmartPointer()
    val name = signature.memberName
    return Pointer {
      typeArgumentPtr
        .dereference()
        ?.jsType
        ?.asRecordType()
        ?.findPropertySignature(name)
        ?.let { VueScriptSetupPropertyContractEvent(typeArgument, it) }
    }
  }
}

private class VueScriptSetupModelDecl(
  private val call: JSCallExpression,
  override val name: String,
  modelType: JSType,
  options: JSObjectLiteralExpression?,
  sourceElement: PsiElement,
) : VueModelDecl, Symbol {

  override val required: Boolean = getRequiredFromPropOptions(options)

  override val local: Boolean = getLocalFromPropOptions(options)

  override val type: JSType = modelType.optionalIf(!required)

  override val referenceType: JSType = modelType.optionalIf(getPropOptionality(options, required))

  override val source: PsiElement = VueImplicitElement(
    name, referenceType, sourceElement, JSImplicitElement.Type.Property, true)

  override fun toString(): String {
    return "VueScriptSetupModelDecl(name='$name', required=$required, jsType=$type, local=$local)"
  }

  override fun equals(other: Any?): Boolean =
    other is VueScriptSetupModelDecl
    && other.call == call
    && other.name == name

  override fun hashCode(): Int {
    var result = call.hashCode()
    result = 31 * result + name.hashCode()
    return result
  }

  override fun createPointer(): Pointer<out VueScriptSetupModelDecl> {
    val call = call.createSmartPointer()
    return Pointer {
      call.dereference()?.let { create(it) }
    }
  }

  companion object {
    fun create(call: JSCallExpression): VueScriptSetupModelDecl? {
      val typeArgs = call.typeArguments
      val arguments = call.stubSafeCallArguments
      val nameElement = arguments.getOrNull(0).asSafely<JSLiteralExpression>()
      val name = if (nameElement != null) {
        getLiteralValue(nameElement) ?: return null
      }
      else {
        MODEL_VALUE_PROP
      }

      val options = when (arguments.size) {
        2 -> arguments[1]
        1 if arguments[0] is JSObjectLiteralExpression -> arguments[0]
        else -> null
      } as? JSObjectLiteralExpression

      val jsType = typeArgs.firstOrNull()?.jsType
                   ?: options?.let { VueSourceModelPropType(name, it) }
                   ?: JSAnyType.get(call)

      return VueScriptSetupModelDecl(call, name, jsType, options, nameElement ?: call)
    }
  }
}

private data class VueScriptSetupModelInputProperty(
  override val modelDecl: VueScriptSetupModelDecl,
) : VueInputProperty, VueModelOwner, PsiSourcedPolySymbol {
  override val name: String
    get() = modelDecl.name

  override val source: PsiElement
    get() = modelDecl.source

  override val required: Boolean
    get() = modelDecl.required

  override val type: JSType
    get() = modelDecl.type

  override fun createPointer(): Pointer<VueScriptSetupModelInputProperty> {
    val modelDecl = modelDecl.createPointer()
    return Pointer { modelDecl.dereference()?.let { VueScriptSetupModelInputProperty(it) } }
  }
}

private data class VueScriptSetupModelEvent(override val modelDecl: VueModelDecl) :
  VueEmitCall, VueModelOwner, PsiSourcedPolySymbol {

  override val name: String
    get() = "$EMIT_CALL_UPDATE_PREFIX${modelDecl.name}"

  override val source: PsiElement
    get() = modelDecl.source

  override val searchTarget: PolySymbolSearchTarget
    get() = PolySymbolSearchTarget.create(this)

  override val params: List<JSParameterTypeDecorator> =
    listOf(JSParameterTypeDecoratorImpl("value", modelDecl.referenceType, false, false, true))

  override val hasStrictSignature: Boolean = true

  override fun createPointer(): Pointer<VueScriptSetupModelEvent> {
    val modelDeclPtr = modelDecl.createPointer()
    return Pointer {
      modelDeclPtr.dereference()?.let { VueScriptSetupModelEvent(it) }
    }
  }
}

private class VueScriptSetupSlot(
  private val typeDeclaration: JSTypeDeclaration,
  signature: JSRecordType.PropertySignature,
) : VueSlot, PsiSourcedPolySymbol {
  override val name: String =
    signature.memberName

  override val source: PsiElement? =
    signature.memberSource.singleElement

  override val type: JSType? =
    signature.jsType?.asSafely<JSFunctionType>()?.parameters?.firstOrNull()?.inferredType

  override fun createPointer(): Pointer<VueScriptSetupSlot> {
    val typeDeclarationPtr = typeDeclaration.createSmartPointer()
    val memberName = name
    return Pointer {
      val typeDeclaration = typeDeclarationPtr.dereference() ?: return@Pointer null
      typeDeclaration.jsType.asRecordType().findPropertySignature(memberName)?.let {
        VueScriptSetupSlot(typeDeclaration, it)
      }
    }
  }

  override fun equals(other: Any?): Boolean =
    other === this
    || other is VueScriptSetupSlot
    && other.typeDeclaration == typeDeclaration
    && other.name == name

  override fun hashCode(): Int {
    var result = typeDeclaration.hashCode()
    result = 31 * result + name.hashCode()
    return result
  }
}
