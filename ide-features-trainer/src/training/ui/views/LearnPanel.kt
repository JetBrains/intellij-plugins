package training.ui.views

import com.intellij.openapi.project.guessCurrentProject
import com.intellij.openapi.util.SystemInfo
import com.intellij.ui.components.labels.LinkLabel
import com.intellij.util.containers.BidirectionalMap
import com.intellij.util.ui.UIUtil
import training.learn.CourseManager
import training.learn.LearnBundle
import training.learn.interfaces.Lesson
import training.ui.*
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
    private var lessonPanel: JPanel? = null
    private var moduleNameLabel: JLabel? = null
    private var allTopicsLabel: LinkLabel<Any>? = null

    private var lessonNameLabel: JLabel? = null //Name of the current lesson
    private var lessonMessagePane: LessonMessagePane? = null
    private var buttonPanel: JPanel? = null
    private var button: JButton? = null


    //XmlModule panel stuff
    var modulePanel: ModulePanel? = null
        private set
    private var moduleNamePanel: JPanel? = null //contains moduleNameLabel and allTopicsLabel

    //modulePanel UI
    private var lessonPanelBoxLayout: BoxLayout? = null

    init {
        layout = boxLayout
        isFocusable = false

        //Obligatory block
        initLessonPanel()
        initModulePanel()

        isOpaque = true
        background = background

        lessonPanel!!.alignmentX = Component.LEFT_ALIGNMENT
        add(lessonPanel)
        modulePanel!!.alignmentX = Component.LEFT_ALIGNMENT

        add(modulePanel)

        //set LearnPanel UI
        preferredSize = Dimension(UISettings.instance.width, 100)
        border = UISettings.instance.emptyBorder
    }

    private fun initLessonPanel() {
        lessonPanel = JPanel()
        lessonPanel!!.name = "lessonPanel"
        lessonPanelBoxLayout = BoxLayout(lessonPanel, BoxLayout.Y_AXIS)
        lessonPanel!!.layout = lessonPanelBoxLayout
        lessonPanel!!.isFocusable = false
        lessonPanel!!.isOpaque = false

        moduleNameLabel = JLabel()
        moduleNameLabel!!.name = "moduleNameLabel"
        moduleNameLabel!!.font = UISettings.instance.plainFont
        moduleNameLabel!!.isFocusable = false
        moduleNameLabel!!.border = UISettings.instance.checkmarkShiftBorder

        allTopicsLabel = LinkLabel(LearnBundle.message("learn.ui.alltopics"), null)
        allTopicsLabel!!.name = "allTopicsLabel"
        allTopicsLabel!!.setListener({ _, _ -> UiManager.setModulesView() }, null)

        lessonNameLabel = JLabel()
        lessonNameLabel!!.name = "lessonNameLabel"
        lessonNameLabel!!.border = UISettings.instance.checkmarkShiftBorder
        lessonNameLabel!!.font = UISettings.instance.lessonHeaderFont
        lessonNameLabel!!.alignmentX = Component.LEFT_ALIGNMENT
        lessonNameLabel!!.isFocusable = false

        lessonMessagePane = LessonMessagePane()
        lessonMessagePane!!.name = "lessonMessagePane"
        lessonMessagePane!!.isFocusable = false
        lessonMessagePane!!.isOpaque = false
        lessonMessagePane!!.alignmentX = Component.LEFT_ALIGNMENT
        lessonMessagePane!!.margin = Insets(0, 0, 0, 0)
        lessonMessagePane!!.border = EmptyBorder(0, 0, 0, 0)
        lessonMessagePane!!.maximumSize = Dimension(UISettings.instance.width, 10000)

        //Set Next Button UI
        button = JButton(LearnBundle.message("learn.ui.button.skip"))
        button!!.margin = Insets(0, 0, 0, 0)
        button!!.isFocusable = false
        button!!.isVisible = true
        button!!.isEnabled = true
        button!!.isOpaque = false

        buttonPanel = JPanel()
        buttonPanel!!.name = "buttonPanel"
        buttonPanel!!.border = UISettings.instance.checkmarkShiftBorder
        buttonPanel!!.isOpaque = false
        buttonPanel!!.isFocusable = false
        buttonPanel!!.layout = BoxLayout(buttonPanel, BoxLayout.X_AXIS)
        buttonPanel!!.alignmentX = Component.LEFT_ALIGNMENT
        buttonPanel!!.add(button)

        //shift right for checkmark
        lessonPanel!!.add(moduleNameLabel)
        lessonPanel!!.add(Box.createVerticalStrut(UISettings.instance.lessonNameGap))
        lessonPanel!!.add(lessonNameLabel)
        lessonPanel!!.add(lessonMessagePane)
        lessonPanel!!.add(Box.createVerticalStrut(UISettings.instance.beforeButtonGap))
        lessonPanel!!.add(Box.createVerticalGlue())
        lessonPanel!!.add(buttonPanel)
        lessonPanel!!.add(Box.createVerticalStrut(UISettings.instance.afterButtonGap))
    }


    fun setLessonName(lessonName: String) {
        lessonNameLabel!!.text = lessonName
        lessonNameLabel!!.foreground = UISettings.instance.defaultTextColor
        lessonNameLabel!!.isFocusable = false
        this.revalidate()
        this.repaint()
    }

    fun setModuleName(moduleName: String) {
        moduleNameLabel!!.text = moduleName
        moduleNameLabel!!.foreground = UISettings.instance.defaultTextColor
        moduleNameLabel!!.isFocusable = false
        this.revalidate()
        this.repaint()
    }


    fun addMessage(text: String) {
        lessonMessagePane!!.addMessage(text)
    }

    fun addMessages(messages: Array<Message>) {

        for (message in messages) {
            if (message.type == Message.MessageType.LINK) {
                //add link handler
                message.setRunnable {
                    if(message.link.isNullOrEmpty()) {
                        val lesson = CourseManager.instance.findLesson(message.text)
                        if (lesson != null) {
                            try {
                                val project = guessCurrentProject(this@LearnPanel)
                                CourseManager.instance.openLesson(project, lesson)
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }

                        }
                    }else{
                        val desktop = if (Desktop.isDesktopSupported()) Desktop.getDesktop() else null
                        if (desktop != null && desktop.isSupported(Desktop.Action.BROWSE)) {
                            try {
                                desktop.browse(URI(message.link))
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }
                        }
                    }
                }
            }
        }

        lessonMessagePane!!.addMessage(messages)
        lessonMessagePane!!.invalidate()
        lessonMessagePane!!.repaint()

        //Pack lesson panel
        this.invalidate()
        this.repaint()
        lessonPanel!!.revalidate()
        lessonPanel!!.repaint()
        //run to update LessonMessagePane.getMinimumSize and LessonMessagePane.getPreferredSize
        lessonPanelBoxLayout!!.invalidateLayout(lessonPanel)
        lessonPanelBoxLayout!!.layoutContainer(lessonPanel!!)
    }

    fun setPreviousMessagesPassed() {
        try {
            lessonMessagePane!!.passPreviousMessages()
        } catch (e: BadLocationException) {
            e.printStackTrace()
        }

    }

    fun setLessonPassed() {

        //        lessonNameLabel.setForeground(lessonPassedColor);
        setButtonToNext()
        this.repaint()
    }

    private fun setButtonToNext() {
        button!!.isVisible = true
        lessonPanel!!.revalidate()
        lessonPanel!!.repaint()
        //        button.requestFocus(true); focus requesting is danger here, may interfere with windows like File Structure
    }


    fun hideButtons() {
        if (button!!.isVisible) button!!.isVisible = false
        this.repaint()
    }

    fun clearLessonPanel() {
        //        while (messages.size() > 0){
        //            lessonMessageContainer.remove(messages.get(0).getPanel());
        //            messages.remove(0);
        //        }
        //        lessonMessageContainer.removeAll();
        lessonNameLabel!!.icon = null
        lessonMessagePane!!.clear()
        //remove links from lessonMessagePane
        val mouseListeners = lessonMessagePane!!.mouseListeners
        for (mouseListener in mouseListeners) {
            lessonMessagePane!!.removeMouseListener(mouseListener)
        }
        //        messages.clear();
        this.revalidate()
        this.repaint()
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
        button!!.action = buttonAction
        if (notPassedLesson != null) {
            if (text != null) {
                button!!.text = text
            } else {
                button!!.text = LearnBundle.message("learn.ui.button.next.lesson") + ": " + notPassedLesson.name
            }
        } else {
            button!!.text = LearnBundle.message("learn.ui.button.next.lesson")
        }
        button!!.isSelected = true
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
        button!!.action = buttonAction
        if (text == null || text.isEmpty()) {
            button!!.text = LearnBundle.message("learn.ui.button.skip")
            button!!.updateUI()
        } else {
            button!!.text = LearnBundle.message("learn.ui.button.skip.module") + " " + text
            button!!.updateUI()
        }
        button!!.isVisible = false
        button!!.isSelected = true
        button!!.isVisible = visible
    }


    fun hideNextButton() {
        button!!.isVisible = false
    }

    private fun initModulePanel() {
        modulePanel = ModulePanel()
        modulePanel!!.name = LearnPanel::modulePanel.name
        modulePanel!!.layout = BoxLayout(modulePanel, BoxLayout.Y_AXIS)
        modulePanel!!.isFocusable = false
        modulePanel!!.isOpaque = false

        //define separator
        modulePanel!!.border = MatteBorder(1, 0, 0, 0, UISettings.instance.separatorColor)
    }

    fun clear() {
        clearLessonPanel()
        //clearModulePanel
        modulePanel!!.removeAll()
    }

    fun updateButtonUi() {
        button!!.updateUI()
    }

    inner class ModulePanel : JPanel() {
        private val lessonLabelMap = BidirectionalMap<Lesson, MyLinkLabel>()

        fun init(lesson: Lesson) {
            initModuleLessons(lesson)
        }

        private fun initModuleLessons(lesson: Lesson?) {
            if (lesson == null) return
            val module = lesson.module
            val myLessons = module.lessons

            //create ModuleLessons region
            val moduleLessons = JLabel()
            moduleLessons.name = "moduleLessons"

            moduleNamePanel = JPanel()
            moduleNamePanel!!.name = "moduleNamePanel"
            moduleNamePanel!!.border = EmptyBorder(UISettings.instance.lessonGap, UISettings.instance.checkIndent, 0, 0)
            moduleNamePanel!!.isOpaque = false
            moduleNamePanel!!.isFocusable = false
            moduleNamePanel!!.layout = BoxLayout(moduleNamePanel, BoxLayout.X_AXIS)
            moduleNamePanel!!.alignmentX = Component.LEFT_ALIGNMENT
            moduleNamePanel!!.add(moduleLessons)
            moduleNamePanel!!.add(Box.createHorizontalStrut(20))
            moduleNamePanel!!.add(Box.createHorizontalGlue())
            moduleNamePanel!!.add(allTopicsLabel)

            moduleLessons.text = lesson.module.name
            moduleLessons.font = UISettings.instance.boldFont
            moduleLessons.isFocusable = false

            add(UISettings.rigidGap(UISettings::moduleNameSeparatorGap))
            add(moduleNamePanel)
            add(UISettings.rigidGap(UISettings::moduleNameLessonsGap))

            buildLessonLabels(lesson, myLessons)
            maximumSize = Dimension(UISettings.instance.width, modulePanel!!.preferredSize.height)
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
                    } catch (e1: Exception) {
                        e1.printStackTrace()
                    }
                }, null)

                if (lesson == currentLesson) {
                    //selected lesson
                    lessonLinkLabel.setTextColor(UISettings.instance.lessonActiveColor)
                } else {
                    lessonLinkLabel.resetTextColor()
                }
                lessonLinkLabel.cursor = Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)
                lessonLabelMap[currentLesson] = lessonLinkLabel
                add(lessonLinkLabel)
            }
        }

        fun updateLessons(lesson: Lesson) {
            for (curLesson in lessonLabelMap.keys) {
                val lessonLabel = lessonLabelMap[curLesson]
                if (lesson == curLesson) {
                    lessonLabel!!.setTextColor(UISettings.instance.lessonActiveColor)
                } else {
                    lessonLabel!!.resetTextColor()
                }
            }
        }

        override fun paintComponent(g: Graphics) {
            super.paintComponent(g)
            paintModuleCheckmarks(g)
        }

        private fun paintModuleCheckmarks(g: Graphics) {
            for (lesson in lessonLabelMap.keys) {
                if (lesson.passed) {
                    val jLabel: MyLinkLabel = lessonLabelMap[lesson]!!
                    val point = jLabel.location
                    if (!SystemInfo.isMac) {
                        LearnIcons.checkMarkGray.paintIcon(this, g, point.x, point.y + 1)
                    } else {
                        LearnIcons.checkMarkGray.paintIcon(this, g, point.x, point.y + 2)
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

    fun clickButton() {
        if (button != null && button!!.isEnabled && button!!.isVisible) button!!.doClick()
    }

    override fun getPreferredSize(): Dimension {
        if (lessonPanel!!.minimumSize == null) return Dimension(10, 10)
        return if (modulePanel!!.minimumSize == null) Dimension(10, 10) else Dimension(
                lessonPanel!!.minimumSize.getWidth().toInt() +
                        UISettings.instance.westInset +
                        UISettings.instance.eastInset,
                lessonPanel!!.minimumSize.getHeight().toInt() +
                        modulePanel!!.minimumSize.getHeight().toInt() +
                        UISettings.instance.northInset +
                        UISettings.instance.southInset)
    }

    override fun getBackground(): Color {
        return if (!UIUtil.isUnderDarcula())
            UISettings.instance.backgroundColor
        else
            UIUtil.getPanelBackground()
    }
}
