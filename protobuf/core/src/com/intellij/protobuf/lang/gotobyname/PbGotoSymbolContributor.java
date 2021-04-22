/*
 * Copyright 2019 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.intellij.protobuf.lang.gotobyname;

import com.intellij.navigation.ChooseByNameContributor;
import com.intellij.navigation.NavigationItem;
import com.intellij.openapi.project.Project;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.stubs.StubIndex;
import com.intellij.protobuf.lang.psi.PbNamedElement;
import com.intellij.protobuf.lang.stub.index.QualifiedNameIndex;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;

/** {@link ChooseByNameContributor#SYMBOL_EP_NAME} extension for protobuf elements. */
public class PbGotoSymbolContributor implements ChooseByNameContributor {

  @NotNull
  @Override
  public String[] getNames(Project project, boolean includeNonProjectItems) {
    // NOTE: this doesn't cover fields, because it is based on a stub index and we do not
    // make stubs for fields.
    Collection<String> names = StubIndex.getInstance().getAllKeys(QualifiedNameIndex.KEY, project);
    return names.toArray(new String[0]);
  }

  @NotNull
  @Override
  public NavigationItem[] getItemsByName(
      String name, String pattern, Project project, boolean includeNonProjectItems) {
    GlobalSearchScope scope =
        includeNonProjectItems
            ? GlobalSearchScope.allScope(project)
            : GlobalSearchScope.projectScope(project);
    Collection<PbNamedElement> results =
        StubIndex.getElements(QualifiedNameIndex.KEY, name, project, scope, PbNamedElement.class);
    return results.toArray(new NavigationItem[0]);
  }
}
