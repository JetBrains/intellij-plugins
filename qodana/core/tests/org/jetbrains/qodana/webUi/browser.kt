package org.jetbrains.qodana.webUi

import com.intellij.ide.browsers.BrowserLauncher
import com.intellij.ide.browsers.WebBrowser
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.registry.Registry
import java.io.File
import java.nio.file.Path

class TestBrowserLauncher : BrowserLauncher() {
  var timesBrowserOpened = 0

  override fun open(url: String) { }

  override fun browse(file: File) { }

  override fun browse(file: Path) { }

  override fun browse(url: String, browser: WebBrowser?, project: Project?) {
    timesBrowserOpened++
  }
}

fun withUnlimitedRestApiRequests(action: () -> Unit) {
  val key = Registry.get("ide.rest.api.requests.per.minute")
  val currentValue = key.asInteger()
  try {
    key.setValue(Int.MAX_VALUE)
    action.invoke()
  }
  finally {
    key.setValue(currentValue)
  }
}
