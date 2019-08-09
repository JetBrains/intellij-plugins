// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.vuejs.model.webtypes

import com.intellij.psi.PsiElement
import org.jetbrains.vuejs.model.VueDirective
import org.jetbrains.vuejs.model.VueEntitiesContainer
import org.jetbrains.vuejs.model.webtypes.json.Attribute_
import org.jetbrains.vuejs.model.webtypes.json.Source

class VueWebTypesDirective(attribute: Attribute_,
                           parent: VueEntitiesContainer,
                           private val sourceSymbolResolver: WebTypesSourceSymbolResolver) : VueDirective {
  override val source: PsiElement? get() = sourceInfo?.let { sourceSymbolResolver.resolve(it) }
  override val parents: List<VueEntitiesContainer> = listOf(parent)
  override val defaultName: String? = attribute.name

  private val sourceInfo: Source? = attribute.source
}
