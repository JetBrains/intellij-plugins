// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.vuejs.model.webtypes

import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiElement
import org.jetbrains.vuejs.model.*
import org.jetbrains.vuejs.model.webtypes.json.WebTypes

class VueWebTypesGlobal(override val project: Project, packageJson: VirtualFile,
                        webTypes: WebTypes, owner: VueEntitiesContainer)
  : VueWebTypesEntitiesContainer(project, packageJson, webTypes, owner), VueGlobal {

  override val global: VueGlobal = this
  override val apps: List<VueApp> = emptyList()
  override val plugins: List<VuePlugin> = emptyList()
  override val unregistered: VueEntitiesContainer = UNREGISTERED_INSTANCE

  companion object {
    private val UNREGISTERED_INSTANCE = object : VueEntitiesContainer {
      override val components: Map<String, VueComponent> = emptyMap()
      override val directives: Map<String, VueDirective> = emptyMap()
      override val filters: Map<String, VueFilter> = emptyMap()
      override val mixins: List<VueMixin> get() = emptyList()
      override val source: PsiElement? = null
      override val parents: List<VueEntitiesContainer> = emptyList()
    }
  }
}
