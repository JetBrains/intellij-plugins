package org.angular2.inspections.quickfixes

import com.intellij.codeInsight.intention.HighPriorityAction
import com.intellij.codeInspection.LocalQuickFix
import com.intellij.codeInspection.ProblemDescriptor
import com.intellij.codeInspection.util.IntentionFamilyName
import com.intellij.codeInspection.util.IntentionName
import com.intellij.javascript.web.js.WebJSResolveUtil
import com.intellij.lang.ecmascript6.psi.impl.ES6ImportPsiUtil
import com.intellij.lang.javascript.psi.JSReferenceExpression
import com.intellij.lang.javascript.psi.impl.JSChangeUtil
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import org.angular2.Angular2DecoratorUtil.FORWARD_REF_FUN
import org.angular2.lang.Angular2Bundle
import org.angular2.lang.Angular2LangUtil.ANGULAR_CORE_PACKAGE

class WrapWithForwardRefQuickFix() : LocalQuickFix, HighPriorityAction {

  override fun getFamilyName(): @IntentionFamilyName String =
    Angular2Bundle.message("angular.quickfix.wrap-with-forwardRef.family")

  override fun getName(): @IntentionName String =
    familyName

  override fun applyFix(project: Project, descriptor: ProblemDescriptor) {
    val reference = descriptor.psiElement as? JSReferenceExpression ?: return

    val forwardRefFun = WebJSResolveUtil.resolveSymbolFromNodeModule(reference, ANGULAR_CORE_PACKAGE, FORWARD_REF_FUN, PsiElement::class.java)
                        ?: return

    val forwardRefCall = JSChangeUtil.createExpressionWithContext("forwardRef(() => ${reference.text})", reference)?.psi
                         ?: return

    val newContext = reference.replace(forwardRefCall)

    ES6ImportPsiUtil.insertJSImport(newContext, FORWARD_REF_FUN, forwardRefFun, null)
  }

}