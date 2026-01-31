// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.angular2.inspections.quickfixes

import com.intellij.codeInsight.template.Template
import com.intellij.lang.javascript.dialects.JSDialectSpecificHandlersFactory
import com.intellij.lang.javascript.psi.JSCallExpression
import com.intellij.lang.javascript.psi.JSRecursiveWalkingElementVisitor
import com.intellij.lang.javascript.psi.JSReferenceExpression
import com.intellij.lang.javascript.psi.JSType
import com.intellij.lang.javascript.psi.JSTypeUtils
import com.intellij.lang.javascript.psi.resolve.JSResolveUtil
import com.intellij.lang.javascript.psi.types.JSCompositeTypeFactory
import com.intellij.lang.javascript.psi.types.JSTypeSource
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Pair
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.xml.XmlAttribute
import com.intellij.util.SmartList
import com.intellij.util.asSafely
import org.angular2.Angular2DecoratorUtil.OUTPUT_DEC
import org.angular2.codeInsight.template.Angular2StandardSymbolsScopesProvider
import org.angular2.lang.Angular2Bundle
import org.angular2.lang.Angular2LangUtil.ANGULAR_CORE_PACKAGE
import org.angular2.lang.Angular2LangUtil.EVENT_EMITTER
import org.angular2.lang.expr.psi.Angular2Action

class CreateDirectiveOutputIntentionAction
  : BaseCreateDirectiveInputOutputAction {

  constructor(reference: JSReferenceExpression, referenceName: String) : super(reference, referenceName)

  constructor(xmlAttribute: XmlAttribute, referenceName: String) : super(xmlAttribute, referenceName)

  override fun isAvailable(project: Project?, element: PsiElement?, editor: Editor?, file: PsiFile?): Boolean =
    (element !is JSReferenceExpression || findEmitCallExpression(element) != null)
    && super.isAvailable(project, element, editor, file)

  override fun getName(): String {
    return Angular2Bundle.message("angular.quickfix.template.create-output.name", myReferencedName)
  }

  override fun calculateAnchors(psiElement: PsiElement): Pair<JSReferenceExpression?, PsiElement?> =
    myElementPointer.dereference()?.asSafely<JSCallExpression>()
      ?.let { Pair.create(it.methodExpression as JSReferenceExpression, psiElement.lastChild) }
    ?: super.calculateAnchors(psiElement)

  override fun buildTemplate(template: Template,
                             referenceExpression: JSReferenceExpression?,
                             isStaticContext: Boolean,
                             anchorParent: PsiElement) {
    addDecoratedField(template, OUTPUT_DEC)
    template.addTextSegment(" = new EventEmitter<")

    addTypeSegment(template)
    template.addTextSegment(">()")
    addSemicolonSegment(template, anchorParent)

    Angular2FixesPsiUtil.insertJSImport(anchorParent, ANGULAR_CORE_PACKAGE, OUTPUT_DEC)
    Angular2FixesPsiUtil.insertJSImport(anchorParent, ANGULAR_CORE_PACKAGE, EVENT_EMITTER)
  }

  override fun inferType(context: PsiElement?): JSType? =
    when (context) {
      is JSReferenceExpression -> findEmitCallExpression(context)
        ?.arguments?.getOrNull(0)
        ?.let { JSResolveUtil.getElementJSType(it) }
      is XmlAttribute -> SmartList<JSType>().also { result ->
        Angular2Action.get(context)?.acceptChildren(object : JSRecursiveWalkingElementVisitor() {
          override fun visitJSReferenceExpression(node: JSReferenceExpression) {
            if (node.referenceName == Angular2StandardSymbolsScopesProvider.`$EVENT` && node.qualifier == null) {
              JSDialectSpecificHandlersFactory.findExpectedType(node)
                ?.takeIf { !JSTypeUtils.isAnyType(it) && !JSTypeUtils.isNullOrUndefinedType(it) }
                ?.let(result::add)
            }
            super.visitJSReferenceExpression(node)
          }
        })
      }.let { JSCompositeTypeFactory.createIntersectionType(it, JSTypeSource.EMPTY_TS_EXPLICITLY_DECLARED) }
      else -> null
    }

  private fun findEmitCallExpression(reference: JSReferenceExpression): JSCallExpression? =
    reference
      .parent.asSafely<JSReferenceExpression>()
      ?.takeIf { it.referenceName == "emit" }
      ?.parent?.asSafely<JSCallExpression>()
      ?.takeIf { it.argumentSize == 1 }
}