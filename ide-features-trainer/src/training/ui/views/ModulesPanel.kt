package training.ui.views

import com.intellij.icons.AllIcons
import com.intellij.openapi.actionSystem.ActionToolbar.NAVBAR_MINIMUM_BUTTON_SIZE
import com.intellij.openapi.actionSystem.Presentation
import com.intellij.openapi.actionSystem.impl.ActionButton
import com.intellij.openapi.project.DumbService
import com.intellij.openapi.project.guessCurrentProject
import com.intellij.openapi.util.SystemInfo
import com.intellij.ui.JBColor
import com.intellij.ui.components.JBLabel
import com.intellij.ui.components.labels.LinkLabel
import com.intellij.util.containers.BidirectionalMap
import com.intellij.util.ui.UIUtil
import training.commands.kotlin.TaskContext
import training.learn.CourseManager
import training.learn.LearnBundle
import training.learn.interfaces.Module
import training.learn.lesson.LessonStateManager
import training.ui.LearnIcons
import training.ui.LearnToolWindow
import training.ui.UISettings
import training.ui.UiManager
import training.util.*
import java.awt.*
import java.awt.event.ActionEvent
import java.awt.event.MouseEvent
import java.awt.event.MouseListener
import javax.swing.*
import javax.swing.border.EmptyBorder
import javax.swing.text.BadLocationException
import javax.swing.text.SimpleAttributeSet
import javax.swing.text.StyleConstants

class ModulesPanel(val learnToolWindow: LearnToolWindow) : JPanel() {

    private var modulesPanel: JPanel = JPanel()
    private val module2linklabel: BidirectionalMap<Module, LinkLabel<Any>> = BidirectionalMap()

    init {
        layout = BoxLayout(this, BoxLayout.Y_AXIS)
        isFocusable = false
        isOpaque = true
        background = background

        //Obligatory block
        setupFontStyles()
        initMainPanel()
        add(createSettingsButtonPanel())
        add(modulesPanel)
        add(Box.createVerticalGlue())

        //set LearnPanel UI
        this.preferredSize = Dimension(UISettings.instance.width, 100)
        this.border = UISettings.instance.emptyBorderWithNoEastHalfNorth

        revalidate()
        repaint()
    }


    private fun setupFontStyles() {
        StyleConstants.setFontFamily(REGULAR, UISettings.instance.fontFace)
        StyleConstants.setFontSize(REGULAR, UISettings.instance.fontSize)
        StyleConstants.setForeground(REGULAR, UISettings.instance.descriptionColor)

        StyleConstants.setLeftIndent(PARAGRAPH_STYLE, 0.0f)
        StyleConstants.setRightIndent(PARAGRAPH_STYLE, 0f)
        StyleConstants.setSpaceAbove(PARAGRAPH_STYLE, 0.0f)
        StyleConstants.setSpaceBelow(PARAGRAPH_STYLE, 0.0f)
        StyleConstants.setLineSpacing(PARAGRAPH_STYLE, 0.0f)
    }


    private fun initMainPanel() {
        modulesPanel.apply {
            name = "modulesPanel"
            layout = BoxLayout(this, BoxLayout.PAGE_AXIS)
            border = UISettings.instance.eastBorder
            isOpaque = false
            isFocusable = false
        }
        initModulesPanel()
    }

