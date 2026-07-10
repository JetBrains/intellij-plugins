// Copyright 2000-2026 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.vuejs.lang.typescript.kolar.vue.compiler.core

// @vue/compiler-core: const enum NodeTypes
enum class NodeTypes(
  val value: Int,
) {
  ROOT(0),
  ELEMENT(1),
  COMMENT(3),
  SIMPLE_EXPRESSION(4),
  INTERPOLATION(5),
  COMPOUND_EXPRESSION(8),
  IF(9),
  FOR(11),

  ;
}
