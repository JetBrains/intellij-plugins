// Copyright 2000-2021 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.vuejs.service

import com.intellij.javascript.nodejs.library.yarn.pnp.YarnPnpManager
import com.intellij.lang.javascript.buildTools.npm.PackageJsonUtil
import com.intellij.lang.javascript.modules.JSTempDirFixtureOptions
import com.intellij.lang.javascript.nodejs.library.yarn.YarnPnpIntegrationFixture.Companion.yarnPnpIntegrationFixture
import com.intellij.openapi.application.ReadAction
import com.intellij.polySymbols.context.PolyContext
import com.intellij.testFramework.junit5.TestApplication
import org.jetbrains.vuejs.lang.VueInspectionsProvider
import org.jetbrains.vuejs.lang.vueRelativeTestDataPath
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test

@TestApplication
class VueYarnPnpIntegrationTest {
  private val yarn by yarnPnpIntegrationFixture(JSTempDirFixtureOptions.forBasePath(vueRelativeTestDataPath() + "/yarnPnp"))

  @Test
  fun testVue2() {
    val root = yarn.fixture.copyDirectoryToProject("vue2", ".")
    yarn.configureYarnBerryAndRunYarnInstall(root)
    val yarnPnpManager = YarnPnpManager.getInstance(yarn.project)
    assertNotNull(yarnPnpManager.findInstalledPackageDir(PackageJsonUtil.findChildPackageJsonFile(root)!!, "vue"))
    yarn.fixture.configureFromTempProjectFile("test.vue")
    yarn.fixture.enableInspections(VueInspectionsProvider())
    yarn.fixture.checkHighlighting()
  }

  @Test
  fun testNuxt() {
    val root = yarn.fixture.copyDirectoryToProject("nuxt", ".")
    yarn.configureYarnBerryAndRunYarnInstall(root)
    val file = yarn.fixture.tempDirFixture.getFile("index.html")!!
    ReadAction.run<Throwable> {
      assertEquals("vue", PolyContext.get("framework", file, yarn.project))
    }
  }

}
