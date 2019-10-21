// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.vuejs.model.webtypes

import org.jetbrains.vuejs.codeInsight.documentation.VueDocumentedItem
import org.jetbrains.vuejs.codeInsight.documentation.VueItemDocumentation
import org.jetbrains.vuejs.model.webtypes.json.DocumentedItem

internal open class VueWebTypesDocumentedItem(item: DocumentedItem,
                                              protected val context: VueWebTypesEntitiesContainer.WebTypesContext) : VueDocumentedItem {

  @Suppress("LeakingThis")
  override val documentation: VueItemDocumentation = VueWebTypesItemDocumentation(item, this, context)
}
