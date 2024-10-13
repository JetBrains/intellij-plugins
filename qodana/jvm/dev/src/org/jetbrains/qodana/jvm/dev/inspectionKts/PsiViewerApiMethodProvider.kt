package org.jetbrains.qodana.jvm.dev.inspectionKts

import com.intellij.dev.psiViewer.properties.tree.nodes.apiMethods.PsiViewerApiMethod
import com.intellij.psi.PsiClass
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiJavaCodeReferenceElement
import com.intellij.psi.PsiType

private class PsiViewerApiMethodProvider : PsiViewerApiMethod.Provider {
  override fun apiMethods(instance: Any, clazz: Class<*>): List<PsiViewerApiMethod> {
    return buildList {
      ifExactClass<PsiFile>(clazz, instance) {
        add(pathRelativeToProjectPsiViewerApiMethod())
      }
      ifExactClass<PsiType>(clazz, instance) {
        add(getAllSuperTypesPsiViewerApiMethod(it))
      }
      ifExactClass<PsiClass>(clazz, instance) {
        add(psiClassAsPsiClassTypePsiViewerApiMethod(it))
      }
      ifExactClass<PsiJavaCodeReferenceElement>(clazz, instance) {
        add(psiCodeReferenceElementAsPsiClassTypePsiViewerApiMethod(it))
      }
    }
  }
}

private inline fun <reified T> ifExactClass(clazz: Class<*>, instance: Any, action: (T) -> Unit) {
  if (clazz == T::class.java && instance is T) {
    action(instance)
  }
}

/** see [org.jetbrains.qodana.inspectionKts.api.getPathRelativeToProject] */
private fun pathRelativeToProjectPsiViewerApiMethod(): PsiViewerApiMethod {
  return PsiViewerApiMethod(
    "getPathRelativeToProject",
    PsiViewerApiMethod.ReturnType(String::class.java, null)
  ) {
    "file/path/relative/to/project (not displayed in PSI Viewer)"
  }
}