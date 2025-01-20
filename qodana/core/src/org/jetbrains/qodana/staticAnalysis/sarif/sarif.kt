package org.jetbrains.qodana.staticAnalysis.sarif

import com.intellij.codeInspection.ex.CodeQualityCategories
import com.intellij.codeInspection.ex.InspectionMetaInformationService
import com.intellij.codeInspection.ex.ToolsImpl
import com.intellij.internal.statistic.eventLog.EventLogConfiguration
import com.intellij.lang.LanguageUtil
import com.intellij.lang.annotation.HighlightSeverity
import com.intellij.openapi.application.ApplicationInfo
import com.intellij.openapi.components.PathMacroManager
import com.intellij.openapi.components.service
import com.intellij.openapi.diagnostic.logger
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VfsUtil
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.openapi.vfs.VirtualFileManager
import com.intellij.util.io.createDirectories
import com.jetbrains.qodana.sarif.SarifUtil
import com.jetbrains.qodana.sarif.model.*
import com.jetbrains.qodana.sarif.model.Level.*
import kotlinx.coroutines.runInterruptible
import kotlinx.coroutines.withContext
import org.jetbrains.qodana.staticAnalysis.StaticAnalysisDispatchers
import org.jetbrains.qodana.staticAnalysis.inspections.runner.getQodanaProductName
import org.jetbrains.qodana.staticAnalysis.inspections.runner.startup.LoadedProfile
import org.jetbrains.qodana.staticAnalysis.profile.QodanaProfile
import org.jetbrains.qodana.staticAnalysis.qodanaEnv
import org.jetbrains.qodana.staticAnalysis.sarif.textFormat.htmlToMarkdown
import org.jetbrains.qodana.staticAnalysis.sarif.textFormat.htmlToPlainText
import org.jetbrains.qodana.staticAnalysis.script.AnalysisKind
import org.jetbrains.qodana.staticAnalysis.stat.UsageCollector.profileForReporting
import java.io.IOException
import java.net.URI
import java.nio.file.Path
import java.text.SimpleDateFormat
import java.time.Instant
import java.util.*
import kotlin.math.max

private val LOG = logger<SarifReport>()

const val SARIF_AUTOMATION_GUID_PROPERTY = "qodana.automation.guid"

private const val PROJECT_DIR_PREFIX = "file://\$PROJECT_DIR\$/"
internal const val SRCROOT_URI_BASE = "SRCROOT"

/** The CI job build URL where Qodana is run, e.g. `https://github.com/JetBrains/qodana-action/actions/runs/1`. */
internal const val QODANA_JOB_URL = "QODANA_JOB_URL"
internal const val SUPPRESS_TOOL_ID_PARAMETER = "suppressToolId"
internal const val CWE_IDS_PARAMETER = "cweIds"
internal const val INSPECTION_ASPECTS_PARAMETER = "codeQualityCategory"

internal const val REGION_LINES_COUNT_IF_PROBLEM_IS_FILE = 2

internal const val QODANA_SEVERITY_KEY = "qodanaSeverity"

internal const val QODANA_NOTIFICATION_KIND = "qodanaKind"
internal const val QODANA_NEW_RESULT_SUMMARY = "qodanaNewResultSummary"
internal const val QODANA_FAILURE_CONDITIONS = "qodanaFailureConditions"

internal const val RELATED_PROBLEMS_CHILD_HASH_PROP = "relatedProblemsChildHash"
internal const val RELATED_PROBLEMS_ROOT_HASH_PROP = "relatedProblemsRootHash"

internal const val PROBLEM_TYPE = "problemType"

fun createSarifReport(runs: List<Run>): SarifReport {
  val schema = URI("https://raw.githubusercontent.com/schemastore/schemastore/master/src/schemas/json/sarif-2.1.0-rtm.5.json")
  return SarifReport(SarifReport.Version._2_1_0, runs).`with$schema`(schema)
}

