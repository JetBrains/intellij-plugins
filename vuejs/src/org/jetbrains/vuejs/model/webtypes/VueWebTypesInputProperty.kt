// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.vuejs.model.webtypes

import com.intellij.lang.javascript.psi.JSType
import com.intellij.psi.PsiElement
import org.jetbrains.vuejs.model.VueInputProperty
import org.jetbrains.vuejs.model.webtypes.json.HtmlTagAttribute

internal class VueWebTypesInputProperty(attribute: HtmlTagAttribute, context: VueWebTypesEntitiesContainer.WebTypesContext)
  : VueWebTypesDocumentedItem(attribute, context), VueInputProperty {

  override val name: String = attribute.name!!
  override val source: PsiElement? = null
  override val required: Boolean = attribute.required ?: false
  override val jsType: JSType? = ((attribute.value as? Map<*, *>)?.get("type") ?: attribute.type)
    ?.let { context.getType(it) }
  override val defaultValue: String? = attribute.default
}
