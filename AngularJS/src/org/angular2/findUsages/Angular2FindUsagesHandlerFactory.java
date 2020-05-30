// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.findUsages;

import com.intellij.find.findUsages.FindUsagesHandler;
import com.intellij.find.findUsages.FindUsagesHandlerFactory;
import com.intellij.find.findUsages.FindUsagesHelper;
import com.intellij.find.findUsages.FindUsagesOptions;
import com.intellij.openapi.application.ReadAction;
import com.intellij.psi.PsiElement;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.usageView.UsageInfo;
import com.intellij.util.Processor;
import org.angular2.entities.Angular2DirectiveSelectorPsiElement;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;

import static com.intellij.psi.search.GlobalSearchScopeUtil.toGlobalSearchScope;

public class Angular2FindUsagesHandlerFactory extends FindUsagesHandlerFactory {
  @Override
  public boolean canFindUsages(@NotNull PsiElement element) {
    return element instanceof Angular2DirectiveSelectorPsiElement;
  }

  @Override
  public @Nullable FindUsagesHandler createFindUsagesHandler(@NotNull PsiElement element, boolean forHighlightUsages) {
    if (element instanceof Angular2DirectiveSelectorPsiElement) {
      return new DirectiveSelectorFindUsagesHandler((Angular2DirectiveSelectorPsiElement)element);
    }
    return null;
  }

  private static class DirectiveSelectorFindUsagesHandler extends FindUsagesHandler {

    protected DirectiveSelectorFindUsagesHandler(@NotNull Angular2DirectiveSelectorPsiElement psiElement) {
      super(psiElement);
    }

    @Override
    public boolean processElementUsages(@NotNull PsiElement element,
                                        @NotNull Processor<? super UsageInfo> processor,
                                        @NotNull FindUsagesOptions options) {
      if (options.isUsages) {
        Collection<String> stringToSearch = ReadAction.compute(() -> getStringsToSearch(element));
        GlobalSearchScope globalSearchScope = ReadAction.compute(() -> toGlobalSearchScope(options.searchScope, element.getProject()));
        boolean success = stringToSearch == null || FindUsagesHelper
          .processUsagesInText(element, stringToSearch, true, globalSearchScope, processor);
        if (!success) return false;
      }
      return super.processElementUsages(element, processor, options);
    }
  }
}
