// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.jetbrains.swift.ift

import com.intellij.openapi.util.NlsSafe
import com.jetbrains.swift.ift.lesson.codegeneration.SwiftCreateFromUsageLesson
import com.jetbrains.swift.ift.lesson.codegeneration.SwiftGenerateLesson
import com.jetbrains.swift.ift.lesson.codegeneration.SwiftOverrideImplementLesson
import com.jetbrains.swift.ift.lesson.codegeneration.SwiftQuickFixesAndIntentionsLesson
import com.jetbrains.swift.ift.lesson.editor.*
import com.jetbrains.swift.ift.lesson.navigation.*
import com.jetbrains.swift.ift.lesson.refactorings.*
import com.jetbrains.swift.ift.lesson.rundebugtest.SwiftDebugLesson
import com.jetbrains.swift.ift.lesson.rundebugtest.SwiftRunLesson
import com.jetbrains.swift.ift.lesson.rundebugtest.SwiftTestLesson
import org.jetbrains.annotations.Nls
import org.jetbrains.annotations.NonNls
import training.learn.course.KLesson
import training.learn.course.LearningCourseBase
import training.learn.course.LearningModule
import training.learn.course.LessonType

class SwiftLearningCourse : LearningCourseBase("Swift") {
  override fun modules() = listOf(
    learningModule(id = "Swift.Editor",
                   name = SwiftLessonsBundle.message("swift.editor.module.name"),
                   description = SwiftLessonsBundle.message("swift.editor.module.description"),
                   fileName = "Editor") {
      listOf(
        SwiftCompletionLesson(),
        SwiftSelectionLesson(),
        SwiftCommentLesson(),
        SwiftDeleteLesson(),
        SwiftDuplicateLesson(),
        SwiftMoveLesson(),
        SwiftFoldingLesson(),
        SwiftMultipleSelectionsLesson(),
        SwiftCodeFormattingLesson(),
        SwiftQuickPopupsLesson(),
      )
    },
    learningModule(id = "Swift.CodeGeneration",
                   name = SwiftLessonsBundle.message("swift.code.generations.module.name"),
                   description = SwiftLessonsBundle.message("swift.code.generations.module.description"),
                   fileName = "CodeGeneration") {
      listOf(
        SwiftGenerateLesson(),
        SwiftOverrideImplementLesson(),
        SwiftCreateFromUsageLesson(),
        SwiftQuickFixesAndIntentionsLesson(),
      )
    },
    learningModule(id = "Swift.Navigation",
                   name = SwiftLessonsBundle.message("swift.navigation.module.name"),
                   description = SwiftLessonsBundle.message("swift.navigation.module.description"),
                   fileName = "Navigation") {
      listOf(
        SwiftMainWindowsViewsLesson(),
        SwiftTODOsBookmarksLesson(),
        SwiftCodeNavigationLesson(),
        SwiftPreciseNavigationLesson(),
        SwiftSearchLesson(),
      )
    },
    learningModule(id = "Swift.Refactorings",
                   name = SwiftLessonsBundle.message("swift.refactorings.module.name"),
                   description = SwiftLessonsBundle.message("swift.refactorings.module.description"),
                   fileName = "Refactorings") {
      listOf(
        SwiftRenameLesson(),
        SwiftExtractVariableLesson(),
        SwiftExtractFunctionLesson(),
        SwiftExtractClosureLesson(),
        SwiftChangeSignatureLesson(),
      )
    },
    learningModule(id = "Swift.RunDebugTest",
                   name = SwiftLessonsBundle.message("swift.run.debug.test.module.name"),
                   description = SwiftLessonsBundle.message("swift.run.debug.test.module.description"),
                   fileName = "RunDebugTest") {
      listOf(
        SwiftRunLesson(),
        SwiftDebugLesson(),
        SwiftTestLesson(),
      )
    },
  )


  private fun learningModule(@NonNls id: String,
                             @Nls name: String,
                             @Nls description: String,
                             @NlsSafe fileName: String,
                             initLessons: () -> List<KLesson>
  ) = LearningModule(id = id,
                     name = name,
                     description = description,
                     sampleFilePath = "LearnProject/" + fileName + ".swift",
                     primaryLanguage = langSupport,
                     moduleType = LessonType.PROJECT,
                     initLessons = initLessons,
  )
}
