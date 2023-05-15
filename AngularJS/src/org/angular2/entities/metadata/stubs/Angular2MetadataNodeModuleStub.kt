// Copyright 2000-2022 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.angular2.entities.metadata.stubs

import com.intellij.json.psi.JsonArray
import com.intellij.json.psi.JsonObject
import com.intellij.json.psi.JsonValue
import com.intellij.psi.stubs.IndexSink
import com.intellij.psi.stubs.StubElement
import com.intellij.psi.stubs.StubInputStream
import com.intellij.psi.stubs.StubOutputStream
import com.intellij.util.io.StringRef
import org.angular2.entities.metadata.Angular2MetadataElementTypes
import org.angular2.entities.metadata.psi.Angular2MetadataNodeModule
import org.angular2.index.Angular2MetadataNodeModuleIndex
import org.angular2.lang.metadata.MetadataUtils
import org.jetbrains.annotations.NonNls

import java.io.IOException
import java.util.function.Consumer

class Angular2MetadataNodeModuleStub : Angular2MetadataElementStub<Angular2MetadataNodeModule> {

  private val myImportAs: StringRef?

  val importAs: String?
    get() = StringRef.toString(myImportAs)

  @Throws(IOException::class)
  constructor(stream: StubInputStream, parentStub: StubElement<*>?)
    : super(stream, parentStub, Angular2MetadataElementTypes.NODE_MODULE) {
    myImportAs = stream.readName()
  }

  constructor(parentStub: StubElement<*>?, fileRoot: JsonValue?)
    : super(null as String?, parentStub, Angular2MetadataElementTypes.NODE_MODULE) {
    val fileRootObj = if (fileRoot is JsonArray)
      fileRoot.valueList[0]
    else
      fileRoot
    if (fileRootObj is JsonObject) {
      myImportAs = StringRef.fromString(MetadataUtils.readStringPropertyValue(fileRootObj.findProperty(IMPORT_AS)))
      MetadataUtils.getPropertyValue<JsonArray>(fileRootObj.findProperty(EXPORTS))
        ?.valueList
        ?.forEach {
          if (it is JsonObject) {
            Angular2MetadataModuleExportStub(this, it)
          }
        }
      MetadataUtils.listObjectProperties(fileRootObj.findProperty(METADATA))
        .forEach(Consumer { this.loadMemberProperty(it) })
    }
    else {
      myImportAs = null
    }
  }

  override fun index(sink: IndexSink) {
    super.index(sink)
    if (importAs != null) {
      sink.occurrence(Angular2MetadataNodeModuleIndex.KEY, importAs!!)
    }
  }

  @Throws(IOException::class)
  override fun serialize(stream: StubOutputStream) {
    super.serialize(stream)
    writeString(myImportAs, stream)
  }

  companion object {

    @NonNls
    private const val IMPORT_AS = "importAs"

    @NonNls
    private const val EXPORTS = "exports"
    private const val METADATA = "metadata"
  }
}
