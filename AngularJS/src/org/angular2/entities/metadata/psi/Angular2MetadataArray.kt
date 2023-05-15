// Copyright 2000-2022 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.angular2.entities.metadata.psi

import org.angular2.entities.metadata.stubs.Angular2MetadataArrayStub

class Angular2MetadataArray(element: Angular2MetadataArrayStub) : Angular2MetadataElement<Angular2MetadataArrayStub>(element) {

  override fun toString(): String {
    return (if (stub.memberName != null) stub.memberName!! + " " else "") + "<metadata array>"
  }
}
