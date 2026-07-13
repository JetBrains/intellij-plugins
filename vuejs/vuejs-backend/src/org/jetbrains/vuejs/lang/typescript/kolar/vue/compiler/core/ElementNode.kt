// Copyright 2000-2026 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.vuejs.lang.typescript.kolar.vue.compiler.core

import com.intellij.psi.xml.XmlTag

// CompilerDOM.ElementNode (minimal — full definition to be added when element.ts is converted)
interface ElementNode : Node {
  val tag: String
  val tagType: ElementTypes
  val isSelfClosing: Boolean
  val children: List<Node>
  val props: List<Node>
}

class ElementNodeImpl(
  private val element: XmlTag,
): ElementNode {
  override val loc: SourceLocation
    get() = PsiSourceLocation(element)
  
  override val tag: String
    get() = element.localName

  override val tagType: ElementTypes
    get() = TODO("not implemented")

  override val isSelfClosing: Boolean
    get() = TODO("not implemented")

  override val children: List<Node>
    get() = TODO("not implemented")

  override val props: List<Node>
    get() = TODO("not implemented")
}
