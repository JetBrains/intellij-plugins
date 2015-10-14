package com.intellij.javascript.flex.mxml;

import com.intellij.lang.javascript.psi.ecmal4.JSClass;
import com.intellij.lang.javascript.search.JSClassInheritorsProvider;
import com.intellij.openapi.project.Project;
import com.intellij.psi.search.GlobalSearchScope;

import java.util.Collection;

public class XmlBackedClassInheritorsProvider implements JSClassInheritorsProvider {
  public Collection<JSClass> getImplementingClasses(String parentName, Project project, GlobalSearchScope scope) {
    return FlexXmlBackedClassesIndex.searchClassInheritors(FlexXmlBackedImplementedInterfacesIndex.NAME, parentName, project, scope);
  }

  public Collection<JSClass> getExtendingClasses(String parentName, Project project, GlobalSearchScope scope) {
    return FlexXmlBackedClassesIndex.searchClassInheritors(FlexXmlBackedSuperClassesIndex.NAME, parentName, project, scope);
  }
}
