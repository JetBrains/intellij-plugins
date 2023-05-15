// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.inspections

import com.intellij.codeInsight.template.Template
import com.intellij.codeInspection.InspectionSuppressor
import com.intellij.codeInspection.LocalQuickFix
import com.intellij.codeInspection.util.InspectionMessage
import com.intellij.lang.javascript.DialectDetector
import com.intellij.lang.javascript.ecmascript6.TypeScriptAnalysisHandlersFactory
import com.intellij.lang.javascript.psi.JSReferenceExpression
import com.intellij.lang.javascript.psi.JSThisExpression
import com.intellij.lang.javascript.psi.ecmal4.JSClass
import com.intellij.lang.javascript.psi.util.JSClassUtils
import com.intellij.lang.javascript.validation.JSProblemReporter
import com.intellij.lang.javascript.validation.JSReferenceChecker
import com.intellij.lang.javascript.validation.TypeScriptReferenceChecker
import com.intellij.lang.javascript.validation.fixes.CreateJSFunctionIntentionAction
import com.intellij.lang.javascript.validation.fixes.CreateJSVariableIntentionAction
import com.intellij.lang.typescript.formatter.TypeScriptCodeStyleSettings
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Pair
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.ResolveResult
import com.intellij.psi.SmartPsiElementPointer
import com.intellij.refactoring.suggested.createSmartPointer
import org.angular2.entities.Angular2ComponentLocator
import org.angular2.inspections.quickfixes.Angular2FixesFactory
import org.angular2.lang.Angular2Bundle
import org.angular2.lang.expr.psi.Angular2PipeReferenceExpression

class Angular2AnalysisHandlersFactory : TypeScriptAnalysisHandlersFactory() {

  override fun getInspectionSuppressor(): InspectionSuppressor {
    return Angular2InspectionSuppressor
  }

  override fun getReferenceChecker(reporter: JSProblemReporter<*>): JSReferenceChecker {
    return object : TypeScriptReferenceChecker(reporter) {
      override fun addCreateFromUsageFixesForCall(methodExpression: JSReferenceExpression,
                                                  isNewExpression: Boolean,
                                                  resolveResults: Array<ResolveResult>,
                                                  quickFixes: MutableList<in LocalQuickFix>) {
        if (methodExpression is Angular2PipeReferenceExpression) {
          // TODO Create pipe from usage
          return
        }
        val qualifier = methodExpression.qualifier
        if (qualifier == null || qualifier is JSThisExpression) {
          val componentClass = Angular2ComponentLocator.findComponentClass(methodExpression)
          if (componentClass != null && methodExpression.referenceName != null) {
            quickFixes.add(CreateComponentMethodIntentionAction(methodExpression))
          }
          return
        }
        super.addCreateFromUsageFixesForCall(methodExpression, isNewExpression, resolveResults, quickFixes)
      }

      @InspectionMessage
      override fun createUnresolvedCallReferenceMessage(methodExpression: JSReferenceExpression, isNewExpression: Boolean): String {
        return if (methodExpression is Angular2PipeReferenceExpression) {
          Angular2Bundle.message("angular.inspection.unresolved-pipe.message", methodExpression.getReferenceName()!!)
        }
        else super.createUnresolvedCallReferenceMessage(methodExpression, isNewExpression)
      }

      override fun reportUnresolvedReference(resolveResults: Array<ResolveResult>,
                                             referenceExpression: JSReferenceExpression,
                                             quickFixes: MutableList<LocalQuickFix>,
                                             @InspectionMessage message: String,
                                             isFunction: Boolean,
                                             inTypeContext: Boolean) {
        if (referenceExpression is Angular2PipeReferenceExpression) {
          Angular2FixesFactory.addUnresolvedDeclarationFixes(referenceExpression, quickFixes)
          // todo reject core TS quickFixes
        }
        super.reportUnresolvedReference(resolveResults, referenceExpression, quickFixes, message, isFunction, inTypeContext)
      }

      override fun addCreateFromUsageFixes(referenceExpression: JSReferenceExpression,
                                           resolveResults: Array<ResolveResult>,
                                           quickFixes: MutableList<in LocalQuickFix>,
                                           inTypeContext: Boolean,
                                           ecma: Boolean): Boolean {
        val qualifier = referenceExpression.qualifier
        if (qualifier == null || qualifier is JSThisExpression) {
          val componentClass = Angular2ComponentLocator.findComponentClass(referenceExpression)
          if (componentClass != null && referenceExpression.referenceName != null) {
            quickFixes.add(CreateComponentFieldIntentionAction(referenceExpression))
          }
          return inTypeContext
        }
        return super.addCreateFromUsageFixes(referenceExpression, resolveResults, quickFixes, inTypeContext, ecma)
      }
    }
  }

