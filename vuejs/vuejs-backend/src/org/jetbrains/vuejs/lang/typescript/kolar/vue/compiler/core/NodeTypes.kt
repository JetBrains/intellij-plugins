// Copyright 2000-2026 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.vuejs.lang.typescript.kolar.vue.compiler.core

// @vue/compiler-core: const enum NodeTypes
object NodeTypes {
  const val ROOT = 0
  const val ELEMENT = 1
  const val TEXT = 2
  const val COMMENT = 3
  const val SIMPLE_EXPRESSION = 4
  const val INTERPOLATION = 5
  const val ATTRIBUTE = 6
  const val DIRECTIVE = 7
  const val COMPOUND_EXPRESSION = 8
  const val IF = 9
  const val IF_BRANCH = 10
  const val FOR = 11
  const val TEXT_CALL = 12
  const val VNODE_CALL = 13
  const val JS_CALL_EXPRESSION = 14
  const val JS_OBJECT_EXPRESSION = 15
  const val JS_PROPERTY = 16
  const val JS_ARRAY_EXPRESSION = 17
  const val JS_FUNCTION_EXPRESSION = 18
  const val JS_CONDITIONAL_EXPRESSION = 19
  const val JS_CACHE_EXPRESSION = 20
  const val JS_BLOCK_STATEMENT = 21
  const val JS_TEMPLATE_LITERAL = 22
  const val JS_IF_STATEMENT = 23
  const val JS_ASSIGNMENT_EXPRESSION = 24
  const val JS_SEQUENCE_EXPRESSION = 25
  const val JS_RETURN_STATEMENT = 26
}
