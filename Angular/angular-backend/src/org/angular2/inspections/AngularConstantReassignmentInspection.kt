package org.angular2.inspections

import com.intellij.codeInspection.LocalInspectionToolSession
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.lang.javascript.inspections.JSConstantReassignmentInspection
import com.intellij.psi.PsiElementVisitor
import org.angular2.lang.expr.Angular2Language
import org.angular2.lang.html.Angular2HtmlLanguage

class AngularConstantReassignmentInspection: JSConstantReassignmentInspection() {

  override val isCoveredByTypeScriptServiceHighlighting: Boolean
    get() = false

  override fun createVisitor(holder: ProblemsHolder, session: LocalInspectionToolSession): PsiElementVisitor {
    val fileLang = holder.file.language
    return if (fileLang.isKindOf(Angular2HtmlLanguage) || fileLang.isKindOf(Angular2Language))
      super.createVisitor(holder, session)
    else
      PsiElementVisitor.EMPTY_VISITOR
  }
}
