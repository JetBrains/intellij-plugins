// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.vuejs.model

abstract class VueModelProximityVisitor : VueModelVisitor() {

  var closest: Proximity? = null

  fun visitSameProximity(proximity: Proximity, call: () -> Boolean): Boolean {
    if (closest != null && closest!! > proximity) {
      return false
    }
    if (!call()) {
      closest = proximity
    }
    return true
  }

}
