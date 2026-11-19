// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.vuejs.libraries.nuxt

import com.intellij.lang.javascript.JSTestUtils
import com.intellij.openapi.application.EDT
import com.intellij.polySymbols.testFramework.hybridFixture
import com.intellij.psi.xml.XmlToken
import com.intellij.testFramework.common.timeoutRunBlocking
import com.intellij.testFramework.junit5.TestApplication
import kotlinx.coroutines.Dispatchers
import org.jetbrains.vuejs.lang.VueInspectionsProvider
import org.jetbrains.vuejs.lang.VueTestModule
import org.jetbrains.vuejs.lang.configureVueDependencies
import org.jetbrains.vuejs.lang.getVueTestDataPath
import org.jetbrains.vuejs.libraries.nuxt.library.resetDotNuxtFolderManager
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import kotlin.time.Duration.Companion.minutes

@TestApplication
class NuxtNavigationTest {
  private val hybrid by hybridFixture(getVueTestDataPath() + "/libraries/nuxt/highlighting")
  private val myFixture get() = hybrid.fixture
  private val project get() = myFixture.project

  @BeforeEach
  fun setUp() {
    resetDotNuxtFolderManager(project)
  }

  @Test
  fun testNuxtGlobalComponents(): Unit = timeoutRunBlocking(timeout = 2.minutes, context = Dispatchers.EDT) {
    myFixture.enableInspections(VueInspectionsProvider())
    myFixture.copyDirectoryToProject("nuxtGlobalComponents", ".")
    myFixture.configureVueDependencies(VueTestModule.VUE_3_2_2)
    val app = myFixture.configureFromTempProjectFile("app.vue")
    awaitDotNuxtFolderProcessing(project)

    val unnested = JSTestUtils.findElementByText(myFixture, "Unnested", XmlToken::class.java)
    myFixture.editor.caretModel.moveToOffset(unnested.textOffset + 1)
    val unnestedTarget = JSTestUtils.getGotoDeclarationTarget(myFixture)
    assertEquals("Unnested.vue", unnestedTarget!!.containingFile.name)

    myFixture.openFileInEditor(app.virtualFile)
    val my1 = JSTestUtils.findElementByText(myFixture, "My1", XmlToken::class.java)
    myFixture.editor.caretModel.moveToOffset(my1.textOffset + 1)
    val my1target = JSTestUtils.getGotoDeclarationTarget(myFixture)
    assertEquals("My1.vue", my1target!!.containingFile.name)
  }
}