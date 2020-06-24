// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.vuejs.codeInsight.template

import com.intellij.psi.PsiElement
import com.intellij.psi.PsiElementResolveResult
import com.intellij.psi.ResolveResult
import org.jetbrains.vuejs.model.VueEntitiesContainer
import org.jetbrains.vuejs.model.VueModelManager
import java.util.function.Consumer

class VueContainerScopeProvider : VueTemplateScopesProvider() {

  override fun getScopes(element: PsiElement, hostElement: PsiElement?): List<VueTemplateScope> {
    return VueModelManager.findEnclosingContainer(hostElement ?: element)
             ?.let { listOf(VueContainerScope(it)) }
           ?: emptyList()
  }

  private class VueContainerScope constructor(private val myEntitiesContainer: VueEntitiesContainer)
    : VueTemplateScope(null) {

    override fun resolve(consumer: Consumer<in ResolveResult>) {
      myEntitiesContainer.thisType
        .asRecordType()
        .properties
        .asSequence()
        .mapNotNull { it.memberSource.singleElement }
        .map { PsiElementResolveResult(it, true) }
        .forEach { consumer.accept(it) }
    }
  }
}
