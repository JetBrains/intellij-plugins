// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.vuejs.libraries.vuex

import com.intellij.openapi.application.PathManager
import com.intellij.openapi.util.io.FileUtil
import com.intellij.psi.PsiDocumentManager
import com.intellij.testFramework.UsefulTestCase
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import junit.framework.TestCase
import org.jetbrains.vuejs.lang.createPackageJsonWithVueDependency
import org.jetbrains.vuejs.lang.renderLookupItems
import java.io.File

class VuexCompletionTest : BasePlatformTestCase() {

  override fun getTestDataPath(): String = PathManager.getHomePath() + "/contrib/vuejs/vuejs-tests/testData/vuex/completion"

  fun testBasicGettersCompletion() {
    createPackageJsonWithVueDependency(myFixture, "\"vuex\": \"^3.0.1\"")
    myFixture.configureByFiles("basicGetter.vue", "basicGetter.js")
    myFixture.completeBasic()
    assertContainsElements(myFixture.lookupElementStrings!!, "getter1", "getter_2")
  }

  fun testBasicMutationsCompletion() {
    createPackageJsonWithVueDependency(myFixture, "\"vuex\": \"^3.0.1\"")
    myFixture.configureByFiles("basicMutations.vue", "basicMutations.js")
    myFixture.completeBasic()
    assertContainsElements(myFixture.lookupElementStrings!!, "mutation1")
  }

  fun testBasicMutations2Completion() {
    createPackageJsonWithVueDependency(myFixture, "\"vuex\": \"^3.0.1\"")
    myFixture.configureByFiles("basicMutations2.js")
    myFixture.completeBasic()
    assertContainsElements(myFixture.lookupElementStrings!!, "mutation1")
  }

  fun testBasicActionsCompletion() {
    createPackageJsonWithVueDependency(myFixture, "\"vuex\": \"^3.0.1\"")
    myFixture.configureByFiles("basicActions.vue", "basicActions.js")
    myFixture.completeBasic()
    assertContainsElements(myFixture.lookupElementStrings!!, "action1", "action_2")
  }

  fun testVuexActions2Completion() {
    createPackageJsonWithVueDependency(myFixture, "\"vuex\": \"^3.0.1\"")
    myFixture.configureByFiles("basicActions2.vue", "basicActions2.ts")
    myFixture.completeBasic()
    assertContainsElements(myFixture.lookupElementStrings!!, "action1", "action_2")
  }

  fun testStorefrontDirectGetter() {
    myFixture.configureStorefront()
    doItemsTest(0, "foo() { return this.\$store.getters['<caret>']", true)
    doItemsTest(0, "foo() { return this.store.getters['<caret>']", true)
    doItemsTest(1, "foo() { return this.\$store.getters['cart/<caret>getCoupon']", true)
    doItemsTest(2, "foo() { return this.\$store.getters.<caret>", true, strict = false, renderPriority = true)
    doItemsTest(3, "foo() { return this.\$store.getters[<caret>]", true, strict = false, renderPriority = true)

    doTypingTest("foo() { return this.\$store.getters['<caret>foo/bar']", true,
                 "cartCoupon\t", "this.\$store.getters['cart/getCoupon']")
  }

  fun testStorefrontDirectState() {
    myFixture.configureStorefront()
    doItemsTest(0, "foo() { return this.\$store.state.<caret>", true, strict = false, renderPriority = true)
    doItemsTest(1, "foo() { return this.\$store.state.category.<caret>", true, strict = false, renderPriority = true)
  }

  fun testStorefrontDirectDispatch() {
    myFixture.configureStorefront()
    doItemsTest(0, "foo() { this.\$store.dispatch('<caret>', code) }", false)
    doItemsTest(0, "foo() { this.store.dispatch('<caret>', code) }", false)
    doItemsTest(0, "foo() { this.\$store.dispatch('<caret>cart/applyCoupon', code) }", false)
    doItemsTest(0, "foo() { this.store.dispatch('<caret>cart/applyCoupon', code) }", false)
    doItemsTest(1, "foo() { this.\$store.dispatch('cart/a<caret>', code) }", false)
    doItemsTest(1, "foo() { this.store.dispatch('cart/a<caret>', code) }", false)
    doItemsTest(2, "foo() { this.\$store.dispatch(<caret>, code) }", false, strict = false, renderPriority = true)
    doItemsTest(2, "foo() { this.store.dispatch(<caret>, code) }", false, strict = false, renderPriority = true)

    doTypingTest("foo() { return this.\$store.dispatch('<caret>foo/bar', code)", false,
                 "cartCoupon\t", "('cart/applyCoupon', code)")
  }

