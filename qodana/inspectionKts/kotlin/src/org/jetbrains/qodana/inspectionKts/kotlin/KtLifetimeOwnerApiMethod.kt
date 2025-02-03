package org.jetbrains.qodana.inspectionKts.kotlin

import com.intellij.dev.psiViewer.properties.tree.PsiViewerPropertyNode
import com.intellij.dev.psiViewer.properties.tree.nodes.apiMethods.PsiViewerApiMethod
import com.intellij.dev.psiViewer.properties.tree.nodes.apiMethods.psiViewerApiReflectionMethods
import com.intellij.dev.psiViewer.properties.tree.nodes.apiMethods.psiViewerReflectionMethodReturnType
import com.intellij.dev.psiViewer.properties.tree.nodes.psiViewerApiClassesExtending
import org.jetbrains.kotlin.analysis.api.KaExperimentalApi
import org.jetbrains.kotlin.analysis.api.KaSession
import org.jetbrains.kotlin.analysis.api.lifetime.KaLifetimeOwner
import org.jetbrains.kotlin.analysis.api.symbols.KaClassLikeSymbol
import org.jetbrains.kotlin.analysis.api.types.KaType
import org.jetbrains.kotlin.psi.KtElement

internal fun KaSession.ktLifetimeOwnerPsiViewerApiMethods(
  nodeContext: PsiViewerPropertyNode.Context,
  parentProvider: KtLifetimeOwnerProvider,
  ktLifetimeOwner: KaLifetimeOwner
): List<Pair<Class<*>, List<PsiViewerApiMethod>>> {
  val apiClasses = ktLifetimeOwner::class.java.ktLifetimeOwnerPsiViewerApiClasses()
  return apiClasses.map { apiClass ->
    val ktLifetimeOwnerApiMethods = apiClass.ktLifetimeOwnerApiMethods()
    val evaluatedMethods = ktLifetimeOwnerApiMethods.mapNotNull { ktLifetimeOwnerApiMethod ->
      evalKtLifetimeOwnerApiMethod(ktLifetimeOwnerApiMethod, ktLifetimeOwner)
    }
    apiClass to evaluatedMethods.mapNotNull {
      it.asPsiViewerApiMethod(nodeContext, parentProvider.entrypointKtElement, parentProvider.valueProvider)
    }
  }
}

internal fun Class<*>.ktLifetimeOwnerApiMethods(): List<KtLifetimeOwnerApiMethod> {
  return ktLifetimeOwnerFromReflectionApiMethods() + ktLifetimeOwnerAdditionalApiMethods()
}

internal class KtLifetimeOwnerApiMethod(
  val name: String,
  val returnType: PsiViewerApiMethod.ReturnType,
  val evaluator: KaSession.(KaLifetimeOwner?) -> Any?
) {
  class Evaluated(
    private val value: Any?,
    private val apiMethod: KtLifetimeOwnerApiMethod
  ) {
    fun asPsiViewerApiMethod(
      nodeContext: PsiViewerPropertyNode.Context,
      entrypointKtElement: KtElement,
      parentValueProvider: KaSession.() -> KaLifetimeOwner?
    ): PsiViewerApiMethod? {
      val wrappedValue = when {
        value == null -> {
          if (nodeContext.showEmptyNodes) {
            PsiViewerApiMethod(apiMethod.name, apiMethod.returnType) {
              null
            }
          }
          else {
            null
          }
        }
        apiMethod.returnType.returnType.isKtLifetimeOwnerType() -> {
          wrapKtLifetimeOwner(value, entrypointKtElement, parentValueProvider, apiMethod)
        }
        apiMethod.returnType.returnedCollectionType?.isKtLifetimeOwnerType() == true -> {
          wrapKtLifetimeOwnerList(value, entrypointKtElement, parentValueProvider, apiMethod) ?: return null
        }
        else -> {
          value
        }
      }
      return PsiViewerApiMethod(apiMethod.name, apiMethod.returnType) {
        wrappedValue
      }
    }
  }
}

internal fun KaSession.evalKtLifetimeOwnerApiMethod(
  method: KtLifetimeOwnerApiMethod,
  ktLifetimeOwner: KaLifetimeOwner?
): KtLifetimeOwnerApiMethod.Evaluated? {
  val methodType = method.returnType.returnedCollectionType ?: method.returnType.returnType

  val isPsiViewerSupportedType = PsiViewerPropertyNode.Factory.findMatchingFactory(methodType) != null
  if (!isPsiViewerSupportedType) return null

  val value = method.evaluator(this, ktLifetimeOwner)
  return KtLifetimeOwnerApiMethod.Evaluated(value, method)
}

private fun wrapKtLifetimeOwner(
  value: Any,
  entrypointKtElement: KtElement,
  parentValueProvider: KaSession.() -> KaLifetimeOwner?,
  method: KtLifetimeOwnerApiMethod,
): KtLifetimeOwnerProvider {
  return KtLifetimeOwnerProvider(
    entrypointKtElement,
    value.toStringPresentation()
  ) analysisApiValueProvider@{
    val parentValue = parentValueProvider()
    method.evaluator(this@analysisApiValueProvider, parentValue) as? KaLifetimeOwner
  }
}

