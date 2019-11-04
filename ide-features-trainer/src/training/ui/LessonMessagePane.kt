/*
 * Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 */
package training.ui

import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.editor.colors.EditorColorsManager
import com.intellij.openapi.util.SystemInfo
import com.intellij.ui.JBColor
import com.intellij.util.ui.JBUI
import java.awt.Font
import java.awt.Graphics
import java.awt.Graphics2D
import java.awt.Point
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import java.awt.geom.RoundRectangle2D
import java.util.concurrent.CopyOnWriteArrayList
import javax.swing.Icon
import javax.swing.JTextPane
import javax.swing.text.BadLocationException
import javax.swing.text.SimpleAttributeSet
import javax.swing.text.StyleConstants

class LessonMessagePane : JTextPane() {

    private val lessonMessages = CopyOnWriteArrayList<LessonMessage>()
    private val fontFamily = Font(UISettings.instance.fontFace, Font.PLAIN, UISettings.instance.fontSize).family

    //, fontFace, check_width + check_right_indent
    init {
        initStyleConstants()
        isEditable = false
    }

    private fun initStyleConstants() {
        font = Font(UISettings.instance.fontFace, Font.PLAIN, UISettings.instance.fontSize)

        StyleConstants.setFontFamily(REGULAR, fontFamily)
        StyleConstants.setFontSize(REGULAR, UISettings.instance.fontSize)
        StyleConstants.setForeground(REGULAR, JBColor.BLACK)

        StyleConstants.setFontFamily(BOLD, fontFamily)
        StyleConstants.setFontSize(BOLD, UISettings.instance.fontSize)
        StyleConstants.setBold(BOLD, true)
        StyleConstants.setForeground(BOLD, JBColor.BLACK)

        StyleConstants.setFontFamily(SHORTCUT, fontFamily)
        StyleConstants.setFontSize(SHORTCUT, UISettings.instance.fontSize)
        StyleConstants.setBold(SHORTCUT, true)
        StyleConstants.setForeground(SHORTCUT, JBColor.BLACK)

        StyleConstants.setForeground(CODE, JBColor.BLUE)
        EditorColorsManager.getInstance().globalScheme.editorFontName
        StyleConstants.setFontFamily(CODE, EditorColorsManager.getInstance().globalScheme.editorFontName)
        StyleConstants.setFontSize(CODE, UISettings.instance.fontSize)

        StyleConstants.setForeground(LINK, JBColor.BLUE)
        StyleConstants.setFontFamily(LINK, fontFamily)
        StyleConstants.setUnderline(LINK, true)
        StyleConstants.setFontSize(LINK, UISettings.instance.fontSize)

        StyleConstants.setLeftIndent(PARAGRAPH_STYLE, UISettings.instance.checkIndent.toFloat())
        StyleConstants.setRightIndent(PARAGRAPH_STYLE, 0f)
        StyleConstants.setSpaceAbove(PARAGRAPH_STYLE, 16.0f)
        StyleConstants.setSpaceBelow(PARAGRAPH_STYLE, 0.0f)
        StyleConstants.setLineSpacing(PARAGRAPH_STYLE, 0.2f)

        StyleConstants.setForeground(REGULAR, UISettings.instance.defaultTextColor)
        StyleConstants.setForeground(BOLD, UISettings.instance.defaultTextColor)
        StyleConstants.setForeground(SHORTCUT, UISettings.instance.shortcutTextColor)
        StyleConstants.setForeground(LINK, UISettings.instance.lessonLinkColor)
        StyleConstants.setForeground(CODE, UISettings.instance.lessonLinkColor)

        this.setParagraphAttributes(PARAGRAPH_STYLE, true)
    }

    fun addMessage(text: String) {
        try {
            val start = document.length
            document.insertString(document.length, text, REGULAR)
            val end = document.length
            lessonMessages.add(LessonMessage(text, start, end))

        } catch (e: BadLocationException) {
            e.printStackTrace()
        }

    }

    fun addMessage(messages: Array<Message>) {
        try {
            val start = document.length
            if (lessonMessages.isNotEmpty())
                document.insertString(document.length, "\n", REGULAR)
            for (message in messages) {
                val startOffset = document.endPosition.offset
                message.startOffset = startOffset
                when (message.type) {
                    Message.MessageType.TEXT_REGULAR -> document.insertString(document.length, message.text, REGULAR)
                    Message.MessageType.TEXT_BOLD -> document.insertString(document.length, message.text, BOLD)
                    Message.MessageType.SHORTCUT -> document.insertString(document.length, " ${message.text} ", SHORTCUT)
                    Message.MessageType.CODE -> document.insertString(document.length, message.text, CODE)
                    Message.MessageType.CHECK -> document.insertString(document.length, message.text, ROBOTO)
                    Message.MessageType.LINK -> appendLink(message)
                    Message.MessageType.ICON -> {
                        val icon = iconFromPath(message)
                        var placeholder = " "
                        while (this.getFontMetrics(this.font).stringWidth(placeholder) < icon.iconWidth) {
                            placeholder += " "
                        }
                        placeholder += " "
                        document.insertString(document.length, placeholder, REGULAR)
                    }
                }
                message.endOffset = document.endPosition.offset
            }
            val end = document.length
            lessonMessages.add(LessonMessage(messages, start, end))
        } catch (e: BadLocationException) {
            LOG.warn(e)
        }
    }

