// Copyright 2000-2022 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.angular2.entities.ivy

import org.angular2.entities.Angular2Declaration

abstract class Angular2IvyDeclaration<T : Angular2IvySymbolDef.Entity>(entityDef: T)
  : Angular2IvyEntity<T>(entityDef), Angular2Declaration {

  override val isStandalone: Boolean
    get() = myEntityDef.isStandalone
}
