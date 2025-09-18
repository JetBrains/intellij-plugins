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
  fun testExtractSingleTag__options() {
    doExtractTest()
  }

  @Test
  fun testExtractSingleTagWithName__options() {
    doExtractTest()
  }

  @Test
  fun testExtractTwoTagsWithProp__options() {
    doExtractTest(numTags = 2)
  }

  @Test
  fun testExtractTagWithAttributeAndMethodCall__options() {
    doExtractTest()
  }

  @Test
  fun testExtractComponentWithOtherComponentInside__options() {
    doExtractTest()
  }

  @Test
  fun testExtractComponentWithOtherComponentInsideTS__options() {
    doExtractTest()
  }

  @Test
  fun testExtractWithVFor__options() {
    doExtractTest()
  }

  @Test
  @Ignore
  fun testExtractForPug__options() {
    doExtractTest()
  }

  @Test
  fun testSameNamedFunctionCalls__options() {
    doExtractTest(numTags = 4)
  }

  @Test
  fun testSameNamedProps__options() {
    doExtractTest(numTags = 2)
  }

  @Test
  fun testCleanupIfNameIsUsed__options() {
    doExtractTest(newCompName = "dd")
  }

  @Test
  @Ignore
  fun testCleanupPugIfNameIsUsed__options() {
    doExtractTest(newCompName = "dd")
  }

  @Test
  fun testFindImport__options() {
    doExtractTest()
  }

  @Test
  fun testFindNonExistingImport__options() {
    doExtractTest()
  }

  @Test
  fun testExtractWithMemberAccess__options() {
    doExtractTest()
  }

  @Test
  fun testExtractWithStyle__options() {
    doExtractTest()
  }

  @Test
  fun testExtractWithStylusStyle__options() {
    doExtractTest()
  }

  @Test
  fun testExtractWithScss__options() {
    doExtractTest()
  }

  @Test
  fun testExtractSingleTagWithImportInStyle__options() {
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