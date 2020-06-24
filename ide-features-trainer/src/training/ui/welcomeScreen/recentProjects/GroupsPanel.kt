// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package training.ui.welcomeScreen.recentProjects

import com.intellij.ide.ProjectGroupActionGroup
import com.intellij.ide.RecentProjectsManager
import com.intellij.ide.RecentProjectsManagerBase
import com.intellij.ide.ReopenProjectAction
import com.intellij.openapi.actionSystem.*
import com.intellij.openapi.application.Application
import com.intellij.openapi.ui.Messages
import com.intellij.openapi.util.io.FileUtil
import com.intellij.openapi.util.io.UniqueNameBuilder
import com.intellij.openapi.wm.impl.welcomeScreen.FlatWelcomeFrame
import com.intellij.openapi.wm.impl.welcomeScreen.NewRecentProjectPanel
import com.intellij.openapi.wm.impl.welcomeScreen.RecentProjectPanel
import com.intellij.openapi.wm.impl.welcomeScreen.WelcomeScreenUIManager
import com.intellij.ui.ColorUtil
import com.intellij.ui.JBColor
import com.intellij.ui.ListUtil
import com.intellij.ui.PopupHandler
import com.intellij.ui.components.JBList
import com.intellij.ui.components.JBScrollPane
import com.intellij.ui.components.panels.NonOpaquePanel
import com.intellij.ui.scale.JBUIScale
import com.intellij.ui.speedSearch.ListWithFilter
import com.intellij.util.IconUtil
import com.intellij.util.PathUtil
import com.intellij.util.SystemProperties
import com.intellij.util.ui.EmptyIcon
import com.intellij.util.ui.JBUI
import com.intellij.util.ui.StartupUiUtil
import com.intellij.util.ui.UIUtil
import com.intellij.util.ui.accessibility.AccessibleContextUtil
import training.actions.ModuleActionGroup
import training.ui.welcomeScreen.recentProjects.actionGroups.CommonActionGroup
import training.ui.welcomeScreen.recentProjects.actionGroups.GroupManager
import training.ui.welcomeScreen.recentProjects.actionGroups.commonActions.DefaultProjectsActionGroup
import java.awt.*
import java.util.*
import javax.swing.*
import javax.swing.event.ListDataEvent
import javax.swing.event.ListDataListener

class GroupsPanel(val app: Application) : NewRecentProjectPanel(app) {
  override fun createList(recentProjectActions: Array<out AnAction>?, size: Dimension?): JBList<AnAction?> {
    val list = ActionList(IFTRecentProjectListActionProvider.instance.getIFTActions().toTypedArray(), this)
    //restore non-project actions after RecentProjectsWelcomeScreenActionBase#rebuildRecentProjectDataModel
    list.model.addListDataListener(RestorableListListener())

    list.background = WelcomeScreenUIManager.getProjectsBackground()
    list.addMouseListener(object : PopupHandler() {
      override fun invokePopup(comp: Component, x: Int, y: Int) {
        val index = list.locationToIndex(Point(x, y))
        if (index != -1 && Arrays.binarySearch(list.selectedIndices, index) < 0) {
          list.selectedIndex = index
        }
        val elementUnderPopup = list.model.getElementAt(index)
        //do not show context menu for non-project related actions on welcome screen (IFT-15)
        if (elementUnderPopup !is ReopenProjectAction && elementUnderPopup !is ProjectGroupActionGroup) return
        val group = ActionManager.getInstance().getAction("WelcomeScreenRecentProjectActionGroup") as ActionGroup?
        if (group != null) {
          ActionManager.getInstance().createActionPopupMenu(ActionPlaces.WELCOME_SCREEN, group).component.show(comp, x, y)
        }
      }
    })
    return list
  }

  //run this function to customize actions of RecentProjectsPanel
  fun customizeActions(): GroupsPanel {
    replaceRemoveRecentProjectsAction()
    return this
  }

  fun validatePath(string: String): Boolean {
    return super.isPathValid(string)
  }

  fun wrap(): JComponent {
    val list = UIUtil.findComponentOfType(this, JList::class.java) ?: return this
    val scroll = JBScrollPane(list,
                              ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
                              ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER)
    scroll.border = null
    return ListWithFilter.wrap(list, scroll) { element: Any ->
      if (element is ReopenProjectAction) {
        val home = SystemProperties.getUserHome()
        var path = element.projectPath
        if (FileUtil.startsWith(path, home)) {
          path = path.substring(home.length)
        }
        return@wrap element.projectName + " " + path
      }
      else if (element is ProjectGroupActionGroup) {
        return@wrap element.group.name
      }
      element.toString()
    }.apply { preferredSize = JBUI.size(300, FlatWelcomeFrame.DEFAULT_HEIGHT) }
  }