  fun testStorefrontDirectCommit() {
    myFixture.configureStorefront()
    doItemsTest(0, "foo() { this.\$store.commit('<caret>', code) }", false)
    doItemsTest(0, "foo() { this.store.commit('<caret>', code) }", false)
    doItemsTest(0, "foo() { this.\$store.commit('<caret>cart/breadcrumbs/set', code) }", false)
    doItemsTest(0, "foo() { this.store.commit('<caret>cart/breadcrumbs/set', code) }", false)
    doItemsTest(1, "foo() { this.\$store.commit('cart/<caret>', code) }", false)
    doItemsTest(1, "foo() { this.store.commit('cart/<caret>', code) }", false)
    doItemsTest(2, "foo() { this.\$store.commit(<caret>, code) }", false, strict = false, renderPriority = true)
    doItemsTest(2, "foo() { this.store.commit(<caret>, code) }", false, strict = false, renderPriority = true)

    doTypingTest("foo() { return this.\$store.commit('<caret>foo/bar', code)", false,
                 "cartset\n", "('cart/breadcrumbs/setfoo/bar', code)")
    doTypingTest("foo() { return this.\$store.commit('<caret>foo/bar', code)", false,
                 "cartset\t", "('cart/breadcrumbs/set', code)")
  }

  private fun doTypingTest(content: String, computedSection: Boolean, toType: String, expectedContents: String, checkJS: Boolean = true) {
    createFile(content, false, computedSection)
    checkTyping(toType, expectedContents)
    if (checkJS) {
      createFile(content, true, computedSection)
      checkTyping(toType, expectedContents)
    }
  }

  private fun checkTyping(toType: String, expectedContents: String) {
    myFixture.completeBasic()
    myFixture.type(toType)
    PsiDocumentManager.getInstance(project).commitAllDocuments()
    TestCase.assertEquals(expectedContents, myFixture.file.findElementAt(myFixture.caretOffset)?.context?.context?.text)
  }

  private fun doItemsTest(id: Int, content: String, computedSection: Boolean, strict: Boolean = true, renderType: Boolean = true,
                          renderPriority: Boolean = false, checkJS: Boolean = true) {
    createFile(content, false, computedSection)
    checkItems(id, strict, renderType, renderPriority)
    if (checkJS) {
      createFile(content, true, computedSection)
      checkItems(id, strict, renderType, renderPriority)
    }
  }

  private fun createFile(content: String, js: Boolean, computedSection: Boolean) {
    val fileContents = """
import {mapActions, mapGetters, mapMutations, mapState} from 'vuex'
import {rootStore} from "aaa"
import {CART_ADD_ITEM, CART_DEL_ITEM} from "./store/cart/mutation-types"

export default {
  ${if (computedSection) "computed" else "methods"}: {
     ${content}
  }
}"""
    myFixture.configureByText("${getTestName(true)}.${if (js) "js" else "ts"}", fileContents)
  }

  private fun checkItems(id: Int, strict: Boolean, renderType: Boolean, renderPriority: Boolean) {
    myFixture.completeBasic()
    val checkFileName: String
    if (File("$testDataPath/${myFixture.file.name}.${id}.txt").exists()) {
      checkFileName = "${myFixture.file.name}.${id}.txt"
    }
    else {
      checkFileName = "${FileUtil.getNameWithoutExtension(myFixture.file.name)}.${id}.txt"
      FileUtil.createIfDoesntExist(File("$testDataPath/$checkFileName"))
    }
    myFixture.renderLookupItems(renderPriority, renderType)
      .let { list ->
        if (strict) {
          myFixture.configureByText("out.txt", list.sorted().joinToString("\n") + "\n")
          myFixture.checkResultByFile(checkFileName, true)
        }
        else {
          val items = myFixture.configureByFile(checkFileName).text.split('\n').filter { !it.isEmpty() }
          UsefulTestCase.assertContainsElements(list, items)
        }
      }
  }
}
