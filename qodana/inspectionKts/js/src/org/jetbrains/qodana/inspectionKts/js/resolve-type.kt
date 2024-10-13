package org.jetbrains.qodana.inspectionKts.js

import com.intellij.dev.psiViewer.properties.tree.nodes.apiMethods.PsiViewerApiMethod
import com.intellij.lang.javascript.psi.JSExpression
import com.intellij.lang.javascript.psi.JSType
import com.intellij.lang.javascript.psi.resolve.JSResolveUtil
import com.intellij.openapi.application.readAction
import com.intellij.psi.PsiElement
import org.jetbrains.qodana.inspectionKts.InspectionKtsDefaultImportProvider

private class JsInspectionKtsDefaultImportsProvider : InspectionKtsDefaultImportProvider {
  override fun imports(): List<String> {
    val thisPackage = "org.jetbrains.qodana.inspectionKts.js"
    return listOf(
      "$thisPackage.resolveJsType",
      "$thisPackage.resolveExpressionJsType"
    )
  }
}

/**
 * DO NOT CHANGE THE SIGNATURE OF THIS METHOD IN NOT API-COMPATIBLE WAYS!
 * IT IS USED BY USER'S .inspection.kts SCRIPTS!!!
 * IF NEEDED, ASK ANY QUESTIONS QODANA CORE TEAM
 */
fun PsiElement.resolveJsType(): JSType? {
  return JSResolveUtil.getElementJSType(this)
}

/**
 * DO NOT CHANGE THE SIGNATURE OF THIS METHOD IN NOT API-COMPATIBLE WAYS!
 * IT IS USED BY USER'S .inspection.kts SCRIPTS!!!
 * IF NEEDED, ASK ANY QUESTIONS QODANA CORE TEAM
 */
fun JSExpression.resolveExpressionJsType(): JSType? {
  return JSResolveUtil.getExpressionJSType(this)
}

private class JsPsiViewerApiMethodProvider : PsiViewerApiMethod.Provider {
  override fun apiMethods(instance: Any, clazz: Class<*>): List<PsiViewerApiMethod> {
    return when (clazz) {
      PsiElement::class.java -> listOf(resolveJsTypeApiMethod(instance))
      JSExpression::class.java -> listOf(resolveJsExpressionTypeApiMethod(instance))
      else -> emptyList()
    }
  }

  private fun resolveJsTypeApiMethod(instance: Any): PsiViewerApiMethod {
    return PsiViewerApiMethod(
      "resolveJsType",
      PsiViewerApiMethod.ReturnType(JSType::class.java, returnedCollectionType = null),
    ) {
      return@PsiViewerApiMethod readAction {
        (instance as? PsiElement)?.resolveJsType()
      }
    }
  }

  private fun resolveJsExpressionTypeApiMethod(instance: Any): PsiViewerApiMethod {
    return PsiViewerApiMethod(
      "resolveExpressionJsType",
      PsiViewerApiMethod.ReturnType(JSType::class.java, returnedCollectionType = null),
    ) {
      return@PsiViewerApiMethod readAction {
        (instance as? JSExpression)?.resolveExpressionJsType()
      }
    }
  }
}