  private fun replaceRemoveRecentProjectsAction() {
    removeRecentProjectAction?.unregisterCustomShortcutSet(myList) ?: return
    removeRecentProjectAction = object : AnAction() {
      override fun actionPerformed(e: AnActionEvent) {
        removeRecentProject()
      }

      override fun update(e: AnActionEvent) {
        e.presentation.isEnabled = true
      }
    }
    removeRecentProjectAction.registerCustomShortcutSet(
      CustomShortcutSet.fromString("DELETE", "BACK_SPACE"), myList, app)
  }

  //do not remove tutorials
  private fun removeRecentProject() {
    val selection = myList.selectedValuesList
    if (selection.any { it is ModuleActionGroup || it is CommonActionGroup }) return
    if (selection != null && selection.size > 0) {
      val rc: Int = Messages.showOkCancelDialog(this as Component,
                                                "Remove '${
                                                selection.joinToString("'\n'")
                                                { action: Any -> (action as AnAction).templatePresentation.text }}' from recent projects list?",
                                                "Remove Recent Project",
                                                "OK",
                                                "Cancel",
                                                Messages.getQuestionIcon())
      if (rc == Messages.OK) {
        for (projectAction: Any in selection) {
          RecentProjectPanel.removeRecentProjectElement(projectAction)
        }
        ListUtil.removeSelectedItems(myList)
      }
    }
  }


  override fun createRenderer(pathShortener: UniqueNameBuilder<ReopenProjectAction>?): ListCellRenderer<AnAction> {
    return object : RecentProjectPanel.RecentProjectItemRenderer() {
      private var nameCell: GridBagConstraints? = null
      private var pathCell: GridBagConstraints? = null
      private var closeButtonCell: GridBagConstraints? = null
      val spacer: JComponent = object : NonOpaquePanel() {
        override fun getPreferredSize(): Dimension {
          return Dimension(JBUIScale.scale(22), super.getPreferredSize().height)
        }
      }

      private fun initConstraints() {
        nameCell = GridBagConstraints()
        pathCell = GridBagConstraints()
        closeButtonCell = GridBagConstraints()

        nameCell!!.gridx = 0
        nameCell!!.gridy = 0
        nameCell!!.weightx = 1.0
        nameCell!!.weighty = 1.0
        nameCell!!.anchor = GridBagConstraints.FIRST_LINE_START
        nameCell!!.insets = JBUI.insets(6, 5, 1, 5)



        pathCell!!.gridx = 0
        pathCell!!.gridy = 1

        pathCell!!.insets = JBUI.insets(1, 5, 6, 5)
        pathCell!!.anchor = GridBagConstraints.LAST_LINE_START


        closeButtonCell!!.gridx = 1
        closeButtonCell!!.gridy = 0
        closeButtonCell!!.anchor = GridBagConstraints.FIRST_LINE_END
        closeButtonCell!!.insets = JBUI.insets(7, 7, 7, 7)
        closeButtonCell!!.gridheight = 2

        //closeButtonCell.anchor = GridBagConstraints.WEST;
      }

      override fun getListBackground(isSelected: Boolean, hasFocus: Boolean): Color {
        return if (isSelected) WelcomeScreenUIManager.getProjectsSelectionBackground(hasFocus)
        else WelcomeScreenUIManager.getProjectsBackground()
      }

      override fun getListForeground(isSelected: Boolean, hasFocus: Boolean): Color {
        return WelcomeScreenUIManager.getProjectsSelectionForeground(isSelected, hasFocus)
      }

      override fun layoutComponents() {
        layout = GridBagLayout()
        initConstraints()
        add(myName, nameCell)
        add(myPath, pathCell)
      }

      override fun getListCellRendererComponent(list: JList<out AnAction>, value: AnAction?, index: Int, selected: Boolean, focused: Boolean): Component {
        val fore = getListForeground(selected, list.hasFocus())
        val back = getListBackground(selected, list.hasFocus())
        val name = JLabel()
        val path = JLabel()
        name.foreground = fore
        path.foreground = if (selected) fore else UIUtil.getInactiveTextColor()

        background = back

        return object : JPanel() {
          init {
            layout = BorderLayout()
            background = back

            val isGroup = value is ProjectGroupActionGroup
            var isInsideGroup = false
            if (value is ReopenProjectAction) {
              val path = value.projectPath
              for (group in RecentProjectsManager.getInstance().groups) {
                val projects = group.projects
                if (projects.contains(path)) {
                  isInsideGroup = true
                  break
                }
              }
            }

            border = JBUI.Borders.empty(5, 7)
            if (isInsideGroup) {
              add(spacer, BorderLayout.WEST)
            }
            if (value is CommonActionGroup) {
              name.text = value.name
              name.font = name.font.deriveFont(Font.BOLD)
              add(name)
              name.icon = IconUtil.toSize(if (value.isExpanded) UIUtil.getTreeExpandedIcon() else UIUtil.getTreeCollapsedIcon(),
                                          JBUIScale.scale(16), JBUIScale.scale(16))
            }
            if (isGroup) {
              when (value) {
                is ProjectGroupActionGroup -> {
                  add(spacer, BorderLayout.WEST)
                  val group = value.group
                  name.text = " " + group.name
                  name.icon = IconUtil.toSize(if (group.isExpanded) UIUtil.getTreeExpandedIcon() else UIUtil.getTreeCollapsedIcon(),
                                              JBUIScale.scale(16), JBUIScale.scale(16))
                  name.font = name.font.deriveFont(Font.BOLD)
                  add(name)
                }
              }
            }
            else if (value is ReopenProjectAction || value is RenderableAction) {
              val i = if (isInsideGroup) 80 else 60
              if (isInsideGroup)
                add(spacer, BorderLayout.WEST)
              val renderableAction: RenderableAction = if (value is ReopenProjectAction) {
                object : RenderableAction {
                  override val action: AnAction
                    get() = TODO("not implemented") //To change initializer of created properties use File | Settings | File Templates.
                  override val name: String
                    get() = value.projectName
                  override val description: String?
                    get() {
                      val realPath = PathUtil.toSystemDependentName(value.projectPath)
                      if (realPath != path.text) {
                        IFTRecentProjectListActionProvider.instance.projectsWithLongPaths.add(value)
                      }
                      return getTitle2Text((value as ReopenProjectAction?)!!, path, JBUIScale.scale(i))
                    }
                  override val icon: Icon?
                    get() {
                      val recentProjectsManage = RecentProjectsManagerBase.instanceEx
                      return recentProjectsManage.getProjectIcon(value.projectPath, StartupUiUtil.isUnderDarcula())
                             ?: if (StartupUiUtil.isUnderDarcula()) {
                               //No dark icon for this project
                               recentProjectsManage.getProjectIcon(value.projectPath, false)
                             }
                             else null
                    }
                  override val emptyIcon: Icon
                    get() = TODO("not implemented") //To change initializer of created properties use File | Settings | File Templates.
                  override var isValid: Boolean
                    get() = isPathValid(value.projectPath)
                    set(value) {}
                }
              }
              else value as RenderableAction

              val p = NonOpaquePanel(BorderLayout())
              name.text = renderableAction.name
              path.text = renderableAction.description ?: ""

              if (value is ModuleActionGroup) {
                val scaledSize: Float = JBUIScale.scaleFontSize(11f) * 1f
                path.font = path.font.deriveFont(scaledSize)
              }
              if (value is ModuleActionGroup)
                if (!value.module.hasNotPassedLesson()) name.foreground = path.foreground
              val nameComponent: JComponent = name

              if (!renderableAction.isValid) {
                path.foreground = ColorUtil.mix(path.foreground, JBColor.red, .5)
              }
              if (renderableAction.description.isNullOrEmpty()) {
                p.add(nameComponent, BorderLayout.CENTER)
              }
              else {
                p.add(nameComponent, BorderLayout.NORTH)
                p.add(path, BorderLayout.SOUTH)
              }

              if (value is ModuleActionGroup)
                p.border = JBUI.Borders.emptyRight(5)
              else
                p.border = JBUI.Borders.emptyRight(30)

              var icon = renderableAction.icon
              if (icon == null) icon = EmptyIcon.ICON_16

              val projectIcon = object : JLabel("", icon, SwingConstants.LEFT) {
                override fun paintComponent(g: Graphics) {
                  val y = if (value is ModuleActionGroup)
                    0
                  else
                    (height - getIcon().iconHeight) / 2
                  getIcon().paintIcon(this, g, 0, y)
                }
              }
              projectIcon.border = JBUI.Borders.emptyRight(8)
              projectIcon.verticalAlignment = SwingConstants.CENTER
              val panel = NonOpaquePanel(BorderLayout())
              panel.add(p)
              panel.add(projectIcon, BorderLayout.WEST)
              add(panel)
            }
            AccessibleContextUtil.setCombinedName(this, name, " - ", path)
            AccessibleContextUtil.setCombinedDescription(this, name, " - ", path)
          }

          override fun getPreferredSize(): Dimension {
            return Dimension(super.getPreferredSize().width, JBUIScale.scale(44))
          }
        }
      }
    }
  }

  @Suppress("UNCHECKED_CAST")
  internal inner class RestorableListListener : ListDataListener {
    override fun contentsChanged(e: ListDataEvent?) {}

    override fun intervalAdded(e: ListDataEvent?) {
      if (e?.index0 == null || e.source == null) return
      if (e.index0 == e.index1) {
        val model = e.source as DefaultListModel<AnAction>
        val action = model.get(e.index1)
        if (action is ProjectGroupActionGroup || action is ReopenProjectAction) {
          if (GroupManager.instance.registeredGroups.filterIsInstance<DefaultProjectsActionGroup>().first().isExpanded.not()) {
            model.remove(e.index0)
          }
        }
      }
    }

    override fun intervalRemoved(e: ListDataEvent?) {
      if (e != null && ((e.source as DefaultListModel<AnAction>).size == 0)) {
        val model = e.source as DefaultListModel<AnAction>
        val nonProjectActions = IFTRecentProjectListActionProvider.instance.getIFTActions(false)
        nonProjectActions.forEach { model.addElement(it) }
      }
    }
  }
}