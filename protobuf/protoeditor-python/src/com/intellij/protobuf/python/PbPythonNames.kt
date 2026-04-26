package com.intellij.protobuf.python

import com.intellij.psi.util.QualifiedName
import com.jetbrains.python.psi.PyClass
import com.jetbrains.python.psi.PyElement
import com.jetbrains.python.psi.PyPsiFacade

internal object PbPythonNames {
  private fun qn(dotted: String): QualifiedName = QualifiedName.fromDottedString(dotted)

  val PROTOBUF_NAMESPACE = qn("google.protobuf")

  private fun inPb(dotted: String): QualifiedName = PROTOBUF_NAMESPACE.append(qn(dotted))

  val MESSAGE_MAP = inPb("internal.containers.MessageMap")
  val SCALAR_MAP = inPb("internal.containers.ScalarMap")

  val REPEATED_MESSAGE = inPb("internal.containers.RepeatedCompositeFieldContainer")
  val REPEATED_SCALAR = inPb("internal.containers.RepeatedScalarFieldContainer")

  val MESSAGE_CLASS = inPb("message.Message")

  val FIELD_DESCRIPTOR = inPb("descriptor.FieldDescriptor")

  val ENUM_METACLASS = inPb("internal.enum_type_wrapper.EnumTypeWrapper")

  // Protobuf well-known types
  val WKT_TIMESTAMP = inPb("Timestamp")
  val WKT_DURATION = inPb("Duration")
  val WKT_DOUBLE_VALUE = inPb("DoubleValue")
  val WKT_FLOAT_VALUE = inPb("FloatValue")
  val WKT_STRING_VALUE = inPb("StringValue")
  val WKT_BYTES_VALUE = inPb("BytesValue")
  val WKT_UINT64_VALUE = inPb("UInt64Value")
  val WKT_INT64_VALUE = inPb("Int64Value")
  val WKT_UINT32_VALUE = inPb("UInt32Value")
  val WKT_INT32_VALUE = inPb("Int32Value")
  val WKT_BOOL_VALUE = inPb("BoolValue")

  // STD types
  val ITERABLE = qn("collections.abc.Iterable")
  val MAPPING = qn("collections.abc.Mapping")

  val TIMEDELTA = qn("datetime.timedelta")
  val DATETIME = qn("datetime.datetime")
}

internal val QualifiedName.belongsToProtobuf: Boolean
  get() = this.matchesPrefix(PbPythonNames.PROTOBUF_NAMESPACE)

internal fun QualifiedName.toPyClass(anchor: PyElement?): PyClass? = anchor?.let {
  PyPsiFacade.getInstance(anchor.project).createClassByQName(this, anchor)
}
