/*
 * Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 */
package training.ui

import com.intellij.openapi.actionSystem.ActionManager
import com.intellij.openapi.application.ApplicationNamesInfo
import com.intellij.openapi.util.text.StringUtil
import org.jdom.Content
import org.jdom.Element
import org.jdom.Text
import org.jdom.output.XMLOutputter
import training.keymap.KeymapUtil.getKeyStrokeText
import training.keymap.KeymapUtil.getShortcutByActionId
import java.util.function.Consumer

class Message(val text: String, val type: MessageType) {

  enum class MessageType { TEXT_REGULAR, TEXT_BOLD, SHORTCUT, CODE, LINK, CHECK, ICON }

  var startOffset = 0
  var endOffset = 0
  private var link: String? = null
  var runnable: Runnable? = null

  fun isText(): Boolean {
    return type == MessageType.TEXT_REGULAR || type == MessageType.TEXT_BOLD
  }

  fun getLink(): String? {
    return link
  }

  fun setLink(link: String?) {
    this.link = link
  }

  override fun toString(): String {
    return "Message{" +
            "messageText='" + text + '\''.toString() +
            ", messageType=" + type +
            '}'
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
        } else if (content is Element) {
          val outputter = XMLOutputter()
          var type = MessageType.TEXT_REGULAR
          var text: String = outputter.outputString(content.content)
          text = StringUtil.unescapeXmlEntities(text)
          var link: String? = null
          when (content.name) {
            "icon" -> type = MessageType.ICON
            "code" -> type = MessageType.CODE
            "shortcut" -> type = MessageType.SHORTCUT
            "strong" -> type = MessageType.TEXT_BOLD
            "a" -> {
              type = MessageType.LINK
              link = content.getAttributeValue("href")
            }
            "action" -> {
              type = MessageType.SHORTCUT
              val shortcutByActionId = getShortcutByActionId(text)
              text = if (shortcutByActionId != null) {
                getKeyStrokeText(shortcutByActionId)
              } else {
                getKeyStrokeText(getShortcutByActionId("GotoAction")) + " → " + ActionManager.getInstance().getAction(text).templatePresentation.text
              }
            }
            "ide" -> {
              type = MessageType.TEXT_REGULAR
              text = ApplicationNamesInfo.getInstance().fullProductName
            }
          }
          val message = Message(text, type)
          message.link = link
          list.add(message)
        }
      })
      return list.toTypedArray()
    }
  }

}