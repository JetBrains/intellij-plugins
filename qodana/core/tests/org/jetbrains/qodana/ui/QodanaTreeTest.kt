package org.jetbrains.qodana.ui

import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.openapi.vfs.refreshAndFindVirtualFile
import com.jetbrains.qodana.sarif.SarifUtil
import com.jetbrains.qodana.sarif.model.SarifReport
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.emptyFlow
import org.assertj.core.api.Assertions.assertThat
import org.jetbrains.qodana.QodanaPluginLightTestBase
import org.jetbrains.qodana.highlight.HighlightedReportData
import org.jetbrains.qodana.highlight.HighlightedReportDataImpl
import org.jetbrains.qodana.problem.SarifProblemProperties
import org.jetbrains.qodana.problem.SarifProblemWithProperties
import org.jetbrains.qodana.problem.SarifProblemWithPropertiesAndFile
import org.jetbrains.qodana.report.*
import org.jetbrains.qodana.runDispatchingOnUi
import org.jetbrains.qodana.settings.ConfigExcludeItem
import org.jetbrains.qodana.staticAnalysis.sarif.QodanaSeverity
import org.jetbrains.qodana.ui.problemsView.tree.model.*
import org.jetbrains.qodana.ui.problemsView.tree.model.impl.QodanaTreeRootBuilder
import org.jetbrains.qodana.ui.problemsView.viewModel.impl.QodanaTreeBuildConfiguration
import java.io.File
import java.nio.file.Path
import kotlin.io.path.Path
import kotlin.io.path.pathString

class QodanaTreeTest : QodanaPluginLightTestBase() {
  private val testDir by lazy { Path(myFixture.testDataPath, "QodanaTreeTest") }
  private val reportPath by lazy { testDir.resolve("report.sarif.json") }
  private val separator = File.separator

  override fun runInDispatchThread() = false

  fun `test build tree`() = runDispatchingOnUi {
    val treeConfiguration = QodanaTreeBuildConfiguration(
      true, true, true, true, true
    )
    val root = loadProblems(reportPath, treeConfiguration)
    assertNotNull(root)
    val nodes = root!!.getTree()

    val severityNodes = nodes.filterIsInstance<QodanaTreeSeverityNode>()
    assertThat(severityNodes.size).isEqualTo(1)
    assertThat(severityNodes[0].primaryData.qodanaSeverity).isEqualTo(QodanaSeverity.HIGH)

    val inspectionNodes = nodes.filterIsInstance<QodanaTreeInspectionNode>()
    assertThat(inspectionNodes.size).isEqualTo(2)
    assertThat(inspectionNodes.map { it.primaryData.inspectionId }.count { it == "INSPECTION_ID" }).isEqualTo(1)
    assertThat(inspectionNodes.map { it.primaryData.inspectionId }.count { it == "INSPECTION_ID2" }).isEqualTo(1)

    val directoryNodes = nodes.filterIsInstance<QodanaTreeDirectoryNode>()
    assertThat(directoryNodes.size).isEqualTo(5)
    assertThat(directoryNodes.map { it.primaryData.ownPath.pathString }.count { it == "anotherModule" }).isEqualTo(2)
    assertThat(directoryNodes.map { it.primaryData.ownPath.pathString }.count { it == "module" }).isEqualTo(1)
    assertThat(directoryNodes.map { it.primaryData.ownPath.pathString }.count { it == "inner" }).isEqualTo(1)
    assertThat(directoryNodes.map { it.primaryData.ownPath.pathString }.count { it == "module${separator}inner" }).isEqualTo(1)

    val fileNodes = nodes.filterIsInstance<QodanaTreeFileNode>()
    assertThat(fileNodes.size).isEqualTo(6)

    assertThat(fileNodes.map { it.primaryData.file.fileName.pathString }.count { it == "Main.java" }).isEqualTo(1)
    assertThat(fileNodes.map { it.primaryData.file.fileName.pathString }.count { it == "AnotherLogic.java" }).isEqualTo(2)
    assertThat(fileNodes.map { it.primaryData.file.fileName.pathString }.count { it == "Inner.java" }).isEqualTo(2)
    assertThat(fileNodes.map { it.primaryData.file.fileName.pathString }.count { it == "Logic.java" }).isEqualTo(1)

    val problemNodes = nodes.filterIsInstance<QodanaTreeProblemNode>()
    assertThat(problemNodes.size).isEqualTo(7)
    assertThat(problemNodes.filter { it.primaryData.sarifProblem.relativePathToFile.startsWith("Main.java") }.size).isEqualTo(2)
    assertThat(problemNodes.filter {
      it.primaryData.sarifProblem.relativePathToFile.startsWith("module/inner/Inner.java")
    }.size).isEqualTo(2)

    assertThat(nodes.filterIsInstance<QodanaTreeNodesWithoutModuleNode>().size).isEqualTo(2)
  }

