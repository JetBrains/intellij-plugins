// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.ruby.ift.lesson.navigation

import training.learn.lesson.general.navigation.SearchEverywhereLesson

class RubySearchEverywhereLesson : SearchEverywhereLesson() {
  override val existedFile = "src/declaration_and_usages_demo.rb"
  override val resultFileName: String = "quadratic_equations_solver.rb"
}