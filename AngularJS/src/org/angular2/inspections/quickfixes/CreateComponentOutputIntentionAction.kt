// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.angular2.inspections.quickfixes

import com.intellij.codeInsight.intention.PriorityAction
import com.intellij.codeInsight.template.Template
import com.intellij.codeInsight.template.impl.ConstantNode
import com.intellij.lang.javascript.psi.JSCallExpression
import com.intellij.lang.javascript.psi.JSReferenceExpression
import com.intellij.lang.javascript.psi.JSType
import com.intellij.lang.javascript.psi.ecmal4.JSClass
import com.intellij.lang.javascript.psi.resolve.JSResolveUtil
import com.intellij.lang.javascript.validation.fixes.CreateJSVariableIntentionAction
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Pair
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.SmartPsiElementPointer
import com.intellij.refactoring.suggested.createSmartPointer
import org.angular2.Angular2DecoratorUtil.OUTPUT_DEC
import org.angular2.entities.Angular2ComponentLocator
import org.angular2.lang.Angular2Bundle
import org.angular2.lang.Angular2LangUtil.ANGULAR_CORE_PACKAGE
import org.angular2.lang.Angular2LangUtil.EVENT_EMITTER

class CreateComponentOutputIntentionAction(emitCallExpression: JSCallExpression, referenceName: String)
  : CreateJSVariableIntentionAction(referenceName, true, false, false) {

  private val emitCallExpressionPointer: SmartPsiElementPointer<JSCallExpression> = emitCallExpression.createSmartPointer()

  override fun isAvailable(project: Project?, element: PsiElement?, editor: Editor?, file: PsiFile?): Boolean {
    return true
  }

  override fun applyFix(project: Project, psiElement: PsiElement, file: PsiFile, editor: Editor?) {
    val componentClass = Angular2ComponentLocator.findComponentClass(psiElement)!!
    doApplyFix(project, componentClass, componentClass.containingFile, null)
  }

  override fun getName(): String {
    return Angular2Bundle.message("angular.quickfix.template.create-output.name", myReferencedName)
  }

  override fun getPriority(): PriorityAction.Priority {
    return PriorityAction.Priority.TOP
  }

  override fun beforeStartTemplateAction(referenceExpression: JSReferenceExpression,
                                         editor: Editor,
                                         anchor: PsiElement,
                                         isStaticContext: Boolean): JSReferenceExpression {
    return referenceExpression
  }

  override fun skipParentIfClass(): Boolean {
    return false
  }

  override fun calculateAnchors(psiElement: PsiElement): Pair<JSReferenceExpression, PsiElement> {
    return Pair.create(emitCallExpressionPointer.element?.methodExpression as JSReferenceExpression, psiElement.lastChild)
  }

  override fun addAccessModifier(template: Template,
                                 referenceExpression: JSReferenceExpression,
                                 staticContext: Boolean,
                                 targetClass: JSClass) {
    Angular2FixesTemplateUtil.addClassMemberModifiers(template, staticContext, targetClass)
  }

  override fun buildTemplate(template: Template,
                             referenceExpression: JSReferenceExpression?,
                             isStaticContext: Boolean,
                             anchorParent: PsiElement) {
    template.addTextSegment("@Output() ")
    template.addTextSegment(myReferencedName)
    template.addTextSegment(" = new EventEmitter<")

    emitCallExpressionPointer.dereference().let { callExpression ->
      val type = callExpression?.arguments?.getOrNull(0)
        ?.let { JSResolveUtil.getElementJSType(it) }
        ?.getTypeText(JSType.TypeTextFormat.CODE)
      if (type == null) {
        addCompletionVar(template)
      }
      else {
        template.addVariable("__type" + referenceExpression!!.getText(), ConstantNode(type), ConstantNode(type), true)
      }
    }
    template.addTextSegment(">()")
    addSemicolonSegment(template, anchorParent)

    Angular2FixesPsiUtil.insertJSImport(anchorParent, ANGULAR_CORE_PACKAGE, OUTPUT_DEC)
    Angular2FixesPsiUtil.insertJSImport(anchorParent, ANGULAR_CORE_PACKAGE, EVENT_EMITTER)
  }
}