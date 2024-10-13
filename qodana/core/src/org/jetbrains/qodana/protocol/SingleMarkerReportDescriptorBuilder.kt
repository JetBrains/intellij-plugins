package org.jetbrains.qodana.protocol

import com.intellij.openapi.project.Project
import com.jetbrains.qodana.sarif.model.*
import org.jetbrains.qodana.report.ReportDescriptorBuilder
import org.jetbrains.qodana.report.ReportResult
import org.jetbrains.qodana.report.ReportValidator
import org.jetbrains.qodana.staticAnalysis.sarif.QODANA_SEVERITY_KEY
import java.net.URI
import java.util.*

class SingleMarkerReportDescriptorBuilder(
  private val project: Project,
  private val parameters: OpenInIdeProblemParameters
) : ReportDescriptorBuilder<SingleMarkerReportDescriptor> {
  override suspend fun createReportDescriptor(): SingleMarkerReportDescriptor {
    val report = constructSingleMarkerReport()
    val validatedReport = when(val validatedReportResult = ReportValidator.validateReport(report)) {
      is ReportResult.Fail -> validatedReportResult.error.throwException()
      is ReportResult.Success -> validatedReportResult.loadedSarifReport
    }

    return SingleMarkerReportDescriptor(project, validatedReport, parameters.pathText, parameters.revisionId, parameters.origin, parameters.message)
  }

  private fun constructSingleMarkerReport(): SarifReport {
    val automationDetails = RunAutomationDetails()
      .withGuid(UUID.randomUUID().toString())
      .withId("${parameters.pathText} - ${parameters.message}")

    val vcsDetails = if (parameters.revisionId != null) VersionControlDetails(URI.create(parameters.origin)).withRevisionId(parameters.revisionId) else null

    val inspectionId = parameters.inspectionId ?: ""
    val category = parameters.inspectionCategory ?: ""

    val result = Result(Message().withText(parameters.message)).withLocations(listOf(
      Location()
        .withPhysicalLocation(
          PhysicalLocation()
            .withArtifactLocation(
              ArtifactLocation().withUri(parameters.path).withUriBaseId("SRCROOT")
            )
            .withRegion(
              Region().withStartColumn(parameters.column).withStartLine(parameters.line).withCharLength(parameters.markerLength.toInt())
            )
        )
    )).withRuleId(inspectionId).withProperties(PropertyBag().apply { put(QODANA_SEVERITY_KEY, parameters.severity?.toString()) })

    val rule = ReportingDescriptor(inspectionId)
      .withShortDescription(MultiformatMessageString(parameters.inspectionName ?: ""))
      .withRelationships(setOf(ReportingDescriptorRelationship(ReportingDescriptorReference().withId(category))))

    val driver = ToolComponent("single-marker-tool")
      .withTaxa(listOf(ReportingDescriptor(category).withName(category)))

    val tool = Tool(driver)
      .withExtensions(setOf(ToolComponent("").withRules(listOf(rule))))

    val run = Run(tool)
      .withAutomationDetails(automationDetails)
      .withVersionControlProvenance(setOf(vcsDetails))
      .withResults(listOf(result))

    return SarifReport(SarifReport.Version._2_1_0, listOf(run))
  }
}