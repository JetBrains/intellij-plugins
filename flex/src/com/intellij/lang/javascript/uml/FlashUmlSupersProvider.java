package com.intellij.lang.javascript.uml;

import com.intellij.diagram.extras.providers.SupersProvider;
import com.intellij.lang.javascript.JavaScriptBundle;
import com.intellij.lang.javascript.psi.ecmal4.JSClass;
import com.intellij.lang.javascript.psi.resolve.JSInheritanceUtil;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;

import java.util.Collection;
import java.util.Comparator;

public class FlashUmlSupersProvider extends SupersProvider<Object> {

  @Override
  public boolean showProgress() {
    return false;
  }

  @Override
  public Object[] getElements(Object element, Project project) {
    final Collection<JSClass> supers = JSInheritanceUtil.findAllParentsForClass((JSClass)element, true);
    return supers.toArray(JSClass.EMPTY_ARRAY);
  }

  @Override
  public boolean isEnabledOn(Object element) {
    return element instanceof JSClass;
  }

  @Override
  public String getHeaderName(Object element, Project project) {
    return JavaScriptBundle.message("javascript.uml.show.supers.header", ((JSClass)element).getName());
  }

  @Override
  public Comparator<Object> getComparator() {
    return (o1, o2) -> PSI_COMPARATOR.compare((PsiElement)o1, (PsiElement)o2);
  }
}
