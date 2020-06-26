// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.vuejs.model.webtypes

import org.jetbrains.vuejs.model.VueDirectiveArgument
import org.jetbrains.vuejs.model.webtypes.json.HtmlAttributeVueArgument

internal class VueWebTypesDirectiveArgument(argument: HtmlAttributeVueArgument,
                                            context: VueWebTypesEntitiesContainer.WebTypesContext)
  : VueWebTypesDocumentedItem(argument, context), VueDirectiveArgument {

  override val required: Boolean = argument.required == true
  override val pattern: Regex? = context.createPattern(argument.pattern)
}
