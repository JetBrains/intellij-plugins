package training.ui.views

import com.intellij.lang.Language
import com.intellij.lang.LanguageExtensionPoint
import com.intellij.openapi.actionSystem.ActionManager
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.DataContext
import com.intellij.openapi.actionSystem.ex.ActionUtil
import com.intellij.util.containers.HashMap
import com.intellij.util.ui.UIUtil
import training.lang.LangManager
import training.lang.LangSupport
import training.learn.BundlePlace
import training.learn.CourseManager
import training.learn.LearnBundle
import training.learn.lesson.LessonStateManager
import training.ui.UISettings
import training.ui.UiManager
import java.awt.Color
import java.awt.Component
import java.awt.Dimension
import java.awt.Insets
import java.awt.event.ActionEvent
import javax.swing.*
import javax.swing.BoxLayout
import javax.swing.border.EmptyBorder
import javax.swing.text.BadLocationException
import javax.swing.text.SimpleAttributeSet
import javax.swing.text.StyleConstants


/**
* @author Sergey Karashevich
*/

sealed class LanguageChoosePanelPlace(bundleAppendix: String) : BundlePlace(bundleAppendix) {
    object WELCOME_SCREEN : LanguageChoosePanelPlace("")
    object TOOL_WINDOW : LanguageChoosePanelPlace(".tool.window")
}

class LanguageChoosePanel(opaque: Boolean = true, private val addButton: Boolean = true, val place: LanguageChoosePanelPlace = LanguageChoosePanelPlace.WELCOME_SCREEN ) : JPanel() {

    private var caption: JLabel? = null
    private var description: MyJTextPane? = null

    private var mainPanel: JPanel? = null

    private val myRadioButtonMap = HashMap<JRadioButton, LanguageExtensionPoint<LangSupport>>()
    private val buttonGroup = ButtonGroup()

    init {
        layout = BoxLayout(this, BoxLayout.Y_AXIS)
        isFocusable = false

        init()
        isOpaque = opaque
        background = background
        initMainPanel()
        add(mainPanel)

        //set LearnPanel UI
        this.preferredSize = Dimension(UISettings.instance.width, 100)
        this.border = UISettings.instance.emptyBorder

        revalidate()
        repaint()
    }


    private fun init() {

        caption = JLabel()
        caption!!.isOpaque = false
        caption!!.font = UISettings.instance.moduleNameFont

        description = MyJTextPane(UISettings.instance.width)
        description!!.isOpaque = false
        description!!.isEditable = false
        description!!.alignmentX = Component.LEFT_ALIGNMENT
        description!!.margin = Insets(0, 0, 0, 0)
        description!!.border = EmptyBorder(0, 0, 0, 0)

        StyleConstants.setFontFamily(REGULAR, UISettings.instance.plainFont.family)
        StyleConstants.setFontSize(REGULAR, UISettings.instance.fontSize)
        StyleConstants.setForeground(REGULAR, UISettings.instance.questionColor)

        StyleConstants.setFontFamily(REGULAR_GRAY, UISettings.instance.plainFont.family)
        StyleConstants.setFontSize(REGULAR_GRAY, UISettings.instance.fontSize)
        StyleConstants.setForeground(REGULAR_GRAY, UISettings.instance.descriptionColor)

        StyleConstants.setLeftIndent(PARAGRAPH_STYLE, 0.0f)
        StyleConstants.setRightIndent(PARAGRAPH_STYLE, 0f)
        StyleConstants.setSpaceAbove(PARAGRAPH_STYLE, 0.0f)
        StyleConstants.setSpaceBelow(PARAGRAPH_STYLE, 0.0f)
        StyleConstants.setLineSpacing(PARAGRAPH_STYLE, 0.0f)
    }

    private fun createLearnButton(): JButton {
        val button = JButton()
        button.action = object : AbstractAction() {
            override fun actionPerformed(e: ActionEvent) {
                button.isEnabled = false
            }
        }
        button.isOpaque = false
        button.action = object : AbstractAction(LearnBundle.messageInPlace("learn.choose.language.button", place)) {
            override fun actionPerformed(e: ActionEvent?) {
                val activeLangSupport = getActiveLangSupport()
                LangManager.getInstance().updateLangSupport(activeLangSupport)

                val action = ActionManager.getInstance().getAction("learn.open.lesson")
                val context = DataContext.EMPTY_CONTEXT
                val event = AnActionEvent.createFromAnAction(action, null, "LearnToolWindow.ChooseLanguageView", context)

                ActionUtil.performActionDumbAware(action, event)
            }
        }
        return button
    }

    private fun createResetResultsButton(): JButton {
        val button = JButton()
        button.action = object : AbstractAction() {
            override fun actionPerformed(e: ActionEvent) {
                button.isEnabled = false
            }
        }
        button.isOpaque = false
        button.action = object : AbstractAction(LearnBundle.message("learn.choose.language.button.reset.tool.window")) {
            override fun actionPerformed(e: ActionEvent?) {
                LessonStateManager.resetPassedStatus()
                myRadioButtonMap.values
                    .flatMap { CourseManager.instance.getModulesByLanguage(it.instance) }
                    .flatMap { module -> module.lessons }
                    .forEach { lesson -> lesson.passed = false }
                UiManager.setLanguageChooserView()
            }
        }
        return button
    }


