// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package training.learn.lesson.python.navigation

import training.learn.interfaces.Module
import training.learn.lesson.general.navigation.FileStructureLesson

class PythonFileStructureLesson(module: Module) : FileStructureLesson(module, "Python") {
  override val searchSubstring: String = "caf"
  override val firstWord: String = "cache"
  override val secondWord: String = "file"
  override val existedFile: String = "src/jinja2/bccache.py"
}
