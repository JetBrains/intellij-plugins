// Copyright 2000-2025 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.intellij.terraform.config.refactoring

import com.intellij.openapi.application.PathManager
import com.intellij.testFramework.fixtures.BasePlatformTestCase

class TfRenameHandlerTest : BasePlatformTestCase() {
  fun testRenameResource() {
    myFixture.testRenameUsingHandler("resource.tf", "resource_after.tf", "new_name")
  }

  fun testRenameDataSource() {
    myFixture.testRenameUsingHandler("datasource.tf", "datasource_after.tf", "new_role")
  }

  fun testRenameLocalVariable() {
    myFixture.testRenameUsingHandler("local.tf", "local_after.tf", "new_local")
  }

  fun testRenameReservedWord() {
    assertThrows(AssertionError::class.java, "No handler for this context") {
      myFixture.testRenameUsingHandler("reserved_word.tf", "reserved_word_after.tf", "new_name")
    }
  }

  override fun getTestDataPath(): String {
    return PathManager.getHomePath() + "/contrib/terraform/test-data/terraform/refactoring"
  }
}
