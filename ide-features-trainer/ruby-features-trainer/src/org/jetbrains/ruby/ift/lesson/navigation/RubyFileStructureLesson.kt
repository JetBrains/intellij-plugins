// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.ruby.ift.lesson.navigation

import com.intellij.openapi.editor.LogicalPosition
import training.learn.lesson.general.navigation.FileStructureLesson

class RubyFileStructureLesson : FileStructureLesson() {
  override val existedFile: String = "src/file_structure_demo.rb"
  override val methodToFindPosition: LogicalPosition = LogicalPosition(80, 6)
}
