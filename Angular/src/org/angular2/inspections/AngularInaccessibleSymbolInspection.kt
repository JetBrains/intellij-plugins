// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.inspections

import com.intellij.codeInspection.LocalInspectionToolSession
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.lang.javascript.evaluation.JSTypeEvaluationLocationProvider.withTypeEvaluationLocation
import com.intellij.lang.javascript.inspections.JSInspection
import com.intellij.lang.javascript.presentable.JSFormatUtil
import com.intellij.lang.javascript.presentable.JSNamedElementPresenter
import com.intellij.lang.javascript.psi.JSElement
import com.intellij.lang.javascript.psi.JSFunction
import com.intellij.lang.javascript.psi.JSReferenceExpression
import com.intellij.lang.javascript.psi.JSThisExpression
import com.intellij.lang.javascript.psi.ecma6.TypeScriptClass
import com.intellij.lang.javascript.psi.ecmal4.JSAttributeList
import com.intellij.lang.javascript.psi.ecmal4.JSAttributeList.AccessType
import com.intellij.lang.javascript.psi.ecmal4.JSAttributeListOwner
import com.intellij.lang.javascript.refactoring.JSVisibilityUtil.getPresentableAccessModifier
import com.intellij.lang.javascript.validation.fixes.JSRemoveReadonlyModifierFix
import com.intellij.openapi.util.text.StringUtil.capitalize
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiElementVisitor
import com.intellij.psi.PsiNamedElement
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.psi.util.parentOfType
import com.intellij.util.asSafely
import com.intellij.webSymbols.PsiSourcedWebSymbol
import com.intellij.webSymbols.utils.qualifiedKind
import com.intellij.webSymbols.utils.unwrapMatchedSymbols
import org.angular2.codeInsight.Angular2HighlightingUtils.TextAttributesKind.TS_KEYWORD
import org.angular2.codeInsight.Angular2HighlightingUtils.htmlName
import org.angular2.codeInsight.Angular2HighlightingUtils.withColor
import org.angular2.codeInsight.Angular2HighlightingUtils.withNameColor
import org.angular2.codeInsight.attributes.Angular2AttributeDescriptor
import org.angular2.codeInsight.config.Angular2Compiler
import org.angular2.codeInsight.config.Angular2Compiler.isStrictTemplates
import org.angular2.entities.source.Angular2SourceUtil
import org.angular2.inspections.quickfixes.AngularChangeModifierQuickFix
import org.angular2.lang.Angular2Bundle
import org.angular2.lang.expr.Angular2Language
import org.angular2.lang.expr.psi.Angular2ElementVisitor
import org.angular2.lang.html.Angular2HtmlLanguage
import org.angular2.lang.html.psi.Angular2HtmlPropertyBinding
import org.angular2.lang.html.psi.PropertyBindingType
import org.angular2.signals.Angular2SignalUtils
import org.angular2.web.NG_DIRECTIVE_INPUTS

class AngularInaccessibleSymbolInspection : JSInspection() {

  override fun getElementsToOptimizeForTSServiceHighlighting(): Set<Class<out PsiElement>> =
    setOf(JSReferenceExpression::class.java, Angular2HtmlPropertyBinding::class.java)

