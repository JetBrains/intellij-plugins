// Copyright 2000-2022 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.angular2.entities.metadata.stubs

import com.intellij.json.psi.*
import com.intellij.lang.javascript.index.flags.BooleanStructureElement
import com.intellij.lang.javascript.index.flags.FlagsStructure
import com.intellij.psi.PsiElement
import com.intellij.psi.stubs.IndexSink
import com.intellij.psi.stubs.StubElement
import com.intellij.psi.stubs.StubInputStream
import com.intellij.psi.stubs.StubOutputStream
import com.intellij.util.io.StringRef
import one.util.streamex.EntryStream
import org.angular2.Angular2DecoratorUtil.ATTRIBUTE_DEC
import org.angular2.Angular2DecoratorUtil.EXPORT_AS_PROP
import org.angular2.Angular2DecoratorUtil.INPUTS_PROP
import org.angular2.Angular2DecoratorUtil.OUTPUTS_PROP
import org.angular2.Angular2DecoratorUtil.SELECTOR_PROP
import org.angular2.entities.Angular2EntityUtils
import org.angular2.index.Angular2MetadataDirectiveIndexKey
import org.angular2.lang.metadata.MetadataUtils.getPropertyValue
import org.angular2.lang.metadata.MetadataUtils.readStringPropertyValue
import org.angular2.lang.metadata.psi.MetadataElementType
import java.io.IOException
import java.util.Collections.emptyMap
import java.util.stream.Collectors.toMap

abstract class Angular2MetadataDirectiveStubBase<Psi : PsiElement> : Angular2MetadataEntityStub<Psi> {

  private val mySelector: StringRef?
  private val myExportAs: StringRef?

  val attributes: Map<String, Int>

  val selector: String?
    get() = StringRef.toString(mySelector)

  val exportAs: String?
    get() = StringRef.toString(myExportAs)

  constructor(memberName: String?,
              parent: StubElement<*>?,
              source: JsonObject,
              decoratorSource: JsonObject,
              elementType: MetadataElementType<*>) : super(memberName, parent, source, elementType) {
    attributes = loadAttributesMapping(source)

    val initializer = getDecoratorInitializer<JsonObject>(decoratorSource)

    if (initializer == null) {
      mySelector = null
      myExportAs = null
      return
    }

    mySelector = StringRef.fromString(readStringPropertyValue(initializer.findProperty(SELECTOR_PROP)))
    myExportAs = StringRef.fromString(readStringPropertyValue(initializer.findProperty(EXPORT_AS_PROP)))
    loadAdditionalBindingMappings(myInputMappings, initializer, INPUTS_PROP)
    loadAdditionalBindingMappings(myOutputMappings, initializer, OUTPUTS_PROP)
  }

  @Throws(IOException::class)
  constructor(stream: StubInputStream,
              parent: StubElement<*>?,
              elementType: MetadataElementType<*>) : super(stream, parent, elementType) {
    mySelector = stream.readName()
    myExportAs = if (readFlag(HAS_EXPORT_AS)) stream.readName() else null
    attributes = if (readFlag(HAS_ATTRIBUTES)) readIntegerMap(stream) else emptyMap()
  }

  @Throws(IOException::class)
  override fun serialize(stream: StubOutputStream) {
    writeFlag(HAS_EXPORT_AS, myExportAs != null)
    writeFlag(HAS_ATTRIBUTES, !attributes.isEmpty())
    super.serialize(stream)
    writeString(mySelector, stream)
    if (myExportAs != null) {
      writeString(myExportAs, stream)
    }

    if (!attributes.isEmpty()) {
      writeIntegerMap(attributes, stream)
    }
  }

  override fun index(sink: IndexSink) {
    super.index(sink)
    if (selector != null) {
      Angular2EntityUtils.getDirectiveIndexNames(selector!!)
        .forEach { indexName -> sink.occurrence(Angular2MetadataDirectiveIndexKey, indexName) }
    }
  }

  override val flagsStructure: FlagsStructure
    get() = FLAGS_STRUCTURE

  private fun loadAdditionalBindingMappings(mappings: MutableMap<String, String>,
                                            initializer: JsonObject,
                                            propertyName: String) {
    val list = initializer.findProperty(propertyName)?.value as? JsonArray
    if (list != null && list.valueList.all { it is JsonStringLiteral }) {
      for (v in list.valueList) {
        val value = (v as JsonStringLiteral).value
        val p = Angular2EntityUtils.parsePropertyMapping(value, v)
        mappings.putIfAbsent(p.first, p.second.name)
      }
    }
    else {
      stubDecoratorFields(initializer, propertyName)
    }
  }

  companion object {
    private val HAS_EXPORT_AS = BooleanStructureElement()
    private val HAS_ATTRIBUTES = BooleanStructureElement()

    @JvmStatic
    protected val FLAGS_STRUCTURE: FlagsStructure = FlagsStructure(
      Angular2MetadataClassStubBase.FLAGS_STRUCTURE,
      HAS_EXPORT_AS,
      HAS_ATTRIBUTES
    )

    private fun loadAttributesMapping(source: JsonObject): Map<String, Int> {
      return getPropertyValue<JsonObject>(source.findProperty(MEMBERS))
               ?.toPropertyValue<JsonArray>(CONSTRUCTOR)
               ?.valueList
               ?.filterIsInstance<JsonObject>()
               ?.firstNotNullOfOrNull { it.toPropertyValue<JsonArray>(PARAMETER_DECORATORS) }
               ?.let { buildAttributesMapping(it) }
             ?: emptyMap()
    }

    private fun buildAttributesMapping(paramDecorators: JsonArray): Map<String, Int> {
      // Checks if the input object represents the @Attribute decorator
      val isAttributeDecorator = { `object`: JsonObject ->
        val expr = getPropertyValue<JsonObject>(`object`.findProperty(EXPRESSION))
        val decoratorName = if (expr != null)
          readStringPropertyValue(expr.findProperty(REFERENCE_NAME))
        else
          null
        ATTRIBUTE_DEC == decoratorName
      }

      // TODO convert this stream to a Kotlin sequence :O
      @Suppress("SSBasedInspection")
      return EntryStream.of(paramDecorators.valueList)
        .selectValues(JsonArray::class.java)
        .flatMapValues { a -> a.valueList.stream() }
        .selectValues(JsonObject::class.java)
        .filterValues(isAttributeDecorator)
        .mapValues { it.toPropertyValue<JsonArray>(ARGUMENTS) }
        .mapValues { o -> o?.valueList?.firstOrNull() }
        .selectValues(JsonStringLiteral::class.java)
        .mapValues { it.value }
        .filterValues { s -> !s.trim { it <= ' ' }.isEmpty() }
        .collect(toMap<Map.Entry<Int, String>, String, Int>(
          { (_, value) -> value }, { (key) -> key }, { i, _ -> i }))
    }

    private inline fun <reified T : JsonValue> JsonObject.toPropertyValue(property: String): T? {
      return getPropertyValue<T>(findProperty(property))
    }
  }
}
