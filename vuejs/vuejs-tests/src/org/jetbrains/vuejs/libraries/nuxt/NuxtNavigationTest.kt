// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.vuejs.libraries.nuxt

import com.intellij.lang.javascript.JSTestUtils
import com.intellij.psi.xml.XmlToken
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import junit.framework.TestCase
import org.jetbrains.vuejs.lang.VueInspectionsProvider
import org.jetbrains.vuejs.lang.VueTestModule
import org.jetbrains.vuejs.lang.configureVueDependencies
import org.jetbrains.vuejs.lang.getVueTestDataPath
import org.jetbrains.vuejs.libraries.nuxt.library.resetDotNuxtFolderManager

class NuxtNavigationTest : BasePlatformTestCase() {

  override fun getTestDataPath(): String = getVueTestDataPath() + "/libraries/nuxt/highlighting"

  override fun setUp() {
    super.setUp()
    resetDotNuxtFolderManager(project)
  }

  fun testNuxtGlobalComponents() {
    myFixture.enableInspections(VueInspectionsProvider())
    myFixture.copyDirectoryToProject("nuxtGlobalComponents", ".")
    myFixture.configureVueDependencies(VueTestModule.VUE_3_2_2)
    val app = myFixture.configureFromTempProjectFile("app.vue")
    awaitDotNuxtFolderProcessing(project)

    val unnested = JSTestUtils.findElementByText(myFixture, "Unnested", XmlToken::class.java)
    myFixture.editor.caretModel.moveToOffset(unnested.textOffset + 1)
    val unnestedTarget = JSTestUtils.getGotoDeclarationTarget(myFixture)
    TestCase.assertEquals("Unnested.vue", unnestedTarget!!.containingFile.name)

    myFixture.openFileInEditor(app.virtualFile)
    val my1 = JSTestUtils.findElementByText(myFixture, "My1", XmlToken::class.java)
    myFixture.editor.caretModel.moveToOffset(my1.textOffset + 1)
    val my1target = JSTestUtils.getGotoDeclarationTarget(myFixture)
    TestCase.assertEquals("My1.vue", my1target!!.containingFile.name)
  }
}