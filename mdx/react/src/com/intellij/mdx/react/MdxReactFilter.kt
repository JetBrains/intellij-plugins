package com.intellij.mdx.react

import com.intellij.codeInsight.template.emmet.filters.ZenCodingFilter
import com.intellij.codeInsight.template.emmet.nodes.GenerationNode
import com.intellij.psi.PsiElement
import com.intellij.react.shared.emmet.getMappedReactAttribute
import com.intellij.react.shared.emmet.mapReactAttributes
import org.intellij.plugin.mdx.lang.template.MdxFileViewProvider

internal class MdxReactFilter : ZenCodingFilter() {
  override fun getSuffix(): String = "react"

  override fun isMyContext(context: PsiElement): Boolean = context.containingFile.viewProvider is MdxFileViewProvider

  override fun getDisplayName(): String = MdxReactBundle.message("react.filter.display.name")

  override fun filterNode(node: GenerationNode): GenerationNode {
    val attributes = node.templateToken.attributes
    for ((name, value) in attributes.toMap()) {
      val mapped = getMappedReactAttribute(name) ?: continue
      attributes.remove(name)
      attributes[mapped] = value
    }
    return mapReactAttributes(node)
  }

  override fun isSystem(): Boolean = true
}
