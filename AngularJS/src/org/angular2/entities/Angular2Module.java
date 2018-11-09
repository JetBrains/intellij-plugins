// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.entities;

import org.jetbrains.annotations.NotNull;

public interface Angular2Module extends Angular2Entity {

  @NotNull
  Angular2Declaration[] getDeclarations();

  @NotNull
  Angular2Module[] getImports();

  @NotNull
  Angular2Declaration[] getExports();

  @NotNull
  Angular2Pipe[] getPipesInScope();

  @NotNull
  Angular2Directive getDirectivesInScope();
}
