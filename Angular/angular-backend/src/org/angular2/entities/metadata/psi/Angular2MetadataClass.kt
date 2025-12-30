// Copyright 2000-2022 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.angular2.entities.metadata.psi

import org.angular2.entities.metadata.stubs.Angular2MetadataClassStub

class Angular2MetadataClass(element: Angular2MetadataClassStub) : Angular2MetadataClassBase<Angular2MetadataClassStub>(element) {

  override fun toString(): String {
    return "${getName()} <metadata class>"
  }
}
