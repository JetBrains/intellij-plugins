package com.intellij.protobuf.lang.intentions

import com.intellij.protobuf.ide.settings.PbProjectSettings
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import org.junit.Assert

class PbIntentionsTest : BasePlatformTestCase() {
  fun `test add import path intention`() {
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
}