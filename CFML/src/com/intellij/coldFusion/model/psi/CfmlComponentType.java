package com.intellij.coldFusion.model.psi;

import com.intellij.coldFusion.model.files.CfmlFile;
import com.intellij.openapi.project.Project;
import com.intellij.psi.search.GlobalSearchScope;

import java.util.Collection;

/**
 * @author vnikolaenko
 */
public class CfmlComponentType extends CfmlType {
  private String myComponentQualifiedPath;
  private Project myProject;
  private CfmlFile myContainingFile;

  public CfmlComponentType(String componentQualifiedPath, CfmlFile containingFile, Project project) {
    super(componentQualifiedPath);
    myComponentQualifiedPath = componentQualifiedPath;
    myProject = project;
    myContainingFile = containingFile;
  }

  public Collection<CfmlComponent> resolve() {
    return CfmlComponentReference.resolveFromQualifiedName(myComponentQualifiedPath, myContainingFile);
    // return CfmlIndex.getInstance(myProject).getComponentsByName(myComponentQualifiedPath);
  }

  @Override
  public boolean isValid() {
    return resolve().size() != 0;
  }

  @Override
  public GlobalSearchScope getResolveScope() {
    return GlobalSearchScope.projectScope(myProject);
  }
}
