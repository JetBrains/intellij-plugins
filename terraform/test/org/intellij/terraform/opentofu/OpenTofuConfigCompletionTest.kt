// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.intellij.terraform.opentofu

import org.intellij.terraform.config.codeinsight.TfBaseCompletionTestCase

internal class OpenTofuConfigCompletionTest: TfBaseCompletionTestCase() {

  fun testTofuBlockCompletion() {
    val file = myFixture.configureByText("main.tofu", """ 
      terraform {
        <caret>
      }
      """)
    myFixture.testCompletionVariants(file.virtualFile.name, "backend", "cloud", "encryption", "experiments", "required_providers", "required_version")
  }

  fun testTofuEncryptionBlockPropertiesCompletion() {
    val file = myFixture.configureByText("main.tofu", """ 
      terraform {
        encryption {
          <caret>
        }
      }
      """)
    myFixture.testCompletionVariants(file.virtualFile.name, "key_provider", "method", "state", "plan", "remote_state_data_sources")
  }


  fun testTofuEncryptionProvidersCompletion() {
    val file = myFixture.configureByText("main.tofu", """ 
      terraform {
        encryption {
          key_provider "<caret>" "" { }
        }
      }
      """)
    myFixture.testCompletionVariants(file.virtualFile.name, "pbkdf2", "aws_kms", "gcp_kms")
  }

  fun testTofuPbkdf2EncryptionProviderPropertiesCompletion() {
    val file = myFixture.configureByText("main.tofu", """ 
      terraform {
        encryption {
          key_provider "pbkdf2" "" {
            <caret>
          }
        }
      }
      """.trimIndent())
    myFixture.testCompletionVariants(file.virtualFile.name, "passphrase", "key_length", "iterations", "salt_length", "hash_function")
  }

  fun testTofuAwsKmsEncryptionProviderPropertiesCompletion() {
    val file = myFixture.configureByText("main.tofu", """ 
      terraform {
        encryption {
          key_provider "aws_kms" "" {
            <caret>
          }
        }
      }
      """.trimIndent())
    myFixture.testCompletionVariants(file.virtualFile.name, "kms_key_id", "key_spec", "region")
  }

  fun testTofuGcpKmsEncryptionProviderPropertiesCompletion() {
    val file = myFixture.configureByText("main.tofu", """ 
      terraform {
        encryption {
          key_provider "gcp_kms" "" {
            <caret>
          }
        }
      }
      """.trimIndent())
    myFixture.testCompletionVariants(file.virtualFile.name, "kms_encryption_key", "key_length")
  }

  fun testEncryptionMethodTypesCompletion() {
    val file = myFixture.configureByText("main.tofu", """ 
      terraform {
        encryption {
          method "<caret>" "" { }
        }
      }
      """.trimIndent())
    myFixture.testCompletionVariants(file.virtualFile.name, "unencrypted", "aes_gcm")
  }

  fun testAesGcmEncryptionMethodPropertiesCompletion() {
    val file = myFixture.configureByText("main.tofu", """ 
      terraform {
        encryption {
          method "aes_gcm" "aes_enc" {
            <caret>
          }
        }
      }
      """.trimIndent())
    myFixture.testCompletionVariants(file.virtualFile.name, "keys")
  }

  fun testAesGcmEncryptionMethodPropertyValueCompletion() {
    val file = myFixture.configureByText("main.tofu", """ 
      terraform {
        encryption {
          method "aes_gcm" "aes_enc" {
            keys = <caret>
          }
        }
      }
      """.trimIndent())
    myFixture.testCompletionVariants(file.virtualFile.name, "key_provider")
  }


  fun testUnencryptedEncryptionMethodPropertiesCompletion() {
    val file = myFixture.configureByText("main.tofu", """ 
      terraform {
        encryption {
          method "unencrypted" "migrate" {
            <caret>
          }
        }
      }
      """.trimIndent())
    myFixture.testCompletionVariants(file.virtualFile.name)
  }

  fun testStatePropertiesCompletion() {
    val file = myFixture.configureByText("main.tofu", """ 
      terraform {
        encryption {
          state {
            <caret>
          }
        }
      }
      """.trimIndent())
    myFixture.testCompletionVariants(file.virtualFile.name, "enforced", "fallback", "method")
  }

