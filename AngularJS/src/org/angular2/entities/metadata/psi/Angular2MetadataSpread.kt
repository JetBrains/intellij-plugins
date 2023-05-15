// Copyright 2000-2022 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.angular2.entities.metadata.psi

import org.angular2.entities.metadata.stubs.Angular2MetadataSpreadStub

class Angular2MetadataSpread(element: Angular2MetadataSpreadStub) : Angular2MetadataElement<Angular2MetadataSpreadStub>(element) {

  val expression: Angular2MetadataElement<*>?
    get() = stub.spreadExpression?.psi as? Angular2MetadataElement<*>

  override fun toString(): String {
    val memberName = stub.memberName
    return (if (memberName == null) "" else "$memberName: ") + "<metadata spread>"
  }
}
