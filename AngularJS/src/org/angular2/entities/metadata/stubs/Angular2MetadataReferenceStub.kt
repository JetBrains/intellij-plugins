// Copyright 2000-2022 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.angular2.entities.metadata.stubs

import com.intellij.json.psi.JsonObject
import com.intellij.json.psi.JsonValue
import com.intellij.lang.javascript.index.flags.BooleanStructureElement
import com.intellij.lang.javascript.index.flags.FlagsStructure
import com.intellij.psi.stubs.StubElement
import com.intellij.psi.stubs.StubInputStream
import com.intellij.psi.stubs.StubOutputStream
import com.intellij.util.io.StringRef
import org.angular2.entities.metadata.Angular2MetadataElementTypes
import org.angular2.entities.metadata.psi.Angular2MetadataReference
import org.angular2.lang.metadata.MetadataUtils.readStringPropertyValue
import java.io.IOException

class Angular2MetadataReferenceStub : Angular2MetadataElementStub<Angular2MetadataReference> {

  private val myName: StringRef?
  private val myModule: StringRef?

  val name: String
    get() = StringRef.toString(myName)

  val module: String?
    get() = StringRef.toString(myModule)

  private constructor(memberName: String?,
                      name: String,
                      module: String?,
                      parent: StubElement<*>?) : super(memberName, parent, Angular2MetadataElementTypes.REFERENCE) {
    myName = StringRef.fromString(name)
    myModule = StringRef.fromString(module)
  }

  @Throws(IOException::class)
  constructor(stream: StubInputStream, parent: StubElement<*>?) : super(stream, parent, Angular2MetadataElementTypes.REFERENCE) {
    myName = stream.readName()
    myModule = if (readFlag(HAS_MODULE_NAME)) stream.readName() else null
  }

  @Throws(IOException::class)
  override fun serialize(stream: StubOutputStream) {
    writeFlag(HAS_MODULE_NAME, myModule != null)
    super.serialize(stream)
    writeString(myName, stream)
    if (myModule != null) {
      writeString(myModule, stream)
    }
  }

  override val flagsStructure: FlagsStructure
    get() = FLAGS_STRUCTURE

  companion object {

    fun createReferenceStub(memberName: String?,
                            source: JsonValue,
                            parent: StubElement<*>?): Angular2MetadataReferenceStub? {
      val sourceObject = source as JsonObject
      if (SYMBOL_REFERENCE == readStringPropertyValue(sourceObject.findProperty(SYMBOL_TYPE))) {
        val name = readStringPropertyValue(sourceObject.findProperty(REFERENCE_NAME))
        val module = readStringPropertyValue(sourceObject.findProperty(REFERENCE_MODULE))
        if (name != null) {
          return Angular2MetadataReferenceStub(memberName, name, module, parent)
        }
      }
      return null
    }

    private val HAS_MODULE_NAME = BooleanStructureElement()
    private val FLAGS_STRUCTURE = FlagsStructure(
      Angular2MetadataElementStub.FLAGS_STRUCTURE,
      HAS_MODULE_NAME
    )
  }
}
