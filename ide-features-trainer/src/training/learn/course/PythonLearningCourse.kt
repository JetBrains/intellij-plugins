// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package training.learn.course

import com.jetbrains.python.PythonLanguage
import training.learn.LearningModule
import training.learn.LessonsBundle
import training.learn.interfaces.ModuleType
import training.learn.lesson.general.*
import training.learn.lesson.general.assistance.CodeFormatLesson
import training.learn.lesson.general.assistance.ParameterInfoLesson
import training.learn.lesson.general.assistance.QuickPopupsLesson
import training.learn.lesson.general.refactorings.ExtractMethodCocktailSortLesson
import training.learn.lesson.general.refactorings.ExtractVariableFromBubbleLesson
import training.learn.lesson.python.assistance.PythonEditorCodingAssistanceLesson
import training.learn.lesson.python.basic.PythonSurroundAndUnwrapLesson
import training.learn.lesson.python.completion.*
import training.learn.lesson.python.navigation.PythonDeclarationAndUsagesLesson
import training.learn.lesson.python.navigation.PythonFileStructureLesson
import training.learn.lesson.python.navigation.PythonSearchEverywhereLesson
import training.learn.lesson.python.refactorings.PythonInPlaceRefactoringLesson
import training.learn.lesson.python.refactorings.PythonQuickFixesRefactoringLesson
import training.learn.lesson.python.refactorings.PythonRefactorMenuLesson
import training.learn.lesson.python.refactorings.PythonRenameLesson
import training.learn.lesson.python.run.PythonDebugLesson
import training.learn.lesson.python.run.PythonRunConfigurationLesson

class PythonLearningCourse : LearningCourseBase(PythonLanguage.INSTANCE.id) {
  override fun modules() = listOf(
    LearningModule(name = LessonsBundle.message("editor.basics.module.name"),
                   description = LessonsBundle.message("editor.basics.module.description"),
                   sanitizedName = "Editor",
                   primaryLanguage = langSupport,
                   moduleType = ModuleType.SCRATCH) {
      fun ls(sampleName: String) = loadSample("EditorBasics/$sampleName")
      listOf(
        GotoActionLesson(it, lang, ls("Actions.py.sample")),
        SelectLesson(it, lang, ls("Selection.py.sample")),
        SingleLineCommentLesson(it, lang, ls("Comment.py.sample")),
        DuplicateLesson(it, lang, ls("Duplicate.py.sample")),
        MoveLesson(it, lang, ls("Move.py.sample")),
        CollapseLesson(it, lang, ls("Collapse.py.sample")),
        PythonSurroundAndUnwrapLesson(it),
        MultipleSelectionHtmlLesson(it),
      )
    },
    LearningModule(name = LessonsBundle.message("code.completion.module.name"),
                   description = LessonsBundle.message("code.completion.module.description"),
                   sanitizedName = "Completion",
                   primaryLanguage = langSupport,
                   moduleType = ModuleType.SCRATCH) {
      listOf(
        PythonBasicCompletionLesson(it),
        PythonTabCompletionLesson(it),
        PythonPostfixCompletionLesson(it),
        PythonSmartCompletionLesson(it),
        FStringCompletionLesson(it),
      )
    },
    LearningModule(name = LessonsBundle.message("refactorings.module.name"),
                   description = LessonsBundle.message("refactorings.module.description"),
                   sanitizedName = "Refactorings",
                   primaryLanguage = langSupport,
                   moduleType = ModuleType.SCRATCH) {
      fun ls(sampleName: String) = loadSample("Refactorings/$sampleName")
      listOf(
        PythonRefactorMenuLesson(it),
        PythonRenameLesson(it),
        ExtractVariableFromBubbleLesson(it, lang, ls("ExtractVariable.py.sample")),
        ExtractMethodCocktailSortLesson(it, lang, ls("ExtractMethod.py.sample")),
        PythonQuickFixesRefactoringLesson(it),
        PythonInPlaceRefactoringLesson(it),
      )
    },
    LearningModule(name = LessonsBundle.message("code.assistance.module.name"),
                   description = LessonsBundle.message("code.assistance.module.description"),
                   sanitizedName = "Assistance",
                   primaryLanguage = langSupport,
                   moduleType = ModuleType.PROJECT) {
      fun ls(sampleName: String) = loadSample("CodeAssistance/$sampleName")
      listOf(
        CodeFormatLesson(it, lang, ls("CodeFormat.py.sample")),
        ParameterInfoLesson(it, lang, ls("ParameterInfo.py.sample")),
        QuickPopupsLesson(it, lang, ls("QuickPopups.py.sample")),
        PythonEditorCodingAssistanceLesson(it, lang, ls("EditorCodingAssistance.py.sample")),
      )
    },
    LearningModule(name = LessonsBundle.message("navigation.module.name"),
                   description = LessonsBundle.message("navigation.module.description"),
                   sanitizedName = "Navigation",
                   primaryLanguage = langSupport,
                   moduleType = ModuleType.PROJECT) {
      listOf(
        PythonDeclarationAndUsagesLesson(it),
        PythonFileStructureLesson(it),
        PythonSearchEverywhereLesson(it),
      )
    },
    LearningModule(name = LessonsBundle.message("run.debug.module.name"),
                   description = LessonsBundle.message("run.debug.module.description"),
                   sanitizedName = "Run",
                   primaryLanguage = langSupport,
                   moduleType = ModuleType.PROJECT) {
      listOf(
        PythonRunConfigurationLesson(it),
        PythonDebugLesson(it),
      )
    },
  )
}