fun createRun(): Run {
  val driver = createDriver()
  val tool = Tool(driver)

  return Run(tool)
    .withInvocations(
      listOf(Invocation(true).withStartTimeUtc(Instant.now()))
    )
    .withProperties(PropertyBag().apply {
      put("deviceId", EventLogConfiguration.getInstance().getOrCreate("FUS").deviceId)
    })
}

internal fun automationDetails(project: Project, analysisKind: AnalysisKind) =
  RunAutomationDetails()
    .withGuid(System.getProperty(SARIF_AUTOMATION_GUID_PROPERTY) ?: UUID.randomUUID().toString())
    .withId(buildReportId(project))
    .withProperties(
      PropertyBag().also {
        it["jobUrl"] = qodanaEnv().QODANA_JOB_URL.value ?: ""
        it["analysisKind"] = analysisKind.stringPresentation
      }
    )

internal fun configProfile(profile: LoadedProfile) =
  "configProfile" to profileForReporting(profile.nameForReporting, profile.pathForReporting)

private fun createDriver(): ToolComponent {
  val appInfo = ApplicationInfo.getInstance()

  val productCode = ApplicationInfo.getInstance().build.productCode
  return ToolComponent(productCode)
    .withFullName(getQodanaProductName())
    .withVersion(appInfo.build.asStringWithoutProductCode())
    .withRules(mutableListOf())
}


private fun buildReportId(project: Project): String {
  qodanaEnv().QODANA_REPORT_ID.value?.let { return it }
  val projectId = qodanaEnv().QODANA_PROJECT_ID.value ?: project.name
  val date = SimpleDateFormat("yyyy-MM-dd").format(Date())
  val tool = "qodana"

  return "$projectId/$tool/$date"
}

suspend fun fillComponents(tool: Tool, qodanaProfile: QodanaProfile) {
  val driver = tool.driver ?: createDriver()
  val taxonomy = InspectionsTaxonomy()
  val components = mutableMapOf<String, ToolComponent>()
  qodanaProfile.mainGroup.profile.tools.forEach { tools ->
    val defaultToolWrapper = tools.getInspectionTool(null)
    val pluginId = defaultToolWrapper.extension?.pluginDescriptor?.pluginId?.idString
    val pluginVersion = defaultToolWrapper.extension?.pluginDescriptor?.version
    val component = if (pluginId != null) {
      components.computeIfAbsent(pluginId) { ToolComponent(pluginId).withVersion(pluginVersion).withRules(mutableListOf()) }
    }
    else {
      driver
    }
    val (index, descriptor) = taxonomy.addTool(defaultToolWrapper)
    component.rules.add(createRule(tools, index, descriptor.id))
  }
  driver.taxa = taxonomy.taxonomy
  tool.driver = driver
  tool.extensions = components.values.toSet()
}

fun createTaxonomyReference(taxonomyIndex: Int, taxonomyId: String): ReportingDescriptorRelationship {
  val productCode = ApplicationInfo.getInstance().build.productCode
  return ReportingDescriptorRelationship(
    ReportingDescriptorReference()
      .withToolComponent(ToolComponentReference().withName(productCode))
      .withId(taxonomyId)
      .withIndex(taxonomyIndex)
  )
    .withKinds(setOf("superset"))
}

