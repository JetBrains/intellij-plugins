package com.intellij.protobuf.lang.intentions

import com.intellij.protobuf.ide.settings.DefaultConfigurator
import com.intellij.protobuf.ide.settings.PbProjectSettings
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import com.intellij.testFramework.fixtures.CodeInsightTestFixture
import org.junit.After
import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

@RunWith(JUnit4::class)
internal class PbIntentionsTest : BasePlatformTestCase() {
  @After
  fun afterEachTest() {
    PbProjectSettings.getInstance(myFixture.project).importPathEntries = mutableListOf(DefaultConfigurator().builtInIncludeEntry)
  }

  @Test
  fun `configure plugin from import statement`() {
    myFixture.addFileToProject("/directory/importMe.proto", """
      message ImportedMessage {}
    """.trimIndent())
    myFixture.configureByText("main.proto", """
      syntax = "proto3";
      
      import "<error descr="Cannot resolve import 'importMe.proto'">importMe.proto</error>";
      
      message MainMessage {
        <error descr="Cannot resolve symbol 'ImportedMessage'">ImportedMessage</error> importedMessageField = 1;
      }
    """.trimIndent())

    myFixture.checkHighlighting(true, true, true)
    myFixture.editor.caretModel.moveToOffset(myFixture.file.text.indexOf("importMe.proto"))
    val availableIntentions = myFixture.availableIntentions
    availableIntentions.firstOrNull { it.text == "Add import path to plugin settings" }!!.invoke(
      myFixture.project, myFixture.editor, myFixture.file
    )

    Assert.assertTrue(
      PbProjectSettings.getInstance(myFixture.project)
        .importPathEntries
        .contains(PbProjectSettings.ImportPathEntry("temp:///src/directory", ""))
    )

    // should be no errors both in import statement and among imported entities
    myFixture.checkResult("""
      syntax = "proto3";
      
      import "importMe.proto";
      
      message MainMessage {
        ImportedMessage importedMessageField = 1;
      }
    """.trimIndent())
  }

  @Test
  fun `add import statement and configure plugin`() {
    myFixture.addFileToProject("/imports/importMe.proto", """
      message ImportedMessage {}
    """.trimIndent())

    myFixture.configureByText("main.proto", """
      syntax = "proto3";
      
      import "google/protobuf/wrappers.proto";
            
      message MainMessage {
        <error descr="Cannot resolve symbol 'Imported<caret>Message'">ImportedMessage</error> importedMessageField = 1;
      }
    """.trimIndent())
    myFixture.checkHighlighting(true, true, true)

    findAndInvokeIntention(myFixture)

    myFixture.checkResult("""
      syntax = "proto3";
      
      import "google/protobuf/wrappers.proto";
      import "importMe.proto";
      
      message MainMessage {
        ImportedMessage importedMessageField = 1;
      }
    """.trimIndent())

    Assert.assertTrue(
      PbProjectSettings.getInstance(myFixture.project)
        .importPathEntries
        .contains(PbProjectSettings.ImportPathEntry("temp:///src/imports", ""))
    )
  }

  @Test
  fun `find existing import statement and configure plugin`() {
    myFixture.addFileToProject("/imports/importMe.proto", """
      message ImportedMessage {}
    """.trimIndent())

    myFixture.configureByText("main.proto", """
      syntax = "proto3";
      
      import "google/protobuf/wrappers.proto";
      import "<error descr="Cannot resolve import 'importMe.proto'">importMe.proto</error>";
            
      message MainMessage {
        <error descr="Cannot resolve symbol 'Imported<caret>Message'">ImportedMessage</error> importedMessageField = 1;
      }
    """.trimIndent())
    myFixture.checkHighlighting(true, true, true)

    Assert.assertFalse(
      PbProjectSettings.getInstance(myFixture.project)
        .importPathEntries
        .contains(PbProjectSettings.ImportPathEntry("temp:///src/imports", ""))
    )

    findAndInvokeIntention(myFixture)
    myFixture.checkResult("""
      syntax = "proto3";
      
      import "google/protobuf/wrappers.proto";
      import "importMe.proto";
            
      message MainMessage {
        ImportedMessage importedMessageField = 1;
      }
    """.trimIndent())
    Assert.assertTrue(
      PbProjectSettings.getInstance(myFixture.project)
        .importPathEntries
        .contains(PbProjectSettings.ImportPathEntry("temp:///src/imports", ""))
    )
  }

