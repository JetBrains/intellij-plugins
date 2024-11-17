package org.jetbrains.qodana.ui.problemsView.tree.ui

import com.intellij.ide.projectView.PresentationData
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.pom.Navigatable
import com.intellij.ui.JBColor
import com.intellij.ui.SimpleTextAttributes
import com.intellij.ui.SimpleTextAttributes.STYLE_PLAIN
import com.intellij.ui.tree.LeafState
import org.jetbrains.qodana.QodanaBundle
import org.jetbrains.qodana.problem.buildDescription
import org.jetbrains.qodana.stats.QodanaPluginStatsCounterCollector
import org.jetbrains.qodana.stats.toSelectedNodeType
import org.jetbrains.qodana.ui.problemsView.tree.model.QodanaTreeProblemNode
import org.jetbrains.qodana.ui.problemsView.tree.model.navigatable
import java.awt.Color

private val FIXED_PROBLEM_COLOR = JBColor(Color(6, 125, 3), Color(115, 189, 121))

class QodanaUiTreeProblemNode(
  parent: QodanaUiTreeNode<*, *>,
  primaryData: QodanaTreeProblemNode.PrimaryData,
) : QodanaUiTreeNodeBase<QodanaTreeProblemNode, QodanaTreeProblemNode.PrimaryData>(parent, primaryData) {
  override fun computeModelTreeNode(): QodanaTreeProblemNode? = computeModelTreeNodeThroughParent()

  override fun getExcludeActionsDescriptors(): List<QodanaUiTreeNode.ExcludeActionDescriptor> = emptyList()

  override fun getChildren(): Collection<QodanaUiTreeNodeBase<*, *>> = emptyList()

  override fun getVirtualFile(): VirtualFile? = modelTreeNode?.virtualFile

  override fun getNavigatable(): Navigatable? {
    val project = project ?: return null
    val modelTreeNode = modelTreeNode ?: return null

    val nodeNavigatable = modelTreeNode.navigatable(project)
    return object : Navigatable by nodeNavigatable {
      override fun navigate(requestFocus: Boolean) {
        QodanaPluginStatsCounterCollector.PROBLEM_NAVIGATED.log(
          project,
          modelTreeNode.toSelectedNodeType(),
          modelTreeNode.problemsCount
        )
        nodeNavigatable.navigate(requestFocus)
      }

      override fun canNavigateToSource(): Boolean {  // needed because of bug KT-18324
        return nodeNavigatable.canNavigateToSource()
      }

      override fun canNavigate(): Boolean {
        return nodeNavigatable.canNavigate()
      }
    }
  }

  override fun doUpdate(project: Project, presentation: PresentationData, modelTreeNode: QodanaTreeProblemNode) {
    val sarifProblem = primaryData.sarifProblem
    val message = sarifProblem.buildDescription(useQodanaPrefix = false, showSeverity = false)
    val sarifProblemProperties = modelTreeNode.sarifProblemProperties

    presentation.setIcon(sarifProblem.qodanaSeverity.icon)

    if (modelTreeNode.excluded) {
      presentation.addText(message, SimpleTextAttributes.EXCLUDED_ATTRIBUTES)
      presentation.appendText(QodanaBundle.message("qodana.TreeNode.excluded"), SimpleTextAttributes.GRAYED_BOLD_ATTRIBUTES)
      return
    }

    presentation.addText(message, SimpleTextAttributes.REGULAR_ATTRIBUTES)

    if (sarifProblem.isInBaseline) {
      presentation.appendText(QodanaBundle.message("qodana.problem.baseline"), SimpleTextAttributes.SYNTHETIC_ATTRIBUTES)
    }
    if (sarifProblemProperties.isMissing) {
      presentation.appendText(QodanaBundle.message("qodana.problem.not.present"), SimpleTextAttributes.ERROR_ATTRIBUTES)
      return
    }
    if (sarifProblemProperties.isFixed) {
      presentation.appendText(QodanaBundle.message("qodana.problem.fixed"), SimpleTextAttributes(STYLE_PLAIN, FIXED_PROBLEM_COLOR))
      return
    }
    val line = sarifProblemProperties.line
    if (line >= 0) {
      presentation.addText(" :${line + 1}", SimpleTextAttributes.GRAYED_ATTRIBUTES)
    }
  }

  override fun getName(): String = primaryData.toString()

  override fun getLeafState(): LeafState = LeafState.ALWAYS
}