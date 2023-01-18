/*
 * Copyright 2000-2013 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.intellij.coldFusion.model.psi;

import com.intellij.coldFusion.model.files.CfmlFile;
import com.intellij.openapi.project.Project;
import com.intellij.psi.search.GlobalSearchScope;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;

public class CfmlComponentType extends CfmlType {
  private final String myComponentQualifiedPath;
  private final Project myProject;
  private final CfmlFile myContainingFile;

  public CfmlComponentType(@NotNull String componentQualifiedPath, CfmlFile containingFile, Project project) {
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
    return !resolve().isEmpty();
  }

  @Override
  public GlobalSearchScope getResolveScope() {
    return GlobalSearchScope.projectScope(myProject);
  }
}
