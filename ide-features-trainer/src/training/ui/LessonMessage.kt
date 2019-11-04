/*
 * Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 */
package training.ui

internal class LessonMessage {

  val messages: List<Message>
  val start: Int
  val end: Int
  var passed: Boolean = false

  constructor(text: String, start: Int, end: Int) {
    this.messages = listOf(Message(text, Message.MessageType.TEXT_REGULAR))
    this.start = start
    this.end = end
  }

  constructor(messages: Array<Message>, start: Int, end: Int) {
    this.messages = messages.asList()
    this.start = start
    this.end = end
  }
}
