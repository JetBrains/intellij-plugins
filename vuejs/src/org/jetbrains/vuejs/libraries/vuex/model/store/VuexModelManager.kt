// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.vuejs.libraries.vuex.model.store

import com.intellij.lang.javascript.psi.JSImplicitElementProvider
import com.intellij.lang.javascript.psi.JSNewExpression
import com.intellij.openapi.project.Project
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.stubs.StubIndex
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
    return StubIndex.getElements(VuexStoreIndex.KEY, STORE, project,
                                 GlobalSearchScope.projectScope(project),
                                 JSImplicitElementProvider::class.java)
      .asSequence()
      .filterIsInstance<JSNewExpression>()
      .filter { call -> call.indexingData?.implicitElements?.find { it.userString == VuexStoreIndex.JS_KEY } != null }
      .map { VuexStoreImpl(it) }
      .toList()
  }
}
