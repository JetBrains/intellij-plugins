// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.jetbrains.lang.dart.ide.hierarchy.call;

import com.intellij.find.findUsages.FindUsagesHandler;
import com.intellij.find.findUsages.FindUsagesOptions;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.usageView.UsageInfo;
import com.intellij.util.CommonProcessors;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class DartCallerTreeStructure extends DartCallHierarchyTreeStructure {

  public DartCallerTreeStructure(Project project, PsiElement element, String currentScopeType) {
    super(project, element, currentScopeType);
  }

  private static void getCallers(@NotNull PsiElement element, @NotNull List<PsiElement> results, @NotNull GlobalSearchScope scope) {
    FindUsagesHandler finder = createFindUsageHandler(element);
    final CommonProcessors.CollectProcessor<UsageInfo> processor = new CommonProcessors.CollectProcessor<>();
    FindUsagesOptions options = new FindUsagesOptions(scope);
    options.isUsages = true;
    options.isSearchForTextOccurrences = false;
    finder.processElementUsages(element, processor, options);
    for (UsageInfo each : processor.getResults()) {
      PsiElement eachElement = each.getElement();
      collectDeclarations(eachElement, results);
    }
  }

  @Override
  protected @NotNull List<PsiElement> getChildren(@NotNull PsiElement element) {
    final List<PsiElement> list = new ArrayList<>();
    getCallers(element, list, getScope());
    return list;
  }
}
