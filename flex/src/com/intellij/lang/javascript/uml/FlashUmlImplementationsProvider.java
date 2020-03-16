// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.intellij.lang.javascript.uml;

import com.intellij.diagram.extras.providers.ImplementationsProvider;
import com.intellij.lang.javascript.JavaScriptBundle;
import com.intellij.lang.javascript.psi.ecmal4.JSClass;
import com.intellij.lang.javascript.search.JSClassSearch;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.util.Processor;
import gnu.trove.THashSet;

import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;

public class FlashUmlImplementationsProvider extends ImplementationsProvider<Object> {

  @Override
  public Object[] getElements(Object element, Project project) {
    JSClass clazz = (JSClass)element;
    final Collection<PsiElement> inheritors = Collections.synchronizedSet(new THashSet<>());

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
  public String getHeaderName(Object element, Project project) {
    return JavaScriptBundle.message("javascript.uml.show.implementations.header", ((JSClass)element).getName());
  }

  @Override
  public Comparator<Object> getComparator() {
    return (o1, o2) -> PSI_COMPARATOR.compare((PsiElement)o1, (PsiElement)o2);
  }
}
