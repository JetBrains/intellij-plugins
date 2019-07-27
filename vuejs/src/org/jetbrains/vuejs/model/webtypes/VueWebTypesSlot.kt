// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.vuejs.model.webtypes

import org.jetbrains.vuejs.model.VueSlot
import org.jetbrains.vuejs.model.webtypes.json.Slot

class VueWebTypesSlot(it: Slot) : VueSlot {

  override val name: String = it.name!!

}
