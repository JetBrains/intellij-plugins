// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.ruby.ift.lesson.navigation

import training.dsl.LessonContext
import training.learn.lesson.general.navigation.DeclarationAndUsagesLesson

class RubyDeclarationAndUsagesLesson : DeclarationAndUsagesLesson() {
  override fun LessonContext.setInitialPosition() = caret(11, 19)
  override val sampleFilePath: String = "src/declaration_and_usages_demo.rb"
  override val entityName = "discriminant"
}
