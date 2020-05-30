// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package training.learn.lesson.python.navigation

import training.learn.interfaces.Module
import training.learn.lesson.general.navigation.DeclarationAndUsagesLesson
import training.learn.lesson.kimpl.LessonContext

class PythonDeclarationAndUsagesLesson(module: Module) : DeclarationAndUsagesLesson(module, "Python") {
  override fun LessonContext.setInitialPosition() = caret(652, 30)
  override val typeOfEntity = "a method"
  override val existedFile: String = "src/jinja2/ext.py"
}
