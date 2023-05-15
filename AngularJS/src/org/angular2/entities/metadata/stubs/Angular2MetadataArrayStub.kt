// Copyright 2000-2022 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.angular2.entities.metadata.stubs

import com.intellij.json.psi.JsonArray
import com.intellij.json.psi.JsonValue
import com.intellij.psi.stubs.StubElement
import com.intellij.psi.stubs.StubInputStream
import org.angular2.entities.metadata.Angular2MetadataElementTypes
import org.angular2.entities.metadata.psi.Angular2MetadataArray

import java.io.IOException

class Angular2MetadataArrayStub : Angular2MetadataElementStub<Angular2MetadataArray> {

  @Throws(IOException::class)
  constructor(stream: StubInputStream, parent: StubElement<*>?)
    : super(stream, parent, Angular2MetadataElementTypes.ARRAY)

  constructor(memberName: String?, source: JsonValue, parent: StubElement<*>?)
    : super(memberName, parent, Angular2MetadataElementTypes.ARRAY) {
    (source as JsonArray).valueList.forEach { v -> createMember(null, v) }
  }
}
