package org.jetbrains.qodana.staticAnalysis.inspections.runner.globalOutput

import com.intellij.codeInspection.InspectionsResultUtil
import com.intellij.openapi.components.PathMacroManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.io.FileUtil
import com.intellij.openapi.util.text.StringUtil
import com.intellij.openapi.vfs.VirtualFileManager
import com.jetbrains.qodana.sarif.model.*
import org.jdom.Element
import org.jetbrains.qodana.staticAnalysis.inspections.runner.JVM_TAINT_ANALYSIS_INSPECTION_ID
import org.jetbrains.qodana.staticAnalysis.inspections.runner.PHP_VULNERABLE_PATHS_INSPECTION_ID
import org.jetbrains.qodana.staticAnalysis.inspections.runner.Problem
import org.jetbrains.qodana.staticAnalysis.inspections.runner.ProblemType
import org.jetbrains.qodana.staticAnalysis.inspections.runner.QodanaException
import org.jetbrains.qodana.staticAnalysis.inspections.runner.QodanaToolResultDatabase
import org.jetbrains.qodana.staticAnalysis.profile.QodanaProfile
import org.jetbrains.qodana.staticAnalysis.sarif.ElementToSarifConverter
import org.jetbrains.qodana.staticAnalysis.sarif.PROBLEM_TYPE
import org.jetbrains.qodana.staticAnalysis.sarif.fingerprints.withPartialFingerprints
import org.jetbrains.qodana.staticAnalysis.sarif.getArtifactLocation
import org.jetbrains.qodana.staticAnalysis.sarif.getOrAssignProperties
import org.jetbrains.qodana.staticAnalysis.sarif.loadTextFromVirtualFile
import java.nio.file.Path

private val vulnerableFlowMessage = Message().withText("Vulnerable code flow").withMarkdown("Vulnerable code flow")

/**
 * Responsible for handling FlowInspection.xml and FlowInspection_aggregate.xml files
 * as results of FlowInspection inspection.
 *
 * Note, that FlowInspection.xml is not being processed, rather taken from the processing queue,
 * so that it won't be processed by the fallback workflow
 */
abstract class GlobalFlowOutputConsumer: GlobalOutputConsumer {
  protected abstract fun getInspectionName(): String
  override suspend fun consumeOwnedFiles(
    profileState: QodanaProfile.QodanaProfileState,
    paths: List<Path>,
    database: QodanaToolResultDatabase,
    project: Project,
    consumer: (List<Problem>, String) -> Unit
  ) {
    if (!GlobalOutputConsumer.reportingInspectionAllowed(profileState, getInspectionName()) || paths.size != 2) return
    GlobalOutputConsumer.consumeOutputXmlFile(paths.first()) { _, root ->
      consumer(root.getChildren("problem").map { FlowProblem(it) }, getInspectionName())
    }
  }

  override fun ownedFiles(paths: List<Path>): List<Path> {
    val inspectionName = getInspectionName()
    val resultsPath = paths.findPathByName(inspectionName)
    val aggregateResultsPath = paths.findPathByName(inspectionName + InspectionsResultUtil.AGGREGATE)
    return listOfNotNull(aggregateResultsPath, resultsPath)
  }

  private fun List<Path>.findPathByName(name: String) = singleOrNull { FileUtil.getNameWithoutExtension(it.toFile()) == name }

  private class FlowProblem(val element: Element) : Problem {
    override suspend fun getSarif(macroManager: PathMacroManager, database: QodanaToolResultDatabase): Result {
      macroManager.collapsePathsRecursively(element)
      return convertFlowFromXmlFormat(element, macroManager).apply {
        properties = (properties ?: PropertyBag()).apply {
          when (ruleId) {
            PHP_VULNERABLE_PATHS_INSPECTION_ID, JVM_TAINT_ANALYSIS_INSPECTION_ID -> {
              if (!graphs.isNullOrEmpty()) {
                this[PROBLEM_TYPE] = ProblemType.TAINT
              } else {
                throw QodanaException("Graphs are empty for taint inspection: $ruleId.")
              }
            }
            else -> {
              this[PROBLEM_TYPE] = ProblemType.REGULAR
            }
          }
        }
      }
    }

    override fun getFile(): String? = element.getChildText("file")

    override fun getModule(): String? = null
  }
}