    private fun initModulesPanel() {
        val modules = CourseManager.instance.modules
        if(DataLoader.liveMode) {
            CourseManager.instance.clearModules()
            CourseManager.instance.initXmlModules()
            module2linklabel.clear()
        }

        if (featureTrainerMode.doesShowResetButton) {
            addResetButton()
        }

        if (featureTrainerMode == TrainingMode.DEVELOPMENT) {
            addDevelopmentTools()
        }

        for (module in modules) {
            if (module.lessons.isEmpty()) continue
            val moduleHeader = JPanel().apply {
                name = "moduleHeader"
                isFocusable = false
                alignmentX = Component.LEFT_ALIGNMENT
                border = UISettings.instance.checkmarkShiftBorder
                isOpaque = false
                layout = BoxLayout(this, BoxLayout.X_AXIS)
            }
            val moduleName = LinkLabel<Any>(module.name, null)
            moduleName.name = "moduleName"
            module2linklabel[module] = moduleName
            moduleName.setListener({ _, _ ->
                val project = guessCurrentProject(modulesPanel)
                val dumbService = DumbService.getInstance(project)
                if (dumbService.isDumb) {
                    val balloon = createBalloon(LearnBundle.message("indexing.message"))
                    balloon.showInCenterOf(module2linklabel[module])
                    return@setListener
                }
                try {
                    var lesson = module.giveNotPassedLesson()
                    if (lesson == null) lesson = module.lessons[0]
                    CourseManager.instance.openLesson(project, lesson)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }, null)
            moduleName.font = UISettings.instance.moduleNameFont
            moduleName.alignmentY = Component.BOTTOM_ALIGNMENT
            moduleName.alignmentX = Component.LEFT_ALIGNMENT
            val progressStr = calcProgress(module)
            val progressLabel: JBLabel
            progressLabel = if (progressStr != null) {
                JBLabel(progressStr)
            } else {
                JBLabel()
            }
            progressLabel.border = EmptyBorder(0, 5, 0, 5)
            progressLabel.name = "progressLabel"
            progressLabel.font = UISettings.instance.italicFont
            progressLabel.foreground = JBColor.BLACK
            progressLabel.alignmentY = Component.BOTTOM_ALIGNMENT
            moduleHeader.add(moduleName)
            moduleHeader.add(UISettings.rigidGap(UISettings::progressGap, isVertical = false))
            moduleHeader.add(progressLabel)

            val descriptionPane = MyJTextPane(UISettings.instance.width)
            descriptionPane.name = "descriptionPane"
            descriptionPane.isEditable = false
            descriptionPane.isOpaque = false
            descriptionPane.setParagraphAttributes(PARAGRAPH_STYLE, true)
            try {
                val descriptionStr = module.description
                descriptionPane.document.insertString(0, descriptionStr, REGULAR)
            } catch (e: BadLocationException) {
                e.printStackTrace()
            }

            descriptionPane.alignmentX = Component.LEFT_ALIGNMENT
            descriptionPane.margin = Insets(0, 0, 0, 0)
            descriptionPane.border = UISettings.instance.checkmarkShiftBorder
            descriptionPane.addMouseListener(delegateToLinkLabel(descriptionPane, moduleName))

            modulesPanel.add(moduleHeader)
            modulesPanel.add(Box.createVerticalStrut(UISettings.instance.headerGap))
            modulesPanel.add(descriptionPane)
            modulesPanel.add(Box.createVerticalStrut(UISettings.instance.moduleGap))
        }
        modulesPanel.add(Box.createVerticalGlue())
    }

    private fun createSettingsButtonPanel(): JPanel {
        val settingsAction = createAnAction(AllIcons.General.Settings) { _ -> UiManager.setLanguageChooserView() }
        val settingsButton = ActionButton(settingsAction,
            Presentation("Settings").apply {
                icon = AllIcons.Nodes.Editorconfig
                isEnabled = true
            },
            "learn.tool.window.module", NAVBAR_MINIMUM_BUTTON_SIZE)
            .apply {
                minimumSize = NAVBAR_MINIMUM_BUTTON_SIZE
                preferredSize = NAVBAR_MINIMUM_BUTTON_SIZE
                maximumSize = NAVBAR_MINIMUM_BUTTON_SIZE
                alignmentX = Component.RIGHT_ALIGNMENT
                isOpaque = false
                isEnabled = true
            }

        return JPanel().apply {
            name = "settingsButtonPanel"
            isFocusable = false
            alignmentX = Component.LEFT_ALIGNMENT
            border = UISettings.instance.smallEastBorder
            isOpaque = false
            layout = BoxLayout(this, BoxLayout.X_AXIS)
            add(Box.createHorizontalGlue())
            add(settingsButton)
        }
    }

    private fun addDevelopmentTools() {
        modulesPanel.add(JCheckBox().apply {
            addItemListener { e -> TaskContext.inTestMode = e.stateChange == 1 }
            isFocusable = true
            isVisible = true
            isSelected = true
            isEnabled = true
            isOpaque = false
            model.isSelected = false
            text = "Run in test mode"
        })

      modulesPanel.add(JButton().apply {
        action = object : AbstractAction() {
          override fun actionPerformed(actionEvent: ActionEvent) {
            learnToolWindow.changeLanguage()
          }
        }
        margin = Insets(0, 0, 0, 0)
        isFocusable = true
        isVisible = true
        isSelected = true
        isEnabled = true
        isOpaque = false
        text = "Change language"
      })
    }

    private fun addResetButton() {
        val modules = CourseManager.instance.modules
        modulesPanel.add(JButton().apply {
            action = object : AbstractAction() {
                override fun actionPerformed(actionEvent: ActionEvent) {
                    LessonStateManager.resetPassedStatus()
                    modules.forEach { module ->
                        module.lessons.forEach { lesson ->
                            lesson.passed = false
                        }
                    }
                    val project = guessCurrentProject(modulesPanel)
                    val firstLesson = modules.first().lessons.first()
                    CourseManager.instance.openLesson(project, firstLesson)
                    UiManager.setLessonView()
                }
            }
            margin = Insets(0, 0, 0, 0)
            isFocusable = true
            isVisible = true
            isSelected = true
            isEnabled = true
            isOpaque = false
            text = "Start from the Beginning"
        })
    }

    private fun delegateToLinkLabel(descriptionPane: MyJTextPane, moduleName: LinkLabel<*>): MouseListener {
        return object : MouseListener {
            override fun mouseClicked(e: MouseEvent) {
                moduleName.doClick()
            }

            override fun mousePressed(e: MouseEvent) {
                moduleName.doClick()
            }

            override fun mouseReleased(e: MouseEvent) {

            }

            override fun mouseEntered(e: MouseEvent) {
                moduleName.entered(e)
                descriptionPane.cursor = Cursor.getPredefinedCursor(12)
            }

            override fun mouseExited(e: MouseEvent) {
                moduleName.exited(e)
                descriptionPane.cursor = Cursor.getDefaultCursor()
            }
        }
    }

    fun updateMainPanel() {
        modulesPanel.removeAll()
        initModulesPanel()
    }

    private fun calcProgress(module: Module): String? {
        val total = module.lessons.size
        var done = 0
        for (lesson in module.lessons) {
            if (lesson.passed) done++
        }
        return if (done != 0) {
            if (done == total)
                ""
            else
                done.toString() + " of " + total + " done"
        } else {
            null
        }
    }

    private inner class MyJTextPane internal constructor(widthOfText: Int) : JTextPane() {

        private var myWidth = 314

        init {
            myWidth = widthOfText
        }

        override fun getPreferredSize(): Dimension {
            return Dimension(myWidth, super.getPreferredSize().height)
        }

        override fun getMaximumSize(): Dimension {
            return preferredSize
        }
    }

    override fun getPreferredSize(): Dimension {
        return Dimension(modulesPanel.minimumSize.getWidth().toInt() + (UISettings.instance.westInset + UISettings.instance.westInset),
                modulesPanel.minimumSize.getHeight().toInt() + (UISettings.instance.northInset + UISettings.instance.southInset))
    }

    override fun paintComponent(g: Graphics) {
        super.paintComponent(g)
        paintModuleCheckmarks(g)
    }

    private fun paintModuleCheckmarks(g: Graphics) {
        if (module2linklabel.isNotEmpty()) {
            for (module in module2linklabel.keys) {
                if (module.giveNotPassedLesson() == null) {
                    val linkLabel = module2linklabel[module]
                    val point = linkLabel!!.locationOnScreen
                    val basePoint = this.locationOnScreen
                    val y = point.y + 1 - basePoint.y
                    if (!SystemInfo.isMac) {
                        LearnIcons.checkMarkGray.paintIcon(this, g, UISettings.instance.westInset, y + 4)
                    } else {
                        LearnIcons.checkMarkGray.paintIcon(this, g, UISettings.instance.westInset, y + 2)
                    }
                }
            }
        }
    }


    override fun getBackground(): Color {
        return if (!UIUtil.isUnderDarcula())
            UISettings.instance.backgroundColor
        else
            UIUtil.getPanelBackground()
    }

    companion object {

        private val REGULAR = SimpleAttributeSet()
        private val PARAGRAPH_STYLE = SimpleAttributeSet()
    }

}