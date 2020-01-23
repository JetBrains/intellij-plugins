// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.vuejs.libraries.vuex.model.store

import com.intellij.lang.javascript.psi.JSCallExpression
import com.intellij.lang.javascript.psi.JSImplicitElementProvider
import com.intellij.lang.javascript.psi.JSNewExpression
import com.intellij.openapi.project.Project
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.stubs.StubIndex
import com.intellij.psi.util.CachedValueProvider
import com.intellij.psi.util.CachedValuesManager
import com.intellij.psi.util.PsiModificationTracker
import org.jetbrains.vuejs.codeInsight.getTextIfLiteral
import org.jetbrains.vuejs.codeInsight.objectLiteralFor
import org.jetbrains.vuejs.libraries.vuex.VuexUtils.REGISTER_MODULE
import org.jetbrains.vuejs.libraries.vuex.VuexUtils.STORE
import org.jetbrains.vuejs.libraries.vuex.index.VuexStoreIndex

object VuexModelManager {

  //fun getVuexContainer(element: PsiElement): VuexContainer? {
  //
  //}
  //
  //fun getVuexStore(vueContainer: VueContainer): VuexStore? {
  //
  //}

  fun getAllVuexStores(project: Project): List<VuexStore> {
    return CachedValuesManager.getManager(project).getCachedValue(project) {
      CachedValueProvider.Result.create(
        StubIndex.getElements(VuexStoreIndex.KEY, STORE, project,
                              GlobalSearchScope.projectScope(project),
                              JSImplicitElementProvider::class.java)
          .asSequence()
          .filterIsInstance<JSNewExpression>()
          .filter { call -> call.indexingData?.implicitElements?.find { it.userString == VuexStoreIndex.JS_KEY } != null }
          .map { VuexStoreImpl(it) }
          .toList(), PsiModificationTracker.MODIFICATION_COUNT)
    }
  }

  fun getRegisteredModules(project: Project): List<VuexModule> {
    return CachedValuesManager.getManager(project).getCachedValue(project) {
      CachedValueProvider.Result.create(
        StubIndex.getElements(VuexStoreIndex.KEY, REGISTER_MODULE, project,
                              GlobalSearchScope.projectScope(project),
                              JSImplicitElementProvider::class.java)
          .asSequence()
          .filterIsInstance<JSCallExpression>()
          .filter { call -> call.indexingData?.implicitElements?.find { it.userString == VuexStoreIndex.JS_KEY } != null }
          .mapNotNull { createRegisteredModule(it) }
          .toList(), PsiModificationTracker.MODIFICATION_COUNT)
    }
  }

  private fun createRegisteredModule(call: JSCallExpression): VuexModule? {
    // TODO make stub safe
    val arguments = call.arguments
    val path = arguments.getOrNull(0)
                 ?.let { getTextIfLiteral(it) }
               ?: return null

    val initializer = arguments.getOrNull(1)
                        ?.let { objectLiteralFor(it) }
                      ?: return null
    return VuexModuleImpl(path, initializer)
  }

}
