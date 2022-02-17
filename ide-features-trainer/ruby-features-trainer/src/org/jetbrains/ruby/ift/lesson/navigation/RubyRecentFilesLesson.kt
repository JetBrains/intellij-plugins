// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.ruby.ift.lesson.navigation

import training.dsl.LessonContext
import training.learn.lesson.general.navigation.RecentFilesLesson

class RubyRecentFilesLesson : RecentFilesLesson() {
  override val sampleFilePath: String = "src/recent_files_demo.rb"

  override val transitionMethodName: String = "print"
  override val transitionFileName: String = "kernel"
  override val stringForRecentFilesSearch: String = "def print"

  override fun LessonContext.setInitialPosition() = caret(transitionMethodName)
}