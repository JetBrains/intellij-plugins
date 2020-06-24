// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package training.learn.interfaces

import training.lang.LangSupport

interface Module {

  val classLoader: ClassLoader
    get() = javaClass.classLoader

  val lessons: List<Lesson>

  val sanitizedName: String

  var id: String?

  val name: String

  val primaryLanguage: LangSupport

  val moduleType: ModuleType

  val description: String?

  fun giveNotPassedLesson(): Lesson?

  fun giveNotPassedAndNotOpenedLesson(): Lesson?

  fun hasNotPassedLesson(): Boolean

  fun calcProgress(): String? {
    val total = lessons.size
    var done = 0
    for (lesson in lessons) {
      if (lesson.passed) done++
    }
    return if (done != 0) {
      if (done == total)
        ""
      else
        "$done of $total done"
    }
    else {
      null
    }
  }

}