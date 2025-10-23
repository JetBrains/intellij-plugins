package org.jetbrains.qodana.problem

import com.intellij.codeInspection.ProblemDescriptorUtil
import com.intellij.openapi.components.PathMacroManager
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.editor.Document
import com.intellij.openapi.project.Project
import com.intellij.openapi.project.guessProjectDir
import com.intellij.openapi.util.NlsSafe
import com.intellij.openapi.util.TextRange
import com.intellij.openapi.vfs.LocalFileSystem
import com.intellij.openapi.vfs.VfsUtil
import com.intellij.openapi.vfs.VirtualFile
import com.jetbrains.qodana.sarif.model.ArtifactLocation
import com.jetbrains.qodana.sarif.model.Location
import com.jetbrains.qodana.sarif.model.OriginalUriBaseIds
import com.jetbrains.qodana.sarif.model.Result
import com.jetbrains.qodana.sarif.model.Run
import org.jetbrains.annotations.VisibleForTesting
import org.jetbrains.qodana.QodanaBundle
import org.jetbrains.qodana.report.ValidatedSarif
import org.jetbrains.qodana.report.isInBaseline
import org.jetbrains.qodana.staticAnalysis.sarif.QodanaSeverity
import org.jetbrains.qodana.staticAnalysis.sarif.fingerprints.forEachNotNull
import org.jetbrains.qodana.staticAnalysis.sarif.qodanaSeverity
import java.io.File
import java.nio.file.InvalidPathException
import java.nio.file.Path
import java.nio.file.Paths
import kotlin.io.path.Path
import kotlin.io.path.pathString

private val LOG = Logger.getInstance(SarifProblem::class.java)

/**
 * Problem from SARIF report.
 * Contained properties are determined by SARIF report itself
 */
data class SarifProblem(
  val startLine: Int?,
  val startColumn: Int?,
  val endLine: Int?,
  val endColumn: Int?,
  val charLength: Int?,
  val relativePathToFile: String,
  val traces: Collection<SarifTrace>,
  val message: String,
  val qodanaSeverity: QodanaSeverity,
  val inspectionId: String,
  val baselineState: Result.BaselineState,
  val snippetText: String?,
  val revisionId: String?
) {
  companion object {
    fun fromReport(project: Project, sarif: ValidatedSarif, projectPath: String? = null): List<SarifProblem> {
      return sarif.runsToResults.flatMap { (run, reportResults) ->
        val absoluteSrcDirPrefix = getAbsolutPathsPrefix(project, reportResults, projectPath)
        val revisionId = run.revisionId()

        val resultsWithRelationshipsTaint = reportResults.associateWith { result ->
          result.locations?.filter { location ->
            location != null && location.relationships != null
          } ?: emptyList()
        }.filter { !it.value.isEmpty() && !it.key.graphs.isNullOrEmpty() }

        val problemsForTaintAnalysisSinks = getPossibleTaintAnalysisSinksResultsAndLocations(
          reportResults, resultsWithRelationshipsTaint
        ).mapNotNull { fromResultWithLocation(run, it.first, it.second, absoluteSrcDirPrefix, revisionId) }

        val otherProblems = reportResults.flatMap { result ->
          result.locations?.filter { location ->
            location != null && resultsWithRelationshipsTaint.all { it.key != result || !it.value.contains(location) }
          }?.mapNotNull { location ->
            fromResultWithLocation(run, result, location, absoluteSrcDirPrefix, revisionId)
          } ?: emptyList()
        }

        otherProblems + problemsForTaintAnalysisSinks
      }
    }
  }

  val isInBaseline: Boolean
    get() = baselineState.isInBaseline

  val hasRange: Boolean
    get() = (startLine ?: -1) >= 0

  val relativeNioFile: Path = Path(relativePathToFile)

  val defaultProperties: SarifProblemProperties =
    SarifProblemProperties(true, false, false, startLine ?: -1, startColumn ?: -1)

  fun getVirtualFile(project: Project): VirtualFile? = project.findRelativeVirtualFile(relativePathToFile)

  fun getTextRangeInDocument(document: Document): TextRange? {
    val lineStartOffset = startLine?.let { line ->
      if (line < document.lineCount)
        document.getLineStartOffset(line)
      else
        null
    } ?: return null

    val offsetInsideStartLine = startColumn ?: return null

    val startOffset = lineStartOffset + offsetInsideStartLine
    val endOffset = getEndOffset(document, startOffset) ?: return null
    if (startOffset > document.getLineEndOffset(startLine) || endOffset > document.textLength) {
      return null
    }
    val textRange = TextRange(startOffset, endOffset)
    if (isEqualToSnippet(document.getText(textRange))) {
      return textRange
    }
    return null
  }

  private fun getEndOffset(document: Document, startOffset: Int): Int? {
    if (charLength != null) {
      return startOffset + charLength
    }
    if (endLine == null || endColumn == null) {
      return null
    }
    if (endLine < document.lineCount) {
      return document.getLineStartOffset(endLine) + endColumn
    }
    return null
  }

  fun isEqualToSnippet(text: String): Boolean {
    return !(snippetText != null && text != snippetText)
  }
}

