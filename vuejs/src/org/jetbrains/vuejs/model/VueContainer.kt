// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.vuejs.model

import com.intellij.psi.PsiElement

interface VueContainer : VueEntitiesContainer {
  val data: List<VueDataProperty>
  val computed: List<VueComputedProperty>
  val methods: List<VueMethod>
  val props: List<VueInputProperty>
  val emits: List<VueEmitCall>
  val slots: List<VueSlot>

  val template: PsiElement?
  val element: String?
  val extends: List<VueContainer>

  fun acceptSelfScope(visitor: VueModelVisitor, onlyPublic: Boolean = true) {
    acceptEntitiesScope(object : VueModelVisitor() {
      override fun visitSelfComponent(component: VueComponent, proximity: Proximity): Boolean {
        return visitor.visitSelfComponent(component, proximity)
               && if (component is VueContainer) visitContainer(component) else true
      }

      override fun visitMixin(mixin: VueMixin, proximity: Proximity): Boolean {
        return visitor.visitMixin(mixin, proximity) && visitContainer(mixin)
      }

      fun visitContainer(container: VueContainer): Boolean {
        return container.props.all { visitor.visitInputProperty(it) }
               && (onlyPublic
                   || (container.data.all { visitor.visitDataProperty(it) }
                       && container.computed.all { visitor.visitComputedProperty(it) }
                       && container.methods.all { visitor.visitMethod(it) }
                      ))
      }
    }, VueModelVisitor.Proximity.GLOBAL)
  }
}

interface VueSlot

interface VueEmitCall

interface VueInputProperty {
  val name: String
  val source: PsiElement?
}

interface VueDataProperty {
  val name: String
  val source: PsiElement?
}

interface VueComputedProperty {
  val name: String
  val source: PsiElement?
}

interface VueMethod {
  val name: String
  val source: PsiElement?
}
