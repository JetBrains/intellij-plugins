package org.jetbrains.qodana.protocol

import com.intellij.openapi.project.Project
import com.jetbrains.qodana.sarif.model.ArtifactLocation
import com.jetbrains.qodana.sarif.model.Location
import com.jetbrains.qodana.sarif.model.Message
import com.jetbrains.qodana.sarif.model.MultiformatMessageString
import com.jetbrains.qodana.sarif.model.OriginalUriBaseIds
import com.jetbrains.qodana.sarif.model.PhysicalLocation
import com.jetbrains.qodana.sarif.model.PropertyBag
import com.jetbrains.qodana.sarif.model.Region
import com.jetbrains.qodana.sarif.model.ReportingDescriptor
import com.jetbrains.qodana.sarif.model.ReportingDescriptorReference
import com.jetbrains.qodana.sarif.model.ReportingDescriptorRelationship
import com.jetbrains.qodana.sarif.model.Result
import com.jetbrains.qodana.sarif.model.Run
import com.jetbrains.qodana.sarif.model.RunAutomationDetails
import com.jetbrains.qodana.sarif.model.SarifReport
import com.jetbrains.qodana.sarif.model.Tool
import com.jetbrains.qodana.sarif.model.ToolComponent
import com.jetbrains.qodana.sarif.model.VersionControlDetails
import org.jetbrains.qodana.report.ReportDescriptorBuilder
import org.jetbrains.qodana.report.ReportResult
import org.jetbrains.qodana.report.ReportValidator
import org.jetbrains.qodana.staticAnalysis.sarif.OriginalUriBaseId
import org.jetbrains.qodana.staticAnalysis.sarif.QODANA_SEVERITY_KEY
import java.net.URI
import java.util.UUID

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
              ArtifactLocation().withUri(parameters.path).withUriBaseId(OriginalUriBaseId.SRCROOT.uriBaseId)
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
    
    val originalUriBaseIds = parameters.projectDirBaseURI?.let { projectDirBaseURI ->
      OriginalUriBaseIds().apply {
        put(
          OriginalUriBaseId.SRCROOT.uriBaseId,
          ArtifactLocation()
            .withUri(projectDirBaseURI)
            .withUriBaseId(OriginalUriBaseId.PROJECTROOT.uriBaseId)
            .withDescription(Message().withText(OriginalUriBaseId.SRCROOT.description))
        )
        put(
          OriginalUriBaseId.PROJECTROOT.uriBaseId,
          ArtifactLocation().withDescription(Message().withText(OriginalUriBaseId.PROJECTROOT.description))
        )
      }
    } ?: OriginalUriBaseIds().apply {
      put(
        OriginalUriBaseId.SRCROOT.uriBaseId,
        ArtifactLocation().withDescription(Message().withText(OriginalUriBaseId.SRCROOT.description))
      )
    }

    val run = Run(tool)
      .withAutomationDetails(automationDetails)
      .withVersionControlProvenance(setOf(vcsDetails))
      .withResults(listOf(result))
      .withOriginalUriBaseIds(originalUriBaseIds)

    return SarifReport(SarifReport.Version._2_1_0, listOf(run))
  }
}