fun SarifProblem.buildDescription(useQodanaPrefix: Boolean, showSeverity: Boolean): @NlsSafe String {
  val useQodanaPrefixInt = if (useQodanaPrefix) 1 else 0
  return if (showSeverity)
    QodanaBundle.message("qodana.problem.description.with.level", qodanaSeverity, useQodanaPrefixInt, message)
  else
    QodanaBundle.message("qodana.problem.description.without.level", useQodanaPrefixInt, message)
}

private fun Result.buildProblemMessage(): String {
  return message?.let { message.text?.let { text -> ProblemDescriptorUtil.unescapeTags(text) } }
         ?: QodanaBundle.message("qodana.problem.default.message")
}

internal fun Project.findRelativeVirtualFile(relativePath: String): VirtualFile? =
  guessProjectDir()?.findFileByRelativePath(relativePath) ?: basePath?.let { VfsUtil.findFileByIoFile(File(it, relativePath), true) }

private fun fromResultWithLocation(
  run: Run,
  result: Result,
  location: Location,
  absoluteSrcDirPrefix: String,
  revisionId: String?
): SarifProblem? {
  if (result.baselineState == Result.BaselineState.ABSENT ||
      result.ruleId == null ||
      location.physicalLocation?.artifactLocation?.uri == null
  ) {
    return null
  }
  val originalUriBaseIds = run.originalUriBaseIds ?: OriginalUriBaseIds()
  val fixedUri = getLocationWithoutPrefix(location, originalUriBaseIds, absoluteSrcDirPrefix) ?: return null

  return SarifProblem(
    /** Line and column are zero-based values */
    startLine = location.physicalLocation.region?.startLine?.minus(1),
    startColumn = location.physicalLocation.region?.startColumn?.minus(1),
    endLine = location.physicalLocation.region?.endLine?.minus(1),
    endColumn = location.physicalLocation.region?.endColumn?.minus(1),
    charLength = location.physicalLocation.region?.charLength,
    traces = result.buildTraces(originalUriBaseIds, absoluteSrcDirPrefix),
    relativePathToFile = fixedUri,
    message = result.buildProblemMessage(),
    qodanaSeverity = result.qodanaSeverity,
    inspectionId = result.ruleId,
    baselineState = result.baselineState ?: Result.BaselineState.NEW,
    snippetText = location.physicalLocation.region?.snippet?.text,
    revisionId = revisionId
  )
}

private fun getLocationWithoutPrefix(location: Location, originalUriBaseIds: OriginalUriBaseIds, absoluteSrcDirPrefix: String): String? {
  val uriBaseId = location.physicalLocation?.artifactLocation?.uriBaseId
  return if (uriBaseId == null) {
    try {
      Paths.get(location.physicalLocation.artifactLocation.uri).pathString.removePrefix(absoluteSrcDirPrefix)
    }
    catch (_: InvalidPathException) {
      null
    }
  }
  else {
    val pathFromProjectRoot = resolveUriBaseId(originalUriBaseIds, uriBaseId)
    pathFromProjectRoot + location.physicalLocation.artifactLocation.uri
  }
}

fun resolveUriBaseId(originalUriBaseIds: OriginalUriBaseIds, uriBaseId: String): String {
  try {
    return resolveUriBaseIdImpl(originalUriBaseIds, uriBaseId, mutableListOf())
  } catch (e: IllegalArgumentException) {
    LOG.warn("Failed to resolve uriBaseId: $uriBaseId", e)
    return ""
  }
}

