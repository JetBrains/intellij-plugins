package org.jetbrains.vuejs.inspections

import com.intellij.codeInspection.LocalInspectionTool
import com.intellij.codeInspection.LocalQuickFix
import com.intellij.codeInspection.LocalQuickFixOnPsiElement
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.lang.ecmascript6.psi.ES6FunctionProperty
import com.intellij.lang.ecmascript6.psi.JSExportAssignment
import com.intellij.lang.javascript.psi.*
import com.intellij.lang.javascript.psi.impl.JSChangeUtil
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiElementVisitor
import com.intellij.psi.PsiFile
import org.jetbrains.vuejs.VueFileType

class DataFunctionInspection : LocalInspectionTool() {
  override fun buildVisitor(holder: ProblemsHolder, isOnTheFly: Boolean): PsiElementVisitor {
    return object: JSElementVisitor() {
      override fun visitJSProperty(node: JSProperty?) {
        if ("data" != node?.name || node?.value is JSFunction || node?.value is JSReferenceExpression) return


        if (isComponent(node!!)) {
          val quickFixes:Array<LocalQuickFix> = if (node.value is JSObjectLiteralExpression) arrayOf(WrapWithFunctionFix(node.value!!)) else emptyArray()
          holder.registerProblem(node.nameIdentifier!!, "Data property should be a function", *quickFixes)
        }
      }

      override fun visitES6FunctionProperty(functionProperty: ES6FunctionProperty?) {
        return
      }
    }
  }

  fun isComponent(property: JSProperty): Boolean {
    return property.parent is JSObjectLiteralExpression && property.parent.parent is JSExportAssignment &&
           property.containingFile.fileType == VueFileType.INSTANCE
  }
}

class WrapWithFunctionFix(psiElement: PsiElement) : LocalQuickFixOnPsiElement(psiElement) {
  override fun getText() = "Wrap data object in function"

  override fun invoke(project: Project, file: PsiFile, startElement: PsiElement, endElement: PsiElement) {
    val expression = startElement as JSObjectLiteralExpression
    val property = expression.parent as JSProperty

    val newProperty = (JSChangeUtil.createExpressionWithContext("{ data() {return ${expression.text}}}", property)!!.psi as JSObjectLiteralExpression).firstProperty!!
    property.replace(newProperty)
  }

  override fun getFamilyName() = "Wrap object"

}
