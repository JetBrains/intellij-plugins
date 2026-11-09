// Copyright 2000-2026 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.vuejs.lang.typescript.kolar.vue.compiler.core

// https://github.com/vuejs/core/blob/main/packages/compiler-core/src/utils.ts
fun isCoreComponent(
  tag: String,
): Boolean =
  when (tag) {
    "Teleport", "teleport",
    "Suspense", "suspense",
    "KeepAlive", "keep-alive",
    "BaseTransition", "base-transition",
      -> true

    else -> false
  }

fun isComponentTag(
  tag: String,
): Boolean =
  tag == "component" || tag == "Component"