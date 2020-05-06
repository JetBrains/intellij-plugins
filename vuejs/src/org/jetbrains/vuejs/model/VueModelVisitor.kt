// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.vuejs.model

abstract class VueModelVisitor {

  open fun visitComponent(name: String, component: VueComponent, proximity: Proximity): Boolean {
    return true
  }

  open fun visitSelfComponent(component: VueComponent, proximity: Proximity): Boolean {
    return component.defaultName?.let { visitComponent(it, component, proximity) } ?: true
  }

  open fun visitSelfApplication(application: VueApp, proximity: Proximity): Boolean {
    return true
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

  open fun visitProperty(property: VueProperty, proximity: Proximity): Boolean {
    return true
  }

  open fun visitInputProperty(prop: VueInputProperty, proximity: Proximity): Boolean {
    return visitProperty(prop, proximity)
  }

  open fun visitComputedProperty(computedProperty: VueComputedProperty, proximity: Proximity): Boolean {
    return visitProperty(computedProperty, proximity)
  }

  open fun visitDataProperty(dataProperty: VueDataProperty, proximity: Proximity): Boolean {
    return visitProperty(dataProperty, proximity)
  }

  open fun visitMethod(method: VueMethod, proximity: Proximity): Boolean {
    return visitProperty(method, proximity)
  }

  enum class Proximity {
    OUT_OF_SCOPE,
    GLOBAL,
    APP,
    PLUGIN,
    LOCAL
  }

}
