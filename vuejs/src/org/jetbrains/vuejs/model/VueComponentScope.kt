// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.vuejs.model

class VueComponentScope(private val component: VueRegularComponent) {

  fun isInScope(toCheck: VueComponent, name: String): Boolean {
    return component.components[name]?.equals(toCheck) ?: false
           || component.applications.all { it.components[name]?.equals(toCheck) ?: false }
           || component.global?.components?.get(name)?.equals(toCheck) ?: false
  }

}
