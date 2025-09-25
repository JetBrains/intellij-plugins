// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.angular2.inspections.quickfixes

import com.intellij.codeInsight.intention.PriorityAction
import com.intellij.codeInsight.template.Expression
import com.intellij.codeInsight.template.Result
import com.intellij.codeInsight.template.Template
import com.intellij.codeInsight.template.impl.ConstantNode
import com.intellij.lang.ecmascript6.psi.impl.ES6ImportPsiUtil
import com.intellij.lang.javascript.psi.JSCallExpression
import com.intellij.lang.javascript.psi.JSReferenceExpression
import com.intellij.lang.javascript.psi.ecmal4.JSClass
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Pair
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.SmartPsiElementPointer
import com.intellij.psi.createSmartPointer
import com.intellij.psi.util.parentOfType
import org.angular2.lang.Angular2Bundle
import org.angular2.signals.Angular2SignalUtils

class CreateComponentSignalIntentionAction(methodExpression: JSReferenceExpression)
  : BaseCreateComponentFieldAction(methodExpression.referenceName) {

  private val myRefExpressionPointer: SmartPsiElementPointer<JSReferenceExpression> = methodExpression.createSmartPointer()

  override fun isAvailable(project: Project?, element: PsiElement?, editor: Editor?, file: PsiFile?): Boolean =
    myRefExpressionPointer.dereference()?.parentOfType<JSCallExpression>()?.argumentSize == 0
    && super.isAvailable(project, element, editor, file)

  override fun getName(): String {
    return Angular2Bundle.message("angular.quickfix.template.create-signal.name", myReferencedName)
  }

  override fun getPriority(): PriorityAction.Priority {
    return PriorityAction.Priority.NORMAL
  }

  override fun calculateAnchors(psiElement: PsiElement): Pair<JSReferenceExpression?, PsiElement?> {
    return Pair.create(myRefExpressionPointer.element, psiElement.lastChild)
  }

  override fun buildTemplate(template: Template,
                             referenceExpression: JSReferenceExpression?,
                             isStaticContext: Boolean,
                             anchorParent: PsiElement) {
    Angular2FixesTemplateUtil.addClassMemberModifiers(template, isStaticContext, anchorParent as JSClass)
    template.addTextSegment("readonly ")
    template.addTextSegment(myReferencedName)
    template.addTextSegment(" = signal<")

    val types = guessTypesForExpression(referenceExpression, anchorParent, false)
      .map { if (!it.matches(Regex("\\|\\s*null($|\\s*\\|)"))) "$it | null" else it }
    if (types.isEmpty()) {
      addCompletionVar(template)
    }
    else {
      val expression: Expression = if (types.size == 1)
        ConstantNode(types[0])
      else
        ConstantNode(null as Result?).withLookupStrings(types)
      template.addVariable("__type" + referenceExpression!!.getText(), expression, expression, true)
    }
    template.addTextSegment(">(")

    val expression: Expression = ConstantNode("null")
    template.addVariable("\$INITIAL_VALUE$", expression, expression, true)
    template.addTextSegment(")")
    addSemicolonSegment(template, anchorParent)

    ES6ImportPsiUtil.insertJSImport(anchorParent, Angular2SignalUtils.SIGNAL_FUNCTION,
                                    Angular2SignalUtils.signalFunction(anchorParent) ?: return, null)
  }
}