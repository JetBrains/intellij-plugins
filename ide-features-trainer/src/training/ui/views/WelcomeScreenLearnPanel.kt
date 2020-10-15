// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package training.ui.views

import com.intellij.icons.AllIcons.General.ChevronDown
import com.intellij.icons.AllIcons.General.ChevronUp
import com.intellij.openapi.actionSystem.ActionPlaces
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.DataContext
import com.intellij.openapi.actionSystem.ex.ActionUtil
import com.intellij.openapi.wm.impl.welcomeScreen.WelcomeScreenUIManager
import com.intellij.ui.AncestorListenerAdapter
import com.intellij.ui.RoundedLineBorder
import com.intellij.util.ui.JBUI
import icons.FeaturesTrainerIcons.PluginIcon
import training.actions.StartLearnAction
import training.learn.CourseManager
import training.learn.LearnBundle
import training.learn.interfaces.Module
import training.ui.views.WelcomeScreenPanelColorsAndFonts.HEADER
import training.ui.views.WelcomeScreenPanelColorsAndFonts.InteractiveCoursesBorder
import training.ui.views.WelcomeScreenPanelColorsAndFonts.MODULE_DESCRIPTION
import training.ui.views.WelcomeScreenPanelColorsAndFonts.MODULE_HEADER
import training.ui.views.WelcomeScreenPanelColorsAndFonts.REGULAR
import java.awt.*
import java.awt.event.*
import javax.swing.*
import javax.swing.border.CompoundBorder
import javax.swing.border.EmptyBorder
import javax.swing.event.AncestorEvent
import javax.swing.text.SimpleAttributeSet

class WelcomeScreenLearnPanel() : JPanel() {

  private val interactiveCoursesPanel: JPanel = JPanel()
  private val helpAndResourcesPanel: JPanel = JPanel()

  private var modulesPanel: JPanel

  private val startLearningButton = JButton(LearnBundle.message("welcome.tab.start.learning.button"))
  private val chevronPanel = JPanel()
  private val chevronLabel = JLabel(ChevronDown)
  private val interactiveCoursesHeader: JTextPane = HeightLimitedPane(LearnBundle.message("welcome.tab.interactive.courses"), HEADER)
  private val learnIdeFeaturesDescription = HeightLimitedPane(LearnBundle.message("welcome.tab.description.learn.ide.features"), REGULAR)
  private val learnIdeFeaturesContent: JPanel = createLearnIdeFeaturesContent()
  private val learnIdeFeaturesPanel: JPanel = HeightLimitedPanel { learnIdeFeaturesContent.preferredSize.height }

  private val pluginPanelWidth = 72
  private val chevronPanelWidth = 55

  private val roundBorder1px = CompoundBorder(RoundedLineBorder(InteractiveCoursesBorder, 4, 1), JBUI.Borders.emptyRight(5))

  private enum class ContentState { COLLAPSED, EXPANDED }

  private var contentState: ContentState = ContentState.COLLAPSED

  init {
    layout = null
    isFocusable = false
    isOpaque = true
    background = WelcomeScreenUIManager.getProjectsBackground()

    val contentPanel = JPanel()
    contentPanel.layout = BoxLayout(contentPanel, BoxLayout.PAGE_AXIS)

    //Obligatory block
    modulesPanel = modulesPanel()
    initInteractiveCoursePanel()
    contentPanel.add(interactiveCoursesPanel)
    contentPanel.add(Box.createVerticalGlue())

    //set LearnPanel UI
    contentPanel.border = EmptyBorder(24, 24, 24, 24)
    contentPanel.isOpaque = false
    add(contentPanel)
    contentPanel.location = Point(0, 0)
    contentPanel.bounds = Rectangle(contentPanel.location, size)

    addComponentListener(object : ComponentAdapter() {
      override fun componentResized(e: ComponentEvent?) {
        contentPanel.bounds = Rectangle(contentPanel.location, size)
        revalidate()
        repaint()
      }

      override fun componentShown(e: ComponentEvent?) {
        contentPanel.bounds = Rectangle(contentPanel.location, size)
        revalidate()
        repaint()
      }
    })

    revalidate()
    repaint()
    addAncestorListener(object : AncestorListenerAdapter() {
      override fun ancestorAdded(event: AncestorEvent?) {
        rootPane?.defaultButton = startLearningButton
      }
    })

    learnIdeFeaturesPanel.addMouseListener(
      object : MouseAdapter() {
        override fun mouseEntered(e: MouseEvent?) {
          if (contentState == ContentState.EXPANDED) return
          learnIdeFeaturesPanel.background = WelcomeScreenPanelColorsAndFonts.HoveredColor
          learnIdeFeaturesPanel.isOpaque = true
          repaint()
          cursor = Cursor(Cursor.HAND_CURSOR);
        }

        override fun mouseExited(e: MouseEvent?) {
          if (e == null) return
          if (Rectangle(Point(learnIdeFeaturesPanel.locationOnScreen.x + 1, learnIdeFeaturesPanel.locationOnScreen.y + 1),
                        Dimension(learnIdeFeaturesPanel.bounds.size.width - 2, learnIdeFeaturesPanel.bounds.size.height - 2)).contains(
              e.locationOnScreen)) return
          learnIdeFeaturesPanel.isOpaque = false
          repaint()
          cursor = Cursor(Cursor.DEFAULT_CURSOR);
        }

        override fun mousePressed(e: MouseEvent?) {
          onLearnIdeFeaturesPanelClick()
        }
      }
    )

    startLearningButton.layout = null
    add(startLearningButton)
    setComponentZOrder(startLearningButton, 0)
    setComponentZOrder(contentPanel, 1)

  }

