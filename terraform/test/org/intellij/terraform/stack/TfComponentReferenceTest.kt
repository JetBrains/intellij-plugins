// Copyright 2000-2026 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.intellij.terraform.stack

import com.intellij.testFramework.fixtures.BasePlatformTestCase
import org.intellij.terraform.config.patterns.TfPsiPatterns
import org.intellij.terraform.hcl.psi.HCLBlock
import org.intellij.terraform.hcl.psi.HCLProperty
import org.intellij.terraform.stack.component.TF_COMPONENT_EXTENSION

internal class TfComponentReferenceTest : BasePlatformTestCase() {
  val fileName: String = "test.$TF_COMPONENT_EXTENSION"

  fun testInputsReference() {
    myFixture.addFileToProject("eks-addon/variables.tf", """
      variable "cluster_certificate_authority_data" {
        type    = string
      }
      variable "oidc_binding_id" {
        type        = string
        description = "used for component dependency"
      }
    """.trimIndent())

    myFixture.configureByText(fileName, """
      component "k8s-addons" {
        source = "./eks-addon"

        inputs = {
          cluster_certificate_authority_data = "some_certificate"
          <caret>oidc_binding_id             = "test_id"
        }
        providers = { }
      }
    """.trimIndent())

    val variable = myFixture.getReferenceAtCaretPosition()?.resolve() as? HCLBlock
                   ?: throw AssertionError("Expected variable block")
    assertEquals("oidc_binding_id", variable.name)
    assertTrue(TfPsiPatterns.VariableRootBlock.accepts(variable))
  }

  fun testProvidersReference() {
    myFixture.addFileToProject("k8s-namespace/versions.tf", """
      terraform {
        required_version = ">= 1.3"
        required_providers {
          helm = {
            source  = "hashicorp/helm"
            version = ">= 2.7"
          }
          kubectl = {
            source  = "alekc/kubectl"
            version = ">= 2.0"
          }
        }
      }
    """.trimIndent())

    myFixture.configureByText(fileName, """
      component "k8s-namespace" {
        source = "./k8s-namespace"
        providers = {
          <caret>helm = provider.helm.helm_v3
          kubectl = provider.kubectl.this
        }
        inputs = { }
      }
    """.trimIndent())
    val requiredProvider = myFixture.getReferenceAtCaretPosition()?.resolve() as? HCLProperty
                           ?: throw AssertionError("Expected required provider property")
    assertEquals("helm", requiredProvider.name)
    assertTrue(TfPsiPatterns.RequiredProvidersProperty.accepts(requiredProvider))
  }
}