  override fun createVisitor(holder: ProblemsHolder, session: LocalInspectionToolSession): PsiElementVisitor {
    val fileLang = holder.file.language
    if (fileLang.isKindOf(Angular2HtmlLanguage) || Angular2Language.`is`(fileLang)) {
      return object : Angular2ElementVisitor() {

        override fun visitElement(element: PsiElement) {
          when (element) {
            is JSReferenceExpression -> checkReference(element)
            is Angular2HtmlPropertyBinding -> checkPropertyBinding(element)
          }
          super.visitElement(element)
        }

        private fun checkReference(node: JSReferenceExpression) {
          if (node.qualifier == null || node.qualifier is JSThisExpression) {
            val resolved = node.resolve()
            val clazz = PsiTreeUtil.getContextOfType(resolved, TypeScriptClass::class.java)
            if (clazz != null && resolved is JSElement && !isAccessible(resolved, AccessType.PROTECTED)) {
              holder.registerProblem(
                node.referenceNameElement ?: node,
                Angular2Bundle.htmlMessage(
                  if (isStrictTemplates(clazz))
                    "angular.inspection.inaccessible-symbol.strict.private.message"
                  else
                    "angular.inspection.inaccessible-symbol.aot.message",
                  capitalize(getKind(resolved)), getHtmlName(resolved), getHtmlAccessModifier(resolved), clazz.htmlName
                ),
                AngularChangeModifierQuickFix(AccessType.PROTECTED))
            }
          }
        }

        private fun checkPropertyBinding(element: Angular2HtmlPropertyBinding) {
          if (element.bindingType == PropertyBindingType.PROPERTY) {
            if (!Angular2Compiler.isStrictInputAccessModifiers(element)) return
            val inputElements = getInputSourceElements(element)
            val owner = Angular2SourceUtil.findComponentClass(element)
                        ?: return
            for (input in inputElements) {
              val accessType = input.attributeList?.accessType
              val inputOwner = input.parentOfType<TypeScriptClass>() ?: return
              val minAccessType = if (inputOwner == owner) AccessType.PROTECTED else AccessType.PUBLIC
              if (!isAccessible(input, minAccessType)) {
                holder.registerProblem(
                  element.nameElement,
                  Angular2Bundle.htmlMessage(
                    if (accessType == AccessType.PRIVATE)
                      "angular.inspection.inaccessible-symbol.strict.private.message"
                    else
                      "angular.inspection.inaccessible-symbol.strict.protected.message",
                    capitalize(getKind(input)), getHtmlName(input), getHtmlAccessModifier(input), inputOwner.htmlName
                  ),
                  AngularChangeModifierQuickFix(minAccessType, inputOwner.name))
              }
              else if (input.attributeList?.hasModifier(JSAttributeList.ModifierType.READONLY) == true
                       && !withTypeEvaluationLocation(element) { Angular2SignalUtils.isSignal(input, null) }) {
                holder.registerProblem(
                  element.nameElement,
                  Angular2Bundle.htmlMessage(
                    "angular.inspection.inaccessible-symbol.strict.read-only.message",
                    getHtmlName(input), getHtmlAccessModifier(input), inputOwner.htmlName),
                  JSRemoveReadonlyModifierFix(input)
                )
              }
            }
          }
        }
      }
    }
    return PsiElementVisitor.EMPTY_VISITOR
  }

}

fun getInputSourceElements(element: Angular2HtmlPropertyBinding): List<JSAttributeListOwner> =
  element.descriptor?.asSafely<Angular2AttributeDescriptor>()?.symbol
    ?.unwrapMatchedSymbols()
    ?.filter { it.qualifiedKind == NG_DIRECTIVE_INPUTS }
    ?.filterIsInstance<PsiSourcedWebSymbol>()
    ?.mapNotNull { it.source }
    ?.filterIsInstance<JSAttributeListOwner>()
    ?.toList()
  ?: emptyList()

fun isAccessible(member: PsiElement?, minAccessType: AccessType): Boolean {
  if (member is JSAttributeListOwner && !(member is JSFunction && member.isConstructor)) {
    val attributes = member.attributeList ?: return true
    val accessType = attributes.accessType
    return attributes.hasModifier(JSAttributeList.ModifierType.STATIC)
           || minAccessType.level <= accessType.level
  }
  return true
}

private val AccessType.level
  get() = when (this) {
    AccessType.PACKAGE_LOCAL -> 1
    AccessType.PUBLIC -> 2
    AccessType.PRIVATE -> 0
    AccessType.PROTECTED -> 1
  }

private fun getKind(member: PsiElement): String {
  return JSNamedElementPresenter(member).describeElementKind()
}

private fun getHtmlAccessModifier(member: JSElement): String {
  return (getPresentableAccessModifier(member)?.text ?: "").withColor(TS_KEYWORD, member)
}

private fun getHtmlName(member: PsiElement): String {
  return (member.asSafely<PsiNamedElement>()?.name
          ?: JSFormatUtil.getAnonymousElementPresentation()).withNameColor(member)
}
