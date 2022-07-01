// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.entities.ivy;

import org.angular2.entities.Angular2Declaration;
import org.jetbrains.annotations.NotNull;

public abstract class Angular2IvyDeclaration<T extends Angular2IvySymbolDef.Entity> extends Angular2IvyEntity<T>
  implements Angular2Declaration {
  public Angular2IvyDeclaration(@NotNull T entityDef) {
    super(entityDef);
  }

  @Override
  public boolean isStandalone() {
    return myEntityDef.isStandalone();
  }
}
