// Copyright 2000-2021 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.vuejs.libraries.i18n

import com.intellij.testFramework.fixtures.BasePlatformTestCase
import org.jetbrains.vuejs.lang.VueInspectionsProvider
import org.jetbrains.vuejs.lang.createPackageJsonWithVueDependency
import org.jetbrains.vuejs.lang.getVueTestDataPath

class I18nTest : BasePlatformTestCase() {

  override fun getTestDataPath(): String = getVueTestDataPath() + "/libraries/i18n"

  fun testHighlighting() {
    createPackageJsonWithVueDependency(myFixture, """ "vue-i18n": "*" """)
    myFixture.enableInspections(VueInspectionsProvider())
    myFixture.configureByFiles("highlighting.vue")
    myFixture.checkHighlighting()
  }

}