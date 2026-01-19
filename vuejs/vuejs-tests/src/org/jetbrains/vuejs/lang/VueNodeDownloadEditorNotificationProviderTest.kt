// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.vuejs.lang

import com.intellij.javascript.nodejs.interpreter.NodeJsInterpreterManager
import com.intellij.javascript.nodejs.interpreter.NodeJsInterpreterRef
import com.intellij.javascript.nodejs.interpreter.download.NodeDownloadEditorNotificationProvider
import com.intellij.testFramework.fixtures.BasePlatformTestCase

class VueNodeDownloadEditorNotificationProviderTest : BasePlatformTestCase() {

  public override fun setUp() {
    super.setUp()
    NodeJsInterpreterManager.getInstance(myFixture.project).setInterpreterRef(NodeJsInterpreterRef.create("noop"))
    myFixture.addFileToProject("package.json", "{}")
  }

  fun testNotificationAppears() {
    myFixture.configureByText("node_download_notification.vue", "")
    assertNotNull(getNotificationData())
  }

  private fun getNotificationData() =
    NodeDownloadEditorNotificationProvider()
      .collectNotificationData(myFixture.project, myFixture.file.virtualFile)

}