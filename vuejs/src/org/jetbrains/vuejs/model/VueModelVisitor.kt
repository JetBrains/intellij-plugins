// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.vuejs.model

abstract class VueModelVisitor {

  open fun visitComponent(name: String, component: VueComponent, proximity: Proximity): Boolean {
    return true
  }

  open fun visitSelfComponent(component: VueComponent, proximity: Proximity): Boolean {
    return component.defaultName?.let { visitComponent(it, component, proximity) } ?: true
  }

  open fun visitMixin(mixin: VueMixin, proximity: Proximity): Boolean {
    return true
  }

  open fun visitFilter(name: String, filter: VueFilter, proximity: Proximity): Boolean {
    return true
  }

  open fun visitDirective(name: String, directive: VueDirective, proximity: Proximity): Boolean {
    return true
  }

  open fun visitInputProperty(prop: VueInputProperty): Boolean {
    return true
  }

  open fun visitComputedProperty(computedProperty: VueComputedProperty): Boolean {
    return true
  }

  open fun visitDataProperty(dataProperty: VueDataProperty): Boolean {
    return true
  }

  open fun visitMethod(method: VueMethod): Boolean {
    return true
  }

  enum class Proximity {
    OUT_OF_SCOPE,
    GLOBAL,
    APP,
    PLUGIN,
    LOCAL
  }

}