  fun `test build tree groupBySeverity disabled`() = runDispatchingOnUi {
    val treeConfiguration = QodanaTreeBuildConfiguration(
      true, false, true, true, true
    )
    val root = loadProblems(reportPath, treeConfiguration)
    assertNotNull(root)
    val nodes = root!!.getTree()

    val severityNodes = nodes.filterIsInstance<QodanaTreeSeverityNode>()
    assertThat(severityNodes.size).isEqualTo(0)

    val inspectionNodes = nodes.filterIsInstance<QodanaTreeInspectionNode>()
    assertThat(inspectionNodes.size).isEqualTo(2)
    assertThat(inspectionNodes.map { it.primaryData.inspectionId }.count { it == "INSPECTION_ID" }).isEqualTo(1)
    assertThat(inspectionNodes.map { it.primaryData.inspectionId }.count { it == "INSPECTION_ID2" }).isEqualTo(1)

    val directoryNodes = nodes.filterIsInstance<QodanaTreeDirectoryNode>()
    assertThat(directoryNodes.size).isEqualTo(5)
    assertThat(directoryNodes.map { it.primaryData.ownPath.pathString }.count { it == "anotherModule" }).isEqualTo(2)
    assertThat(directoryNodes.map { it.primaryData.ownPath.pathString }.count { it == "module" }).isEqualTo(1)
    assertThat(directoryNodes.map { it.primaryData.ownPath.pathString }.count { it == "inner" }).isEqualTo(1)
    assertThat(directoryNodes.map { it.primaryData.ownPath.pathString }.count { it == "module${separator}inner" }).isEqualTo(1)

    val fileNodes = nodes.filterIsInstance<QodanaTreeFileNode>()
    assertThat(fileNodes.size).isEqualTo(6)
    assertThat(fileNodes.map { it.primaryData.file.fileName.pathString }.count { it == "Main.java" }).isEqualTo(1)
    assertThat(fileNodes.map { it.primaryData.file.fileName.pathString }.count { it == "AnotherLogic.java" }).isEqualTo(2)
    assertThat(fileNodes.map { it.primaryData.file.fileName.pathString }.count { it == "Inner.java" }).isEqualTo(2)
    assertThat(fileNodes.map { it.primaryData.file.fileName.pathString }.count { it == "Logic.java" }).isEqualTo(1)

    val problemNodes = nodes.filterIsInstance<QodanaTreeProblemNode>()
    assertThat(problemNodes.size).isEqualTo(7)

    assertThat(nodes.filterIsInstance<QodanaTreeNodesWithoutModuleNode>().size).isEqualTo(2)
  }

  fun `test build tree groupByInspection disabled`() = runDispatchingOnUi {
    val treeConfiguration = QodanaTreeBuildConfiguration(
      true, true, false, true, true
    )
    val root = loadProblems(reportPath, treeConfiguration)
    assertNotNull(root)
    val nodes = root!!.getTree()

    val inspectionNodes = nodes.filterIsInstance<QodanaTreeInspectionNode>()
    assertThat(inspectionNodes.size).isEqualTo(0)

    val severityNodes = nodes.filterIsInstance<QodanaTreeSeverityNode>()
    assertThat(severityNodes.size).isEqualTo(1)
    assertThat(severityNodes[0].primaryData.qodanaSeverity).isEqualTo(QodanaSeverity.HIGH)

    val directoryNodes = nodes.filterIsInstance<QodanaTreeDirectoryNode>()
    assertThat(directoryNodes.size).isEqualTo(3)
    assertThat(directoryNodes.map { it.primaryData.ownPath.pathString }.count { it == "anotherModule" }).isEqualTo(1)
    assertThat(directoryNodes.map { it.primaryData.ownPath.pathString }.count { it == "module" }).isEqualTo(1)
    assertThat(directoryNodes.map { it.primaryData.ownPath.pathString }.count { it == "inner" }).isEqualTo(1)

    val fileNodes = nodes.filterIsInstance<QodanaTreeFileNode>()
    assertThat(fileNodes.size).isEqualTo(4)
    assertThat(fileNodes.map { it.primaryData.file.fileName.pathString }.count { it == "Main.java" }).isEqualTo(1)
    assertThat(fileNodes.map { it.primaryData.file.fileName.pathString }.count { it == "AnotherLogic.java" }).isEqualTo(1)
    assertThat(fileNodes.map { it.primaryData.file.fileName.pathString }.count { it == "Logic.java" }).isEqualTo(1)
    assertThat(fileNodes.map { it.primaryData.file.fileName.pathString }.count { it == "Inner.java" }).isEqualTo(1)

    val problemNodes = nodes.filterIsInstance<QodanaTreeProblemNode>()
    assertThat(problemNodes.size).isEqualTo(7)

    assertThat(nodes.filterIsInstance<QodanaTreeNodesWithoutModuleNode>().size).isEqualTo(1)
    val b = nodes.filterIsInstance<QodanaTreeNodesWithoutModuleNode>()[0].children.nodesSequence.toList()
    assertThat(b.size).isEqualTo(3)
  }

  fun `test build tree groupByDirectory disabled`() = runDispatchingOnUi {
    val treeConfiguration = QodanaTreeBuildConfiguration(
      true, true, true, true, false
    )
    val root = loadProblems(reportPath, treeConfiguration)
    assertNotNull(root)
    val nodes = root!!.getTree()

    val directoryNodes = nodes.filterIsInstance<QodanaTreeDirectoryNode>()
    assertThat(directoryNodes.size).isEqualTo(0)

    val severityNodes = nodes.filterIsInstance<QodanaTreeSeverityNode>()
    assertThat(severityNodes.size).isEqualTo(1)
    assertThat(severityNodes[0].primaryData.qodanaSeverity).isEqualTo(QodanaSeverity.HIGH)

    val inspectionNodes = nodes.filterIsInstance<QodanaTreeInspectionNode>()
    assertThat(inspectionNodes.size).isEqualTo(2)
    assertThat(inspectionNodes.map { it.primaryData.inspectionId }.count { it == "INSPECTION_ID" }).isEqualTo(1)
    assertThat(inspectionNodes.map { it.primaryData.inspectionId }.count { it == "INSPECTION_ID2" }).isEqualTo(1)

    val fileNodes = nodes.filterIsInstance<QodanaTreeFileNode>()
    assertThat(fileNodes.size).isEqualTo(6)
    assertThat(fileNodes.map { it.primaryData.file.fileName.pathString }.count { it == "Main.java" }).isEqualTo(1)
    assertThat(fileNodes.map { it.primaryData.file.fileName.pathString }.count { it == "AnotherLogic.java" }).isEqualTo(2)
    assertThat(fileNodes.map { it.primaryData.file.fileName.pathString }.count { it == "Inner.java" }).isEqualTo(2)
    assertThat(fileNodes.map { it.primaryData.file.fileName.pathString }.count { it == "Logic.java" }).isEqualTo(1)

    val problemNodes = nodes.filterIsInstance<QodanaTreeProblemNode>()
    assertThat(problemNodes.size).isEqualTo(7)

    assertThat(nodes.filterIsInstance<QodanaTreeNodesWithoutModuleNode>().size).isEqualTo(2)
  }

