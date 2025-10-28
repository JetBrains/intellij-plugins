// Copyright 2000-2025 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.intellij.terraform.terragrunt

import com.intellij.testFramework.fixtures.BasePlatformTestCase
import org.intellij.terraform.hcl.HCLBundle
import org.intellij.terraform.terragrunt.inspection.TerragruntDuplicatedBlocksInspection

class TerragruntInspectionTest : BasePlatformTestCase() {
  fun testDuplicateTerragruntBlocks() {
    myFixture.enableInspections(TerragruntDuplicatedBlocksInspection::class.java)
    val errorMessage = HCLBundle.message("terragrunt.duplicated.blocks.inspection.error.message", "root")
    myFixture.configureByText(TERRAGRUNT_MAIN_FILE, """
      <warning descr="$errorMessage">include "root" {
        path = find_in_parent_folders("root.hcl")
      }</warning>

      <warning descr="$errorMessage">include "root" {
        path = "${'$'}{get_terragrunt_dir()}/../../_env/app.hcl"
      }</warning>
    """.trimIndent())
    myFixture.checkHighlighting(true, false, true)
  }
}