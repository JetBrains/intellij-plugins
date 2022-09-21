// Copyright 2000-2021 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.intellij.javascript.flex.mxml.schema;

import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Pair;
import com.intellij.psi.search.GlobalSearchScope;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public final class CodeContextHolder {
  private final Set<Module> myModulesWithSdkComponentsHandled = new HashSet<>();
  private final Map<String, Map<Module, CodeContext>> myStandardContexts = new HashMap<>();
  private final Map<String, Map<Pair<Module, GlobalSearchScope>, CodeContext>> myNSToCodeContextMap = new HashMap<>();

  synchronized void clear() {
    myNSToCodeContextMap.clear();
    myStandardContexts.clear();
    myModulesWithSdkComponentsHandled.clear();
  }

  @Nullable
  public synchronized CodeContext getCodeContext(@NotNull String namespace, @NotNull Module module, @NotNull GlobalSearchScope scope) {
    Map<Pair<Module, GlobalSearchScope>, CodeContext> map = myNSToCodeContextMap.get(namespace);
    return map != null ? map.get(Pair.create(module, scope)) : null;
  }

  synchronized void putCodeContext(@NotNull String namespace, @NotNull Module module, @NotNull GlobalSearchScope scope, @NotNull CodeContext codeContext) {
    var map = myNSToCodeContextMap.get(namespace);
    if (map == null) {
      map = new HashMap<>();
      myNSToCodeContextMap.put(namespace, map);
    }
    map.put(Pair.create(module, scope), codeContext);
  }

  synchronized void clearCodeContext(@NotNull String namespace, @NotNull Module module, @NotNull GlobalSearchScope scope) {
    var map = myNSToCodeContextMap.get(namespace);
    if (map != null) {
      map.remove(Pair.create(module, scope));
    }
  }

  public static CodeContextHolder getInstance(@NotNull Project project) {
    return project.getService(CodeContextHolder.class);
  }

  @Nullable
  public synchronized CodeContext getStandardContext(final String namespace, final Module module) {
    final Map<Module, CodeContext> map = myStandardContexts.get(namespace);
    return map == null ? null : map.get(module);
  }

  public synchronized Collection<String> getNamespaces(Module module, GlobalSearchScope scope) {
    final List<String> result = new ArrayList<>();
    for (final Map.Entry<String, Map<Module, CodeContext>> entry : myStandardContexts.entrySet()) {
      if (entry.getValue().containsKey(module)) {
        result.add(entry.getKey());
      }
    }
    for (var entry : myNSToCodeContextMap.entrySet()) {
      if (entry.getValue().containsKey(Pair.create(module, scope))) {
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
