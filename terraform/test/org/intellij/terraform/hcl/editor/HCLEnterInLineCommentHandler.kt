package org.intellij.terraform.hcl.editor

import com.intellij.openapi.actionSystem.IdeActions
import com.intellij.testFramework.fixtures.BasePlatformTestCase

class HCLEnterInLineCommentHandlerTest : BasePlatformTestCase() {

  fun testLineHashComment() {
    myFixture.configureByText("main.tf", """
      // Here are <caret> end-of-line comments
      provider "docker" {
        host = "npipe:////./pipe/docker_engine"
      }
    """.trimIndent())
    myFixture.performEditorAction(IdeActions.ACTION_EDITOR_ENTER)
    myFixture.checkResult("""
      // Here are 
      // end-of-line comments
      provider "docker" {
        host = "npipe:////./pipe/docker_engine"
      }
    """.trimIndent())
  }

  fun testDoubleSlashComment() {
    myFixture.configureByText("main.tf", """
      # Another test <caret> end-of-line comments
      terraform {
        required_providers {}
      }
    """.trimIndent())
    myFixture.performEditorAction(IdeActions.ACTION_EDITOR_ENTER)
    myFixture.checkResult("""
      # Another test 
      # end-of-line comments
      terraform {
        required_providers {}
      }
    """.trimIndent())
  }
}