  fun `test exclude path event`() = runDispatchingOnUi {
    val treeConfiguration = QodanaTreeBuildConfiguration(
      true, true, true, true, true
    )
    var root = loadProblems(reportPath, treeConfiguration)
    assertNotNull(root)
    root = root!!.processTreeEvent(
      QodanaTreeExcludeEvent(
        setOf(ConfigExcludeItem(null, "module")), project
      ),
      QodanaTreePath.Builder()
    )
    val nodes = root.getTree()

    val directoryNodes = nodes.filterIsInstance<QodanaTreeDirectoryNode>()
    assertThat(directoryNodes.filter { it.excluded }.size).isEqualTo(3)

    val fileNodes = nodes.filterIsInstance<QodanaTreeFileNode>()
    assertThat(fileNodes.filter { it.excluded }.size).isEqualTo(3)

    val problemNodes = nodes.filterIsInstance<QodanaTreeProblemNode>()
    assertThat(problemNodes.filter { it.excluded }.size).isEqualTo(3)
  }

  fun `test exclude all paths event`() = runDispatchingOnUi {
    val treeConfiguration = QodanaTreeBuildConfiguration(
      true, true, true, true, true
    )
    var root = loadProblems(reportPath, treeConfiguration)
    assertNotNull(root)
    root = root!!.processTreeEvent(
      QodanaTreeExcludeEvent(
        setOf(ConfigExcludeItem(null, "")), project
      ),
      QodanaTreePath.Builder()
    )
    val nodes = root.getTree()

    val directoryNodes = nodes.filterIsInstance<QodanaTreeDirectoryNode>()
    assertThat(directoryNodes.filter { it.excluded }.size).isEqualTo(5)

    val fileNodes = nodes.filterIsInstance<QodanaTreeFileNode>()
    assertThat(fileNodes.filter { it.excluded }.size).isEqualTo(6)

    val problemNodes = nodes.filterIsInstance<QodanaTreeProblemNode>()
    assertThat(problemNodes.filter { it.excluded }.size).isEqualTo(7)
  }

  fun `test exclude inspection event`() = runDispatchingOnUi {
    val treeConfiguration = QodanaTreeBuildConfiguration(
      true, true, true, true, true
    )
    var root = loadProblems(reportPath, treeConfiguration)
    assertNotNull(root)
    root = root!!.processTreeEvent(
      QodanaTreeExcludeEvent(
        setOf(ConfigExcludeItem("INSPECTION_ID", null)),
        project
      ),
      QodanaTreePath.Builder()
    )
    val nodes = root.getTree()

    val directoryNodes = nodes.filterIsInstance<QodanaTreeDirectoryNode>()
    assertThat(directoryNodes.filter { it.excluded }.size).isEqualTo(3)

    val fileNodes = nodes.filterIsInstance<QodanaTreeFileNode>()
    assertThat(fileNodes.filter { it.excluded }.size).isEqualTo(4)

    val problemNodes = nodes.filterIsInstance<QodanaTreeProblemNode>()
    assertThat(problemNodes.filter { it.excluded }.size).isEqualTo(5)
  }

  fun `test exclude path for inspection event`() = runDispatchingOnUi {
    val treeConfiguration = QodanaTreeBuildConfiguration(
      true, true, true, true, true
    )
    var root = loadProblems(reportPath, treeConfiguration)
    assertNotNull(root)
    root = root!!.processTreeEvent(
      QodanaTreeExcludeEvent(
        setOf(ConfigExcludeItem("INSPECTION_ID", "anotherModule")),
        project
      ),
      QodanaTreePath.Builder()
    )
    val nodes1 = root.getTree()

    val directoryNodes1 = nodes1.filterIsInstance<QodanaTreeDirectoryNode>()
    assertThat(directoryNodes1.filter { it.excluded }.size).isEqualTo(1)

    val fileNodes1 = nodes1.filterIsInstance<QodanaTreeFileNode>()
    assertThat(fileNodes1.filter { it.excluded }.size).isEqualTo(1)

    val problemNodes1 = nodes1.filterIsInstance<QodanaTreeProblemNode>()
    assertThat(problemNodes1.filter { it.excluded }.size).isEqualTo(1)

    root = root.processTreeEvent(
      QodanaTreeExcludeEvent(
        setOf(ConfigExcludeItem("INSPECTION_ID", "anotherModule"), ConfigExcludeItem("INSPECTION_ID2", "anotherModule")),
        project
      ),
      QodanaTreePath.Builder())
    val nodes2 = root.getTree()

    val directoryNodes2 = nodes2.filterIsInstance<QodanaTreeDirectoryNode>()
    assertThat(directoryNodes2.filter { it.excluded }.size).isEqualTo(2)

    val fileNodes2 = nodes2.filterIsInstance<QodanaTreeFileNode>()
    assertThat(fileNodes2.filter { it.excluded }.size).isEqualTo(2)

    val problemNodes2 = nodes2.filterIsInstance<QodanaTreeProblemNode>()
    assertThat(problemNodes2.filter { it.excluded }.size).isEqualTo(2)
  }

