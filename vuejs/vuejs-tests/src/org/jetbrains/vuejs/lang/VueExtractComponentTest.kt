// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.vuejs.lang

import org.jetbrains.vuejs.VueTestCase
import org.jetbrains.vuejs.intentions.extractComponent.VueExtractComponentRefactoring
import org.jetbrains.vuejs.intentions.extractComponent.getContextForExtractComponentIntention
import org.junit.Ignore
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

@RunWith(JUnit4::class)
class VueExtractComponentTest :
  VueTestCase("extract_component") {

  @Test
  fun testExtractSingleTag() {
    doExtractTest()
  }

  @Test
  fun testExtractSingleTagWithName() {
    doExtractTest()
  }

  @Test
  fun testExtractTwoTagsWithProp() {
    doExtractTest(numTags = 2)
  }

  @Test
  fun testExtractTagWithAttributeAndMethodCall() {
    doExtractTest()
  }

  @Test
  fun testExtractComponentWithOtherComponentInside() {
    doExtractTest()
  }

  @Test
  fun testExtractComponentWithOtherComponentInsideTS() {
    doExtractTest()
  }

  @Test
  fun testExtractWithVFor() {
    doExtractTest()
  }

  @Test
  @Ignore
  fun testExtractForPug() {
    doExtractTest()
  }

  @Test
  fun testSameNamedFunctionCalls() = doExtractTest(
    4)

  @Test
  fun testSameNamedProps() = doExtractTest(
    2)

  @Test
  fun testCleanupIfNameIsUsed() {
    doExtractTest(newCompName = "dd")
  }

  @Test
  @Ignore
  fun testCleanupPugIfNameIsUsed() {
    doExtractTest(newCompName = "dd")
  }

  @Test
  fun testFindImport() {
    doExtractTest()
  }

  @Test
  fun testFindNonExistingImport() {
    doExtractTest()
  }

  @Test
  fun testExtractWithMemberAccess() {
    doExtractTest()
  }

  @Test
  fun testExtractWithStyle() {
    doExtractTest()
  }

  @Test
  fun testExtractWithStylusStyle() {
    doExtractTest()
  }

  @Test
  fun testExtractWithScss() {
    doExtractTest()
  }

  @Test
  fun testExtractSingleTagWithImportInStyle() {
    doExtractTest()
  }

  private fun doExtractTest(
    numTags: Int = 1,
    newCompName: String = "NewComponent",
  ) {
    doConfiguredTest(
      VueTestModule.VUE_3_6_0,
      dir = true,
      checkResult = true,
      configureFileName = "App.vue",
    ) {
      val element = file.findElementAt(editor.caretModel.currentCaret.offset)
      assertNotNull(element)
      val context = getContextForExtractComponentIntention(editor, element!!)
      assertNotNull(context)
      assertEquals(numTags, context!!.size)

      val refactoring = VueExtractComponentRefactoring(
        project = project,
        list = context,
        editor = editor,
      )

      refactoring.perform(newCompName)
    }
  }
}