// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.vuejs.codeInsight.template

import com.intellij.psi.PsiElement
import com.intellij.psi.PsiElementResolveResult
import com.intellij.psi.ResolveResult
import org.jetbrains.vuejs.model.VueComponent
import org.jetbrains.vuejs.model.VueEntitiesContainer
import org.jetbrains.vuejs.model.VueModelManager
import org.jetbrains.vuejs.model.source.INSTANCE_PROPS_PROP
import org.jetbrains.vuejs.model.source.INSTANCE_SLOTS_PROP
import java.util.function.Consumer

/**
 * [Details](https://github.com/vuejs/core/releases/tag/v3.6.0-alpha.1#about-vapor-mode:~:text=Implicit%20instance%20properties%20like%20%24slots%20and%20%24props%20are%20not%20available%20in%20Vapor%20template%20expressions)
 */
private val VAPOR_EXCLUDED_PROPERTIES = setOf(
  INSTANCE_SLOTS_PROP,
  INSTANCE_PROPS_PROP,
)

class VueContainerScopeProvider : VueTemplateScopesProvider() {

  override fun getScopes(
    element: PsiElement,
    hostElement: PsiElement?,
  ): List<VueTemplateScope> {
    val container = VueModelManager.findEnclosingContainer(hostElement ?: element)
    return listOf(VueContainerScope(container))
  }

  private class VueContainerScope(
    private val container: VueEntitiesContainer,
  ) : VueTemplateScope(null) {

    override fun resolve(
      consumer: Consumer<in ResolveResult>,
    ) {
      val excludedProperties = if (container is VueComponent && container.vapor) {
        VAPOR_EXCLUDED_PROPERTIES
      }
      else emptySet()

      container.thisType
        .asRecordType()
        .properties
        .asSequence()
        .filter { it.memberName !in excludedProperties }
        .mapNotNull { it.memberSource.singleElement }
        .map { PsiElementResolveResult(it, true) }
        .forEach { consumer.accept(it) }
    }
  }
}