  @Test
  fun `no intention for non-existent fqn`() {
    myFixture.configureByText("main.proto", """
      syntax = "proto3";
                  
      message MainMessage {
        <error descr="Cannot resolve symbol 'Unknown<caret>Message'">UnknownMessage</error> importedMessageField = 1;
      }
    """.trimIndent())

    myFixture.checkHighlighting(true, true, true)
    Assert.assertNull(myFixture.getAvailableIntention("Add import statement and configure import path"))
  }

  @Test
  fun `add import statement to empty imports list`() {
    myFixture.addFileToProject("/imports/importMe.proto", """
      message ImportedMessage {}
    """.trimIndent())

    myFixture.configureByText("main.proto", """
      syntax = "proto3";
                  
      message MainMessage {
        <error descr="Cannot resolve symbol 'Imported<caret>Message'">ImportedMessage</error> importedMessageField = 1;
      }
    """.trimIndent())
    myFixture.checkHighlighting(true, true, true)

    findAndInvokeIntention(myFixture)

    myFixture.checkResult("""
      syntax = "proto3";
      import "importMe.proto";
      
      message MainMessage {
        ImportedMessage importedMessageField = 1;
      }
    """.trimIndent())

    Assert.assertTrue(
      PbProjectSettings.getInstance(myFixture.project)
        .importPathEntries
        .contains(PbProjectSettings.ImportPathEntry("temp:///src/imports", ""))
    )
  }

  @Test
  fun `add import statement to file without required header`() {
    myFixture.addFileToProject("/imports/importMe.proto", """
      message ImportedMessage {}
    """.trimIndent())

    myFixture.configureByText("main.proto", """
      message MainMessage {
        <error descr="Cannot resolve symbol 'Imported<caret>Message'">ImportedMessage</error> importedMessageField = 1;
      }
    """.trimIndent())
    myFixture.checkHighlighting(true, true, true)

    findAndInvokeIntention(myFixture)

    myFixture.checkResult("""
      import "importMe.proto";
      message MainMessage {
        ImportedMessage importedMessageField = 1;
      }
    """.trimIndent())

    Assert.assertTrue(
      PbProjectSettings.getInstance(myFixture.project)
        .importPathEntries
        .contains(PbProjectSettings.ImportPathEntry("temp:///src/imports", ""))
    )
  }

  @Test
  fun `resolve ambiguous import, add statement and configure settings`() {
    myFixture.addFileToProject("/imports1/importMe1.proto", """
      message ImportedMessage {}
    """.trimIndent())
    myFixture.addFileToProject("/imports2/importMe2.proto", """
      message ImportedMessage {}
    """.trimIndent())

    myFixture.configureByText("main.proto", """
      message MainMessage {
        <error descr="Cannot resolve symbol 'Imported<caret>Message'">ImportedMessage</error> importedMessageField = 1;
      }
    """.trimIndent())
    myFixture.checkHighlighting(true, true, true)

    findAndInvokeIntention(myFixture)

    myFixture.checkResult("""
      import "importMe1.proto";
      message MainMessage {
        ImportedMessage importedMessageField = 1;
      }
    """.trimIndent())
    Assert.assertTrue(
      PbProjectSettings.getInstance(myFixture.project)
        .importPathEntries
        .contains(PbProjectSettings.ImportPathEntry("temp:///src/imports1", ""))
    )
  }

  @Test
  fun `find and fix one of several suitable import statements`() {

  }

  private fun findAndInvokeIntention(fixture: CodeInsightTestFixture) {
    val intention = fixture.getAvailableIntention("Add import statement and configure import path")
    Assert.assertNotNull(intention)
    intention!!.invoke(fixture.project, fixture.editor, fixture.file)
  }
}