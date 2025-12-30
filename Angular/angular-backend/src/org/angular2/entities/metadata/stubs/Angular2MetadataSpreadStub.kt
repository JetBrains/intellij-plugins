// Copyright 2000-2022 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.angular2.entities.metadata.stubs

import com.intellij.json.psi.JsonObject
import com.intellij.json.psi.JsonValue
import com.intellij.psi.stubs.StubElement
import com.intellij.psi.stubs.StubInputStream
import org.angular2.entities.metadata.Angular2MetadataElementTypes
import org.angular2.entities.metadata.psi.Angular2MetadataSpread
import org.angular2.lang.metadata.MetadataUtils.readStringPropertyValue
import org.jetbrains.annotations.NonNls
import java.io.IOException

class Angular2MetadataSpreadStub : Angular2MetadataElementStub<Angular2MetadataSpread> {

  val spreadExpression: Angular2MetadataElementStub<*>?
    get() = findMember(SPREAD_EXPRESSION) as? Angular2MetadataElementStub<*>

  private constructor(memberName: String?,
                      spreadExpression: JsonValue,
                      parent: StubElement<*>?) : super(memberName, parent, Angular2MetadataElementTypes.SPREAD) {
    createMember(SPREAD_EXPRESSION, spreadExpression)
  }

  @Throws(IOException::class)
  constructor(stream: StubInputStream, parent: StubElement<*>?) : super(stream, parent, Angular2MetadataElementTypes.SPREAD)

  companion object {

    @NonNls
    private val SPREAD_EXPRESSION = "#expression"

    fun createSpreadStub(memberName: String?,
                         source: JsonValue,
                         parent: StubElement<*>?): Angular2MetadataSpreadStub? {
      val sourceObject = source as JsonObject
      if (SYMBOL_SPREAD == readStringPropertyValue(sourceObject.findProperty(SYMBOL_TYPE))) {
        val spreadExpression = sourceObject.findProperty(EXPRESSION)?.value
        if (spreadExpression != null) {
          return Angular2MetadataSpreadStub(memberName, spreadExpression, parent)
        }
      }
      return null
    }
  }
}
