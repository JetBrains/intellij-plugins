// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.intellij.javascript.ift.debug

import com.intellij.javascript.ift.debug.lesson.BeforeDebuggingLesson
import com.intellij.javascript.ift.debug.lesson.DebuggingFirstPartLesson
import com.intellij.javascript.ift.debug.lesson.DebuggingSecondPartLesson
import com.intellij.lang.javascript.JavascriptLanguage
import training.dsl.LessonUtil
import training.learn.course.LearningCourseBase
import training.learn.course.LearningModule
import training.learn.course.LessonType

private class JavaScriptDebugLearningCourse : LearningCourseBase(JavascriptLanguage.id) {
  override fun modules() = listOf(
    LearningModule(id = "JS.Debugger",
                   name = JsDebugLessonsBundle.message("js.debugger.module.name", LessonUtil.productName),
                   description = JsDebugLessonsBundle.message("js.debugger.module.description"),
                   primaryLanguage = langSupport,
                   moduleType = LessonType.PROJECT) {
      listOf(
        BeforeDebuggingLesson(),
        DebuggingFirstPartLesson(),
        DebuggingSecondPartLesson(),
      )
    },
  )
}