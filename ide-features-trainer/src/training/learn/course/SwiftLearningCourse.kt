// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package training.learn.course

import training.learn.LearningModule
import training.learn.LessonsBundle
import training.learn.interfaces.ModuleType
import training.learn.lesson.swift.codegeneration.SwiftCreateFromUsageLesson
import training.learn.lesson.swift.codegeneration.SwiftGenerateLesson
import training.learn.lesson.swift.codegeneration.SwiftOverrideImplementLesson
import training.learn.lesson.swift.codegeneration.SwiftQuickFixesAndIntentionsLesson
import training.learn.lesson.swift.editor.*
import training.learn.lesson.swift.navigation.*
import training.learn.lesson.swift.refactorings.*
import training.learn.lesson.swift.rundebugtest.SwiftDebugLesson
import training.learn.lesson.swift.rundebugtest.SwiftRunLesson
import training.learn.lesson.swift.rundebugtest.SwiftTestLesson

class SwiftLearningCourse : LearningCourseBase("Swift") {
  override fun modules() = listOf(
    LearningModule(name = LessonsBundle.message("swift.editor.module.name"),
                   description = LessonsBundle.message("swift.editor.module.description"),
                   sampleFileName = "Editor",
                   primaryLanguage = langSupport,
                   moduleType = ModuleType.PROJECT) {
      listOf(
        SwiftCompletionLesson(it),
        SwiftSelectionLesson(it),
        SwiftCommentLesson(it),
        SwiftDeleteLesson(it),
        SwiftDuplicateLesson(it),
        SwiftMoveLesson(it),
        SwiftFoldingLesson(it),
        SwiftMultipleSelectionsLesson(it),
        SwiftCodeFormattingLesson(it),
        SwiftQuickPopupsLesson(it),
      )
    },
    LearningModule(name = LessonsBundle.message("swift.code.generations.module.name"),
                   description = LessonsBundle.message("swift.code.generations.module.description"),
                   sampleFileName = "CodeGeneration",
                   primaryLanguage = langSupport,
                   moduleType = ModuleType.PROJECT) {
      listOf(
        SwiftGenerateLesson(it),
        SwiftOverrideImplementLesson(it),
        SwiftCreateFromUsageLesson(it),
        SwiftQuickFixesAndIntentionsLesson(it),
      )
    },
    LearningModule(name = LessonsBundle.message("swift.navigation.module.name"),
                   description = LessonsBundle.message("swift.navigation.module.description"),
                   sampleFileName = "Navigation",
                   primaryLanguage = langSupport,
                   moduleType = ModuleType.PROJECT) {
      listOf(
        SwiftMainWindowsViewsLesson(it),
        SwiftTODOsBookmarksLesson(it),
        SwiftCodeNavigationLesson(it),
        SwiftPreciseNavigationLesson(it),
        SwiftSearchLesson(it),
      )
    },
    LearningModule(name = LessonsBundle.message("swift.refactorings.module.name"),
                   description = LessonsBundle.message("swift.refactorings.module.description"),
                   sampleFileName = "Refactorings",
                   primaryLanguage = langSupport,
                   moduleType = ModuleType.PROJECT) {
      listOf(
        SwiftRenameLesson(it),
        SwiftExtractVariableLesson(it),
        SwiftExtractMethodLesson(it),
        SwiftExtractClosureLesson(it),
        SwiftChangeSignatureLesson(it),
      )
    },
    LearningModule(name = LessonsBundle.message("swift.run.debug.test.module.name"),
                   description = LessonsBundle.message("swift.run.debug.test.module.description"),
                   sampleFileName = "RunDebugTest",
                   primaryLanguage = langSupport,
                   moduleType = ModuleType.PROJECT) {
      listOf(
        SwiftRunLesson(it),
        SwiftDebugLesson(it),
        SwiftTestLesson(it),
      )
    },
  )
}