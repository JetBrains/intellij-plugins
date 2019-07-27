package org.jetbrains.vuejs.lang

import com.intellij.testFramework.fixtures.BasePlatformTestCase
import com.maddyhome.idea.copyright.CopyrightProfile
import com.maddyhome.idea.copyright.psi.UpdateCopyrightFactory
import junit.framework.TestCase

class VueCopyrightTest : BasePlatformTestCase() {
  @Throws(Exception::class)

  private fun updateCopyright() {
    val options = CopyrightProfile()
    options.notice = "first line\nsecond line"
    options.keyword = "Copyright"
    val copyright = UpdateCopyrightFactory.createUpdateCopyright(myFixture.project, myFixture.module,
                                                                 myFixture.file, options)
    TestCase.assertNotNull(copyright)
    copyright!!.prepare()
    copyright.complete()
  }

  fun testUpdateCopyright() {
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
