package training.ui.views

import com.intellij.openapi.project.guessCurrentProject
import com.intellij.openapi.util.SystemInfo
import com.intellij.ui.JBColor
import com.intellij.ui.components.JBLabel
import com.intellij.ui.components.labels.LinkLabel
import com.intellij.util.containers.BidirectionalMap
import com.intellij.util.ui.UIUtil
import training.learn.CourseManager
import training.learn.Module
import training.ui.LearnIcons
import training.ui.UISettings
import java.awt.*
import java.awt.event.MouseEvent
import java.awt.event.MouseListener
import javax.swing.Box
import javax.swing.BoxLayout
import javax.swing.JPanel
import javax.swing.JTextPane
import javax.swing.border.Border
import javax.swing.border.EmptyBorder
import javax.swing.text.BadLocationException
import javax.swing.text.SimpleAttributeSet
import javax.swing.text.StyleConstants

/**
 * Created by karashevich on 26/06/15.
 */
class ModulesPanel : JPanel() {

    private var lessonPanel: JPanel? = null

    private val module2linklabel: BidirectionalMap<Module, LinkLabel<Any>>?

    init {
        module2linklabel = BidirectionalMap()
        layout = BoxLayout(this, BoxLayout.Y_AXIS)
        isFocusable = false

        //Obligatory block
        generalizeUI()
        isOpaque = true
        background = background
        initMainPanel()
        add(lessonPanel)
        add(Box.createVerticalGlue())

        //set LearnPanel UI
        this.preferredSize = Dimension(UISettings.instance.width, 100)
        this.border = UISettings.instance.emptyBorder

        revalidate()
        repaint()
    }


    private fun generalizeUI() {

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
        lessonPanel = JPanel()
        lessonPanel!!.name = "lessonPanel"
        lessonPanel!!.layout = BoxLayout(lessonPanel, BoxLayout.PAGE_AXIS)
        lessonPanel!!.isOpaque = false
        lessonPanel!!.isFocusable = false
        initModulesPanel()
    }

    private fun initModulesPanel() {
        val modules = CourseManager.instance.modules
        for (module in modules) {
            if (module.lessons.size == 0) continue
            val moduleHeader = JPanel()
            moduleHeader.name = "moduleHeader"
            moduleHeader.isFocusable = false
            moduleHeader.alignmentX = Component.LEFT_ALIGNMENT
            moduleHeader.border = UISettings.instance.checkmarkShiftBorder
            moduleHeader.isOpaque = false
            moduleHeader.layout = BoxLayout(moduleHeader, BoxLayout.X_AXIS)
            val moduleName = LinkLabel<Any>(module.name, null)
            moduleName.name = "moduleName"
            module2linklabel!![module] = moduleName
            moduleName.setListener({ aSource, aLinkData ->
                try {
                    val guessCurrentProject = guessCurrentProject(lessonPanel)
                    var lesson = module.giveNotPassedLesson()
                    if (lesson == null) lesson = module.lessons[0]
                    CourseManager.instance.openLesson(guessCurrentProject, lesson)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }, null)
            moduleName.setFont(UISettings.instance.moduleNameFont)
            moduleName.setAlignmentY(Component.BOTTOM_ALIGNMENT)
            moduleName.setAlignmentX(Component.LEFT_ALIGNMENT)
            val progressStr = calcProgress(module)
            val progressLabel: JBLabel
            if (progressStr != null) {
                progressLabel = JBLabel(progressStr)
            } else {
                progressLabel = JBLabel()
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

            lessonPanel!!.add(moduleHeader)
            lessonPanel!!.add(Box.createVerticalStrut(UISettings.instance.headerGap))
            lessonPanel!!.add(descriptionPane)
            lessonPanel!!.add(Box.createVerticalStrut(UISettings.instance.moduleGap))
        }
        lessonPanel!!.add(Box.createVerticalGlue())
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
        lessonPanel!!.removeAll()
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
        return Dimension(lessonPanel!!.minimumSize.getWidth().toInt() + (UISettings.instance.westInset + UISettings.instance.westInset),
                lessonPanel!!.minimumSize.getHeight().toInt() + (UISettings.instance.northInset + UISettings.instance.southInset))
    }

    override fun paintComponent(g: Graphics) {
        super.paintComponent(g)
        paintModuleCheckmarks(g)
    }

    private fun paintModuleCheckmarks(g: Graphics) {
        if (module2linklabel != null || module2linklabel!!.size > 0) {
            for (module in module2linklabel.keys) {
                if (module.giveNotPassedLesson() == null) {
                    val linkLabel = module2linklabel[module]
                    val point = linkLabel!!.getLocationOnScreen()
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