// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.intellij.javascript.ift.debug

import com.intellij.icons.AllIcons
import com.intellij.lang.javascript.dialects.JSLanguageLevel
import com.intellij.lang.javascript.settings.JSRootConfiguration
import com.intellij.openapi.application.ModalityState
import com.intellij.openapi.editor.impl.DocumentMarkupModel
import training.dsl.LessonContext
import training.dsl.TaskRuntimeContext

fun LessonContext.setLanguageLevel() {
  prepareRuntimeTask(ModalityState.NON_MODAL) {
    JSRootConfiguration.getInstance(project).storeLanguageLevelAndUpdateCaches(JSLanguageLevel.ES6)
  }
}

fun TaskRuntimeContext.lineContainsBreakpoint(line: Int): Boolean {
  val document = editor.document
  val breakpoint = DocumentMarkupModel.forDocument(document, project, true).allHighlighters
    .filter {
      it.gutterIconRenderer?.icon == AllIcons.Debugger.Db_set_breakpoint && document.getLineNumber(it.startOffset) + 1 == line
    }
  return breakpoint.isNotEmpty()
}
