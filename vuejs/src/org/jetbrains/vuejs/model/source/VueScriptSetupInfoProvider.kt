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
import org.jetbrains.vuejs.index.VueFrameworkHandler
import org.jetbrains.vuejs.index.findModule
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
    override val directives: Map<String, VueDirective>

    override val props: List<VueInputProperty>
    override val emits: List<VueEmitCall>
    override val modelDecls: List<VueModelDecl>

    override val computed: List<VueComputedProperty>
      get() = rawBindings.filterIsInstance(VueComputedProperty::class.java)

    override val data: List<VueDataProperty>
      get() = rawBindings.filterIsInstance(VueDataProperty::class.java)

    override val methods: List<VueMethod>
      get() = rawBindings.filterIsInstance(VueMethod::class.java)

    private val rawBindings: List<VueNamedSymbol>

    init {
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

      this.components = components
      this.directives = directives


      var props: List<VueInputProperty> = emptyList()
      var emits: List<VueEmitCall> = emptyList()
      var rawBindings: List<VueNamedSymbol> = emptyList()
      val modelDecls: MutableMap<String, VueModelDecl> = mutableMapOf()

      module.getStubSafeDefineCalls().forEach { call ->
        when (VueFrameworkHandler.getFunctionNameFromVueIndex(call)) {
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

      this.props = props
      this.emits = emits
      this.modelDecls = modelDecls.values.toList()
      this.rawBindings = rawBindings
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
              .map { (name, property) ->
                VueDefaultContainerInfoProvider.VueSourceInputProperty(name, property, defaults.contains(name))
              }
          }
          is JSArrayLiteralExpression -> {
            props = getStringLiteralsFromInitializerArray(arg)
              .map { literal ->
                val name = getTextIfLiteral(literal) ?: ""
                VueDefaultContainerInfoProvider.VueSourceInputProperty(name, literal, defaults.contains(name))
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

    private fun JSExecutionScope.getStubSafeDefineCalls(): Sequence<JSCallExpression> {
      (this as? JSStubElementImpl<*>)?.stub?.let { moduleStub ->
        return moduleStub.childrenStubs.asSequence().flatMap { stub ->
          when (val psi = stub.psi) {
            is JSCallExpression -> sequenceOf(psi)
            is JSStatement -> {
              stub.childrenStubs.asSequence()
                .filter {
                  it.stubType == JSStubElementTypes.VARIABLE ||
                  it.stubType == TypeScriptStubElementTypes.TYPESCRIPT_VARIABLE ||
                  it.stubType == JSStubElementTypes.DESTRUCTURING_ELEMENT
                }
                .flatMap { it.childrenStubs.asSequence() }
                .filter { it.stubType == JSStubElementTypes.CALL_EXPRESSION }
                .mapNotNull { it.psi as? JSCallExpression }
            }
            else -> emptySequence()
          }
        }
      }

      return this.children.asSequence().filterIsInstance<JSStatement>()
        .flatMap { it.children.asSequence() }
        .map {
          when (it) {
            is JSVariable -> it.initializer
            is JSDestructuringElement -> it.initializer
            else -> it
          }
        }
        .filterIsInstance<JSCallExpression>()
        .mapNotNull { call ->
          when ((call.methodExpression as? JSReferenceExpression)?.referenceName) {
            DEFINE_PROPS_FUN, DEFINE_EMITS_FUN, DEFINE_EXPOSE_FUN, WITH_DEFAULTS_FUN, DEFINE_MODEL_FUN -> {
              call
            }
            else -> null
          }
        }
    }

    private fun analyzeDefineEmits(call: JSCallExpression): List<VueEmitCall> {
      val arg = call.stubSafeCallArguments.getOrNull(0)

      if (arg is JSArrayLiteralExpression) {
        return arg.stubSafeChildren.mapNotNull { literal ->
          (literal as? JSLiteralExpression)
            ?.significantValue
            ?.let { VueScriptSetupLiteralBasedEvent(unquoteWithoutUnescapingStringLiteralValue(it), literal) }
        }
      }

      val eventSources =
        arg.asSafely<JSObjectLiteralExpression>()
          ?.let { JSResolveUtil.getElementJSType(it) }
          ?.asRecordType()
          ?.properties
          ?.associate { it.memberName to it.memberSource.singleElement }
        ?: emptyMap()

      return JSResolveUtil
               .getElementJSType(call)
               ?.asRecordType()
               ?.callSignatures
               ?.mapNotNull { callSignature ->
                 callSignature
                   .functionType
                   .parameters.getOrNull(0)
                   ?.inferredType
                   ?.asSafely<JSStringLiteralTypeImpl>()
                   ?.let {
                     val name = unquoteWithoutUnescapingStringLiteralValue(it.valueAsString)
                     val source = eventSources[name] ?: it.sourceElement
                     VueScriptSetupTypedEvent(name, source, callSignature.functionType)
                   }
               } ?: emptyList()
    }

    private fun analyzeDefineModel(call: JSCallExpression): VueModelDecl? {
      val typeArgs = call.typeArguments
      val arguments = call.stubSafeCallArguments
      val nameElement = arguments.getOrNull(0).asSafely<JSLiteralExpression>()
      val name = if (nameElement != null) {
        nameElement.significantValue?.let { unquoteWithoutUnescapingStringLiteralValue(it) }?.takeIf { it.isNotBlank() } ?: return null
      }
      else {
        MODEL_VALUE_PROP
      }

      val options = when {
        arguments.size == 2 -> arguments[1]
        arguments.size == 1 && arguments[0] is JSObjectLiteralExpression -> arguments[0]
        else -> null
      } as? JSObjectLiteralExpression

      val jsType = typeArgs.firstOrNull()?.jsType ?: options?.let { VueSourceModelPropType(name, it) } ?: JSAnyType.get(call, true)
      return VueScriptSetupModelDecl(name, jsType, options, nameElement ?: call)
    }

    private fun JSCallExpression.getInnerDefineProps(): JSCallExpression? =
      stubSafeCallArguments
        .getOrNull(0)
        .asSafely<JSCallExpression>()
        ?.takeIf { VueFrameworkHandler.getFunctionNameFromVueIndex(it) == DEFINE_PROPS_FUN }

  }

  private class VueScriptSetupInputProperty(private val propertySignature: JSRecordType.PropertySignature,
                                            private val hasOuterDefault: Boolean) : VueInputProperty {
    override val name: String
      get() = propertySignature.memberName

    override val source: PsiElement?
      get() = propertySignature.memberSource.singleElement

    override val required: Boolean
      get() {
        // modifying required by hasOuterDefault is controversial, Vue compiler will still raise a warning in the dev mode,
        // but for editors, it makes more sense to treat such prop as optional.
        if (hasOuterDefault) return false
        return !propertySignature.isOptional
      }

    override val jsType: JSType?
      get() = propertySignature.jsTypeWithOptionality

    override fun toString(): String {
      return "VueScriptSetupInputProperty(name='$name', required=$required, jsType=$jsType)"
    }

  }

  private class VueScriptSetupLiteralBasedEvent(override val name: String,
                                                override val source: PsiElement?) : VueEmitCall

  private class VueScriptSetupTypedEvent(
    override val name: String,
    override val source: PsiElement?,
    private val eventSignature: JSFunctionType,
  ) : VueEmitCall {
    override val params: List<JSParameterTypeDecorator>
      get() = eventSignature.parameters.drop(1)

    override val hasStrictSignature: Boolean
      get() = true
  }

  private class VueScriptSetupModelDecl(override val name: String,
                                        modelType: JSType,
                                        options: JSObjectLiteralExpression?,
                                        sourceElement: PsiElement) : VueModelDecl {

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

  private class VueScriptSetupModelInputProperty(private val modelDecl: VueModelDecl) : VueInputProperty {
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

  private class VueScriptSetupModelEvent(private val modelDecl: VueModelDecl) : VueEmitCall {
    override val name: String
      get() = "update:${modelDecl.name}"

    override val source: PsiElement?
      get() = modelDecl.source

    override val params: List<JSParameterTypeDecorator> =
      listOf(JSParameterTypeDecoratorImpl("value", modelDecl.referenceType, false, false, true))

    override val hasStrictSignature: Boolean = true
  }

}
