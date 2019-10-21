// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.vuejs.model.webtypes

import com.intellij.openapi.util.UserDataHolderBase
import com.intellij.psi.PsiElement
import com.intellij.psi.util.CachedValuesManager
import org.jetbrains.vuejs.codeInsight.documentation.VueDocumentedItem
import org.jetbrains.vuejs.codeInsight.documentation.VueItemDocumentation
import org.jetbrains.vuejs.model.webtypes.json.Source
import org.jetbrains.vuejs.model.webtypes.json.SourceEntity

internal open class VueWebTypesSourceEntity(sourceEntity: SourceEntity,
                                            protected val context: VueWebTypesEntitiesContainer.WebTypesContext)
  : VueDocumentedItem, UserDataHolderBase() {

  @Suppress("LeakingThis")
  override val documentation: VueItemDocumentation = VueWebTypesItemDocumentation(sourceEntity, this, context)

  private val sourceInfo: Source? = sourceEntity.source
  val source: PsiElement?
    get() {
      return sourceInfo?.let {
        CachedValuesManager.getManager(context.project).getCachedValue(this) {
          context.resolveSourceSymbol(it)
        }
      }
    }
}
