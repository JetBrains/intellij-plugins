// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.findUsages;

import com.intellij.find.findUsages.FindUsagesHandler;
import com.intellij.lang.javascript.findUsages.JavaScriptFindUsagesHandlerFactory;
import com.intellij.lang.javascript.psi.ecmal4.JSClass;
import com.intellij.psi.PsiElement;
import org.angular2.entities.Angular2EntitiesProvider;
import org.angular2.entities.Angular2Entity;
import org.jetbrains.annotations.NotNull;

public class Angular2FindUsagesHandlerFactory extends JavaScriptFindUsagesHandlerFactory {

  @Override
  public boolean canFindUsages(@NotNull PsiElement element) {
    return element instanceof JSClass
           || Angular2EntitiesProvider.getEntity(element) != null;
  }

  @Override
  public FindUsagesHandler createFindUsagesHandler(@NotNull PsiElement element, boolean forHighlightUsages) {
    final Angular2Entity entity;
    if (!forHighlightUsages && (entity = Angular2EntitiesProvider.getEntity(element)) != null) {
      PsiElement entitySource = entity.getEntitySource();
      return new JavaScriptFindUsagesHandler(
        element != entitySource ? entity.getSourceElement() : entitySource,
        element == entitySource ? new PsiElement[]{entity.getSourceElement()} : PsiElement.EMPTY_ARRAY);
    }
    return super.createFindUsagesHandler(element, forHighlightUsages);
  }
}
