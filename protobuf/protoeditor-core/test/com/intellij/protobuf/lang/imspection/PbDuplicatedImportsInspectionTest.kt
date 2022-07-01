package com.intellij.protobuf.lang.imspection

import com.intellij.protobuf.ide.settings.DefaultConfigurator
import com.intellij.protobuf.ide.settings.PbProjectSettings
import com.intellij.protobuf.lang.inspection.PbDuplicatedImportsInspection
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import org.junit.After
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

@RunWith(JUnit4::class)
class PbDuplicatedImportsInspectionTest : BasePlatformTestCase() {
  override fun setUp() {
    super.setUp()
    myFixture.enableInspections(PbDuplicatedImportsInspection::class.java)
  }

  @After
  fun afterEachTest() {
    PbProjectSettings.getInstance(myFixture.project).importPathEntries = mutableListOf(DefaultConfigurator().builtInIncludeEntry)
  }

  @Test
  fun `test several imports with the same text`() {
    PbProjectSettings.getInstance(myFixture.project).importPathEntries.add(PbProjectSettings.ImportPathEntry("temp:///src/directory", ""))
    myFixture.addFileToProject("/directory/importMe.proto", """
      syntax = "proto3";
      message ImportedMessage {}
    """.trimIndent())

    myFixture.configureByText("main.proto", """
      syntax = "proto3";

      import <error descr="Duplicated import statements">"directory/importMe.proto"</error>;
      import <error descr="Duplicated import statements">"directory/importMe.proto"</error>;

      message MainMessage {}
    """.trimIndent())
    myFixture.checkHighlighting(true, true, true)
  }

  @Test
  fun `test effectively equal imports detection`() {
    PbProjectSettings.getInstance(myFixture.project).importPathEntries.apply {
      add(PbProjectSettings.ImportPathEntry("temp:///src/root", ""))
      add(PbProjectSettings.ImportPathEntry("temp:///src/root/directory", ""))
    }

    myFixture.addFileToProject("/root/directory/importMe.proto", """
      syntax = "proto3";
      message ImportedMessage {}
    """.trimIndent())

    myFixture.configureByText("main.proto", """
      syntax = "proto3";
      
      import <error descr="Duplicated import statements">"importMe.proto"</error>;
      import <error descr="Duplicated import statements">"directory/importMe.proto"</error>;
            
      message MainMessage {}
    """.trimIndent())
    myFixture.checkHighlighting(true, true, true)
  }

  @Test
  fun `test import without duplicates not affected`() {
    PbProjectSettings.getInstance(myFixture.project).importPathEntries.apply {
      add(PbProjectSettings.ImportPathEntry("temp:///src/directory1", ""))
      add(PbProjectSettings.ImportPathEntry("temp:///src/directory2", ""))
    }
    myFixture.addFileToProject("/directory1/importMe1.proto", """
      syntax = "proto3";
      message ImportedMessage1 {}
    """.trimIndent())
    myFixture.addFileToProject("/directory2/importMe2.proto", """
      syntax = "proto3";
      message ImportedMessage2 {}
    """.trimIndent())

    myFixture.configureByText("main.proto", """
      syntax = "proto3";

      import <error descr="Duplicated import statements">"importMe1.proto"</error>;
      import "importMe2.proto";
      import <error descr="Duplicated import statements">"importMe1.proto"</error>;

      message MainMessage {}
    """.trimIndent())
    myFixture.checkHighlighting(true, true, true)
  }
}