// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package training.learn.lesson.ruby.navigation

import training.learn.interfaces.Module
import training.learn.lesson.general.navigation.DeclarationAndUsagesLesson
import training.learn.lesson.kimpl.LessonContext

class RubyDeclarationAndUsagesLesson(module: Module) : DeclarationAndUsagesLesson(module, "ruby") {
  override fun LessonContext.setInitialPosition() = caret(20, 45)
  override val typeOfEntity = "an attribute accessor"
  override val existedFile: String = "lib/active_support/core_ext/date/calculations.rb"
}
