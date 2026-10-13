// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.intellij.javascript.ift.debug

import com.intellij.icons.AllIcons
import com.intellij.openapi.editor.impl.DocumentMarkupModel
import training.dsl.TaskRuntimeContext

internal fun TaskRuntimeContext.lineContainsBreakpoint(line: Int): Boolean {
  val document = editor.document
  val breakpoint = DocumentMarkupModel.forDocument(document, project, true).allHighlighters
    .filter {
      it.isValid && it.gutterIconRenderer?.icon == AllIcons.Debugger.Db_set_breakpoint && document.getLineNumber(it.startOffset) + 1 == line
    }
  return breakpoint.isNotEmpty()
}
