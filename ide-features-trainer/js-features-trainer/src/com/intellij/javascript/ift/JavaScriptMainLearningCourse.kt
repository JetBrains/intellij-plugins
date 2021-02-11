// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.intellij.javascript.ift

import com.intellij.javascript.ift.lesson.editor.*
import com.intellij.javascript.ift.lesson.testing.JestLesson
import com.intellij.lang.javascript.JavascriptLanguage
import training.learn.LearningModule
import training.learn.course.LearningCourseBase
import training.learn.interfaces.LessonType
import training.learn.lesson.kimpl.LessonUtil

class JavaScriptMainLearningCourse : LearningCourseBase(JavascriptLanguage.INSTANCE.id) {
  override fun modules() = listOf(
    LearningModule(name = JsLessonsBundle.message("js.editor.basics.module.name"),
                   description = JsLessonsBundle.message("js.editor.basics.module.description"),
                   primaryLanguage = langSupport,
                   moduleType = LessonType.PROJECT) {
      listOf(
        BasicCompletionLesson(),
        CodeInspectionLesson(),
        RefactoringLesson(),
        CodeEditingLesson(),
        NavigationLesson(),
      )
    },
    LearningModule(name = JsLessonsBundle.message("js.getting.started.module.name"),
                   description = JsLessonsBundle.message("js.getting.started.module.description", LessonUtil.productName),
                   primaryLanguage = langSupport,
                   moduleType = LessonType.PROJECT) {
      listOf(
        JestLesson(),
      )
    },
  )
}