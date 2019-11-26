package training.ui.welcomeScreen.recentProjects

import com.intellij.icons.AllIcons
import com.intellij.ide.DataManager
import com.intellij.ide.ProjectGroupActionGroup
import com.intellij.ide.RecentProjectsManager
import com.intellij.ide.ReopenProjectAction
import com.intellij.openapi.actionSystem.ActionToolbar
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.DefaultActionGroup
import com.intellij.openapi.actionSystem.ex.ActionUtil
import com.intellij.openapi.wm.impl.welcomeScreen.RecentProjectPanel
import com.intellij.ui.ListActions
import com.intellij.ui.ListUtil
import com.intellij.ui.components.JBList
import com.intellij.ui.scale.JBUIScale
import com.intellij.util.IconUtil
import com.intellij.util.PathUtil
import training.actions.ModuleActionGroup
import java.awt.Dimension
import java.awt.Graphics
import java.awt.Point
import java.awt.Rectangle
import java.awt.event.ActionEvent
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import javax.swing.AbstractAction
import javax.swing.Icon
import javax.swing.ListSelectionModel

class ActionList(listData: Array<out AnAction>, private val groupsPanel: GroupsPanel) : JBList<AnAction?>(*listData) {
  private var mousePoint: Point? = null

  init {
    setExpandableItemsEnabled(false)
    setEmptyText("  No Project Open Yet  ")
    selectionMode = ListSelectionModel.MULTIPLE_INTERVAL_SELECTION
    getAccessibleContext().accessibleName = RecentProjectPanel.RECENT_PROJECTS_LABEL
    val handler = MouseHandler()
    addMouseListener(handler)
    addMouseMotionListener(handler)
    installKeyboardActions()
  }

  fun getCloseIconRect(index: Int): Rectangle {
    val bounds = getCellBounds(index, index)
    val icon = toSize(AllIcons.Welcome.Project.Remove)
    return Rectangle(bounds.width - icon.iconWidth - JBUIScale.scale(10),
                     bounds.y + (bounds.height - icon.iconHeight) / 2,
                     icon.iconWidth, icon.iconHeight)
  }

  override fun paint(g: Graphics) {
    super.paint(g)
    val lastMousePoint = mousePoint ?: return
    val index = locationToIndex(lastMousePoint)
    if (index != -1) {
      if (model.getElementAt(index) is ProjectGroupActionGroup || model.getElementAt(index) is ReopenProjectAction) {
        val iconRect = getCloseIconRect(index)
        val icon = toSize(if (iconRect.contains(lastMousePoint)) AllIcons.Welcome.Project.Remove_hover else AllIcons.Welcome.Project.Remove)
        icon.paintIcon(this, g, iconRect.x, iconRect.y)
      }
    }
  }

  override fun getToolTipText(event: MouseEvent): String {
    val i = locationToIndex(event.point)
    if (i != -1) {
      val elem: Any = model.getElementAt(i)!!
      if (elem is ReopenProjectAction) {
        val path = elem.projectPath
        val valid: Boolean = groupsPanel.validatePath(path)
        if (!valid || IFTRecentProjectsManager.manager.projectsWithLongPaths.contains(elem)) {
          val suffix = if (valid) "" else " (unavailable)"
          return PathUtil.toSystemDependentName(path) + suffix
        }
      }
      if (elem is ModuleActionGroup) {
        return elem.description ?: ""
      }
    }
    return super.getToolTipText(event) ?: ""
  }

  override fun getPreferredScrollableViewportSize(): Dimension {
    return size ?: super.getPreferredScrollableViewportSize()
  }

  internal inner class MouseHandler : MouseAdapter() {
    override fun mouseEntered(e: MouseEvent) {
      mousePoint = e.point
    }

    override fun mouseExited(e: MouseEvent) {
      mousePoint = null
    }

    override fun mouseMoved(e: MouseEvent) {
      mousePoint = e.point
    }

    override fun mouseReleased(e: MouseEvent) {
      val point = e.point
      val list = this@ActionList
      val index = list.locationToIndex(point)
      if (index != -1) {
        if (getCloseIconRect(index).contains(point)) {
          e.consume()
          val element: Any = model.getElementAt(index)!!
          if (element is ProjectGroupActionGroup) {
            val group = element.group
            removeRecentProjectElement(group)
            ListUtil.removeSelectedItems(this@ActionList)
          }
          else if (element is ReopenProjectAction) {
            removeRecentProjectElement(element)
            ListUtil.removeSelectedItems(this@ActionList)
          }
        }
      }
    }
  }

  private fun installKeyboardActions() {
    actionMap.put(ListActions.Right.ID, object : AbstractAction() {
      override fun actionPerformed(e: ActionEvent) {
        val selected = this@ActionList.selectedValue
        val groupAction = if (selected is DefaultActionGroup) selected else return
        if (groupAction.isPopup) {
          val listContext = DataManager.getInstance().getDataContext(this@ActionList)
          val event = AnActionEvent.createFromAnAction(groupAction, null, "WelcomeFrame.ActionList", listContext)
          ActionUtil.performActionDumbAware(groupAction, event)
        }
      }
    })

    actionMap.put(ListActions.Left.ID, object : AbstractAction() {
      override fun actionPerformed(e: ActionEvent) {
        val selected = this@ActionList.selectedValue
        val groupAction = if (selected is DefaultActionGroup) selected else return
        if (!groupAction.isPopup) {
          val listContext = DataManager.getInstance().getDataContext(this@ActionList)
          val event = AnActionEvent.createFromAnAction(groupAction, null, "WelcomeFrame.ActionList", listContext)
          ActionUtil.performActionDumbAware(groupAction, event)
        }
      }
    })
  }


  private fun removeRecentProjectElement(element: Any?) {
    val manager = RecentProjectsManager.getInstance()
    if (element is ReopenProjectAction) {
      manager.removePath(element.projectPath)
    }
    else if (element is ProjectGroupActionGroup) {
      val group = element.group
      for (path in group.projects) {
        manager.removePath(path)
      }
      manager.removeGroup(group)
    }
  }


  private fun toSize(icon: Icon): Icon {
    return IconUtil.toSize(icon,
                           ActionToolbar.DEFAULT_MINIMUM_BUTTON_SIZE.getWidth().toInt(),
                           ActionToolbar.DEFAULT_MINIMUM_BUTTON_SIZE.getHeight().toInt())
  }

}