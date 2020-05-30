// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package training.ui.views

import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.project.guessCurrentProject
import com.intellij.openapi.util.SystemInfo
import com.intellij.ui.components.labels.LinkLabel
import com.intellij.util.containers.BidirectionalMap
import com.intellij.util.ui.JBUI
import com.intellij.util.ui.UIUtil
import icons.FeaturesTrainerIcons
import training.keymap.KeymapUtil
import training.learn.CourseManager
import training.learn.LearnBundle
import training.learn.interfaces.Lesson
import training.learn.lesson.kimpl.KLesson
import training.ui.LessonMessagePane
import training.ui.Message
import training.ui.UISettings
import training.ui.UiManager
import training.util.LearningLessonsAutoExecutor
import training.util.TrainingMode
import training.util.featureTrainerMode
import java.awt.*
import java.awt.event.ActionEvent
import java.net.URI
import javax.swing.*
import javax.swing.border.EmptyBorder
import javax.swing.border.MatteBorder
import javax.swing.text.BadLocationException

/**
 * @author Sergey Karashevich
 */
class LearnPanel : JPanel() {

  private val boxLayout: BoxLayout = BoxLayout(this, BoxLayout.Y_AXIS)

  //XmlLesson panel items
  private val lessonPanel = JPanel()
  private val moduleNameLabel = JLabel()
  private val allTopicsLabel: LinkLabel<Any> = LinkLabel(LearnBundle.message("learn.ui.alltopics"), null)

  private val lessonNameLabel = JLabel() //Name of the current lesson
  private val lessonMessagePane = LessonMessagePane()
  private val buttonPanel = JPanel()
  private val button = JButton(LearnBundle.message("learn.ui.button.skip"))

  //XmlModule panel stuff
  val modulePanel = ModulePanel()

  //modulePanel UI
  private val lessonPanelBoxLayout = BoxLayout(lessonPanel, BoxLayout.Y_AXIS)

  init {
    layout = boxLayout
    isFocusable = false

    //Obligatory block
    initLessonPanel()
    initModulePanel()

    isOpaque = true
    background = background

    lessonPanel.alignmentX = Component.LEFT_ALIGNMENT
    add(lessonPanel)
    modulePanel.alignmentX = Component.LEFT_ALIGNMENT

    add(modulePanel)

    //set LearnPanel UI
    preferredSize = Dimension(UISettings.instance.width, 100)
    border = UISettings.instance.emptyBorder
  }

  private fun initLessonPanel() {
    lessonPanel.name = "lessonPanel"
    lessonPanel.layout = lessonPanelBoxLayout
    lessonPanel.isFocusable = false
    lessonPanel.isOpaque = false

    moduleNameLabel.name = "moduleNameLabel"
    moduleNameLabel.font = UISettings.instance.moduleNameFont
    moduleNameLabel.isFocusable = false
    moduleNameLabel.border = UISettings.instance.checkmarkShiftBorder

    allTopicsLabel.name = "allTopicsLabel"
    allTopicsLabel.setListener({ _, _ -> UiManager.setModulesView() }, null)

    lessonNameLabel.name = "lessonNameLabel"
    lessonNameLabel.border = UISettings.instance.checkmarkShiftBorder
    lessonNameLabel.font = UISettings.instance.lessonHeaderFont
    lessonNameLabel.alignmentX = Component.LEFT_ALIGNMENT
    lessonNameLabel.isFocusable = false

    lessonMessagePane.name = "lessonMessagePane"
    lessonMessagePane.isFocusable = false
    lessonMessagePane.isOpaque = false
    lessonMessagePane.alignmentX = Component.LEFT_ALIGNMENT
    lessonMessagePane.margin = JBUI.emptyInsets()
    lessonMessagePane.border = EmptyBorder(0, 0, 0, 0)
    lessonMessagePane.maximumSize = Dimension(UISettings.instance.width, 10000)

    //Set Next Button UI
    button.margin = JBUI.emptyInsets()
    button.isFocusable = false
    button.isVisible = true
    button.isEnabled = true
    button.isOpaque = false

    buttonPanel.name = "buttonPanel"
    buttonPanel.border = UISettings.instance.checkmarkShiftBorder
    buttonPanel.isOpaque = false
    buttonPanel.isFocusable = false
    buttonPanel.layout = BoxLayout(buttonPanel, BoxLayout.X_AXIS)
    buttonPanel.alignmentX = Component.LEFT_ALIGNMENT
    buttonPanel.add(button)

    //shift right for checkmark
    lessonPanel.add(moduleNameLabel)
    lessonPanel.add(Box.createVerticalStrut(UISettings.instance.lessonNameGap))
    lessonPanel.add(lessonNameLabel)
    lessonPanel.add(lessonMessagePane)
    lessonPanel.add(Box.createVerticalStrut(UISettings.instance.beforeButtonGap))
    lessonPanel.add(Box.createVerticalGlue())
    lessonPanel.add(buttonPanel)
    lessonPanel.add(Box.createVerticalStrut(UISettings.instance.afterButtonGap))
  }

