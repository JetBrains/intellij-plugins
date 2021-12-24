package com.intellij.protobuf.lang.intentions

import com.intellij.protobuf.ide.settings.DefaultConfigurator
import com.intellij.protobuf.ide.settings.PbProjectSettings
import com.intellij.testFramework.fixtures.BasePlatformTestCase
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
  fun `test configure plugin from import statement`() {
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
  fun `test add import statement and configure plugin`() {
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

    val intention = myFixture.getAvailableIntention("Add import statement and configure import path")
    Assert.assertNotNull(intention)

    intention!!.invoke(myFixture.project, myFixture.editor, myFixture.file)
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
  fun `test find existing import statement and configure plugin`() {
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

    val intention = myFixture.getAvailableIntention("Add import statement and configure import path")
    Assert.assertNotNull(intention)

    intention!!.invoke(myFixture.project, myFixture.editor, myFixture.file)
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
  fun `test no intention for non-existent fqn`() {
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
  fun `test add import statement to empty imports list`() {
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

    val intention = myFixture.getAvailableIntention("Add import statement and configure import path")
    Assert.assertNotNull(intention)

    intention!!.invoke(myFixture.project, myFixture.editor, myFixture.file)
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
  fun `test add import statement to file without required header`() {
    myFixture.addFileToProject("/imports/importMe.proto", """
      message ImportedMessage {}
    """.trimIndent())

    myFixture.configureByText("main.proto", """
      message MainMessage {
        <error descr="Cannot resolve symbol 'Imported<caret>Message'">ImportedMessage</error> importedMessageField = 1;
      }
    """.trimIndent())
    myFixture.checkHighlighting(true, true, true)

    val intention = myFixture.getAvailableIntention("Add import statement and configure import path")
    Assert.assertNotNull(intention)

    intention!!.invoke(myFixture.project, myFixture.editor, myFixture.file)
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
}