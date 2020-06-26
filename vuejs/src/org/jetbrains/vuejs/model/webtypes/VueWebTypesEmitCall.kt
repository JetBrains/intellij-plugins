// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.vuejs.model.webtypes

import org.jetbrains.vuejs.model.VueEmitCall
import org.jetbrains.vuejs.model.webtypes.json.HtmlTagEvent

internal class VueWebTypesEmitCall(event: HtmlTagEvent, context: VueWebTypesEntitiesContainer.WebTypesContext)
  : VueWebTypesDocumentedItem(event, context), VueEmitCall {

  override val name: String = event.name!!
}
