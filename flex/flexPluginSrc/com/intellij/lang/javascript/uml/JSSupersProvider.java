package com.intellij.lang.javascript.uml;

import com.intellij.lang.javascript.psi.ecmal4.JSClass;
import com.intellij.lang.javascript.psi.resolve.JSInheritanceUtil;
import com.intellij.lang.javascript.JSBundle;
import com.intellij.openapi.project.Project;
import com.intellij.diagram.extras.providers.SupersProvider;
import com.intellij.psi.PsiElement;

import java.util.Comparator;
import java.util.Collection;

public class JSSupersProvider extends SupersProvider<Object> {

  @Override
  public boolean showProgress() {
    return false;
  }

  public Object[] getElements(Object element, Project project) {
    final Collection<JSClass> supers = JSInheritanceUtil.findAllParentsForClass((JSClass)element, true);
    return supers.toArray(new JSClass[supers.size()]);
  }

  public boolean isEnabledOn(Object element) {
    return element instanceof JSClass;
  }

  public String getHeaderName(Object element, Project project) {
    return JSBundle.message("javascript.uml.show.supers.header", ((JSClass)element).getName());
  }

  public Comparator<Object> getComparator() {
    return new Comparator<Object>() {
      public int compare(Object o1, Object o2) {
        return PSI_COMPARATOR.compare((PsiElement)o1, (PsiElement)o2);
      }
    };
  }
}
