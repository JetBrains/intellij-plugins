package com.intellij.javascript.karma.debug

import com.intellij.execution.ExecutionException
import com.intellij.execution.ui.RunContentDescriptor
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.ThrowableComputable
import org.jetbrains.concurrency.Promise

fun prepareKarmaDebugger(project: Project, debuggableWebBrowser: DebuggableWebBrowser, producer: ThrowableComputable<RunContentDescriptor, ExecutionException>): Promise<RunContentDescriptor> {
  return debuggableWebBrowser.debugEngine.prepareDebugger(project, debuggableWebBrowser.webBrowser)
    .then { it -> producer.compute() }
}