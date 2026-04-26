package com.intellij.protobuf.python.types

import com.intellij.protobuf.python.PbPythonProtoUtils.resolveInProto
import com.intellij.protobuf.python.PbPythonSourceContext
import com.intellij.psi.PsiElement
import com.intellij.psi.util.QualifiedName
import com.jetbrains.python.codeInsight.PyCustomMember
import com.jetbrains.python.psi.PyFile
import com.jetbrains.python.psi.resolve.PyResolveContext
import com.jetbrains.python.psi.types.PyModuleMembersProvider
import com.jetbrains.python.psi.types.TypeEvalContext

internal class PbPythonModuleMembersProvider : PyModuleMembersProvider() {
  override fun resolveMember(module: PyFile, name: String, resolveContext: PyResolveContext): PsiElement? {
    val source = PbPythonSourceContext.resolve(module) ?: return null
    val localQn = QualifiedName.fromComponents(name)
    return resolveInProto(source, localQn).firstOrNull()
  }

  override fun getMembersByQName(module: PyFile, qName: String, context: TypeEvalContext): Collection<PyCustomMember> {
    val source = PbPythonSourceContext.resolve(module) ?: return emptyList()
    return PbPythonNamespaceType(source, module).getChildren()
      .mapNotNull { symbol ->
        symbol.name?.let { PyCustomMember(it, symbol) }
      }
  }
}
