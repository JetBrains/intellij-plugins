// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package training.learn.course

import org.jetbrains.plugins.ruby.ruby.lang.RubyLanguage
import training.learn.LearningModule
import training.learn.LessonsBundle
import training.learn.interfaces.LessonType
import training.learn.lesson.general.*
import training.learn.lesson.general.assistance.CodeFormatLesson
import training.learn.lesson.general.assistance.ParameterInfoLesson
import training.learn.lesson.general.assistance.QuickPopupsLesson
import training.learn.lesson.general.refactorings.ExtractMethodCocktailSortLesson
import training.learn.lesson.general.refactorings.ExtractVariableFromBubbleLesson
import training.learn.lesson.ruby.assistance.RubyEditorCodingAssistanceLesson
import training.learn.lesson.ruby.basic.RubySurroundAndUnwrapLesson
import training.learn.lesson.ruby.completion.RubyBasicCompletionLesson
import training.learn.lesson.ruby.completion.RubyCompletionWithTabLesson
import training.learn.lesson.ruby.completion.RubyHippieCompletionLesson
import training.learn.lesson.ruby.completion.RubyPostfixCompletionLesson
import training.learn.lesson.ruby.navigation.RubyDeclarationAndUsagesLesson
import training.learn.lesson.ruby.navigation.RubyFileStructureLesson
import training.learn.lesson.ruby.navigation.RubyRecentFilesLesson
import training.learn.lesson.ruby.navigation.RubySearchEverywhereLesson
import training.learn.lesson.ruby.refactorings.RubyRefactorMenuLesson
import training.learn.lesson.ruby.refactorings.RubyRenameLesson

class RubyLearningCourse : LearningCourseBase(RubyLanguage.INSTANCE.id) {
  override fun modules() = listOf(
    LearningModule(name = LessonsBundle.message("editor.basics.module.name"),
                   description = LessonsBundle.message("editor.basics.module.description"),
                   primaryLanguage = langSupport,
                   moduleType = LessonType.SCRATCH) {
      fun ls(sampleName: String) = loadSample("EditorBasics/$sampleName")
      listOf(
        GotoActionLesson(it, lang, ls("Actions.rb.sample")),
        SelectLesson(it, lang, ls("Selection.rb.sample")),
        SingleLineCommentLesson(it, lang, ls("Comment.rb.sample")),
        DuplicateLesson(it, lang, ls("Duplicate.rb.sample")),
        MoveLesson(it, lang, ls("Move.rb.sample")),
        CollapseLesson(it, lang, ls("Collapse.rb.sample")),
        RubySurroundAndUnwrapLesson(it),
        MultipleSelectionHtmlLesson(it),
      )
    },
    LearningModule(name = LessonsBundle.message("code.completion.module.name"),
                   description = LessonsBundle.message("code.completion.module.description"),
                   primaryLanguage = langSupport,
                   moduleType = LessonType.PROJECT) {
      listOf(
        RubyBasicCompletionLesson(it),
        RubyHippieCompletionLesson(it),
        RubyPostfixCompletionLesson(it),
        RubyCompletionWithTabLesson(it),
      )
    },
    LearningModule(name = LessonsBundle.message("refactorings.module.name"),
                   description = LessonsBundle.message("refactorings.module.description"),
                   primaryLanguage = langSupport,
                   moduleType = LessonType.PROJECT) {
      fun ls(sampleName: String) = loadSample("Refactorings/$sampleName")
      listOf(
        RubyRefactorMenuLesson(it),
        RubyRenameLesson(it),
        ExtractVariableFromBubbleLesson(it, lang, ls("ExtractVariable.rb.sample")),
        ExtractMethodCocktailSortLesson(it, lang, ls("ExtractMethod.rb.sample")),
      )
    },
    LearningModule(name = LessonsBundle.message("code.assistance.module.name"),
                   description = LessonsBundle.message("code.assistance.module.description"),
                   primaryLanguage = langSupport,
                   moduleType = LessonType.PROJECT) {
      fun ls(sampleName: String) = loadSample("CodeAssistance/$sampleName")
      listOf(
        CodeFormatLesson(it, lang, ls("CodeFormat.rb.sample")),
        ParameterInfoLesson(it, lang, ls("ParameterInfo.rb.sample")),
        QuickPopupsLesson(it, lang, ls("QuickPopups.rb.sample")),
        RubyEditorCodingAssistanceLesson(it, lang, ls("EditorCodingAssistance.rb.sample")),
      )
    },
    LearningModule(name = LessonsBundle.message("navigation.module.name"),
                   description = LessonsBundle.message("navigation.module.description"),
                   primaryLanguage = langSupport,
                   moduleType = LessonType.PROJECT) {
      listOf(
        RubyDeclarationAndUsagesLesson(it),
        RubyFileStructureLesson(it),
        RubyRecentFilesLesson(it),
        RubySearchEverywhereLesson(it)
      )
    },
  )
}