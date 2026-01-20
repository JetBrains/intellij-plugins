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
open class VueExtractComponentTest(
  useTsc: Boolean = true,
) : VueTestCase("extract_component", useTsc = useTsc) {

  class WithoutServiceTest :
    VueExtractComponentTest(useTsc = false)

  @Test
  fun testExtractSingleTag__options() {
    doExtractTest()
  }

  @Test
  fun testExtractSingleTag__composition() {
    doExtractTest()
  }

  @Test
  fun testExtractSingleTag__vapor() {
    doExtractTest()
  }

  @Test
  fun testExtractSingleTagWithName__options() {
    doExtractTest()
  }

  @Test
  fun testExtractSingleTagWithName__composition() {
    doExtractTest()
  }

  @Test
  fun testExtractSingleTagWithName__vapor() {
    doExtractTest()
  }

  @Test
  fun testExtractTwoTagsWithProp__options() {
    doExtractTest(numTags = 2)
  }

  @Test
  fun testExtractTwoTagsWithProp__composition() {
    doExtractTest(numTags = 2)
  }

  @Test
  fun testExtractTwoTagsWithProp__vapor() {
    doExtractTest(numTags = 2)
  }

  @Test
  fun testExtractTagWithAttributeAndMethodCall__options() {
    doExtractTest()
  }

  @Test
  fun testExtractTagWithAttributeAndMethodCall__composition() {
    doExtractTest()
  }

  @Test
  fun testExtractTagWithAttributeAndMethodCall__vapor() {
    doExtractTest()
  }

  @Test
  fun testExtractComponentWithOtherComponentInside__options() {
    doExtractTest()
  }

  @Test
  fun testExtractComponentWithOtherComponentInside__composition() {
    doExtractTest()
  }

  @Test
  fun testExtractComponentWithOtherComponentInside__vapor() {
    doExtractTest()
  }

  @Test
  fun testExtractComponentWithOtherComponentInsideTS__options() {
    doExtractTest()
  }

  @Test
  fun testExtractComponentWithOtherComponentInsideTS__composition() {
    doExtractTest()
  }

  @Test
  fun testExtractComponentWithOtherComponentInsideTS__vapor() {
    doExtractTest()
  }

  @Test
  fun testExtractWithVFor__options() {
    doExtractTest()
  }

  @Test
  fun testExtractWithVFor__composition() {
    doExtractTest()
  }

  @Test
  fun testExtractWithVFor__vapor() {
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
  fun testSameNamedFunctionCalls__composition() {
    doExtractTest(numTags = 4)
  }

  @Test
  fun testSameNamedFunctionCalls__vapor() {
    doExtractTest(numTags = 4)
  }

  @Test
  fun testSameNamedProps__options() {
    doExtractTest(numTags = 2)
  }

  @Test
  fun testSameNamedProps__composition() {
    doExtractTest(numTags = 2)
  }

  @Test
  fun testSameNamedProps__vapor() {
    doExtractTest(numTags = 2)
  }

  @Test
  fun testCleanupIfNameIsUsed__options() {
    doExtractTest(newCompName = "dd")
  }

  @Test
  fun testCleanupIfNameIsUsed__composition() {
    doExtractTest(newCompName = "dd")
  }

  @Test
  fun testCleanupIfNameIsUsed__vapor() {
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
  fun testFindImport__composition() {
    doExtractTest()
  }

  @Test
  fun testFindImport__vapor() {
    doExtractTest()
  }

  @Test
  fun testFindNonExistingImport__options() {
    doExtractTest()
  }

  @Test
  fun testFindNonExistingImport__composition() {
    doExtractTest()
  }

  @Test
  fun testFindNonExistingImport__vapor() {
    doExtractTest()
  }

  @Test
  fun testExtractWithMemberAccess__options() {
    doExtractTest()
  }

  @Test
  fun testExtractWithMemberAccess__composition() {
    doExtractTest()
  }

  @Test
  fun testExtractWithMemberAccess__vapor() {
    doExtractTest()
  }

  @Test
  fun testExtractWithStyle__options() {
    doExtractTest()
  }

  @Test
  fun testExtractWithStyle__composition() {
    doExtractTest()
  }

  @Test
  fun testExtractWithStyle__vapor() {
    doExtractTest()
  }

  @Test
  fun testExtractWithStylusStyle__options() {
    doExtractTest()
  }

  @Test
  fun testExtractWithStylusStyle__composition() {
    doExtractTest()
  }

  @Test
  fun testExtractWithStylusStyle__vapor() {
    doExtractTest()
  }

  @Test
  fun testExtractWithScss__options() {
    doExtractTest()
  }

  @Test
  fun testExtractWithScss__composition() {
    doExtractTest()
  }

  @Test
  fun testExtractWithScss__vapor() {
    doExtractTest()
  }

  @Test
  fun testExtractSingleTagWithImportInStyle__options() {
    doExtractTest()
  }

  @Test
  fun testExtractSingleTagWithImportInStyle__composition() {
    doExtractTest()
  }

  @Test
  fun testExtractSingleTagWithImportInStyle__vapor() {
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