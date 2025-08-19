// Copyright 2000-2025 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.intellij.terraform.copyright

import com.intellij.testFramework.fixtures.BasePlatformTestCase
import com.maddyhome.idea.copyright.CopyrightProfile
import com.maddyhome.idea.copyright.psi.UpdateCopyrightFactory
import org.intellij.terraform.config.TerraformFileType
import org.intellij.terraform.opentofu.OpenTofuFileType

internal class TfCopyrightsProviderTest : BasePlatformTestCase() {
  fun testEmptyTfFile() {
    checkCopyright(
      """
      """.trimIndent(),
      """
        # Copyright 2000-2025 JetBrains s.r.o. and contributors.
        # Use of this source code is governed by the Apache 2.0 license.
        
        
      """.trimIndent(),
      TerraformFileType.defaultExtension
    )
  }

  fun testLineCommentInTofu() {
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
      """.trimIndent(),
      OpenTofuFileType.DEFAULT_EXTENSION
    )
  }

  fun testTfFileWithComment() {
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
      """.trimIndent(),
      TerraformFileType.DEFAULT_EXTENSION
    )
  }

  fun testOutdatedVersionInTofu() {
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
      """.trimIndent(),
      OpenTofuFileType.DEFAULT_EXTENSION
    )
  }

  private fun checkCopyright(before: String, after: String, extension: String) {
    val testName = getTestName(true)
    myFixture.configureByText("$testName.$extension", before)

    val profile = CopyrightProfile()
    profile.notice = "Copyright 2000-2025 JetBrains s.r.o. and contributors.\nUse of this source code is governed by the Apache 2.0 license."

    val updateCopyright = UpdateCopyrightFactory.createUpdateCopyright(project, module, myFixture.getFile(), profile)
    assertNotNull(updateCopyright)

    updateCopyright?.prepare()
    updateCopyright?.complete()
    myFixture.checkResult(after)
  }
}
