package org.jetbrains.qodana.ui.problemsView.tree.ui

import com.intellij.analysis.problemsView.Problem
import com.intellij.analysis.problemsView.toolWindow.Node
import com.intellij.analysis.problemsView.toolWindow.Root
import com.intellij.ide.projectView.PresentationData
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.ui.SimpleTextAttributes
import org.jetbrains.qodana.ui.problemsView.QodanaProblemsViewPanel
import org.jetbrains.qodana.ui.problemsView.tree.model.QodanaTreeNode
import org.jetbrains.qodana.ui.problemsView.tree.model.QodanaTreeRoot
import org.jetbrains.qodana.vcs.trimRevisionString
import java.time.ZoneId
import java.time.format.DateTimeFormatter

class QodanaUiTreeRoot(
  panel: QodanaProblemsViewPanel,
  private val modelRootProvider: () -> QodanaTreeRoot?,
) : Root(panel), QodanaUiTreeNode<QodanaTreeRoot, QodanaTreeRoot.PrimaryData> {
  override val parent: QodanaUiTreeNode<*, *>? = null

  override val primaryData: QodanaTreeRoot.PrimaryData
    get() = modelTreeNode?.primaryData ?: QodanaTreeRoot.PrimaryData("", null, null, null)

  override val modelTreeNode: QodanaTreeRoot?
    get() = modelRootProvider.invoke()

  override fun computeModelTreeNode(): QodanaTreeRoot? = modelTreeNode

  override fun computeAncestorsAndThisModelNodes(): List<QodanaTreeNode<*, *, *>> {
    return listOfNotNull(modelTreeNode)
  }

  override fun getExcludeActionsDescriptors(): List<QodanaUiTreeNode.ExcludeActionDescriptor> = emptyList()

  override fun asViewNode(): Node = this

  override fun update(project: Project, presentation: PresentationData) {
    val reportName = primaryData.reportName
    val branch = primaryData.branch
    val revision = primaryData.revision?.trimRevisionString()
    val formattedDate = primaryData.createdAt?.let {
      DateTimeFormatter.ofPattern("dd/MM/yy, HH:mm").format(it.atZone(ZoneId.systemDefault()))
    }

    @Suppress("HardCodedStringLiteral") val additionalDescriptionText = StringBuilder().apply {
      val firstElement = branch ?: revision ?: formattedDate
      if (firstElement != null) {
        append(firstElement)
      }
      if (revision != null && firstElement !== revision) {
        append(" $revision")
      }
      if (formattedDate != null && firstElement !== formattedDate) {
        append(", $formattedDate")
      }
    }.toString()

    presentation.addText(reportName, SimpleTextAttributes.REGULAR_BOLD_ATTRIBUTES)
    presentation.appendGrayedText(additionalDescriptionText)
    presentation.appendProblemsCount(getProblemCount())
  }

  override fun hashCode(): Int = System.identityHashCode(this)

  override fun equals(other: Any?): Boolean = this === other

  override fun getChildren(): Collection<Node> {
    return computeModelTreeNode()?.children?.nodesSequence?.mapNotNull { it.toUiNode(this)?.asViewNode() }?.toList() ?: emptyList()
  }

  override fun getProblemCount(): Int = modelTreeNode?.problemsCount ?: 0

  override fun getProblemFiles(): Collection<VirtualFile> = emptyList()

  override fun getFileProblemCount(file: VirtualFile): Int = 0

  override fun getFileProblems(file: VirtualFile): Collection<Problem> = emptyList()

  override fun getOtherProblemCount(): Int = 0

  override fun getOtherProblems(): Collection<Problem> = emptyList()
}