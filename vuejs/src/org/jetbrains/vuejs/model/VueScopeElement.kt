// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.vuejs.model

import com.intellij.psi.PsiElement

interface VueScopeElement {

  val source: PsiElement?

  val parents: List<VueEntitiesContainer>

  val global: VueGlobal?
    get() {
      return source?.let { VueModelManager.getGlobal(it) }
    }

  fun visitScope(visitor: VueModelVisitor, minimumProximity: VueModelVisitor.Proximity = VueModelVisitor.Proximity.GLOBAL): Boolean {
    val visited = mutableSetOf<Pair<String, VueScopeElement>>()
    val containersQueue = mutableListOf<Pair<VueEntitiesContainer, VueModelVisitor.Proximity>>()

    if (minimumProximity <= VueModelVisitor.Proximity.GLOBAL) {
      global?.let {
        containersQueue.add(Pair(it, VueModelVisitor.Proximity.GLOBAL))
        it.plugins.forEach { plugin -> containersQueue.add(Pair(plugin, VueModelVisitor.Proximity.GLOBAL)) }
        if (minimumProximity <= VueModelVisitor.Proximity.OUT_OF_SCOPE) {
          containersQueue.add(Pair(it.unregistered, VueModelVisitor.Proximity.OUT_OF_SCOPE))
        }
      }
    }

    parents.forEach { parent ->
      when (parent) {
        is VueApp -> if (minimumProximity <= VueModelVisitor.Proximity.APP)
          containersQueue.add(Pair(parent, VueModelVisitor.Proximity.APP))
        is VuePlugin -> if (minimumProximity <= VueModelVisitor.Proximity.PLUGIN)
          containersQueue.add(Pair(parent, VueModelVisitor.Proximity.PLUGIN))
      }
    }

    if (this is VueEntitiesContainer
        && (this is VueMixin || this is VueComponent)) {
      containersQueue.add(Pair(this, VueModelVisitor.Proximity.LOCAL))
      if ((this is VueMixin
           && visited.add(Pair("", this))
           && !visitor.visitMixin(this, VueModelVisitor.Proximity.LOCAL))
          || (this is VueComponent
              && this.defaultName != null
              && visited.add(Pair(this.defaultName!!, this))
              && !visitor.visitComponent(this.defaultName!!, this, VueModelVisitor.Proximity.LOCAL))) {
        return false
      }
    }

    containersQueue.sortBy { it.second }

    while (containersQueue.isNotEmpty()) {
      val (container, proximity) = containersQueue.removeAt(containersQueue.size - 1)
      container.components.forEach { (name, component) ->
        if (visited.add(Pair(name, component))
            && !visitor.visitComponent(name, component, proximity)) {
          return false
        }
      }
      container.directives.forEach { (name, directive) ->
        if (visited.add(Pair(name, directive))
            && !visitor.visitDirective(name, directive, proximity)) {
          return false
        }
      }
      container.filters.forEach { (name, filter) ->
        if (visited.add(Pair(name, filter))
            && !visitor.visitFilter(name, filter, proximity)) {
          return false
        }
      }
      container.mixins.forEach { mixin ->
        if (visited.add(Pair("", mixin))) {
          if (!visitor.visitMixin(mixin, proximity)) {
            return false
          }
          containersQueue.add(Pair(mixin, proximity))
        }
      }
    }
    return true
  }

}
