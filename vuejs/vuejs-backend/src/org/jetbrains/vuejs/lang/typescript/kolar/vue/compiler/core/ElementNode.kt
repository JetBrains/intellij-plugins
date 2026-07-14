// Copyright 2000-2026 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.vuejs.lang.typescript.kolar.vue.compiler.core

// CompilerDOM.ElementNode (minimal — full definition to be added when element.ts is converted)
interface ElementNode : Node {
  val tag: String
  val tagType: ElementTypes
  val isSelfClosing: Boolean
  val children: List<Node>
  val props: List<Node>
}
