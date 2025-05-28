// Copyright 2000-2025 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.intellij.terraform.config.codeinsight

internal class TfDynamicBlocksCompletionTestCase : TfBaseCompletionTestCase() {

  fun testResourceForEachIteratorCompletion() {
    doBasicCompletionTest(
      """
        resource "aws_security_group" "x" {
          dynamic "ingress" {
            for_each = local.admin_locations
            content {
              cidr_blocks = <caret>
            }
          }
        }
      """.trimIndent(), "ingress"
    )

    doBasicCompletionTest(
      """
        resource "aws_security_group" "x" {
          dynamic "ingress" {
            for_each = local.admin_locations
            iterator = location
            content {
              cidr_blocks = <caret>
            }
          }
        }
      """.trimIndent(), "location"
    )

    doBasicCompletionTest(
      """
        resource "aws_security_group" "x" {
          dynamic "ingress" {
            for_each = local.admin_locations
            content {
              cidr_blocks = <caret>aaa.value
            }
          }
        }
      """.trimIndent(), "ingress"
    )

    doBasicCompletionTest(
      """
        resource "aws_security_group" "x" {
          dynamic "ingress" {
            for_each = local.admin_locations
            iterator = location
            content {
              cidr_blocks = <caret>aaa.value
            }
          }
        }
      """.trimIndent(), "location"
    )
  }

  fun testResourceSelectFromForEachIteratorCompletion() {
    doBasicCompletionTest(
      """
        resource "aws_security_group" "x" {
          dynamic "ingress" {
            for_each = local.admin_locations
            content {
              cidr_blocks = ingress.<caret>
            }
          }
        }
      """.trimIndent(), "key", "value"
    )

    doBasicCompletionTest(
      """
        resource "aws_security_group" "x" {
          dynamic "ingress" {
            for_each = local.admin_locations
            iterator = location
            content {
              cidr_blocks = location.<caret>
            }
          }
        }
      """.trimIndent(), "key", "value"
    )
  }

  fun testResourceSelectFromForEachValueIteratorCompletion() {
    val locals = """
      locals {
        admin_locations = [
          {
            cidr_range = "xx.xxx.xxx.xxx/xx",
            description = "Office"
          }
        ]
      }
    """.trimIndent()

    doBasicCompletionTest(
      locals + """
        resource "aws_security_group" "x" {
          dynamic "ingress" {
            for_each = local.admin_locations
            content {
              cidr_blocks = ingress.value.<caret>
            }
          }
        }
      """.trimIndent(), "cidr_range", "description"
    )

    doBasicCompletionTest(
      locals + """
        resource "aws_security_group" "x" {
          dynamic "ingress" {
            for_each = local.admin_locations
            iterator = location
            content {
              cidr_blocks = location.value.<caret>
            }
          }
        }
      """.trimIndent(), "cidr_range", "description"
    )
  }
}