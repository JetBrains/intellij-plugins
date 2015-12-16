package org.angularjs.codeInsight.attributes;

import com.intellij.openapi.project.Project;

/**
 * @author Dennis.Ushakov
 */
public class AngularBindingDescriptor extends AngularAttributeDescriptor {
  public AngularBindingDescriptor(Project project,
                                  String attributeName) {
    super(project, attributeName, null);
  }
  //@Override
  //public PsiElement getDeclaration() {
  //  final String name = getName();
  //  final String binding = name.substring(1, name.length() - 1);
  //  final GlobalSearchScope scope = ProjectScope.getContentScope(myProject);
  //  final Collection<JSPsiElementBase> implicit = JSClassResolver.findElementsByNameIncludingImplicit(binding, scope, false);
  //  for (JSPsiElementBase base : implicit) {
  //    if (base instanceof JSField) {
  //      return base;
  //    }
  //  }
  //  return null;
  //}
}
