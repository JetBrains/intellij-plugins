// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package training.learn.course

import com.intellij.lang.javascript.JavascriptLanguage
import training.learn.LearningModule
import training.learn.LessonsBundle
import training.learn.interfaces.ModuleType
import training.learn.lesson.javascript.debugger.BeforeDebuggingLesson
import training.learn.lesson.javascript.debugger.DebuggingFirstPartLesson
import training.learn.lesson.javascript.debugger.DebuggingSecondPartLesson
import training.learn.lesson.kimpl.LessonUtil

class JavaScriptDebugLearningCourse : LearningCourseBase(JavascriptLanguage.INSTANCE.id) {
  override fun modules() = listOf(
    LearningModule(name = LessonsBundle.message("js.debugger.module.name", LessonUtil.productName),
                   description = LessonsBundle.message("js.debugger.module.description"),
                   sanitizedName = "Debugger",
                   primaryLanguage = langSupport,
                   moduleType = ModuleType.PROJECT) {
      listOf(
        BeforeDebuggingLesson(it),
        DebuggingFirstPartLesson(it),
        DebuggingSecondPartLesson(it),
      )
    },
  )
}