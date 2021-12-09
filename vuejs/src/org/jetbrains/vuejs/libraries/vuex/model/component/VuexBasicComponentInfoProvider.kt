// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.vuejs.libraries.vuex.model.component

import com.intellij.extapi.psi.StubBasedPsiElementBase
import com.intellij.lang.javascript.JSElementTypes
import com.intellij.lang.javascript.psi.*
import com.intellij.lang.javascript.psi.ecma6.impl.JSLocalImplicitElementImpl
import com.intellij.lang.javascript.psi.ecma6.impl.JSLocalImplicitFunctionImpl
import com.intellij.lang.javascript.psi.stubs.JSImplicitElement
import com.intellij.psi.PsiElement
import com.intellij.psi.stubs.StubElement
import com.intellij.psi.util.CachedValueProvider
import com.intellij.psi.util.CachedValuesManager
import com.intellij.psi.util.PsiModificationTracker
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.util.castSafelyTo
import one.util.streamex.StreamEx
import org.jetbrains.vuejs.libraries.vuex.VuexUtils.MAP_ACTIONS
import org.jetbrains.vuejs.libraries.vuex.VuexUtils.MAP_GETTERS
import org.jetbrains.vuejs.libraries.vuex.VuexUtils.MAP_MUTATIONS
import org.jetbrains.vuejs.libraries.vuex.VuexUtils.MAP_STATE
import org.jetbrains.vuejs.libraries.vuex.model.store.VuexStoreStateElement
import org.jetbrains.vuejs.model.*
import org.jetbrains.vuejs.model.source.COMPUTED_PROP
import org.jetbrains.vuejs.model.source.METHODS_PROP
import org.jetbrains.vuejs.model.source.VueContainerInfoProvider

class VuexBasicComponentInfoProvider : VueContainerInfoProvider.VueInitializedContainerInfoProvider(::VuexComponentInfo) {

  private class VuexComponentInfo(declaration: JSElement) : VueInitializedContainerInfo(declaration) {

    override val computed: List<VueComputedProperty> get() = get(COMPUTED_STATE) + get(COMPUTED_GETTERS)
    override val methods: List<VueMethod> get() = get(METHODS)

    companion object {
      private val COMPUTED_STATE = SimpleMemberAccessor(ContainerMember.ComputedState, ::VuexMappedSourceComputedStateProperty)
      private val COMPUTED_GETTERS = SimpleMemberAccessor(ContainerMember.ComputedGetters, ::VuexMappedSourceComputedGetterProperty)
      private val METHODS = SimpleMemberAccessor(ContainerMember.Methods, ::VuexMappedSourceMethod)
    }

    private class SimpleMemberAccessor<T : VueNamedSymbol>(val member: ContainerMember,
                                                           val provider: (String, JSElement) -> T)
      : ListAccessor<T>() {

      override fun build(declaration: JSElement): List<T> {
        return member.readMembers(declaration).map { (name, element) -> provider(name, element) }
      }
    }

    private enum class ContainerMember(val propertyName: String,
                                       private vararg val functionNames: String) {
      ComputedState(COMPUTED_PROP, MAP_STATE),
      ComputedGetters(COMPUTED_PROP, MAP_GETTERS),
      Methods(METHODS_PROP, MAP_ACTIONS, MAP_MUTATIONS);

      fun readMembers(descriptor: JSElement): List<Pair<String, JSElement>> {
        val property = (descriptor as? JSObjectLiteralExpression)?.findProperty(propertyName) ?: return emptyList()

        PsiTreeUtil.getStubChildOfType(property, JSCallExpression::class.java)
          ?.let {
            return if (functionNames.contains((it.stubSafeMethodExpression as? JSReferenceExpression)?.referenceName))
              readArguments(it)
            else emptyList()
          }
        return (property.objectLiteralExpressionInitializer ?: return emptyList())
          .let { PsiTreeUtil.getStubChildrenOfTypeAsList(it, JSSpreadExpression::class.java) }
          .let { StreamEx.of(it) }
          .map { PsiTreeUtil.getStubChildOfType(it, JSCallExpression::class.java) }
          .filter { it != null && functionNames.contains((it.stubSafeMethodExpression as? JSReferenceExpression)?.referenceName) }
          .flatCollection { readArguments(it!!) }
          .distinctBy { it.first }
          .toList()
      }

      fun readArguments(call: JSCallExpression): List<Pair<String, JSElement>> {
        (call as? StubBasedPsiElementBase<*>)
          ?.greenStub
          ?.let {
            return readStubbedArguments(it)
          }
        return readPsiArguments(call)
      }

      fun readPsiArguments(call: JSCallExpression): List<Pair<String, JSElement>> {
        val args = call.arguments
        val initializer = if (args.size > 1 && args[0] is JSLiteralExpression) {
          args[1]
        }
        else if (args.isNotEmpty()) {
          args[0]
        }
        else {
          return emptyList()
        }
        if (initializer is JSObjectLiteralExpression) {
          return initializer.properties.asSequence()
            .filter { it.name != null }
            .map { Pair(it.name!!, it) }
            .toList()
        }
        if (initializer is JSArrayLiteralExpression) {
          return initializer.expressions.asSequence()
            .mapNotNull { it as? JSLiteralExpression }
            .filter { it.isQuotedLiteral && it.stringValue != null }
            .map { Pair(it.stringValue!!, it) }
            .toList()
        }
        return emptyList()
      }

      fun readStubbedArguments(call: StubElement<PsiElement>): List<Pair<String, JSElement>> {
        call.findChildStubByType(JSElementTypes.OBJECT_LITERAL_EXPRESSION)
          ?.let { stub ->
            return stub.psi.properties.asSequence()
              .filter { it.name != null }
              .map { Pair(it.name!!, it) }
              .toList()
          }
        return call.getChildrenByType(JSElementTypes.LITERAL_EXPRESSION) { arrayOfNulls<JSLiteralExpression>(it) }
          .asSequence()
          .filter { it?.significantValue != null }
          .map { Pair(it!!.significantValue!!, it) }
          .toList()
      }

    }
  }
}

