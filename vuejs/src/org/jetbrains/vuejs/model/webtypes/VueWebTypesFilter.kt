// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.vuejs.model.webtypes

import org.jetbrains.vuejs.model.VueEntitiesContainer
import org.jetbrains.vuejs.model.VueGlobal
import org.jetbrains.vuejs.model.webtypes.json.HtmlVueFilter

internal class VueWebTypesFilter(filter: HtmlVueFilter,
                                 context: VueWebTypesEntitiesContainer.WebTypesContext)
  : VueWebTypesSourceEntity(filter, context), org.jetbrains.vuejs.model.VueFilter {

  override val global: VueGlobal? get() = context.parent.global
  override val parents: List<VueEntitiesContainer> get() = listOf(context.parent)

  override val defaultName: String = filter.name!!

}
