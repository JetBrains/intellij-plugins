// Copyright 2000-2022 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.angular2.entities.metadata.stubs

import com.intellij.json.psi.JsonObject
import com.intellij.json.psi.JsonValue
import com.intellij.psi.stubs.StubElement
import com.intellij.psi.stubs.StubInputStream
import org.angular2.entities.metadata.Angular2MetadataElementTypes
import org.angular2.entities.metadata.psi.Angular2MetadataCall
import org.angular2.lang.metadata.MetadataUtils.readStringPropertyValue
import org.jetbrains.annotations.NonNls
import java.io.IOException

class Angular2MetadataCallStub : Angular2MetadataElementStub<Angular2MetadataCall> {

  val callResult: Angular2MetadataElementStub<*>?
    get() = findMember(CALL_RESULT) as? Angular2MetadataElementStub<*>

  private constructor(memberName: String?,
                      callResult: JsonValue,
                      parent: StubElement<*>?) : super(memberName, parent, Angular2MetadataElementTypes.CALL) {
    createMember(CALL_RESULT, callResult)
  }

  @Throws(IOException::class)
  constructor(stream: StubInputStream, parent: StubElement<*>?) : super(stream, parent, Angular2MetadataElementTypes.CALL)

  companion object {

    @NonNls
    private val CALL_RESULT = "#expression"

    fun createCallStub(memberName: String?,
                       source: JsonValue,
                       parent: StubElement<*>?): Angular2MetadataCallStub? {
      val sourceObject = source as JsonObject
      if (SYMBOL_CALL == readStringPropertyValue(sourceObject.findProperty(SYMBOL_TYPE))) {
        val callResult = sourceObject.findProperty(EXPRESSION)?.value
        if (callResult != null) {
          return Angular2MetadataCallStub(memberName, callResult, parent)
        }
      }
      return null
    }
  }
}
