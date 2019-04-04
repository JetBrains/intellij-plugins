// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.findUsages;

import com.intellij.find.findUsages.FindUsagesHandler;
import com.intellij.find.findUsages.FindUsagesHandlerFactory;
import com.intellij.psi.PsiElement;
import org.angular2.entities.Angular2DirectiveSelectorPsiElement;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class Angular2FindUsagesHandlerFactory extends FindUsagesHandlerFactory {
  @Override
  public boolean canFindUsages(@NotNull PsiElement element) {
    return element instanceof Angular2DirectiveSelectorPsiElement;
  }

  @Nullable
  @Override
  public FindUsagesHandler createFindUsagesHandler(@NotNull PsiElement element, boolean forHighlightUsages) {
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
    protected boolean isSearchForTextOccurrencesAvailable(@NotNull PsiElement psiElement, boolean isSingleFile) {
      return true;
    }
  }
}
