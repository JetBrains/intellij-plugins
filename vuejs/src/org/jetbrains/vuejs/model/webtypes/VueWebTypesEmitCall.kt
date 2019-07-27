// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.vuejs.model.webtypes

import org.jetbrains.vuejs.model.VueEmitCall
import org.jetbrains.vuejs.model.webtypes.json.Event

class VueWebTypesEmitCall(it: Event) : VueEmitCall {
  override val name: String = it.name!!
}
