// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.angular2.codeInsight

import com.intellij.lang.javascript.JSFixtureOptions
import com.intellij.lang.javascript.JSStructureViewFixture.jsStructureViewFixture
import com.intellij.lang.javascript.StructureViewTestUtil
import com.intellij.lang.javascript.jsProjectFixture
import com.intellij.openapi.application.EDT
import com.intellij.testFramework.common.timeoutRunBlocking
import com.intellij.testFramework.junit5.TestApplication
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.angular2.Angular2TestUtil
import org.junit.jupiter.api.Test

@TestApplication
class Angular2StructureViewTest {
  private val projectFixture = jsProjectFixture()
  private val js by jsStructureViewFixture(
    projectFixture,
    JSFixtureOptions(Angular2TestUtil.getBaseTestDataPath() + "structureView").withBasePath(Angular2TestUtil.getBaseTestDataPath() + "structureView"),
  )

  @Test
  fun testStructure(): Unit = timeoutRunBlocking {
    withContext(Dispatchers.EDT) {
      js.fixture.configureByFiles("Structure.ts", "package.json")
      StructureViewTestUtil.checkStructureView(js.fixture.getEditor())
    }
  }
}
