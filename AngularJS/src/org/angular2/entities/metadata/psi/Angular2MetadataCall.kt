// Copyright 2000-2022 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.angular2.entities.metadata.psi

import org.angular2.entities.metadata.stubs.Angular2MetadataCallStub

class Angular2MetadataCall(element: Angular2MetadataCallStub) : Angular2MetadataElement<Angular2MetadataCallStub>(element) {

  val value: Angular2MetadataElement<*>?
    get() {
      var callResult = stub.callResult?.psi
      val refs = HashSet<Angular2MetadataElement<*>>()
      while (callResult is Angular2MetadataReference && refs.add(callResult)) {
        callResult = callResult.resolve()
      }
      return if (callResult is Angular2MetadataFunction) {
        callResult.value
      }
      else null
    }

  override fun toString(): String {
    val memberName = stub.memberName
    return (if (memberName == null) "" else "$memberName: ") + "<metadata call>"
  }
}
