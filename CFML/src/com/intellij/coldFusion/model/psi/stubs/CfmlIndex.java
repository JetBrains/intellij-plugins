// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.intellij.coldFusion.model.psi.stubs;

import com.intellij.coldFusion.model.psi.CfmlComponent;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Disposer;
import com.intellij.openapi.util.Key;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.stubs.StubIndex;
import com.intellij.psi.stubs.StubIndexKey;
import com.intellij.psi.util.PsiUtilCore;
import com.intellij.util.indexing.FileBasedIndex;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public abstract class CfmlIndex implements Disposable {
  private static final Logger LOG = Logger.getInstance(CfmlIndex.class);
  private static final Key<GlobalSearchScope> MY_SCOPE_KEY = Key.create("default.cfml.scope");
  private static final Map<Project, CfmlIndex> managers = new HashMap<>();
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

  private static final class CfmlIndexManagerImpl extends CfmlIndex {
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

  public @NotNull Collection<CfmlComponent> getComponentsByName(final @Nullable String name) {
    return getComponentsByNameInScope(name, getSearchScope());
  }

  public @NotNull Collection<CfmlComponent> getInterfacesByName(final @Nullable String name) {
    return getInterfacesByNameInScope(name, getSearchScope());
  }

  public @NotNull Collection<CfmlComponent> getComponentsByNameInScope(final @Nullable String name, GlobalSearchScope scope) {
    if (name == null) return Collections.emptyList();
    Collection<CfmlComponent> cfmlComponents = StubIndex.getElements(CfmlComponentIndex.KEY, StringUtil.toLowerCase(name), project, scope,
                                                                     CfmlComponent.class);
    return workaroundIndexBug(cfmlComponents, CfmlComponent.class, CfmlComponentIndex.KEY);
  }

  public @NotNull Collection<CfmlComponent> getInterfacesByNameInScope(final @Nullable String name, GlobalSearchScope scope) {
    if (name == null) return Collections.emptyList();
    Collection<CfmlComponent> cfmlComponents = StubIndex.getElements(CfmlInterfaceIndex.KEY, StringUtil.toLowerCase(name), project, scope,
                                                                     CfmlComponent.class);
    return workaroundIndexBug(cfmlComponents, CfmlComponent.class, CfmlInterfaceIndex.KEY);
  }

  public @NotNull Collection<String> getAllComponentsNames() {
    return StubIndex.getInstance().getAllKeys(CfmlComponentIndex.KEY, project);
  }

  public @NotNull Collection<String> getAllInterfaceNames() {
    return StubIndex.getInstance().getAllKeys(CfmlInterfaceIndex.KEY, project);
  }

  @Override
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

    Set<T> result = new HashSet<>(items.size());
    for (T element : items) {
      if (aClass.isInstance(element)) {
        result.add(element);
      }
      else {
        rebuildFileIndex(element, key);
      }
    }
    return result;
  }

  public static void rebuildFileIndex(PsiElement element, StubIndexKey k) {
    VirtualFile faultyContainer = PsiUtilCore.getVirtualFile(element);
    LOG.warn("Wrong element " + element.getText() + " from " + faultyContainer + " in index: " + k);
    if (faultyContainer != null && faultyContainer.isValid()) {
      FileBasedIndex.getInstance().requestReindex(faultyContainer);
    }
  }
}
