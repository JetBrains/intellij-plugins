// Copyright 2000-2026 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.vuejs.lang.typescript.kolar.vue.compiler.dom

import org.jetbrains.vuejs.lang.typescript.kolar.vue.shared.isHTMLTag
import org.jetbrains.vuejs.lang.typescript.kolar.vue.shared.isMathMLTag
import org.jetbrains.vuejs.lang.typescript.kolar.vue.shared.isSVGTag

fun isNativeTag(
  tag: String,
): Boolean =
  isHTMLTag(tag)
  || isSVGTag(tag)
  || isMathMLTag(tag)
