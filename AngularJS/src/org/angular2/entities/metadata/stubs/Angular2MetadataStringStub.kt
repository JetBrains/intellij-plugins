// Copyright 2000-2022 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.angular2.entities.metadata.stubs

import com.intellij.json.psi.JsonStringLiteral
import com.intellij.json.psi.JsonValue
import com.intellij.psi.stubs.StubElement
import com.intellij.psi.stubs.StubInputStream
import com.intellij.psi.stubs.StubOutputStream
import com.intellij.util.io.StringRef
import org.angular2.entities.metadata.Angular2MetadataElementTypes
import org.angular2.entities.metadata.psi.Angular2MetadataString
import java.io.IOException

class Angular2MetadataStringStub : Angular2MetadataElementStub<Angular2MetadataString> {

  private val myValue: StringRef

  val value: String
    get() = myValue.string

  constructor(memberName: String?, source: JsonValue, parent: StubElement<*>?)
    : super(memberName, parent, Angular2MetadataElementTypes.STRING) {
    myValue = StringRef.fromString((source as JsonStringLiteral).value)
  }

  @Throws(IOException::class)
  constructor(stream: StubInputStream, parent: StubElement<*>?)
    : super(stream, parent, Angular2MetadataElementTypes.STRING) {
    myValue = stream.readName()!!
  }

  @Throws(IOException::class)
  override fun serialize(stream: StubOutputStream) {
    super.serialize(stream)
    writeString(myValue, stream)
  }
}
