package org.jetbrains.qodana.inspectionKts.kotlin

import com.intellij.dev.psiViewer.properties.tree.PsiViewerPropertyNode
import com.intellij.dev.psiViewer.properties.tree.PsiViewerPropertyNodeAppender
import com.intellij.dev.psiViewer.properties.tree.nodes.PsiViewerPsiElementNode
import com.intellij.dev.psiViewer.properties.tree.nodes.PsiViewerRootNode
import com.intellij.dev.psiViewer.properties.tree.nodes.apiMethods.PsiViewerApiMethod
import com.intellij.dev.psiViewer.properties.tree.nodes.computePsiViewerPropertyNodesByCallingApiMethods
import com.intellij.openapi.application.readAction
import com.intellij.ui.SimpleTextAttributes
import icons.KotlinBaseResourcesIcons
import org.jetbrains.kotlin.analysis.api.KaSession
import org.jetbrains.kotlin.analysis.api.analyze
import org.jetbrains.kotlin.idea.references.KtReference
import org.jetbrains.kotlin.psi.*
import org.jetbrains.qodana.QodanaBundle

private class KotlinAnalyzeEntrypointNodeAppender : PsiViewerPropertyNodeAppender {
  override suspend fun appendChildren(
    nodeContext: PsiViewerPropertyNode.Context,
    parent: PsiViewerPropertyNode
  ): List<PsiViewerPropertyNode> {
    val psiElement = when(parent) {
      is PsiViewerRootNode -> parent.element
      is PsiViewerPsiElementNode -> parent.psiElement
      else -> return emptyList()
    }
    val ktElement = psiElement as? KtElement ?: return emptyList()

    val kotlinAnalyzeEntrypointNode = KotlinAnalyzeEntrypointNode(nodeContext, ktElement)
    val children = kotlinAnalyzeEntrypointNode.children.computeChildren()

    return if (children.isEmpty() && !nodeContext.showEmptyNodes) emptyList() else listOf(kotlinAnalyzeEntrypointNode)
  }
}

private class KotlinAnalyzeEntrypointNode(
  private val nodeContext: PsiViewerPropertyNode.Context,
  private val ktElement: KtElement
) : PsiViewerPropertyNode {
  override val children = PsiViewerPropertyNode.Children.Async {
    val ktLifetimeOwnerApiMethods = getEntrypointKtLifetimeApiMethods(ktElement)
    if (ktLifetimeOwnerApiMethods.isEmpty()) {
      return@Async emptyList()
    }
    val psiViewerApiMethods: List<PsiViewerApiMethod> = readAction {
      analyze(ktElement) {
        ktLifetimeOwnerApiMethods.mapNotNull { apiMethod ->
          val evaluatedMethod = evalKtLifetimeOwnerApiMethod(apiMethod, ktLifetimeOwner = null)
          evaluatedMethod?.asPsiViewerApiMethod(nodeContext, ktElement, parentValueProvider = { null })
        }
      }
    }
    computePsiViewerPropertyNodesByCallingApiMethods(nodeContext, psiViewerApiMethods)
  }

  override val presentation = PsiViewerPropertyNode.Presentation {
    it.icon = KotlinBaseResourcesIcons.Kotlin
    @Suppress("HardCodedStringLiteral")
    it.append("analyze(ktElement) { ... }")
    it.append(" ")
    it.append(QodanaBundle.message("psi.viewer.kotlin.api.in.analyze.block"), SimpleTextAttributes.GRAYED_ATTRIBUTES)
  }

  override val weight: Int
    get() = -1
}

private fun getEntrypointKtLifetimeApiMethods(ktElement: KtElement): List<KtLifetimeOwnerApiMethod> {
  return buildList {
    if (ktElement is KtDeclaration) {
      add(ktApiMethodOnPsi(ktElement, "getReturnKtType") { it.returnType })
    }
    if (ktElement is KtExpression) {
      add(ktApiMethodOnPsi(ktElement, "getKtType") { it.expressionType })
    }
    if (ktElement is KtFunction) {
      add(ktApiMethodOnPsi(ktElement, "getFunctionalType") { it.functionType })
    }
    if (ktElement is KtParameter) {
      add(ktApiMethodOnPsi(ktElement, "getParameterSymbol") { it.symbol })
    }
    if (ktElement is KtNamedFunction) {
      add(ktApiMethodOnPsi(ktElement, "getFunctionLikeSymbol") { it.symbol })
      add(ktApiMethodOnPsi(ktElement, "getAnonymousFunctionSymbol") { it.symbol })
    }
    if (ktElement is KtConstructor<*>) {
      add(ktApiMethodOnPsi(ktElement, "getConstructorSymbol") { it.symbol })
    }
    if (ktElement is KtTypeParameter) {
      add(ktApiMethodOnPsi(ktElement, "getTypeParameterSymbol") { it.symbol })
    }
    if (ktElement is KtTypeAlias) {
      add(ktApiMethodOnPsi(ktElement, "getTypeAliasSymbol") { it.symbol })
    }
    if (ktElement is KtEnumEntry) {
      add(ktApiMethodOnPsi(ktElement, "getEnumEntrySymbol") { it.symbol })
    }
    if (ktElement is KtFunctionLiteral) {
      add(ktApiMethodOnPsi(ktElement, "getAnonymousFunctionSymbol") { it.symbol })
    }
    if (ktElement is KtProperty) {
      add(ktApiMethodOnPsi(ktElement, "getVariableSymbol") { it.symbol })
    }
    if (ktElement is KtObjectLiteralExpression) {
      add(ktApiMethodOnPsi(ktElement, "getAnonymousObjectSymbol") { it.symbol })
    }
    if (ktElement is KtClassOrObject) {
      add(ktApiMethodOnPsi(ktElement, "getClassOrObjectSymbol") { it.symbol })
      add(ktApiMethodOnPsi(ktElement, "getNamedClassOrObjectSymbol") { it.symbol })
    }
    if (ktElement is KtPropertyAccessor) {
      add(ktApiMethodOnPsi(ktElement, "getPropertyAccessorSymbol") { it.symbol })
    }
    if (ktElement is KtReference) {
      add(ktApiMethodOnPsi(ktElement, "resolveToSymbol") { it.resolveToSymbol() })
    }
  }
}

private inline fun <T, reified R> ktApiMethodOnPsi(
  psi: T,
  name: String,
  noinline evaluator: KaSession.(T) -> R?
): KtLifetimeOwnerApiMethod {
  return ktApiMethodOnPsi(psi, name, R::class.java, evaluator)
}

private fun <T, R> ktApiMethodOnPsi(
  psi: T,
  name: String,
  returnType: Class<R>,
  evaluator: KaSession.(T) -> R?
): KtLifetimeOwnerApiMethod {
  return KtLifetimeOwnerApiMethod(
    name,
    PsiViewerApiMethod.ReturnType(returnType, null)
  ) {
    evaluator.invoke(this, psi)
  }
}
