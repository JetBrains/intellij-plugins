// Copyright 2000-2022 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.angular2.entities.metadata.psi

import org.angular2.entities.metadata.stubs.Angular2MetadataFunctionStub

class Angular2MetadataFunction(element: Angular2MetadataFunctionStub) : Angular2MetadataElement<Angular2MetadataFunctionStub>(element) {

  val value: Angular2MetadataElement<*>?
    get() = stub.functionValue?.psi as? Angular2MetadataElement<*>

  override fun toString(): String {
    return (if (stub.memberName != null) stub.memberName!! + " " else "") + "<metadata function>"
  }
}
