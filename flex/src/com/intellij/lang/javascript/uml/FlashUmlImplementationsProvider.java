// Copyright 2000-2021 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.intellij.lang.javascript.uml;

import com.intellij.diagram.extras.providers.ImplementationsProvider;
import com.intellij.lang.javascript.flex.FlexBundle;
import com.intellij.lang.javascript.psi.ecmal4.JSClass;
import com.intellij.lang.javascript.search.JSClassSearch;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.util.Processor;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;

public class FlashUmlImplementationsProvider extends ImplementationsProvider<Object> {

  @Override
  public Object @NotNull [] getElements(Object element, @NotNull Project project) {
    JSClass clazz = (JSClass)element;
    final Collection<PsiElement> inheritors = Collections.synchronizedSet(new HashSet<>());

    final Processor<JSClass> p = aClass -> {
      final PsiElement navigationElement = aClass.getNavigationElement();
      inheritors.add(navigationElement instanceof JSClass ? navigationElement : aClass);
      return true;
    };
    JSClassSearch.searchClassInheritors(clazz, true).forEach(p);
    if (clazz.isInterface()) {
      JSClassSearch.searchInterfaceImplementations(clazz, true).forEach(p);
    }
    return inheritors.toArray(PsiElement.EMPTY_ARRAY);
  }

  @Override
  public boolean isEnabledOn(Object element) {
    return element instanceof JSClass;
  }

  @Override
  public @NotNull String getHeaderName(Object element, @NotNull Project project) {
    return FlexBundle.message("javascript.uml.show.implementations.header", ((JSClass)element).getName());
  }

  @Override
  public @NotNull Comparator<Object> getComparator() {
    return (o1, o2) -> PSI_COMPARATOR.compare((PsiElement)o1, (PsiElement)o2);
  }
}
