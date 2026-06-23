package com.intellij.protobuf.python

import com.intellij.protobuf.python.types.PbPythonAbstractType
import com.intellij.psi.util.QualifiedName
import com.jetbrains.python.psi.PyFile
import com.jetbrains.python.psi.PyFromImportStatement
import com.jetbrains.python.psi.PyImportElement
import com.jetbrains.python.psi.PyQualifiedExpression
import com.jetbrains.python.psi.PyReferenceExpression
import com.jetbrains.python.psi.resolve.PyResolveUtil
import com.jetbrains.python.psi.types.TypeEvalContext

internal object PbPythonPsiUtils {

  fun locateInProto(expr: PyReferenceExpression, context: TypeEvalContext): List<Pair<PbPythonSourceContext, QualifiedName>> {
    locateInProtoUsingQualifier(expr, context)?.let { return listOf(it) }

    return buildList {
      for ((pyFile, localQn) in decomposeReference(expr)) {
        val source = PbPythonSourceContext.resolve(pyFile) ?: continue
        add(source to localQn)
      }
    }
  }

  fun locateInProtoUsingQualifier(expr: PyQualifiedExpression, context: TypeEvalContext): Pair<PbPythonSourceContext, QualifiedName>? {
    val qualifier = expr.qualifier ?: return null
    val name = expr.referencedName ?: return null
    val qualType = context.getType(qualifier) as? PbPythonAbstractType<*> ?: return null
    return qualType.source to qualType.localQn.append(name)
  }

  /**
   * Resolves expression to a Python file and local qualified name.
   *
   * For example, given `foo_pb2.Foo.Bar` returns `(foo_pb2.py, Foo.Bar)`.
   */
  private fun decomposeReference(refExpr: PyReferenceExpression): List<Pair<PyFile, QualifiedName>> {
    // Using import statement
    val resolvedImportElement = refExpr.parent as? PyImportElement
                                ?: PyResolveUtil.resolveLocally(refExpr).firstNotNullOfOrNull { it as? PyImportElement }
    if (resolvedImportElement != null) {
      // Cheap syntactic gate: only generated `_pb2`/`_pb` modules can hold protobuf types.
      // Done before any resolve so ordinary imports skip the expensive import resolution below.
      if (isProtobufModuleName(resolvedImportElement.importedQName)) {
        val sourceFile = resolvedImportElement.multiResolve()
          .firstNotNullOfOrNull { it.element as? PyFile }
        if (sourceFile != null) {
          return listOf(sourceFile to QualifiedName.fromComponents())
        }
      }

      val importStatement = resolvedImportElement.containingImportStatement
      if (importStatement is PyFromImportStatement) {
        if (!isProtobufModuleName(importStatement.importSourceQName)) return emptyList()
        val importSource = importStatement.importSource
        val sourceFile = importSource?.reference?.resolve() as? PyFile ?: return emptyList()
        val localQn = resolvedImportElement.importedQName ?: return emptyList()
        return listOf(sourceFile to localQn)
      }

      return emptyList()
    }

    // From <file> import *
    val name = refExpr.name
    val containingFile = refExpr.containingFile as? PyFile
    if (name != null && containingFile != null) {
      return containingFile.fromImports
        .asSequence()
        .filter { it.isStarImport }
        .filter { isProtobufModuleName(it.importSourceQName) }
        .mapNotNull { it.importSource?.reference?.resolve() as? PyFile }
        .map { it to QualifiedName.fromComponents(name) }
        .toList()
    }

    return emptyList()
  }

  /**
   * Cheap syntactic check whether [qName] refers to a generated protobuf module (`*_pb2` / `*_pb`),
   * performed without resolving the reference so that ordinary imports skip the expensive import
   * resolution in [decomposeReference].
   */
  private fun isProtobufModuleName(qName: QualifiedName?): Boolean {
    val baseName = qName?.lastComponent ?: return false
    return PbPythonSourceContext.ApiVersion.fromFileName(baseName) != null
  }
}
