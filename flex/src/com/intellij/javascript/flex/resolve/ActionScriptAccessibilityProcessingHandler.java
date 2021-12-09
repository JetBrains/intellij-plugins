// Copyright 2000-2021 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.intellij.javascript.flex.resolve;

import com.intellij.lang.actionscript.psi.ActionScriptPsiImplUtil;
import com.intellij.lang.javascript.index.JSLocalNamespaceEvaluator;
import com.intellij.lang.javascript.psi.JSElement;
import com.intellij.lang.javascript.psi.JSExpression;
import com.intellij.lang.javascript.psi.JSPsiElementBase;
import com.intellij.lang.javascript.psi.JSReferenceExpression;
import com.intellij.lang.javascript.psi.ecmal4.JSAttributeList;
import com.intellij.lang.javascript.psi.ecmal4.JSAttributeListOwner;
import com.intellij.lang.javascript.psi.ecmal4.JSClass;
import com.intellij.lang.javascript.psi.resolve.*;
import com.intellij.psi.PsiElement;
import com.intellij.psi.ResolveResult;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Konstantin.Ulitin
 */
public class ActionScriptAccessibilityProcessingHandler extends AccessibilityProcessingHandler {

  private static final String AS3_NAMESPACE_VALUE = "http://adobe.com/AS3/2006/builtin";
  private String myTypeName;
  private Map<String, String> openedNses;
  private boolean defaultNsIsNotAllowed;
  private boolean anyNsAllowed;

  public ActionScriptAccessibilityProcessingHandler(@Nullable PsiElement _place, boolean skipNsResolving) {
    super(_place);
    allowUnqualifiedStaticsFromInstance = place instanceof JSReferenceExpression && ((JSReferenceExpression)place).getQualifier() == null;
    if (place instanceof JSReferenceExpression) {
      final JSReferenceExpression namespace = ((JSReferenceExpression)place).getNamespaceElement();

      // TODO: e.g. protected is also ns
      if (namespace != null) {
        String ns = skipNsResolving ? namespace.getText() : ActionScriptPsiImplUtil.calcNamespaceReference(place);
        if (ns != null) {
          openedNses = new HashMap<>(1);
          openedNses.put(ns, null);
          defaultNsIsNotAllowed = true;
        }
        else {
          anyNsAllowed = true;
        }
      }
    }
  }

  @Override
  public void setTypeName(final String qualifiedName) {
    myTypeName = qualifiedName;
  }

  @Override
  protected boolean acceptsForMembersVisibility(@NotNull JSPsiElementBase element, @NotNull SinkResolveProcessor resolveProcessor) {
    if (!(element instanceof JSAttributeListOwner)) return true;
    final JSAttributeList attributeList = ((JSAttributeListOwner)element).getAttributeList();

    if (JSResolveUtil.getClassOfContext(place) != JSResolveUtil.getClassOfContext(element)) {
      if (!acceptPrivateMembers) {
        if (attributeList != null && attributeList.getAccessType() == JSAttributeList.AccessType.PRIVATE) {
          resolveProcessor.addPossibleCandidateResult(element, JSResolveResult.PRIVATE_MEMBER_IS_NOT_ACCESSIBLE);
          return false;
        }
      }

      if (!acceptProtectedMembers) {
        if (attributeList != null &&
            attributeList.getAccessType() == JSAttributeList.AccessType.PROTECTED
          ) {
          // we are resolving in context of the class or element within context of the class
          if ((myClassScopes != null || isParentClassContext(element))) {
            resolveProcessor.addPossibleCandidateResult(element, JSResolveResult.PROTECTED_MEMBER_IS_NOT_ACCESSIBLE);
            return false;
          } // if element / context out of class then protected element is ok due to includes
        }
      }
    }

    PsiElement elt = JSResolveUtil.findParent(element);

    if (processStatics) {
      if ((attributeList == null || !attributeList.hasModifier(JSAttributeList.ModifierType.STATIC))) {
        if (JSResolveUtil.PROTOTYPE_FIELD_NAME.equals(resolveProcessor.getName())) return true;
        resolveProcessor.addPossibleCandidateResult(element, JSResolveResult.INSTANCE_MEMBER_INACCESSIBLE);
        return false;
      }
      if (myTypeName != null && elt instanceof JSClass && !myTypeName.equals(((JSClass)elt).getQualifiedName())) {
        // static members are inherited in TypeScript classes
        resolveProcessor.addPossibleCandidateResult(element, JSResolveResult.STATIC_MEMBER_INACCESSIBLE);
        return false;
      }
    }
    else if (myClassDeclarationStarted && !allowUnqualifiedStaticsFromInstance) {
      if (attributeList != null && attributeList.hasModifier(JSAttributeList.ModifierType.STATIC)) {
        boolean referencingClass = false;

        if (place instanceof JSReferenceExpression) {
          JSExpression qualifier = ((JSReferenceExpression)place).getQualifier();
          if (qualifier instanceof JSReferenceExpression) {
            JSElement expression = JSLocalNamespaceEvaluator.calcRefExprValue((JSReferenceExpression)qualifier);
            if (expression instanceof JSReferenceExpression && expression != qualifier) {
              for (ResolveResult r : ((JSReferenceExpression)expression).multiResolve(false)) {
                PsiElement rElement = r.getElement();
                if (rElement instanceof JSClass) {
                  referencingClass = true;
                  break;
                }
              }
            }
          }
        }
        if (!referencingClass) {
          resolveProcessor.addPossibleCandidateResult(element, JSResolveResult.STATIC_MEMBER_INACCESSIBLE);
          return false;
        }
      }
    }

    if (processActionScriptNotAllowedNsAttributes(element, resolveProcessor, attributeList)) return false;
    return true;
  }

  private boolean processActionScriptNotAllowedNsAttributes(@NotNull PsiElement element,
                                                            @NotNull SinkResolveProcessor resolveProcessor,
                                                            @Nullable JSAttributeList attributeList) {
    if (!resolveProcessor.getResultSink().isActionScript()) return false;

    String attributeNs = ActionScriptPsiImplUtil.getNamespace(attributeList);
    if (attributeNs != null) {
      String resolvedNs = ActionScriptPsiImplUtil.resolveNamespaceValue(attributeList);
      if (resolvedNs == null && !resolveProcessor.getResultSink().isActionScript()) {
        resolvedNs = attributeNs; // AS3
      }
      attributeNs = resolvedNs;
  }

    if (openedNses == null && attributeNs != null) {
      if (!anyNsAllowed &&
          place instanceof JSReferenceExpression &&
          !JSResolveUtil.isExprInTypeContext((JSReferenceExpression)place)
        ) {
        openedNses = ActionScriptResolveUtil.calculateOpenNses(place);
      }
    }

    if (openedNses != null &&
        !openedNses.containsKey(attributeNs) &&
        !AS3_NAMESPACE_VALUE.equals(attributeNs) &&
        !ActionScriptResolveUtil.AS3_NAMESPACE.equals(attributeNs) // AS3 is opened by default from compiler settings and for JavaScript symbols
      ) {
      if (attributeNs != null || defaultNsIsNotAllowed) {
        resolveProcessor.addPossibleCandidateResult(element, JSResolveResult.MEMBER_FROM_UNOPENED_NAMESPACE);
        return true;
      }
    }
    return false;
  }

  @Override
  protected boolean checkConstructorWithNew(@NotNull PsiElement element, @NotNull SinkResolveProcessor resolveProcessor) {
    return true;
  }
}
