// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angularjs.findUsages;

import com.intellij.find.findUsages.FindUsagesHandler;
import com.intellij.lang.javascript.findUsages.JavaScriptFindUsagesHandlerFactory;
import com.intellij.lang.javascript.psi.ecmal4.JSClass;
import com.intellij.psi.PsiElement;
import org.angular2.entities.Angular2EntitiesProvider;
import org.angular2.entities.Angular2Entity;
import org.angularjs.codeInsight.DirectiveUtil;
import org.jetbrains.annotations.NotNull;

public class AngularJSFindUsagesHandlerFactory extends JavaScriptFindUsagesHandlerFactory {

  @Override
  public boolean canFindUsages(@NotNull PsiElement element) {
    return element instanceof JSClass
           || DirectiveUtil.getDirective(element) != null
           || Angular2EntitiesProvider.getEntity(element) != null;
  }

  @Override
  public FindUsagesHandler createFindUsagesHandler(@NotNull PsiElement element, boolean forHighlightUsages) {
    final Angular2Entity entity;
    if (!forHighlightUsages && (entity = Angular2EntitiesProvider.getEntity(element)) != null) {
      JSClass cls = entity.getTypeScriptClass();
      return new JavaScriptFindUsagesHandlerFactory.JavaScriptFindUsagesHandler(
        element != cls ? entity.getSourceElement() : cls,
        element == cls ? new PsiElement[]{entity.getSourceElement()} : PsiElement.EMPTY_ARRAY);
    }
    return super.createFindUsagesHandler(element, forHighlightUsages);
  }
}
