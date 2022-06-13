// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.entities;

import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.Set;

import static org.angular2.entities.Angular2EntityUtils.forEachEntity;
import static org.angular2.entities.Angular2EntityUtils.forEachModule;

public interface Angular2Module extends Angular2Entity {

  @NotNull
  Set<Angular2Declaration> getDeclarations();

  boolean areDeclarationsFullyResolved();

  @NotNull
  Set<Angular2Entity> getImports();

  @NotNull
  Set<Angular2Entity> getExports();

  boolean areExportsFullyResolved();

  boolean isScopeFullyResolved();

  boolean isPublic();

  @NotNull
  Set<Angular2Declaration> getAllExportedDeclarations();

  /**
   * @see Angular2Component#getDeclarationsInScope()
   */
  default @NotNull Set<Angular2Declaration> getDeclarationsInScope() {
    Set<Angular2Declaration> result = new HashSet<>(getDeclarations());
    forEachEntity(
      getImports(),
      module -> result.addAll(module.getAllExportedDeclarations()),
      declaration -> {
        if (declaration.isStandalone()) result.add(declaration);
      }
    );
    forEachModule(getExports(), module -> result.addAll(module.getAllExportedDeclarations()));
    return result;
  }

  Angular2Module[] EMPTY_ARRAY = new Angular2Module[0];
}
