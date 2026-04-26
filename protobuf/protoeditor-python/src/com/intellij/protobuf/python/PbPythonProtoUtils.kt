package com.intellij.protobuf.python

import com.intellij.protobuf.lang.psi.PbElement
import com.intellij.protobuf.lang.psi.PbEnumDefinition
import com.intellij.protobuf.lang.psi.PbFile
import com.intellij.protobuf.lang.psi.PbSymbol
import com.intellij.protobuf.lang.psi.util.PbPsiUtil
import com.intellij.psi.util.QualifiedName

internal object PbPythonProtoUtils {

  /**
   * @return A list of either [PbSymbol] or a single [PbFile]
   */
  fun resolveInProto(source: PbPythonSourceContext, localQn: QualifiedName): List<PbElement> =
    if (localQn.components.isEmpty()) {
      listOf(source.pbFile)
    }
    else {
      (resolveInPb2(source, localQn) + resolveInPb1WithNormalizedNames(source, localQn))
        .distinctBy { it.qualifiedName }
    }

  private fun resolveInPb2(
    source: PbPythonSourceContext,
    localQn: QualifiedName,
  ): List<PbSymbol> {
    val symbolMap = getLocalSymbolMap(source.pbFile)
    val symbols = symbolMap[localQn].orEmpty()

    val parentLocalQn = localQn.removeLastComponent()
    val enumValues = symbolMap[parentLocalQn].orEmpty().asSequence()
      .filterIsInstance<PbEnumDefinition>()
      .flatMap { it.enumValueMap[localQn.lastComponent] }

    return symbols + enumValues
  }

  /**
   * For API v1, the code generator converts nested messages like `Foo.Bar.Baz` to `Foo_Bar_Baz`.
   *
   * Try to match with the `.` separators normalized to `_`.
   */
  private fun resolveInPb1WithNormalizedNames(
    source: PbPythonSourceContext,
    localQn: QualifiedName,
  ): List<PbSymbol> {
    if (source.apiVersion != PbPythonSourceContext.ApiVersion.V1 || '_' !in localQn.toString()) {
      return emptyList()
    }
    val symbolMap = getLocalSymbolMap(source.pbFile)
    val symbols = findUnderscored(symbolMap, localQn)

    val parentLocalQn = localQn.removeLastComponent()
    val enumValues = findUnderscored(symbolMap, parentLocalQn).asSequence()
      .filterIsInstance<PbEnumDefinition>()
      .flatMap { it.enumValueMap[localQn.lastComponent] }

    return symbols + enumValues
  }

  private fun findUnderscored(symbolMap: Map<QualifiedName, List<PbSymbol>>, qn: QualifiedName): List<PbSymbol> {
    val target = qn.join("_")
    return symbolMap
      .filter { (candidateLocalQn, _) -> candidateLocalQn.join("_") == target }
      .values.flatten()
  }

  fun getLocalSymbolMap(pbFile: PbFile): Map<QualifiedName, List<PbSymbol>> =
    pbFile.exportedQualifiedSymbolMap.values.asSequence()
      .flatten()
      .filterNot { PbPsiUtil.isGeneratedMapEntry(it) || PbPsiUtil.isPackageElement(it) }
      .mapNotNull { symbol ->
        val fullQn = symbol.qualifiedName ?: return@mapNotNull null
        val symbolFile = symbol.containingFile as? PbFile ?: return@mapNotNull null

        // Imported symbols are re-exported with the same local name, but their package name differs
        val localQn = fullQn.removeHead(symbolFile.packageQualifiedName.componentCount)
        localQn to symbol
      }
      .groupBy({ it.first }, { it.second })
}
