// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.vuejs.model

interface VueApp : VueContainer {

  val mode: VueMode
    get() = VueMode.CLASSIC

  val rootComponent: VueComponent? get() = null

  fun getProximity(library: VueLibrary): VueModelVisitor.Proximity

}
