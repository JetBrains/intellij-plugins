// Copyright 2000-2026 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.intellij.flex.completion

import com.intellij.codeInsight.lookup.LookupElement
import com.intellij.flex.util.FlexTestUtils
import com.intellij.lang.javascript.JSCompletionInTextFieldFixture
import com.intellij.lang.javascript.JSFixtureOptions
import com.intellij.lang.javascript.flex.FlexModuleType
import com.intellij.lang.javascript.flex.FlexSupportLoader
import com.intellij.lang.javascript.psi.JSExpressionCodeFragment
import com.intellij.lang.javascript.psi.JSFile
import com.intellij.lang.javascript.psi.ecmal4.JSClass
import com.intellij.lang.javascript.psi.impl.JSChangeUtil
import com.intellij.lang.javascript.psi.impl.JSPsiImplUtils
import com.intellij.openapi.application.runReadActionBlocking
import com.intellij.openapi.project.Project
import com.intellij.testFramework.fixtures.CodeInsightTestFixture
import com.intellij.testFramework.junit5.fixture.TestFixture
import org.jetbrains.annotations.TestOnly
import org.junit.jupiter.api.Assertions.assertTrue
import java.lang.reflect.Method

/**
 * The helpers of a test of the ActionScript completion in a text field, on a module of the Flex type.
 * Create it with [flexCompletionInTextFieldFixture].
 *
 * The test data path is [BASE_PATH] in the Flex test data. The setup allows the Flex VFS roots until the end of the test.
 */
@TestOnly
class FlexCompletionInTextFieldFixture(
  fixture: CodeInsightTestFixture,
  testClass: Class<*>,
  testMethod: Method,
  options: JSFixtureOptions,
) : JSCompletionInTextFieldFixture(fixture, testClass, testMethod, options) {
  override fun setUp() {
    super.setUp()
    FlexTestUtils.allowFlexVfsRootsFor(testRootDisposable, "")
  }

  /**
   * Sets up the Flex SDK of the module, when the `FlexTestOptions` of the test ask for it.
   */
  fun setUpJdk() {
    FlexTestUtils.setupFlexSdk(fixture.module, getTestName(false), testClass, testRootDisposable)
  }

  /**
   * Runs the completion in [fragment] with the text of [file], and checks that the lookup has [included] and not [excluded].
   * When [choose] is not `null`, it selects this item and checks the `<test name>_after.txt` file.
   */
  fun checkTextFieldCompletion(
    fragment: JSExpressionCodeFragment,
    included: Array<String>,
    excluded: Array<String>,
    choose: String?,
    file: String,
  ) {
    doTestTextFieldFromFile(fragment, file) {
      assertContains(fixture.lookupElements!!, true, *included)
      assertContains(fixture.lookupElements!!, false, *excluded)
      if (choose != null) {
        var found = false
        for (item in fixture.lookupElements!!) {
          if (choose == item.lookupString) {
            fixture.lookup.currentItem = item
            fixture.type('\n')
            found = true
            break
          }
        }
        assertTrue(found, "Item '$choose' not found in lookup")
        fixture.checkResultByFile(getTestName(false) + "_after.txt")
      }
    }
  }

  /**
   * Creates the ActionScript class `Foo` with the function `a` outside the project files, in a read action.
   */
  fun createFakeClass(): JSClass? = runReadActionBlocking {
    JSPsiImplUtils.findClass(JSChangeUtil.createJSTreeFromText(project, "package {class Foo { function a() {}} }", FlexSupportLoader.ECMA_SCRIPT_L4)!!
                               .psi.containingFile as JSFile)
  }

  companion object {
    const val BASE_PATH: String = "/js2_completion/"

    @JvmField
    val DEFALUT_VALUES: Array<String> = arrayOf("true", "false", "null", "NaN", "Infinity")

    /**
     * Creates a [FlexCompletionInTextFieldFixture] on a new module of the Flex type in [projectFixture].
     * Declare the result as an instance field, so that each test gets its own fixture.
     */
    @JvmStatic
    @TestOnly
    fun flexCompletionInTextFieldFixture(projectFixture: TestFixture<Project>): TestFixture<FlexCompletionInTextFieldFixture> =
      jsCompletionInTextFieldFixture(
        "flexCompletionInTextFieldFixture", projectFixture,
        JSFixtureOptions(FlexTestUtils.getTestDataPath(BASE_PATH)).withModuleType(FlexModuleType.MODULE_TYPE_ID),
        ::FlexCompletionInTextFieldFixture,
      )

    private fun assertContains(items: Array<LookupElement>, contains: Boolean, vararg expected: String) {
      val c = expected.toHashSet()
      for (item in items) {
        val s = item.lookupString
        val removed = c.remove(s)
        if (!contains) {
          assertTrue(!removed, "'$s' is not expected to be part of completion list")
        }
      }
      if (contains) {
        assertTrue(c.isEmpty(), "Items [" + c.joinToString(",") + "] are expected to be part of completion list")
      }
    }
  }
}
