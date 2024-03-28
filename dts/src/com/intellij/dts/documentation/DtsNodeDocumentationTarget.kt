package com.intellij.dts.documentation

import com.intellij.dts.lang.psi.DtsNode
import com.intellij.dts.lang.psi.DtsRefNode
import com.intellij.dts.lang.psi.getDtsPresentableText
import com.intellij.dts.lang.psi.getDtsReferenceTarget
import com.intellij.dts.util.DtsTreeUtil
import com.intellij.dts.zephyr.binding.DtsZephyrBindingProvider
import com.intellij.model.Pointer
import com.intellij.platform.backend.documentation.DocumentationTarget
import com.intellij.platform.backend.presentation.TargetPresentation
import com.intellij.psi.createSmartPointer

class DtsNodeDocumentationTarget(private val node: DtsNode) : DtsDocumentationTarget(node.project) {
  override fun createPointer(): Pointer<out DocumentationTarget> {
    val propertyPtr = node.createSmartPointer()

    return Pointer {
      propertyPtr.dereference()?.let { DtsNodeDocumentationTarget(it) }
    }
  }

  override fun computePresentation(): TargetPresentation {
    return TargetPresentation.builder(node.getDtsPresentableText()).icon(node.getIcon(0)).presentation()
  }

  private fun nodeNameTarget(node: DtsNode): DtsNode {
    if (node !is DtsRefNode) return node
    return node.getDtsReferenceTarget() ?: node
  }

  override fun buildDocumentation(html: DtsDocumentationHtmlBuilder) {
    buildNodeName(html, nodeNameTarget(node))
    DtsTreeUtil.findParentNode(node)?.let { parent -> buildDeclaredIn(html, parent) }

    val binding = DtsZephyrBindingProvider.bindingFor(node, fallbackBinding = false) ?: return
    buildNodeBinding(html, binding)
  }
}