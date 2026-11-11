// Copyright 2000-2026 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.intellij.prettierjs.next

import com.intellij.javascript.nodejs.library.yarn.pnp.YarnPnpNodePackage
import com.intellij.javascript.nodejs.util.NodePackage
import com.intellij.javascript.nodejs.util.NodePackageRef
import com.intellij.lang.javascript.buildTools.npm.PackageJsonUtil
import com.intellij.lang.javascript.modules.TestNpmPackage
import com.intellij.lang.javascript.nodejs.library.yarn.YarnPnpIntegrationFixture
import com.intellij.openapi.vfs.newvfs.impl.VfsRootAccess
import com.intellij.prettierjs.PrettierConfiguration
import com.intellij.prettierjs.PrettierUtil
import com.intellij.prettierjs.stable.PRETTIER_LATEST_TEST_PACKAGE_SPEC
import com.intellij.prettierjs.stable.ReformatWithPrettierGenericTest
import com.intellij.testFramework.junit5.TestApplication
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertInstanceOf
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test

/**
 * Prettier tests running against the latest version (prettier@latest).
 * Note: These tests may fail when Prettier releases breaking changes.
 */
@TestApplication
@TestNpmPackage(PRETTIER_LATEST_TEST_PACKAGE_SPEC)
class ReformatWithPrettierLatestTest : ReformatWithPrettierGenericTest() {
  @Test
  fun testNextVersion(): Unit = prettier.withInstallation {
    prettier.doReformatFile<Throwable>("toReformat", "js") {
      prettier.performNpmInstallForPackageJson("package.json")
      val nextPrettier = NodePackage(prettier.fixture.tempDirPath + "/node_modules/prettier")
      PrettierConfiguration.getInstance(prettier.project).withLinterPackage(NodePackageRef.create(nextPrettier))
    }
  }

  /**
   * Tests Prettier integration with Yarn Berry PnP (Plug'n'Play).
   * Installs Yarn globally, configures Yarn Berry, and uses YarnPnpNodePackage for resolution.
   */
  @Test
  fun testYarnPrettierBasicExample(): Unit = prettier.withInstallation {
    prettier.doReformatFile<Throwable>("toReformat", "js") {
      val file = prettier.fixture.findFileInTempDir("toReformat.js")
      val root = file.parent
      val yarnPkg = YarnPnpIntegrationFixture.installYarnGlobally(prettier.nodeJsAppRule)
      VfsRootAccess.allowRootAccess(prettier.testRootDisposable, yarnPkg.systemIndependentPath)
      YarnPnpIntegrationFixture.configureYarnBerryAndRunYarnInstall(prettier.project, yarnPkg, prettier.nodeJsAppRule, root)
      configureYarnPrettierPackage(root)
    }
  }

  private fun configureYarnPrettierPackage(root: com.intellij.openapi.vfs.VirtualFile) {
    val configuration = PrettierConfiguration.getInstance(prettier.project)
    val packageJsonFile = PackageJsonUtil.findChildPackageJsonFile(root)
    assertNotNull(packageJsonFile, "package.json not found in $root")
    val yarnPrettierPkg = YarnPnpNodePackage.create(
      prettier.project,
      packageJsonFile!!,
      PrettierUtil.PACKAGE_NAME, false, false
    )
    assertNotNull(yarnPrettierPkg)
    configuration.withLinterPackage(NodePackageRef.create(yarnPrettierPkg!!))
    val readYarnPrettierPkg = configuration.getPackage(null)
    assertInstanceOf(YarnPnpNodePackage::class.java, readYarnPrettierPkg)
    assertEquals("yarn:package.json:prettier", readYarnPrettierPkg.systemIndependentPath)
  }
}
