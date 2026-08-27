package com.intellij.protobuf.python.documentation

import com.intellij.platform.backend.documentation.DocumentationTarget
import com.intellij.platform.backend.documentation.PsiDocumentationTargetProvider
import com.intellij.protobuf.lang.psi.PbSymbol
import com.intellij.protobuf.python.PbPythonProtoUtils.resolveInProto
import com.intellij.protobuf.python.PbPythonPsiUtils.locateInProto
import com.intellij.protobuf.python.PbPythonPsiUtils.locateInProtoUsingQualifier
import com.intellij.protobuf.python.types.PbPythonMessageType
import com.intellij.psi.PsiElement
import com.intellij.psi.util.parentOfType
import com.jetbrains.python.psi.PyArgumentList
import com.jetbrains.python.psi.PyCallExpression
import com.jetbrains.python.psi.PyElement
import com.jetbrains.python.psi.PyKeywordArgument
import com.jetbrains.python.psi.PyQualifiedExpression
import com.jetbrains.python.psi.PyReferenceExpression
import com.jetbrains.python.psi.types.TypeEvalContext

internal class PbPythonDocumentationTargetProvider : PsiDocumentationTargetProvider {
  override fun documentationTarget(element: PsiElement, originalElement: PsiElement?): DocumentationTarget? {
    val parent = originalElement?.parent as? PyElement ?: return null
    val context = TypeEvalContext.userInitiated(parent.project, parent.containingFile)

    val (pbSymbol, sourceElement) = when (parent) {
      // Documentation when hovering over '()' in Message calls
      is PyArgumentList -> {
        val callee = (parent.parent as? PyCallExpression)?.callee ?: return null
        val messageType = context.getType(callee) as? PbPythonMessageType ?: return null
        val pbSymbol = messageType.pbElement ?: return null
        pbSymbol to callee
      }

      // Documentation for Message arguments in calls
      is PyKeywordArgument -> {
        val keyword = parent.keyword ?: return null
        val callee = parent.parentOfType<PyCallExpression>()?.callee ?: return null
        val messageType = context.getType(callee) as? PbPythonMessageType ?: return null

        val localQn = messageType.localQn.append(keyword)
        val pbSymbol = resolveInProto(messageType.source, localQn)
                         .filterIsInstance<PbSymbol>()
                         .firstOrNull() ?: return null
        pbSymbol to parent
      }

      // Documentation for message field assignments
      is PyQualifiedExpression if parent.isQualified -> {
        val (source, localQn) = locateInProtoUsingQualifier(parent, context) ?: return null
        val pbSymbol = resolveInProto(source, localQn)
                         .filterIsInstance<PbSymbol>()
                         .firstOrNull() ?: return null
        pbSymbol to parent
      }

      // Documentation for regular variables and field reads
      is PyReferenceExpression -> {
        val pbSymbol = locateInProto(parent, context).asSequence()
                         .flatMap { (source, localQn) -> resolveInProto(source, localQn) }
                         .filterIsInstance<PbSymbol>()
                         .firstOrNull() ?: return null
        pbSymbol to parent
      }

      else -> return null
    }

    return PbPythonDocumentationTarget(pbSymbol, sourceElement)
  }
}
