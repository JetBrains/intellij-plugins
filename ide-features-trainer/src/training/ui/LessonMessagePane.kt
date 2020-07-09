// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package training.ui

import com.intellij.openapi.actionSystem.ActionManager
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.editor.colors.EditorColorsManager
import com.intellij.openapi.keymap.KeymapManager
import com.intellij.openapi.keymap.impl.ActionShortcutRestrictions
import com.intellij.openapi.keymap.impl.ui.KeymapPanel
import com.intellij.openapi.ui.popup.Balloon
import com.intellij.openapi.ui.popup.JBPopupFactory
import com.intellij.openapi.util.SystemInfo
import com.intellij.ui.JBColor
import com.intellij.ui.awt.RelativePoint
import com.intellij.ui.components.labels.LinkLabel
import com.intellij.util.ui.JBUI
import icons.FeaturesTrainerIcons
import training.keymap.KeymapUtil
import training.util.invokeActionForFocusContext
import training.util.useNewLearningUi
import java.awt.*
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import java.awt.geom.RoundRectangle2D
import java.util.concurrent.CopyOnWriteArrayList
import javax.swing.*
import javax.swing.text.BadLocationException
import javax.swing.text.SimpleAttributeSet
import javax.swing.text.StyleConstants

class LessonMessagePane : JTextPane() {
  private data class RangeData(val range: IntRange, val action: (Point) -> Unit)

  private val lessonMessages = CopyOnWriteArrayList<LessonMessage>()
  private val fontFamily = Font(UISettings.instance.fontFace, Font.PLAIN, UISettings.instance.fontSize).family

  private val ranges = mutableListOf<RangeData>()

