package com.intellij.protobuf.python.navigation

import com.intellij.codeInsight.navigation.actions.TypeDeclarationPlaceAwareProvider
import com.intellij.openapi.editor.Editor
import com.intellij.protobuf.lang.psi.PbEnumDefinition
import com.intellij.protobuf.lang.psi.PbEnumValue
import com.intellij.protobuf.lang.psi.PbMapField
import com.intellij.protobuf.lang.psi.PbMessageType
import com.intellij.protobuf.lang.psi.PbSimpleField
import com.intellij.protobuf.lang.psi.PbTypeName
import com.intellij.protobuf.python.PbPythonNames
import com.intellij.protobuf.python.PbPythonPsiUtils.findPbSymbolForPyClass
import com.intellij.protobuf.python.toPyClass
import com.intellij.protobuf.python.types.PbPythonAbstractType
import com.intellij.protobuf.python.types.PbPythonTypeUtils.getScalarTypeByProtoName
import com.intellij.psi.PsiDocumentManager
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.util.parentOfType
import com.jetbrains.python.psi.PyClass
import com.jetbrains.python.psi.PyElement
import com.jetbrains.python.psi.PyFile
import com.jetbrains.python.psi.PyTypedElement
import com.jetbrains.python.psi.types.PyTypeUtil.toStream
import com.jetbrains.python.psi.types.TypeEvalContext

internal class PbPythonTypeDeclarationProvider : TypeDeclarationPlaceAwareProvider {

  override fun getSymbolTypeDeclarations(symbol: PsiElement, editor: Editor, offset: Int): Array<PsiElement>? {
    val contextFile = PsiDocumentManager.getInstance(symbol.project).getPsiFile(editor.document)
    return getTypeDeclarationsInner(symbol, contextFile)
  }

  override fun getSymbolTypeDeclarations(symbol: PsiElement): Array<PsiElement>? =
    getTypeDeclarationsInner(symbol, null)

  private fun getTypeDeclarationsInner(
    stubbedSymbol: PsiElement,
    contextFile: PsiFile?,
  ): Array<PsiElement>? {
    // Python anchor also ensures that this provider only handles calls from Python files
    val pyAnchorFile = stubbedSymbol.containingFile as? PyFile
                       ?: contextFile as? PyFile
                       ?: return null

    val symbol = when (stubbedSymbol) {
      is PyClass -> findPbSymbolForPyClass(stubbedSymbol).firstOrNull() // .pyi stub
      else -> stubbedSymbol
    }

    val targets = when (symbol) {
      is PbMessageType ->
        listOfNotNull(PbPythonNames.MESSAGE_CLASS.toPyClass(pyAnchorFile))

      is PbEnumDefinition ->
        listOfNotNull(PbPythonNames.ENUM_METACLASS.toPyClass(pyAnchorFile))

      is PbSimpleField ->
        resolvePbTypeNameDeclarations(symbol.typeName, pyAnchorFile)

      is PbMapField ->
        symbol.valueType?.let { resolvePbTypeNameDeclarations(it, pyAnchorFile) }.orEmpty()

      is PbEnumValue ->
        listOfNotNull(symbol.parentOfType<PbEnumDefinition>())

      is PyTypedElement -> {
        val context = TypeEvalContext.userInitiated(pyAnchorFile.project, pyAnchorFile)
        val type = context.getType(symbol) as? PbPythonAbstractType<*> ?: return null
        listOfNotNull(type.pbElement)
      }

      else -> return null
    }

    if (targets.isEmpty()) return null
    return targets.distinct().toTypedArray()
  }

  private fun resolvePbTypeNameDeclarations(typeName: PbTypeName, anchor: PyElement): List<PsiElement> {
    // Option 1: it's another Protobuf type
    typeName.symbolPath.reference?.resolve()?.let { return listOf(it) }
    // Option 2: it's some builtin Python type
    return getScalarTypeByProtoName(typeName, anchor).toStream()
      .mapNotNull { it?.declarationElement }
      .toList()
  }
}
