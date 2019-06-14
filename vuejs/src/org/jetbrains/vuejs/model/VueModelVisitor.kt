// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.vuejs.model

import com.intellij.psi.PsiElement

interface VueModelVisitor {

  fun visitComponent(name: String, component: VueComponent, proximity: Proximity): Boolean {
    return true
  }

  fun visitMixin(mixin: VueMixin, proximity: Proximity): Boolean {
    return true
  }

  fun visitFilter(name: String, filter: VueFilter, proximity: Proximity): Boolean {
    return true
  }

  fun visitDirective(name: String, directive: VueDirective, proximity: Proximity): Boolean {
    return true
  }

  fun visitContextScope(context: PsiElement, minimumProximity: Proximity) {
    (VueModelManager.findComponent(context) ?: VueModelManager.getGlobal(context))
      ?.visitScope(this, minimumProximity)
  }

  enum class Proximity {
    OUT_OF_SCOPE,
    GLOBAL,
    APP,
    PLUGIN,
    LOCAL,
  }

}
