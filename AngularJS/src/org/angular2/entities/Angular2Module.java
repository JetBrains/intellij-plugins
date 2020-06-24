// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.entities;

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

  boolean isPublic();

  @NotNull
  Set<Angular2Declaration> getAllExportedDeclarations();

  default @NotNull Set<Angular2Declaration> getDeclarationsInScope() {
    Set<Angular2Declaration> result = new HashSet<>(getDeclarations());
    for (Angular2Module imported : getImports()) {
      result.addAll(imported.getAllExportedDeclarations());
    }
    for (Angular2Entity entity : getExports()) {
      if (entity instanceof Angular2Module) {
        result.addAll(((Angular2Module)entity).getAllExportedDeclarations());
      }
    }
    return result;
  }

  Angular2Module[] EMPTY_ARRAY = new Angular2Module[0];
}