  fun `test exclude path for inspection event then exclude only for another inspection`() = runDispatchingOnUi {
    val treeConfiguration = QodanaTreeBuildConfiguration(
      true, true, true, true, true
    )
    var root = loadProblems(reportPath, treeConfiguration)
    assertNotNull(root)
    root = root!!.processTreeEvent(
      QodanaTreeExcludeEvent(
        setOf(ConfigExcludeItem("INSPECTION_ID", "anotherModule")),
        project
      ),
      QodanaTreePath.Builder()
    )
    val nodes1 = root.getTree()

    val directoryNodes1 = nodes1.filterIsInstance<QodanaTreeDirectoryNode>()
    assertThat(directoryNodes1.filter { it.excluded }.size).isEqualTo(1)

    val fileNodes1 = nodes1.filterIsInstance<QodanaTreeFileNode>()
    assertThat(fileNodes1.filter { it.excluded }.size).isEqualTo(1)

    val problemNodes1 = nodes1.filterIsInstance<QodanaTreeProblemNode>()
    assertThat(problemNodes1.filter { it.excluded }.size).isEqualTo(1)

    val excludedProblem1 = problemNodes1.first { it.excluded }

    root = root.processTreeEvent(
      QodanaTreeExcludeEvent(
        setOf(ConfigExcludeItem("INSPECTION_ID2", "anotherModule")),
        project
      ),
      QodanaTreePath.Builder())
    val nodes2 = root.getTree()

    val directoryNodes2 = nodes2.filterIsInstance<QodanaTreeDirectoryNode>()
    assertThat(directoryNodes2.filter { it.excluded }.size).isEqualTo(1)

    val fileNodes2 = nodes2.filterIsInstance<QodanaTreeFileNode>()
    assertThat(fileNodes2.filter { it.excluded }.size).isEqualTo(1)

    val problemNodes2 = nodes2.filterIsInstance<QodanaTreeProblemNode>()
    assertThat(problemNodes2.filter { it.excluded }.size).isEqualTo(1)

    val excludedProblem2 = problemNodes2.first { it.excluded }

    assertThat(excludedProblem1).isNotEqualTo(excludedProblem2)
  }

  fun `test delete file then restore`() = runDispatchingOnUi {
    val treeConfiguration = QodanaTreeBuildConfiguration(
      true, true, true, true, true
    )
    var root = loadProblems(reportPath, treeConfiguration)
    assertNotNull(root)

    val nodes1 = root!!.getTree()

    val fileNodes1 = nodes1.filterIsInstance<QodanaTreeFileNode>()
    assertThat(fileNodes1.map { it.primaryData.file.fileName.pathString }.count { it == "Main.java" }).isEqualTo(1)

    val problemNodes1 = nodes1.filterIsInstance<QodanaTreeProblemNode>()
    assertThat(problemNodes1.size).isEqualTo(7)

    root = root.processTreeEvent(
      QodanaTreeProblemEvent(
        problemNodes1
          .filter { it.primaryData.sarifProblem.relativePathToFile.endsWith("Main.java") }
          .map { it.primaryData.sarifProblem }
          .map {
            SarifProblemWithPropertiesAndFile(
              it,
              SarifProblemProperties(false, false, false, it.startLine!!, it.startColumn!!),
              project,
              null
            )
          }.toSet()),
      QodanaTreePath.Builder()
    )

    val intermediateNodes = root.getTree()

    val fileNodesInter = intermediateNodes.filterIsInstance<QodanaTreeFileNode>()
    assertThat(fileNodesInter.map { it.primaryData.file.fileName.pathString }.count { it == "Main.java" }).isEqualTo(0)

    val problemNodesInter = intermediateNodes.filterIsInstance<QodanaTreeProblemNode>()
    assertThat(problemNodesInter.size).isEqualTo(5)

    root = root.processTreeEvent(
      QodanaTreeProblemEvent(
        problemNodes1
          .filter { it.primaryData.sarifProblem.relativePathToFile.endsWith("Main.java") }
          .map { it.primaryData.sarifProblem }
          .map {
            SarifProblemWithPropertiesAndFile(
              it,
              SarifProblemProperties(true, false, false, it.startLine!!, it.startColumn!!),
              project,
              testDir.resolve("project/Main.java").refreshAndFindVirtualFile()
            )
          }.toSet()),
      QodanaTreePath.Builder()
    )

    val nodes2 = root.getTree()

    val fileNodes2 = nodes2.filterIsInstance<QodanaTreeFileNode>()
    assertThat(fileNodes2.map { it.primaryData.file.fileName.pathString }.count { it == "Main.java" }).isEqualTo(1)

    val problemNodes2 = nodes2.filterIsInstance<QodanaTreeProblemNode>()
    assertThat(problemNodes2.size).isEqualTo(7)
  }

  fun `test delete file in directory then restore`() = runDispatchingOnUi {
    val treeConfiguration = QodanaTreeBuildConfiguration(
      true, true, true, true, true
    )
    var root = loadProblems(reportPath, treeConfiguration)
    assertNotNull(root)

    val nodes1 = root!!.getTree()

    val directoryNodes1 = nodes1.filterIsInstance<QodanaTreeDirectoryNode>()
    assertThat(directoryNodes1.size).isEqualTo(5)
    assertThat(directoryNodes1.map { it.primaryData.ownPath.pathString }.count { it == "inner" }).isEqualTo(1)
    assertThat(directoryNodes1.map { it.primaryData.ownPath.pathString }.count { it == "module${separator}inner" }).isEqualTo(1)

    val fileNodes1 = nodes1.filterIsInstance<QodanaTreeFileNode>()
    assertThat(fileNodes1.map { it.primaryData.file.fileName.pathString }.count { it == "Logic.java" }).isEqualTo(1)

    val problemNodes1 = nodes1.filterIsInstance<QodanaTreeProblemNode>()
    assertThat(problemNodes1.size).isEqualTo(7)

    root = root.processTreeEvent(
      QodanaTreeProblemEvent(
        problemNodes1
          .filter { it.primaryData.sarifProblem.relativePathToFile.endsWith("module/Logic.java") }
          .map { it.primaryData.sarifProblem }
          .map {
            SarifProblemWithPropertiesAndFile(
              it,
              SarifProblemProperties(false, false, false, it.startLine!!, it.startColumn!!),
              project,
              null
            )
          }.toSet()),
      QodanaTreePath.Builder()
    )

    val intermediateNodes = root.getTree()

    val directoryNodesInter = intermediateNodes.filterIsInstance<QodanaTreeDirectoryNode>()
    assertThat(directoryNodesInter.size).isEqualTo(4)
    assertThat(directoryNodesInter.map { it.primaryData.ownPath.pathString }.count { it == "inner" }).isEqualTo(0)
    assertThat(directoryNodesInter.map { it.primaryData.ownPath.pathString }.count { it == "module${separator}inner" }).isEqualTo(2)

    val fileNodesInter = intermediateNodes.filterIsInstance<QodanaTreeFileNode>()
    assertThat(fileNodesInter.map { it.primaryData.file.fileName.pathString }.count { it == "Logic.java" }).isEqualTo(0)

    val problemNodesInter = intermediateNodes.filterIsInstance<QodanaTreeProblemNode>()
    assertThat(problemNodesInter.size).isEqualTo(6)

    root = root.processTreeEvent(
      QodanaTreeProblemEvent(
        problemNodes1
          .filter { it.primaryData.sarifProblem.relativePathToFile.endsWith("module/Logic.java") }
          .map { it.primaryData.sarifProblem }
          .map {
            SarifProblemWithPropertiesAndFile(
              it,
              SarifProblemProperties(true, false, false, it.startLine!!, it.startColumn!!),
              project,
              testDir.resolve("project/Main.java").refreshAndFindVirtualFile()
            )
          }.toSet()),
      QodanaTreePath.Builder()
    )

    val nodes2 = root.getTree()

    val directoryNodes2 = nodes2.filterIsInstance<QodanaTreeDirectoryNode>()
    assertThat(directoryNodes2.size).isEqualTo(5)
    assertThat(directoryNodes2.map { it.primaryData.ownPath.pathString }.count { it == "inner" }).isEqualTo(1)
    assertThat(directoryNodes2.map { it.primaryData.ownPath.pathString }.count { it == "module${separator}inner" }).isEqualTo(1)

    val fileNodes2 = nodes2.filterIsInstance<QodanaTreeFileNode>()
    assertThat(fileNodes2.map { it.primaryData.file.fileName.pathString }.count { it == "Logic.java" }).isEqualTo(1)

    val problemNodes2 = nodes2.filterIsInstance<QodanaTreeProblemNode>()
    assertThat(problemNodes2.size).isEqualTo(7)
  }

