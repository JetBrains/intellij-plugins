// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package training.learn.course

import com.intellij.lang.javascript.JavascriptLanguage
import training.learn.LearningModule
import training.learn.LessonsBundle
import training.learn.interfaces.LessonType
import training.learn.lesson.javascript.editor.*
import training.learn.lesson.javascript.testing.JestLesson
import training.learn.lesson.kimpl.LessonUtil

class JavaScriptMainLearningCourse : LearningCourseBase(JavascriptLanguage.INSTANCE.id) {
  override fun modules() = listOf(
    LearningModule(name = LessonsBundle.message("js.editor.basics.module.name"),
                   description = LessonsBundle.message("js.editor.basics.module.description"),
                   primaryLanguage = langSupport,
                   moduleType = LessonType.PROJECT) {
      listOf(
        BasicCompletionLesson(it),
        CodeInspectionLesson(it),
        RefactoringLesson(it),
        CodeEditingLesson(it),
        NavigationLesson(it),
      )
    },
    LearningModule(name = LessonsBundle.message("js.getting.started.module.name"),
                   description = LessonsBundle.message("js.getting.started.module.description", LessonUtil.productName),
                   primaryLanguage = langSupport,
                   moduleType = LessonType.PROJECT) {
      listOf(
        JestLesson(it),
      )
    },
  )
}