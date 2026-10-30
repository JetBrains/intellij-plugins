package com.intellij.lang.javascript.frameworks.nextjs

import com.intellij.lang.javascript.JSFixtureOptions
import com.intellij.lang.javascript.JSMoveFixture.jsMoveFixture
import com.intellij.lang.javascript.jsProjectFixture
import com.intellij.testFramework.junit5.TestApplication
import org.junit.jupiter.api.Test

@TestApplication
class NextJsMoveTest {
  private val projectFixture = jsProjectFixture()
  private val js by jsMoveFixture(projectFixture, JSFixtureOptions(NextJsTestUtil.getTestDataPath()).withBasePath("/move/"))

  @Test
  fun testForm() {
    js.doTest("pages/component.js", "pages/target")
  }

  @Test
  fun testPlaceholder() {
    js.doTest("pages/post", "pages/test")
  }
}