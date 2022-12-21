// Copyright 2000-2022 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.angular2.entities.metadata.stubs

import com.intellij.json.psi.JsonObject
import com.intellij.psi.stubs.IndexSink
import com.intellij.psi.stubs.StubElement
import com.intellij.psi.stubs.StubInputStream
import com.intellij.psi.stubs.StubOutputStream
import com.intellij.util.io.StringRef
import org.angular2.entities.metadata.Angular2MetadataElementTypes
import org.angular2.entities.metadata.psi.Angular2MetadataPipe
import org.angular2.index.Angular2MetadataPipeIndex
import org.angular2.lang.metadata.MetadataUtils
import java.io.IOException

class Angular2MetadataPipeStub : Angular2MetadataEntityStub<Angular2MetadataPipe> {

  private val myPipeName: StringRef?

  val pipeName: String
    get() = StringRef.toString(myPipeName)

  private constructor(memberName: String?,
                      parent: StubElement<*>?,
                      classSource: JsonObject,
                      pipeName: String) : super(memberName, parent, classSource, Angular2MetadataElementTypes.PIPE) {
    myPipeName = StringRef.fromString(pipeName)
  }

  @Throws(IOException::class)
  constructor(stream: StubInputStream, parent: StubElement<*>?) : super(stream, parent, Angular2MetadataElementTypes.PIPE) {
    myPipeName = stream.readName()
  }

  @Throws(IOException::class)
  override fun serialize(stream: StubOutputStream) {
    super.serialize(stream)
    writeString(myPipeName, stream)
  }

  override fun index(sink: IndexSink) {
    super.index(sink)
    sink.occurrence(Angular2MetadataPipeIndex.KEY, pipeName)
  }

  override val loadInOuts: Boolean
    get() {
      return false
    }

  companion object {

    fun createPipeStub(memberName: String?,
                       parent: StubElement<*>?,
                       classSource: JsonObject,
                       decoratorSource: JsonObject): Angular2MetadataPipeStub? {
      return getDecoratorInitializer<JsonObject>(decoratorSource)
        ?.findProperty(NAME)
        ?.let { MetadataUtils.readStringPropertyValue(it) }
        ?.let { pipeName -> Angular2MetadataPipeStub(memberName, parent, classSource, pipeName) }
    }
  }
}
