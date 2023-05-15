// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.vuejs.inspections

import com.intellij.codeInspection.LocalInspectionTool
import com.intellij.codeInspection.LocalQuickFix
import com.intellij.codeInspection.LocalQuickFixOnPsiElement
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.lang.ecmascript6.psi.JSExportAssignment
import com.intellij.lang.javascript.psi.*
import com.intellij.lang.javascript.psi.impl.JSPsiElementFactory
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiElementVisitor
import com.intellij.psi.PsiFile
import org.jetbrains.vuejs.VueBundle
import org.jetbrains.vuejs.lang.html.VueFileType

class DataFunctionInspection : LocalInspectionTool() {
  override fun buildVisitor(holder: ProblemsHolder, isOnTheFly: Boolean): PsiElementVisitor {
    return object : JSElementVisitor() {
      override fun visitJSProperty(node: JSProperty) {
        if ("data" != node.name || node.value is JSFunction || node.value is JSReferenceExpression) return

        if (isComponent(node)) {
          val quickFixes: Array<LocalQuickFix> =
            if (node.value is JSObjectLiteralExpression)
              arrayOf(WrapWithFunctionFix(node.value!!))
            else emptyArray()
          holder.registerProblem(node.nameIdentifier!!, VueBundle.message("vue.inspection.message.data.property.should.be.function"),
                                 *quickFixes)
        }
      }

      override fun visitJSFunctionProperty(functionProperty: JSFunctionProperty) {
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
  override fun getText(): String = VueBundle.message("vue.quickfix.wrap.with.function.text")

  override fun invoke(project: Project, file: PsiFile, startElement: PsiElement, endElement: PsiElement) {
    val expression = startElement as JSObjectLiteralExpression
    val property = expression.parent as JSProperty

    val newProperty = JSPsiElementFactory.createJSExpression(
      "{ data() {return ${expression.text}}}", property, JSObjectLiteralExpression::class.java).firstProperty!!
    property.replace(newProperty)
  }

  override fun getFamilyName(): String = VueBundle.message("vue.quickfix.wrap.with.function.family")

}
