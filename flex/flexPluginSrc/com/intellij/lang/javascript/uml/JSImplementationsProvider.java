package com.intellij.lang.javascript.uml;

import com.intellij.diagram.extras.providers.ImplementationsProvider;
import com.intellij.lang.javascript.JSBundle;
import com.intellij.lang.javascript.search.JSClassSearch;
import com.intellij.lang.javascript.psi.ecmal4.JSClass;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;

import java.util.Comparator;
import java.util.Collection;
import java.util.ArrayList;

public class JSImplementationsProvider extends ImplementationsProvider<Object> {

  public Object[] getElements(Object element, Project project) {
    JSClass clazz = (JSClass)element;
    final Collection<JSClass> inheritors = new ArrayList<JSClass>();

    inheritors.addAll(JSClassSearch.searchClassInheritors(clazz, true).findAll());

    if (clazz.isInterface()) {
      inheritors.addAll(JSClassSearch.searchInterfaceImplementations(clazz, true).findAll());
    }
    return inheritors.toArray(new JSClass[inheritors.size()]);
  }

  public boolean isEnabledOn(Object element) {
    return element instanceof JSClass;
  }

  public String getHeaderName(Object element, Project project) {
    return JSBundle.message("javascript.uml.show.implementations.header", ((JSClass)element).getName());
  }

  public Comparator<Object> getComparator() {
    return new Comparator<Object>() {
      public int compare(Object o1, Object o2) {
        return PSI_COMPARATOR.compare((PsiElement)o1, (PsiElement)o2);
      }
    };
  }
}
