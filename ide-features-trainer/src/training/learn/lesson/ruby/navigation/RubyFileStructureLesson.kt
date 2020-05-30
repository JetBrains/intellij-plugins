// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package training.learn.lesson.ruby.navigation

import training.learn.interfaces.Module
import training.learn.lesson.general.navigation.FileStructureLesson

class RubyFileStructureLesson(module: Module) : FileStructureLesson(module, "ruby") {
  override val searchSubstring: String = "coo"
  override val firstWord: String = "condition"
  override val secondWord: String = "option"
  override val existedFile: String = "lib/active_support/callbacks.rb"
}
