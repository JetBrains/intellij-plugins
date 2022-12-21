// Copyright 2000-2022 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.angular2.entities.metadata.psi

import org.angular2.entities.metadata.stubs.Angular2MetadataStringStub

class Angular2MetadataString(element: Angular2MetadataStringStub) : Angular2MetadataElement<Angular2MetadataStringStub>(element) {

  val value: String
    get() = stub.value

  override fun toString(): String {
    val memberName = stub.memberName
    return ((if (memberName == null) "" else "$memberName: ")
            + value + " <metadata string>")
  }
}
