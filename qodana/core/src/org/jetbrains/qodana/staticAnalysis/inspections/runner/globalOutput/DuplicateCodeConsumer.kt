package org.jetbrains.qodana.staticAnalysis.inspections.runner.globalOutput

import com.intellij.codeInspection.ex.JsonInspectionsReportConverter
import com.intellij.openapi.components.PathMacroManager
import com.intellij.openapi.components.service
import com.intellij.openapi.diagnostic.logger
import com.intellij.openapi.diagnostic.thisLogger
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.io.FileUtil
import com.intellij.openapi.util.text.StringUtil
import com.intellij.openapi.vfs.VfsUtil
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.openapi.vfs.VirtualFileManager
import com.intellij.openapi.vfs.toNioPathOrNull
import com.jetbrains.qodana.sarif.SarifUtil
import com.jetbrains.qodana.sarif.model.ArtifactLocation
import com.jetbrains.qodana.sarif.model.Location
import com.jetbrains.qodana.sarif.model.Message
import com.jetbrains.qodana.sarif.model.Notification
import com.jetbrains.qodana.sarif.model.PhysicalLocation
import com.jetbrains.qodana.sarif.model.PropertyBag
import com.jetbrains.qodana.sarif.model.Result
import org.jdom.Element
import org.jetbrains.qodana.QodanaBundle
import org.jetbrains.qodana.staticAnalysis.inspections.runner.Problem
import org.jetbrains.qodana.staticAnalysis.inspections.runner.ProblemType
import org.jetbrains.qodana.staticAnalysis.inspections.runner.QodanaToolResultDatabase
import org.jetbrains.qodana.staticAnalysis.inspections.runner.globalOutput.GlobalOutputConsumer.Companion.consumeOutputXmlFile
import org.jetbrains.qodana.staticAnalysis.profile.QodanaProfile
import org.jetbrains.qodana.staticAnalysis.sarif.CommonDescriptor
import org.jetbrains.qodana.staticAnalysis.sarif.ElementToSarifConverter
import org.jetbrains.qodana.staticAnalysis.sarif.PROBLEM_TYPE
import org.jetbrains.qodana.staticAnalysis.sarif.fingerprints.BaselineEqualityV1
import org.jetbrains.qodana.staticAnalysis.sarif.fingerprints.fingerprintOf
import org.jetbrains.qodana.staticAnalysis.sarif.fingerprints.withPartialFingerprints
import org.jetbrains.qodana.staticAnalysis.sarif.getPhysicalLocation
import org.jetbrains.qodana.staticAnalysis.sarif.getProblemOffset
import org.jetbrains.qodana.staticAnalysis.sarif.notifications.RuntimeNotificationCollector
import org.jetbrains.qodana.staticAnalysis.sarif.notifications.ToolErrorInspectListener
import org.jetbrains.qodana.staticAnalysis.sarif.withKind
import java.nio.file.Path
import java.time.Instant

private val LOG = logger<DuplicateCodeConsumer>()
private val gson = SarifUtil.createGson()

private const val INSPECTION_NAME = "DuplicatedCode"

/**
 * Responsible for handling DuplicatedCode.xml and DuplicatedCode_aggregate.xml files as results of DuplicatedCode inspection.
 */
class DuplicateCodeConsumer : GlobalOutputConsumer {
  override suspend fun consumeOwnedFiles(
    profileState: QodanaProfile.QodanaProfileState,
    paths: List<Path>,
    database: QodanaToolResultDatabase,
    project: Project,
    consumer: (List<Problem>, String) -> Unit,
  ) {
    val macroManager = PathMacroManager.getInstance(project)
    if (!GlobalOutputConsumer.reportingInspectionAllowed(profileState, INSPECTION_NAME) || paths.size != 2) return
    consumeOutputXmlFile(paths.first()) { _, root ->
      consumeDuplicatedCodeXml(root, database, macroManager)
    }
    consumeOutputXmlFile(paths.last()) { _, root ->
      consumeDuplicatedCodeAggregateXml(root, project, consumer)
    }
  }

  override fun ownedFiles(paths: List<Path>): List<Path> {
    val duplicatedCode =
      paths.singleOrNull { FileUtil.getNameWithoutExtension(it.toFile()) == JsonInspectionsReportConverter.DUPLICATED_CODE }
    val duplicatedCodeAggregate =
      paths.singleOrNull { FileUtil.getNameWithoutExtension(it.toFile()) == JsonInspectionsReportConverter.DUPLICATED_CODE_AGGREGATE }
    return listOfNotNull(duplicatedCode, duplicatedCodeAggregate)
  }

  private suspend fun consumeDuplicatedCodeXml(root: Element, database: QodanaToolResultDatabase, macroManager: PathMacroManager) {
    val message = Message().withText("Duplicated code").withMarkdown("Duplicated code")
    for (problem in root.getChildren("problem")) {
      try {
        macroManager.collapsePathsRecursively(problem)
        val sarif = ElementToSarifConverter.convertFromXmlFormat(problem, macroManager, 0, message) // no fixes here
        val problemLocation = ElementToSarifConverter.commonDescriptor(problem)
        val print = requireNotNull(sarif.fingerprintOf(BaselineEqualityV1)) { "Fingerprints not generated" }
        database.insertDuplicate(problemLocation.file,
                                 problemLocation.line ?: 0,
                                 findOffset(macroManager, problemLocation.file, problemLocation) ?: 0,
                                 0,
                                 print,
                                 gson.toJson(sarif, Result::class.java))
      }
      catch (e: Exception) {
        LOG.warn(e)
      }
    }
  }

