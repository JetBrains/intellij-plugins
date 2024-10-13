package org.jetbrains.qodana.inspectionKts.kotlin

import com.intellij.dev.psiViewer.properties.tree.PsiViewerPropertyNode
import com.intellij.dev.psiViewer.properties.tree.nodes.apiMethods.PsiViewerApiMethod
import com.intellij.dev.psiViewer.properties.tree.nodes.psiViewerPropertyNodeForApiClass
import com.intellij.openapi.application.readAction
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import org.jetbrains.kotlin.analysis.api.KaSession
import org.jetbrains.kotlin.analysis.api.analyze
import org.jetbrains.kotlin.analysis.api.lifetime.KaLifetimeOwner
import org.jetbrains.kotlin.psi.KtElement

/**
 * We can't store and access properties of [KaLifetimeOwner] outside of [analyze],
 * so instead we always compute it using [valueProvider] inside [analyze] block
 */
internal class KtLifetimeOwnerProvider(
  val entrypointKtElement: KtElement,
  val presentation: PsiViewerPropertyNode.Presentation,
  val valueProvider: KaSession.() -> KaLifetimeOwner?
)

internal class KtLifetimeOwnerPsiViewerNode(
  private val nodeContext: PsiViewerPropertyNode.Context,
  private val ktLifetimeOwnerProvider: KtLifetimeOwnerProvider,
) : PsiViewerPropertyNode {
  class Factory : PsiViewerPropertyNode.Factory {
    override fun isMatchingType(clazz: Class<*>): Boolean {
      return clazz.isKtLifetimeOwnerType()
    }

    override suspend fun createNode(nodeContext: PsiViewerPropertyNode.Context, returnedValue: Any): PsiViewerPropertyNode? {
      val ktLifetimeOwnerProvider = returnedValue as? KtLifetimeOwnerProvider ?: return null
      return KtLifetimeOwnerPsiViewerNode(nodeContext, ktLifetimeOwnerProvider)
    }
  }

  override val children = PsiViewerPropertyNode.Children.Async {
    val ktLifetimeOwnerApiMethods: List<Pair<Class<*>, List<PsiViewerApiMethod>>> = readAction {
      analyze(ktLifetimeOwnerProvider.entrypointKtElement) {
        val ktLifetimeOwner = ktLifetimeOwnerProvider.valueProvider(this@analyze) ?: return@analyze emptyList()
        ktLifetimeOwnerPsiViewerApiMethods(nodeContext, parentProvider = ktLifetimeOwnerProvider, ktLifetimeOwner)
      }
    }
    val childrenNodes = coroutineScope {
      ktLifetimeOwnerApiMethods
        .mapIndexed { idx, (apiClass, apiMethods) ->
          async {
            psiViewerPropertyNodeForApiClass(nodeContext, apiClass, apiMethods, weight = idx)
          }
        }
        .awaitAll()
        .filterNotNull()
    }
    childrenNodes
  }

  override val presentation: PsiViewerPropertyNode.Presentation
    get() = ktLifetimeOwnerProvider.presentation

  override val weight: Int
    get() = 150
}

internal fun Class<*>.isKtLifetimeOwnerType(): Boolean {
  return KaLifetimeOwner::class.java.isAssignableFrom(this)
}

