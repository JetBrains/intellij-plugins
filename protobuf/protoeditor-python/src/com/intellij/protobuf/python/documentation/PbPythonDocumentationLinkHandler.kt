package com.intellij.protobuf.python.documentation

import com.intellij.codeInsight.documentation.DocumentationManagerProtocol
import com.intellij.openapi.vfs.VirtualFileManager
import com.intellij.platform.backend.documentation.DocumentationLinkHandler
import com.intellij.platform.backend.documentation.DocumentationTarget
import com.intellij.platform.backend.documentation.LinkResolveResult
import com.intellij.protobuf.lang.psi.PbFile
import com.intellij.protobuf.lang.psi.PbNamedElement
import com.intellij.protobuf.lang.psi.PbSymbol
import com.intellij.protobuf.lang.stub.index.QualifiedNameIndex
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.stubs.StubIndex
import com.intellij.psi.util.QualifiedName
import com.jetbrains.python.psi.PyElement

/**
 * Resolves a type-name link in [PbPythonDocumentationTarget].
 *
 * A link is `psi_element://<file URL>:<qualified name>`, or `psi_element://<qualified name>` when the file is not known.
 */
internal class PbPythonDocumentationLinkHandler : DocumentationLinkHandler {

  override fun resolveLink(target: DocumentationTarget, url: String): LinkResolveResult? {
    val docTarget = target as? PbPythonDocumentationTarget ?: return null
    val anchor = docTarget.pyAnchor ?: return null
    if (!url.startsWith(DocumentationManagerProtocol.PSI_ELEMENT_PROTOCOL)) return null
    val link = url.removePrefix(DocumentationManagerProtocol.PSI_ELEMENT_PROTOCOL)

    val symbol = resolveByFileAndQualifiedName(link, anchor) ?: resolveByQualifiedName(link.substringAfterLast(':'), anchor)
                 ?: return null
    return LinkResolveResult.resolvedTarget(PbPythonDocumentationTarget(symbol, anchor))
  }

  private fun resolveByFileAndQualifiedName(link: String, anchor: PyElement): PbSymbol? {
    val separator = link.lastIndexOf(':')
    if (separator < 0 || !link.contains("://")) return null

    val virtualFile = VirtualFileManager.getInstance().findFileByUrl(link.take(separator)) ?: return null
    val pbFile = anchor.manager.findFile(virtualFile) as? PbFile ?: return null
    val qualifiedName = QualifiedName.fromDottedString(link.substring(separator + 1))
    return pbFile.localQualifiedSymbolMap[qualifiedName]?.firstOrNull()
  }

  private fun resolveByQualifiedName(qualifiedName: String, anchor: PyElement): PbSymbol? {
    val project = anchor.project
    return StubIndex.getElements(QualifiedNameIndex.KEY, qualifiedName, project, GlobalSearchScope.allScope(project), PbNamedElement::class.java)
      .firstNotNullOfOrNull { it as? PbSymbol }
  }
}
