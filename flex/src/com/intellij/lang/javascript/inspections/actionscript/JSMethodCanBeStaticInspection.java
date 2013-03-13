/*
 * Copyright 2000-2006 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.intellij.lang.javascript.inspections.actionscript;

import com.intellij.codeInspection.LocalInspectionToolSession;
import com.intellij.codeInspection.LocalQuickFix;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.lang.ASTNode;
import com.intellij.lang.javascript.JSBundle;
import com.intellij.lang.javascript.JSElementTypes;
import com.intellij.lang.javascript.index.JSNamedElementIndexItemBase;
import com.intellij.lang.javascript.index.JSNamedElementProxy;
import com.intellij.lang.javascript.inspections.JSInspection;
import com.intellij.lang.javascript.psi.*;
import com.intellij.lang.javascript.psi.ecmal4.JSAttributeList;
import com.intellij.lang.javascript.psi.ecmal4.JSAttributeListOwner;
import com.intellij.lang.javascript.psi.ecmal4.JSClass;
import com.intellij.lang.javascript.psi.ecmal4.JSSuperExpression;
import com.intellij.lang.javascript.psi.impl.JSFunctionBaseImpl;
import com.intellij.lang.javascript.psi.resolve.ImplicitJSVariableImpl;
import com.intellij.lang.javascript.psi.resolve.JSInheritanceUtil;
import com.intellij.lang.javascript.psi.resolve.JSResolveUtil;
import com.intellij.lang.javascript.validation.fixes.MakeMethodStaticFix;
import com.intellij.openapi.extensions.Extensions;
import com.intellij.openapi.util.Condition;
import com.intellij.openapi.util.Ref;
import com.intellij.psi.PsiElement;
import gnu.trove.THashSet;
import org.jetbrains.annotations.NotNull;

/**
 * @by Maxim.Mossienko
 */
public class JSMethodCanBeStaticInspection extends JSInspection {
  @NotNull
  public String getDisplayName() {
    return JSBundle.message("js.method.can.be.static.inspection.name");
  }

  @NotNull
  protected JSElementVisitor createVisitor(final ProblemsHolder holder, LocalInspectionToolSession session) {
    return new JSElementVisitor() {
      Condition<PsiElement>[] addins;

      @Override
      public void visitJSFunctionDeclaration(final JSFunction function) {
        final PsiElement parent = JSResolveUtil.findParent(function);
        if (!(parent instanceof JSClass)) return;
        final JSClass clazz = (JSClass)parent;
        if (clazz.isInterface()) return;
        final ASTNode nameIdentifier = function.findNameIdentifier();
        if (nameIdentifier == null) return;

        final JSAttributeList attributeList = function.getAttributeList();
        if (attributeList == null ||
            attributeList.hasModifier(JSAttributeList.ModifierType.STATIC) ||
            attributeList.hasModifier(JSAttributeList.ModifierType.OVERRIDE)
           ) {
          return;
        }

        if (function.isConstructor()) return;

        if (!attributeList.hasModifier(JSAttributeList.ModifierType.FINAL) &&
            attributeList.getAccessType() != JSAttributeList.AccessType.PRIVATE) {
          final JSAttributeList classAttributeList = clazz.getAttributeList();
          if (classAttributeList != null && classAttributeList.findAttributeByName("Abstract") != null) {
            return;
          }
        }

        if (addins == null) {
          addins = Extensions.getRootArea().<Condition<PsiElement>>getExtensionPoint("com.intellij.cantBeStatic").getExtensions();
        }

        for (Condition<PsiElement> addin : addins) {
          if (addin.value(function)) return;
        }

        boolean isEmpty = false;
        final JSSourceElement[] body = function.getBody();
        if (body.length == 0) {
          isEmpty = true;
        } else if (body[0] instanceof JSBlockStatement) {
          isEmpty = body[0].getNode().findChildByType(JSElementTypes.SOURCE_ELEMENTS) == null;
        }

        if (isEmpty) return;

        final Ref<Boolean> dependsOnInstance = new Ref<Boolean>();
        function.acceptChildren(new JSRecursiveElementVisitor() {
          @Override
          public void visitJSThisExpression(JSThisExpression node) {
            dependsOnInstance.set(Boolean.TRUE);
          }

          @Override
          public void visitJSSuperExpression(JSSuperExpression superExpression) {
            dependsOnInstance.set(Boolean.TRUE);
          }

          @Override
          public void visitJSReferenceExpression(JSReferenceExpression node) {
            if (node.getQualifier() == null && !JSResolveUtil.isSelfReference(node)) {
              final PsiElement resolve = node.resolve();

              if (resolve instanceof ImplicitJSVariableImpl &&
                  ("hostComponent".equals(((ImplicitJSVariableImpl)resolve).getName()) ||
                   "outerDocument".equals(((ImplicitJSVariableImpl)resolve).getName()))) {
                dependsOnInstance.set(Boolean.TRUE);
              }
              else if (resolve instanceof JSAttributeListOwner && !(resolve instanceof JSClass)) {
                final JSAttributeList resolvedMemberAttrList = ((JSAttributeListOwner)resolve).getAttributeList();
                if (resolvedMemberAttrList != null &&
                    !resolvedMemberAttrList.hasModifier(JSAttributeList.ModifierType.STATIC) &&
                    ( !(resolve instanceof JSFunction) || !((JSFunction)resolve).isConstructor()) &&
                    resolve != function
                   ) {
                  final PsiElement resolvedMemberParent = JSResolveUtil.findParent(resolve);

                  if (resolvedMemberParent instanceof JSClass) {
                    dependsOnInstance.set(Boolean.TRUE);
                  }
                }
              } else if (resolve instanceof JSNamedElementProxy &&
                         ((JSNamedElementProxy)resolve).getType() == JSNamedElementIndexItemBase.NamedItemType.AttributeValue
                        ) {
                dependsOnInstance.set(Boolean.TRUE);
              }
            }
            super.visitJSReferenceExpression(node);
          }

          @Override
          public void visitJSFunctionExpression(JSFunctionExpression node) {
            checkFunForExternals((JSFunctionBaseImpl)node);
          }

          private void checkFunForExternals(JSFunctionBaseImpl node) {
            THashSet<String> usedExternalVars = new THashSet<String>();
            node.addReferencedExternalNames(usedExternalVars);
            if (usedExternalVars.size() > 0) dependsOnInstance.set(Boolean.TRUE);
          }

          @Override
          public void visitJSFunctionDeclaration(JSFunction node) {
            checkFunForExternals((JSFunctionBaseImpl)node);
          }

          @Override
          public void visitElement(PsiElement element) {
            if (dependsOnInstance.get() != null) return;
            super.visitElement(element);
          }
        });

        if (dependsOnInstance.get() == null && !JSInheritanceUtil.participatesInHierarchy(function)) {
          LocalQuickFix[] fixes = holder.isOnTheFly() ? new LocalQuickFix[]{new MakeMethodStaticFix()} : LocalQuickFix.EMPTY_ARRAY;
          holder.registerProblem(nameIdentifier.getPsi(), JSBundle.message("js.method.can.be.static"), fixes);
        }
      }
    };
  }
}
