// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.vuejs.service

import com.intellij.codeInsight.completion.CompletionType
import com.intellij.lang.javascript.service.JSLanguageService
import com.intellij.lang.javascript.service.JSLanguageServiceBase
import com.intellij.lang.javascript.service.JSLanguageServiceProvider
import com.intellij.lang.javascript.typescript.service.TypeScriptServiceTestBase
import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.testFramework.JSUnit38AssumeSupportRunner
import com.intellij.util.containers.ContainerUtil
import com.intellij.util.ui.UIUtil
import org.jetbrains.vuejs.lang.VueTestModule
import org.jetbrains.vuejs.lang.configureDependencies
import org.jetbrains.vuejs.lang.typescript.service.VueTypeScriptService
import org.jetbrains.vuejs.lang.vueRelativeTestDataPath
import org.junit.runner.RunWith

@RunWith(JSUnit38AssumeSupportRunner::class)
class VueTypeScriptServiceTest : TypeScriptServiceTestBase() {
  override fun getService(): JSLanguageServiceBase {
    val services = JSLanguageServiceProvider.getLanguageServices(project)
    return ContainerUtil.find(services) { el: JSLanguageService? -> el is VueTypeScriptService } as JSLanguageServiceBase
  }

  override fun getExtension(): String {
    return "vue"
  }

  private fun completeTsLangAndAssert() {
    doTestWithCopyDirectory()
    myFixture.type(" lang=\"\bts\"")
    FileDocumentManager.getInstance().saveDocument(myFixture.getDocument(myFixture.file))
    UIUtil.dispatchAllInvocationEvents()
    checkAfterFile("vue")
  }

  override fun getBasePath(): String {
    return vueRelativeTestDataPath() + BASE_PATH
  }

  @TypeScriptVersion(TypeScriptVersions.TS26)
  fun testSimpleVue() {
    doTestWithCopyDirectory()
    myFixture.configureByFile("SimpleVueNoTs.vue")
    checkHighlightingByOptions(false)
  }

  @TypeScriptVersion(TypeScriptVersions.TS26)
  @Throws(Exception::class)
  fun testSimpleCompletion() {
    checkBaseStringQualifiedCompletionWithTemplates(
      {
        doTestWithCopyDirectory()
        myFixture.complete(
          CompletionType.BASIC)
      }, true)
  }

  @TypeScriptVersion(TypeScriptVersions.TS26)
  fun testSimpleVueNoTs() {
    doTestWithCopyDirectory()
    myFixture.configureByFile("SimpleVue.vue")
    checkHighlightingByOptions(false)
  }

  @TypeScriptVersion(TypeScriptVersions.TS26)
  fun testSimpleVueEditing() {
    doTestWithCopyDirectory()
    myFixture.type('\b')
    checkAfterFile("vue")
    myFixture.type('s')
    checkAfterFile("2.vue")
  }

  @TypeScriptVersion(TypeScriptVersions.TS26)
  fun testSimpleVueEditingNoTs() {
    completeTsLangAndAssert()
  }

  @TypeScriptVersion(TypeScriptVersions.TS26)
  fun testSimpleVueEditingNoTsNoRefs() {
    completeTsLangAndAssert()
  }

  @TypeScriptVersion(TypeScriptVersions.TS26)
  fun testSimpleVueEditingCloseTag() {
    doTestWithCopyDirectory()
    myFixture.type('\b')
    checkAfterFile("vue")
    myFixture.type('/')
    checkAfterFile("2.vue")
  }

  @TypeScriptVersion(TypeScriptVersions.TS26)
  fun testSimpleVueTsx() {
    doTestWithCopyDirectory()
  }

  @TypeScriptVersion(TypeScriptVersions.TS26)
  fun testNoScriptSection() {
    myFixture.configureDependencies(VueTestModule.VUE_2_5_3)
    doTestWithCopyDirectory()
    myFixture.configureByFile("NoScriptSectionImport.vue")
    checkHighlightingByOptions(false)
  }

  @TypeScriptVersion(TypeScriptVersions.TS26)
  fun testNoScriptSectionVue3() {
    myFixture.configureDependencies(VueTestModule.VUE_3_0_0)
    doTestWithCopyDirectory()
    myFixture.configureByFile("main.ts")
    checkHighlightingByOptions(false)
  }

  @TypeScriptVersion(TypeScriptVersions.TS26)
  fun testConfigScope() {
    doTestWithCopyDirectory()
  }

  @TypeScriptVersion(TypeScriptVersions.TS26)
  fun testSimpleVueTsToTsx() {
    doTestWithCopyDirectory()
    myFixture.type('x')
    checkAfterFile("vue")
  }

  @TypeScriptVersion(TypeScriptVersions.TS26)
  fun testSimpleVueTsxToTs() {
    doTestWithCopyDirectory()
    myFixture.type('\b')
    checkAfterFile("vue")
  }

  companion object {
    private const val BASE_PATH = "/ts_ls_highlighting"
  }
}