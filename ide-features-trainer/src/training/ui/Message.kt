// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package training.ui

import com.intellij.openapi.application.ApplicationNamesInfo
import com.intellij.openapi.util.text.StringUtil
import org.jdom.Content
import org.jdom.Element
import org.jdom.Text
import org.jdom.output.XMLOutputter
import training.keymap.KeymapUtil.getGotoActionText
import training.keymap.KeymapUtil.getKeyStrokeText
import training.keymap.KeymapUtil.getShortcutByActionId
import java.lang.reflect.Field
import java.util.function.Consumer
import javax.swing.Icon

class Message(val type: MessageType, private val textFn: () -> String) {

  constructor(text: String, type: MessageType): this(type, {text})

  val text: String
    get() = textFn()

  enum class MessageType { TEXT_REGULAR, TEXT_BOLD, SHORTCUT, CODE, LINK, CHECK, ICON, ICON_IDX, PROPOSE_RESTORE }

  var startOffset = 0
  var endOffset = 0
  var link: String? = null
  var runnable: Runnable? = null

  override fun toString(): String {
    return "Message{" +
           "messageText='" + text + '\''.toString() +
           ", messageType=" + type +
           '}'
  }

  fun toIcon(): Icon? {
    val iconName = text.substringAfterLast(".")
    val path = text.substringBeforeLast(".")
    val fullPath = "com.intellij.icons.${path.replace(".", "$")}"
    val field: Field?
    try {
      field = Class.forName(fullPath).getField(iconName)
    } catch (e: NoSuchFieldException) {
      return null
    }
    return field?.get(null) as Icon
  }

  companion object {
    fun convert(element: Element?): Array<Message> {
      if (element == null) {
        return arrayOf()
      }
      val list: MutableList<Message> = mutableListOf()
      element.content.forEach(Consumer { content: Content ->
        if (content is Text) {
          list.add(Message(content.getValue(), MessageType.TEXT_REGULAR))
        }
        else if (content is Element) {
          val outputter = XMLOutputter()
          var type = MessageType.TEXT_REGULAR
          val text: String = StringUtil.unescapeXmlEntities(outputter.outputString(content.content))
          var textFn = { text }
          var link: String? = null
          when (content.name) {
            "icon" -> type = MessageType.ICON
            "icon_idx" -> type = MessageType.ICON_IDX
            "code" -> type = MessageType.CODE
            "shortcut" -> type = MessageType.SHORTCUT
            "strong" -> type = MessageType.TEXT_BOLD
            "a" -> {
              type = MessageType.LINK
              link = content.getAttributeValue("href")
            }
            "action" -> {
              type = MessageType.SHORTCUT
              link = text
              textFn = {
                val shortcutByActionId = getShortcutByActionId(text)
                if (shortcutByActionId != null) {
                  getKeyStrokeText(shortcutByActionId)
                }
                else {
                  getGotoActionText(text)
                }
              }
            }
            "raw_action" -> {
              type = MessageType.SHORTCUT
            }
            "ide" -> {
              type = MessageType.TEXT_REGULAR
              textFn = { ApplicationNamesInfo.getInstance().fullProductName }
            }
          }
          val message = Message(type, textFn)
          message.link = link
          list.add(message)
        }
      })
      return list.toTypedArray()
    }
  }

}