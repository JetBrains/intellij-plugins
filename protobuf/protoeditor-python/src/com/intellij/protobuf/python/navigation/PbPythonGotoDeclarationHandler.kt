package com.intellij.protobuf.python.navigation

import com.intellij.codeInsight.navigation.actions.GotoDeclarationHandler
import com.intellij.openapi.editor.Editor
import com.intellij.protobuf.python.PbPythonSourceContext
import com.intellij.psi.PsiElement
import com.intellij.psi.util.parentOfType
import com.jetbrains.python.PythonLanguage
import com.jetbrains.python.psi.PyFile
import com.jetbrains.python.psi.PyReferenceExpression

/**
 * Only for navigating from a generated Python file to the original `.proto` file
 */
internal class PbPythonGotoDeclarationHandler : GotoDeclarationHandler {
  override fun getGotoDeclarationTargets(
    sourceElement: PsiElement?,
    offset: Int,
    editor: Editor?,
  ): Array<PsiElement>? {
    if (sourceElement?.language != PythonLanguage.INSTANCE) return null

    val refExpr = sourceElement.parentOfType<PyReferenceExpression>() ?: return null
    val pyFile = refExpr.reference.resolve() as? PyFile ?: return null
    val source = PbPythonSourceContext.resolve(pyFile) ?: return null
    return arrayOf(source.pbFile)
  }
}