  private class CreateComponentFieldIntentionAction(referenceExpression: JSReferenceExpression)
    : CreateJSVariableIntentionAction(referenceExpression.referenceName, true, false, false) {

    private val myRefExpressionPointer: SmartPsiElementPointer<JSReferenceExpression>

    init {
      myRefExpressionPointer = createPointerFor(referenceExpression)
    }

    override fun applyFix(project: Project, psiElement: PsiElement, file: PsiFile, editor: Editor?) {
      val componentClass = Angular2ComponentLocator.findComponentClass(psiElement)!!
      doApplyFix(project, componentClass, componentClass.containingFile, null)
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
      return Pair.create(myRefExpressionPointer.element, psiElement.lastChild)
    }

    override fun addAccessModifier(template: Template,
                                   referenceExpression: JSReferenceExpression,
                                   staticContext: Boolean,
                                   targetClass: JSClass) {
      addClassMemberModifiers(template, staticContext, targetClass)
    }
  }

  private class CreateComponentMethodIntentionAction(methodExpression: JSReferenceExpression)
    : CreateJSFunctionIntentionAction(methodExpression.referenceName, true, false) {

    private val myRefExpressionPointer: SmartPsiElementPointer<JSReferenceExpression>

    init {
      myRefExpressionPointer = createPointerFor(methodExpression)
    }

    override fun applyFix(project: Project, psiElement: PsiElement, file: PsiFile, editor: Editor?) {
      val componentClass = Angular2ComponentLocator.findComponentClass(psiElement)!!
      doApplyFix(project, componentClass, componentClass.containingFile, null)
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
      return Pair.create(myRefExpressionPointer.element, psiElement.lastChild)
    }

    override fun writeFunctionAndName(template: Template,
                                      createdMethodName: String,
                                      anchorParent: PsiElement,
                                      clazz: PsiElement?,
                                      referenceExpression: JSReferenceExpression) {
      val actualName = if (referenceExpression.qualifier is JSThisExpression) {
        referenceExpression.referenceName ?: createdMethodName
      }
      else {
        createdMethodName
      }
      template.addTextSegment(JSClassUtils.createClassFunctionName(actualName, anchorParent))
    }

    override fun addAccessModifier(template: Template,
                                   referenceExpression: JSReferenceExpression,
                                   staticContext: Boolean,
                                   targetClass: JSClass) {
      addClassMemberModifiers(template, staticContext, targetClass)
    }
  }

  companion object {

    private fun addClassMemberModifiers(template: Template, staticContext: Boolean, targetClass: JSClass) {
      if (DialectDetector.isTypeScript(targetClass)) {
        if (TypeScriptCodeStyleSettings.getTypeScriptSettings(targetClass).USE_PUBLIC_MODIFIER) {
          template.addTextSegment("public ")
        }
        if (staticContext) {
          template.addTextSegment("static ")
        }
      }
    }

    private fun createPointerFor(methodExpression: JSReferenceExpression): SmartPsiElementPointer<JSReferenceExpression> {
      return methodExpression.createSmartPointer()
    }
  }
}