  private fun modulesPanel(): JPanel {
    val modules = CourseManager.instance.modules
    val panel = JPanel()
    //{ components.sumOf { it.preferredSize.height } }
    panel.isOpaque = false
    panel.layout = BoxLayout(panel, BoxLayout.PAGE_AXIS)

    panel.add(rigid(16, 12))
    for (module in modules) {
      panel.add(moduleHeader(module))
      panel.add(rigid(2, 2))
      panel.add(moduleDescription(module))
      panel.add(rigid(16, 16))
    }

    return panel
  }

  private fun moduleDescription(module: Module): HeightLimitedPane {
    return HeightLimitedPane(module.description ?: "", MODULE_DESCRIPTION)
  }

  private fun moduleHeader(module: Module): HeightLimitedPane {
    return HeightLimitedPane(module.name, MODULE_HEADER)
  }

  private fun initInteractiveCoursePanel() {
    learnIdeFeaturesContent.alignmentY = Component.TOP_ALIGNMENT

    learnIdeFeaturesPanel.layout = BoxLayout(learnIdeFeaturesPanel, BoxLayout.LINE_AXIS)
    learnIdeFeaturesPanel.isOpaque = false
    learnIdeFeaturesPanel.border = EmptyBorder(0, 0, 0, 0)

    val pluginLabel = JLabel(PluginIcon)
    val pluginPanel = JPanel()
    pluginPanel.layout = BorderLayout()
    pluginPanel.border = EmptyBorder(12, 0, 0, 0)
    pluginPanel.isOpaque = false
    pluginPanel.add(pluginLabel, BorderLayout.NORTH)
    pluginPanel.alignmentY = Component.TOP_ALIGNMENT
    pluginPanel.add(
      JPanel().apply { preferredSize = Dimension(pluginPanelWidth, 1); minimumSize = Dimension(pluginPanelWidth, 1); isOpaque = false },
      BorderLayout.CENTER)

    chevronPanel.layout = BorderLayout()
    chevronPanel.isOpaque = false
    chevronPanel.alignmentY = Component.TOP_ALIGNMENT
    chevronPanel.add(chevronLabel, BorderLayout.CENTER)
    chevronPanel.add(
      JPanel().apply { preferredSize = Dimension(chevronPanelWidth, 1); minimumSize = Dimension(chevronPanelWidth, 1); isOpaque = false },
      BorderLayout.NORTH)

    pluginPanel.alignmentY = Component.TOP_ALIGNMENT
    learnIdeFeaturesContent.alignmentY = Component.TOP_ALIGNMENT
    chevronPanel.alignmentY = Component.TOP_ALIGNMENT

    learnIdeFeaturesPanel.add(pluginPanel)
    learnIdeFeaturesPanel.add(learnIdeFeaturesContent)
    learnIdeFeaturesPanel.add(chevronPanel)

    //apply rounded corner
    learnIdeFeaturesPanel.border = roundBorder1px

    //apply fonts
    learnIdeFeaturesPanel.alignmentX = LEFT_ALIGNMENT

    interactiveCoursesPanel.layout = BoxLayout(interactiveCoursesPanel, BoxLayout.PAGE_AXIS)
    interactiveCoursesPanel.isOpaque = false

    interactiveCoursesPanel.add(interactiveCoursesHeader)
    interactiveCoursesPanel.add(rigid(0, 12))
    interactiveCoursesPanel.add(learnIdeFeaturesPanel)

  }

