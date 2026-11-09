// Copyright 2000-2026 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.vuejs.lang.typescript.kolar.vue.compiler.core.impl

import com.intellij.openapi.project.Project
import com.intellij.psi.xml.XmlTag
import org.jetbrains.vuejs.lang.typescript.kolar.vue.compiler.core.AttributeNode
import org.jetbrains.vuejs.lang.typescript.kolar.vue.compiler.core.DirectiveNode
import org.jetbrains.vuejs.lang.typescript.kolar.vue.compiler.core.ElementNode
import org.jetbrains.vuejs.lang.typescript.kolar.vue.compiler.core.ElementTypes
import org.jetbrains.vuejs.lang.typescript.kolar.vue.compiler.core.ForNode
import org.jetbrains.vuejs.lang.typescript.kolar.vue.compiler.core.IfNode
import org.jetbrains.vuejs.lang.typescript.kolar.vue.compiler.core.Node
import org.jetbrains.vuejs.lang.typescript.kolar.vue.compiler.core.RootNode
import org.jetbrains.vuejs.lang.typescript.kolar.vue.compiler.core.SimpleExpressionNode
import org.jetbrains.vuejs.lang.typescript.kolar.vue.compiler.core.isComponentTag
import org.jetbrains.vuejs.lang.typescript.kolar.vue.compiler.core.isCoreComponent
import org.jetbrains.vuejs.lang.typescript.kolar.vue.compiler.dom.isBuiltInComponent

class RootNodeImpl(
  private val template: XmlTag,
) : NodeImpl(template),
    RootNode {
  override val children: List<Node> by children(template, ParentScope.ELEMENT)

  // `context.components` of `@vue/compiler-core`: `transformElement` resolves a component on node exit,
  // so the tags are collected in post-order.
  override val components: List<String> by lazy {
    val components = linkedSetOf<String>()
    collectComponents(children, components)
    components.toList()
  }

  // for file generation
  internal val project: Project
    get() = template.project
}

private fun collectComponents(
  nodes: List<Node>,
  components: MutableSet<String>,
) {
  for (node in nodes) {
    when (node) {
      is ElementNode -> {
        collectComponents(node.children, components)
        if (node.tagType == ElementTypes.COMPONENT) {
          resolveComponentName(node)?.let { components.add(it) }
        }
      }
      is IfNode -> {
        for (branch in node.branches) {
          collectComponents(branch.children, components)
        }
      }
      is ForNode -> collectComponents(node.children, components)
    }
  }
}

// https://github.com/vuejs/core/blob/main/packages/compiler-core/src/transforms/transformElement.ts (`resolveComponentType`)
private fun resolveComponentName(
  node: ElementNode,
): String? {
  val tag = node.tag
  // 1. dynamic component
  if (isComponentTag(tag) && node.props.any { isIsProp(it) }) {
    return null
  }
  // 2. built-in components
  if (isCoreComponent(tag) || isBuiltInComponent(tag)) {
    return null
  }
  // 5. user component (resolve)
  return tag
}

private fun isIsProp(
  prop: Node,
): Boolean =
  when (prop) {
    is AttributeNode -> prop.name == "is"
    is DirectiveNode -> prop.name == "bind" && (prop.arg as? SimpleExpressionNode)?.let { it.isStatic && it.content == "is" } == true
    else -> false
  }