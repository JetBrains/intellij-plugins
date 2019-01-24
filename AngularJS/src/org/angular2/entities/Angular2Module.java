// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.entities;

import com.intellij.util.containers.ContainerUtil;
import com.intellij.util.containers.Stack;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.Set;

public interface Angular2Module extends Angular2Entity {

  @NotNull
  Set<Angular2Declaration> getDeclarations();

  boolean areDeclarationsFullyResolved();

  @NotNull
  Set<Angular2Module> getImports();

  @NotNull
  Set<Angular2Entity> getExports();

  boolean areExportsFullyResolved();

  boolean isScopeFullyResolved();

  @NotNull
  default Set<Angular2Declaration> getDeclarationsInScope() {
    Set<Angular2Declaration> result = new HashSet<>(getDeclarations());
    Set<Angular2Module> processedModules = ContainerUtil.newHashSet(this);
    Stack<Angular2Module> moduleQueue = new Stack<>(getImports());
    getExports().forEach(e -> {
      if (e instanceof Angular2Module) moduleQueue.add((Angular2Module)e);
    });
    while (!moduleQueue.empty()) {
      Angular2Module module = moduleQueue.pop();
      if (processedModules.add(module)) {
        for (Angular2Entity export : module.getExports()) {
          if (export instanceof Angular2Module) {
            moduleQueue.push((Angular2Module)export);
          }
          else if (export instanceof Angular2Declaration) {
            result.add((Angular2Declaration)export);
          }
          else {
            throw new IllegalArgumentException("Class " + export.getClass() + " extends neither Angular2Module nor Angular2Declaration");
          }
        }
      }
    }
    return result;
  }

  @NotNull
  default Set<Angular2Pipe> getPipesInScope() {
    return ContainerUtil.map2SetNotNull(getDeclarationsInScope(), decl -> {
      if (decl instanceof Angular2Pipe) {
        return (Angular2Pipe)decl;
      }
      return null;
    });
  }

  @NotNull
  default Set<Angular2Directive> getDirectivesInScope() {
    return ContainerUtil.map2SetNotNull(getDeclarationsInScope(), decl -> {
      if (decl instanceof Angular2Directive) {
        return (Angular2Directive)decl;
      }
      return null;
    });
  }
}
