package org.jetbrains.qodana.ui.problemsView.tree.model

import com.intellij.openapi.util.NlsContexts
import com.intellij.openapi.util.NlsSafe
import com.intellij.openapi.vfs.VirtualFile
import org.jetbrains.qodana.problem.SarifProblem
import org.jetbrains.qodana.problem.SarifProblemProperties
import org.jetbrains.qodana.staticAnalysis.sarif.QodanaSeverity
import java.nio.file.Path
import java.time.Instant
import kotlin.io.path.invariantSeparatorsPathString

interface QodanaTreeRoot : QodanaTreeNode<QodanaTreeRoot, QodanaTreeRoot.Children, QodanaTreeRoot.PrimaryData> {
  data class PrimaryData(
    @NlsSafe val reportName: String,
    @NlsSafe val branch: String?,
    @NlsSafe val revision: String?,
    val createdAt: Instant?,
  ) : QodanaTreeNode.PrimaryData

  sealed interface Children : QodanaTreeNode.Children<Children> {
    interface SeverityNodes : Children {
      val nodes: List<QodanaTreeSeverityNode>
    }

    interface InspectionOrFileSystemLevelNodes : Children {
      val inspectionOrFileSystemLevelChildren: QodanaTreeInspectionOrFileSystemLevelChildren
    }
  }
}

interface QodanaTreeSeverityNode: QodanaTreeNode<QodanaTreeSeverityNode, QodanaTreeInspectionOrFileSystemLevelChildren, QodanaTreeSeverityNode.PrimaryData> {
  data class PrimaryData(val qodanaSeverity: QodanaSeverity) : QodanaTreeNode.PrimaryData
}

sealed interface QodanaTreeInspectionOrFileSystemLevelChildren : QodanaTreeNode.Children<QodanaTreeInspectionOrFileSystemLevelChildren> {
  interface InspectionCategoryNodes : QodanaTreeInspectionOrFileSystemLevelChildren {
    val nodes: List<QodanaTreeInspectionCategoryNode>
  }

  interface FileSystemLevelNodes : QodanaTreeInspectionOrFileSystemLevelChildren {
    val fileSystemLevelChildren: QodanaTreeFileSystemLevelChildren
  }
}

interface QodanaTreeInspectionCategoryNode : QodanaTreeNode<QodanaTreeInspectionCategoryNode, QodanaTreeInspectionCategoryNode.Children, QodanaTreeInspectionCategoryNode.PrimaryData> {
  data class PrimaryData(@NlsContexts.Label val inspectionCategory: String?) : QodanaTreeNode.PrimaryData

  interface Children : QodanaTreeNode.Children<Children> {
    val nodes: List<QodanaTreeInspectionNode>
  }
}

interface QodanaTreeInspectionNode : QodanaTreeNode<QodanaTreeInspectionNode, QodanaTreeFileSystemLevelChildren, QodanaTreeInspectionNode.PrimaryData> {
  data class PrimaryData(val inspectionId: String) : QodanaTreeNode.PrimaryData

  val inspectionName: String?
}

sealed interface QodanaTreeFileSystemLevelChildren : QodanaTreeNode.Children<QodanaTreeFileSystemLevelChildren> {
  interface ModuleNodes : QodanaTreeFileSystemLevelChildren {
    val moduleNodes: List<QodanaTreeModuleNode>

    val nodesWithoutModule : QodanaTreeNodesWithoutModuleNode?
  }

  interface FileAndDirectoryNodes : QodanaTreeFileSystemLevelChildren {
    val fileAndDirectoryNodeChildren: QodanaTreeFileAndDirectoryNodeChildren
  }

  interface FileNodes : QodanaTreeFileSystemLevelChildren {
    val fileNodesChildren: FileNodesChildren
  }
}

interface QodanaTreeModuleNode : QodanaTreeNode<QodanaTreeModuleNode, QodanaTreeModuleNode.Children, QodanaTreeModuleNode.PrimaryData> {
  data class PrimaryData(val moduleData: ModuleData) : QodanaTreeNode.PrimaryData

  sealed interface Children : QodanaTreeNode.Children<Children> {

    interface FileAndDirectoryNodes : Children {
      val fileAndDirectoryNodeChildren: QodanaTreeFileAndDirectoryNodeChildren
    }

    interface FileNodes : Children {
      val fileNodesChildren: FileNodesChildren
    }
  }
}

interface QodanaTreeNodesWithoutModuleNode : QodanaTreeNode<QodanaTreeNodesWithoutModuleNode, QodanaTreeModuleNode.Children, QodanaTreeNodesWithoutModuleNode.PrimaryData> {
  data class PrimaryData(val path: Path) : QodanaTreeNode.PrimaryData
}

interface QodanaTreeFileAndDirectoryNodeChildren : QodanaTreeNode.Children<QodanaTreeFileAndDirectoryNodeChildren> {
  val fileNodeChildren: List<QodanaTreeFileNode>

  val directoryNodeChildren: List<QodanaTreeDirectoryNode>
}

interface FileNodesChildren : QodanaTreeNode.Children<FileNodesChildren> {
  val nodes: Collection<QodanaTreeFileNode>
}

interface QodanaTreeDirectoryNode : QodanaTreeNode<QodanaTreeDirectoryNode, QodanaTreeFileAndDirectoryNodeChildren, QodanaTreeDirectoryNode.PrimaryData> {
  data class PrimaryData(val fullPath: Path, val parentPath: Path) : QodanaTreeNode.PrimaryData {
    val ownPath: Path
      get() = parentPath.relativize(fullPath)
  }

  override val children: QodanaTreeFileAndDirectoryNodeChildren
}


interface QodanaTreeFileNode : QodanaTreeNode<QodanaTreeFileNode, QodanaTreeFileNode.Children, QodanaTreeFileNode.PrimaryData> {
  data class PrimaryData(val parentPath: Path, val file: Path) : QodanaTreeNode.PrimaryData {
    val invariantFilePathString by lazy {
      file.invariantSeparatorsPathString
    }
  }

  interface Children : QodanaTreeNode.Children<Children> {
    val nodes: List<QodanaTreeProblemNode>
  }

  val virtualFile: VirtualFile
}

interface QodanaTreeProblemNode : QodanaTreeNode<QodanaTreeProblemNode, QodanaTreeProblemNode.NoChildren, QodanaTreeProblemNode.PrimaryData> {
  data class PrimaryData(val sarifProblem: SarifProblem) : QodanaTreeNode.PrimaryData

  object NoChildren : QodanaTreeNode.Children<NoChildren> {
    override val nodesSequence: Sequence<QodanaTreeNode<*, *, *>>
      get() = emptySequence()

    override val nodeByPrimaryDataFinder: (QodanaTreeNode.PrimaryData) -> QodanaTreeNode<*, *, *>?
      get() = { null }

    override fun computeNewChildren(event: QodanaTreeEvent, pathBuilder: QodanaTreePath.Builder): NoChildren? = null
  }

  override val children: NoChildren
    get() = NoChildren

  override val isValid: Boolean
    get() = sarifProblemProperties.isPresent

  val sarifProblemProperties: SarifProblemProperties

  val virtualFile: VirtualFile
}