private class VuexMappedSourceComputedStateProperty(override val name: String,
                                                    private val element: JSElement) : VueComputedProperty {
  override val source: JSElement
    get() = getCachedVuexImplicitElement(element, true, name, JSImplicitElement.Type.Property) { name, resolved ->
      when (resolved) {
        is JSProperty -> resolved.jsType
        is JSFunctionItem -> resolved.returnType
        else -> null
      }?.let {
        VueImplicitElement(name, it, resolved, JSImplicitElement.Type.Property, true)
      }
    }

  override val jsType: JSType? get() = (source as? JSTypeOwner)?.jsType
}

private class VuexMappedSourceComputedGetterProperty(override val name: String,
                                                     private val element: JSElement) : VueComputedProperty {
  override val source: JSElement
    get() = getCachedVuexImplicitElement(element, false, name, JSImplicitElement.Type.Property) { name, resolved ->
      (resolved as? JSFunctionItem)?.let { function ->
        VueImplicitElement(name, function.returnType, function, JSImplicitElement.Type.Property, true)
      }
    }

  override val jsType: JSType? get() = (source as? JSTypeOwner)?.jsType
}

private class VuexMappedSourceMethod(override val name: String,
                                     private val element: JSElement) : VueMethod {
  override val source: JSElement
    get() = getCachedVuexImplicitElement(element, false, name, JSImplicitElement.Type.Function) { name, resolved ->
      (resolved as? JSFunctionItem)?.let { function ->
        VueImplicitFunction(
          name, function.returnType, function,
          function.parameters.asSequence()
            .drop(1)
            .map { JSLocalImplicitFunctionImpl.ParameterImpl(it.name, it.inferredType, it.isOptional, it.isRest) }
            .toList())
      }
    }

  override val jsType: JSType? get() = (source as? JSTypeOwner)?.jsType
}

private fun resolveToVuexSymbol(source: JSElement, resolveState: Boolean): JSElement? {
  var element = source
  var function: JSFunctionItem? = null

  if (element is JSProperty) {
    function = element.tryGetFunctionInitializer()
    if (function == null && JSTypeUtils.isStringOrStringUnion(element.jsType, false)) {
      element.initializer?.let {
        element = it
      }
    }
  }
  return if (function == null && element is JSLiteralExpression) {
    element.references.lastOrNull()?.resolve()
      ?.castSafelyTo<JSLocalImplicitElementImpl>()
      ?.takeIf { it is VueImplicitElement || (resolveState && it is VuexStoreStateElement) }
      ?.context
      ?.let {
        if (resolveState) {
          it.castSafelyTo<JSProperty>()
        }
        else when (it) {
          is JSFunctionItem -> it
          is JSProperty -> it.tryGetFunctionInitializer()
          else -> null
        }
      }
  }
  else {
    function
  }
}

private fun getCachedVuexImplicitElement(source: JSElement,
                                         resolveState: Boolean,
                                         name: String,
                                         type: JSImplicitElement.Type,
                                         elementProvider: (name: String, el: JSElement) -> JSImplicitElement?): JSImplicitElement =
  CachedValuesManager.getCachedValue(source, CachedValuesManager.getManager(source.project).getKeyForClass(elementProvider::class.java)) {
    CachedValueProvider.Result.create(
      resolveToVuexSymbol(source, resolveState)?.let { elementProvider(name, it) }
      ?: VueImplicitElement(name, null, source, type, false), PsiModificationTracker.MODIFICATION_COUNT)
  }