  private fun createLearnIdeFeaturesContent(): JPanel {
    val panel = JPanel()
    panel.layout = BoxLayout(panel, BoxLayout.PAGE_AXIS)
    panel.isOpaque = false

    panel.add(rigid(12, 10))

    val learnIdeFeaturesHeader = JLabel(LearnBundle.message("welcome.tab.header.learn.ide.features"))
    learnIdeFeaturesHeader.font = learnIdeFeaturesHeader.font.deriveFont(Font.BOLD)
    learnIdeFeaturesHeader.alignmentX = LEFT_ALIGNMENT
    panel.add(learnIdeFeaturesHeader)

    panel.add(rigid(1, 4))

    panel.add(learnIdeFeaturesDescription)

    panel.add(rigid(4, 9))

    startLearningButton.setButtonAction()
    startLearningButton.margin = Insets(0, 0, 0, 0)
    startLearningButton.isSelected = true
    startLearningButton.isOpaque = false
    startLearningButton.alignmentX = LEFT_ALIGNMENT

    val buttonPlace = buttonPixelHunting(startLearningButton)
    panel.add(buttonPlace)

    panel.add(rigid(18, 21))

    return panel
  }

  private fun buttonPixelHunting(button: JButton): JPanel {

    val buttonSizeWithoutInsets = Dimension(button.preferredSize.width - button.insets.left - button.insets.right,
                                            button.preferredSize.height - button.insets.top - button.insets.bottom)

    val buttonPlace = JPanel().apply {
      maximumSize = buttonSizeWithoutInsets
      preferredSize = buttonSizeWithoutInsets
      minimumSize = buttonSizeWithoutInsets
      isOpaque = false
      alignmentX = LEFT_ALIGNMENT
    }

    buttonPlace.addComponentListener(object : ComponentAdapter() {
      override fun componentResized(e: ComponentEvent?) {
        adjustButton()
      }

      private fun adjustButton() {
        val adjX = buttonPlace.locationOnScreen.x - locationOnScreen.x
        val adjY = buttonPlace.locationOnScreen.y - locationOnScreen.y
        button.bounds = Rectangle(adjX - button.insets.left, adjY - button.insets.top, button.preferredSize.width,
                                  button.preferredSize.height)
        button.repaint()
      }

      override fun componentMoved(e: ComponentEvent?) {
        adjustButton()
      }

      override fun componentShown(e: ComponentEvent?) {
        adjustButton()
      }
    })
    return buttonPlace
  }

  private fun JButton.setButtonAction() {
    this.action = object : AbstractAction(LearnBundle.message("welcome.tab.start.learning.button")) {
      override fun actionPerformed(e: ActionEvent?) {
        val startLearnAction = StartLearnAction()
        val anActionEvent = AnActionEvent.createFromAnAction(startLearnAction, null, ActionPlaces.WELCOME_SCREEN, DataContext.EMPTY_CONTEXT)
        ActionUtil.performActionDumbAware(startLearnAction, anActionEvent)
      }

    }
  }

  private fun onLearnIdeFeaturesPanelClick() {
    if (contentState == ContentState.COLLAPSED) {
      expandContent()
      chevronLabel.icon = ChevronUp
      contentState = ContentState.EXPANDED
    }
    else {
      collapseContent()
      chevronLabel.icon = ChevronDown
      contentState = ContentState.COLLAPSED
    }
    chevronLabel.repaint()
  }

  private fun expandContent() {
    learnIdeFeaturesPanel.isOpaque = false
    learnIdeFeaturesDescription.maximumSize = learnIdeFeaturesDescription.preferredSize
    //learnIdeFeaturesContent.add(modulesPanel.apply { maximumSize = Dimension(learnIdeFeaturesContent.width, maximumSize.height) })
    learnIdeFeaturesContent.add(modulesPanel)
    chevronPanel.maximumSize = chevronPanel.size
    learnIdeFeaturesContent.revalidate()
    learnIdeFeaturesContent.repaint()
    learnIdeFeaturesPanel.repaint()

  }

  private fun collapseContent() {
    learnIdeFeaturesContent.remove(modulesPanel)
    learnIdeFeaturesContent.revalidate()
    learnIdeFeaturesContent.repaint()
    learnIdeFeaturesPanel.repaint()
  }

  private fun rigid(_width: Int, _height: Int): Component {
    return Box.createRigidArea(
      Dimension(JBUI.scale(_width), JBUI.scale(_height))).apply { (this as JComponent).alignmentX = LEFT_ALIGNMENT }
  }

  private class HeightLimitedPane(text: String, style: SimpleAttributeSet, val _width: Int? = null) : JTextPane() {
    init {
      isEditable = false
      document.insertString(0, text, style)
      //ensure that style has been applied
      styledDocument.setCharacterAttributes(0, text.length, style, true)
      styledDocument.setParagraphAttributes(0, text.length, style, true)
      isOpaque = false
      isEditable = false
      alignmentX = LEFT_ALIGNMENT
    }

    override fun getMaximumSize(): Dimension {
      if (_width == null) {
        return this.preferredSize
      }
      else {
        return Dimension(width, this.preferredSize.height)
      }
    }
  }

  private class HeightLimitedPanel(private val calculateInnerComponentHeight: () -> Int) : JPanel() {
    override fun getMaximumSize(): Dimension {
      return Dimension(this.preferredSize.width, calculateInnerComponentHeight())
    }
  }

}