  private fun consumeDuplicatedCodeAggregateXml(root: Element, project: Project, consumer: (List<Problem>, String) -> Unit) {
    consumer(root.getChildren("duplicate").map { DuplicatesProblem(project, it) }, INSPECTION_NAME)
  }

  private fun findOffset(macroManager: PathMacroManager, file: String, descriptor: CommonDescriptor): Int? {
    val text = loadText(macroManager, file) ?: return null
    return getProblemOffset(text, descriptor)
  }
}

private fun loadText(macroManager: PathMacroManager, file: String): String? {
  val virtualFile = findSourceFile(macroManager, file) ?: return null
  return VfsUtil.loadText(virtualFile)
}

private fun findSourceFile(macroManager: PathMacroManager, file: String): VirtualFile? =
  VirtualFileManager.getInstance().findFileByUrl(macroManager.expandPathNonNull(file))?.takeUnless { it.isDirectory }

private class DuplicatesProblem(private val project: Project, private val element: Element) : Problem {
  override suspend fun getSarif(macroManager: PathMacroManager, database: QodanaToolResultDatabase): Result? {
    macroManager.collapsePathsRecursively(element)

    var clusterJson: String? = null
    val locations = mutableListOf<Location>()
    for (fragment in element.getChildren("fragment")) {
      val file = fragment.getAttributeValue(ElementToSarifConverter.FILE)
      val line = Integer.parseInt(fragment.getAttributeValue(ElementToSarifConverter.LINE))
      val start = Integer.parseInt(fragment.getAttributeValue("start"))
      val end = Integer.parseInt(fragment.getAttributeValue("end"))

      // A file without any row is a file that the inspection never analyzed.
      val json = selectJson(database, file, line, start) ?: selectFileJson(database, file) ?: continue
      if (clusterJson == null) clusterJson = json

      val location = createLocation(macroManager, json, file, start, end - start)
      if (location == null) {
        reportLostFragment(macroManager, file, line, start)
        continue
      }
      locations.add(location)
    }

    // A cluster needs two members to be a duplicate.
    if (clusterJson == null || locations.size < 2) return null

    return gson.fromJson(clusterJson, Result::class.java)
      .withLocations(locations)
      .apply {
        properties = (properties ?: PropertyBag()).apply {
          this[PROBLEM_TYPE] = ProblemType.DUPLICATES
        }
      }
      .withPartialFingerprints()
  }

  override fun getFile(): String? = null

  override fun getModule(): String? = null

  private fun selectJson(database: QodanaToolResultDatabase, file: String, line: Int, start: Int): String? {
    database.selectDuplicate(file, line, start).use { query ->
      val jsons = query.executeQuery().toList()
      if (jsons.size > 1) {
        thisLogger().warn("${jsons.size} duplicates of duplicate problem found, $file:$line:$start")
      }
      return jsons.firstOrNull()
    }
  }

  private fun selectFileJson(database: QodanaToolResultDatabase, file: String): String? {
    database.selectDuplicateInFile(file).use { query ->
      return query.executeQuery().firstOrNull()
    }
  }

  /** The aggregate report holds the authoritative character offset, so the line and the column come from it. */
  private suspend fun createLocation(
    macroManager: PathMacroManager,
    json: String,
    file: String,
    start: Int,
    length: Int,
  ): Location? {
    val location = gson.fromJson(json, Result::class.java).locations?.getOrNull(0) ?: return null
    val text = loadText(macroManager, file) ?: return null
    if (start < 0 || start > text.length) return null
    val lineColumn = StringUtil.offsetToLineColumn(text, start) ?: return null

    val descriptor = CommonDescriptor(
      file,
      lineColumn.line + 1,
      lineColumn.column,
      length,
      location.physicalLocation?.contextRegion?.snippet?.text,
      location.physicalLocation?.region?.sourceLanguage,
    )
    return location.withPhysicalLocation(getPhysicalLocation(descriptor, macroManager, 0))
  }

  private fun reportLostFragment(macroManager: PathMacroManager, file: String, line: Int, start: Int) {
    thisLogger().warn("Can't find duplicate problem in db, $file:$line:$start")
    val location = findSourceFile(macroManager, file)?.toNioPathOrNull()?.toString()
      ?.let(ArtifactLocation()::withUri)
      ?.let(PhysicalLocation()::withArtifactLocation)
      ?.let(Location()::withPhysicalLocation)
    val notification = Notification()
      .withMessage(Message().withText(QodanaBundle.message("notification.duplicates.fragment.not.restored")))
      .withLocations(setOfNotNull(location))
      .withTimeUtc(Instant.now())
      .withLevel(Notification.Level.ERROR)
      .withProperties(PropertyBag().apply { put(ToolErrorInspectListener.TOOL_ID, INSPECTION_NAME) })
      .withKind(ToolErrorInspectListener.TOOL_ERROR_NOTIFICATION)
    project.service<RuntimeNotificationCollector>().add(notification)
  }
}
