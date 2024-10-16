package org.jetbrains.qodana.inspectionKts.js

import com.intellij.dev.psiViewer.properties.tree.PsiViewerPropertyNode
import com.intellij.dev.psiViewer.properties.tree.nodes.computePsiViewerApiClassesNodes
import com.intellij.dev.psiViewer.properties.tree.nodes.psiViewerApiClassesExtending
import com.intellij.dev.psiViewer.properties.tree.nodes.psiViewerPsiTypeAttributes
import com.intellij.lang.javascript.psi.JSType

private class PsiViewerJSTypeNode(
  private val jsType: JSType,
  private val nodeContext: PsiViewerPropertyNode.Context
) : PsiViewerPropertyNode {
  class Factory : PsiViewerPropertyNode.Factory {
    override fun isMatchingType(clazz: Class<*>): Boolean = JSType::class.java.isAssignableFrom(clazz)

    override suspend fun createNode(nodeContext: PsiViewerPropertyNode.Context, returnedValue: Any): PsiViewerPropertyNode? {
      val jsType = returnedValue as? JSType ?: return null
      return PsiViewerJSTypeNode(jsType, nodeContext)
    }
  }

  override val presentation = PsiViewerPropertyNode.Presentation {
    @Suppress("HardCodedStringLiteral")
    it.append(jsType.toString(), psiViewerPsiTypeAttributes())
  }

  override val children = PsiViewerPropertyNode.Children.Async {
    val apiClasses = jsType::class.java.psiViewerApiClassesExtending(JSType::class.java)
    computePsiViewerApiClassesNodes(apiClasses, jsType, nodeContext)
  }

  override val weight: Int
    get() = 30
}