// Copyright 2000-2025 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.intellij.terraform.terragrunt

import com.intellij.lang.Language
import org.intellij.terraform.config.CompletionTestCase
import org.intellij.terraform.hil.HILLanguage

internal class TerragruntHilCompletionTest : CompletionTestCase() {
  override fun getFileName(): String = TERRAGRUNT_MAIN_FILE
  override fun getExpectedLanguage(): Language = HILLanguage

  fun testHilTerragruntFunctions() {
    doBasicCompletionTest($$"""
      terraform {
        source = "${get_par<caret>("root")}/modules/vpc"
      }
    """.trimIndent(), "get_parent_terragrunt_dir", "get_path_from_repo_root", "get_path_to_repo_root", "get_terraform_commands_that_need_parallelism")

    doBasicCompletionTest($$"""
      terraform {
        extra_arguments "common_var" {
          arguments = [
            "-var-file=${get_terragrunt_dir()}/${path_relative<caret>}/common.tfvars",
          ]
        }
      }
    """.trimIndent(), "path_relative_from_include", "path_relative_to_include")
  }
}