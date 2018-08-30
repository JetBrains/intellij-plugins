// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.intellij.javascript.flex.mxml.schema;

import com.intellij.ProjectTopics;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ModuleRootEvent;
import com.intellij.openapi.roots.ModuleRootListener;
import gnu.trove.THashMap;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class CodeContextHolder {
  private final Set<Module> myModulesWithSdkComponentsHandled = new HashSet<>();
  private final Map<String, Map<Module, CodeContext>> myStandardContexts = new HashMap<>();
  private final Map<String, Map<Module, CodeContext>> myNSToCodeContextMap = new THashMap<>();
  static final CodeContext EMPTY = new CodeContext(null, null);

  public CodeContextHolder(Project project) {
    project.getMessageBus().connect().subscribe(ProjectTopics.PROJECT_ROOTS, new ModuleRootListener() {
      @Override
      public void rootsChanged(@NotNull final ModuleRootEvent event) {
        synchronized (CodeContextHolder.this) {
          myNSToCodeContextMap.clear();
          myStandardContexts.clear();
          myModulesWithSdkComponentsHandled.clear();
        }
      }
    });
  }

  @Nullable
  public synchronized CodeContext getCodeContext(@NotNull final String namespace, @NotNull final Module module) {
    final Map<Module, CodeContext> map = myNSToCodeContextMap.get(namespace);
    if (map != null) return map.get(module);
    return null;
  }

  public synchronized void putCodeContext(@NotNull final String namespace, @NotNull final Module module, @NotNull final CodeContext codeContext) {
    Map<Module, CodeContext> map = myNSToCodeContextMap.get(namespace);
    if (map == null) {
      map = new THashMap<>();
      myNSToCodeContextMap.put(namespace, map);
    }
    map.put(module, codeContext);
  }

  public synchronized void clearCodeContext(@NotNull final String namespace, @NotNull final Module module) {
    Map<Module, CodeContext> map = myNSToCodeContextMap.get(namespace);
    if (map != null) {
      map.remove(module);
    }
  }

  public static CodeContextHolder getInstance(@NotNull Project project) {
    return project.getComponent(CodeContextHolder.class);
  }

  @Nullable
  public synchronized CodeContext getStandardContext(final String namespace, final Module module) {
    final Map<Module, CodeContext> map = myStandardContexts.get(namespace);
    return map == null ? null : map.get(module);
  }

  public synchronized Collection<String> getNamespaces(final Module module) {
    final List<String> result = new ArrayList<>();
    for (final Map.Entry<String, Map<Module, CodeContext>> entry : myStandardContexts.entrySet()) {
      if (entry.getValue().containsKey(module)) {
        result.add(entry.getKey());
      }
    }
    for (final Map.Entry<String, Map<Module, CodeContext>> entry : myNSToCodeContextMap.entrySet()) {
      if (entry.getValue().containsKey(module)) {
        result.add(entry.getKey());
      }
    }
    return result;
  }

  synchronized void putStandardContext(final String namespace, final Module module, final CodeContext codeContext) {
    Map<Module, CodeContext> map = myStandardContexts.get(namespace);
    if (map == null) {
      map = new HashMap<>();
      myStandardContexts.put(namespace, map);
    }
    map.put(module, codeContext);
  }

  synchronized boolean areSdkComponentsHandledForModule(final Module module) {
    return myModulesWithSdkComponentsHandled.contains(module);
  }

  synchronized boolean setSdkComponentsHandledForModule(final Module module) {
    return myModulesWithSdkComponentsHandled.add(module);
  }
}
