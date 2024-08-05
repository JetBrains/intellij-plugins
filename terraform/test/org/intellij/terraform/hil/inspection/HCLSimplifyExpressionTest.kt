// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.intellij.terraform.hil.inspection

import com.intellij.testFramework.fixtures.BasePlatformTestCase

internal class HCLSimplifyExpressionTest: BasePlatformTestCase() {

  fun testSimplifyExistingExpression() {
    myFixture.configureByText("main.tf", """
      variable "instance_types" {
        type        = list(string)
        default     = ["t2.micro", "t2.small", "t2.medium", "t2.large"]
      }

      variable "index" {
        type        = number
        default     = 2
      }

      output "selected_instance_type" {
        description = "The selected instance type from the list"
        value       = <weak_warning descr="Can be replaced with list indexing (may change semantics)">elem<caret>ent(var.instance_types, var.index)</weak_warning>
      }
    """.trimIndent())
    myFixture.enableInspections(HCLSimplifyExpressionInspection())
    myFixture.checkHighlighting()
    myFixture.availableIntentions.firstOrNull()?.asIntention()?.invoke(myFixture.project, myFixture.editor, myFixture.file)
    myFixture.checkResult("""
      variable "instance_types" {
        type        = list(string)
        default     = ["t2.micro", "t2.small", "t2.medium", "t2.large"]
      }

      variable "index" {
        type        = number
        default     = 2
      }

      output "selected_instance_type" {
        description = "The selected instance type from the list"
        value       = var.instance_types[var.index]
      }
    """.trimIndent())
  }

  fun testDoNotSuggestFixForComplexExpression() {
    myFixture.configureByText("main.tf", """
      output "selected_list_element" {
        description = "The selected instance type from the list using custom element2 function"
        value       = elem<caret>ent(["a", "b", "c"], length(["a", "b", "c"])-1)
      }
    """.trimIndent())
    myFixture.enableInspections(HCLSimplifyExpressionInspection())
    myFixture.checkHighlighting()
    assertEmpty(myFixture.availableIntentions)
  }

}