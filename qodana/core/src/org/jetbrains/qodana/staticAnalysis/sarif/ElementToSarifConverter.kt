package org.jetbrains.qodana.staticAnalysis.sarif

import com.intellij.codeHighlighting.HighlightDisplayLevel
import com.intellij.codeInsight.daemon.impl.ProblemRelatedLocation
import com.intellij.codeInspection.DefaultInspectionToolResultExporter
import com.intellij.codeInspection.ProblemDescriptorUtil
import com.intellij.codeInspection.ex.CWE_TOP25_2023
import com.intellij.codeInspection.ex.InspectionMetaInformationService
import com.intellij.openapi.application.readAction
import com.intellij.openapi.components.PathMacroManager
import com.intellij.openapi.components.service
import com.jetbrains.qodana.sarif.model.*
import org.jdom.Element
import org.jetbrains.qodana.staticAnalysis.sarif.fingerprints.BaselineEqualityV2
import org.jetbrains.qodana.staticAnalysis.sarif.fingerprints.withPartialFingerprints
import org.jetbrains.qodana.staticAnalysis.sarif.textFormat.escapeContentInTag
import org.jetbrains.qodana.staticAnalysis.sarif.textFormat.htmlToMarkdown
import org.jetbrains.qodana.staticAnalysis.sarif.textFormat.htmlToPlainText

data class CommonDescriptor(val file: String, val line: Int?, val column: Int?, val length: Int?, val highlightedElement: String?, val language: String?)

/**
 * Provides methods to convert tool results from XML to SARIF format. Should be kept in sync with [JsonInspectionsReportConverter] logic
 * and any changes in the XML format of the inspection result.
 */
object ElementToSarifConverter {
  const val FILE = "file"
  const val LINE = "line"
  const val DESCRIPTION = "description"
  private const val OFFSET = "offset"
  private const val LENGTH = "length"
  private const val INSPECTION_RESULTS_LANGUAGE = "language"
  private const val FRAMEWORK = "framework"
  private const val MODULE = "module"
  private const val SEVERITY_ATTR = "severity"
  private const val HIGHLIGHTED_ELEMENT = "highlighted_element"

  internal suspend fun convertFromXmlFormat(problem: Element,
                                            macroManager: PathMacroManager,
                                            linesMargin: Int = CONTEXT_MAX_LINES_MARGIN,
                                            providedMessage: Message? = null,
                                            customizeBeforeFingerprint: suspend (Result) -> Unit = { }): Result {
    val problemLocation = commonDescriptor(problem)
    val result = convertCommonXmlPart(problem, providedMessage)
    var loc: Location? = null
    if (problemLocation.file.isNotBlank()) {
      loc = Location().also {
        it.physicalLocation = getPhysicalLocation(problemLocation, macroManager,
                                                   ContextMarginProvider.resetContextMargin(result, linesMargin))
      }
    }
    getLogicalLocation(problem)?.also { ll ->
      loc = (loc ?: Location()).also { l ->
        l.logicalLocations = ll
      }
    }

    getFingerprintData(problem)?.let { fingerprintData ->
      val props = result.properties ?: PropertyBag()
      props[BaselineEqualityV2.ADDITIONAL_FINGERPRINT_DATA] = fingerprintData
      result.properties = props
    }

    return result
      .withLocations(listOfNotNull(loc))
      .also { customizeBeforeFingerprint(it) }
      .withPartialFingerprints()
  }

  fun commonDescriptor(problem: Element): CommonDescriptor {
    return CommonDescriptor(
      problem.getChildText(FILE) ?: "",
      tryGetInt(problem, LINE),
      tryGetInt(problem, OFFSET),
      tryGetInt(problem, LENGTH),
      problem.getChildText(HIGHLIGHTED_ELEMENT),
      problem.getChildText(INSPECTION_RESULTS_LANGUAGE))
  }

  suspend fun ProblemRelatedLocation.toSarifLocation(macroManager: PathMacroManager, result: Result): Location {
    val loc = Location()

    val descriptor = readAction {
      val psiElement = getPsiElement()
      val path = macroManager.collapsePath(psiElement?.containingFile?.virtualFile?.url)
      CommonDescriptor(
        path ?: "",
        getLineNumber() + 1,
        getOffset(),
        getRange()?.length,
        psiElement?.text,
        psiElement?.language?.displayName
      )
    }
    loc.physicalLocation = getPhysicalLocation(descriptor, macroManager,
                                               ContextMarginProvider.resetContextMargin(result, CONTEXT_MAX_LINES_MARGIN))

    return loc
  }

  suspend fun convertCommonXmlPart(problem: Element, providedMessage: Message? = null): Result {
    val problemClass = problem
      .getChild(DefaultInspectionToolResultExporter.INSPECTION_RESULTS_PROBLEM_CLASS_ELEMENT)
    val severity = problemClass?.getAttributeValue(SEVERITY_ATTR)
    val inspectionId = problemClass.getAttributeValue(DefaultInspectionToolResultExporter.INSPECTION_RESULTS_ID_ATTRIBUTE)
    val level = HighlightDisplayLevel.find(severity)?.severity?.toLevel() ?: Level.NOTE
    val description: String? = problem.getChildText(DESCRIPTION)?.let {
      escapeContentInTag(ProblemDescriptorUtil.removeLocReference(it), "code")
    }

    val tags = mutableListOf<String>()
    for (element in sequenceOf(problem.getChildText(INSPECTION_RESULTS_LANGUAGE),
                               problem.getChild(FRAMEWORK)?.text)
    ) {
      if (!element.isNullOrEmpty()) {
        tags.add(element)
      }
    }
    val cweIds = service<InspectionMetaInformationService>().getState().inspections[inspectionId]?.cweIds
    if (cweIds != null && cweIds.any { CWE_TOP25_2023.contains(it) }) {
      tags.add("CWE Top 25")
    }

    val message = providedMessage ?: safeDescriptionText(description)
    return Result(message)
      .withRuleId(inspectionId)
      .withLevel(level)
      .withProperties(PropertyBag().withIdeaAndQodanaSeverities(severity, level).also {
        it.tags.addAll(tags)
      })
  }

  fun safeDescriptionText(description: String?): Message? =
    Message().withText(description?.let { htmlToPlainText(description) }).withMarkdown(description?.let { htmlToMarkdown(it) })

  private fun getFingerprintData(element: Element): Map<String, String>? =
    element.getChild(DefaultInspectionToolResultExporter.INSPECTION_RESULTS_FINGERPRINT_DATA)
      ?.attributes
      ?.associate { attr -> attr.name to attr.value }
      ?.takeUnless { it.isEmpty() }

  private fun getLogicalLocation(problem: Element): Set<LogicalLocation>? {
    val module = problem.getChildText(MODULE)
    if (module != null) {
      return setOf(LogicalLocation().withFullyQualifiedName(module).withKind("module"))
    }
    return null
  }

  private fun tryGetInt(problem: Element, elementName: String): Int? = problem.getChildText(elementName)?.toInt()
}
