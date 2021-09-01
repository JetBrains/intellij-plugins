// Copyright 2000-2021 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.vuejs.model.source

import com.intellij.lang.ecmascript6.psi.ES6ImportSpecifier
import com.intellij.lang.javascript.JSStubElementTypes
import com.intellij.lang.javascript.evaluation.JSCodeBasedTypeFactory
import com.intellij.lang.javascript.psi.*
import com.intellij.lang.javascript.psi.ecma6.TypeScriptObjectType
import com.intellij.lang.javascript.psi.ecma6.TypeScriptPropertySignature
import com.intellij.lang.javascript.psi.impl.JSStubElementImpl
import com.intellij.lang.javascript.psi.resolve.JSEvaluateContext
import com.intellij.lang.javascript.psi.types.JSTypeComparingContextService.NULL_CHECKS
import com.intellij.lang.javascript.psi.types.primitives.JSUndefinedType
import com.intellij.lang.javascript.psi.util.JSStubBasedPsiTreeUtil
import com.intellij.openapi.util.io.FileUtil
import com.intellij.psi.PsiElement
import com.intellij.psi.util.CachedValueProvider
import com.intellij.psi.util.CachedValuesManager
import com.intellij.psi.util.PsiModificationTracker
import com.intellij.psi.xml.XmlFile
import com.intellij.util.ProcessingContext
import com.intellij.util.castSafelyTo
import org.jetbrains.vuejs.codeInsight.collectPropertiesRecursively
import org.jetbrains.vuejs.codeInsight.getStringLiteralsFromInitializerArray
import org.jetbrains.vuejs.codeInsight.getTextIfLiteral
import org.jetbrains.vuejs.codeInsight.stubSafeCallArguments
import org.jetbrains.vuejs.index.VueFrameworkHandler
import org.jetbrains.vuejs.index.findModule
import org.jetbrains.vuejs.model.*

class VueScriptSetupInfoProvider : VueContainerInfoProvider {

  override fun getInfo(descriptor: VueSourceEntityDescriptor): VueContainerInfoProvider.VueContainerInfo? =
    descriptor.source
      .takeIf { it is JSObjectLiteralExpression || it is XmlFile }
      ?.let { findModule(it, true) }
      ?.let { module ->
        CachedValuesManager.getCachedValue(module) {
          CachedValueProvider.Result.create(VueScriptSetupInfo(module), PsiModificationTracker.MODIFICATION_COUNT)
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
        }, false
      )
      val fileName = FileUtil.getNameWithoutExtension(module.containingFile.name)
      VueModelManager.findEnclosingComponent(module)?.let { component ->
        components.computeIfAbsent(fileName.capitalize()) { component }
      }
      this.components = components
      this.directives = directives

      var props: List<VueInputProperty> = emptyList()
      val emits: List<VueEmitCall> = emptyList()
      var rawBindings: List<VueNamedSymbol> = emptyList()

      module.getStubSafeDefineCalls()
        .forEach { call ->
          when (call.indexingData
            ?.implicitElements?.find { it.userString == VueFrameworkHandler.METHOD_NAME_USER_STRING }
            ?.name) {

            DEFINE_PROPS_FUN -> {
              val arguments = call.stubSafeCallArguments
              val typeArgs = call.typeArguments
              if (typeArgs.size == 1) {
                val arg = typeArgs[0]
                if (arg is TypeScriptObjectType) {
                  props = arg.typeMembers.asSequence()
                    .filterIsInstance<TypeScriptPropertySignature>()
                    .map { VueScriptSetupInputProperty(it) }
                    .toList()
                }
              }
              else if (arguments.size == 1) {
                val arg = arguments[0]
                if (arg is JSObjectLiteralExpression) {
                  props = collectPropertiesRecursively(arg)
                    .map { (name, property) -> VueDefaultContainerInfoProvider.VueSourceInputProperty(name, property) }
                }
                else if (arg is JSArrayLiteralExpression) {
                  props = getStringLiteralsFromInitializerArray(arg)
                    .map { VueDefaultContainerInfoProvider.VueSourceInputProperty(getTextIfLiteral(it) ?: "", it) }
                }
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

    private fun JSEmbeddedContent.getStubSafeDefineCalls(): Sequence<JSCallExpression> {
      (this as? JSStubElementImpl<*>)?.stub?.let { moduleStub ->
        return moduleStub.childrenStubs.asSequence()
          .flatMap { stub ->
            when (val psi = stub.psi) {
              is JSCallExpression -> sequenceOf(psi)
              is JSStatement -> {
                stub.childrenStubs.asSequence()
                  .filter { it.stubType == JSStubElementTypes.VARIABLE }
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
        .map { if (it is JSVariable) it.initializer else it }
        .filterIsInstance<JSCallExpression>()
        .mapNotNull { call ->
          when ((call.methodExpression as? JSReferenceExpression)?.referenceName) {
            WITH_DEFAULTS_FUN -> {
              call.arguments.getOrNull(0)
                .castSafelyTo<JSCallExpression>()
                ?.takeIf { (it.methodExpression as? JSReferenceExpression)?.referenceName == DEFINE_PROPS_FUN }
            }
            DEFINE_PROPS_FUN, DEFINE_EMITS_FUN, DEFINE_EXPOSE_FUN -> {
              call
            }
            else -> null
          }
        }
    }

  }

  private class VueScriptSetupInputProperty(private val propertySignature: TypeScriptPropertySignature) : VueInputProperty {

    private val context = ProcessingContext().also { it.put(NULL_CHECKS, true) }

    override val source: PsiElement?
      get() = propertySignature.memberSource.singleElement

    override val jsType: JSType?
      get() = propertySignature.jsTypeWithOptionality

    override val required: Boolean
      get() = propertySignature.jsTypeWithOptionality?.let {
        it.isDirectlyAssignableType(JSUndefinedType(it.source), context)
      } == false

    override val name: String
      get() = propertySignature.memberName

  }

}