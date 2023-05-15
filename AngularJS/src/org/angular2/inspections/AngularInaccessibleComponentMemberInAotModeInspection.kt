// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.inspections

import com.intellij.codeInspection.LocalInspectionTool
import com.intellij.codeInspection.LocalInspectionToolSession
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.lang.javascript.DialectDetector
import com.intellij.lang.javascript.presentable.JSFormatUtil
import com.intellij.lang.javascript.presentable.JSNamedElementPresenter
import com.intellij.lang.javascript.psi.*
import com.intellij.lang.javascript.psi.ecma6.TypeScriptClass
import com.intellij.lang.javascript.psi.ecmal4.JSAttributeList
import com.intellij.lang.javascript.psi.ecmal4.JSAttributeListOwner
import com.intellij.lang.javascript.refactoring.JSVisibilityUtil.getPresentableAccessModifier
import com.intellij.openapi.util.text.StringUtil.capitalize
import com.intellij.psi.*
import com.intellij.psi.search.LocalSearchScope
import com.intellij.psi.search.searches.ReferencesSearch
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.util.AstLoadingFilter
import com.intellij.util.asSafely
import org.angular2.entities.Angular2EntitiesProvider
import org.angular2.inspections.quickfixes.AngularMakePublicQuickFix
import org.angular2.lang.Angular2Bundle
import org.angular2.lang.Angular2LangUtil
import org.angular2.lang.expr.Angular2Language
import org.angular2.lang.expr.psi.Angular2ElementVisitor
import org.angular2.lang.html.Angular2HtmlLanguage

class AngularInaccessibleComponentMemberInAotModeInspection : LocalInspectionTool() {

  override fun buildVisitor(holder: ProblemsHolder,
                            isOnTheFly: Boolean,
                            session: LocalInspectionToolSession): PsiElementVisitor {
    val fileLang = holder.file.language
    if (fileLang.isKindOf(Angular2HtmlLanguage.INSTANCE) || Angular2Language.INSTANCE.`is`(fileLang)) {
      return object : Angular2ElementVisitor() {
        override fun visitJSReferenceExpression(node: JSReferenceExpression) {
          if (node.qualifier == null || node.qualifier is JSThisExpression) {
            val resolved = node.resolve()
            val clazz = PsiTreeUtil.getContextOfType(resolved, TypeScriptClass::class.java)
            if (clazz != null && resolved is JSElement && accept(resolved)) {

              holder.registerProblem(
                node.referenceNameElement ?: node,
                capitalize(Angular2Bundle.message("angular.inspection.aot-inaccessible-member.message.template-symbol",
                                                  getAccessModifier(resolved), getKind(resolved), getName(resolved))),

                AngularMakePublicQuickFix())
            }
          }
        }
      }
    }
    else if (DialectDetector.isTypeScript(holder.file) && Angular2LangUtil.isAngular2Context(holder.file)) {
      return object : JSElementVisitor() {
        override fun visitTypeScriptClass(typeScriptClass: TypeScriptClass) {
          val component = Angular2EntitiesProvider.getComponent(typeScriptClass)
          val template = component?.templateFile ?: return
          val candidates = HashSet<JSElement>()
          for (member in typeScriptClass.members) {
            if (accept(member)) {
              candidates.add(member)
            }
          }
          retainReferenced(template, candidates)
          for (member in candidates) {
            holder.registerProblem(
              member.asSafely<PsiNameIdentifierOwner>()?.nameIdentifier ?: member,
              capitalize(Angular2Bundle.message("angular.inspection.aot-inaccessible-member.message.member",
                                                getAccessModifier(member), getKind(member), getName(member))),
              AngularMakePublicQuickFix())
          }
        }
      }
    }
    return PsiElementVisitor.EMPTY_VISITOR
  }

  companion object {

    fun accept(member: PsiElement?): Boolean {
      if (member is JSAttributeListOwner && !(member is JSFunction && member.isConstructor)) {
        val attributes = member.attributeList ?: return false
        val accessType = attributes.accessType
        return !attributes.hasModifier(JSAttributeList.ModifierType.STATIC)
               && accessType == JSAttributeList.AccessType.PRIVATE
      }
      return false
    }

    private fun getKind(member: PsiElement): String {
      return JSNamedElementPresenter(member).describeElementKind()
    }

    private fun getAccessModifier(member: JSElement): String {
      return getPresentableAccessModifier(member)?.text ?: ""
    }

    private fun getName(member: PsiElement): String {
      return member.asSafely<PsiNamedElement>()?.name
             ?: JSFormatUtil.getAnonymousElementPresentation()
    }

    private fun retainReferenced(template: PsiFile, candidates: MutableSet<out PsiElement>) {
      val fileScope = LocalSearchScope(template)
      val iterator = candidates.iterator()
      AstLoadingFilter.forceAllowTreeLoading<RuntimeException>(template) {
        while (iterator.hasNext()) {
          if (ReferencesSearch.search(iterator.next(), fileScope, true).findFirst() == null) {
            iterator.remove()
          }
        }
      }
    }
  }
}