  fun testStateFallbackPropertiesCompletion() {
    val file = myFixture.configureByText("main.tofu", """ 
      terraform {
        encryption {
          state {
            enforced = true
            fallback {
              <caret>
            }
          }
        }
      }
      """.trimIndent())
    myFixture.testCompletionVariants(file.virtualFile.name, "method")
  }

  fun testPlanPropertiesCompletion() {
    val file = myFixture.configureByText("main.tofu", """ 
      terraform {
        encryption {
          plan {
            <caret>
          }
        }
      }
      """.trimIndent())
    myFixture.testCompletionVariants(file.virtualFile.name, "enforced", "fallback", "method")
  }

  fun testPlanFallbackPropertiesCompletion() {
    val file = myFixture.configureByText("main.tofu", """ 
      terraform {
        encryption {
          plan {
            enforced = true
            fallback {
              <caret>
            }
          }
        }
      }
      """.trimIndent())
    myFixture.testCompletionVariants(file.virtualFile.name, "method")
  }

  fun testKeyProviderTypesCompletionTest() {
    val file = myFixture.configureByText("main.tofu", """ 
      terraform {
        encryption {
          key_provider "some_key_provider" "some_name" {
            # Key provider options here
          }
          
          key_provider "another_key_provider" "another_name" {
            # Key provider options here
          }
      
          method "some_method" "some_method_name" {
            # Method options here
            keys = key_provider.<caret>
          }
      }
      """.trimIndent())
    myFixture.testCompletionVariants(file.virtualFile.name, "some_key_provider", "another_key_provider")
  }

  fun testKeyProviderNamesCompletionTest() {
    val file = myFixture.configureByText("main.tofu", """ 
      terraform {
        encryption {
          key_provider "some_key_provider" "some_name" {
            # Key provider options here
          }
          
          key_provider "another_key_provider" "another_name" {
            # Key provider options here
          }
      
          method "some_method" "some_method_name" {
            # Method options here
            keys = key_provider.some_key_provider.<caret>
          }
      }
      """.trimIndent())
    myFixture.testCompletionVariants(file.virtualFile.name, "some_name")
  }

  fun testMethodTypesCompletionTest() {
    val file = myFixture.configureByText("main.tofu", """ 
      terraform {
        encryption {
          key_provider "some_key_provider" "some_name" {
            # Key provider options here
          }
          
          method "some_method" "some_method_name" {
            # Method options here
            keys = key_provider.some_key_provider.some_name
          }

          method "another_method" "another_method_name" {
            # Method options here
            keys = key_provider.some_key_provider.some_name
          }
          
          state {
            # Encryption/decryption for state data
            method = method.<caret>
          }
          
      }
      """.trimIndent())
    myFixture.testCompletionVariants(file.virtualFile.name, "some_method", "another_method")
  }

  fun testMethodPrefixCompletionTest() {
    val file = myFixture.configureByText("main.tofu", """ 
      terraform {
        encryption {
          key_provider "some_key_provider" "some_name" {
            # Key provider options here
          }
          
          method "some_method" "some_method_name" {
            # Method options here
            keys = key_provider.some_key_provider.some_name
          }

          method "another_method" "another_method_name" {
            # Method options here
            keys = key_provider.some_key_provider.some_name
          }
          
          state {
            # Encryption/decryption for state data
            method = <caret>
          }
          
      }
      """.trimIndent())
    myFixture.testCompletionVariants(file.virtualFile.name, "method")
  }


  fun testMethodIdsCompletionTest() {
    val file = myFixture.configureByText("main.tofu", """ 
      terraform {
        encryption {
          key_provider "some_key_provider" "some_name" {
            # Key provider options here
          }
          
          method "some_method" "some_method_name" {
            # Method options here
            keys = key_provider.some_key_provider.some_name
          }

          method "some_method" "some_method_name2" {
            # Method options here
            keys = key_provider.some_key_provider.some_name
          }

          method "another_method" "another_method_name" {
            # Method options here
            keys = key_provider.some_key_provider.some_name
          }
          
          state {
            # Encryption/decryption for state data
            method = method.some_method.<caret>
          }
          
      }
      """.trimIndent())
    myFixture.testCompletionVariants(file.virtualFile.name, "some_method_name", "some_method_name2")
  }


}