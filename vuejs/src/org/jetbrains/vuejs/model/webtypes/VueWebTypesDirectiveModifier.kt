// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.vuejs.model.webtypes

import org.jetbrains.vuejs.model.VueDirectiveModifier
import org.jetbrains.vuejs.model.webtypes.json.HtmlAttributeVueModifier

internal class VueWebTypesDirectiveModifier(modifier: HtmlAttributeVueModifier,
                                            context: VueWebTypesEntitiesContainer.WebTypesContext)
  : VueWebTypesDocumentedItem(modifier, context), VueDirectiveModifier {

  override val name: String = modifier.name!!
  override val pattern: Regex? = context.createPattern(modifier.pattern)
}
