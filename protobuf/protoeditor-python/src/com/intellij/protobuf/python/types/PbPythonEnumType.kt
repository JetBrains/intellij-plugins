package com.intellij.protobuf.python.types

import com.intellij.protobuf.lang.psi.PbEnumDefinition
import com.intellij.protobuf.lang.psi.PbSymbol
import com.intellij.protobuf.python.PbPythonNames
import com.intellij.protobuf.python.PbPythonSourceContext
import com.intellij.protobuf.python.toPyClass
import com.intellij.psi.util.QualifiedName
import com.jetbrains.python.psi.PyElement
import com.jetbrains.python.psi.types.PyClassLikeType
import com.jetbrains.python.psi.types.PyClassTypeImpl
import com.jetbrains.python.psi.types.TypeEvalContext

internal class PbPythonEnumType(
  pbEnum: PbEnumDefinition?,
  source: PbPythonSourceContext,
  localQn: QualifiedName,
  pyAnchor: PyElement?,
) : PbPythonAbstractType<PbEnumDefinition>(pbEnum, source, localQn, true, pyAnchor) {

  override val typeName: String = "Protobuf Enum"

  override val pbElement: PbEnumDefinition?
    get() = getPbElement { it as? PbEnumDefinition }

  override fun asDefinition(isDefinitionFlag: Boolean): PbPythonEnumType = this

  override fun isCallable(): Boolean = false

  override fun getMetaClassType(context: TypeEvalContext, inherited: Boolean): PyClassLikeType? =
    PbPythonNames.ENUM_METACLASS.toPyClass(getPyAnchor(context))?.let { PyClassTypeImpl(it, true) }

  override fun getChildren(): Collection<PbSymbol> = pbElement?.enumValues ?: emptyList()
}
