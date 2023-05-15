// Copyright 2000-2022 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.angular2.entities.metadata.stubs

import com.intellij.json.psi.JsonArray
import com.intellij.json.psi.JsonObject
import com.intellij.lang.javascript.index.flags.BooleanStructureElement
import com.intellij.lang.javascript.index.flags.FlagsStructure
import com.intellij.psi.stubs.StubElement
import com.intellij.psi.stubs.StubInputStream
import com.intellij.psi.stubs.StubOutputStream
import com.intellij.util.asSafely
import com.intellij.util.io.StringRef
import org.angular2.entities.metadata.Angular2MetadataElementTypes
import org.angular2.entities.metadata.psi.Angular2MetadataModuleExport
import org.angular2.lang.metadata.MetadataUtils
import org.jetbrains.annotations.NonNls
import java.io.IOException
import java.util.*

class Angular2MetadataModuleExportStub : Angular2MetadataElementStub<Angular2MetadataModuleExport> {

  private val myFrom: StringRef?
  val exportMappings: Map<String, String>

  val from: String?
    get() = StringRef.toString(myFrom)

  constructor(parent: StubElement<*>, source: JsonObject)
    : super(null as String?, parent, Angular2MetadataElementTypes.MODULE_EXPORT) {
    myFrom = StringRef.fromString(MetadataUtils.readStringPropertyValue(source.findProperty(FROM)))
    exportMappings = source.findProperty(EXPORT)
                       ?.value
                       ?.asSafely<JsonArray>()
                       ?.valueList
                       ?.filterIsInstance<JsonObject>()
                       ?.mapNotNull { obj ->
                         val name = MetadataUtils.readStringPropertyValue(obj.findProperty(NAME))
                         val `as` = MetadataUtils.readStringPropertyValue(obj.findProperty(AS))
                         if (name == null || `as` == null)
                           null
                         else
                           Pair(`as`, name)
                       }
                       ?.toMap()
                     ?: emptyMap()
  }

  @Throws(IOException::class)
  constructor(stream: StubInputStream, parent: StubElement<*>?)
    : super(stream, parent, Angular2MetadataElementTypes.MODULE_EXPORT) {
    myFrom = stream.readName()
    exportMappings = if (readFlag(HAS_EXPORT_MAPPINGS)) Collections.unmodifiableMap(readStringMap(stream))
    else emptyMap()
  }


  @Throws(IOException::class)
  override fun serialize(stream: StubOutputStream) {
    writeFlag(HAS_EXPORT_MAPPINGS, !exportMappings.isEmpty())
    super.serialize(stream)
    writeString(myFrom, stream)
    if (!exportMappings.isEmpty()) {
      writeStringMap(exportMappings, stream)
    }
  }

  override val flagsStructure: FlagsStructure
    get() = FLAGS_STRUCTURE

  companion object {

    @NonNls
    private val FROM = "from"

    @NonNls
    private val EXPORT = "export"

    @NonNls
    private val AS = "as"

    @NonNls
    private val NAME = "name"

    private val HAS_EXPORT_MAPPINGS = BooleanStructureElement()
    private val FLAGS_STRUCTURE = FlagsStructure(
      Angular2MetadataElementStub.FLAGS_STRUCTURE,
      HAS_EXPORT_MAPPINGS
    )
  }
}
