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
package com.intellij.coldFusion.model.psi.stubs;

import com.intellij.coldFusion.model.psi.CfmlComponent;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Disposer;
import com.intellij.openapi.util.Key;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.stubs.StubIndex;
import com.intellij.psi.stubs.StubIndexKey;
import com.intellij.psi.util.PsiUtilBase;
import com.intellij.util.indexing.FileBasedIndex;
import gnu.trove.THashSet;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

/**
 * @author vnikolaenko
 */
public abstract class CfmlIndex implements Disposable {
  private static final Logger LOG = Logger.getInstance("#com.intellij.coldFusion.model.psi.stubs.CfmlIndex");
  private static final Key<GlobalSearchScope> MY_SCOPE_KEY = Key.create("default.cfml.scope");
  private static final Map<Project, CfmlIndex> managers = new HashMap<Project, CfmlIndex>();
  private final Project project;

  private CfmlIndex(Project project) {
    this.project = project;
  }

  public static synchronized CfmlIndex getInstance(@NotNull Project project) {
    CfmlIndex manager = managers.get(project);
    if (manager == null) {
      manager = new CfmlIndexManagerImpl(project);
      managers.put(project, manager);
      Disposer.register(project, manager);
    }
    return manager;
  }

  private static class CfmlIndexManagerImpl extends CfmlIndex {
    private CfmlIndexManagerImpl(Project project) {
      super(project);
    }
  }

  public GlobalSearchScope getSearchScope() {
    GlobalSearchScope allScope = project.getUserData(MY_SCOPE_KEY);
    if (allScope == null) {
      project.putUserData(MY_SCOPE_KEY,
                          allScope = GlobalSearchScope.projectScope(project));
    }
    return allScope;
  }

  @NotNull
  public Collection<CfmlComponent> getComponentsByName(@Nullable final String name) {
    return getComponentsByNameInScope(name, getSearchScope());
  }

  @NotNull
  public Collection<CfmlComponent> getInterfacesByName(@Nullable final String name) {
    return getInterfacesByNameInScope(name, getSearchScope());
  }

  @NotNull
  public Collection<CfmlComponent> getComponentsByNameInScope(@Nullable final String name, GlobalSearchScope scope) {
    if (name == null) return Collections.emptyList();
    Collection<CfmlComponent> cfmlComponents = StubIndex.getInstance().get(CfmlComponentIndex.KEY, name.toLowerCase(), project, scope);
    return workaroundIndexBug(cfmlComponents, CfmlComponent.class, CfmlComponentIndex.KEY);
  }

  @NotNull
  public Collection<CfmlComponent> getInterfacesByNameInScope(@Nullable final String name, GlobalSearchScope scope) {
    if (name == null) return Collections.emptyList();
    Collection<CfmlComponent> cfmlComponents = StubIndex.getInstance().get(CfmlInterfaceIndex.KEY, name.toLowerCase(), project, scope);
    return workaroundIndexBug(cfmlComponents, CfmlComponent.class, CfmlInterfaceIndex.KEY);
  }

  @NotNull
  public Collection<String> getAllComponentsNames() {
    return StubIndex.getInstance().getAllKeys(CfmlComponentIndex.KEY, project);
  }

  @NotNull
  public Collection<String> getAllInterfaceNames() {
    return StubIndex.getInstance().getAllKeys(CfmlInterfaceIndex.KEY, project);
  }

  public void dispose() {
    managers.remove(project);
  }

  // reused code (com.jetbrains.php.PHPIndex) for the same reason
  private static <T extends PsiElement> Collection<T> workaroundIndexBug(Collection<T> items, final Class<T> aClass,
                                                                         StubIndexKey key) {
    // following code is workaround against known yet unresolved bug with index corruption.
    // but lets be optimistic and suppose that most of the time data is ok

    boolean ok = true;
    for (PsiElement element : items) {
      if (!aClass.isInstance(element)) {
        rebuildFileIndex(element, key);
        ok = false;
        break;
      }
    }

    if (ok) return items;

    Set<T> result = new THashSet<T>(items.size());
    for (PsiElement element : items) {
      if (aClass.isInstance(element)) {
        //noinspection unchecked
        result.add((T)element);
      }
      else {
        rebuildFileIndex(element, key);
      }
    }
    return result;
  }

  public static void rebuildFileIndex(PsiElement element, StubIndexKey k) {
    VirtualFile faultyContainer = PsiUtilBase.getVirtualFile(element);
    LOG.warn("Wrong element " + element.getText() + " from " + faultyContainer + " in index: " + k);
    if (faultyContainer != null && faultyContainer.isValid()) {
      FileBasedIndex.getInstance().requestReindex(faultyContainer);
    }
  }
}
