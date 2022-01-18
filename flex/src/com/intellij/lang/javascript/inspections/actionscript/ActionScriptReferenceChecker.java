// Copyright 2000-2022 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.intellij.lang.javascript.inspections.actionscript;

import com.intellij.codeInsight.highlighting.ReadWriteAccessDetector;
import com.intellij.codeInspection.LocalQuickFix;
import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.javascript.flex.mxml.FlexCommonTypeNames;
import com.intellij.lang.javascript.findUsages.JSReadWriteAccessDetector;
import com.intellij.lang.javascript.flex.AddImportECMAScriptClassOrFunctionAction;
import com.intellij.lang.javascript.flex.FlexModuleType;
import com.intellij.lang.javascript.flex.ImportUtils;
import com.intellij.lang.javascript.highlighting.JSFixFactory;
import com.intellij.lang.javascript.psi.*;
import com.intellij.lang.javascript.psi.e4x.JSE4XNamespaceReference;
import com.intellij.lang.javascript.psi.ecmal4.JSAttributeList;
import com.intellij.lang.javascript.psi.ecmal4.JSClass;
import com.intellij.lang.javascript.psi.ecmal4.JSReferenceListMember;
import com.intellij.lang.javascript.psi.ecmal4.impl.JSPackageWrapper;
import com.intellij.lang.javascript.psi.impl.JSReferenceExpressionImpl;
import com.intellij.lang.javascript.psi.resolve.ActionScriptResolveUtil;
import com.intellij.lang.javascript.psi.resolve.JSResolveResult;
import com.intellij.lang.javascript.psi.resolve.JSResolveUtil;
import com.intellij.lang.javascript.psi.types.JSAnyType;
import com.intellij.lang.javascript.validation.JSProblemReporter;
import com.intellij.lang.javascript.validation.TypedJSReferenceChecker;
import com.intellij.lang.javascript.validation.fixes.CreateFlexMobileViewIntentionAndFix;
import com.intellij.lang.javascript.validation.fixes.CreateJSEventMethod;
import com.intellij.lang.javascript.validation.fixes.CreateJSFunctionIntentionAction;
import com.intellij.lang.javascript.validation.fixes.CreateJSPropertyAccessorIntentionAction;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleType;
import com.intellij.openapi.module.ModuleUtilCore;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.ResolveResult;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.ArrayUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public final class ActionScriptReferenceChecker extends TypedJSReferenceChecker {
  public ActionScriptReferenceChecker(@NotNull JSProblemReporter<?> reporter) {
    super(reporter);
  }

  @Nullable
  @Override
  protected LocalQuickFix getPreferredQuickFixForUnresolvedRef(final PsiElement nameIdentifier) {
    final Module module = ModuleUtilCore.findModuleForPsiElement(nameIdentifier);
    if (module == null || ModuleType.get(module) != FlexModuleType.getInstance()) return null;

    final String conditionalCompilerDefinitionName = getPotentialConditionalCompilerDefinitionName(nameIdentifier);
    if (conditionalCompilerDefinitionName != null) {
      return new DeclareConditionalCompilerDefinitionFix(module, conditionalCompilerDefinitionName);
    }

    final JSCallExpression callExpression = PsiTreeUtil.getParentOfType(nameIdentifier, JSCallExpression.class);
    if (callExpression == null) return null;

    if (JSResolveUtil.isEventListenerCall(callExpression)) {
      final JSExpression[] params = callExpression.getArguments();

      if (params.length >= 2 && PsiTreeUtil.isAncestor(params[1], nameIdentifier, true)) {
        return new CreateJSEventMethod(nameIdentifier.getText(), () -> {
          PsiElement responsibleElement = null;
          if (params[0] instanceof JSReferenceExpression) {
            responsibleElement = ((JSReferenceExpression)params[0]).getQualifier();
          }

          return responsibleElement == null ? FlexCommonTypeNames.FLASH_EVENT_FQN : responsibleElement.getText();
        });
      }
    }
    else if (needsFlexMobileViewAsFirstArgument(callExpression)) {
      final JSExpression[] params = callExpression.getArguments();

      if (params.length >= 1 && PsiTreeUtil.isAncestor(params[0], nameIdentifier, true)) {
        final String contextPackage = JSResolveUtil.getPackageNameFromPlace(callExpression);
        final String fqn = StringUtil.getQualifiedName(contextPackage, nameIdentifier.getText());

        final CreateFlexMobileViewIntentionAndFix fix = new CreateFlexMobileViewIntentionAndFix(fqn, nameIdentifier, true);
        fix.setCreatedClassFqnConsumer(fqn1 -> {
          final String packageName = StringUtil.getPackageName(fqn1);
          if (StringUtil.isNotEmpty(packageName) && !packageName.equals(contextPackage)) {
            ImportUtils.doImport(nameIdentifier, fqn1, true);
          }
        });
        return fix;
      }
    }

    return null;
  }

  @Nullable
  private static String getPotentialConditionalCompilerDefinitionName(final PsiElement identifier) {
    final PsiElement parent1 = identifier.getParent();
    final PsiElement parent2 = parent1 == null ? null : parent1.getParent();
    final PsiElement parent3 = parent2 == null ? null : parent2.getParent();
    if (parent1 instanceof JSReferenceExpression && ((JSReferenceExpression)parent1).getQualifier() == null &&
        parent2 instanceof JSE4XNamespaceReference &&
        parent3 instanceof JSReferenceExpression && ((JSReferenceExpression)parent3).getQualifier() == null) {
      return getNormalizedConditionalCompilerDefinitionName(parent3.getText());
    }
    return null;
  }

  @Nullable
  private static String getNormalizedConditionalCompilerDefinitionName(final String name) {
    final int colonsIndex = name.indexOf("::");
    if (colonsIndex > 0) {
      final String first = name.substring(0, colonsIndex).trim();
      final String second = name.substring(colonsIndex + "::".length()).trim();
      if (StringUtil.isJavaIdentifier(first) && StringUtil.isJavaIdentifier(second)) {
        return first + "::" + second;
      }
    }
    return null;
  }

  private static boolean needsFlexMobileViewAsFirstArgument(final JSCallExpression callExpression) {
    final JSExpression methodExpr = callExpression.getMethodExpression();
    final PsiElement function = methodExpr instanceof JSReferenceExpression ? ((JSReferenceExpression)methodExpr).resolve() : null;
    final PsiElement clazz =
      function instanceof JSFunction && ArrayUtil.contains(((JSFunction)function).getName(), "pushView", "replaceView")
      ? function.getParent()
      : null;
    return clazz instanceof JSClass && "spark.components.ViewNavigator".equals(((JSClass)clazz).getQualifiedName());
  }

  @Override
  protected boolean addCreateFromUsageFixes(JSReferenceExpression node,
                                            ResolveResult[] resolveResults,
                                            List<LocalQuickFix> fixes,
                                            boolean inTypeContext,
                                            boolean ecma) {
    final PsiElement nodeParent = node.getParent();
    final JSExpression qualifier = node.getQualifier();
    final String referencedName = node.getReferenceName();

    inTypeContext = super.addCreateFromUsageFixes(node, resolveResults, fixes, inTypeContext, ecma);
    if (!(nodeParent instanceof JSArgumentList) && nodeParent.getParent() instanceof JSCallExpression) {
      inTypeContext = true;
    }

    if (!inTypeContext) {
      boolean getter = !(node.getParent() instanceof JSDefinitionExpression);
      fixes.add(new CreateJSPropertyAccessorIntentionAction(referencedName, getter));
    }
    if (qualifier == null) {
      boolean canHaveTypeFix = false;

      if (nodeParent instanceof JSReferenceListMember) {
        canHaveTypeFix = true;
      }
      else if (!(nodeParent instanceof JSDefinitionExpression) && resolveResults.length == 0) {
        canHaveTypeFix = true;
        fixes.add(createClassOrInterfaceFix(node, false));
        fixes.add(createClassOrInterfaceFix(node, true));
      }

      if (!inTypeContext && JSReadWriteAccessDetector.ourInstance.getExpressionAccess(node) == ReadWriteAccessDetector.Access.Read) {
        canHaveTypeFix = true;
        fixes.add(new CreateJSFunctionIntentionAction(referencedName, true, false));
      }

      if (canHaveTypeFix) fixes.add(new AddImportECMAScriptClassOrFunctionAction(null, node));
    }
    else if (canHaveImportTo(resolveResults)) {
      fixes.add(new AddImportECMAScriptClassOrFunctionAction(null, node));
    }
    return inTypeContext;
  }

  @Override
  protected void addCreateFromUsageFixesForCall(@NotNull JSReferenceExpression methodExpression,
                                                boolean isNewExpression,
                                                ResolveResult @NotNull [] resolveResults,
                                                @NotNull List<LocalQuickFix> quickFixes) {
    if (canHaveImportTo(resolveResults)) {
      quickFixes.add(new AddImportECMAScriptClassOrFunctionAction(null, methodExpression));
    }
    if (!(isNewExpression)) {
      //foo() -> AS methods are callable without this -> method
      quickFixes.add(JSFixFactory.getInstance().createJSFunctionIntentionAction(methodExpression.getReferenceName(), true, false, false));
    }

    super.addCreateFromUsageFixesForCall(methodExpression, isNewExpression, resolveResults, quickFixes);
  }

  private static boolean canHaveImportTo(ResolveResult[] resolveResults) {
    if (resolveResults.length == 0) return true;
    for (ResolveResult r : resolveResults) {
      if (!r.isValidResult()) {
        if (r instanceof JSResolveResult &&
            ((JSResolveResult)r).getResolveProblemKey() == JSResolveResult.QUALIFIED_NAME_IS_NOT_IMPORTED) {
          return true;
        }
        continue;
      }
      PsiElement element = r.getElement();
      if (element instanceof JSClass) return true;
      if (element instanceof JSFunction) {
        if (((JSFunction)element).isConstructor()) return true;
      }
    }
    return false;
  }


  @Nullable
  @Override
  public ProblemHighlightType getUnresolvedReferenceHighlightType(@NotNull JSReferenceExpression node) {
    JSExpression qualifier = ((JSReferenceExpressionImpl)node).getResolveQualifier();

    if (qualifier != null) {
      final PsiFile containingFile = node.getContainingFile();
      JSType type = null;
      boolean checkType = false;

      if (qualifier instanceof JSReferenceExpression) {
        ResolveResult[] results = ((JSReferenceExpression)qualifier).multiResolve(false);

        if (results.length != 0) {
          PsiElement resultElement = results[0].getElement();
          if (resultElement instanceof JSPackageWrapper) return ProblemHighlightType.ERROR;
          type = getResolveResultType(qualifier, resultElement);
          checkType = true;
        }
      }
      else {
        type = JSResolveUtil.getExpressionJSType(qualifier);
        checkType = true;
      }
      if (checkType && (type instanceof JSAnyType || type == null)) {
        return ProblemHighlightType.LIKE_UNKNOWN_SYMBOL;
      }

      JSClass jsClass = ActionScriptResolveUtil.findClassOfQualifier(qualifier, containingFile);
      if (jsClass == null) {
        return ProblemHighlightType.ERROR;
      }

      final JSAttributeList attributeList = jsClass.getAttributeList();
      if (attributeList == null || !attributeList.hasModifier(JSAttributeList.ModifierType.DYNAMIC)) {
        return ProblemHighlightType.ERROR;
      }

      final String qualifiedName = jsClass.getQualifiedName();
      if ("Error".equals(qualifiedName) || "Date".equals(qualifiedName)) {
        return ProblemHighlightType.GENERIC_ERROR_OR_WARNING;
      }
    }

    return super.getUnresolvedReferenceHighlightType(node);
  }

  @Nullable
  protected JSType getResolveResultType(JSExpression qualifier, PsiElement resultElement) {
    if (resultElement instanceof JSVariable) { // do not evaluate initializer
      return ((JSVariable)resultElement).getJSType();
    }

    return JSResolveUtil.getExpressionJSType(qualifier);
  }
}