  fun `test delete inspection then restore`() = runDispatchingOnUi {
    val treeConfiguration = QodanaTreeBuildConfiguration(
      true, true, true, true, true
    )
    var root = loadProblems(reportPath, treeConfiguration)
    assertNotNull(root)

    val nodes1 = root!!.getTree()

    val inspectionNodes1 = nodes1.filterIsInstance<QodanaTreeInspectionNode>()
    assertThat(inspectionNodes1.size).isEqualTo(2)
    assertThat(inspectionNodes1.map { it.primaryData.inspectionId }.count { it == "INSPECTION_ID" }).isEqualTo(1)
    assertThat(inspectionNodes1.map { it.primaryData.inspectionId }.count { it == "INSPECTION_ID2" }).isEqualTo(1)

    val fileNodes1 = nodes1.filterIsInstance<QodanaTreeFileNode>()
    assertThat(fileNodes1.size).isEqualTo(6)

    val problemNodes1 = nodes1.filterIsInstance<QodanaTreeProblemNode>()
    assertThat(problemNodes1.size).isEqualTo(7)

    root = root.processTreeEvent(
      QodanaTreeProblemEvent(
        problemNodes1
          .filter { it.primaryData.sarifProblem.inspectionId == "INSPECTION_ID2" }
          .map { it.primaryData.sarifProblem }
          .map {
            SarifProblemWithPropertiesAndFile(
              it,
              SarifProblemProperties(false, false, false, it.startLine!!, it.startColumn!!),
              project,
              null
            )
          }.toSet()),
      QodanaTreePath.Builder()
    )

    val intermediateNodes = root.getTree()

    val inspectionNodesInter = intermediateNodes.filterIsInstance<QodanaTreeInspectionNode>()
    assertThat(inspectionNodesInter.size).isEqualTo(1)
    assertThat(inspectionNodesInter.map { it.primaryData.inspectionId }.count { it == "INSPECTION_ID" }).isEqualTo(1)
    assertThat(inspectionNodesInter.map { it.primaryData.inspectionId }.count { it == "INSPECTION_ID2" }).isEqualTo(0)

    val fileNodesInter = intermediateNodes.filterIsInstance<QodanaTreeFileNode>()
    assertThat(fileNodesInter.size).isEqualTo(4)

    val problemNodesInter = intermediateNodes.filterIsInstance<QodanaTreeProblemNode>()
    assertThat(problemNodesInter.size).isEqualTo(5)

    root = root.processTreeEvent(
      QodanaTreeProblemEvent(
        problemNodes1
          .filter {
            it.primaryData.sarifProblem.relativePathToFile.endsWith("Inner.java") && it.primaryData.sarifProblem.inspectionId == "INSPECTION_ID2"
          }
          .map { it.primaryData.sarifProblem }
          .map {
            SarifProblemWithPropertiesAndFile(
              it,
              SarifProblemProperties(true, false, false, it.startLine!!, it.startColumn!!),
              project,
              testDir.resolve("project/module/inner/Inner.java").refreshAndFindVirtualFile()
            )
          }.toSet()),
      QodanaTreePath.Builder()
    )

    root = root.processTreeEvent(
      QodanaTreeProblemEvent(
        problemNodes1
          .filter {
            it.primaryData.sarifProblem.relativePathToFile.endsWith("AnotherLogic.java") && it.primaryData.sarifProblem.inspectionId == "INSPECTION_ID2"
          }
          .map { it.primaryData.sarifProblem }
          .map {
            SarifProblemWithPropertiesAndFile(
              it,
              SarifProblemProperties(true, false, false, it.startLine!!, it.startColumn!!),
              project,
              testDir.resolve("project/anotherModule/AnotherLogic.java").refreshAndFindVirtualFile()
            )
          }.toSet()),
      QodanaTreePath.Builder()
    )

    val nodes2 = root.getTree()

    val inspectionNodes2 = nodes2.filterIsInstance<QodanaTreeInspectionNode>()
    assertThat(inspectionNodes2.size).isEqualTo(2)
    assertThat(inspectionNodes2.map { it.primaryData.inspectionId }.count { it == "INSPECTION_ID" }).isEqualTo(1)
    assertThat(inspectionNodes2.map { it.primaryData.inspectionId }.count { it == "INSPECTION_ID2" }).isEqualTo(1)

    val fileNodes2 = nodes2.filterIsInstance<QodanaTreeFileNode>()
    assertThat(fileNodes2.size).isEqualTo(6)

    val problemNodes2 = nodes2.filterIsInstance<QodanaTreeProblemNode>()
    assertThat(problemNodes2.size).isEqualTo(7)
  }

