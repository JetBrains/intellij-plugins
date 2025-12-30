// Copyright 2000-2022 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.angular2.entities.metadata.stubs

import com.intellij.json.psi.*
import com.intellij.lang.javascript.index.flags.BooleanStructureElement
import com.intellij.lang.javascript.index.flags.FlagsStructure
import com.intellij.openapi.util.NotNullLazyValue
import com.intellij.psi.PsiElement
import com.intellij.psi.stubs.IndexSink
import com.intellij.psi.stubs.StubElement
import com.intellij.psi.stubs.StubInputStream
import com.intellij.psi.stubs.StubOutputStream
import org.angular2.Angular2DecoratorUtil.COMPONENT_DEC
import org.angular2.Angular2DecoratorUtil.DIRECTIVE_DEC
import org.angular2.Angular2DecoratorUtil.INPUT_DEC
import org.angular2.Angular2DecoratorUtil.MODULE_DEC
import org.angular2.Angular2DecoratorUtil.OUTPUT_DEC
import org.angular2.Angular2DecoratorUtil.PIPE_DEC
import org.angular2.entities.Angular2DirectiveKind
import org.angular2.entities.Angular2EntityUtils
import org.angular2.index.Angular2MetadataClassNameIndexKey
import org.angular2.lang.metadata.MetadataUtils
import org.angular2.lang.metadata.MetadataUtils.getPropertyValue
import org.angular2.lang.metadata.MetadataUtils.readStringPropertyValue
import org.angular2.lang.metadata.psi.MetadataElementType
import org.jetbrains.annotations.NonNls
import java.io.IOException
import java.util.*

open class Angular2MetadataClassStubBase<Psi : PsiElement> : Angular2MetadataElementStub<Psi> {

  protected val myInputMappings: MutableMap<String, String>
  protected val myOutputMappings: MutableMap<String, String>
  protected open val loadInOuts: Boolean get() = true

  val className: String?
    get() = memberName

  val extendsReference: Angular2MetadataReferenceStub?
    get() = childrenStubs
      .find { it is Angular2MetadataReferenceStub && EXTENDS_MEMBER == it.memberName } as? Angular2MetadataReferenceStub

  val inputMappings: Map<String, String>
    get() = Collections.unmodifiableMap(myInputMappings)

  val outputMappings: Map<String, String>
    get() = Collections.unmodifiableMap(myOutputMappings)

  open val directiveKind: Angular2DirectiveKind?
    get() {
      val isStructural = readFlag(IS_STRUCTURAL_DIRECTIVE_FLAG)
      val isRegular = readFlag(IS_REGULAR_DIRECTIVE_FLAG)
      return if (isStructural || isRegular) {
        Angular2DirectiveKind.get(isRegular, isStructural)
      }
      else null
    }

  constructor(memberName: String?,
              parent: StubElement<*>?,
              source: JsonObject,
              elementType: MetadataElementType<*>) : super(memberName, parent, elementType) {
    @Suppress("LeakingThis")
    if (loadInOuts) {
      readTemplateFlag(source)
    }
    val extendsClass = getPropertyValue<JsonObject>(source.findProperty(EXTENDS))
    if (extendsClass != null) {
      @Suppress("LeakingThis")
      Angular2MetadataReferenceStub.createReferenceStub(EXTENDS_MEMBER, extendsClass, this)
    }
    myOutputMappings = HashMap<String, String>()
    myInputMappings = HashMap<String, String>()
    MetadataUtils.listObjectProperties(source.findProperty(MEMBERS))
      .forEach { this.loadMember(it, myInputMappings, myOutputMappings) }
    MetadataUtils.listObjectProperties(source.findProperty(STATICS))
      .filter { prop ->
        prop.value is JsonObject
        && SYMBOL_FUNCTION == readStringPropertyValue((prop.value as JsonObject).findProperty(SYMBOL_TYPE))
      }
      .forEach { this.loadMemberProperty(it) }
  }

  @Throws(IOException::class)
  constructor(stream: StubInputStream,
              parent: StubElement<*>?, elementType: MetadataElementType<*>) : super(stream, parent, elementType) {
    myInputMappings = if (readFlag(HAS_INPUT_MAPPINGS)) readStringMap(stream).toMutableMap() else mutableMapOf()
    myOutputMappings = if (readFlag(HAS_OUTPUT_MAPPINGS)) readStringMap(stream).toMutableMap() else mutableMapOf()
  }

  @Throws(IOException::class)
  override fun serialize(stream: StubOutputStream) {
    writeFlag(HAS_INPUT_MAPPINGS, !myInputMappings.isEmpty())
    writeFlag(HAS_OUTPUT_MAPPINGS, !myOutputMappings.isEmpty())
    super.serialize(stream)
    if (!myInputMappings.isEmpty()) {
      writeStringMap(myInputMappings, stream)
    }
    if (!myOutputMappings.isEmpty()) {
      writeStringMap(myOutputMappings, stream)
    }
  }

