package org.jetbrains.qodana.ui.link

import com.intellij.collaboration.ui.HorizontalListPanel
import com.intellij.collaboration.ui.VerticalListPanel
import com.intellij.icons.AllIcons
import com.intellij.navigation.extractVcsOriginCanonicalPath
import com.intellij.ui.SimpleColoredComponent
import com.intellij.ui.SimpleTextAttributes
import com.intellij.util.IconUtil
import com.intellij.util.text.DateFormatUtil
import com.intellij.util.ui.*
import org.jetbrains.qodana.QodanaBundle
import org.jetbrains.qodana.cloud.project.QodanaCloudProjectsPagedLoader
import java.awt.Component
import javax.swing.JList
import javax.swing.ListCellRenderer

class QodanaCloudProjectRenderer : ListCellRenderer<QodanaCloudProjectsPagedLoader.ProjectData> {
  private val teamInsetsLeft: JBInsets = JBUI.insetsLeft(10)
  private val projectInsetsLeft: JBInsets = JBUI.insetsLeft(30)
  private val emptyTopBottomBorder: JBEmptyBorder = JBUI.Borders.empty(4, 0)

  private val teamNameComponent = SimpleColoredComponent().apply {
    ipad = teamInsetsLeft
  }
  private val teamDescriptionComponent = SimpleColoredComponent().apply {
    ipad = teamInsetsLeft
  }
  private val teamRenderer = VerticalListPanel().apply {
    add(teamNameComponent)
    add(teamDescriptionComponent)
    border = emptyTopBottomBorder
  }

  private val projectNameComponent = SimpleColoredComponent().apply {
    ipad = projectInsetsLeft
  }
  private val projectVcsUrlComponent = SimpleColoredComponent()

  private val projectProblemsCountComponent = SimpleColoredComponent().apply {
    ipad = projectInsetsLeft
  }
  private val projectBranchComponent = SimpleColoredComponent()
  private val projectLastRunComponent = SimpleColoredComponent()

  private val projectRenderer = VerticalListPanel().apply {
    isOpaque = true
    val projectTitle = HorizontalListPanel().apply {
      add(projectNameComponent)
      add(projectVcsUrlComponent)
    }
    add(projectTitle)

    val projectDescription = HorizontalListPanel().apply {
      add(projectProblemsCountComponent)
      add(projectBranchComponent)
      add(projectLastRunComponent)
    }
    add(projectDescription)
    border = emptyTopBottomBorder
  }

  override fun getListCellRendererComponent(
    list: JList<out QodanaCloudProjectsPagedLoader.ProjectData>,
    cloudProject: QodanaCloudProjectsPagedLoader.ProjectData,
    index: Int,
    isSelected: Boolean,
    cellHasFocus: Boolean
  ): Component {
    fillProjectComponents(cloudProject)

    val model = list.model
    val renderWithTeam = index == 0 || model.getElementAt(index).team != model.getElementAt(index - 1).team
    if (!renderWithTeam) {
      UIUtil.setBackgroundRecursively(projectRenderer, ListUiUtil.WithTallRow.background(list, isSelected, true))
      return projectRenderer
    }

    val team = cloudProject.team
    val teamName = team.name ?: QodanaBundle.message("qodana.link.project.dialog.team.id", team.id)
    teamNameComponent.clear()
    teamNameComponent.append(teamName, SimpleTextAttributes.REGULAR_BOLD_ATTRIBUTES)

    val teamDescription = QodanaBundle.message("qodana.link.project.dialog.team.description", team.projectsCount, team.membersCount)
    teamDescriptionComponent.clear()
    teamDescriptionComponent.append(teamDescription, SimpleTextAttributes.GRAYED_SMALL_ATTRIBUTES)
    return VerticalListPanel().apply {
      isOpaque = true
      add(teamRenderer)
      add(projectRenderer)
      UIUtil.setBackgroundRecursively(this, ListUiUtil.WithTallRow.background(list, isSelected = false, hasFocus = true))
      UIUtil.setBackgroundRecursively(projectRenderer, ListUiUtil.WithTallRow.background(list, isSelected, true))
    }
  }

  private fun fillProjectComponents(cloudProject: QodanaCloudProjectsPagedLoader.ProjectData) {
    val descriptionAttributes = SimpleTextAttributes.GRAYED_SMALL_ATTRIBUTES
    projectNameComponent.clear()
    projectVcsUrlComponent.clear()
    projectProblemsCountComponent.clear()
    projectBranchComponent.clear()
    projectLastRunComponent.clear()

    val cloudProjectName = cloudProject.name ?: QodanaBundle.message("qodana.link.project.dialog.project.id", cloudProject.id)
    projectNameComponent.append(cloudProjectName)
    val vcsUrl = extractVcsOriginCanonicalPath(cloudProject.vcsUrl)
    if (vcsUrl != null) {
      projectVcsUrlComponent.append(vcsUrl, descriptionAttributes)
    }

    val problemsCount = cloudProject.problemsCount
    if (problemsCount == null) {
      projectProblemsCountComponent.append(QodanaBundle.message("qodana.link.project.dialog.project.no.runs"), descriptionAttributes)
      return
    }


    val baselineProblemsCount = cloudProject.baselineProblemsCount ?: 0
    val problemsCountMessage = QodanaBundle.message("qodana.link.project.dialog.project.problems", problemsCount, baselineProblemsCount)
    projectProblemsCountComponent.append(problemsCountMessage, descriptionAttributes)

    val branch = cloudProject.branch
    val branchText = branch?.let { if (it.length > 16) "${branch.subSequence(0, 15)}..." else branch }
                     ?: QodanaBundle.message("qodana.link.project.dialog.project.no.branch")
    projectBranchComponent.icon = IconUtil.resizeSquared(AllIcons.Vcs.BranchNode, 12)
    projectBranchComponent.append(branchText, descriptionAttributes)

    val lastRunDate = cloudProject.lastRunDate
    if (lastRunDate != null) {
      val description = QodanaBundle.message("qodana.link.project.last.run", DateFormatUtil.formatPrettyDateTime(lastRunDate))
      projectLastRunComponent.icon = IconUtil.resizeSquared(AllIcons.Vcs.History, 11)
      projectLastRunComponent.append(description, descriptionAttributes)
    }
  }
}