package org.jetbrains.qodana.staticAnalysis.inspections.runner.globalOutput

import com.intellij.codeInspection.InspectionsResultUtil
import com.intellij.openapi.components.PathMacroManager
import com.intellij.openapi.util.io.FileUtil
import com.intellij.openapi.project.Project
import com.jetbrains.qodana.sarif.model.CodeFlow
import com.jetbrains.qodana.sarif.model.Location
import com.jetbrains.qodana.sarif.model.LocationRelationship
import com.jetbrains.qodana.sarif.model.PropertyBag
import com.jetbrains.qodana.sarif.model.Result
import com.jetbrains.qodana.sarif.model.ThreadFlow
import com.jetbrains.qodana.sarif.model.ThreadFlowLocation
import org.jdom.Element
import org.jetbrains.qodana.staticAnalysis.inspections.runner.Problem
import org.jetbrains.qodana.staticAnalysis.inspections.runner.ProblemType
import org.jetbrains.qodana.staticAnalysis.inspections.runner.QodanaToolResultDatabase
import org.jetbrains.qodana.staticAnalysis.profile.QodanaProfile
import org.jetbrains.qodana.staticAnalysis.sarif.ElementToSarifConverter
import org.jetbrains.qodana.staticAnalysis.sarif.PROBLEM_TYPE
import org.jetbrains.qodana.staticAnalysis.sarif.fingerprints.withPartialFingerprints
import org.jetbrains.qodana.staticAnalysis.sarif.getOrAssignProperties
import java.nio.file.Path
import org.jetbrains.qodana.staticAnalysis.inspections.runner.globalOutput.CustomGlobalFlowFingerprintCalculator.Companion.withCustomPartialFingerprints

/**
 * Abstract base class for consuming DFA trace aggregate XML files and converting them to SARIF
 * using codeFlows/threadFlows (linear, sequential representation suitable for DFA analysis paths
 * such as null dereference and data flow errors).
 */
abstract class GlobalTraceFlowOutputConsumer : GlobalOutputConsumer {

  protected abstract fun getInspectionName(): String

  override fun ownedFiles(paths: List<Path>): List<Path> {
    val inspectionName = getInspectionName()
    val resultsPath = paths.singleOrNull { FileUtil.getNameWithoutExtension(it.fileName.toString()) == inspectionName }
    val aggregateResultsPath = paths.singleOrNull {
      FileUtil.getNameWithoutExtension(it.fileName.toString()) == inspectionName + InspectionsResultUtil.AGGREGATE
    }
    return listOfNotNull(aggregateResultsPath, resultsPath)
  }

  override suspend fun consumeOwnedFiles(
    profileState: QodanaProfile.QodanaProfileState,
    paths: List<Path>,
    database: QodanaToolResultDatabase,
    project: Project,
    consumer: (List<Problem>, String) -> Unit
  ) {
    if (!GlobalOutputConsumer.reportingInspectionAllowed(profileState, getInspectionName()) || paths.size != 2) return
    GlobalOutputConsumer.consumeOutputXmlFile(paths.first()) { _, root ->
      consumer(root.getChildren("problem").map { TraceProblem(it) }, getInspectionName())
    }
  }

  private class TraceProblem(val element: Element) : Problem {
    override suspend fun getSarif(macroManager: PathMacroManager, database: QodanaToolResultDatabase): Result {
      macroManager.collapsePathsRecursively(element)
      return convertTraceFromXmlFormat(element, macroManager).apply {
        if (!codeFlows.isNullOrEmpty()) {
          getOrAssignProperties()[PROBLEM_TYPE] = ProblemType.TAINT
        }
      }
    }

    override fun getFile(): String? = element.getChildText("file")

    override fun getModule(): String? = null
  }
}

private suspend fun convertTraceFromXmlFormat(problem: Element, macroManager: PathMacroManager): Result {
  val problemLocation = ElementToSarifConverter.commonDescriptor(problem)
  val descriptionInProblem = problem.getChildText(ElementToSarifConverter.DESCRIPTION)
  val description = if (descriptionInProblem != null) {
    ElementToSarifConverter.safeDescriptionText(descriptionInProblem)
  } else {
    null
  }
  val result = ElementToSarifConverter.convertCommonXmlPart(problem, description)

  // Collect (order, ThreadFlowLocation) pairs from all markers across all fragments;
  // also build fragment-level locations that reference their contained marker orders.
  val orderedLocations = mutableListOf<Pair<Int, ThreadFlowLocation>>()
  val fragmentLocations = mutableListOf<Location>()
  for (fragment in problem.getChild("fragments").getChildren("fragment")) {
    val fragmentFile = fragment.getAttributeValue(ElementToSarifConverter.FILE)
    val fragmentLine = fragment.getAttributeValue(ElementToSarifConverter.LINE).toInt()
    val fragmentColumn = fragment.getAttributeValue("column").toInt()
    val fragmentStart = fragment.getAttributeValue("start").toInt()
    val fragmentEnd = fragment.getAttributeValue("end").toInt()
    val markerRelationships = mutableSetOf<LocationRelationship>()
    var fragmentMinOrder = Int.MAX_VALUE
    for (marker in fragment.getChildren("marker")) {
      val markerLine = marker.getAttributeValue(ElementToSarifConverter.LINE).toInt()
      val markerColumn = marker.getAttributeValue("column").toInt()
      val markerStart = marker.getAttributeValue("start").toInt()
      val markerEnd = marker.getAttributeValue("end").toInt()
      val markerOrder = marker.getAttributeValue("order").toInt()
      val markerFqn = marker.getAttributeValue(FQN_ATTRIBUTE_NAME)
      markerRelationships.add(LocationRelationship().withTarget(markerOrder).withKinds(setOf("includes")))
      if (markerOrder < fragmentMinOrder) fragmentMinOrder = markerOrder
      val location = getLocationByAttributes(
        fragmentFile, markerLine, markerColumn, markerStart, markerEnd,
        problemLocation.language!!, macroManager, false
      )
      if (location != null) {
        orderedLocations.add(markerOrder to ThreadFlowLocation()
          .withLocation(location)
          .withExecutionOrder(markerOrder)
          .withProperties(PropertyBag().also { it[FQN_ATTRIBUTE_NAME] = markerFqn })
        )
      }
    }
    getLocationByAttributes(fragmentFile, fragmentLine, fragmentColumn, fragmentStart, fragmentEnd,
                            problemLocation.language!!, macroManager, true)
      ?.withRelationships(markerRelationships)
      ?.let { fragmentLocations.add(it) }
  }

  // Sort by order ascending (source → sink)
  orderedLocations.sortBy { it.first }
  val threadFlowLocations = orderedLocations.map { it.second }

  if (threadFlowLocations.isNotEmpty()) {
    result.withCodeFlows(listOf(
      CodeFlow().withThreadFlows(listOf(
        ThreadFlow().withLocations(threadFlowLocations)
      ))
    ))
  }

  val targetMap = computeTarget(problem)
  val sources = computeSources(problem)
  result
    .getOrAssignProperties()
    .also { it["sink"] = targetMap }
    .also { it["sources"] = sources }

  return result
    .withLocations(fragmentLocations)
    .withPartialFingerprints()
    .withCustomPartialFingerprints()
}