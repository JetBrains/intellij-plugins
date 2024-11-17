package org.jetbrains.qodana.inspectionKts.kotlin

import com.intellij.dev.psiViewer.properties.tree.PsiViewerPropertyNode
import com.intellij.dev.psiViewer.properties.tree.nodes.apiMethods.psiViewerApiMethods
import com.intellij.dev.psiViewer.properties.tree.nodes.computePsiViewerPropertyNodesByCallingApiMethods
import org.jetbrains.kotlin.builtins.functions.FunctionTypeKind
import org.jetbrains.kotlin.name.CallableId
import org.jetbrains.kotlin.name.ClassId
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.name.Name

internal class KotlinDomainClassPsiViewerNode(
  private val nodeContext: PsiViewerPropertyNode.Context,
  private val value: Any
) : PsiViewerPropertyNode {
  class Factory : PsiViewerPropertyNode.Factory {
    override fun isMatchingType(clazz: Class<*>): Boolean {
      return clazz in domainClasses()
    }

    override suspend fun createNode(nodeContext: PsiViewerPropertyNode.Context, returnedValue: Any): PsiViewerPropertyNode {
      return KotlinDomainClassPsiViewerNode(nodeContext, returnedValue)
    }
  }

  override val children = PsiViewerPropertyNode.Children.Async {
    val apiMethods = value::class.java.psiViewerApiMethods(value)
    computePsiViewerPropertyNodesByCallingApiMethods(nodeContext, apiMethods)
  }

  override val presentation = PsiViewerPropertyNode.Presentation {
    @Suppress("HardCodedStringLiteral")
    it.append(value.toString())
  }

  override val weight: Int
    get() = 80 + domainClasses().indexOf(value::class.java)
}

private fun domainClasses(): List<Class<*>> {
  return listOf(
    ClassId::class.java,
    FqName::class.java,
    Name::class.java,
    FunctionTypeKind::class.java,
    CallableId::class.java,
  )
}