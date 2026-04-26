package com.intellij.protobuf.python.types

import com.intellij.protobuf.lang.psi.PbField
import com.intellij.protobuf.lang.psi.PbMapField
import com.intellij.protobuf.lang.psi.PbMessageType
import com.intellij.protobuf.lang.psi.PbSimpleField
import com.intellij.protobuf.lang.psi.PbSymbol
import com.intellij.protobuf.lang.psi.util.PbPsiUtil
import com.intellij.protobuf.python.PbPythonNames
import com.intellij.protobuf.python.PbPythonSourceContext
import com.intellij.protobuf.python.toPyClass
import com.intellij.protobuf.python.types.PbPythonTypeUtils.convertMapFieldToPyType
import com.intellij.protobuf.python.types.PbPythonTypeUtils.convertProtoTypeToPyType
import com.intellij.protobuf.python.types.PbPythonTypeUtils.createCollectionType
import com.intellij.psi.util.QualifiedName
import com.jetbrains.python.psi.LanguageLevel
import com.jetbrains.python.psi.PyClass
import com.jetbrains.python.psi.PyElement
import com.jetbrains.python.psi.PyElementGenerator
import com.jetbrains.python.psi.impl.PyBuiltinCache
import com.jetbrains.python.psi.types.PyCallableParameter
import com.jetbrains.python.psi.types.PyCallableParameterImpl
import com.jetbrains.python.psi.types.PyClassLikeType
import com.jetbrains.python.psi.types.PyClassTypeImpl
import com.jetbrains.python.psi.types.PyType
import com.jetbrains.python.psi.types.PyUnionType
import com.jetbrains.python.psi.types.TypeEvalContext

