/*
 * Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 */
package training.learn.log

import training.learn.interfaces.Lesson
import java.util.*

class LessonLog(lesson: Lesson) {

  val logData = ArrayList<Pair<Date, String>>()
  var exerciseCount = 0

  fun log(actionString: String) {
    logData.add(Pair(Date(), actionString))
  }

  fun resetCounter() {
    exerciseCount = 0
  }

  fun print() {
    for ((first) in logData) {
      println(first.toString() + ": " + first)
    }
  }

  init {
    log("Log is created. XmlLesson:" + lesson.name)
  }

}