private suspend fun convertFlowFromXmlFormat(problem: Element, macroManager: PathMacroManager): Result {
  val problemLocation = ElementToSarifConverter.commonDescriptor(problem)
  val descriptionInProblem = problem.getChildText(ElementToSarifConverter.DESCRIPTION)
  val description = if (descriptionInProblem != null) {
    ElementToSarifConverter.safeDescriptionText(descriptionInProblem)
  } else {
    vulnerableFlowMessage
  }
  val result = ElementToSarifConverter.convertCommonXmlPart(problem, description)

  val nodes = mutableSetOf<Node>()
  val edges = mutableSetOf<Edge>()
  val fragmentLocations = mutableListOf<Location>()
  for (fragment in problem.getChild("fragments").getChildren("fragment")) {
    val fragmentFile = fragment.getAttributeValue(ElementToSarifConverter.FILE)
    val fragmentLine = fragment.getAttributeValue(ElementToSarifConverter.LINE).toInt()
    val fragmentColumn = fragment.getAttributeValue("column").toInt()
    val fragmentStart = fragment.getAttributeValue("start").toInt()
    val fragmentEnd = fragment.getAttributeValue("end").toInt()
    val markerRelationships = mutableSetOf<LocationRelationship>()
    for (marker in fragment.getChildren("marker")) {
      val markerLine = marker.getAttributeValue(ElementToSarifConverter.LINE).toInt()
      val markerColumn = marker.getAttributeValue("column").toInt()
      val markerStart = marker.getAttributeValue("start").toInt()
      val markerEnd = marker.getAttributeValue("end").toInt()
      val markerOrder = marker.getAttributeValue("order")
      val location =
        getLocationByAttributes(fragmentFile, markerLine, markerColumn, markerStart, markerEnd, problemLocation.language!!,
                                macroManager)
      if (location != null) {
        val successors = marker.getChild("successors")?.getChildren("marker")?.map(Element::getText)
        val predecessors = marker.getChild("predecessors")?.getChildren("marker")?.map(Element::getText)
        val sanitizedVulnerabilities = getVulnerabilityValues(marker, "sanitized_vulnerabilities")
        nodes.add(
          Node(markerOrder)
            .withLocation(location)
            .withProperties(PropertyBag()
                              .also { it["successors"] = successors }
                              .also { it["predecessors"] = predecessors }
                              .also { it["sanitized_vulnerabilities"] = sanitizedVulnerabilities })
        )

        if (successors != null) {
          for (successor in successors) {
            val edgeCnt = (edges.size + 1).toString()
            edges.add(Edge(edgeCnt, markerOrder, successor))
          }
        }
      }
      markerRelationships.add(LocationRelationship().withTarget(markerOrder.toInt()).withKinds(setOf("includes")))
    }
    getLocationByAttributes(fragmentFile, fragmentLine, fragmentColumn, fragmentStart, fragmentEnd, problemLocation.language!!,
                            macroManager)
      ?.withRelationships(markerRelationships)?.let {
        fragmentLocations.add(it)
      }
  }

  val targetMap = computeTarget(problem)
  val sources = computeSources(problem)

  result
    .getOrAssignProperties()
    .also { it["sink"] = targetMap }
    .also { it["sources"] = sources }

  return result
    .withLocations(fragmentLocations)
    .withGraphs(setOf(Graph().withNodes(nodes).withEdges(edges)))
    .withPartialFingerprints()
}

private fun computeSources(problem: Element) = problem.getChild("sources").getChildren("source").map {
  mapOf(
    "file" to it.getAttributeValue(ElementToSarifConverter.FILE),
    "sanitized_vulnerabilities" to getVulnerabilityValues(it, "sanitized_vulnerabilities"),
    "text" to it.getAttributeValue("text"),
    "order" to it.getAttributeValue("order"),
  )
}

private fun computeTarget(problem: Element): Map<String, Any?> {
  val target = problem.getChild("sink")
  val targetText = target.getAttributeValue("text")
  val targetFqn = target.getAttributeValue("fqn")
  val vulnerabilityValues = getVulnerabilityValues(target, "vulnerabilities")
  val targetParams = target.getChild("parameters")?.getChildren("parameter")?.firstOrNull()?.getAttributeValue("name")
  return mapOf(
    "text" to targetText,
    "fqn" to targetFqn,
    "vulnerabilities" to vulnerabilityValues,
    "parameters" to targetParams
  )
}

private fun getLocationByAttributes(fileUrl: String,
                                    line: Int, column: Int, startCharOffset: Int,
                                    endCharOffset: Int, language: String,
                                    macroManager: PathMacroManager): Location? {
  val artifactLocation = getArtifactLocation(fileUrl)
  val physicalLocation = PhysicalLocation().withArtifactLocation(artifactLocation)
  val virtualFile = VirtualFileManager.getInstance().findFileByUrl(macroManager.expandPath(fileUrl))
  val text = loadTextFromVirtualFile(virtualFile)
  if (text != null) {
    val startOffset = StringUtil.lineColToOffset(text, line - 1, column - 1)
    val length = endCharOffset - startCharOffset
    val snippet = ArtifactContent().withText(text.subSequence(startOffset, startOffset + length) as String?)
    physicalLocation
      .withRegion(getFlowProblemRegion(startOffset, column, length, line, language, snippet))
  }
  return Location()
    .withPhysicalLocation(physicalLocation)
}

private fun getFlowProblemRegion(charOffset: Int?,
                                 startColumn: Int?,
                                 length: Int?,
                                 line: Int?,
                                 language: String?,
                                 snippet: ArtifactContent?) = Region()
  .withCharOffset(charOffset)
  .withStartColumn(startColumn)
  .withCharLength(length)
  .withStartLine(line)
  .withSourceLanguage(language)
  .withSnippet(snippet)

private fun getVulnerabilityValues(marker: Element, tag: String): List<String>? {
  if (marker.getChild(tag) != null) {
    val vulnerabilities = marker.getChild(tag).getChildren("vulnerability")
    if (vulnerabilities != null) {
      return vulnerabilities.map { it.getAttributeValue("name") }
    }
  }
  return null
}