    /**
     * inserts a checkmark icon to the end of the LessonMessagePane document as a styled label.
     */
    @Throws(BadLocationException::class)
    fun passPreviousMessages() {
        if (lessonMessages.size > 0) {
            val lessonMessage = lessonMessages[lessonMessages.size - 1]
            lessonMessage.passed = true

            //Repaint text with passed style
            val passedStyle = this.addStyle("PassedStyle", null)
            StyleConstants.setForeground(passedStyle, UISettings.instance.passedColor)
            styledDocument.setCharacterAttributes(0, lessonMessage.end, passedStyle, false)
        }
    }

    fun clear() {
        text = ""
        lessonMessages.clear()
    }

    /**
     * Appends link inside JTextPane to Run another lesson

     * @param message - should have LINK type. message.runnable starts when the message has been clicked.
     */
    @Throws(BadLocationException::class)
    private fun appendLink(message: Message) {
        val startLink = document.endPosition.offset
        document.insertString(document.length, message.text, LINK)
        val endLink = document.endPosition.offset

        addMouseListener(object : MouseAdapter() {

            override fun mouseClicked(me: MouseEvent) {
                val x = me.x
                val y = me.y

                val clickOffset = viewToModel(Point(x, y))
                val runnable = message.runnable
                if (clickOffset in startLink..endLink && runnable != null) {
                    runnable.run()
                }

            }
        })
    }

    override fun paintComponent(g: Graphics) {
        try {
            paintMessages(g)
        } catch (e: BadLocationException) {
            e.printStackTrace()
        }

        super.paintComponent(g)
        paintLessonCheckmarks(g)
    }

    private fun paintLessonCheckmarks(g: Graphics) {
        for (lessonMessage in lessonMessages) {
            if (lessonMessage.passed) {
                var startOffset = lessonMessage.start
                if (startOffset != 0) startOffset++
                try {
                    val rectangle = modelToView(startOffset)
                    if (SystemInfo.isMac) {
                        LearnIcons.checkMarkGray.paintIcon(this, g, rectangle.x - UISettings.instance.checkIndent, rectangle.y + JBUI.scale(1))
                    } else {
                        LearnIcons.checkMarkGray.paintIcon(this, g, rectangle.x - UISettings.instance.checkIndent, rectangle.y + JBUI.scale(1))
                    }
                } catch (e: BadLocationException) {
                    e.printStackTrace()
                }

            }
        }
    }

    @Throws(BadLocationException::class)
    private fun paintMessages(g: Graphics) {
        val g2d = g as Graphics2D
        for (lessonMessage in lessonMessages) {
            val myMessages = lessonMessage.messages
            for (myMessage in myMessages) {
                if (myMessage.type == Message.MessageType.SHORTCUT) {
                    val startOffset = myMessage.startOffset
                    val endOffset = myMessage.endOffset
                    val rectangleStart = modelToView(startOffset)
                    val rectangleEnd = modelToView(endOffset - 2)
                    val color = g2d.color
                    val fontSize = UISettings.instance.fontSize

                    g2d.color = UISettings.instance.shortcutBackgroundColor
                    val r2d: RoundRectangle2D
                    r2d = if (!SystemInfo.isMac)
                        RoundRectangle2D.Double(rectangleStart.getX() - 2 * indent, rectangleStart.getY() - indent + 1,
                                rectangleEnd.getX() - rectangleStart.getX() + 4 * indent, (fontSize + 3 * indent).toDouble(),
                                arc.toDouble(), arc.toDouble())
                    else
                        RoundRectangle2D.Double(rectangleStart.getX() - 2 * indent, rectangleStart.getY() - indent,
                                rectangleEnd.getX() - rectangleStart.getX() + 4 * indent, (fontSize + 3 * indent).toDouble(),
                                arc.toDouble(), arc.toDouble())
                    g2d.fill(r2d)
                    g2d.color = color
                } else if (myMessage.type == Message.MessageType.ICON) {
                    val rect = modelToView(myMessage.startOffset)
                    val icon = iconFromPath(myMessage)
                    icon.paintIcon(this, g2d, rect.x, rect.y)
                }
            }
        }
    }

    private fun iconFromPath(myMessage: Message): Icon {
        val iconName = myMessage.text.substringAfterLast(".")
        val path = myMessage.text.substringBeforeLast(".")
        val fullPath = "com.intellij.icons.${path.replace(".", "$")}"
        return Class.forName(fullPath).getField(iconName).get(null) as Icon
    }

    companion object {

        private val LOG = Logger.getInstance(LessonMessagePane::class.java)

        //Style Attributes for LessonMessagePane(JTextPane)
        private val REGULAR = SimpleAttributeSet()
        private val BOLD = SimpleAttributeSet()
        private val SHORTCUT = SimpleAttributeSet()
        private val ROBOTO = SimpleAttributeSet()
        private val CODE = SimpleAttributeSet()
        private val LINK = SimpleAttributeSet()
        private val PARAGRAPH_STYLE = SimpleAttributeSet()

        //arc & indent for shortcut back plate
        private val arc by lazy { JBUI.scale(4) }
        private val indent by lazy { JBUI.scale(2) }
    }


}