private fun resolveUriBaseIdImpl(originalUriBaseIds: OriginalUriBaseIds, uriBaseId: String, visited: MutableList<String>): String {
  if (visited.contains(uriBaseId)) {
    visited.add(uriBaseId)
    throw IllegalArgumentException("Cyclic uriBaseId resolution: ${visited.joinToString()}")
  }
  visited.add(uriBaseId)

  val artifactLocation = originalUriBaseIds[uriBaseId] ?: return ""

  val uri = artifactLocation.uri ?: ""
  val parentUriBaseId = artifactLocation.uriBaseId

  return if (parentUriBaseId != null) {
    val parentUri = resolveUriBaseIdImpl(originalUriBaseIds, parentUriBaseId, visited)
    parentUri + uri
  } else {
    uri
  }
}

private fun getAbsolutPathsPrefix(project: Project, reportResults: List<Result>, projectPath: String?): String {
  projectPath ?: return ""
  val macroManager = PathMacroManager.getInstance(project)
  val absolutePaths = reportResults.flatMap { result ->
    result.locations
      ?.filter {
        val artifactLocation: ArtifactLocation? = it.physicalLocation?.artifactLocation
        artifactLocation != null && artifactLocation.uriBaseId == null
      }
      ?.mapNotNull { location ->
        val uriString = location.physicalLocation.artifactLocation?.uri ?: return@mapNotNull null
        val uri = macroManager.expandPath(uriString)
        try {
          Paths.get(uri).pathString
        } catch (_: IllegalArgumentException) {
          null
        }
      } ?: emptyList()
  }.distinct()

  val absoluteUrisPrefix = absolutePaths.foldRight(if (absolutePaths.isEmpty()) "" else absolutePaths[0]) { l, r -> l.commonPrefixWith(r) }

  return if (absoluteUrisPrefix == "") absoluteUrisPrefix else tryGuessSrcDir(absoluteUrisPrefix, projectPath)
}

private fun tryGuessSrcDir(absoluteUrisPrefix: String, projectPathString: String): String {
  val absoluteUrisPrefixPath = Paths.get(absoluteUrisPrefix)
  val projectPath = Paths.get(projectPathString)

  var i = absoluteUrisPrefixPath.nameCount - 1
  var firstExistingIndex = -1

  do {
    val pathToFind = projectPath.resolve(absoluteUrisPrefixPath.subpath(i, absoluteUrisPrefixPath.nameCount))
    if (LocalFileSystem.getInstance().findFileByNioFile(pathToFind) != null) {
      firstExistingIndex = i
    }
  } while (--i >= 0)

  return if (firstExistingIndex == -1) absoluteUrisPrefix else {
    val suffixToRemove = absoluteUrisPrefixPath.subpath(firstExistingIndex, absoluteUrisPrefixPath.nameCount).pathString
    absoluteUrisPrefixPath.pathString.removeSuffix(suffixToRemove)
  }
}

private fun getPossibleTaintAnalysisSinksResultsAndLocations(
  reportResults: List<Result>,
  resultsWithRelationshipsTaint: Map<Result, List<Location>>
): List<Pair<Result, Location>> {
  return reportResults
    .asSequence()
    .filter { it.graphs != null }
    .flatMap { result ->
      result.graphs.flatMap { graph ->
        graph.nodes.filter { node ->
          graph.edges.none { it.sourceNodeId == node.id }
        }.map { node -> result to node }
      }
    }
    .mapNotNull { (result, node) ->
      val location = node.location
                     ?: resultsWithRelationshipsTaint[result]?.singleOrNull { location ->
                       location.relationships.any { relationship -> relationship.target == node.id.toInt() }
                     }
      location?.let { result to it }
    }
    .toList()
}

private fun Result.buildTraces(originalUriBaseIds: OriginalUriBaseIds, absoluteSrcDirPrefix: String) = buildList {
  graphs.forEachNotNull { graph ->
    val nodes = graph.nodes.sortedBy { it.id.toIntOrNull() }.mapNotNull { node ->
      SarifTrace.Node(
        startLine = node.location.physicalLocation.region.startLine?.minus(1),
        startColumn = node.location.physicalLocation.region.startColumn?.minus(1),
        charLength = node.location.physicalLocation.region.charLength,
        relativePathToFile = getLocationWithoutPrefix(node.location, originalUriBaseIds, absoluteSrcDirPrefix)
      )
    }
    if (nodes.isNotEmpty()) {
      add(SarifTrace(message.text, nodes))
    }
  }
}

@VisibleForTesting
fun Run.revisionId(): String? = versionControlProvenance?.firstOrNull()?.revisionId

data class SarifTrace(val description: String, val nodes: Collection<Node>) {
  data class Node(
    val startLine: Int?, val startColumn: Int?, val charLength: Int?, val relativePathToFile: String?)
}