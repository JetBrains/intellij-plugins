package org.jetbrains.qodana.staticAnalysis.inspections.runner.globalOutput

import com.intellij.codeInspection.ex.JsonInspectionsReportConverter
import com.intellij.openapi.components.PathMacroManager
import com.intellij.openapi.diagnostic.logger
import com.intellij.openapi.diagnostic.thisLogger
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.io.FileUtil
import com.intellij.openapi.vfs.VfsUtil
import com.intellij.openapi.vfs.VirtualFileManager
import com.jetbrains.qodana.sarif.SarifUtil
import com.jetbrains.qodana.sarif.model.Location
import com.jetbrains.qodana.sarif.model.Message
import com.jetbrains.qodana.sarif.model.PropertyBag
import com.jetbrains.qodana.sarif.model.Result
import org.jdom.Element
import org.jetbrains.qodana.staticAnalysis.inspections.runner.Problem
import org.jetbrains.qodana.staticAnalysis.inspections.runner.ProblemType
import org.jetbrains.qodana.staticAnalysis.inspections.runner.QodanaToolResultDatabase
import org.jetbrains.qodana.staticAnalysis.inspections.runner.globalOutput.GlobalOutputConsumer.Companion.consumeOutputXmlFile
import org.jetbrains.qodana.staticAnalysis.profile.QodanaProfile
import org.jetbrains.qodana.staticAnalysis.sarif.*
import org.jetbrains.qodana.staticAnalysis.sarif.fingerprints.BaselineEqualityV1
import org.jetbrains.qodana.staticAnalysis.sarif.fingerprints.fingerprintOf
import org.jetbrains.qodana.staticAnalysis.sarif.fingerprints.withPartialFingerprints
import java.nio.file.Path

private val LOG = logger<DuplicateCodeConsumer>()
private val gson = SarifUtil.createGson()

/**
 * Responsible for handling DuplicatedCode.xml and DuplicatedCode_aggregate.xml files as results of DuplicatedCode inspection.
 */
class DuplicateCodeConsumer: GlobalOutputConsumer {
  private val inspectionName = "DuplicatedCode"

  override suspend fun consumeOwnedFiles(
    profileState: QodanaProfile.QodanaProfileState,
    paths: List<Path>,
    database: QodanaToolResultDatabase,
    project: Project,
    consumer: (List<Problem>, String) -> Unit
  ) {
    val macroManager = PathMacroManager.getInstance(project)
    if (!GlobalOutputConsumer.reportingInspectionAllowed(profileState, inspectionName) || paths.size != 2) return
    consumeOutputXmlFile(paths.first()) { _, root ->
      consumeDuplicatedCodeXml(root, database, macroManager)
    }
    consumeOutputXmlFile(paths.last()) { _, root ->
      consumeDuplicatedCodeAggregateXml(root, consumer)
    }
  }

  override fun ownedFiles(paths: List<Path>): List<Path> {
    val duplicatedCode = paths.singleOrNull { FileUtil.getNameWithoutExtension(it.toFile()) == JsonInspectionsReportConverter.DUPLICATED_CODE }
    val duplicatedCodeAggregate = paths.singleOrNull { FileUtil.getNameWithoutExtension(it.toFile()) == JsonInspectionsReportConverter.DUPLICATED_CODE_AGGREGATE }
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

  private fun consumeDuplicatedCodeAggregateXml(root: Element, consumer: (List<Problem>, String) -> Unit) {
    consumer(root.getChildren("duplicate").map { DuplicatesProblem(it) }, "DuplicatedCode")
  }

  private fun findOffset(macroManager: PathMacroManager, file: String, descriptor: CommonDescriptor): Int? {
    val virtualFile = VirtualFileManager.getInstance().findFileByUrl(macroManager.expandPath(file))
    if (virtualFile == null || virtualFile.isDirectory) return null
    val text = VfsUtil.loadText(virtualFile)
    return getProblemOffset(text, descriptor)
  }
}

private class DuplicatesProblem(val element: Element): Problem {
  override suspend fun getSarif(macroManager: PathMacroManager, database: QodanaToolResultDatabase): Result? {
    macroManager.collapsePathsRecursively(element)
    var mainResult: Result? = null
    val locs = mutableListOf<Location>()
    for (fragment in element.getChildren("fragment")) {
      val file = fragment.getAttributeValue(ElementToSarifConverter.FILE)
      val line = Integer.parseInt(fragment.getAttributeValue(ElementToSarifConverter.LINE))
      val start = Integer.parseInt(fragment.getAttributeValue("start"))
      val end = Integer.parseInt(fragment.getAttributeValue("end"))
      val length = end - start
      database.selectDuplicate(file, line, start).use { query ->
        val jsons = query.executeQuery().toList()
        val json = jsons.firstOrNull()

        val problemLocation = "$file:$line:$start"
        if (json == null) {
          thisLogger().warn("Can't find duplicate problem in db, $problemLocation")
          return null
        }
        if (jsons.size > 1) {
          thisLogger().warn("${jsons.size} duplicates of duplicate problem found, $problemLocation")
        }

        val result = gson.fromJson(json, Result::class.java)
        mainResult = mainResult ?: result
        val location = result.locations.getOrNull(0) ?: return null
        locs.add(
          location.withPhysicalLocation(
            getPhysicalLocation(
              CommonDescriptor(
                file,
                line,
                location.physicalLocation.region.startColumn - 1,
                length,
                location.physicalLocation.contextRegion.snippet.text,
                location.physicalLocation.region.sourceLanguage),
              macroManager,
              0)
          ))
      }
    }
    val sarif = mainResult ?: return null
    return sarif
      .withLocations(locs)
      .apply {
        properties = (properties ?: PropertyBag()).apply {
          this[PROBLEM_TYPE] = ProblemType.DUPLICATES
        }
      }
      .withPartialFingerprints()
  }

  override fun getFile(): String? = null

  override fun getModule(): String? = null
}
