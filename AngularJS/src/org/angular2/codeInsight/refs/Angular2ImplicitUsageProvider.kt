// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.codeInsight.refs

import com.intellij.codeInsight.daemon.ImplicitUsageProvider
import com.intellij.lang.javascript.psi.JSFunction
import com.intellij.lang.javascript.psi.JSPsiElementBase
import com.intellij.lang.javascript.psi.ecma6.TypeScriptClass
import com.intellij.lang.javascript.psi.ecma6.TypeScriptField
import com.intellij.lang.javascript.psi.ecma6.TypeScriptFunction
import com.intellij.lang.javascript.psi.ecma6.impl.TypeScriptParameterImpl
import com.intellij.lang.javascript.psi.ecmal4.JSAttributeListOwner
import com.intellij.lang.javascript.psi.ecmal4.JSClass
import com.intellij.lang.javascript.psi.resolve.JSResolveUtil
import com.intellij.lang.javascript.psi.util.JSUtils
import com.intellij.openapi.util.Ref
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiReference
import com.intellij.psi.search.LocalSearchScope
import com.intellij.psi.search.searches.ReferencesSearch
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.util.AstLoadingFilter
import org.angular2.Angular2DecoratorUtil
import org.angular2.Angular2DecoratorUtil.COMPONENT_DEC
import org.angular2.Angular2DecoratorUtil.DIRECTIVE_DEC
import org.angular2.codeInsight.controlflow.Angular2ControlFlowBuilder.Companion.NG_TEMPLATE_CONTEXT_GUARD
import org.angular2.codeInsight.controlflow.Angular2ControlFlowBuilder.Companion.NG_TEMPLATE_GUARD_PREFIX
import org.angular2.entities.Angular2EntitiesProvider
import org.angular2.entities.Angular2EntityUtils.NG_ACCEPT_INPUT_TYPE_PREFIX
import org.angular2.lang.Angular2Bundle
import org.angular2.lang.Angular2LangUtil


class Angular2ImplicitUsageProvider : ImplicitUsageProvider {

  override fun isImplicitUsage(element: PsiElement): Boolean {
    if (element is TypeScriptFunction || element is TypeScriptField) {
      val name = (element as JSAttributeListOwner).name
      if (name != null) {
        if (NG_TEMPLATE_CONTEXT_GUARD == name || name.startsWith(NG_TEMPLATE_GUARD_PREFIX)) {
          val cls = JSUtils.getMemberContainingClass(element)
          return cls.hasDirectiveDecorator()
                 && (name == NG_TEMPLATE_CONTEXT_GUARD || cls.hasMember(name.substring(NG_TEMPLATE_GUARD_PREFIX.length)))
        }
        else if (element is TypeScriptField && name.startsWith(NG_ACCEPT_INPUT_TYPE_PREFIX)) {
          // https://angular.io/guide/template-typecheck#input-setter-coercion
          val cls = JSUtils.getMemberContainingClass(element)
          return cls.hasMember(name.substring(NG_ACCEPT_INPUT_TYPE_PREFIX.length)) && cls.hasDirectiveDecorator()
        }
      }
    }

    if (element is TypeScriptFunction) {
      if (element.isSetProperty || element.isGetProperty) {
        val theOtherOne = Ref<JSFunction>()
        Angular2ReferenceExpressionResolver.findPropertyAccessor(element, element.isGetProperty) { value -> theOtherOne.set(value) }
        if (!theOtherOne.isNull && Angular2DecoratorUtil.findDecorator(
            theOtherOne.get(), Angular2DecoratorUtil.INPUT_DEC, Angular2DecoratorUtil.OUTPUT_DEC) != null) {
          return true
        }
      }
    }

    // todo check: TypeScriptFunction case doesn't seem to be needed anymore, and with Ivy the other one might be needless too
    if (element is TypeScriptFunction
        || ((element is TypeScriptField || element is TypeScriptParameterImpl)
            && Angular2DecoratorUtil.isPrivateMember(element as JSPsiElementBase))) {
      val cls = JSUtils.getMemberContainingClass(element)
      if (cls is TypeScriptClass && Angular2LangUtil.isAngular2Context(element)) {
        val template = Angular2EntitiesProvider.getComponent(cls)?.templateFile
        if (template != null) {
          return isReferencedInTemplate(element, template)
        }
      }
    }
    return false
  }

  private fun JSClass.hasMember(name: String): Boolean =
    jsType.asRecordType().findPropertySignature(name) != null

  private fun JSClass.hasDirectiveDecorator(): Boolean =
    Angular2DecoratorUtil.findDecorator(this, COMPONENT_DEC, DIRECTIVE_DEC) != null

  private fun isReferencedInTemplate(node: PsiElement, template: PsiFile): Boolean {
    val predicate = { reference: PsiReference ->
      reference is PsiElement &&
      !(JSResolveUtil.isSelfReference(reference) || node is JSFunction && PsiTreeUtil.isAncestor(node, reference, false))
    }

    val scope = LocalSearchScope(arrayOf<PsiElement>(template),
                                 Angular2Bundle.message("angular.search-scope.template"),
                                 true)
    // TODO stub references in Angular templates
    return AstLoadingFilter.forceAllowTreeLoading<Boolean, RuntimeException>(template) {
      ReferencesSearch.search(node, scope, true).anyMatch(predicate)
    }
  }

  override fun isImplicitRead(element: PsiElement): Boolean {
    return false
  }

  override fun isImplicitWrite(element: PsiElement): Boolean {
    return false
  }
}