private fun wrapKtLifetimeOwnerList(
  value: Any,
  entrypointKtElement: KtElement,
  parentValueProvider: KaSession.() -> KaLifetimeOwner?,
  method: KtLifetimeOwnerApiMethod,
): List<KtLifetimeOwnerProvider>? {
  val list = value.castToNonNullList() ?: return null
  return list.mapIndexed { idx, listElement ->
    KtLifetimeOwnerProvider(
      entrypointKtElement,
      listElement.toStringPresentation()
    ) {
      val parentValue = parentValueProvider()
      val evaluatedList = method.evaluator(this, parentValue)?.castToNonNullList()
      evaluatedList?.get(idx) as? KaLifetimeOwner
    }
  }
}

private fun Any.toStringPresentation(): PsiViewerPropertyNode.Presentation {
  val string = toString()
  val isNotOverridenToStringValue = string.contains("@")
  val stringPresentation = if (isNotOverridenToStringValue) {
    this::class.java.ktLifetimeOwnerPsiViewerApiClasses().first().canonicalName
  } else {
    string
  }

  return PsiViewerPropertyNode.Presentation {
    it.append(stringPresentation)
  }
}

private fun Class<*>.ktLifetimeOwnerFromReflectionApiMethods(): List<KtLifetimeOwnerApiMethod> {
  return psiViewerApiReflectionMethods(this).mapNotNull { reflectionMethod ->
    if (reflectionMethod.name.contains("qualifiers", ignoreCase = true)) return@mapNotNull null

    val returnType = psiViewerReflectionMethodReturnType(reflectionMethod)
    KtLifetimeOwnerApiMethod(reflectionMethod.name, returnType) { instance ->
      instance ?: return@KtLifetimeOwnerApiMethod null
      reflectionMethod.invoke(instance)
    }
  }
}

@OptIn(KaExperimentalApi::class)
private fun Class<*>.ktLifetimeOwnerAdditionalApiMethods(): List<KtLifetimeOwnerApiMethod> {
  return buildList {
    if (this@ktLifetimeOwnerAdditionalApiMethods == KaType::class.java) {
      addAll(
        listOf(
          ktTypeApiMethod("isPrimitive") { it.isPrimitive },
          ktTypeApiMethod("isUnit") { it.isUnitType },
          ktTypeApiMethod("isInt") { it.isIntType },
          ktTypeApiMethod("isLong") { it.isLongType },
          ktTypeApiMethod("isShort") { it.isShortType },
          ktTypeApiMethod("isByte") { it.isByteType },
          ktTypeApiMethod("isFloat") { it.isFloatType },

          ktTypeApiMethod("isDouble") { it.isDoubleType },
          ktTypeApiMethod("isChar") { it.isCharType },
          ktTypeApiMethod("isBoolean") { it.isBooleanType },
          ktTypeApiMethod("isString") { it.isStringType },
          ktTypeApiMethod("isCharSequence") { it.isCharSequenceType },
          ktTypeApiMethod("isAny") { it.isAnyType },
          ktTypeApiMethod("getExpandedClassSymbol") { it.expandedSymbol },
          ktTypeApiMethod("getFullyExpandedType") { it.fullyExpandedType },

          ktTypeApiMethod("getFunctionTypeKind") { it.functionTypeKind },
          ktTypeApiMethod("isFunctionalInterfaceType") { it.isFunctionalInterface },
          ktTypeApiMethod("isFunctionType") { it.isFunctionType },
          ktTypeApiMethod("isKFunctionType") { it.isKFunctionType },
          ktTypeApiMethod("isSuspendFunctionType") { it.isSuspendFunctionType },
          ktTypeApiMethod("isKSuspendFunctionType") { it.isKSuspendFunctionType },
        )
      )
    }
    if (this@ktLifetimeOwnerAdditionalApiMethods == KaClassLikeSymbol::class.java) {
      add(getFQNApiMethod())
    }
  }
}

private fun Class<*>.ktLifetimeOwnerPsiViewerApiClasses(): List<Class<*>> {
  return psiViewerApiClassesExtending(KaLifetimeOwner::class.java)
    .filter { !it.name.contains("fe10", ignoreCase = true) }
}

private fun Any.castToNonNullList(): List<Any>? {
  return ((this as? Array<*>)?.toList() ?: (this as? Collection<*>))?.filterNotNull()
}

private inline fun <reified T> ktTypeApiMethod(
  name: String,
  noinline evaluator: KaSession.(KaType) -> T?
): KtLifetimeOwnerApiMethod {
  return ktTypeApiMethod(name, T::class.java, evaluator)
}

private fun <T> ktTypeApiMethod(
  name: String,
  returnType: Class<T>,
  evaluator: KaSession.(KaType) -> T?
): KtLifetimeOwnerApiMethod {
  return KtLifetimeOwnerApiMethod(name, PsiViewerApiMethod.ReturnType(returnType, null)) {
    if (it !is KaType) return@KtLifetimeOwnerApiMethod null
    evaluator(this, it)
  }
}

private fun getFQNApiMethod(): KtLifetimeOwnerApiMethod {
  return KtLifetimeOwnerApiMethod("getFQN", PsiViewerApiMethod.ReturnType(String::class.java, null)) {
    if (it !is KaClassLikeSymbol) return@KtLifetimeOwnerApiMethod null
    it.getFQN()
  }
}