  fun `test exclude file then delete file then restore`() = runDispatchingOnUi {
    val treeConfiguration = QodanaTreeBuildConfiguration(
      true, true, true, true, true
    )
    var root = loadProblems(reportPath, treeConfiguration)
    assertNotNull(root)
    root = root!!.processTreeEvent(
      QodanaTreeExcludeEvent(
        setOf(ConfigExcludeItem(null, "Main.java")), project
      ),
      QodanaTreePath.Builder()
    )

    val nodes1 = root.getTree()

    val directoryNodes1 = nodes1.filterIsInstance<QodanaTreeDirectoryNode>()
    assertThat(directoryNodes1.filter { it.excluded }.size).isEqualTo(0)

    val fileNodes1 = nodes1.filterIsInstance<QodanaTreeFileNode>()
    assertThat(fileNodes1.filter { it.excluded }.size).isEqualTo(1)

    val problemNodes1 = nodes1.filterIsInstance<QodanaTreeProblemNode>()
    assertThat(problemNodes1.filter { it.excluded }.size).isEqualTo(2)

    val excludedProblems1 = problemNodes1.filter { it.excluded }.map { it.primaryData }.toSet()
    val excludedFiles1 = fileNodes1.filter { it.excluded }.map { it.primaryData }.toSet()

    root = root.processTreeEvent(
      QodanaTreeProblemEvent(
        problemNodes1
          .filter { it.primaryData.sarifProblem.relativePathToFile.endsWith("Main.java") }
          .map { it.primaryData.sarifProblem }
          .map {
            SarifProblemWithPropertiesAndFile(
              it,
              SarifProblemProperties(false, false, false, it.startLine!!, it.startColumn!!),
              project,
              null
            )
        }.toSet()),
        QodanaTreePath.Builder()
      )

    val intermediateNodes = root.getTree()
    assertThat(intermediateNodes.size).isLessThan(nodes1.size)

    val directoryNodesInter = intermediateNodes.filterIsInstance<QodanaTreeDirectoryNode>()
    assertThat(directoryNodesInter.filter { it.excluded }.size).isEqualTo(0)

    val fileNodesInter = intermediateNodes.filterIsInstance<QodanaTreeFileNode>()
    assertThat(fileNodesInter.filter { it.excluded }.size).isEqualTo(0)

    val problemNodesInter = intermediateNodes.filterIsInstance<QodanaTreeProblemNode>()
    assertThat(problemNodesInter.filter { it.excluded }.size).isEqualTo(0)

    root = root.processTreeEvent(
      QodanaTreeProblemEvent(
        problemNodes1
          .filter { it.primaryData.sarifProblem.relativePathToFile.endsWith("Main.java") }
          .map { it.primaryData.sarifProblem }
          .map {
            SarifProblemWithPropertiesAndFile(
              it,
              SarifProblemProperties(true, false, false, it.startLine!!, it.startColumn!!),
              project,
              testDir.resolve("project/Main.java").refreshAndFindVirtualFile()
            )
        }.toSet()),
      QodanaTreePath.Builder()
    )

    val nodes2 = root.getTree()

    val directoryNodes2 = nodes2.filterIsInstance<QodanaTreeDirectoryNode>()
    assertThat(directoryNodes2.filter { it.excluded }.size).isEqualTo(0)

    val fileNodes2 = nodes2.filterIsInstance<QodanaTreeFileNode>()
    assertThat(fileNodes2.filter { it.excluded }.size).isEqualTo(1)

    val problemNodes2 = nodes2.filterIsInstance<QodanaTreeProblemNode>()
    assertThat(problemNodes2.filter { it.excluded }.size).isEqualTo(2)

    val excludedProblems2 = problemNodes2.filter { it.excluded }.map { it.primaryData }.toSet()
    val excludedFiles2 = fileNodes2.filter { it.excluded }.map { it.primaryData }.toSet()

    assertThat(excludedProblems1).isEqualTo(excludedProblems2)
    assertThat(excludedFiles1).isEqualTo(excludedFiles2)
  }

