package org.jetbrains.qodana.jvm.dev.inspectionKts

import com.intellij.dev.psiViewer.properties.tree.nodes.apiMethods.PsiViewerApiMethod
import com.intellij.openapi.application.readAction
import com.intellij.psi.*
import org.jetbrains.qodana.inspectionKts.InspectionKtsDefaultImportProvider

private class JavaInspectionKtsDefaultImportsProvider : InspectionKtsDefaultImportProvider {
  override fun imports(): List<String> {
    val thisPackage = "org.jetbrains.qodana.jvm.dev.inspectionKts"
    return listOf(
      "$thisPackage.getAllSuperTypes",
      "$thisPackage.asPsiClassType",
    )
  }
}

internal fun getAllSuperTypesPsiViewerApiMethod(psiType: PsiType): PsiViewerApiMethod {
  return PsiViewerApiMethod(
    "getAllSuperTypes",
    PsiViewerApiMethod.ReturnType(List::class.java, PsiType::class.java)
  ) {
    readAction {
      psiType.getAllSuperTypes()
    }
  }
}

/**
 * DO NOT CHANGE THE SIGNATURE OF THIS METHOD IN NOT API-COMPATIBLE WAYS!
 * IT IS USED BY USER'S .inspection.kts SCRIPTS!!!
 * IF NEEDED, ASK ANY QUESTIONS QODANA CORE TEAM
 */
fun PsiType.getAllSuperTypes(): List<PsiType> {
  return superTypes.flatMap { superType ->
    listOf(superType) + superType.getAllSuperTypes()
  }
}

internal fun psiClassAsPsiClassTypePsiViewerApiMethod(psiClass: PsiClass): PsiViewerApiMethod {
  return PsiViewerApiMethod(
    "asPsiClassType",
    PsiViewerApiMethod.ReturnType(PsiClassType::class.java, null)
  ) {
    readAction {
      psiClass.asPsiClassType()
    }
  }
}

/**
 * DO NOT CHANGE THE SIGNATURE OF THIS METHOD IN NOT API-COMPATIBLE WAYS!
 * IT IS USED BY USER'S .inspection.kts SCRIPTS!!!
 * IF NEEDED, ASK ANY QUESTIONS QODANA CORE TEAM
 */
fun PsiClass.asPsiClassType(): PsiClassType {
  val factory = JavaPsiFacade.getElementFactory(project)
  return factory.createType(this)
}

internal fun psiCodeReferenceElementAsPsiClassTypePsiViewerApiMethod(reference: PsiJavaCodeReferenceElement): PsiViewerApiMethod {
  return PsiViewerApiMethod(
    "asPsiClassType",
    PsiViewerApiMethod.ReturnType(PsiClassType::class.java, null)
  ) {
    readAction {
      reference.asPsiClassType()
    }
  }
}

/**
 * DO NOT CHANGE THE SIGNATURE OF THIS METHOD IN NOT API-COMPATIBLE WAYS!
 * IT IS USED BY USER'S .inspection.kts SCRIPTS!!!
 * IF NEEDED, ASK ANY QUESTIONS QODANA CORE TEAM
 */
fun PsiJavaCodeReferenceElement.asPsiClassType(): PsiClassType {
  val factory = JavaPsiFacade.getElementFactory(project)
  return factory.createType(this)
}