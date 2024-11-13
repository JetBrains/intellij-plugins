// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.intellij.javascript.flex.resolve

import com.intellij.lang.actionscript.psi.ActionScriptPsiImplUtil
import com.intellij.lang.javascript.index.JSLocalNamespaceEvaluator
import com.intellij.lang.javascript.psi.JSPsiElementBase
import com.intellij.lang.javascript.psi.JSReferenceExpression
import com.intellij.lang.javascript.psi.ecmal4.JSAttributeList
import com.intellij.lang.javascript.psi.ecmal4.JSAttributeListOwner
import com.intellij.lang.javascript.psi.ecmal4.JSClass
import com.intellij.lang.javascript.psi.resolve.*
import com.intellij.psi.PsiElement

class ActionScriptAccessibilityProcessingHandler(_place: PsiElement?, skipNsResolving: Boolean) : AccessibilityProcessingHandler(_place) {
  private var myTypeName: String? = null
  private var openedNses: MutableMap<String?, String?>? = null
  private var defaultNsIsNotAllowed = false
  private var anyNsAllowed = false

  init {
    allowUnqualifiedStaticsFromInstance = place is JSReferenceExpression && (place as JSReferenceExpression).getQualifier() == null
    if (place is JSReferenceExpression) {
      val namespace = (place as JSReferenceExpression).getNamespaceElement()

      // TODO: e.g. protected is also ns
      if (namespace != null) {
        val ns = if (skipNsResolving) namespace.getText() else ActionScriptPsiImplUtil.calcNamespaceReference(place)
        if (ns != null) {
          openedNses = HashMap<String?, String?>(1)
          openedNses!!.put(ns, null)
          defaultNsIsNotAllowed = true
        }
        else {
          anyNsAllowed = true
        }
      }
    }
  }

  override fun setTypeName(qualifiedName: String?) {
    myTypeName = qualifiedName
  }

  override fun acceptsForMembersVisibility(element: JSPsiElementBase, resolveProcessor: SinkResolveProcessor<*>): Boolean {
    if (element !is JSAttributeListOwner) return true
    val attributeList = (element as JSAttributeListOwner).getAttributeList()

    if (JSResolveUtil.getClassOfContext(place) !== JSResolveUtil.getClassOfContext(element)) {
      if (!acceptPrivateMembers) {
        if (attributeList != null && attributeList.getAccessType() == JSAttributeList.AccessType.PRIVATE) {
          resolveProcessor.addPossibleCandidateResult(element, JSResolveResult.ProblemKind.PRIVATE_MEMBER_IS_NOT_ACCESSIBLE)
          return false
        }
      }

      if (!acceptProtectedMembers) {
        if (attributeList != null &&
            attributeList.getAccessType() == JSAttributeList.AccessType.PROTECTED
        ) {
          // we are resolving in context of the class or element within context of the class
          if ((myClassScopes != null || isParentClassContext(element))) {
            resolveProcessor.addPossibleCandidateResult(element, JSResolveResult.ProblemKind.PROTECTED_MEMBER_IS_NOT_ACCESSIBLE)
            return false
          } // if element / context out of class then protected element is ok due to includes
        }
      }
    }

    val elt = JSResolveUtil.findParent(element)

    if (isProcessStatics) {
      if ((attributeList == null || !attributeList.hasModifier(JSAttributeList.ModifierType.STATIC))) {
        if (JSResolveUtil.PROTOTYPE_FIELD_NAME == resolveProcessor.getName()) return true
        resolveProcessor.addPossibleCandidateResult(element, JSResolveResult.ProblemKind.INSTANCE_MEMBER_INACCESSIBLE)
        return false
      }
      if (myTypeName != null && elt is JSClass && (myTypeName != elt.getQualifiedName())) {
        // static members are inherited in TypeScript classes
        resolveProcessor.addPossibleCandidateResult(element, JSResolveResult.ProblemKind.STATIC_MEMBER_INACCESSIBLE)
        return false
      }
    }
    else if (myClassDeclarationStarted && !allowUnqualifiedStaticsFromInstance) {
      if (attributeList != null && attributeList.hasModifier(JSAttributeList.ModifierType.STATIC)) {
        var referencingClass = false

        if (place is JSReferenceExpression) {
          val qualifier = (place as JSReferenceExpression).getQualifier()
          if (qualifier is JSReferenceExpression) {
            val expression = JSLocalNamespaceEvaluator.calcRefExprValue(qualifier)
            if (expression is JSReferenceExpression && expression !== qualifier) {
              for (r in expression.multiResolve(false)) {
                val rElement = r.getElement()
                if (rElement is JSClass) {
                  referencingClass = true
                  break
                }
              }
            }
          }
        }
        if (!referencingClass) {
          resolveProcessor.addPossibleCandidateResult(element, JSResolveResult.ProblemKind.STATIC_MEMBER_INACCESSIBLE)
          return false
        }
      }
    }

    if (processActionScriptNotAllowedNsAttributes(element, resolveProcessor, attributeList)) return false
    return true
  }

  private fun processActionScriptNotAllowedNsAttributes(
    element: PsiElement,
    resolveProcessor: SinkResolveProcessor<*>,
    attributeList: JSAttributeList?
  ): Boolean {
    if (!resolveProcessor.getResultSink().isActionScript) return false

    var attributeNs = ActionScriptPsiImplUtil.getNamespace(attributeList)
    if (attributeNs != null) {
      var resolvedNs = ActionScriptPsiImplUtil.resolveNamespaceValue(attributeList)
      if (resolvedNs == null && !resolveProcessor.getResultSink().isActionScript) {
        resolvedNs = attributeNs // AS3
      }
      attributeNs = resolvedNs
    }

    if (openedNses == null && attributeNs != null) {
      if (!anyNsAllowed &&
          place is JSReferenceExpression && !JSResolveUtil.isExprInTypeContext(place as JSReferenceExpression)
      ) {
        openedNses = ActionScriptResolveUtil.calculateOpenNses(place)
      }
    }

    if (openedNses != null && !openedNses!!.containsKey(
        attributeNs) && (AS3_NAMESPACE_VALUE != attributeNs) && (ActionScriptResolveUtil.AS3_NAMESPACE != attributeNs) // AS3 is opened by default from compiler settings and for JavaScript symbols
    ) {
      if (attributeNs != null || defaultNsIsNotAllowed) {
        resolveProcessor.addPossibleCandidateResult(element, JSResolveResult.ProblemKind.MEMBER_FROM_UNOPENED_NAMESPACE)
        return true
      }
    }
    return false
  }

  override fun checkConstructorWithNew(element: PsiElement, resolveProcessor: SinkResolveProcessor<*>): Boolean {
    return true
  }

  companion object {
    private const val AS3_NAMESPACE_VALUE = "http://adobe.com/AS3/2006/builtin"
  }
}
