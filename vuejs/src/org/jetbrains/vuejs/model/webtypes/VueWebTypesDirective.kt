// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.vuejs.model.webtypes

import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import org.jetbrains.vuejs.model.VueDirective
import org.jetbrains.vuejs.model.VueEntitiesContainer
import org.jetbrains.vuejs.model.webtypes.json.Attribute_

class VueWebTypesDirective(attribute: Attribute_,
                           parent: VueEntitiesContainer,
                           private val pathResolver: (String) -> PsiFile?) : VueDirective {
  override val source: PsiElement? get() = sourceFile?.let { pathResolver(it) }
  override val parents: List<VueEntitiesContainer> = listOf(parent)
  override val defaultName: String? = attribute.name

  private val sourceFile: String? = attribute.sourceFile
}
