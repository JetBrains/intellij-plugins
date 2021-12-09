// Copyright 2000-2021 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.intellij.javascript.karma.debug

import com.intellij.execution.ExecutionException
import com.intellij.execution.ui.RunContentDescriptor
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.ThrowableComputable
import org.jetbrains.concurrency.Promise

internal fun prepareKarmaDebugger(project: Project, debuggableWebBrowser: DebuggableWebBrowser, producer: ThrowableComputable<RunContentDescriptor, ExecutionException>): Promise<RunContentDescriptor> {
  return debuggableWebBrowser.debugEngine.prepareDebugger(project, debuggableWebBrowser.webBrowser)
    .then { _ -> producer.compute() }
}