  fun `test exclude dir then delete file then restore`() = runDispatchingOnUi {
    val treeConfiguration = QodanaTreeBuildConfiguration(
      true, true, true, true, true
    )
    var root = loadProblems(reportPath, treeConfiguration)
    assertNotNull(root)
    root = root!!.processTreeEvent(
      QodanaTreeExcludeEvent(
        setOf(ConfigExcludeItem(null, "module")), project
      ),
      QodanaTreePath.Builder()
    )

    val nodes1 = root.getTree()

    val directoryNodes1 = nodes1.filterIsInstance<QodanaTreeDirectoryNode>()
    assertThat(directoryNodes1.filter { it.excluded }.size).isEqualTo(3)

    val fileNodes1 = nodes1.filterIsInstance<QodanaTreeFileNode>()
    assertThat(fileNodes1.filter { it.excluded }.size).isEqualTo(3)

    val problemNodes1 = nodes1.filterIsInstance<QodanaTreeProblemNode>()
    assertThat(problemNodes1.filter { it.excluded }.size).isEqualTo(3)

    val excludedProblems1 = problemNodes1.filter { it.excluded }.map { it.primaryData }.toSet()
    val excludedFiles1 = fileNodes1.filter { it.excluded }.map { it.primaryData }.toSet()

    root = root.processTreeEvent(
      QodanaTreeProblemEvent(
        problemNodes1
          .filter { it.primaryData.sarifProblem.relativePathToFile.endsWith("Inner.java") }
          .map { it.primaryData.sarifProblem }
          .map {
            SarifProblemWithPropertiesAndFile(
              it,
              SarifProblemProperties(false, false, false, it.startLine!!, it.startColumn!!),
              project,
              null
            )
          }.toSet()),
      QodanaTreePath.Builder()
    )

    val intermediateNodes = root.getTree()
    assertThat(intermediateNodes.size).isLessThan(nodes1.size)

    val directoryNodesInter = intermediateNodes.filterIsInstance<QodanaTreeDirectoryNode>()
    assertThat(directoryNodesInter.filter { it.excluded }.size).isEqualTo(1)

    val fileNodesInter = intermediateNodes.filterIsInstance<QodanaTreeFileNode>()
    assertThat(fileNodesInter.filter { it.excluded }.size).isEqualTo(1)

    val problemNodesInter = intermediateNodes.filterIsInstance<QodanaTreeProblemNode>()
    assertThat(problemNodesInter.filter { it.excluded }.size).isEqualTo(1)

    root = root.processTreeEvent(
      QodanaTreeProblemEvent(
        problemNodes1
          .filter { it.primaryData.sarifProblem.relativePathToFile.endsWith("Inner.java") }
          .map { it.primaryData.sarifProblem }
          .map {
            SarifProblemWithPropertiesAndFile(
              it,
              SarifProblemProperties(true, false, false, it.startLine!!, it.startColumn!!),
              project,
              testDir.resolve("project/module/inner/Inner.java").refreshAndFindVirtualFile()
            )
          }.toSet()),
      QodanaTreePath.Builder()
    )

    val nodes2 = root.getTree()

    val directoryNodes2 = nodes2.filterIsInstance<QodanaTreeDirectoryNode>()
    assertThat(directoryNodes2.filter { it.excluded }.size).isEqualTo(3)

    val fileNodes2 = nodes2.filterIsInstance<QodanaTreeFileNode>()
    assertThat(fileNodes2.filter { it.excluded }.size).isEqualTo(3)

    val problemNodes2 = nodes2.filterIsInstance<QodanaTreeProblemNode>()
    assertThat(problemNodes2.filter { it.excluded }.size).isEqualTo(3)

    val excludedProblems2 = problemNodes2.filter { it.excluded }.map { it.primaryData }.toSet()
    val excludedFiles2 = fileNodes2.filter { it.excluded }.map { it.primaryData }.toSet()

    assertThat(excludedProblems1).isEqualTo(excludedProblems2)
    assertThat(excludedFiles1).isEqualTo(excludedFiles2)
  }


  fun `test exclude inspection then delete file then restore`() = runDispatchingOnUi {
    val treeConfiguration = QodanaTreeBuildConfiguration(
      true, true, true, true, true
    )
    var root = loadProblems(reportPath, treeConfiguration)
    assertNotNull(root)
    root = root!!.processTreeEvent(
      QodanaTreeExcludeEvent(
        setOf(ConfigExcludeItem("INSPECTION_ID", null)), project
      ),
      QodanaTreePath.Builder()
    )

    val nodes1 = root.getTree()

    val inspectionNodes1 = nodes1.filterIsInstance<QodanaTreeInspectionNode>()
    assertThat(inspectionNodes1.filter { it.excluded }.size).isEqualTo(1)

    val directoryNodes1 = nodes1.filterIsInstance<QodanaTreeDirectoryNode>()
    assertThat(directoryNodes1.filter { it.excluded }.size).isEqualTo(3)

    val fileNodes1 = nodes1.filterIsInstance<QodanaTreeFileNode>()
    assertThat(fileNodes1.filter { it.excluded }.size).isEqualTo(4)

    val problemNodes1 = nodes1.filterIsInstance<QodanaTreeProblemNode>()
    assertThat(problemNodes1.filter { it.excluded }.size).isEqualTo(5)

    val excludedProblems1 = problemNodes1.filter { it.excluded }.map { it.primaryData }.toSet()
    val excludedFiles1 = fileNodes1.filter { it.excluded }.map { it.primaryData }.toSet()

    root = root.processTreeEvent(
      QodanaTreeProblemEvent(
        problemNodes1
          .filter { it.primaryData.sarifProblem.relativePathToFile.endsWith("AnotherLogic.java") }
          .map { it.primaryData.sarifProblem }
          .map {
            SarifProblemWithPropertiesAndFile(
              it,
              SarifProblemProperties(false, false, false, it.startLine!!, it.startColumn!!),
              project,
              null
            )
          }.toSet()
      ),
      QodanaTreePath.Builder()
    )

    val intermediateNodes = root.getTree()
    assertThat(intermediateNodes.size).isLessThan(nodes1.size)

    val inspectionNodesInt = intermediateNodes.filterIsInstance<QodanaTreeInspectionNode>()
    assertThat(inspectionNodesInt.filter { it.excluded }.size).isEqualTo(1)

    val directoryNodesInter = intermediateNodes.filterIsInstance<QodanaTreeDirectoryNode>()
    assertThat(directoryNodesInter.filter { it.excluded }.size).isEqualTo(2)

    val fileNodesInter = intermediateNodes.filterIsInstance<QodanaTreeFileNode>()
    assertThat(fileNodesInter.filter { it.excluded }.size).isEqualTo(3)

    val problemNodesInter = intermediateNodes.filterIsInstance<QodanaTreeProblemNode>()
    assertThat(problemNodesInter.filter { it.excluded }.size).isEqualTo(4)

    root = root.processTreeEvent(
      QodanaTreeProblemEvent(
        problemNodes1
          .filter { it.primaryData.sarifProblem.relativePathToFile.endsWith("AnotherLogic.java") }
          .map { it.primaryData.sarifProblem }
          .map {
            SarifProblemWithPropertiesAndFile(
              it,
              SarifProblemProperties(true, false, false, it.startLine!!, it.startColumn!!),
              project,
              testDir.resolve("project/anotherModule/AnotherLogic.java").refreshAndFindVirtualFile()
            )
          }.toSet()),
      QodanaTreePath.Builder()
    )

    val nodes2 = root.getTree()

    val inspectionNodes2 = nodes2.filterIsInstance<QodanaTreeInspectionNode>()
    assertThat(inspectionNodes2.filter { it.excluded }.size).isEqualTo(1)

    val directoryNodes2 = nodes2.filterIsInstance<QodanaTreeDirectoryNode>()
    assertThat(directoryNodes2.filter { it.excluded }.size).isEqualTo(3)

    val fileNodes2 = nodes2.filterIsInstance<QodanaTreeFileNode>()
    assertThat(fileNodes2.filter { it.excluded }.size).isEqualTo(4)

    val problemNodes2 = nodes2.filterIsInstance<QodanaTreeProblemNode>()
    assertThat(problemNodes2.filter { it.excluded }.size).isEqualTo(5)

    val excludedProblems2 = problemNodes2.filter { it.excluded }.map { it.primaryData }.toSet()
    val excludedFiles2 = fileNodes2.filter { it.excluded }.map { it.primaryData }.toSet()

    assertThat(excludedProblems1).isEqualTo(excludedProblems2)
    assertThat(excludedFiles1).isEqualTo(excludedFiles2)
  }

