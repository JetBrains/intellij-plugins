// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.vuejs.libraries.vuex.model.store

import com.intellij.extapi.psi.StubBasedPsiElementBase
import com.intellij.lang.javascript.JSElementTypes
import com.intellij.lang.javascript.psi.*
import com.intellij.lang.javascript.psi.ecma6.impl.JSLocalImplicitElementImpl
import com.intellij.lang.javascript.psi.stubs.JSImplicitElement
import com.intellij.lang.javascript.psi.types.JSTypeSourceFactory
import com.intellij.psi.PsiElement
import com.intellij.psi.util.CachedValueProvider.Result
import com.intellij.psi.util.CachedValuesManager
import com.intellij.psi.util.PsiModificationTracker
import org.jetbrains.vuejs.codeInsight.objectLiteralFor
import org.jetbrains.vuejs.codeInsight.resolveElementTo
import org.jetbrains.vuejs.libraries.vuex.types.VuexGetterType
import org.jetbrains.vuejs.model.VueImplicitElement
import org.jetbrains.vuejs.model.source.EntityContainerInfoProvider.InitializedContainerInfoProvider.BooleanValueAccessor
import org.jetbrains.vuejs.model.source.VueSourceEntityDescriptor

abstract class VuexContainerImpl : VuexContainer {

  override val actions: Map<String, VuexAction>
    get() = get(VuexContainerInfoProvider.VuexContainerInfo::actions)

  override val getters: Map<String, VuexGetter>
    get() = get(VuexContainerInfoProvider.VuexContainerInfo::getters)

  override val state: Map<String, VuexStateProperty>
    get() = get(VuexContainerInfoProvider.VuexContainerInfo::state)

  override val mutations: Map<String, VuexMutation>
    get() = get(VuexContainerInfoProvider.VuexContainerInfo::mutations)

  override val modules: Map<String, VuexModule>
    get() = get(VuexContainerInfoProvider.VuexContainerInfo::modules)

  private fun <T> get(accessor: (VuexContainerInfoProvider.VuexContainerInfo) -> Map<String, T>): Map<String, T> =
    getInfo()?.let { accessor.invoke(it) } ?: mapOf()

  protected fun get(accessor: (VuexContainerInfoProvider.VuexContainerInfo) -> Boolean): Boolean =
    getInfo()?.let { accessor.invoke(it) } == true

  private fun getInfo(): VuexContainerInfoProvider.VuexContainerInfo? =
    initializer?.let { VuexContainerInfoProvider.INSTANCE.getInfo(VueSourceEntityDescriptor(it, null)) }
}

class VuexModuleImpl(override val name: String,
                     private val initializerElement: PsiElement,
                     nameElement: PsiElement,
                     private val forceNamespaced: Boolean = false) : VuexContainerImpl(), VuexModule {

  constructor(name: String, element: PsiElement, forceNamespaced: Boolean = false) : this(name, element, element, forceNamespaced)

  override val source = nameElement

  override fun getResolveTarget(namespace: String, qualifiedName: String): JSLocalImplicitElementImpl {
    return VueImplicitElement(qualifiedName.substring(namespace.length), null, source,
                              JSImplicitElement.Type.Variable, true)
  }

  override val isNamespaced: Boolean
    get() = forceNamespaced || get(VuexContainerInfoProvider.VuexContainerInfo::isNamespaced)

  override val initializer: JSElement?
    get() {
      val initializerElement = initializerElement
      if (initializerElement is JSObjectLiteralExpression) return initializerElement
      if (initializerElement is JSFile) return initializerElement
      return CachedValuesManager.getCachedValue(initializerElement) {
        resolveElementTo(initializerElement, JSObjectLiteralExpression::class, JSFile::class)
          ?.let { Result.create(it, initializerElement, it) }
        ?: Result.create(null as JSElement?, initializerElement, PsiModificationTracker.MODIFICATION_COUNT)
      }
    }
}

class VuexStoreImpl(override val source: JSCallExpression) : VuexContainerImpl(), VuexStore {
  override val initializer: JSObjectLiteralExpression?
    get() {
      val storeCreationCall = this.source
      return CachedValuesManager.getCachedValue(storeCreationCall) {
        readLiteralFromParams(storeCreationCall)
          ?.let { Result.create(it, storeCreationCall, it) }
        ?: Result.create(null as JSObjectLiteralExpression?, storeCreationCall, PsiModificationTracker.MODIFICATION_COUNT)
      }
    }

  companion object {
    fun readLiteralFromParams(call: JSCallExpression): JSObjectLiteralExpression? {
      (call as? StubBasedPsiElementBase<*>)
        ?.stub
        ?.let {
          @Suppress("USELESS_CAST")
          return it.findChildStubByType(JSElementTypes.OBJECT_LITERAL_EXPRESSION)
            ?.psi as JSObjectLiteralExpression?
        }
      return call.arguments.getOrNull(0) as? JSObjectLiteralExpression
    }
  }
}

abstract class VuexNamedSymbolImpl(override val name: String,
                                   override val source: PsiElement) : VuexNamedSymbol

class VuexStatePropertyImpl(name: String, source: PsiElement)
  : VuexNamedSymbolImpl(name, source), VuexStateProperty {
  override fun getResolveTarget(namespace: String, qualifiedName: String): JSLocalImplicitElementImpl {
    return VuexStoreStateElement(qualifiedName.substring(namespace.length), qualifiedName, source, (source as? JSTypeOwner)?.jsType)
  }
}

class VuexActionImpl(name: String, source: PsiElement)
  : VuexNamedSymbolImpl(name, source), VuexAction {

  override val isRoot: Boolean get() = initializer?.let { IS_ROOT.build(it) } == true

  private val initializer: JSObjectLiteralExpression?
    get() {
      val initializerHolder = source
      return CachedValuesManager.getCachedValue(initializerHolder) {
        objectLiteralFor(initializerHolder)?.let { Result.create(it, initializerHolder, it) }
        ?: Result.create(null as JSObjectLiteralExpression?, initializerHolder, PsiModificationTracker.MODIFICATION_COUNT)
      }
    }

  companion object {
    private val IS_ROOT = BooleanValueAccessor("root")
  }
}

class VuexGetterImpl(name: String, source: PsiElement)
  : VuexNamedSymbolImpl(name, source), VuexGetter {

  private val jsType: JSType?
    get() = (source as? JSTypeOwner)?.let { VuexGetterType(JSTypeSourceFactory.createTypeSource(source, false), it) }

  override fun getResolveTarget(namespace: String, qualifiedName: String): JSLocalImplicitElementImpl =
    VueImplicitElement(qualifiedName.substring(namespace.length), jsType, source, JSImplicitElement.Type.Property, true)

}

class VuexMutationImpl(name: String, source: PsiElement)
  : VuexNamedSymbolImpl(name, source), VuexMutation
