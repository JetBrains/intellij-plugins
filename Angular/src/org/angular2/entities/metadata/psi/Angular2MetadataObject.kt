// Copyright 2000-2022 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.angular2.entities.metadata.psi

import org.angular2.entities.metadata.stubs.Angular2MetadataObjectStub

class Angular2MetadataObject(element: Angular2MetadataObjectStub) : Angular2MetadataElement<Angular2MetadataObjectStub>(element) {

  override fun toString(): String {
    return (if (stub.memberName != null) stub.memberName!! + " " else "") + "<metadata object>"
  }
}
