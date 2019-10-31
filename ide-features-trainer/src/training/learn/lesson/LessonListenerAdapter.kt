/*
 * Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 */
package training.learn.lesson

import training.learn.exceptons.BadLessonException
import training.learn.exceptons.BadModuleException
import training.learn.exceptons.LessonIsOpenedException
import training.learn.interfaces.Lesson
import java.awt.FontFormatException
import java.io.IOException
import java.util.concurrent.ExecutionException

open class LessonListenerAdapter : LessonListener {

  override fun lessonStarted(lesson: Lesson) { }
  override fun lessonPassed(lesson: Lesson) { }
  override fun lessonClosed(lesson: Lesson) { }

  @Throws(BadLessonException::class, ExecutionException::class, IOException::class, FontFormatException::class, InterruptedException::class,
          BadModuleException::class, LessonIsOpenedException::class)
  override fun lessonNext(lesson: Lesson) { }

}