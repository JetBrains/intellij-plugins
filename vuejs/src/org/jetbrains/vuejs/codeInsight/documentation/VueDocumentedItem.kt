// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.vuejs.codeInsight.documentation

interface VueDocumentedItem {
  val documentation: VueItemDocumentation
    get() {
      return object : VueItemDocumentation {
        override val defaultName: String? = VueItemDocumentation.nameOf(this@VueDocumentedItem)
        override val type: String = VueItemDocumentation.typeOf(this@VueDocumentedItem)
        override val library: String? = null
        override val description: String? = null
        override val docUrl: String? = null
      }
    }
}
