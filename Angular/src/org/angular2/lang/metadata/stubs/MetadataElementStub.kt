// Copyright 2000-2022 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.angular2.lang.metadata.stubs

import com.intellij.json.psi.*
import com.intellij.lang.javascript.index.flags.BooleanStructureElement
import com.intellij.lang.javascript.index.flags.FlagsStructure
import com.intellij.lang.javascript.index.flags.FlagsStructureElement
import com.intellij.lang.javascript.index.flags.IntFlagsSerializer
import com.intellij.openapi.util.NotNullLazyValue
import com.intellij.openapi.util.Pair
import com.intellij.openapi.util.ThrowableComputable
import com.intellij.openapi.util.io.DataInputOutputUtilRt
import com.intellij.psi.PsiElement
import com.intellij.psi.stubs.*
import com.intellij.util.ThrowableConsumer
import com.intellij.util.io.DataInputOutputUtil
import com.intellij.util.io.StringRef
import org.angular2.lang.metadata.MetadataUtils.readStringPropertyValue
import org.angular2.lang.metadata.psi.MetadataElementType
import org.jetbrains.annotations.NonNls
import java.io.IOException

abstract class MetadataElementStub<Psi : PsiElement> : StubBase<Psi> {

  private val myMemberName: StringRef?
  private var myFlags: Int = 0
  private val membersMap = NotNullLazyValue.lazy {
    childrenStubs
      .asSequence()
      .filterIsInstance<MetadataElementStub<*>>()
      .filter { it.memberName != null }
      .associateBy { it.memberName }
  }

  val memberName: String?
    get() = StringRef.toString(myMemberName)

  protected abstract val typeFactory: Map<String, ConstructorFromJsonValue>

  protected open val flagsStructure: FlagsStructure
    get() = FLAGS_STRUCTURE

  constructor(memberName: String?, parent: StubElement<*>?, elementType: MetadataElementType<*>) : super(parent, elementType) {
    myMemberName = StringRef.fromString(memberName)
  }

  @Throws(IOException::class)
  constructor(stream: StubInputStream, parent: StubElement<*>?, elementType: MetadataElementType<*>) : super(parent, elementType) {
    @Suppress("LeakingThis")
    val flagsSize = flagsStructure.size()
    if (flagsSize > 0) {
      assert(flagsSize <= Integer.SIZE) { this.javaClass }
      myFlags = DataInputOutputUtil.readINT(stream)
    }
    myMemberName = if (readFlag(HAS_MEMBER_NAME)) stream.readName() else null
  }

  @Throws(IOException::class)
  open fun serialize(stream: StubOutputStream) {
    writeFlag(HAS_MEMBER_NAME, myMemberName != null)
    if (flagsStructure.size() > 0) {
      DataInputOutputUtil.writeINT(stream, myFlags)
    }
    if (myMemberName != null) {
      writeString(myMemberName, stream)
    }
  }

  open fun index(sink: IndexSink) {}

  protected fun <T> readFlag(structureElement: FlagsStructureElement<T>): T {
    return IntFlagsSerializer.INSTANCE.readValue(flagsStructure, structureElement, myFlags)
  }

  protected fun <T> writeFlag(structureElement: FlagsStructureElement<T>, value: T) {
    myFlags = IntFlagsSerializer.INSTANCE.writeValue(flagsStructure, structureElement, value, myFlags)
  }

  protected fun loadMemberProperty(p: JsonProperty) {
    createMember(p.name, p.value)
  }

  protected fun createMember(name: String?, member: JsonValue?) {
    var constructor: ConstructorFromJsonValue? = null
    when (member) {
      is JsonArray -> {
        constructor = typeFactory[ARRAY_TYPE]
      }
      is JsonObject -> {
        val type = readStringPropertyValue(member.findProperty(SYMBOL_TYPE))
        constructor = typeFactory[type ?: OBJECT_TYPE]
      }
      is JsonStringLiteral -> {
        constructor = typeFactory[STRING_TYPE]
      }
    }
    constructor?.construct(name, member!!, this)
  }

  fun findMember(name: String): MetadataElementStub<*>? {
    return membersMap.value[name]
  }

  protected fun interface ConstructorFromJsonValue {
    fun construct(memberName: String?,
                  source: JsonValue,
                  parent: StubElement<*>?): MetadataElementStub<*>?
  }

