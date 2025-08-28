// Copyright 2000-2021 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.vuejs.model.source

import com.intellij.lang.ecmascript6.psi.ES6ImportSpecifier
import com.intellij.lang.javascript.JSStringUtil.unquoteWithoutUnescapingStringLiteralValue
import com.intellij.lang.javascript.JSStubElementTypes
import com.intellij.lang.javascript.evaluation.JSCodeBasedTypeFactory
import com.intellij.lang.javascript.psi.*
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
import com.intellij.lang.typescript.TypeScriptStubElementTypes
import com.intellij.openapi.util.NlsSafe
import com.intellij.openapi.util.io.FileUtil
import com.intellij.openapi.util.text.StringUtil
import com.intellij.psi.PsiElement
import com.intellij.psi.util.CachedValueProvider
import com.intellij.psi.util.CachedValuesManager
import com.intellij.psi.util.PsiModificationTracker
import com.intellij.psi.xml.XmlFile
import com.intellij.util.asSafely
import org.jetbrains.vuejs.codeInsight.*
import org.jetbrains.vuejs.index.findModule
import org.jetbrains.vuejs.index.getFunctionNameFromVueIndex
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

    override val components: Map<String, VueComponent>
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

    private val injectionCalls: List<VueNamedSymbol>
      get() = CachedValuesManager.getCachedValue(module) {
        CachedValueProvider.Result.create(getInjectionCalls(module), PsiModificationTracker.MODIFICATION_COUNT)
      }

    private fun analyzeModule(module: JSExecutionScope): VueScriptSetupStructure {
      val components = mutableMapOf<String, VueComponent>()
      val directives = mutableMapOf<String, VueDirective>()

      JSStubBasedPsiTreeUtil.processDeclarationsInScope(
        module,
        { element, _ ->
          val name = (element as? JSPsiNamedElementBase)?.let { if (it is ES6ImportSpecifier) it.declaredName else it.name }
          if (name != null && isScriptSetupLocalDirectiveName(name)) {
            directives[name.substring(1)] = VueScriptSetupLocalDirective(name, element)
          }
          else if (name != null && element !is JSClass) {
            (VueModelManager.getComponent(VueComponents.getComponentDescriptor(element)) ?: VueUnresolvedComponent(element, element, name))
              .let { components[name] = if (it is VueRegularComponent) VueLocallyDefinedRegularComponent(it, element) else it }
          }
          true
        },
        false
      )

      val fileName = FileUtil.getNameWithoutExtension(module.containingFile.name)
      VueModelManager.findEnclosingComponent(module)?.let { component ->
        components.putIfAbsent(StringUtil.capitalize(fileName), component)
      }

      var props: List<VueInputProperty> = emptyList()
      var emits: List<VueEmitCall> = emptyList()
      var slots: List<VueSlot> = emptyList()
      var rawBindings: List<VueNamedSymbol> = emptyList()
      val modelDecls: MutableMap<String, VueModelDecl> = mutableMapOf()

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
            rawBindings = call.stubSafeCallArguments
                            .getOrNull(0)
                            ?.let { JSCodeBasedTypeFactory.getPsiBasedType(it, JSEvaluateContext(it.containingFile)) }
                            ?.let { VueCompositionInfoHelper.createRawBindings(call, it) }
                          ?: emptyList()

          }
          DEFINE_MODEL_FUN -> analyzeDefineModel(call)?.let { modelDecls[it.name] = it }
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

    private fun getInjectionCalls(module: JSExecutionScope): List<VueNamedSymbol> {
      val symbols: MutableList<VueNamedSymbol> = mutableListOf()

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
          .map { VueScriptSetupInputProperty(it, defaults.contains(it.memberName)) }
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
          (literal as? JSLiteralExpression)
            ?.significantValue
            ?.let { VueScriptSetupLiteralBasedEvent(unquoteWithoutUnescapingStringLiteralValue(it), literal) }
        }
      }

      val typeArgument = call.typeArguments
        .firstOrNull()
        ?.jsType
        ?.asRecordType()

      if (typeArgument != null && typeArgument.hasProperties()) {
        return typeArgument.properties.map {
          VueScriptSetupPropertyContractEvent(
            name = it.memberName,
            source = it.memberSource.singleElement,
            parametersType = it.jsType,
          )
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
              VueScriptSetupCallSignatureEvent(name, source, callSignature.functionType)
            }
        }
    }

    private fun analyzeDefineSlots(call: JSCallExpression): List<VueSlot> =
      call.takeIf { JSStubBasedPsiTreeUtil.isStubBased(it) }
        ?.typeArguments
        ?.singleOrNull()
        ?.jsType?.asRecordType()?.typeMembers
        ?.asSequence()
        ?.filterIsInstance<JSRecordType.PropertySignature>()
        ?.map {
          val slotType = it.jsType?.asSafely<JSFunctionType>()?.parameters?.firstOrNull()?.inferredType
          VueScriptSetupSlot(it.memberName, it.memberSource.singleElement, slotType)
        }
        ?.toList()
      ?: emptyList()

    private fun analyzeDefineModel(call: JSCallExpression): VueModelDecl? {
      val typeArgs = call.typeArguments
      val arguments = call.stubSafeCallArguments
      val nameElement = arguments.getOrNull(0).asSafely<JSLiteralExpression>()
      val name = if (nameElement != null) {
        getLiteralValue(nameElement) ?: return null
      }
      else {
        MODEL_VALUE_PROP
      }

      val options = when {
        arguments.size == 2 -> arguments[1]
        arguments.size == 1 && arguments[0] is JSObjectLiteralExpression -> arguments[0]
        else -> null
      } as? JSObjectLiteralExpression

      val jsType = typeArgs.firstOrNull()?.jsType ?: options?.let { VueSourceModelPropType(name, it) } ?: JSAnyType.get(call)
      return VueScriptSetupModelDecl(name, jsType, options, nameElement ?: call)
    }

    private fun JSCallExpression.getInnerDefineProps(): JSCallExpression? =
      stubSafeCallArguments
        .getOrNull(0)
        .asSafely<JSCallExpression>()
        ?.takeIf { getFunctionNameFromVueIndex(it) == DEFINE_PROPS_FUN }

    private fun getLiteralValue(literal: JSLiteralExpression): String? =
      literal.significantValue
        ?.let { unquoteWithoutUnescapingStringLiteralValue(it) }
        ?.takeIf { it.isNotBlank() }

    override fun equals(other: Any?): Boolean =
      (other as? VueScriptSetupInfo)?.module == module

    override fun hashCode(): Int = module.hashCode()
  }

  private class VueScriptSetupInputProperty(
    private val propertySignature: JSRecordType.PropertySignature,
    private val hasOuterDefault: Boolean,
  ) : VueInputProperty {
    override val name: String
      get() = propertySignature.memberName

    override val source: PsiElement? = propertySignature.memberSource.singleElement?.let { sourceElement ->
      VueImplicitElement(name, propertySignature.jsType?.optionalIf(isOptional),
                         sourceElement, JSImplicitElement.Type.Property, true)
    }

    override val required: Boolean
      get() {
        // modifying required by hasOuterDefault is controversial, Vue compiler will still raise a warning in the dev mode,
        // but for editors, it makes more sense to treat such prop as optional.
        if (hasOuterDefault) return false
        return !propertySignature.isOptional
      }

    override val jsType: JSType?
      get() = propertySignature.jsTypeWithOptionality

    private val isOptional: Boolean
      get() = if (hasOuterDefault) false else propertySignature.isOptional

    override fun toString(): String {
      return "VueScriptSetupInputProperty(name='$name', required=$required, jsType=$jsType)"
    }

  }

  private class VueScriptSetupLiteralBasedEvent(
    override val name: String,
    override val source: PsiElement?,
  ) : VueEmitCall

  private class VueScriptSetupCallSignatureEvent(
    override val name: String,
    override val source: PsiElement?,
    private val eventSignature: JSFunctionType,
  ) : VueEmitCall {
    override val params: List<JSParameterTypeDecorator>
      get() = eventSignature.parameters.drop(1)

    override val hasStrictSignature: Boolean
      get() = true
  }

  private class VueScriptSetupPropertyContractEvent(
    override val name: String,
    override val source: PsiElement?,
    private val parametersType: JSType?,
  ) : VueEmitCall {
    override val params: List<JSParameterTypeDecorator> by lazy {
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
  }

  private class VueScriptSetupModelDecl(
    override val name: String,
    modelType: JSType,
    options: JSObjectLiteralExpression?,
    sourceElement: PsiElement,
  ) : VueModelDecl {

    override val required: Boolean = getRequiredFromPropOptions(options)

    override val local: Boolean = getLocalFromPropOptions(options)

    override val jsType: JSType = modelType.optionalIf(!required)

    override val referenceType: JSType = modelType.optionalIf(getPropOptionality(options, required))

    override val source: PsiElement = VueImplicitElement(
      name, referenceType, sourceElement, JSImplicitElement.Type.Property, true)

    override fun toString(): String {
      return "VueScriptSetupModelDecl(name='$name', required=$required, jsType=$jsType, local=$local)"
    }
  }

  private class VueScriptSetupModelInputProperty(override val modelDecl: VueModelDecl) : VueInputProperty, VueModelOwner {
    override val name: String
      get() = modelDecl.name

    override val source: PsiElement?
      get() = modelDecl.source

    override val required: Boolean
      get() = modelDecl.required

    override val jsType: JSType?
      get() = modelDecl.jsType

    override fun toString(): String {
      return "VueScriptSetupModelInputProperty(modelDecl=$modelDecl)"
    }
  }

  private class VueScriptSetupModelEvent(override val modelDecl: VueModelDecl) : VueEmitCall, VueModelOwner {
    override val name: String
      get() = "update:${modelDecl.name}"

    override val source: PsiElement?
      get() = modelDecl.source

    override val params: List<JSParameterTypeDecorator> =
      listOf(JSParameterTypeDecoratorImpl("value", modelDecl.referenceType, false, false, true))

    override val hasStrictSignature: Boolean = true
  }

  private class VueScriptSetupSlot(
    override val name: String,
    override val source: PsiElement?,
    override val scope: JSType?,
  ) : VueSlot

  private data class VueScriptSetupStructure(
    val components: Map<String, VueComponent>,
    val directives: Map<String, VueDirective>,
    val props: List<VueInputProperty>,
    val emits: List<VueEmitCall>,
    val slots: List<VueSlot>,
    val rawBindings: List<VueNamedSymbol>,
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
              it.elementType == JSStubElementTypes.VARIABLE ||
              it.elementType == TypeScriptStubElementTypes.TYPESCRIPT_VARIABLE ||
              it.elementType == JSStubElementTypes.DESTRUCTURING_ELEMENT
            }
            .flatMap { it.childrenStubs.asSequence() }
            .filter { it.elementType == JSStubElementTypes.CALL_EXPRESSION }
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
