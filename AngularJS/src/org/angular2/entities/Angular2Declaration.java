// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.entities;

import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Collections;

import static org.angular2.entities.Angular2EntitiesProvider.getDeclarationToModuleMap;

public interface Angular2Declaration extends Angular2Entity {

  default @NotNull Collection<Angular2Module> getAllModules() {
    return Collections.unmodifiableCollection(
      getDeclarationToModuleMap(getSourceElement().getProject()).get(this));
  }
}