  companion object {
    @NonNls
    @JvmStatic
    protected val SYMBOL_TYPE = "__symbolic"

    @NonNls
    @JvmStatic
    protected val SYMBOL_REFERENCE = "reference"

    @NonNls
    @JvmStatic
    protected val SYMBOL_PROPERTY = "property"

    @NonNls
    @JvmStatic
    protected val SYMBOL_FUNCTION = "function"

    @NonNls
    @JvmStatic
    protected val SYMBOL_METHOD = "method"

    @NonNls
    @JvmStatic
    protected val SYMBOL_CALL = "call"

    @NonNls
    @JvmStatic
    protected val SYMBOL_CLASS = "class"

    @NonNls
    @JvmStatic
    protected val SYMBOL_SPREAD = "spread"

    @NonNls
    @JvmStatic
    protected val PARAMETER_DECORATORS = "parameterDecorators"

    @NonNls
    @JvmStatic
    protected val DECORATORS = "decorators"

    @NonNls
    @JvmStatic
    protected val EXPRESSION = "expression"

    @NonNls
    @JvmStatic
    protected val ARGUMENTS = "arguments"

    @NonNls
    @JvmStatic
    protected val MEMBERS = "members"

    @NonNls
    @JvmStatic
    protected val STATICS = "statics"

    @NonNls
    @JvmStatic
    protected val EXTENDS = "extends"

    @NonNls
    @JvmStatic
    protected val CONSTRUCTOR = "__ctor__"

    @NonNls
    @JvmStatic
    protected val REFERENCE_NAME = "name"

    @NonNls
    @JvmStatic
    protected val REFERENCE_MODULE = "module"

    @NonNls
    @JvmStatic
    protected val FUNCTION_VALUE = "value"

    @NonNls
    @JvmStatic
    protected val STRING_TYPE = "#string"

    @NonNls
    @JvmStatic
    protected val ARRAY_TYPE = "#array"

    @NonNls
    @JvmStatic
    protected val OBJECT_TYPE = "#object"

    private val HAS_MEMBER_NAME = BooleanStructureElement()

    @JvmStatic
    protected val FLAGS_STRUCTURE = FlagsStructure(
      HAS_MEMBER_NAME
    )

    @Throws(IOException::class)
    @JvmStatic
    protected fun writeString(ref: StringRef?, dataStream: StubOutputStream) {
      dataStream.writeName(StringRef.toString(ref))
    }

    @Throws(IOException::class)
    @JvmStatic
    protected fun writeStringMap(map: Map<String, String>, stream: StubOutputStream) {
      DataInputOutputUtilRt.writeMap(stream, map, ThrowableConsumer { stream.writeName(it) },
                                     ThrowableConsumer { stream.writeName(it) })
    }

    @Throws(IOException::class)
    @JvmStatic
    protected fun writeIntegerMap(map: Map<String, Int>, stream: StubOutputStream) {
      DataInputOutputUtilRt.writeMap(stream, map, ThrowableConsumer { stream.writeName(it) },
                                     ThrowableConsumer { stream.writeVarInt(it) })
    }

    @Throws(IOException::class)
    @JvmStatic
    protected fun readStringMap(stream: StubInputStream): Map<String, String> {
      return DataInputOutputUtilRt.readMap(stream, ThrowableComputable<String, IOException> { stream.readNameString() },
                                           ThrowableComputable<String, IOException> { stream.readNameString() })
    }

    @Throws(IOException::class)
    @JvmStatic
    protected fun writeStringList(list: List<String>, stream: StubOutputStream) {
      DataInputOutputUtilRt.writeSeq(stream, list, ThrowableConsumer { stream.writeName(it) })
    }

    @Throws(IOException::class)
    @JvmStatic
    protected fun readStringList(stream: StubInputStream): List<String> {
      return DataInputOutputUtilRt.readSeq(stream, ThrowableComputable<String, IOException> { stream.readNameString() })
    }

    @Throws(IOException::class)
    @JvmStatic
    protected fun readIntegerMap(stream: StubInputStream): Map<String, Int> {
      return DataInputOutputUtilRt.readMap(stream, ThrowableComputable<String, IOException> { stream.readNameString() },
                                           ThrowableComputable { stream.readVarInt() })
    }

    @JvmStatic
    protected fun decoratorsSequence(sourceClass: JsonObject): Sequence<Pair<String, JsonObject>> {
      val list = sourceClass.findProperty(DECORATORS)?.value as? JsonArray
                 ?: return emptySequence()
      return list.valueList.asSequence()
        .filterIsInstance<JsonObject>()
        .filter { SYMBOL_CALL == readStringPropertyValue(it.findProperty(SYMBOL_TYPE)) }
        .mapNotNull { it.findProperty(EXPRESSION)?.value as? JsonObject }
        .filter { SYMBOL_REFERENCE == readStringPropertyValue(it.findProperty(SYMBOL_TYPE)) }
        .mapNotNull { obj ->
          readStringPropertyValue(obj.findProperty(REFERENCE_NAME))?.let {
            Pair.create(it, obj.parent.parent as JsonObject)
          }
        }
    }

    @JvmStatic
    protected inline fun <reified T : JsonValue> getDecoratorInitializer(decorator: JsonObject): T? {
      val args = decorator.findProperty(ARGUMENTS)?.value as? JsonArray
      return if (args != null && args.valueList.size == 1) args.valueList[0] as? T else null
    }
  }
}
