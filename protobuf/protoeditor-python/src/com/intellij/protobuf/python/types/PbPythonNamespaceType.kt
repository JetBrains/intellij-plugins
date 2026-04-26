package com.intellij.protobuf.python.types

import com.intellij.protobuf.lang.psi.PbFile
import com.intellij.protobuf.lang.psi.PbSymbol
import com.intellij.protobuf.python.PbPythonProtoUtils.getLocalSymbolMap
import com.intellij.protobuf.python.PbPythonSourceContext
import com.intellij.psi.util.QualifiedName
import com.jetbrains.python.psi.PyElement

internal class PbPythonNamespaceType(
  source: PbPythonSourceContext,
  pyAnchor: PyElement?,
) : PbPythonAbstractType<PbFile>(source.pbFile, source, QualifiedName.fromComponents(), true, pyAnchor) {

  override val typeName: String = "Protobuf Namespace"

  override val pbElement: PbFile?
    get() = getPbElement { it as? PbFile }

  override fun asDefinition(isDefinitionFlag: Boolean): PbPythonNamespaceType = this

  override fun isCallable(): Boolean = false

  override fun getChildren(): Collection<PbSymbol> =
    getLocalSymbolMap(source.pbFile)
      .asSequence()
      .filter { (candidateLocalQn, _) -> candidateLocalQn.componentCount == 1 }
      .flatMap { (_, candidates) -> candidates.asSequence() }
      .toList()
}
