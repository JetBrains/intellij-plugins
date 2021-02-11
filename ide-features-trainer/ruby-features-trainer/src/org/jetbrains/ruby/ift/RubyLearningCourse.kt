// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.ruby.ift

import org.jetbrains.plugins.ruby.ruby.lang.RubyLanguage
import org.jetbrains.ruby.ift.lesson.assistance.RubyEditorCodingAssistanceLesson
import org.jetbrains.ruby.ift.lesson.basic.RubyContextActionsLesson
import org.jetbrains.ruby.ift.lesson.basic.RubySelectLesson
import org.jetbrains.ruby.ift.lesson.basic.RubySurroundAndUnwrapLesson
import org.jetbrains.ruby.ift.lesson.completion.RubyBasicCompletionLesson
import org.jetbrains.ruby.ift.lesson.completion.RubyCompletionWithTabLesson
import org.jetbrains.ruby.ift.lesson.completion.RubyHippieCompletionLesson
import org.jetbrains.ruby.ift.lesson.completion.RubyPostfixCompletionLesson
import org.jetbrains.ruby.ift.lesson.navigation.RubyDeclarationAndUsagesLesson
import org.jetbrains.ruby.ift.lesson.navigation.RubyFileStructureLesson
import org.jetbrains.ruby.ift.lesson.navigation.RubyRecentFilesLesson
import org.jetbrains.ruby.ift.lesson.navigation.RubySearchEverywhereLesson
import org.jetbrains.ruby.ift.lesson.refactorings.RubyRefactorMenuLesson
import org.jetbrains.ruby.ift.lesson.refactorings.RubyRenameLesson
import training.learn.LearningModule
import training.learn.LessonsBundle
import training.learn.course.LearningCourseBase
import training.learn.interfaces.LessonType
import training.learn.lesson.general.*
import training.learn.lesson.general.assistance.CodeFormatLesson
import training.learn.lesson.general.assistance.ParameterInfoLesson
import training.learn.lesson.general.assistance.QuickPopupsLesson
import training.learn.lesson.general.navigation.FindInFilesLesson
import training.learn.lesson.general.refactorings.ExtractMethodCocktailSortLesson
import training.learn.lesson.general.refactorings.ExtractVariableFromBubbleLesson

class RubyLearningCourse : LearningCourseBase(RubyLanguage.INSTANCE.id) {
  override fun modules() = listOf(
    LearningModule(name = LessonsBundle.message("editor.basics.module.name"),
                   description = LessonsBundle.message("editor.basics.module.description"),
                   primaryLanguage = langSupport,
                   moduleType = LessonType.SCRATCH) {
      fun ls(sampleName: String) = loadSample("EditorBasics/$sampleName")
      listOf(
        RubyContextActionsLesson(),
        GotoActionLesson(lang, ls("Actions.rb.sample")),
        RubySelectLesson(),
        SingleLineCommentLesson(lang, ls("Comment.rb.sample")),
        DuplicateLesson(lang, ls("Duplicate.rb.sample")),
        MoveLesson(lang, "set(v)", ls("Move.rb.sample")),
        CollapseLesson(lang, ls("Collapse.rb.sample")),
        RubySurroundAndUnwrapLesson(),
        MultipleSelectionHtmlLesson(),
      )
    },
    LearningModule(name = LessonsBundle.message("code.completion.module.name"),
                   description = LessonsBundle.message("code.completion.module.description"),
                   primaryLanguage = langSupport,
                   moduleType = LessonType.SINGLE_EDITOR) {
      listOf(
        RubyBasicCompletionLesson(),
        RubyHippieCompletionLesson(),
        RubyPostfixCompletionLesson(),
        RubyCompletionWithTabLesson(),
      )
    },
    LearningModule(name = LessonsBundle.message("refactorings.module.name"),
                   description = LessonsBundle.message("refactorings.module.description"),
                   primaryLanguage = langSupport,
                   moduleType = LessonType.SINGLE_EDITOR) {
      fun ls(sampleName: String) = loadSample("Refactorings/$sampleName")
      listOf(
        RubyRefactorMenuLesson(),
        RubyRenameLesson(),
        ExtractVariableFromBubbleLesson(lang, ls("ExtractVariable.rb.sample")),
        ExtractMethodCocktailSortLesson(lang, ls("ExtractMethod.rb.sample")),
      )
    },
    LearningModule(name = LessonsBundle.message("code.assistance.module.name"),
                   description = LessonsBundle.message("code.assistance.module.description"),
                   primaryLanguage = langSupport,
                   moduleType = LessonType.SINGLE_EDITOR) {
      fun ls(sampleName: String) = loadSample("CodeAssistance/$sampleName")
      listOf(
        CodeFormatLesson(lang, ls("CodeFormat.rb.sample"), false),
        ParameterInfoLesson(lang, ls("ParameterInfo.rb.sample")),
        QuickPopupsLesson(lang, ls("QuickPopups.rb.sample")),
        RubyEditorCodingAssistanceLesson(lang, ls("EditorCodingAssistance.rb.sample")),
      )
    },
    LearningModule(name = LessonsBundle.message("navigation.module.name"),
                   description = LessonsBundle.message("navigation.module.description"),
                   primaryLanguage = langSupport,
                   moduleType = LessonType.PROJECT) {
      listOf(
        RubySearchEverywhereLesson(),
        FindInFilesLesson(lang, "src/warehouse/find_in_files_sample.rb"),
        RubyDeclarationAndUsagesLesson(),
        RubyFileStructureLesson(),
        RubyRecentFilesLesson(),
      )
    },
  )
}