  private suspend fun loadProblems(path: Path, treeConfiguration: QodanaTreeBuildConfiguration): QodanaTreeRoot? {
    val reportDescriptor = reportTestControllerFromSarifFile(path)
    val report = reportDescriptor.loadReport(project) ?: return null
    val highlightedReportData = HighlightedReportDataImpl.create(project, reportDescriptor, report)

    return buildTreeRoot(highlightedReportData, treeConfiguration)
  }

  private fun reportTestControllerFromSarifFile(path: Path): ReportDescriptorMock {
    val sarif = SarifUtil.readReport(path)

    return ReportDescriptorMock(path.fileName.toString(), sarif)
  }

  private class ReportDescriptorMock(val id: String, private val report: SarifReport?) : ReportDescriptor {

    private val _isAvailableFlow: MutableSharedFlow<NotificationCallback?> = MutableSharedFlow(replay = 1)

    override val isReportAvailableFlow: Flow<NotificationCallback?>
      get() = _isAvailableFlow

    override val browserViewProviderFlow: Flow<BrowserViewProvider> = emptyFlow()

    override val bannerContentProviderFlow: Flow<BannerContentProvider?> = emptyFlow()

    override val noProblemsContentProviderFlow: Flow<NoProblemsContentProvider> = emptyFlow()

    override suspend fun refreshReport(): ReportDescriptor = error("must not be invoked")

    override suspend fun loadReport(project: Project) = report?.let { LoadedReport.Sarif(ValidatedSarif(it), AggregatedReportMetadata(emptyMap()), "") }

    override fun hashCode(): Int = id.hashCode()

    override fun equals(other: Any?): Boolean {
      if (other !is ReportDescriptorMock) return false

      return id == other.id
    }

    override fun toString(): String = id
  }

  private suspend fun buildTreeRoot(highlightedReportData: HighlightedReportData, treeConfiguration: QodanaTreeBuildConfiguration): QodanaTreeRoot {
    val sarifProblemPropertiesProvider = highlightedReportData.sarifProblemPropertiesProvider.value

    val sarifProblemsWithProperties = sarifProblemPropertiesProvider.problemsWithProperties
      .filter { if (treeConfiguration.showBaselineProblems) true else !it.problem.isInBaseline }
      .toList()

    val sarifProblemsWithVirtualFiles = computeSarifProblemsWithVirtualFiles(sarifProblemsWithProperties)

    val moduleDataProvider = if (treeConfiguration.groupByModule) {
      ModuleDataProvider.create(project, sarifProblemsWithVirtualFiles.map { it.first.problem to it.second })
    } else {
      null
    }
    val treeContext = QodanaTreeContext(
      treeConfiguration.groupBySeverity,
      treeConfiguration.groupByInspection,
      moduleDataProvider,
      treeConfiguration.groupByDirectory,
      null,
      project,
      highlightedReportData.inspectionsInfoProvider
    )
    return QodanaTreeRootBuilder(
      treeContext,
      sarifProblemsWithVirtualFiles,
      highlightedReportData.excludedDataFlow.value,
      QodanaTreeRoot.PrimaryData("", null, null, null)
    ).buildRoot()
  }

  private fun computeSarifProblemsWithVirtualFiles(
    sarifProblemsWithProperties: List<SarifProblemWithProperties>
  ): List<Pair<SarifProblemWithProperties, VirtualFile>> {
    val projectDir = testDir.resolve("project")
    val sarifProblemFileToVirtualFile: Map<String, VirtualFile> = sarifProblemsWithProperties
      .map { it.problem }
      .distinctBy { it.relativePathToFile }
      .mapNotNull { sarifProblem ->
        val virtualFile = projectDir.resolve(sarifProblem.relativePathToFile).refreshAndFindVirtualFile() ?: return@mapNotNull null
        sarifProblem.relativePathToFile to virtualFile
      }
      .toMap()

    return sarifProblemsWithProperties.mapNotNull {
      it to (sarifProblemFileToVirtualFile[it.problem.relativePathToFile] ?: return@mapNotNull null)
    }
  }

  private fun QodanaTreeRoot.getTree(): List<QodanaTreeNode<*, *, *>> {
    val processedTreeNodes = mutableListOf<QodanaTreeNode<*, *, *>>()
    val treeNodesToProcess = ArrayDeque<QodanaTreeNode<*, *, *>>()
    treeNodesToProcess.addFirst(this)
    while (treeNodesToProcess.size > 0) {
      processedTreeNodes.add(treeNodesToProcess.removeFirst())
      processedTreeNodes.last().children.nodesSequence.forEach { treeNodesToProcess.addLast(it) }
    }
    return processedTreeNodes
  }
}
