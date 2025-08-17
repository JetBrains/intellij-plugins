// Copyright 2000-2025 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.intellij.javascript.flex.refactoring.changeSignature;

import com.intellij.lang.javascript.flex.ECMAScriptImportOptimizer;
import com.intellij.lang.javascript.flex.ImportUtils;
import com.intellij.lang.javascript.psi.JSExpression;
import com.intellij.lang.javascript.psi.JSFunction;
import com.intellij.lang.javascript.psi.JSVariable;
import com.intellij.lang.javascript.psi.ecmal4.JSClass;
import com.intellij.lang.javascript.psi.ecmal4.JSPackageStatement;
import com.intellij.lang.javascript.psi.ecmal4.JSQualifiedNamedElement;
import com.intellij.lang.javascript.psi.impl.JSChangeUtil;
import com.intellij.lang.javascript.psi.impl.JSPsiImplUtils;
import com.intellij.lang.javascript.psi.resolve.JSResolveResult;
import com.intellij.lang.javascript.psi.resolve.JSResolveUtil;
import com.intellij.lang.javascript.refactoring.FormatFixer;
import com.intellij.lang.javascript.refactoring.changeSignature.JSChangeSignatureProcessor;
import com.intellij.lang.javascript.refactoring.util.JSRefactoringUtil;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.SmartPointerManager;
import com.intellij.psi.SmartPsiElementPointer;
import com.intellij.util.containers.ContainerUtil;
import com.intellij.util.containers.MultiMap;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

public class ActionScriptImportProcessor implements JSChangeSignatureProcessor.RequiredImportProcessor {
  private final MultiMap<PsiElement, String> imports = new MultiMap<>();

  @Override
  public void computeImportsForExpression(@Nullable String newlyAddedText, @NotNull PsiElement context) {
    imports.putValues(context, computeRequiredActionScriptImports(newlyAddedText, context));
  }

  @Override
  public void computeImportsForType(@Nullable String type, @NotNull PsiElement context) {
    String currentPackage = JSResolveUtil.getPackageNameFromPlace(context);
    if (type != null && type.contains(".") &&
        JSPsiImplUtils.differentPackageName(StringUtil.getPackageName(type), currentPackage)) {
      imports.putValue(context, type);
    }
  }

  @Override
  public void finish() {
    List<SmartPsiElementPointer<PsiElement>> elements = ContainerUtil.map(imports.keySet(), SmartPointerManager::createPointer);

    for (Map.Entry<PsiElement, Collection<String>> entry : imports.entrySet()) {
      if (!entry.getValue().isEmpty()) {
        FormatFixer formatFixer = ImportUtils.insertImportStatements(entry.getKey(), entry.getValue());
        if (formatFixer != null) {
          formatFixer.fixFormat();
        }
      }
    }

    List<PsiFile> toOptimizeImports = elements
      .stream()
      .map(element -> element.getContainingFile())
      .filter(file -> file != null)
      .distinct()
      .toList();
    List<FormatFixer> formatters = new ArrayList<>();
    for (PsiFile file : toOptimizeImports) {
      formatters.addAll(ECMAScriptImportOptimizer.executeNoFormat(file));
    }
    JSRefactoringUtil.format(formatters);
  }

  private static List<String> computeRequiredActionScriptImports(String text, final PsiElement context) {
    ArrayList<String> result = new ArrayList<>();
    JSExpression expression = JSChangeUtil.createExpressionPsiWithContext(text, context, JSExpression.class);
    if (expression == null) {
      return ContainerUtil.emptyList();
    }
    JSChangeSignatureProcessor.processUnresolvedReferencesForElement(expression, context, (__, resolveResults) -> {
      if (resolveResults.length != 1) {
        return;
      }

      JSResolveResult resolveResult = (JSResolveResult)resolveResults[0];
      if (resolveResult.getElement() == null || resolveResult.getResolveProblemKind() != null &&
                                                JSResolveResult.ProblemKind.QUALIFIED_NAME_IS_NOT_IMPORTED !=
                                                resolveResult.getResolveProblemKind()) {
        return;
      }

      PsiElement resolved = resolveResult.getElement();
      if (JSResolveUtil.isConstructorFunction(resolved)) {
        resolved = resolved.getParent();
      }

      if (resolved instanceof JSClass || ((resolved instanceof JSFunction ||
                                           resolved instanceof JSVariable) && resolved.getParent() instanceof JSPackageStatement)) {
        String qName = ((JSQualifiedNamedElement)resolved).getQualifiedName();
        if (qName != null && qName.contains(".") &&
            JSPsiImplUtils.differentPackageName(StringUtil.getPackageName(qName), JSResolveUtil.getPackageNameFromPlace(context))) {
          result.add(qName);
        }
      }
    });
    return result;
  }
}