  override fun index(sink: IndexSink) {
    super.index(sink)
    if (className != null) {
      sink.occurrence(Angular2MetadataClassNameIndexKey, className!!)
    }
  }

  override val flagsStructure: FlagsStructure
    get() = FLAGS_STRUCTURE

  private fun readTemplateFlag(source: JsonObject) {
    val members = source.findProperty(MEMBERS)?.value as? JsonObject
    val constructor = members?.findProperty(CONSTRUCTOR)
    val constructorText = if (constructor != null) constructor.text else ""
    val kind = Angular2DirectiveKind.get(constructorText.contains(Angular2EntityUtils.ELEMENT_REF),
                                         constructorText.contains(Angular2EntityUtils.TEMPLATE_REF),
                                         constructorText.contains(Angular2EntityUtils.VIEW_CONTAINER_REF),
                                         false, false)
    writeFlag(IS_STRUCTURAL_DIRECTIVE_FLAG, kind != null && kind.isStructural)
    writeFlag(IS_REGULAR_DIRECTIVE_FLAG, kind != null && kind.isRegular)
  }

  private fun loadMember(property: JsonProperty, inputMappings: MutableMap<String, String>, outputMappings: MutableMap<String, String>) {
    val name = property.name
    val `val` = property.value as? JsonArray
    if (`val` == null || `val`.valueList.size != 1) {
      return
    }
    val obj = `val`.valueList[0] as? JsonObject ?: return
    val memberSymbol = readStringPropertyValue(obj.findProperty(SYMBOL_TYPE))
    if (loadInOuts && (SYMBOL_PROPERTY == memberSymbol || SYMBOL_METHOD == memberSymbol)) {
      decoratorsSequence(obj).forEach { dec ->
        if (INPUT_DEC == dec.first) {
          addBindingMapping(name, inputMappings, getDecoratorInitializer(dec.second))
        }
        else if (OUTPUT_DEC == dec.first) {
          addBindingMapping(name, outputMappings, getDecoratorInitializer(dec.second))
        }
      }
    }
    else if (SYMBOL_FUNCTION == memberSymbol) {
      loadMemberProperty(property)
    }
  }

  private fun interface EntityFactory {
    fun create(memberName: String?,
               parent: StubElement<*>?,
               classSource: JsonObject,
               decoratorSource: JsonObject): Angular2MetadataClassStubBase<*>?
  }

  companion object {
    @NonNls
    private val EXTENDS_MEMBER = "#ext"

    private val ENTITY_FACTORIES = NotNullLazyValue.lazy {
      mapOf(
        MODULE_DEC to EntityFactory { memberName, parent, classSource, decoratorSource ->
          Angular2MetadataModuleStub(memberName, parent, classSource, decoratorSource)
        },
        PIPE_DEC to EntityFactory { memberName, parent, classSource, decoratorSource ->
          Angular2MetadataPipeStub.createPipeStub(memberName, parent, classSource, decoratorSource)
        },
        COMPONENT_DEC to EntityFactory { memberName, parent, source, decoratorSource ->
          Angular2MetadataComponentStub(memberName, parent, source, decoratorSource)
        },
        DIRECTIVE_DEC to EntityFactory { memberName, parent, source, decoratorSource ->
          Angular2MetadataDirectiveStub(memberName, parent, source, decoratorSource)
        }
      )
    }

    private val entityFactories: Map<String, EntityFactory>
      get() = ENTITY_FACTORIES.value

    fun createClassStub(memberName: String?,
                        source: JsonValue,
                        parent: StubElement<*>?): Angular2MetadataClassStubBase<*> {
      return decoratorsSequence(source as JsonObject)
               .mapNotNull { entityFactories[it.first]?.create(memberName, parent, source, it.second) }
               .firstOrNull()
             ?: Angular2MetadataClassStub(memberName, source, parent)
    }

    private val IS_STRUCTURAL_DIRECTIVE_FLAG = BooleanStructureElement()
    private val IS_REGULAR_DIRECTIVE_FLAG = BooleanStructureElement()
    private val HAS_INPUT_MAPPINGS = BooleanStructureElement()
    private val HAS_OUTPUT_MAPPINGS = BooleanStructureElement()

    @JvmStatic
    protected val FLAGS_STRUCTURE: FlagsStructure = FlagsStructure(
      Angular2MetadataElementStub.FLAGS_STRUCTURE,
      IS_STRUCTURAL_DIRECTIVE_FLAG,
      IS_REGULAR_DIRECTIVE_FLAG,
      HAS_INPUT_MAPPINGS,
      HAS_OUTPUT_MAPPINGS
    )

    private fun addBindingMapping(fieldName: String,
                                  mappings: MutableMap<String, String>,
                                  initializer: JsonStringLiteral?) {
      val bindingName = initializer?.value ?: fieldName
      mappings[fieldName] = bindingName
    }
  }
}
