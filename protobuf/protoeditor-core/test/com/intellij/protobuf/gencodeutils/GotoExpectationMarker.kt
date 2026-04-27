package com.intellij.protobuf.gencodeutils

import com.intellij.openapi.util.TextRange
import com.intellij.protobuf.lang.psi.PbFile
import com.intellij.protobuf.lang.psi.PbNamedElement
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiNamedElement
import junit.framework.TestCase.assertNotNull

/**
 * Expectation marker for testing Goto Definition and Goto Type Declaration.
 *
 * Marker: `EXPECT-NEXT: <file> / <element>`
 *
 * See [ExpectationMarkerBase]
 */
class GotoExpectationMarker private constructor(
  textRange: TextRange,
  val expectedFile: String,
  val expectedElementName: String,
) : ExpectationMarkerBase(textRange) {

  fun checkGotoTargets(srcReference: String, targets: Array<PsiElement>, lineNumber: Int) =
    when {
      expectedElementName == FILE_TARGET -> checkProtoFileTarget(srcReference, targets, lineNumber)
      expectedFile.endsWith(".proto") -> checkProtoElementTarget(srcReference, targets, lineNumber)
      else -> checkSourceTarget(srcReference, targets, lineNumber)
    }

  private fun checkProtoFileTarget(srcReference: String, targets: Array<PsiElement>, lineNumber: Int) {
    val matchingFile = targets
      .filterIsInstance<PbFile>()
      .firstOrNull { it.name == expectedFile }

    assertNotNull(errorMessage(srcReference, "file", targets, lineNumber), matchingFile)
  }

  private fun checkProtoElementTarget(srcReference: String, targets: Array<PsiElement>, lineNumber: Int) {
    val matchingElement = targets
      .filterIsInstance<PbNamedElement>()
      .firstOrNull { element ->
        element.pbFile.name == expectedFile &&
        element.protoRelativeQualifiedName() == expectedElementName
      }

    assertNotNull(errorMessage(srcReference, "proto element", targets, lineNumber), matchingElement)
  }

  private fun checkSourceTarget(srcReference: String, targets: Array<PsiElement>, lineNumber: Int) {
    val matchingElement = targets
      .filterIsInstance<PsiNamedElement>()
      .firstOrNull { element ->
        element.containingFile?.name == expectedFile &&
        element.name == expectedElementName
      }

    assertNotNull(errorMessage(srcReference, "source element", targets, lineNumber), matchingElement)
  }

  private fun errorMessage(
    srcReference: String,
    kind: String,
    targets: Array<PsiElement>,
    lineNumber: Int,
  ): String =
    "$srcReference -> $expectedFile: $expectedElementName " +
    "target $kind was not found among: ${renderTargets(targets)} " +
    "at line $lineNumber"

  private fun PbNamedElement.protoRelativeQualifiedName(): String? {
    val elementName = qualifiedName ?: return null
    val filePackage = pbFile.packageQualifiedName
    return elementName.removeHead(filePackage.componentCount).toString()
  }

  private fun renderTargets(targets: Array<PsiElement>): String {
    if (targets.isEmpty()) return "<no targets>"

    return targets.joinToString(separator = ", ") { target ->
      when (target) {
        is PbFile -> "PbFile(${target.name})"
        is PbNamedElement -> {
          val name = target.protoRelativeQualifiedName() ?: "<unnamed>"
          "PbNamedElement(${target.pbFile.name} / $name)"
        }
        is PsiNamedElement -> {
          val fileName = target.containingFile?.name ?: "<no file>"
          val name = target.name ?: "<unnamed>"
          "PsiNamedElement($fileName / $name)"
        }
        else -> {
          val fileName = target.containingFile?.name ?: "<no file>"
          "${target.javaClass.simpleName}(in $fileName)"
        }
      }
    }
  }

  companion object {
    // TODO: Rename to "EXPECT-GOTO"
    const val EXPECT_MARKER = "EXPECT-NEXT:"
    private const val FILE_TARGET = "<file>"

    @JvmStatic
    fun parseExpectations(file: PsiFile): List<GotoExpectationMarker> =
      parseFile(file, EXPECT_MARKER) { partitionRange, markerValue ->
        val parts = markerValue.split('/', limit = 2).map { it.trim() }
        require(parts.size == 2 && parts.all { it.isNotEmpty() })

        GotoExpectationMarker(partitionRange, parts[0], parts[1])
      }
  }
}
