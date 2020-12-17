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
        RubyContextActionsLesson(it),
        GotoActionLesson(it, lang, ls("Actions.rb.sample")),
        RubySelectLesson(it),
        SingleLineCommentLesson(it, lang, ls("Comment.rb.sample")),
        DuplicateLesson(it, lang, ls("Duplicate.rb.sample")),
        MoveLesson(it, lang, "set(v)", ls("Move.rb.sample")),
        CollapseLesson(it, lang, ls("Collapse.rb.sample")),
        RubySurroundAndUnwrapLesson(it),
        MultipleSelectionHtmlLesson(it),
      )
    },
    LearningModule(name = LessonsBundle.message("code.completion.module.name"),
                   description = LessonsBundle.message("code.completion.module.description"),
                   primaryLanguage = langSupport,
                   moduleType = LessonType.SINGLE_EDITOR) {
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
                   moduleType = LessonType.SINGLE_EDITOR) {
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
                   moduleType = LessonType.SINGLE_EDITOR) {
      fun ls(sampleName: String) = loadSample("CodeAssistance/$sampleName")
      listOf(
        CodeFormatLesson(it, lang, ls("CodeFormat.rb.sample"), false),
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