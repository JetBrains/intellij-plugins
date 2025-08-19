// Copyright 2000-2025 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.intellij.javascript.flex;


import com.intellij.javascript.flex.resolve.ActionScriptFlexPsiImplUtil;
import com.intellij.javascript.flex.resolve.ActionScriptImportHandler;
import com.intellij.javascript.flex.resolve.ActionScriptResolveProcessor;
import com.intellij.javascript.flex.resolve.ActionScriptSinkResolveProcessor;
import com.intellij.lang.actionscript.psi.ActionScriptPsiImplUtil;
import com.intellij.lang.javascript.DialectDetector;
import com.intellij.lang.javascript.DialectOptionHolder;
import com.intellij.lang.javascript.dialects.JSDialectSpecificHandlersFactory;
import com.intellij.lang.javascript.flex.ActionScriptPsiExtensions;
import com.intellij.lang.javascript.generation.ActionScriptBaseCreateMembersFix;
import com.intellij.lang.javascript.psi.*;
import com.intellij.lang.javascript.psi.ecmal4.*;
import com.intellij.lang.javascript.psi.resolve.*;
import com.intellij.lang.javascript.psi.stubs.JSImplicitElement;
import com.intellij.lang.javascript.psi.util.JSUtils;
import com.intellij.lang.javascript.validation.UnusedImportsUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.*;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.util.PsiTreeUtil;
import kotlin.Pair;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static com.intellij.lang.javascript.psi.JSCommonTypeNames.OBJECT_CLASS_NAME;

public class ActionScriptPsiExtensionsImpl implements ActionScriptPsiExtensions {
  @Override
  public @Nullable PsiElement findClassFromNamespace(@NotNull String qname, @NotNull PsiElement context) {
    final DialectOptionHolder dialect = DialectDetector.dialectOfElement(context);
    if (dialect != null && (dialect.isJavaScript() || dialect.isTypeScript)) return null;

    GlobalSearchScope scope = JSResolveUtil.getResolveScope(context);
    final VirtualFile vFile = context.getContainingFile().getVirtualFile();
    if (!JSCommonTypeNames.OBJECT_CLASS_NAME.equals(qname) && (vFile == null || scope.contains(vFile))) {
      // we need to take Object from our predefined file so skip file (e.g.swf) local lookup
      JSNamedElement localElement = ActionScriptImportHandler.findFileLocalElement(qname, context);
      if (localElement != null) return localElement;
    }

    return JSDialectSpecificHandlersFactory.forElement(context).getClassResolver().findClassByQName(qname, scope);

  }

  @Override
  public @Nullable Pair<@Nullable JSElement, JSResolveResult.@Nullable ProblemKind> checkActionScriptQualifiedNameHasNecessaryImport(
    @Nullable PsiElement element,
    @Nullable JSElement importElement,
    @Nullable PsiElement placeTopParent,
    @Nullable PsiElement place,
    @NotNull String name
  ) {
    {
      JSResolveResult.ProblemKind resolveProblem = null;
      if (importElement == null &&
          (element instanceof JSClass ||
           element instanceof JSFunction ||
           element instanceof JSVariable ||
           element instanceof JSNamespaceDeclaration)) {
        if (placeTopParent instanceof JSReferenceExpression && !(placeTopParent.getParent() instanceof JSImportStatement)) {
          if (placeTopParent.getParent() instanceof JSNewExpression &&
              element instanceof JSFunction &&
              ((JSFunction)element).isConstructor()) {
            element = element.getParent();
          }
          final String qName = element instanceof JSQualifiedNamedElement ? ((JSQualifiedNamedElement)element).getQualifiedName() : null;

          if (qName != null &&
              qName.indexOf('.') != -1 &&
              !(JSResolveUtil.getPackageName(element).equals(JSResolveUtil.getPackageNameFromPlace(place))) &&
              !UnusedImportsUtil.isSomeNodeThatShouldNotHaveImportsWhenQualified((JSReferenceExpression)placeTopParent, element)
          ) {
            ActionScriptSinkResolveProcessor<?> processor =
              new ActionScriptSinkResolveProcessor<>(name, new ResolveResultSink(null, name)) {
                @Override
                public boolean execute(@NotNull PsiElement element, @NotNull ResolveState state) {
                  if (element instanceof JSQualifiedNamedElement) {
                    if (!qName.equals(((JSQualifiedNamedElement)element).getQualifiedName())) return true;
                  }
                  else {
                    return true;
                  }
                  return super.execute(element, state);
                }
              };

            processor.putUserData(ActionScriptResolveProcessor.ASKING_FOR_QUALIFIED_IMPORT, qName);
            PsiNamedElement importOwner =
              PsiTreeUtil.getParentOfType(placeTopParent, JSFunction.class, JSFile.class, JSPackageStatement.class, JSClass.class);
            if (importOwner != null) { // optimization, start directly from import owner
              PsiElement elt = PsiTreeUtil.getChildOfAnyType(importOwner, PsiWhiteSpace.class);
              if (elt == null) elt = importOwner.getFirstChild();
              JSResolveUtil.treeWalkUp(processor, importOwner, elt, place);
            }
            boolean noImportNoResolve = processor.getResult() == null;

            if (noImportNoResolve) {
              resolveProblem = JSResolveResult.ProblemKind.QUALIFIED_NAME_IS_NOT_IMPORTED;
            }
            else {
              final ResolveResult[] resultsAsResolveResults = ((ResolveResultSink)processor.getResultSink()).getResultsAsResolveResults();
              if (resultsAsResolveResults.length != 0) importElement = ((JSResolveResult)resultsAsResolveResults[0]).getActionScriptImport();
            }
          }
        }
      }
      return new Pair<>(importElement, resolveProblem);
    }
  }

