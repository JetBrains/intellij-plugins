// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.inspections

import com.intellij.codeInsight.intention.PriorityAction
import com.intellij.codeInsight.template.Expression
import com.intellij.codeInsight.template.Template
import com.intellij.codeInsight.template.impl.ConstantNode
import com.intellij.codeInspection.InspectionSuppressor
import com.intellij.codeInspection.LocalQuickFix
import com.intellij.codeInspection.util.InspectionMessage
import com.intellij.lang.ecmascript6.psi.impl.ES6ImportPsiUtil
import com.intellij.lang.javascript.DialectDetector
import com.intellij.lang.javascript.DialectOptionHolder
import com.intellij.lang.javascript.ecmascript6.TypeScriptAnalysisHandlersFactory
import com.intellij.lang.javascript.psi.JSExpression
import com.intellij.lang.javascript.psi.JSReferenceExpression
import com.intellij.lang.javascript.psi.JSThisExpression
import com.intellij.lang.javascript.psi.JSType
import com.intellij.lang.javascript.psi.JSType.TypeTextFormat.CODE
import com.intellij.lang.javascript.psi.ecmal4.JSClass
import com.intellij.lang.javascript.psi.util.JSClassUtils
import com.intellij.lang.javascript.validation.JSProblemReporter
import com.intellij.lang.javascript.validation.JSReferenceChecker
import com.intellij.lang.javascript.validation.JSTypeChecker
import com.intellij.lang.javascript.validation.TypeScriptReferenceChecker
import com.intellij.lang.javascript.validation.fixes.CreateJSFunctionIntentionAction
import com.intellij.lang.javascript.validation.fixes.CreateJSVariableIntentionAction
import com.intellij.lang.typescript.formatter.TypeScriptCodeStyleSettings
import com.intellij.lang.typescript.validation.TypeScriptTypeChecker
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Pair
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.ResolveResult
import com.intellij.psi.SmartPsiElementPointer
import com.intellij.refactoring.suggested.createSmartPointer
import com.intellij.util.ProcessingContext
import org.angular2.codeInsight.Angular2HighlightingUtils.TextAttributesKind.NG_PIPE
import org.angular2.codeInsight.Angular2HighlightingUtils.withColor
import org.angular2.entities.Angular2ComponentLocator
import org.angular2.inspections.quickfixes.Angular2FixesFactory
import org.angular2.lang.Angular2Bundle
import org.angular2.lang.expr.psi.Angular2PipeReferenceExpression
import org.angular2.lang.html.psi.Angular2HtmlPropertyBinding
import org.angular2.signals.Angular2SignalUtils
import org.angular2.signals.Angular2SignalUtils.SIGNAL_FUNCTION

class Angular2AnalysisHandlersFactory : TypeScriptAnalysisHandlersFactory() {

  override fun getInspectionSuppressor(): InspectionSuppressor {
    return Angular2InspectionSuppressor
  }

  override fun <T : Any?> getTypeChecker(problemReporter: JSProblemReporter<T>): JSTypeChecker =
    object : TypeScriptTypeChecker(problemReporter) {

      override fun getFixes(expr: JSExpression?,
                            declaredJSType: JSType,
                            elementToChangeTypeOf: PsiElement?,
                            expressionJSType: JSType,
                            context: ProcessingContext?,
                            holder: DialectOptionHolder?): Collection<LocalQuickFix> {
        val quickFixes = super.getFixes(expr, declaredJSType, elementToChangeTypeOf, expressionJSType, context, holder)
        if (elementToChangeTypeOf is Angular2HtmlPropertyBinding) {
          return Angular2FixesFactory.getCreateInputTransformFixes(elementToChangeTypeOf,
                                                                   expressionJSType.substitute().getTypeText(CODE)) + quickFixes
        }
        return quickFixes
      }
    }

  override fun getReferenceChecker(reporter: JSProblemReporter<*>): JSReferenceChecker =
    object : TypeScriptReferenceChecker(reporter) {
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
            if (Angular2SignalUtils.supportsSignals(componentClass)) {
              quickFixes.add(CreateComponentSignalIntentionAction(methodExpression))
            }
          }
          return
        }
        super.addCreateFromUsageFixesForCall(methodExpression, isNewExpression, resolveResults, quickFixes)
      }

      @InspectionMessage
      override fun createUnresolvedCallReferenceMessage(methodExpression: JSReferenceExpression, isNewExpression: Boolean): String {
        return if (methodExpression is Angular2PipeReferenceExpression) {
          Angular2Bundle.htmlMessage(
            "angular.inspection.unresolved-pipe.message",
            methodExpression.getReferenceName()!!.withColor(NG_PIPE, methodExpression)
          )
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

  private class CreateComponentFieldIntentionAction(referenceExpression: JSReferenceExpression)
    : CreateJSVariableIntentionAction(referenceExpression.referenceName, true, false, false) {

    private val myRefExpressionPointer: SmartPsiElementPointer<JSReferenceExpression> = createPointerFor(referenceExpression)

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

  private class CreateComponentSignalIntentionAction(methodExpression: JSReferenceExpression)
    : CreateJSVariableIntentionAction(methodExpression.referenceName, true, false, false) {

    private val myRefExpressionPointer: SmartPsiElementPointer<JSReferenceExpression> = createPointerFor(methodExpression)

    override fun applyFix(project: Project, psiElement: PsiElement, file: PsiFile, editor: Editor?) {
      val componentClass = Angular2ComponentLocator.findComponentClass(psiElement)!!
      doApplyFix(project, componentClass, componentClass.containingFile, null)
    }

    override fun getName(): String {
      return Angular2Bundle.message("angular.quickfix.template.create-signal.name", myReferencedName)
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
      return Pair.create(myRefExpressionPointer.element, psiElement.lastChild)
    }

    override fun addAccessModifier(template: Template,
                                   referenceExpression: JSReferenceExpression,
                                   staticContext: Boolean,
                                   targetClass: JSClass) {
      addClassMemberModifiers(template, staticContext, targetClass)
    }

    override fun buildTemplate(template: Template,
                               referenceExpression: JSReferenceExpression?,
                               isStaticContext: Boolean,
                               anchorParent: PsiElement) {
      template.addTextSegment(myReferencedName)
      template.addTextSegment(" = signal<")
      val type = guessTypeForExpression(referenceExpression, anchorParent, false)
      if (type == null) {
        addCompletionVar(template)
      }
      else {
        val expression: Expression = ConstantNode("$type | null")
        template.addVariable("\$TYPE$", expression, expression, true)
      }
      template.addTextSegment(">(")

      val expression: Expression = ConstantNode("null")
      template.addVariable("\$INITIAL_VALUE$", expression, expression, true)
      template.addTextSegment(")")
      addSemicolonSegment(template, anchorParent)

      ES6ImportPsiUtil.insertJSImport(anchorParent, SIGNAL_FUNCTION,
                                      Angular2SignalUtils.signalFunction(anchorParent) ?: return, null)
    }
  }

  private class CreateComponentMethodIntentionAction(methodExpression: JSReferenceExpression)
    : CreateJSFunctionIntentionAction(methodExpression.referenceName, true, false) {

    private val myRefExpressionPointer: SmartPsiElementPointer<JSReferenceExpression> = createPointerFor(methodExpression)

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
