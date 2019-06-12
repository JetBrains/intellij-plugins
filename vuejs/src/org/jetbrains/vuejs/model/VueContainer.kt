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
  val extends: Any?
}

interface VueSlot {

}

interface VueEmitCall {

}

interface VueInputProperty {

}

interface VueDataProperty {

}

interface VueComputedProperty {

}

interface VueMethod {

}
