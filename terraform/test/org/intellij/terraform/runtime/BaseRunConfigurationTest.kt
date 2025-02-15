// Copyright 2000-2025 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.intellij.terraform.runtime

import com.intellij.testFramework.fixtures.BasePlatformTestCase
import com.intellij.testFramework.fixtures.TempDirTestFixture
import com.intellij.testFramework.fixtures.impl.TempDirTestFixtureImpl
import org.intellij.terraform.TfTestUtils
import org.intellij.terraform.install.TfToolType

internal abstract class BaseRunConfigurationTest : BasePlatformTestCase() {

  override fun getTestDataPath(): String = TfTestUtils.getTestDataPath() + "/runtime"

  override fun createTempDirTestFixture(): TempDirTestFixture {
    return TempDirTestFixtureImpl()
  }

  override fun setUp() {
    super.setUp()
    TfProjectSettings.getInstance(myFixture.project).toolPath = TfToolType.TERRAFORM.getBinaryName()
  }
}