  @Override
  public @Nullable JSSinkResolveProcessor newDuplicateCheckProcessor(@NotNull String name,
                                                                     @NotNull PsiElement place,
                                                                     @Nullable PsiElement scope,
                                                                     @NotNull PsiElement decl) {
    return new ActionScriptSinkResolveProcessor<ResolveResultSink>(name, place, new ResolveResultSink(decl, name) {
      @Override
      public void addCandidateResult(PsiElement element, boolean isCompleteMatch, JSResolveResult.ProblemKind problemKind) {
      }
    }) {
      private final PsiElement myDecl = decl;

      @Override
      public boolean execute(@NotNull PsiElement element, @NotNull ResolveState state) {
        if (element == myDecl) return true;

        if (element instanceof JSFunction elementFunction && myDecl instanceof JSFunction declFunction) {
          if ((declFunction.isGetProperty() && elementFunction.isSetProperty()) ||
              (declFunction.isSetProperty() && elementFunction.isGetProperty())) {
            return true;
          }
        }
        else if (element instanceof JSProperty elementProperty && myDecl instanceof JSProperty declProperty) {

          if ((declProperty.isGetProperty() && elementProperty.isSetProperty()) ||
              (declProperty.isSetProperty() && elementProperty.isGetProperty())) {
            return true;
          }
        }

        if (element instanceof JSFunction && myDecl instanceof JSClass && element.getParent() == myDecl) {
          return true;
        }

        if (isScopeNamedElement(element) && isScopeNamedElement(myDecl)) {
          JSAttributeList attrList = element instanceof JSAttributeListOwner ? ((JSAttributeListOwner)element).getAttributeList() : null;
          JSAttributeList attrList2 = myDecl instanceof JSAttributeListOwner ? ((JSAttributeListOwner)myDecl).getAttributeList() : null;

          if (attrList != null && attrList2 != null) {
            final String ns = ActionScriptPsiImplUtil.getNamespaceValue(attrList);
            final String ns2 = ActionScriptPsiImplUtil.getNamespaceValue(attrList2);

            if ((ns != null && !ns.equals(ns2)) || ns2 != null && !ns2.equals(ns)) {
              return true;
            }

            JSConditionalCompileVariableReference conditionalCompileVar =
              ActionScriptPsiImplUtil.getConditionalCompileVariableReference(attrList);
            JSConditionalCompileVariableReference conditionalCompileVar2 =
              ActionScriptPsiImplUtil.getConditionalCompileVariableReference(attrList2);

            if (conditionalCompileVar != null && conditionalCompileVar2 != null) {
              JSReferenceExpression expression = conditionalCompileVar.getExpression();
              JSReferenceExpression expression2 = conditionalCompileVar2.getExpression();

              if (expression != null && expression2 != null) {
                if (!expression.getText().equals(expression2.getText())) return true;
              }
            }
          }
          else if (attrList != null && ActionScriptPsiImplUtil.getNamespace(attrList) != null ||
                   attrList2 != null && ActionScriptPsiImplUtil.getNamespace(attrList2) != null) {
            return true;
          }

          final boolean notStatic2 = attrList2 == null || !attrList2.hasModifier(JSAttributeList.ModifierType.STATIC);
          final boolean notStatic = attrList == null || !attrList.hasModifier(JSAttributeList.ModifierType.STATIC);
          if ((notStatic2 && !notStatic) || (notStatic && !notStatic2)) {
            return true;
          }
        }

        if (element instanceof ImplicitJSVariableImpl || element instanceof JSImplicitElement) {
          return true;
        }
        if (element instanceof JSDefinitionExpression) return true;

        return super.execute(element, state);
      }

      private static boolean isScopeNamedElement(@NotNull PsiElement element) {
        return element instanceof JSAttributeListOwner;
      }
    };
  }

  @Override
  public @Nullable JSSinkResolveProcessor newCollectMembersToImplementProcessor(@NotNull ResultSink sink) {
    return new ActionScriptSinkResolveProcessor<>(sink) {
      @Override
      public boolean execute(final @NotNull PsiElement element, final @NotNull ResolveState state) {
        if (element instanceof JSFunction) {
          JSClass containingClass = JSUtils.getMemberContainingClass(element);
          if (containingClass != null &&
              OBJECT_CLASS_NAME.equals(containingClass.getQualifiedName()) ||
              ((JSFunction)element).isConstructor()) {
            return true;
          }
        }
        else if (!(element instanceof JSField)) {
          return true;
        }

        if (place instanceof JSFunction && ((JSFunction)place).isConstructor()) {
          if (element instanceof JSField) return true;
        }

        return super.execute(element, state);
      }

      {
        setToProcessHierarchy(true);
        setToProcessActionScriptImplicits(false);
      }
    };
  }

  @Override
  public @Nullable String calcNamespaceId(@NotNull JSAttributeList attributeList, @NotNull String namespace, @NotNull PsiElement element) {
    return ActionScriptBaseCreateMembersFix.calcNamespaceId(attributeList, namespace, element);
  }

  @Override
  public @Nullable String calcNamespaceReference(@NotNull JSElement element) {
    return ActionScriptFlexPsiImplUtil.calcNamespaceReference(element);
  }
}