// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.intellij.terraform.opentofu

import com.intellij.testFramework.fixtures.BasePlatformTestCase
import org.intellij.terraform.hcl.HCLBundle
import org.intellij.terraform.opentofu.inspection.OpenTofuUnknownEncryptionMethodInspection
import org.intellij.terraform.opentofu.inspection.OpenTofuUnknownKeyProviderInspection

internal class OpenTofuInspectionTest: BasePlatformTestCase() {

  fun testDetectUnknownKeyProvider() {
    myFixture.enableInspections(OpenTofuUnknownKeyProviderInspection::class.java)
    val errorMessage = HCLBundle.message("opentofu.unknown.key.provider.inspection.message", "unknown")
    myFixture.configureByText("test.tofu", """
      terraform {
        encryption {
           key_provider "pbkdf2" "foo" {
            passphrase = "correct-horse-battery-staple"
            key_length = 32
            iterations = 600000
            salt_length = 32
            hash_function = "sha512"
          }
      
          method "aes_gcm" "yourname" {
            keys = key_provider.pbkdf2.<error descr="$errorMessage">unknown</error>
          }
          method "aes_gcm" "yourname2" {
            keys = key_provider.pbkdf2.foo
          }
        }
      }
    """.trimIndent())
    myFixture.checkHighlighting(true, false, true)
  }

  fun testDetectUnknownMethod() {
    myFixture.enableInspections(OpenTofuUnknownEncryptionMethodInspection::class.java)
    val errorMessage = HCLBundle.message("opentofu.unknown.encryption.method.inspection.message", "unknown")
    myFixture.configureByText("test.tofu", """
      terraform {
        encryption {
           key_provider "pbkdf2" "foo" {
            passphrase = "correct-horse-battery-staple"
            key_length = 32
            iterations = 600000
            salt_length = 32
            hash_function = "sha512"
          }
      
          method "aes_gcm" "yourname" {
            keys = key_provider.pbkdf2.foo
          }

          state {
            method = method.aes_gcm.yourname
          }
          
          plan {
            method = method.aes_gcm.<error descr="${errorMessage}">unknown</error>
          }
        }        
      }
    """.trimIndent())
    myFixture.checkHighlighting(true, false, true)
  }


}