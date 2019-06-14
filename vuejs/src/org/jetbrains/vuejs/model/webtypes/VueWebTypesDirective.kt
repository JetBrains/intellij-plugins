// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.vuejs.model.webtypes

import com.intellij.psi.PsiElement
import org.jetbrains.vuejs.model.VueDirective
import org.jetbrains.vuejs.model.VueEntitiesContainer
import org.jetbrains.vuejs.model.webtypes.json.Attribute_

class VueWebTypesDirective(it: Attribute_, vueWebTypesPlugin: VueWebTypesPlugin) : VueDirective {
  override val source: PsiElement? = null
  override val parents: List<VueEntitiesContainer> = listOf(vueWebTypesPlugin)
  override val defaultName: String? = it.name
}
