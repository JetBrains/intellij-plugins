// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.intellij.terraform

import com.intellij.openapi.editor.ex.EditorEx
import com.intellij.platform.navbar.testFramework.contextNavBarPathStrings
import com.intellij.testFramework.fixtures.BasePlatformTestCase

class TfNavBarTest : BasePlatformTestCase() {

  fun testNavBarResource() {
    myFixture.configureByText("example.tf", """
      resource "aws_s3_bucket" <caret> "example_bucket" {
      }
    """.trimIndent())

    assertNavBarModel(
      "src",
      "example.tf",
      "resource aws_s3_bucket example_bucket"
    )
  }

  fun testNavBarProperties() {
    myFixture.configureByText("example.tf", """
      provider "aws" {
        region = "us-west-2"
      }

      resource "aws_s3_bucket" "example_bucket" {
        bucket = "terraform-example-bucket"
        acl    = "private"                  
        tags = {
          Name<caret> = "ExampleBucket"
          Environment = "Dev"
        }
      }
    """.trimIndent())

    assertNavBarModel(
      "src",
      "example.tf",
      "resource aws_s3_bucket example_bucket",
      "tags",
      "Name"
    )
  }

  fun testNavBarBlock() {
    myFixture.configureByText("example.tf", """
      resource "aws_s3_bucket" "example_bucket" {<caret>
      }
    """.trimIndent())

    assertNavBarModel(
      "src",
      "example.tf",
      "resource aws_s3_bucket example_bucket"
    )
  }

  fun testNavBarObject() {
    myFixture.configureByText("example.tf", """
      resource "aws_s3_bucket" "example_bucket" {
        tags = {<caret>
        }
      }
    """.trimIndent())

    assertNavBarModel(
      "src",
      "example.tf",
      "resource aws_s3_bucket example_bucket",
      "tags"
    )
  }

  private fun assertNavBarModel(vararg expectedItems: String) {
    val dataContext = (myFixture.editor as EditorEx).getDataContext()
    val items = contextNavBarPathStrings(dataContext)
    assertOrderedEquals(items, expectedItems.toList())
  }
}