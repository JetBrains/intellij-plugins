// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.vuejs.model

abstract class VueModelProximityVisitor : VueModelVisitor() {

  private var closest: Proximity? = null

  fun acceptSameProximity(proximity: Proximity, condition: Boolean, ifTrue: () -> Unit): Boolean {
    if (closest != null && closest!! > proximity) {
      return false
    }
    if (condition) {
      closest = proximity
      ifTrue()
    }
    return true
  }

}
