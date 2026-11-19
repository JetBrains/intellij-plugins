package org.jetbrains.vuejs.lang

import com.intellij.openapi.application.EDT
import com.intellij.polySymbols.testFramework.hybridFixture
import com.intellij.testFramework.common.timeoutRunBlocking
import com.intellij.testFramework.junit5.TestApplication
import com.maddyhome.idea.copyright.CopyrightProfile
import com.maddyhome.idea.copyright.psi.UpdateCopyrightFactory
import kotlinx.coroutines.Dispatchers
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test
import kotlin.time.Duration.Companion.minutes

@TestApplication
class VueCopyrightTest {
  private val hybrid by hybridFixture("")
  private val myFixture get() = hybrid.fixture

  @Throws(Exception::class)

  private fun updateCopyright() {
    val options = CopyrightProfile()
    options.notice = "first line\nsecond line"
    options.keyword = "Copyright"
    val copyright = UpdateCopyrightFactory.createUpdateCopyright(myFixture.project, myFixture.module,
                                                                 myFixture.file, options)
    assertNotNull(copyright)
    copyright!!.prepare()
    copyright.complete()
  }

  @Test
  fun testUpdateCopyright(): Unit = timeoutRunBlocking(timeout = 2.minutes, context = Dispatchers.EDT) {
    myFixture.configureByText("UpdateCopyright.vue",
                              """<template>Hello!</template>
""")
    updateCopyright()
    myFixture.checkResult(
      """<!--
  - first line
  - second line
  -->

<template>Hello!</template>
""")
  }
}
