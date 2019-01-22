// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.entities;

import com.intellij.util.containers.ContainerUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Collections;

import static org.angular2.entities.Angular2EntitiesProvider.getDeclarationToModuleMap;

public interface Angular2Declaration extends Angular2Entity {

  @Nullable
  default Angular2Module getModule() {
    return ContainerUtil.getFirstItem(getAllModules());
  }

  @NotNull
  default Collection<Angular2Module> getAllModules() {
    return Collections.unmodifiableCollection(
      getDeclarationToModuleMap(getSourceElement().getProject()).get(this));
  }
}
