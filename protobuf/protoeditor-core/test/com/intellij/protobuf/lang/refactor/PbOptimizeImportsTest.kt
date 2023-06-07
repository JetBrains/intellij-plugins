package com.intellij.protobuf.lang.refactor

import com.intellij.codeInsight.actions.OptimizeImportsAction
import com.intellij.ide.DataManager
import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.protobuf.ide.settings.PbProjectSettings
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import org.junit.After
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

@RunWith(JUnit4::class)
internal class PbOptimizeImportsTest : BasePlatformTestCase() {

  @After
  fun afterEachTest() {
    PbProjectSettings.getInstance(myFixture.project).importPathEntries = mutableListOf()
  }

  @Test
  fun `test duplicate import`() {
    PbProjectSettings.getInstance(myFixture.project).importPathEntries.add(PbProjectSettings.ImportPathEntry("temp:///src/directory", ""))
    myFixture.addFileToProject("/directory/importMe.proto", """
      syntax = "proto3";
      message ImportedMessage {}
    """.trimIndent())

    myFixture.configureByText("main.proto", """
      syntax = "proto3";
      
      import "directory/import<caret>Me.proto";
      import "directory/importMe.proto";
      
      message MainMessage {}
    """.trimIndent())
    myFixture.checkHighlighting(true, true, true)
    OptimizeImportsAction.actionPerformedImpl(DataManager.getInstance().getDataContext(myFixture.editor.contentComponent))
    FileDocumentManager.getInstance().saveAllDocuments()

    myFixture.checkResult("""
      syntax = "proto3";
      
      import "directory/importMe.proto";
      
      message MainMessage {}
    """.trimIndent())
  }

  @Test
  fun `test implicit duplicate import`() {
    PbProjectSettings.getInstance(myFixture.project).importPathEntries.add(PbProjectSettings.ImportPathEntry("temp:///src/root", ""))
    PbProjectSettings.getInstance(myFixture.project).importPathEntries.add(PbProjectSettings.ImportPathEntry("temp:///src/root/directory", ""))
    myFixture.addFileToProject("/root/directory/importMe.proto", """
      syntax = "proto3";
      message ImportedMessage {}
    """.trimIndent())

    myFixture.configureByText("main.proto", """
      syntax = "proto3";
      
      import "import<caret>Me.proto";
      import "directory/importMe.proto";
            
      message MainMessage {}
    """.trimIndent())
    myFixture.checkHighlighting(true, true, true)
    OptimizeImportsAction.actionPerformedImpl(DataManager.getInstance().getDataContext(myFixture.editor.contentComponent))
    FileDocumentManager.getInstance().saveAllDocuments()

    myFixture.checkResult("""
      syntax = "proto3";
      
      import "importMe.proto";
      
      message MainMessage {}
    """.trimIndent())
  }
}