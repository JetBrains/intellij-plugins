package com.intellij.lang.javascript.uml;

import com.intellij.diagram.extras.providers.ImplementationsProvider;
import com.intellij.lang.javascript.JSBundle;
import com.intellij.lang.javascript.search.JSClassSearch;
import com.intellij.lang.javascript.psi.ecmal4.JSClass;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.util.Processor;
import gnu.trove.THashSet;

import java.util.Collections;
import java.util.Comparator;
import java.util.Collection;

public class JSImplementationsProvider extends ImplementationsProvider<Object> {

  public Object[] getElements(Object element, Project project) {
    JSClass clazz = (JSClass)element;
    final Collection<PsiElement> inheritors = Collections.synchronizedSet(new THashSet<PsiElement>());

    final Processor<JSClass> p = new Processor<JSClass>() {
      public boolean process(final JSClass aClass) {
        final PsiElement navigationElement = aClass.getNavigationElement();
        inheritors.add(navigationElement instanceof JSClass ? navigationElement : aClass);
        return true;
      }
    };
    JSClassSearch.searchClassInheritors(clazz, true).forEach(p);
    if (clazz.isInterface()) {
      JSClassSearch.searchInterfaceImplementations(clazz, true).forEach(p);
    }
    return inheritors.toArray(new PsiElement[inheritors.size()]);
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
