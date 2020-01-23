// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.vuejs.libraries.vuex.model.store

import com.intellij.extapi.psi.StubBasedPsiElementBase
import com.intellij.lang.javascript.JSElementTypes
import com.intellij.lang.javascript.psi.*
import com.intellij.lang.javascript.psi.stubs.JSObjectLiteralExpressionStub
import com.intellij.openapi.vfs.VirtualFileManager.VFS_STRUCTURE_MODIFICATIONS
import com.intellij.psi.PsiElement
import com.intellij.psi.util.CachedValueProvider.Result.create
import com.intellij.psi.util.CachedValuesManager
import org.jetbrains.vuejs.codeInsight.objectLiteralFor

abstract class VuexContainerImpl : VuexContainer {

  protected abstract val sourceElement: PsiElement

  protected abstract val initializer: JSObjectLiteralExpression?

  override val source: PsiElement
    get() = initializer ?: sourceElement

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

  private fun <T> get(accessor: (VuexContainerInfoProvider.VuexContainerInfo) -> Map<String, T>): Map<String, T> {
    return VuexContainerInfoProvider.INSTANCE.getInfo(initializer, null)?.let {
      accessor.invoke(it)
    } ?: mapOf()
  }

  protected fun get(accessor: (VuexContainerInfoProvider.VuexContainerInfo) -> Boolean): Boolean {
    return VuexContainerInfoProvider.INSTANCE.getInfo(initializer, null)?.let {
      accessor.invoke(it)
    } == true
  }

}

class VuexModuleImpl(override val name: String, override val sourceElement: PsiElement) : VuexContainerImpl(), VuexModule {

  override val isNamespaced: Boolean
    get() = get(VuexContainerInfoProvider.VuexContainerInfo::isNamespaced)

  override val initializer: JSObjectLiteralExpression?
    get() {
      (sourceElement as? JSObjectLiteralExpression)?.let { return it }
      val sourceElement = (this.sourceElement as? JSProperty) ?: return null
      return CachedValuesManager.getCachedValue(sourceElement) {
        objectLiteralFor(sourceElement)?.let { create(it, sourceElement, it) }
        ?: create(null as JSObjectLiteralExpression?, sourceElement, VFS_STRUCTURE_MODIFICATIONS)
      }
    }
}

class VuexStoreImpl(override val sourceElement: JSNewExpression) : VuexContainerImpl(), VuexStore {
  override val initializer: JSObjectLiteralExpression?
    get() {
      val storeCreationCall = this.sourceElement
      return CachedValuesManager.getCachedValue(storeCreationCall) {
        readLiteralFromParams(storeCreationCall)
          ?.let { create(it, storeCreationCall, it) }
        ?: create(null as JSObjectLiteralExpression?, storeCreationCall, VFS_STRUCTURE_MODIFICATIONS)
      }
    }

  companion object {
    fun readLiteralFromParams(call: JSCallExpression): JSObjectLiteralExpression? {
      (call as? StubBasedPsiElementBase<*>)
        ?.stub
        ?.let {
          @Suppress("USELESS_CAST")
          return it.findChildStubByType<JSObjectLiteralExpression, JSObjectLiteralExpressionStub>(JSElementTypes.OBJECT_LITERAL_EXPRESSION)
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
  override val jsType: JSType? = null
}

class VuexActionImpl(name: String, source: PsiElement)
  : VuexNamedSymbolImpl(name, source), VuexAction

class VuexGetterImpl(name: String, source: PsiElement)
  : VuexNamedSymbolImpl(name, source), VuexGetter {
  override val jsType: JSType? = null
}

class VuexMutationImpl(name: String, source: PsiElement)
  : VuexNamedSymbolImpl(name, source), VuexMutation