    private fun initMainPanel() {

        mainPanel = JPanel().apply {
            layout = BoxLayout(this, BoxLayout.PAGE_AXIS)
            isOpaque = false
            isFocusable = false

            add(caption)
            add(Box.createVerticalStrut(UISettings.instance.afterCaptionGap))
            add(description)
            add(Box.createVerticalStrut(UISettings.instance.groupGap))
            if (place == LanguageChoosePanelPlace.TOOL_WINDOW) border = UISettings.instance.checkmarkShiftBorder
        }

        try {
            initSouthPanel()
        } catch (e: BadLocationException) {
            e.printStackTrace()
        }


        caption!!.text = LearnBundle.messageInPlace("learn.choose.language.caption", place)
        try {
            description!!.document.insertString(0, LearnBundle.messageInPlace("learn.choose.language.description", place), REGULAR)
        } catch (e: BadLocationException) {
            e.printStackTrace()
        }

    }


    @Throws(BadLocationException::class)
    private fun initSouthPanel() {
        val radioButtonPanel = JPanel()
        radioButtonPanel.isOpaque = false
        radioButtonPanel.border = EmptyBorder(0, 12, 0, 0)
        radioButtonPanel.layout = BoxLayout(radioButtonPanel, BoxLayout.PAGE_AXIS)

        val sortedLangSupportExtensions = LangManager.getInstance().supportedLanguagesExtensions.sortedBy {it.language}

        for (langSupportExt: LanguageExtensionPoint<LangSupport> in sortedLangSupportExtensions) {

            val radioButton = createRadioButton(langSupportExt) ?: continue
            buttonGroup.add(radioButton)
            //add radio buttons
            myRadioButtonMap[radioButton] = langSupportExt
            radioButtonPanel.add(radioButton, Component.LEFT_ALIGNMENT)
        }
        //set selected language if it is not started
        if (LangManager.getInstance().getLangSupport() != null) {
            val button = myRadioButtonMap.keys.firstOrNull { myRadioButtonMap[it]?.instance ==  LangManager.getInstance().getLangSupport()}
            if (button != null) buttonGroup.setSelected(button.model, true)
            else buttonGroup.setSelected(buttonGroup.elements.nextElement().model, true)
        } else {
            buttonGroup.setSelected(buttonGroup.elements.nextElement().model, true)
        }
        mainPanel!!.add(radioButtonPanel)
        mainPanel!!.add(Box.createVerticalStrut(UISettings.instance.groupGap))
        if (addButton) mainPanel!!.add(createLearnButton())
        if (addButton && place == LanguageChoosePanelPlace.TOOL_WINDOW) {
            mainPanel!!.add(Box.createVerticalStrut(UISettings.instance.languagePanelButtonsGap))
            mainPanel!!.add(createResetResultsButton())
        }
    }

    private fun createRadioButton(langSupportExt: LanguageExtensionPoint<LangSupport>): JRadioButton? {
        val lessonsCount = CourseManager.instance.calcLessonsForLanguage(langSupportExt.instance)
        val lang: Language = Language.findLanguageByID(langSupportExt.language) ?: return null
        val passedLessons = CourseManager.instance.calcPassedLessonsForLanguage(langSupportExt.instance)
        val buttonName = "${lang.displayName} ($lessonsCount lesson${if (lessonsCount != 1) "s" else ""}${if (passedLessons > 0) ", $passedLessons passed" else ""}) "
        val radioButton = JRadioButton(buttonName)
        radioButton.border = UISettings.instance.radioButtonBorder
        radioButton.isOpaque = false
        return radioButton
    }


    fun updateMainPanel() {
        mainPanel!!.removeAll()
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
        return Dimension(mainPanel!!.minimumSize.getWidth().toInt() + (UISettings.instance.westInset + UISettings.instance.eastInset),
                mainPanel!!.minimumSize.getHeight().toInt() + (UISettings.instance.northInset + UISettings.instance.southInset))
    }


    override fun getBackground(): Color {
        if (!UIUtil.isUnderDarcula())
            return UISettings.instance.backgroundColor
        else
            return UIUtil.getPanelBackground()
    }

    fun getActiveLangSupport(): LangSupport {
        val activeButton: AbstractButton = buttonGroup.elements.toList().find { button -> button.isSelected } ?: throw Exception("Unable to get active language")
        assert (activeButton is JRadioButton)
        assert (myRadioButtonMap.containsKey(activeButton))
        return myRadioButtonMap[activeButton]!!.instance
    }

    companion object {

        private val REGULAR = SimpleAttributeSet()
        private val REGULAR_GRAY = SimpleAttributeSet()
        private val PARAGRAPH_STYLE = SimpleAttributeSet()
    }

}