  //, fontFace, check_width + check_right_indent
  init {
    initStyleConstants()
    isEditable = false
    val listener = object : MouseAdapter() {
      override fun mouseClicked(me: MouseEvent) {
        val rangeData = getRangeDataForMouse(me) ?: return
        val middle = (rangeData.range.first + rangeData.range.last) / 2
        val rectangle = modelToView(middle)
        rangeData.action(Point(rectangle.x.toInt(), (rectangle.y + rectangle.height).toInt()))
      }

      override fun mouseMoved(me: MouseEvent) {
        val rangeData = getRangeDataForMouse(me)
        if (rangeData == null) {
          cursor = Cursor.getDefaultCursor()
        }
        else {
          cursor = Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)
        }
      }
    }
    addMouseListener(listener)
    addMouseMotionListener(listener)
  }

  private fun getRangeDataForMouse(me: MouseEvent) : RangeData? {
    val point = Point(me.x, me.y)
    val offset = viewToModel(point)
    val result = ranges.find { offset in it.range } ?: return null
    if (offset < 0 || offset >= document.length) return null
    for (i in result.range) {
      val rectangle = modelToView(i)
      if (me.x >= rectangle.x && me.y >= rectangle.y && me.y <= rectangle.y + rectangle.height) {
        return result
      }
    }
    return null
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

  fun messagesNumber(): Int = lessonMessages.size

  fun resetMessagesNumber(number: Int) {
    if (number == 0) {
      clear()
      return
    }
    val end = lessonMessages[number - 1].end
    document.remove(end, document.length - end)
    while (number < lessonMessages.size) {
      lessonMessages.removeAt(lessonMessages.size - 1)
    }
  }

  fun addMessage(text: String) {
    try {
      val start = document.length
      document.insertString(document.length, text, REGULAR)
      val end = document.length
      lessonMessages.add(LessonMessage(text, start, end))

    }
    catch (e: BadLocationException) {
      LOG.warn(e)
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
          Message.MessageType.SHORTCUT -> appendShortcut(message)
          Message.MessageType.CODE -> document.insertString(document.length, message.text, CODE)
          Message.MessageType.CHECK -> document.insertString(document.length, message.text, ROBOTO)
          Message.MessageType.LINK -> appendLink(message)
          Message.MessageType.ICON -> message.toIcon()?.let { addPlaceholderForIcon(it) }
          Message.MessageType.ICON_IDX -> LearningUiManager.iconMap[message.text]?.let { addPlaceholderForIcon(it) }
          Message.MessageType.PROPOSE_RESTORE -> document.insertString(document.length, message.text, BOLD)
        }
        message.endOffset = document.endPosition.offset
      }
      val end = document.length
      lessonMessages.add(LessonMessage(messages, start, end))
    }
    catch (e: BadLocationException) {
      LOG.warn(e)
    }
  }

  private fun addPlaceholderForIcon(icon: Icon) {
    var placeholder = " "
    while (this.getFontMetrics(this.font).stringWidth(placeholder) <= icon.iconWidth) {
      placeholder += " "
    }
    placeholder += " "
    document.insertString(document.length, placeholder, REGULAR)
  }

  /**
   * inserts a checkmark icon to the end of the LessonMessagePane document as a styled label.
   */
  @Throws(BadLocationException::class)
  fun passPreviousMessages() {
    val lessonMessage = lessonMessages.lastOrNull() ?: return
    lessonMessage.passed = true

    //Repaint text with passed style
    setPassedStyle(lessonMessage)
  }

  private fun setPassedStyle(lessonMessage: LessonMessage) {
    val passedStyle = this.addStyle("PassedStyle", null)
    StyleConstants.setForeground(passedStyle, UISettings.instance.passedColor)
    styledDocument.setCharacterAttributes(0, lessonMessage.end, passedStyle, false)
  }

  fun redrawMessagesAsCompleted() {
    val copy = lessonMessages.toList()
    clear()
    for (lessonMessage in copy) {
      addMessage(lessonMessage.messages.toTypedArray())
    }
    for ((index, it) in lessonMessages.withIndex()) {
      it.passed = copy[index].passed
    }
    addMessage(arrayOf(Message("Completed!", Message.MessageType.TEXT_BOLD)))
    val completedStyle = this.addStyle("Completed", null)
    StyleConstants.setForeground(completedStyle, UISettings.instance.completedColor)
    styledDocument.setCharacterAttributes(lessonMessages.last().start, lessonMessages.last().end, completedStyle, false)
    repaint()
  }

  fun redrawMessages() {
    val copy = lessonMessages.toList()
    clear()
    for (lessonMessage in copy) {
      addMessage(lessonMessage.messages.toTypedArray())
    }
    for ((index, it) in lessonMessages.withIndex()) {
      it.passed = copy[index].passed
      if (it.passed) setPassedStyle(it)
    }
  }

  fun clear() {
    text = ""
    lessonMessages.clear()
    ranges.clear()
  }

  /**
   * Appends link inside JTextPane to Run another lesson

   * @param message - should have LINK type. message.runnable starts when the message has been clicked.
   */
  @Throws(BadLocationException::class)
  private fun appendLink(message: Message) {
    val clickRange = appendClickableRange(message.text, LINK)
    val runnable = message.runnable ?: return
    ranges.add(RangeData(clickRange) { runnable.run() })
  }

  private fun appendShortcut(message: Message) {
    val range = appendClickableRange(" ${message.text} ", SHORTCUT)
    val clickRange = IntRange(range.first + 1, range.last - 1) // exclude around spaces
    ranges.add(RangeData(clickRange) { showShortcutBalloon(it, message.link, message.text) })
  }

  private fun showShortcutBalloon(it: Point, actionName: String?, shortcut: String) {
    lateinit var balloon: Balloon
    val jPanel = JPanel()
    jPanel.layout = BoxLayout(jPanel, BoxLayout.Y_AXIS)
    if (SystemInfo.isMac) {
      jPanel.add(JLabel(KeymapUtil.decryptMacShortcut(shortcut)))
    }
    val action = actionName?.let { ActionManager.getInstance().getAction(it) }
    if (action != null) {
      jPanel.add(JLabel(action.templatePresentation.text))
      jPanel.add(LinkLabel<Any>("Apply this action", null) { _, _ ->
        invokeActionForFocusContext(action)
        balloon.hide()
      })
      jPanel.add(LinkLabel<Any>("Add shortcut", null) { _, _ ->
        KeymapPanel.addKeyboardShortcut(actionName, ActionShortcutRestrictions.getInstance().getForActionId(actionName), KeymapManager.getInstance().activeKeymap, this)
        balloon.hide()
        repaint()
      })
    }
    val builder = JBPopupFactory.getInstance()
      .createDialogBalloonBuilder(jPanel, null)
      //.setRequestFocus(true)
      .setHideOnClickOutside(true)
      .setCloseButtonEnabled(true)
      .setAnimationCycle(0)
      .setBlockClicksThroughBalloon(true)
      //.setContentInsets(Insets(0, 0, 0, 0))
    builder.setBorderColor(JBColor(Color.BLACK, Color.WHITE))
    balloon = builder.createBalloon()
    balloon.show(RelativePoint(this, it), Balloon.Position.below)
  }

  private fun appendClickableRange(clickable: String, attributeSet: SimpleAttributeSet): IntRange {
    val startLink = document.length
    document.insertString(document.length, clickable, attributeSet)
    val endLink = document.length
    return startLink..endLink
  }

  override fun paintComponent(g: Graphics) {
    try {
      paintMessages(g)
    }
    catch (e: BadLocationException) {
      LOG.warn(e)
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
          val checkmark = if (useNewLearningUi) FeaturesTrainerIcons.GreenCheckmark else FeaturesTrainerIcons.Checkmark
          if (SystemInfo.isMac) {
            checkmark.paintIcon(this, g, rectangle.x - UISettings.instance.checkIndent, rectangle.y + JBUI.scale(1))
          }
          else {
            checkmark.paintIcon(this, g, rectangle.x - UISettings.instance.checkIndent, rectangle.y + JBUI.scale(1))
          }
        }
        catch (e: BadLocationException) {
          LOG.warn(e)
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
        }
        else if (myMessage.type == Message.MessageType.ICON) {
          val rect = modelToView(myMessage.startOffset)
          val icon = myMessage.toIcon()
          icon?.paintIcon(this, g2d, rect.x, rect.y)
        }
        else if (myMessage.type == Message.MessageType.ICON_IDX) {
          val rect = modelToView(myMessage.startOffset)
          val icon = LearningUiManager.iconMap[myMessage.text]
          icon?.paintIcon(this, g2d, rect.x, rect.y)
        }
      }
    }
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
