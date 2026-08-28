package com.intellij.protobuf.python.gutter

import com.intellij.codeInsight.daemon.RelatedItemLineMarkerInfo
import com.intellij.codeInsight.daemon.RelatedItemLineMarkerProvider
import com.intellij.codeInsight.navigation.NavigationGutterIconBuilder
import com.intellij.icons.AllIcons
import com.intellij.openapi.editor.markup.GutterIconRenderer
import com.intellij.protobuf.lang.psi.PbEnumDefinition
import com.intellij.protobuf.lang.psi.PbFile
import com.intellij.protobuf.lang.psi.PbMessageDefinition
import com.intellij.protobuf.lang.psi.PbPackageStatement
import com.intellij.protobuf.lang.psi.PbSymbol
import com.intellij.protobuf.lang.psi.ProtoKeywordTokenType
import com.intellij.protobuf.python.PbPythonBundle
import com.intellij.protobuf.python.PbPythonSourceContext
import com.intellij.psi.PsiElement
import com.intellij.psi.search.FilenameIndex
import com.intellij.psi.search.GlobalSearchScope
import com.jetbrains.python.psi.PyClass
import com.jetbrains.python.psi.PyFile
import com.jetbrains.python.psi.resolve.PyResolveUtil
import com.jetbrains.python.psi.types.TypeEvalContext
import com.jetbrains.python.pyi.PyiFile
import javax.swing.Icon

/**
 * Adds gutter icons to a `.proto` file that navigate to the Python code generated from it:
 * the `package` keyword navigates to the generated `.py` file,
 * and the name of a message or an enum navigates to its class in the generated `.pyi` stub.
 *
 * The markers need index access and resolve in other files, so they are collected in the slow pass.
 */
internal class PbPythonLineMarkerProvider : RelatedItemLineMarkerProvider() {

  override fun getName(): String = PbPythonBundle.message("line.marker.provider.name")

  override fun getIcon(): Icon = AllIcons.Gutter.ImplementedMethod

  override fun collectNavigationMarkers(
    elements: List<PsiElement>,
    result: MutableCollection<in RelatedItemLineMarkerInfo<*>>,
    forNavigation: Boolean,
  ) {
    val pbFile = elements.firstOrNull()?.containingFile as? PbFile ?: return
    // Both files are looked up at most once for all elements of the pass
    val generatedPyFile by lazy { findGeneratedFile<PyFile>(pbFile, "py") }
    val generatedPyiFile by lazy { findGeneratedFile<PyiFile>(pbFile, "pyi") }

    // Go to Related Symbol passes the definitions instead of their name identifiers
    val candidates = if (forNavigation) elements + elements.mapNotNull { (it as? PbSymbol)?.nameIdentifier } else elements

    for (element in candidates.distinct()) {
      val parent = element.parent
      val marker = when {
        parent is PbPackageStatement && element.node.elementType is ProtoKeywordTokenType ->
          generatedPyFile?.let { createGeneratedPyLineMarker(element, it) }
        (parent is PbMessageDefinition || parent is PbEnumDefinition) && element == parent.nameIdentifier ->
          generatedPyiFile?.let { createPyiClassLineMarker(element, parent as PbSymbol, pbFile, it) }
        else -> null
      }
      marker?.let(result::add)
    }
  }

  private fun createGeneratedPyLineMarker(keyword: PsiElement, pyFile: PyFile): RelatedItemLineMarkerInfo<PsiElement> =
    NavigationGutterIconBuilder
      .create(AllIcons.Gutter.ImplementedMethod)
      .setTarget(pyFile)
      .setTooltipText(PbPythonBundle.message("line.marker.generated.python"))
      .setAlignment(GutterIconRenderer.Alignment.RIGHT)
      .createLineMarkerInfo(keyword)

  private fun createPyiClassLineMarker(
    identifier: PsiElement,
    pbSymbol: PbSymbol,
    pbFile: PbFile,
    pyiFile: PyiFile,
  ): RelatedItemLineMarkerInfo<PsiElement>? {
    val qualifiedName = pbSymbol.qualifiedName ?: return null
    val localQualifiedName = qualifiedName.removeHead(pbFile.packageQualifiedName.componentCount)

    val context = TypeEvalContext.codeAnalysis(pyiFile.project, pyiFile)
    val target = PyResolveUtil.resolveQualifiedNameInScope(localQualifiedName, pyiFile, context)
                   .firstNotNullOfOrNull { (it as? PyClass)?.nameIdentifier } ?: return null

    return NavigationGutterIconBuilder
      .create(AllIcons.Gutter.Unique)
      .setTarget(target)
      .setTooltipText(PbPythonBundle.message("line.marker.python.stub"))
      .setAlignment(GutterIconRenderer.Alignment.RIGHT)
      .createLineMarkerInfo(identifier)
  }

  /**
   * Finds the file with [extension] that `protoc` generated from [pbFile].
   * A file with the expected name can come from another `.proto` file with the same name, so each candidate is checked.
   */
  private inline fun <reified T : PyFile> findGeneratedFile(pbFile: PbFile, extension: String): T? {
    val virtualFile = pbFile.virtualFile ?: return null
    val baseName = virtualFile.nameWithoutExtension
    val scope = GlobalSearchScope.allScope(pbFile.project)

    return PbPythonSourceContext.ApiVersion.entries.asSequence()
      .flatMap { FilenameIndex.getVirtualFilesByName("$baseName${it.suffix}.$extension", scope) }
      .mapNotNull { pbFile.manager.findFile(it) as? T }
      .firstOrNull { PbPythonSourceContext.resolve(it)?.pbFile == pbFile }
  }
}
