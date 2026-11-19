// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.vuejs.lang

import com.intellij.javascript.nodejs.interpreter.NodeJsInterpreterManager
import com.intellij.javascript.nodejs.interpreter.NodeJsInterpreterRef
import com.intellij.javascript.nodejs.interpreter.download.NodeDownloadEditorNotificationProvider
import com.intellij.polySymbols.testFramework.hybridFixture
import com.intellij.testFramework.junit5.TestApplication
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

@TestApplication
class VueNodeDownloadEditorNotificationProviderTest {
  private val hybrid by hybridFixture("")
  private val myFixture get() = hybrid.fixture

  @BeforeEach
  fun setUp() {
    NodeJsInterpreterManager.getInstance(myFixture.project).setInterpreterRef(NodeJsInterpreterRef.create("noop"))
    myFixture.addFileToProject("package.json", "{}")
  }

  @Test
  fun testNotificationAppears() {
    myFixture.configureByText("node_download_notification.vue", "")
    assertNotNull(getNotificationData())
  }

  private fun getNotificationData() =
    NodeDownloadEditorNotificationProvider()
      .collectNotificationData(myFixture.project, myFixture.file.virtualFile)

}