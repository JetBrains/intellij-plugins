// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.refactoring;

import com.intellij.lang.javascript.psi.stubs.JSImplicitElement;
import com.intellij.openapi.util.NlsContexts.DetailedDescription;
import com.intellij.psi.ElementDescriptionLocation;
import com.intellij.psi.ElementDescriptionProvider;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiNamedElement;
import com.intellij.usageView.UsageViewLongNameLocation;
import com.intellij.usageView.UsageViewTypeLocation;
import org.angular2.entities.Angular2DirectiveSelectorSymbol;
import org.angular2.index.Angular2IndexingHandler;
import org.angular2.lang.Angular2Bundle;
import org.angular2.lang.html.psi.Angular2HtmlAttrVariable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class Angular2ElementDescriptionProvider implements ElementDescriptionProvider {

  @Override
  public @Nullable String getElementDescription(@NotNull PsiElement element, @NotNull ElementDescriptionLocation location) {
    String type = getTypeDescription(element);
    if (type != null) {
      if (location instanceof UsageViewTypeLocation) {
        return type;
      }
      if (location instanceof UsageViewLongNameLocation) {
        return type + " " + ((PsiNamedElement)element).getName();
      }
      return ((PsiNamedElement)element).getName();
    }
    return null;
  }

  private static @DetailedDescription String getTypeDescription(@NotNull PsiElement element) {
    if (element instanceof Angular2DirectiveSelectorSymbol) {
      return ((Angular2DirectiveSelectorSymbol)element).isElementSelector()
             ? Angular2Bundle.message("angular.description.element-selector")
             : Angular2Bundle.message("angular.description.attribute-selector");
    }
    if (element instanceof JSImplicitElement
        && Angular2IndexingHandler.isPipe((JSImplicitElement)element)) {
      return Angular2Bundle.message("angular.description.pipe");
    }
    if (element instanceof Angular2HtmlAttrVariable) {
      return Angular2Bundle.message("angular.description.ref-var");
    }
    return null;
  }
}
