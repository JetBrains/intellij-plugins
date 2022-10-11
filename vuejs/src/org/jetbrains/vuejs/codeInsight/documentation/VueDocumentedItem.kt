// Copyright 2000-2022 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.vuejs.codeInsight.documentation

import org.jetbrains.vuejs.model.VueSourceElement

interface VueDocumentedItem : VueSourceElement {
  @Suppress("DEPRECATION")
  val description: String? get() = null

  val documentation: VueItemDocumentation
    get() {
      return object : VueItemDocumentation {
        override val defaultName: String? = VueItemDocumentation.nameOf(this@VueDocumentedItem)
        override val type: String = VueItemDocumentation.typeOf(this@VueDocumentedItem)
        override val library: String? = null
        override val description: String? = this@VueDocumentedItem.description
        override val docUrl: String? = null
      }
    }
}