  fun setLessonName(lessonName: String) {
    lessonNameLabel.text = lessonName
    lessonNameLabel.foreground = UISettings.instance.defaultTextColor
    lessonNameLabel.isFocusable = false
    this.revalidate()
    this.repaint()
  }

  fun setModuleName(moduleName: String) {
    moduleNameLabel.text = moduleName
    moduleNameLabel.foreground = UISettings.instance.defaultTextColor
    moduleNameLabel.isFocusable = false
    this.revalidate()
    this.repaint()
  }

  fun addMessage(text: String) {
    lessonMessagePane.addMessage(text)
  }

  fun addMessages(messages: Array<Message>) {
    for (message in messages) {
      if (message.type == Message.MessageType.LINK) {
        //add link handler
        message.runnable = Runnable {
          val link = message.link
          if (link == null || link.isEmpty()) {
            val lesson = CourseManager.instance.findLesson(message.text)
            if (lesson != null) {
              try {
                val project = guessCurrentProject(this@LearnPanel)
                CourseManager.instance.openLesson(project, lesson)
              }
              catch (e: Exception) {
                LOG.warn(e)
              }

            }
          }
          else {
            val desktop = if (Desktop.isDesktopSupported()) Desktop.getDesktop() else null
            if (desktop != null && desktop.isSupported(Desktop.Action.BROWSE)) {
              try {
                desktop.browse(URI(link))
              }
              catch (e: Exception) {
                LOG.warn(e)
              }
            }
          }
        }
      }
    }

    lessonMessagePane.addMessage(messages)
    adjustMessagesArea()
  }

  private fun adjustMessagesArea() {
    //invoke #getPreferredSize explicitly to update actual size of LessonMessagePane
    lessonMessagePane.preferredSize

    //Pack lesson panel
    lessonPanel.repaint()
    //run to update LessonMessagePane.getMinimumSize and LessonMessagePane.getPreferredSize
    lessonPanelBoxLayout.invalidateLayout(lessonPanel)
    lessonPanelBoxLayout.layoutContainer(lessonPanel)
  }

  fun resetMessagesNumber(number: Int) {
    lessonMessagePane.resetMessagesNumber(number)
    adjustMessagesArea()
  }

  fun messagesNumber(): Int = lessonMessagePane.messagesNumber()

  fun setPreviousMessagesPassed() {
    try {
      lessonMessagePane.passPreviousMessages()
    }
    catch (e: BadLocationException) {
      LOG.warn(e)
    }
  }

  fun setLessonPassed() {
    setButtonToNext()
    this.repaint()
  }

  private fun setButtonToNext() {
    button.isVisible = true
    lessonPanel.revalidate()
    lessonPanel.repaint()
  }

  fun clearLessonPanel() {
    lessonNameLabel.icon = null
    lessonMessagePane.clear()
    //remove links from lessonMessagePane
    val mouseListeners = lessonMessagePane.mouseListeners
    for (mouseListener in mouseListeners) {
      lessonMessagePane.removeMouseListener(mouseListener)
    }
    revalidate()
    repaint()
  }

