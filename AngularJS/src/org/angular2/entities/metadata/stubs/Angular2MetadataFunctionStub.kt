// Copyright 2000-2022 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.angular2.entities.metadata.stubs

import com.intellij.json.psi.JsonObject
import com.intellij.json.psi.JsonValue
import com.intellij.psi.stubs.IndexSink
import com.intellij.psi.stubs.StubElement
import com.intellij.psi.stubs.StubInputStream
import org.angular2.entities.metadata.Angular2MetadataElementTypes
import org.angular2.entities.metadata.psi.Angular2MetadataFunction
import org.angular2.index.Angular2MetadataFunctionIndex
import org.angular2.lang.metadata.MetadataUtils.readStringPropertyValue
import org.jetbrains.annotations.NonNls
import java.io.IOException

class Angular2MetadataFunctionStub : Angular2MetadataElementStub<Angular2MetadataFunction> {

  val functionValue: Angular2MetadataElementStub<*>?
    get() = findMember(VALUE_OBJ) as? Angular2MetadataElementStub<*>

  @Throws(IOException::class)
  constructor(stream: StubInputStream, parent: StubElement<*>?)
    : super(stream, parent, Angular2MetadataElementTypes.FUNCTION)

  constructor(memberName: String, value: JsonValue, parent: StubElement<*>?)
    : super(memberName, parent, Angular2MetadataElementTypes.FUNCTION) {
    createMember(VALUE_OBJ, value)
  }

  override fun index(sink: IndexSink) {
    super.index(sink)
    if (memberName != null) {
      sink.occurrence(Angular2MetadataFunctionIndex.KEY, memberName!!)
    }
  }

  companion object {

    @NonNls
    private val VALUE_OBJ = "#value"

    fun createFunctionStub(memberName: String?,
                           source: JsonValue,
                           parent: StubElement<*>?): Angular2MetadataFunctionStub? {
      val sourceObject = source as JsonObject
      if (memberName != null && SYMBOL_FUNCTION == readStringPropertyValue(
          sourceObject.findProperty(SYMBOL_TYPE))) {
        val value = sourceObject.findProperty(FUNCTION_VALUE)?.value
        if (value != null) {
          return Angular2MetadataFunctionStub(memberName, value, parent)
        }
      }
      return null
    }
  }
}
