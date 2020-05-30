// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.vuejs.model.webtypes

import com.intellij.openapi.util.NullableLazyValue
import org.jetbrains.vuejs.codeInsight.documentation.VueDocumentedItem
import org.jetbrains.vuejs.codeInsight.documentation.VueItemDocumentation
import org.jetbrains.vuejs.model.webtypes.json.DocumentedItem

internal class VueWebTypesItemDocumentation(itemJson: DocumentedItem,
                                            private val itemModel: VueDocumentedItem,
                                            private val context: VueWebTypesEntitiesContainer.WebTypesContext) : VueItemDocumentation {
  override val defaultName: String? get() = VueItemDocumentation.nameOf(itemModel)
  override val type: String get() = VueItemDocumentation.typeOf(itemModel)

  private val lazyDescription: NullableLazyValue<String>

  override val description: String? get() = lazyDescription.value
  override val docUrl: String? = itemJson.docUrl
  override val library: String? get() = context.pluginName
  override val customSections: Map<String, String> get() = VueItemDocumentation.createSections(itemModel)

  init {
    val rawDescription = itemJson.description
    lazyDescription = NullableLazyValue.createValue {
      rawDescription?.let { this.context.renderDescription(rawDescription) }
    }
  }
}