  @JvmOverloads
  fun setButtonNextAction(runnable: Runnable, notPassedLesson: Lesson?, text: String? = null) {
    val buttonAction = object : AbstractAction() {
      override fun actionPerformed(actionEvent: ActionEvent) {
        runnable.run()
      }
    }
    buttonAction.putValue(Action.NAME, "Next")
    buttonAction.isEnabled = true
    button.action = buttonAction
    val keyStroke = getNextLessonKeyStrokeText()
    button.text = if (text != null) {
      "$text ($keyStroke)"
    }
    else if (notPassedLesson != null) {
      "${LearnBundle.message("learn.ui.button.next.lesson")}: ${notPassedLesson.name} ($keyStroke)"
    }
    else {
      LearnBundle.message("learn.ui.button.next.lesson") + " ($keyStroke)"
    }
    button.isSelected = true
    rootPane?.defaultButton = button
  }

  fun setButtonSkipActionAndText(runnable: Runnable?, text: String?, visible: Boolean) {
    rootPane?.defaultButton = null
    val buttonAction = object : AbstractAction() {
      override fun actionPerformed(actionEvent: ActionEvent) {
        runnable?.run()
      }
    }

    buttonAction.isEnabled = true
    button.action = buttonAction
    val keyStroke = getNextLessonKeyStrokeText()
    if (text == null || text.isEmpty()) {
      button.text = "${LearnBundle.message("learn.ui.button.skip")} ($keyStroke)"
      button.updateUI()
    }
    else {
      button.text = "${LearnBundle.message("learn.ui.button.skip.module")}: $text ($keyStroke)"
      button.updateUI()
    }
    button.isVisible = false
    button.isSelected = true
    button.isVisible = visible
  }

  private fun getNextLessonKeyStrokeText() =
    KeymapUtil.getKeyStrokeText(KeymapUtil.getShortcutByActionId("learn.next.lesson"))

  fun hideNextButton() {
    button.isVisible = false
  }

  private fun initModulePanel() {
    modulePanel.name = LearnPanel::modulePanel.name
    modulePanel.layout = BoxLayout(modulePanel, BoxLayout.Y_AXIS)
    modulePanel.isFocusable = false
    modulePanel.isOpaque = false

    //define separator
    modulePanel.border = MatteBorder(1, 0, 0, 0, UISettings.instance.separatorColor)
  }

  fun clear() {
    clearLessonPanel()
    //clearModulePanel
    modulePanel.removeAll()
  }

  fun updateButtonUi() {
    button.updateUI()
  }

