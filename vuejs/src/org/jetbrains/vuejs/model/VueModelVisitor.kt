// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.vuejs.model

interface VueModelVisitor {

  fun visitComponent(name: String, component: VueComponent, proximity: Proximity)

  fun visitMixin(mixin: VueMixin, proximity: Proximity)

  fun visitFilter(name: String, filter: VueFilter, proximity: Proximity)

  fun visitDirective(name: String, directive: VueDirective, proximity: Proximity)

  enum class Proximity {
    OUT_OF_SCOPE,
    GLOBAL,
    APP,
    PLUGIN,
    LOCAL,
  }

}
