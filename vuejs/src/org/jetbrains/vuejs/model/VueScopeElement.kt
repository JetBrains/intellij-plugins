// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.vuejs.model

import com.intellij.psi.PsiElement
import org.jetbrains.vuejs.codeInsight.fromAsset

interface VueScopeElement {

  val source: PsiElement?

  val parents: List<VueEntitiesContainer>

  val global: VueGlobal?
    get() {
      return source?.let { VueModelManager.getGlobal(it) }
    }

  fun acceptEntities(visitor: VueModelVisitor,
                     minimumProximity: VueModelVisitor.Proximity = VueModelVisitor.Proximity.GLOBAL): Boolean {
    val visited = mutableSetOf<Pair<String, VueScopeElement>>()
    val containersStack = mutableListOf<Pair<VueEntitiesContainer, VueModelVisitor.Proximity>>()

    if (minimumProximity <= VueModelVisitor.Proximity.GLOBAL) {
      global?.let {
        containersStack.add(Pair(it, VueModelVisitor.Proximity.GLOBAL))
        it.plugins.forEach { plugin -> containersStack.add(Pair(plugin, plugin.defaultProximity)) }
        if (minimumProximity <= VueModelVisitor.Proximity.OUT_OF_SCOPE) {
          containersStack.add(Pair(it.unregistered, VueModelVisitor.Proximity.OUT_OF_SCOPE))
        }
      }
    }

    if (minimumProximity <= VueModelVisitor.Proximity.PLUGIN) {
      parents.forEach { parent ->
        when (parent) {
          is VueApp -> if (minimumProximity <= VueModelVisitor.Proximity.APP)
            containersStack.add(Pair(parent, VueModelVisitor.Proximity.APP))
          is VuePlugin -> if (minimumProximity <= VueModelVisitor.Proximity.PLUGIN)
            containersStack.add(Pair(parent, VueModelVisitor.Proximity.PLUGIN))
        }
      }
    }

    if (this is VueEntitiesContainer) {
      containersStack.add(Pair(this, VueModelVisitor.Proximity.LOCAL))
    }

    containersStack.sortBy { it.second }

    while (containersStack.isNotEmpty()) {
      val (container, proximity) = containersStack.removeAt(containersStack.size - 1)

      if (!visited.add(Pair("", container))) continue

      if ((container is VueMixin && !visitor.visitMixin(container, proximity)) ||
          (container is VueComponent && !visitor.visitSelfComponent(container, proximity)) ||
          (container is VueApp && proximity == VueModelVisitor.Proximity.LOCAL && !visitor.visitSelfApplication(container, proximity))) {
        return false
      }

      ((container as? VueContainer)?.extends)?.forEach {
        containersStack.add(Pair(it, proximity))
      }
      container.mixins.forEach { mixin ->
        containersStack.add(Pair(mixin, proximity))
      }
      container.components.forEach { (name, component) ->
        if (visited.add(Pair(fromAsset(name), component))
            && !visitor.visitComponent(name, component, proximity)) {
          return false
        }
      }
      container.directives.forEach { (name, directive) ->
        if (visited.add(Pair(fromAsset(name), directive))
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
    }
    return true
  }

  fun acceptPropertiesAndMethods(visitor: VueModelVisitor, onlyPublic: Boolean = true) {
    acceptEntities(object : VueModelVisitor() {
      override fun visitSelfComponent(component: VueComponent, proximity: Proximity): Boolean {
        return if (component is VueContainer) visitContainer(component, proximity) else true
      }

      override fun visitSelfApplication(application: VueApp, proximity: Proximity): Boolean {
        return visitContainer(application, proximity)
      }

      override fun visitMixin(mixin: VueMixin, proximity: Proximity): Boolean {
        return visitContainer(mixin, proximity)
      }

      fun visitContainer(container: VueContainer, proximity: Proximity): Boolean {
        return container.props.all { visitor.visitInputProperty(it, proximity) }
               && (onlyPublic
                   || (container.data.all { visitor.visitDataProperty(it, proximity) }
                       && container.computed.all { visitor.visitComputedProperty(it, proximity) }
                       && container.methods.all { visitor.visitMethod(it, proximity) }
                      ))
      }
    }, VueModelVisitor.Proximity.GLOBAL)
  }

  fun collectModelDirectiveProperties(): VueModelDirectiveProperties {
    var prop: String? = null
    var event: String? = null
    acceptEntities(object : VueModelVisitor() {
      override fun visitSelfComponent(component: VueComponent, proximity: Proximity): Boolean {
        return if (component is VueContainer) visitContainer(component) else true
      }

      override fun visitSelfApplication(application: VueApp, proximity: Proximity): Boolean {
        return visitContainer(application)
      }

      override fun visitMixin(mixin: VueMixin, proximity: Proximity): Boolean {
        return visitContainer(mixin)
      }

      fun visitContainer(container: VueContainer): Boolean {
        container.model?.let {
          prop = prop ?: it.prop
          event = event ?: it.event
        }
        return prop == null && event == null
      }
    }, VueModelVisitor.Proximity.GLOBAL)
    return VueModelDirectiveProperties(prop, event)
  }

}
