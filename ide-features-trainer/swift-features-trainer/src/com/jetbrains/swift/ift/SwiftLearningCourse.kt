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
                     sampleFilePath = "LearnProject/$fileName.swift",
                     primaryLanguage = langSupport,
                     moduleType = LessonType.PROJECT,
                     initLessons = initLessons,
  )

  override fun getLessonIdToTipsMap(): Map<String, List<String>> = mapOf(
    // Editor
    "swift.completions.basiccompletion" to listOf("CodeCompletionNoShift", "CodeCompletion", "SmartTypeCompletion", "CamelHumpsInCodeCompletion"),
    "swift.editorbasics.selection" to listOf("smart_selection"),
    "swift.editorbasics.commentline" to listOf("comments"),
    "swift.editorbasics.deleteline" to listOf("DeleteLine"),
    "swift.editorbasics.duplicate" to listOf("CtrlD"),
    "swift.editorbasics.move" to listOf("MoveUpDown"),
    "swift.editorbasics.multipleselections" to listOf("Multiselection1", "Multiselection2"),
    "swift.codeassistance.codeformatting" to listOf("LayoutCode"),
    "swift.codeassistance.quickpopups" to listOf("parameter-info", "ExternalJavaDoc", "CtrlShiftIForLookup"),

    // CodeGeneration
    "swift.codegeneration.generate" to listOf("GenerateEqualsHash", "Generate", "GenerateDescription"),
    "swift.codegeneration.overrideimplement" to listOf("OverrideImplementMethods"),
    "swift.codegeneration.createfromusage" to listOf("GenerateFromUsage"),
    "swift.codegeneration.quickfixes" to listOf("QuickFix", "ContextActions"),

    // Navigation
    "swift.navigation.toolwindows" to listOf("StructureToolWindow"),
    "swift.navigation.bookmarks" to listOf("PreviewTODO", "FavoritesToolWindow1", "FavoritesToolWindow2"),
    "swift.navigation.code" to listOf("GoToClass", "CamelPrefixesInNavigationPopups", "GoToSymbol", "GoToDeclaration", "recent-files", "Switcher"),
    "swift.navigation.precise" to listOf("JumpToLastEdit", "GotoNextError", "GotoNextError"),
    "swift.navigation.search" to listOf("find-usages", "HighlightUsagesInFile", "FindInPath", "FindReplaceToggle", "GoToAction"),

    // Refactorings
    "swift.refactorings.rename" to listOf("Rename"),
    "swift.refactorings.extract.variable" to listOf("IntroduceVariable"),
    "swift.refactorings.extract.method" to listOf("ExtractMethod"),
    "swift.refactorings.extract.closure" to listOf("ExtractClosure"),
    "swift.refactorings.change.signature" to listOf("ChangeSignature"),

    // RunDebugTest
    "swift.rdt.run" to listOf("SelectRunDebugConfiguration"),
    "swift.rdt.debug" to listOf("tips_go_RunToCursor", "EvaluateExpression"),
    "swift.rdt.test" to listOf("RunTests"),
  )
}
