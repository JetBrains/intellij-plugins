package org.jetbrains.qodana.ui.problemsView

import com.intellij.ide.OccurenceNavigator
import com.intellij.pom.Navigatable
import com.intellij.util.ui.tree.TreeUtil
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import kotlinx.coroutines.supervisorScope
import kotlinx.coroutines.withContext
import org.jetbrains.concurrency.asDeferred
import org.jetbrains.qodana.QodanaBundle
import org.jetbrains.qodana.coroutines.QodanaDispatchers
import org.jetbrains.qodana.ui.problemsView.tree.model.*
import org.jetbrains.qodana.ui.problemsView.tree.ui.QodanaUiTreeNode
import org.jetbrains.qodana.ui.problemsView.tree.ui.QodanaUiTreeVisitor
import java.util.concurrent.atomic.AtomicReference

class QodanaProblemsViewNavigator(private val panel: QodanaProblemsViewPanel, scope: CoroutineScope) : OccurenceNavigator {
  private val nextProblemNodePath = AtomicReference<QodanaTreePathToProblemNode?>(null)
  private val previousProblemNodePath = AtomicReference<QodanaTreePathToProblemNode?>(null)

  private val navigateRequestsFlow = MutableSharedFlow<Pair<QodanaTreePathToProblemNode?, Boolean>>(
    extraBufferCapacity = 1,
    onBufferOverflow = BufferOverflow.DROP_OLDEST
  )

  init {
    scope.launch(QodanaDispatchers.Default) {
      navigateRequestsFlow.distinctUntilChanged().collectLatest { (treePathToProblemNode, requestFocus) ->
        supervisorScope {
          launch navigate@ {
            val problemNode = treePathToProblemNode?.problemNode ?: return@navigate
            val treePath = treePathToProblemNode.path

            TreeUtil.promiseSelect(panel.tree, QodanaUiTreeVisitor(treePath)).asDeferred().await() ?: return@navigate

            val navigatable = problemNode.navigatable(panel.project)
            withContext(QodanaDispatchers.Ui) {
              navigatable.navigate(requestFocus)
            }
            navigateRequestsFlow.emit(null to false)
          }
        }
      }
    }
  }

  override fun hasNextOccurence(): Boolean {
    val modelNodesInCurrentSelection = getModelNodesInCurrentSelection()
    val newNextProblemNodePath = findNextProblemNodePath(modelNodesInCurrentSelection)
    nextProblemNodePath.set(newNextProblemNodePath)

    return newNextProblemNodePath != null
  }

  override fun hasPreviousOccurence(): Boolean {
    val modelNodesInCurrentSelection = getModelNodesInCurrentSelection()
    val newPreviousNextProblemNodePath = findPreviousProblemNodePath(modelNodesInCurrentSelection)
    previousProblemNodePath.set(newPreviousNextProblemNodePath)

    return newPreviousNextProblemNodePath != null
  }

  private fun getModelNodesInCurrentSelection(): List<QodanaTreeNode<*, *, *>> {
    val selectedQodanaUiTreeNode = (panel.tree.selectionPath?.lastPathComponent as? QodanaUiTreeNode<*, *>) ?: return emptyList()
    return selectedQodanaUiTreeNode.computeAncestorsAndThisModelNodes()
  }

  override fun goNextOccurence(): OccurenceNavigator.OccurenceInfo = createOccurenceInfo(nextProblemNodePath.get())

  override fun goPreviousOccurence(): OccurenceNavigator.OccurenceInfo = createOccurenceInfo(previousProblemNodePath.get())

  private fun createOccurenceInfo(problemNodePath: QodanaTreePathToProblemNode?): OccurenceNavigator.OccurenceInfo {
    val navigatable = object : Navigatable {
      override fun navigate(requestFocus: Boolean) {
        navigateRequestsFlow.tryEmit(problemNodePath to requestFocus)
      }

      override fun canNavigate(): Boolean = true

      override fun canNavigateToSource(): Boolean = true
    }

    return OccurenceNavigator.OccurenceInfo(navigatable, -1, -1)
  }

  override fun getNextOccurenceActionName(): String = QodanaBundle.message("qodana.problem.navigation.next")

  override fun getPreviousOccurenceActionName(): String = QodanaBundle.message("qodana.problem.navigation.previous")
}