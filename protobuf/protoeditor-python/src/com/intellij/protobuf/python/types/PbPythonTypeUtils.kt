package com.intellij.protobuf.python.types

import com.intellij.protobuf.lang.psi.PbEnumDefinition
import com.intellij.protobuf.lang.psi.PbFile
import com.intellij.protobuf.lang.psi.PbMapField
import com.intellij.protobuf.lang.psi.PbMessageType
import com.intellij.protobuf.lang.psi.PbSymbol
import com.intellij.protobuf.lang.psi.PbTypeName
import com.intellij.protobuf.python.PbPythonNames
import com.intellij.protobuf.python.PbPythonSourceContext
import com.intellij.protobuf.python.belongsToProtobuf
import com.intellij.protobuf.python.toPyClass
import com.intellij.psi.util.QualifiedName
import com.jetbrains.python.psi.LanguageLevel
import com.jetbrains.python.psi.PyClass
import com.jetbrains.python.psi.PyElement
import com.jetbrains.python.psi.impl.PyBuiltinCache
import com.jetbrains.python.psi.types.PyClassLikeType
import com.jetbrains.python.psi.types.PyClassType
import com.jetbrains.python.psi.types.PyCollectionTypeImpl
import com.jetbrains.python.psi.types.PyType
import com.jetbrains.python.psi.types.PyUnionType

internal object PbPythonTypeUtils {

  fun getMapContainerClassQName(type: PyType?): QualifiedName =
    when (type) {
      is PbPythonMessageType -> PbPythonNames.MESSAGE_MAP
      else -> PbPythonNames.SCALAR_MAP
    }

  fun getRepeatedContainerClassQName(type: PyType?): QualifiedName {
    when (type) {
      is PbPythonMessageType -> return PbPythonNames.REPEATED_MESSAGE
      is PyClassLikeType -> type.classQName?.let { qn ->
        if (QualifiedName.fromDottedString(qn).belongsToProtobuf) {
          return PbPythonNames.REPEATED_MESSAGE
        }
      }
    }
    return PbPythonNames.REPEATED_SCALAR
  }

  fun getScalarTypeByProtoName(name: PbTypeName, anchor: PyElement): PyType? {
    val builtins = PyBuiltinCache.getInstance(anchor)
    return when (name.shortName.lowercase()) {
      "int32", "int64", "sint32", "sint64", "sfixed32", "sfixed64",
      "uint32", "uint64", "fixed32", "fixed64",
        -> builtins.intType
      "float", "double" -> PyUnionType.union(builtins.floatType, builtins.intType)
      "string" -> builtins.strType
      "bool" -> builtins.boolType
      "bytes" -> builtins.getBytesType(LanguageLevel.forElement(anchor))
      else -> null
    }
  }

  fun convertProtoTypeToPyType(typeName: PbTypeName, source: PbPythonSourceContext, anchor: PyElement): PyType? {
    getScalarTypeByProtoName(typeName, anchor)?.let { return it }

    val resolved = typeName.symbolPath.reference?.resolve() as? PbSymbol ?: return null

    val wellKnownTypeSuperclass = findWellKnownTypeSuperclass(resolved, source, anchor)

    val file = resolved.containingFile as? PbFile ?: return null
    val qn = resolved.qualifiedName ?: return null
    val localName = qn.removeHead(file.packageQualifiedName.componentCount)

    // Api version should not change across the resolution context
    val resolvedSource = PbPythonSourceContext(file, source.apiVersion)

    return when (resolved) {
      is PbMessageType -> PbPythonMessageType(resolved, resolvedSource, localName, false, anchor, wellKnownTypeSuperclass)
      is PbEnumDefinition -> PbPythonEnumType(resolved, resolvedSource, localName, anchor)
      else -> null
    }
  }

  fun convertMapFieldToPyType(field: PbMapField, source: PbPythonSourceContext, anchor: PyElement): Pair<PyType?, PyType?> {
    val builtins = PyBuiltinCache.getInstance(anchor)

    val keyType = field.keyType?.let { convertProtoTypeToPyType(it, source, anchor) } ?: builtins.objectType
    val valueType = field.valueType?.let { convertProtoTypeToPyType(it, source, anchor) } ?: builtins.objectType

    return keyType to valueType
  }

  // TODO: Check if needed
  fun convertTypeToAssignable(type: PyType?, anchor: PyElement): PyType? {
    val builtins = PyBuiltinCache.getInstance(anchor)
    return when (type) {
      is PbPythonEnumType -> builtins.intType
      // For PbPythonMessageType might be a wellKnownTypeSuperclass
      else -> type
    }
  }

  fun findWellKnownTypeSuperclass(pbSymbol: PbSymbol, source: PbPythonSourceContext, anchor: PyElement): PyClass? {
    val resolvedQName = pbSymbol.qualifiedName ?: return null
    if (!resolvedQName.belongsToProtobuf) return null

    val containingFile = pbSymbol.containingFile as? PbFile ?: return null
    val fileName = containingFile.name.removeSuffix(".proto")
    val className = pbSymbol.name ?: return null

    // Api version should not change across the resolution context
    val qn = containingFile.packageQualifiedName
      .append("${fileName}${source.apiVersion.suffix}")
      .append(className)
    return qn.toPyClass(anchor)
  }

  fun createCollectionType(collectionClass: PyClass?, elements: List<PyType?>, fallbackCollectionType: PyClassType?): PyType? {
    val effectiveClass = collectionClass ?: fallbackCollectionType?.pyClass
    if (effectiveClass != null) {
      return PyCollectionTypeImpl(effectiveClass, false, elements)
    }
    return fallbackCollectionType?.toInstance()
  }
}
