// Copyright 2000-2022 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.angular2.entities.metadata.psi

import org.angular2.entities.Angular2Declaration
import org.angular2.entities.metadata.stubs.Angular2MetadataEntityStub

abstract class Angular2MetadataDeclaration<Stub : Angular2MetadataEntityStub<*>>(element: Stub)
  : Angular2MetadataEntity<Stub>(element), Angular2Declaration {

  override // Standalone Components are a post-Ivy addition
  val isStandalone: Boolean
    get() = false
}
