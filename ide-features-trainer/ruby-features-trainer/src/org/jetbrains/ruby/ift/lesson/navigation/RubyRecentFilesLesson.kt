// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.ruby.ift.lesson.navigation

import training.learn.lesson.general.navigation.RecentFilesLesson
import training.learn.lesson.kimpl.LessonContext

class RubyRecentFilesLesson : RecentFilesLesson("ruby") {
  override val existedFile: String = "src/recent_files_demo.rb"

  override val transitionMethodName: String = "print"
  override val transitionFileName: String = "kernel"
  override val stringForRecentFilesSearch: String = transitionMethodName

  override fun LessonContext.setInitialPosition() = caret(transitionMethodName)
}