/*
 * Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 */
package training.learn.lesson.kimpl

import training.learn.interfaces.Lesson
import training.learn.interfaces.Module
import training.learn.lesson.LessonListener
import training.learn.lesson.LessonState
import training.learn.lesson.LessonStateManager

abstract class KLesson(final override val id: String,
                       final override val name: String,
                       override var module: Module,
                       override val lang: String) : Lesson {

  constructor(name: String, module: Module, lang: String) : this(name, name, module, lang)

  abstract val lessonContent: LessonContext.() -> Unit
  @Volatile override var passed = LessonStateManager.getStateFromBase(id) == LessonState.PASSED
  @Volatile override var isOpen = false
  override val lessonListeners: MutableList<LessonListener> = mutableListOf()
}
