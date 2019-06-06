// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.vuejs.model.webtypes

import org.jetbrains.vuejs.model.*
import org.jetbrains.vuejs.model.webtypes.json.WebTypes
import java.util.*

class VueWebTypesPlugin(webTypes: WebTypes, override val nodeModule: String?) : VuePlugin {
  override val components: Map<String, VueComponent>

  override val directives: Map<String, VueDirective>
    get() = Collections.emptyMap()
  override val filters: Map<String, VueFilter>
    get() = Collections.emptyMap()
  override val mixins: List<VueMixin>
    get() = Collections.emptyList()

  init {
    assert(webTypes.framework == WebTypes.Framework.VUE)
    components = webTypes.contributions
                   ?.html
                   ?.tags
                   ?.filter { it.name != null }
                   ?.associateBy({ it.name!! }, { VueWebTypesComponent(it) })
                 ?: Collections.emptyMap()
  }

}
