// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package training.learn.lesson.java.navigation

import training.learn.interfaces.Module
import training.learn.lesson.general.navigation.SearchEverywhereLesson

class JavaSearchEverywhereLesson(module: Module) : SearchEverywhereLesson(module, "JAVA") {
  override val existedFile = "src/RecentFilesDemo.java"
  override val resultFileName: String = "QuadraticEquationsSolver.java"
}
