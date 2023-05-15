// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.vuejs.libraries.vuex.model.store

import com.intellij.lang.javascript.psi.JSCallExpression
import com.intellij.lang.javascript.psi.JSImplicitElementProvider
import com.intellij.lang.javascript.psi.JSObjectLiteralExpression
import com.intellij.lang.javascript.psi.stubs.JSImplicitElement
import com.intellij.lang.javascript.psi.util.JSStubBasedPsiTreeUtil
import com.intellij.lang.javascript.psi.util.stubSafeCallArguments
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.stubs.StubIndex
import com.intellij.psi.util.CachedValueProvider
import com.intellij.psi.util.CachedValuesManager
import com.intellij.psi.util.PsiModificationTracker
import com.intellij.util.asSafely
import org.jetbrains.vuejs.codeInsight.getTextIfLiteral
import org.jetbrains.vuejs.libraries.nuxt.model.NuxtModelManager
import org.jetbrains.vuejs.libraries.vuex.VuexUtils.REGISTER_MODULE
import org.jetbrains.vuejs.libraries.vuex.VuexUtils.STORE
import org.jetbrains.vuejs.libraries.vuex.VuexUtils.isVuexContext
import org.jetbrains.vuejs.libraries.vuex.index.VuexStoreIndex

object VuexModelManager {

  fun getVuexStoreContext(element: PsiElement): VuexStoreContext? {
    if (!isVuexContext(element)) return null
    var stores = getAllVuexStores(element.project)
    // Introduce extension point if another provider would need to be added
    NuxtModelManager.getApplication(element)?.vuexStore?.let {
      stores = stores + it
    }
    val registeredModules = getRegisteredModules(element.project)
    return if (stores.isNotEmpty() || registeredModules.isNotEmpty())
      VuexStoreContextImpl(stores, registeredModules, element)
    else
      null
  }

  private fun getAllVuexStores(project: Project): List<VuexStore> {
    return CachedValuesManager.getManager(project).getCachedValue(project) {
      val elements = StubIndex.getElements(VuexStoreIndex.KEY, STORE, project,
                                           GlobalSearchScope.projectScope(project),
                                           JSImplicitElementProvider::class.java)
      val result = elements
        .asSequence()
        .filterIsInstance<JSCallExpression>()
        .filter { call -> call.indexingData?.implicitElements?.find { it.userString == VuexStoreIndex.JS_KEY } != null }
        .map { VuexStoreImpl(it) }
        .toList()

      CachedValueProvider.Result.create(result, PsiModificationTracker.MODIFICATION_COUNT)
    }
  }

  private fun getRegisteredModules(project: Project): List<VuexModule> {
    return CachedValuesManager.getManager(project).getCachedValue(project) {
      CachedValueProvider.Result.create(
        StubIndex.getElements(VuexStoreIndex.KEY, REGISTER_MODULE, project,
                              GlobalSearchScope.projectScope(project),
                              JSImplicitElementProvider::class.java)
          .asSequence()
          .filterIsInstance<JSCallExpression>()
          .mapNotNull { createRegisteredModule(it) }
          .toList(), PsiModificationTracker.MODIFICATION_COUNT)
    }
  }

  private fun createRegisteredModule(call: JSCallExpression): VuexModule? {
    val implicitElement = call.indexingData?.implicitElements?.find { it.userString == VuexStoreIndex.JS_KEY }
                          ?: return null

    val arguments = call.stubSafeCallArguments
    val nameElement = arguments.getOrNull(0)
                      ?: return null
    val path = getTextIfLiteral(nameElement)
               ?: return null

    val initializer = arguments.getOrNull(1)
                        ?.asSafely<JSObjectLiteralExpression>()
                      ?: resolveFromImplicitElement(implicitElement)
                      ?: return null
    return VuexModuleImpl(path, initializer, nameElement)
  }

  private fun resolveFromImplicitElement(implicitElement: JSImplicitElement): PsiElement? {
    return JSStubBasedPsiTreeUtil.resolveLocally(implicitElement.jsType?.typeText ?: return null,
                                                 implicitElement.context ?: return null)
  }

  private class VuexStoreContextImpl(override val rootStores: List<VuexStore>,
                                     override val registeredModules: List<VuexModule>,
                                     override val element: PsiElement) : VuexStoreContext

}