  inner class ModulePanel : JPanel() {
    private val lessonLabelMap = BidirectionalMap<Lesson, MyLinkLabel>()
    private val moduleNamePanel = JPanel() //contains moduleNameLabel and allTopicsLabel

    fun init(lesson: Lesson) {
      val module = lesson.module
      val myLessons = module.lessons

      //create ModuleLessons region
      val moduleLessons = JLabel()
      moduleLessons.name = "moduleLessons"

      moduleNamePanel.name = "moduleNamePanel"
      moduleNamePanel.border = EmptyBorder(UISettings.instance.lessonGap, UISettings.instance.checkIndent, 0, 0)
      moduleNamePanel.isOpaque = false
      moduleNamePanel.isFocusable = false
      moduleNamePanel.layout = BoxLayout(moduleNamePanel, BoxLayout.X_AXIS)
      moduleNamePanel.alignmentX = Component.LEFT_ALIGNMENT
      moduleNamePanel.removeAll()
      moduleNamePanel.add(moduleLessons)
      moduleNamePanel.add(Box.createHorizontalStrut(20))
      moduleNamePanel.add(Box.createHorizontalGlue())
      moduleNamePanel.add(allTopicsLabel)

      if (featureTrainerMode == TrainingMode.DEVELOPMENT && lesson is KLesson) {
        moduleNamePanel.add(JButton().apply {
          action = object : AbstractAction() {
            override fun actionPerformed(actionEvent: ActionEvent) {
              LearningLessonsAutoExecutor.runSingleLesson(guessCurrentProject(moduleNamePanel), lesson)
            }
          }
          margin = JBUI.emptyInsets()
          isFocusable = false
          isVisible = true
          isSelected = false
          isEnabled = true
          isOpaque = false
          text = "Run"
        })
      }

      moduleLessons.text = lesson.module.name
      moduleLessons.font = UISettings.instance.boldFont
      moduleLessons.isFocusable = false

      add(UISettings.rigidGap(UISettings::moduleNameSeparatorGap))
      add(moduleNamePanel)
      add(UISettings.rigidGap(UISettings::moduleNameLessonsGap))

      buildLessonLabels(lesson, myLessons)
      maximumSize = Dimension(UISettings.instance.width, modulePanel.preferredSize.height)
    }

    private fun buildLessonLabels(lesson: Lesson, myLessons: List<Lesson>) {
      lessonLabelMap.clear()
      for (currentLesson in myLessons) {
        val lessonName = currentLesson.name

        val lessonLinkLabel = MyLinkLabel(lessonName)
        lessonLinkLabel.horizontalTextPosition = SwingConstants.LEFT
        lessonLinkLabel.border = EmptyBorder(0, UISettings.instance.checkIndent, UISettings.instance.lessonGap, 0)
        lessonLinkLabel.isFocusable = false
        lessonLinkLabel.setListener({ _, _ ->
                                      try {
                                        val project = guessCurrentProject(this@LearnPanel)
                                        CourseManager.instance.openLesson(project, currentLesson)
                                      }
                                      catch (e1: Exception) {
                                        LOG.warn(e1)
                                      }
                                    }, null)

        if (lesson == currentLesson) {
          //selected lesson
          lessonLinkLabel.setTextColor(UISettings.instance.lessonActiveColor)
        }
        else {
          lessonLinkLabel.resetTextColor()
        }
        lessonLinkLabel.cursor = Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)
        lessonLabelMap[currentLesson] = lessonLinkLabel
        add(lessonLinkLabel)
      }
    }

    fun updateLessons(lesson: Lesson) {
      for ((curLesson, lessonLabel) in lessonLabelMap.entries) {
        if (lesson == curLesson) {
          lessonLabel.setTextColor(UISettings.instance.lessonActiveColor)
        }
        else {
          lessonLabel.resetTextColor()
        }
      }
    }

    override fun paintComponent(g: Graphics) {
      super.paintComponent(g)
      paintModuleCheckmarks(g)
    }

    private fun paintModuleCheckmarks(g: Graphics) {
      for ((lesson, jLabel) in lessonLabelMap.entries) {
        if (lesson.passed) {
          val point = jLabel.location
          if (!SystemInfo.isMac) {
            FeaturesTrainerIcons.Checkmark.paintIcon(this, g, point.x, point.y + 1)
          }
          else {
            FeaturesTrainerIcons.Checkmark.paintIcon(this, g, point.x, point.y + 2)
          }
        }
      }
    }


    internal inner class MyLinkLabel(text: String) : LinkLabel<Any>(text, null) {

      private var userTextColor: Color? = null

      override fun getTextColor(): Color {
        return userTextColor ?: super.getTextColor()
      }

      fun setTextColor(color: Color) {
        userTextColor = color
      }

      fun resetTextColor() {
        userTextColor = null
      }
    }
  }

  fun hasNextButton(): Boolean = button.isEnabled && button.isVisible

  fun clickButton() {
    if (hasNextButton()) button.doClick()
  }

  override fun getPreferredSize(): Dimension {
    if (lessonPanel.minimumSize == null) return Dimension(10, 10)
    return if (modulePanel.minimumSize == null) Dimension(10, 10)
    else Dimension(
      lessonPanel.minimumSize.getWidth().toInt() +
      UISettings.instance.westInset +
      UISettings.instance.eastInset,
      lessonPanel.minimumSize.getHeight().toInt() +
      modulePanel.minimumSize.getHeight().toInt() +
      UISettings.instance.northInset +
      UISettings.instance.southInset)
  }

  override fun getBackground(): Color {
    return if (!UIUtil.isUnderDarcula())
      UISettings.instance.backgroundColor
    else
      UIUtil.getPanelBackground()
  }

  companion object {
    private val LOG = Logger.getInstance(LearnPanel::class.java)
  }
}
