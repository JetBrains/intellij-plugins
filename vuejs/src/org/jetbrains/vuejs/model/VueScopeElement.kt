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

  fun visitScope(visitor: VueModelVisitor, minimumProximity: VueModelVisitor.Proximity = VueModelVisitor.Proximity.GLOBAL) {
    val visited = mutableSetOf<Pair<String, VueScopeElement>>()
    val containersQueue = mutableListOf<Pair<VueEntitiesContainer, VueModelVisitor.Proximity>>()

    val visitContainer = { container: VueEntitiesContainer, proximity: VueModelVisitor.Proximity, containerVisitor: VueModelVisitor ->
      container.components.forEach { (name, component) ->
        if (visited.add(Pair(name, component))) containerVisitor.visitComponent(name, component, proximity)
      }
      container.directives.forEach { (name, directive) ->
        if (visited.add(Pair(name, directive))) containerVisitor.visitDirective(name, directive, proximity)
      }
      container.filters.forEach { (name, filter) ->
        if (visited.add(Pair(name, filter))) containerVisitor.visitFilter(name, filter, proximity)
      }
      container.mixins.forEach { mixin ->
        if (visited.add(Pair("", mixin))) {
          containersQueue.add(Pair(mixin, proximity))
          containerVisitor.visitMixin(mixin, proximity)
        }
      }
    }

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
    }

    containersQueue.sortBy { it.second }

    while (containersQueue.isNotEmpty()) {
      val (first, second) = containersQueue.removeAt(containersQueue.size - 1)
      visitContainer(first, second, visitor)
    }
  }

}
