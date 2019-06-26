// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.jetbrains.lang.dart.ide.refactoring;

import com.intellij.find.findUsages.FindUsagesHandler;
import com.intellij.find.findUsages.FindUsagesOptions;
import com.intellij.openapi.editor.Editor;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiNamedElement;
import com.intellij.psi.PsiReference;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.search.SearchScope;
import com.intellij.refactoring.rename.inplace.VariableInplaceRenamer;
import com.intellij.usageView.UsageInfo;
import com.intellij.util.CommonProcessors;
import com.jetbrains.lang.dart.ide.findUsages.DartServerFindUsagesHandler;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;

public class DartVariableInplaceRenamer extends VariableInplaceRenamer {
  public DartVariableInplaceRenamer(@NotNull PsiNamedElement elementToRename,
                                    @NotNull Editor editor) {
    super(elementToRename, editor);
  }

  @Override
  protected Collection<PsiReference> collectRefs(SearchScope referencesSearchScope) {
    final CommonProcessors.CollectProcessor<PsiReference> referenceProcessor = new CommonProcessors.CollectProcessor<PsiReference>() {
      @Override
      protected boolean accept(PsiReference reference) {
        return acceptReference(reference);
      }
    };

    FindUsagesHandler findUsages = new DartServerFindUsagesHandler(myElementToRename);
    final CommonProcessors.CollectProcessor<UsageInfo> processor = new CommonProcessors.CollectProcessor<>();
    FindUsagesOptions options = new FindUsagesOptions(GlobalSearchScope.projectScope(myElementToRename.getProject()));
    options.isUsages = true;
    options.isSearchForTextOccurrences = false;
    findUsages.processElementUsages(myElementToRename, processor, options);
    for (UsageInfo usageInfo : processor.getResults()) {
      PsiElement element = usageInfo.getElement();
      if (element != null) {
        referenceProcessor.process(element.getReference());
      }
    }

    return referenceProcessor.getResults();
  }

  @Override
  protected PsiElement checkLocalScope() {
    PsiElement element = myElementToRename;
    while (element != null && element.getNode().getElementType().toString() != "FUNCTION_BODY") {
      element = element.getParent();
    }

    return element;
  }
}