suspend fun createRule(tools: ToolsImpl, taxonomyIndex: Int, taxonomyId: String): ReportingDescriptor {
  val defaultToolWrapper = tools.getInspectionTool(null)

  val ideaSeverity = tools.level.severity
  val sarifLevel = tools.level.severity.toLevel()
  val defaultConfiguration = ReportingConfiguration()
    .withEnabled(tools.isEnabled && tools.tools.any { it.isEnabled })
    .withLevel(sarifLevel)
    .withParameters(
      PropertyBag()
        .withIdeaAndQodanaSeverities(ideaSeverity.name, sarifLevel)
        .withSuppressToolId(defaultToolWrapper.tool.suppressId)
        .addCWEId(defaultToolWrapper.tool.shortName)
        .addCodeQualityCategory(defaultToolWrapper.tool.shortName)
    )

  val reportingDescriptorRelationship = createTaxonomyReference(taxonomyIndex, taxonomyId)

  val description = defaultToolWrapper.loadDescription() ?: run {
    LOG.warn("Missing description for tool ${defaultToolWrapper.id} in taxonomy ${taxonomyId}")
    "No description available"
  }

  return ReportingDescriptor(tools.shortName)
    .withShortDescription(MultiformatMessageString().withText(defaultToolWrapper.displayName))
    .withFullDescription(
      MultiformatMessageString()
        .withText(htmlToPlainText(description))
        .withMarkdown(htmlToMarkdown(description))
    )
    .withRelationships(setOf(reportingDescriptorRelationship))
    .withDefaultConfiguration(defaultConfiguration)
}

internal fun PropertyBag.withIdeaAndQodanaSeverities(ideaSeverity: String?, sarifLevel: Level): PropertyBag {
  this["ideaSeverity"] = ideaSeverity
  this[QODANA_SEVERITY_KEY] = ideaSeverity?.let {
    QodanaSeverity.fromIdeaSeverity(it).toString()
  } ?: QodanaSeverity.fromSarifLevel(sarifLevel).toString()
  return this
}

internal fun PropertyBag.withSuppressToolId(suppressToolId: String?): PropertyBag {
  this[SUPPRESS_TOOL_ID_PARAMETER] = suppressToolId
  return this
}

internal suspend fun PropertyBag.addCWEId(inspectionId: String): PropertyBag {
  val metaInformation = service<InspectionMetaInformationService>().getState().inspections[inspectionId]
  if (metaInformation?.cweIds != null) {
    this[CWE_IDS_PARAMETER] = metaInformation.cweIds
  }
  return this
}

internal suspend fun PropertyBag.addCodeQualityCategory(inspectionId: String): PropertyBag {
  val metaInformation = service<InspectionMetaInformationService>().getState().inspections[inspectionId]
  val codeQualityCategory = metaInformation?.codeQualityCategory ?: listOf(CodeQualityCategories.UNSPECIFIED.id)
  val allowedAspects = CodeQualityCategories.entries.map { it.id }
  this[INSPECTION_ASPECTS_PARAMETER] = if (codeQualityCategory in allowedAspects) codeQualityCategory else CodeQualityCategories.UNSPECIFIED.id
  return this
}

internal val Result.qodanaSeverity: QodanaSeverity
  get() = (properties?.get(QODANA_SEVERITY_KEY) as? String)?.let { prop -> QodanaSeverity.entries.firstOrNull { it.toString() == prop } }
          ?: (properties?.get("ideaSeverity") as? String)?.let(QodanaSeverity::fromIdeaSeverity)
          ?: QodanaSeverity.fromSarifLevel(level)

internal fun HighlightSeverity.toLevel(): Level {
  return when (this) {
    HighlightSeverity.ERROR -> ERROR
    HighlightSeverity.WARNING -> WARNING
    HighlightSeverity.WEAK_WARNING -> NOTE
    else -> NOTE
  }
}

internal fun Level.toSeverity(): HighlightSeverity {
  return when (this) {
    ERROR -> HighlightSeverity.ERROR
    WARNING -> HighlightSeverity.WARNING
    else -> HighlightSeverity.WEAK_WARNING
  }
}

internal suspend fun writeReport(path: Path, report: SarifReport) {
  try {
    runInterruptible(StaticAnalysisDispatchers.IO) {
      path.parent.createDirectories()
      SarifUtil.writeReport(path, report)
    }
  }
  catch (e: IOException) {
    LOG.error("Writing sarif report error. Path: $path", e)
  }
}

