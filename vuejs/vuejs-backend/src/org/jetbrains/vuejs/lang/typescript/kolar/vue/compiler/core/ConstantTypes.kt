// Copyright 2000-2026 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.vuejs.lang.typescript.kolar.vue.compiler.core

// CompilerDOM.ConstantTypes
enum class ConstantTypes {
  NOT_CONSTANT,   // 0
  CAN_SKIP_PATCH, // 1
  CAN_HOIST,      // 2
  CAN_STRINGIFY,  // 3

  ;
}
