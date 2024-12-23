// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.intellij.javascript.ift

import com.intellij.javascript.ift.lesson.editor.*
import com.intellij.javascript.ift.lesson.testing.JestLesson
import com.intellij.lang.javascript.JavascriptLanguage
import training.dsl.LessonUtil
import training.learn.course.LearningCourseBase
import training.learn.course.LearningModule
import training.learn.course.LessonType

private class JavaScriptMainLearningCourse : LearningCourseBase(JavascriptLanguage.id) {
  override fun modules() = listOf(
    LearningModule(id = "JS.EditorBasics",
                   name = JsLessonsBundle.message("js.editor.basics.module.name"),
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
    LearningModule(id = "JS.Testing",
                   name = JsLessonsBundle.message("js.getting.started.module.name"),
                   description = JsLessonsBundle.message("js.getting.started.module.description", LessonUtil.productName),
                   primaryLanguage = langSupport,
                   moduleType = LessonType.PROJECT) {
      listOf(
        JestLesson(),
      )
    },
  )
}