internal suspend fun getPhysicalLocation(problem: CommonDescriptor,
                                 macroManager: PathMacroManager,
                                 linesMargin: Int
): PhysicalLocation {
  val artifactLocation = getArtifactLocation(problem.file)
  val physicalLocation = PhysicalLocation().withArtifactLocation(artifactLocation)

  val (text, virtualFile) = withContext(StaticAnalysisDispatchers.IO) {
    val virtualFile = VirtualFileManager.getInstance().findFileByUrl(macroManager.expandPath(problem.file))
    val text = loadTextFromVirtualFile(virtualFile)
    text to virtualFile
  }
  val fileLanguage = LanguageUtil.getFileLanguage(virtualFile)
  if (text != null) {
    // the problem is a "whole file problem" in two cases:
    // the whole file reported or 0 range in start of the file
    // if the whole file is reported as a problem, do not put any region/context region
    // for "whole file" problems don't provide the code region
    val problemIsFile = problem.line == 1 && problem.column == 0 &&
                        (problem.length == 0 || problem.length == text.length)
    if (!problemIsFile) {
      physicalLocation
        .withRegion(getRegion(problem, text))
        .withContextRegion(getContextRegion(problem, text, linesMargin, fileLanguage))
    }
  }
  return physicalLocation
}

fun getArtifactLocation(fileUrl: String): ArtifactLocation? {
  return if (fileUrl.startsWith(PROJECT_DIR_PREFIX)) {
    ArtifactLocation().withUri(fileUrl.removePrefix(PROJECT_DIR_PREFIX)).withUriBaseId(SRCROOT_URI_BASE)
  }
  else {
    ArtifactLocation().withUri(fileUrl)
  }
}

internal fun getRegion(problem: CommonDescriptor, text: String) = getRegionByOffset(problem, text, getProblemOffset(text, problem))

internal fun getRegionByOffset(problem: CommonDescriptor, text: String, offset: Int?): Region? {
  if (offset == null || problem.line == null) return null

  val problemLength = if (problem.length != null) {
    val nonZeroLength = max(1, problem.length) // Why? - in case of empty range the editor artificially sets the range to 1, so we should do it too, see QD-8624
    if (text.length >= offset + nonZeroLength) nonZeroLength else null
  } else {
    null
  }

  val snippet = problemLength?.let { text.substring(offset, offset + problemLength) } ?: problem.highlightedElement

  return Region()
    .withCharOffset(offset)
    .withStartColumn(problem.column?.let { it + 1 })
    .withCharLength(problemLength)
    .withStartLine(problem.line)
    .withSourceLanguage(problem.language)
    .withSnippet(ArtifactContent().withText(snippet))
}


fun loadTextFromVirtualFile(virtualFile: VirtualFile?): String? {
  return if (virtualFile != null && !virtualFile.isDirectory) VfsUtil.loadText(virtualFile) else null
}

internal fun Result.getOrAssignProperties(): PropertyBag {
  if (properties == null) {
    properties = PropertyBag()

  }
  return properties!!
}

internal fun Run.getOrAssignProperties(): PropertyBag {
  if (properties == null) {
    properties = PropertyBag()

  }
  return properties!!
}

internal var Notification.qodanaKind
  get() = properties?.get(QODANA_NOTIFICATION_KIND) as String?
  set(value) {
    val props = properties ?: PropertyBag()
    props[QODANA_NOTIFICATION_KIND] = value
    withProperties(props)
  }

internal fun Notification.withKind(kind: String) = apply { qodanaKind = kind }

@Suppress("UNCHECKED_CAST")
internal var Run.resultSummary: Map<String, Int>?
  get() = (properties?.get(QODANA_NEW_RESULT_SUMMARY) as Map<String, Number>?)?.mapValues { (_, v) -> v.toInt() }
  set(value) {
    val props = properties ?: PropertyBag()
    props[QODANA_NEW_RESULT_SUMMARY] = value
    withProperties(props)
  }
