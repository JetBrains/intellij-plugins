// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package training.learn.lesson.java.navigation

import training.learn.interfaces.Module
import training.learn.lesson.general.navigation.DeclarationAndUsagesLesson
import training.learn.lesson.kimpl.LessonContext

class JavaDeclarationAndUsagesLesson(module: Module) : DeclarationAndUsagesLesson(module, "JAVA") {
  override fun LessonContext.setInitialPosition() = caret("foo()")
  override val typeOfEntity = 0
  override val existedFile: String = "src/JustDemoClass1.java"
}
