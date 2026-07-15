// Copyright 2000-2026 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.vuejs.lang.typescript.kolar.vue.compiler.core.impl

import com.intellij.lang.javascript.psi.util.stubSafeGetAttribute
import com.intellij.psi.util.siblings
import com.intellij.psi.xml.XmlTag
import org.jetbrains.vuejs.lang.typescript.kolar.vue.compiler.core.IfBranchNode
import org.jetbrains.vuejs.lang.typescript.kolar.vue.compiler.core.IfNode
import org.jetbrains.vuejs.lang.typescript.kolar.vue.compiler.core.SourceLocation

class IfNodeImpl(
  private val tag: XmlTag,
) : IfNode {
  override val loc: SourceLocation
    // TODO: include all branches?
    get() = PsiSourceLocation(tag)

  override val branches: List<IfBranchNode> by lazy {
    buildList {
      add(IfBranchNodeImpl(tag, V_IF))

      var current = tag

      current.siblings(withSelf = false)
        .filterIsInstance<XmlTag>()
        .takeWhile { it.stubSafeGetAttribute(V_ELSE_IF) != null }
        .onEach { current = it }
        .mapTo(this) { IfBranchNodeImpl(current, V_ELSE_IF) }

      current.siblings(withSelf = false)
        .filterIsInstance<XmlTag>()
        .take(1)
        .filter { it.stubSafeGetAttribute(V_ELSE) != null }
        .mapTo(this) { IfBranchNodeImpl(current, V_ELSE) }
    }
  }
}