internal class PbPythonMessageType(
  pbMessage: PbMessageType?,
  source: PbPythonSourceContext,
  localQn: QualifiedName,
  isDefinitionFlag: Boolean,
  pyAnchor: PyElement?,
  val wellKnownTypeSuperclass: PyClass?,
) : PbPythonAbstractType<PbMessageType>(pbMessage, source, localQn, isDefinitionFlag, pyAnchor) {

  override val typeName: String = "Protobuf Message"

  override val pbElement: PbMessageType?
    get() = getPbElement { it as? PbMessageType }

  override fun asDefinition(isDefinitionFlag: Boolean): PbPythonMessageType =
    PbPythonMessageType(pbElement, source, localQn, isDefinitionFlag, getPyAnchor(), wellKnownTypeSuperclass)

  override fun getSuperClassTypes(context: TypeEvalContext): List<PyClassLikeType> {
    val superclass = wellKnownTypeSuperclass
                     ?: PbPythonNames.MESSAGE_CLASS.toPyClass(getPyAnchor(context))
                     ?: return emptyList()
    return listOf(PyClassTypeImpl(superclass, isDefinition))
  }

  override fun isAttributeWritable(name: String, context: TypeEvalContext): Boolean {
    val pbMessage = pbElement ?: return true  // To not spam warnings if broken

    val isFieldWritable = pbMessage.symbols.asSequence()
      .filterIsInstance<PbField>()
      .filter { it.name == name }
      .any { field ->
        // Direct assignment is forbidden for repeated containers, map containers, and message-typed fields
        !field.isRepeated &&
        field !is PbMapField &&
        field.typeName?.symbolPath?.reference?.resolve() !is PbMessageType
      }

    return isFieldWritable
  }

  override fun getChildren(): Collection<PbSymbol> =
    pbElement?.symbols?.filterNot { PbPsiUtil.isGeneratedMapEntry(it) } ?: emptyList()

  override fun getParameters(context: TypeEvalContext): List<PyCallableParameter>? {
    if (!isDefinition) return null
    val pbMessage = pbElement ?: return null

    val anchor = getPyAnchor(context) ?: return null
    val builtins = PyBuiltinCache.getInstance(anchor)
    val noneExpr = PyElementGenerator.getInstance(anchor.project)
      .createExpressionFromText(LanguageLevel.forElement(anchor), "None")

    return pbMessage.symbols
      .asSequence()
      .filterIsInstance<PbField>()
      .mapNotNull { field ->
        val fieldName = field.name ?: return@mapNotNull null

        val paramType = when (field) {
          is PbSimpleField -> getSimpleFieldParameterType(field, anchor, builtins)
          is PbMapField -> getMapFieldParameterType(field, anchor, builtins)
          else -> null
        }

        // Optional[paramType]
        PyUnionType.union(paramType, builtins.noneType)?.let { optionalType ->
          PyCallableParameterImpl.nonPsi(fieldName, optionalType, noneExpr, field)
        }
      }
      .toList()
  }

  private fun getSimpleFieldParameterType(
    field: PbSimpleField,
    anchor: PyElement,
    builtins: PyBuiltinCache,
  ): PyType? {
    val type = convertProtoTypeToPyType(field.typeName, source, anchor) ?: return null
    val typesToUnion = buildList {
      when (type) {
        is PbPythonMessageType -> {  // Union[WKT/type, {alternatives}, Mapping]
          if (type.wellKnownTypeSuperclass != null) {
            add(PyClassTypeImpl(type.wellKnownTypeSuperclass, false))
          }
          else {
            add(type)
          }

          add(type.getAlternativeType(anchor, builtins))

          PbPythonNames.MAPPING.toPyClass(anchor)?.let {
            add(PyClassTypeImpl(it, false))
          }
        }
        is PbPythonEnumType -> {  // Union[int, str]
          add(builtins.intType)
          add(builtins.strType)
        }
        else -> {   // just `type`
          add(type)
        }
      }
    }
    val possibleTypes = PyUnionType.union(typesToUnion.filterNotNull())

    return if (field.isRepeated) {  // Iterable of possible types
      val pythonIterable = PbPythonNames.ITERABLE.toPyClass(anchor)
      createCollectionType(pythonIterable, listOf(possibleTypes), builtins.listType)
    }
    else {
      possibleTypes
    }
  }

  private fun getMapFieldParameterType(
    field: PbMapField,
    anchor: PyElement,
    builtins: PyBuiltinCache,
  ): PyType? {
    val (keyType, valueType) = convertMapFieldToPyType(field, source, anchor)

    val pythonMapping = PbPythonNames.MAPPING.toPyClass(anchor)
    val possibleValueTypes = if (valueType is PbPythonMessageType && pythonMapping != null) {
      PyUnionType.union(valueType, PyClassTypeImpl(pythonMapping, false))
    }
    else {
      valueType
    }

    return createCollectionType(pythonMapping, listOf(keyType, possibleValueTypes), builtins.dictType)
  }

  fun getAlternativeType(anchor: PyElement, builtins: PyBuiltinCache): PyType? =
    when (pbElement?.qualifiedName) {
      PbPythonNames.WKT_TIMESTAMP ->
        PbPythonNames.DATETIME.toPyClass(anchor)?.let { PyClassTypeImpl(it, false) }
      PbPythonNames.WKT_DURATION ->
        PbPythonNames.TIMEDELTA.toPyClass(anchor)?.let { PyClassTypeImpl(it, false) }
      PbPythonNames.WKT_DOUBLE_VALUE, PbPythonNames.WKT_FLOAT_VALUE ->
        builtins.floatType
      PbPythonNames.WKT_INT32_VALUE, PbPythonNames.WKT_UINT32_VALUE,
      PbPythonNames.WKT_INT64_VALUE, PbPythonNames.WKT_UINT64_VALUE,
        -> builtins.intType
      PbPythonNames.WKT_BOOL_VALUE ->
        builtins.boolType
      PbPythonNames.WKT_STRING_VALUE ->
        builtins.strType
      PbPythonNames.WKT_BYTES_VALUE ->
        builtins.getBytesType(LanguageLevel.forElement(anchor))
      else -> null
    }
}
