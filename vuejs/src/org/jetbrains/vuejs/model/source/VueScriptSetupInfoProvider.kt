// Copyright 2000-2021 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.vuejs.model.source

import com.intellij.lang.ecmascript6.psi.ES6ImportSpecifier
import com.intellij.lang.javascript.JSStubElementTypes
import com.intellij.lang.javascript.evaluation.JSCodeBasedTypeFactory
import com.intellij.lang.javascript.psi.*
import com.intellij.lang.javascript.psi.impl.JSStubElementImpl
import com.intellij.lang.javascript.psi.resolve.JSEvaluateContext
import com.intellij.lang.javascript.psi.util.JSStubBasedPsiTreeUtil
import com.intellij.lang.typescript.TypeScriptStubElementTypes
import com.intellij.openapi.util.NlsSafe
import com.intellij.openapi.util.io.FileUtil
import com.intellij.psi.PsiElement
import com.intellij.psi.util.CachedValueProvider
import com.intellij.psi.util.CachedValuesManager
import com.intellij.psi.util.PsiModificationTracker
import com.intellij.psi.xml.XmlFile
import com.intellij.util.asSafely
import com.intellij.util.containers.sequenceOfNotNull
import org.jetbrains.vuejs.codeInsight.collectMembers
import org.jetbrains.vuejs.codeInsight.getStringLiteralsFromInitializerArray
import org.jetbrains.vuejs.codeInsight.getTextIfLiteral
import org.jetbrains.vuejs.codeInsight.stubSafeCallArguments
import org.jetbrains.vuejs.index.VueFrameworkHandler
import org.jetbrains.vuejs.index.findModule
import org.jetbrains.vuejs.model.*

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

  class VueScriptSetupInfo(val module: JSEmbeddedContent) : VueContainerInfoProvider.VueContainerInfo {

    override val components: Map<String, VueComponent>
    override val directives: Map<String, VueDirective>

    override val props: List<VueInputProperty>
    override val emits: List<VueEmitCall>

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
          if (name?.getOrNull(0)?.isUpperCase() == true) {
            (VueModelManager.getComponent(VueComponents.getComponentDescriptor(element)) ?: VueUnresolvedComponent(element, element, name))
              .let { components[name] = if (it is VueRegularComponent) VueLocallyDefinedRegularComponent(it, element) else it }
          }
          else if (name?.getOrNull(0) == 'v' && name.getOrNull(1)?.isUpperCase() == true) {
            directives[name.substring(1)] = VueSourceDirective(name.substring(1), element)
          }
          true
        },
        false
      )

      val fileName = FileUtil.getNameWithoutExtension(module.containingFile.name)
      VueModelManager.findEnclosingComponent(module)?.let { component ->
        components.putIfAbsent(fileName.capitalize(), component)
      }

      this.components = components
      this.directives = directives


      var props: List<VueInputProperty> = emptyList()
      val emits: List<VueEmitCall> = emptyList()
      var rawBindings: List<VueNamedSymbol> = emptyList()

      module.getStubSafeDefineCalls().forEach { call ->
        when (VueFrameworkHandler.getFunctionNameFromVueIndex(call)) {
          DEFINE_PROPS_FUN -> {
            val parent = call.context
            val defaults = when { // todo is there a spread here?
              //parent is JSCallExpression && isDefinePropsCallExpression(parent) -> {
              //  val objectLiteral = parent.arguments.getOrNull(1) as? JSObjectLiteralExpression
              //  objectLiteral?.properties?.mapNotNull { it.name } ?: listOf()
              //}
              parent is JSDestructuringElement -> {
                // parent.target could be an JSDestructuringArray, but Vue does not support it
                val objectDestructure = parent.target as? JSDestructuringObject
                objectDestructure?.properties?.mapNotNull { destructuringProperty ->
                  val element = destructuringProperty.destructuringElement
                  if (!destructuringProperty.isRest && element is JSVariable && element.hasOwnInitializer()) {
                    element.name
                  }
                  else {
                    null // aliased prop without default value
                  }
                } ?: listOf()
              }
              else -> listOf()
            }
            props = analyzeDefineProps(call, defaults)
          }
          WITH_DEFAULTS_FUN -> {
            val definePropsCall = call.getInnerDefineProps().firstOrNull()
            if (definePropsCall != null) {
              // Vue can throw Error: [@vue/compiler-sfc] The 2nd argument of withDefaults must be an object literal.
              // Let's be more forgiving here and try to interpret props without defaults
              val withDefaultsObjectLiteral = call.stubSafeCallArguments.getOrNull(1) as? JSObjectLiteralExpression
              val defaults = withDefaultsObjectLiteral?.properties?.mapNotNull { it.name } ?: listOf()

              props = analyzeDefineProps(definePropsCall, defaults)
            }
          }
          DEFINE_EMITS_FUN -> {
            // TODO
          }
          DEFINE_EXPOSE_FUN -> {
            rawBindings = call.stubSafeCallArguments
                            .getOrNull(0)
                            ?.let { JSCodeBasedTypeFactory.getPsiBasedType(it, JSEvaluateContext(it.containingFile)) }
                            ?.let { VueCompositionInfoHelper.createRawBindings(call, it) }
                          ?: emptyList()

          }
        }
      }

      this.props = props
      this.emits = emits
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

    private fun JSEmbeddedContent.getStubSafeDefineCalls(): Sequence<JSCallExpression> {
      (this as? JSStubElementImpl<*>)?.stub?.let { moduleStub ->
        return moduleStub.childrenStubs.asSequence().flatMap { stub ->
          when (val psi = stub.psi) {
            is JSCallExpression -> sequenceOf(psi)
            is JSStatement -> {
              stub.childrenStubs.asSequence()
                .filter { it.stubType == JSStubElementTypes.VARIABLE ||
                          it.stubType == TypeScriptStubElementTypes.TYPESCRIPT_VARIABLE ||
                          it.stubType == JSStubElementTypes.DESTRUCTURING_ELEMENT  }
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
            DEFINE_PROPS_FUN, DEFINE_EMITS_FUN, DEFINE_EXPOSE_FUN, WITH_DEFAULTS_FUN -> {
              call
            }
            else -> null
          }
        }
    }

    private fun JSCallExpression.getInnerDefineProps(): Sequence<JSCallExpression> {
      (this as? JSStubElementImpl<*>)?.stub?.let { callStub ->
        return callStub.childrenStubs.asSequence()
          .filter { it.stubType == JSStubElementTypes.CALL_EXPRESSION }
          .mapNotNull { it.psi as? JSCallExpression }
          .filter { innerCall -> VueFrameworkHandler.getFunctionNameFromVueIndex(innerCall) == DEFINE_PROPS_FUN }
      }

      return sequenceOfNotNull(this.arguments.getOrNull(0)
                                 .asSafely<JSCallExpression>()
                                 ?.takeIf { (it.methodExpression as? JSReferenceExpression)?.referenceName == DEFINE_PROPS_FUN }
      )
    }

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

}
