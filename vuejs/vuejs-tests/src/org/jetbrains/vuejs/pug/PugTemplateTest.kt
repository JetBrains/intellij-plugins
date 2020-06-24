// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.vuejs.pug

import com.intellij.lang.javascript.inspections.JSIncompatibleTypesComparisonInspection
import com.intellij.openapi.application.PathManager
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import org.jetbrains.vuejs.lang.createPackageJsonWithVueDependency

class PugTemplateTest : BasePlatformTestCase() {

  override fun getTestDataPath(): String = PathManager.getHomePath() + "/contrib/vuejs/vuejs-tests/testData/pug/"

  fun testJadeExtendsResolve() {
    createPackageJsonWithVueDependency(myFixture)
    myFixture.copyDirectoryToProject("jadeExtends", ".")
    myFixture.configureFromTempProjectFile("test.vue")
    myFixture.checkHighlighting()
  }

  fun testPugExtendsResolve() {
    createPackageJsonWithVueDependency(myFixture)
    myFixture.copyDirectoryToProject("pugExtends", ".")
    myFixture.configureFromTempProjectFile("test.vue")
    myFixture.checkHighlighting()
  }

  fun testInjectedExpressions() {
    myFixture.enableInspections(JSIncompatibleTypesComparisonInspection())
    myFixture.configureByFile("injectedExpressions.vue")
    myFixture.checkHighlighting()
  }

}
