// Copyright 2000-2025 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.intellij.terraform.copyright

import com.intellij.openapi.fileTypes.LanguageFileType
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import com.maddyhome.idea.copyright.CopyrightProfile
import com.maddyhome.idea.copyright.psi.UpdateCopyrightFactory
import org.intellij.terraform.config.TerraformFileType
import org.intellij.terraform.opentofu.OpenTofuFileType
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized

@RunWith(Parameterized::class)
internal class TfCopyrightsProviderTest(private val fileType: LanguageFileType) : BasePlatformTestCase() {

  @Test
  fun testEmptyFile() {
    checkCopyright(
      """
      """.trimIndent(),
      """
        # Copyright 2000-2025 JetBrains s.r.o. and contributors.
        # Use of this source code is governed by the Apache 2.0 license.
        
        
      """.trimIndent()
    )
  }

  @Test
  fun testLineComment() {
    checkCopyright(
      """
        resource "null_resource" "example" {
        }
      """.trimIndent(),
      """
        # Copyright 2000-2025 JetBrains s.r.o. and contributors.
        # Use of this source code is governed by the Apache 2.0 license.

        resource "null_resource" "example" {
        }
      """.trimIndent()
    )
  }

  @Test
  fun testFileWithComment() {
    checkCopyright(
      """
        # Some serious comment

        terraform {
          required_version = "1.8.0"
          required_providers {
            aws = {
              source  = "hashicorp/aws"
              version = "5.99.1"
            }
          }
        }
      """.trimIndent(),
      """
        # Copyright 2000-2025 JetBrains s.r.o. and contributors.
        # Use of this source code is governed by the Apache 2.0 license.

        # Some serious comment

        terraform {
          required_version = "1.8.0"
          required_providers {
            aws = {
              source  = "hashicorp/aws"
              version = "5.99.1"
            }
          }
        }
      """.trimIndent()
    )
  }

  @Test
  fun testOutdatedVersion() {
    checkCopyright(
      """
        # Copyright 2000-2023 JetBrains s.r.o. and contributors.
        # Use of this source code is governed by the Apache 2.0 license.

        resource "aws_instance" "test" {
          id = "some_id"
        }
      """.trimIndent(),
      """
        # Copyright 2000-2025 JetBrains s.r.o. and contributors.
        # Use of this source code is governed by the Apache 2.0 license.

        resource "aws_instance" "test" {
          id = "some_id"
        }
      """.trimIndent()
    )
  }

  private fun checkCopyright(before: String, after: String) {
    val testName = getTestName(true)
    myFixture.configureByText("$testName.${fileType.defaultExtension}", before)

    val profile = CopyrightProfile()
    profile.notice = "Copyright 2000-2025 JetBrains s.r.o. and contributors.\nUse of this source code is governed by the Apache 2.0 license."

    val updateCopyright = UpdateCopyrightFactory.createUpdateCopyright(project, module, myFixture.getFile(), profile)
    assertNotNull(updateCopyright)

    updateCopyright?.prepare()
    updateCopyright?.complete()
    myFixture.checkResult(after)
  }

  companion object {
    @JvmStatic
    @Parameterized.Parameters(name = "{0}")
    fun testData(): List<LanguageFileType> = listOf(TerraformFileType, OpenTofuFileType)
  }
}
