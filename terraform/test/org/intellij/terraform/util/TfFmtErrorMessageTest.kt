// Copyright 2000-2025 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.intellij.terraform.util

import junit.framework.TestCase
import org.intellij.terraform.hcl.formatter.normalizeAnsiText

internal class TfFmtErrorMessageTest : TestCase() {

  fun testResourceErrorMessage() {
    val before = """
      [31m[31m╷[0m[0m
      [31m│[0m [0m[1m[31mError: [0m[0m[1mArgument or block definition required[0m
      [31m│[0m [0m
      [31m│[0m [0m[0m  on <stdin> line 5, in resource "aws_secretsmanager_secret" "this":
      [31m│[0m [0m   5:   [4mname_prefix[0m[0m
      [31m│[0m [0m
      [31m│[0m [0mAn argument or block definition is required here. To set an argument, use
      [31m│[0m [0mthe equals sign "=" to introduce the argument value.
      [31m╵[0m[0m
      [0m[0m
    """.trimIndent()
    val after = normalizeAnsiText(before)

    assertEquals("""
      Error: Argument or block definition required
       
         on <stdin> line 5, in resource "aws_secretsmanager_secret" "this":
          5:   name_prefix
       
       An argument or block definition is required here. To set an argument, use
       the equals sign "=" to introduce the argument value.
    """.trimIndent(), after)
  }

  fun testTfBlockErrorMessage() {
    val before = """
      [31m[31m╷[0m[0m
      [31m│[0m [0m[1m[31mError: [0m[0m[1mArgument or block definition required[0m
      [31m│[0m [0m
      [31m│[0m [0m[0m  on <stdin> line 3, in terraform:
      [31m│[0m [0m   3:   [4mrequired_providers[0m[0m
      [31m│[0m [0m
      [31m│[0m [0mAn argument or block definition is required here. To set an argument, use
      [31m│[0m [0mthe equals sign "=" to introduce the argument value.
      [31m╵[0m[0m
      [0m[0m
    """.trimIndent()
    val after = normalizeAnsiText(before)

    assertEquals("""
      Error: Argument or block definition required
       
         on <stdin> line 3, in terraform:
          3:   required_providers
       
       An argument or block definition is required here. To set an argument, use
       the equals sign "=" to introduce the argument value.
    """.trimIndent(), after)
  }
}