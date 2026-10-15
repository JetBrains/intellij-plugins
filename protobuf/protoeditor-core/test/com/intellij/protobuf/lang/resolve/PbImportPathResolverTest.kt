package com.intellij.protobuf.lang.resolve

import com.intellij.openapi.application.runReadActionBlocking
import com.intellij.platform.testFramework.junit5.codeInsight.fixture.codeInsightModuleFixture
import com.intellij.protobuf.fixtures.PbCodeInsightTestUtil
import com.intellij.protobuf.ide.settings.PbProjectSettings
import com.intellij.protobuf.lang.psi.PbImportName
import com.intellij.psi.PsiPolyVariantReference
import com.intellij.psi.util.parentOfType
import com.intellij.testFramework.junit5.TestApplication
import com.intellij.testFramework.junit5.fixture.projectFixture
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

@TestApplication
class PbImportPathResolverTest {
  companion object {
    private val projectFixture = projectFixture(openAfterCreation = true)
  }

  private val myFixture by codeInsightModuleFixture(projectFixture)

  @BeforeEach
  fun beforeEachTest() {
    PbCodeInsightTestUtil.setUpTestDataPath(myFixture)
    PbProjectSettings.getInstance(myFixture.project).apply {
      isIncludeContentRoots = false
      isIncludeProtoDirectories = false
      isThirdPartyConfigurationEnabled = false
      isIndexBasedResolveEnabled = false
      importPathEntries = mutableListOf()
    }
  }

  @Test
  fun `test no imports resolved when all settings are disabled`() {
    myFixture.addFileToProject("imported.proto", "")
    myFixture.configureByText("root.proto", """
      import "<error descr="Cannot resolve import 'imported.proto'">imported.proto</error>";
    """)
    myFixture.checkHighlighting()
  }

  @Test
  fun `test resolve import without explicit import path configuration`() {
    PbProjectSettings.getInstance(myFixture.project).isIndexBasedResolveEnabled = true

    val importedFile = myFixture.addFileToProject("/dir1/dir2/dir3/imported.proto", "")
    myFixture.configureByText("root.proto", """
      import "dir2/dir3/impor<caret>ted.proto"
    """.trimIndent())

    val (reference, resolved) = runReadActionBlocking {
      val reference = myFixture.file.findElementAt(myFixture.editor.caretModel.offset)!!.parentOfType<PbImportName>()?.reference
      reference to reference?.resolve()
    }
    assertTrue(reference is PbImportReference)
    assertEquals(importedFile, resolved)
  }

  @Test
  fun `test resolve import from content root`() {
    PbProjectSettings.getInstance(myFixture.project).isIncludeContentRoots = true

    val importedFile = myFixture.addFileToProject("/dir1/dir2/dir3/imported.proto", "")
    myFixture.configureByText("root.proto", """
      import "dir1/dir2/dir3/impor<caret>ted.proto"
    """.trimIndent())

    val (reference, resolved) = runReadActionBlocking {
      val reference = myFixture.file.findElementAt(myFixture.editor.caretModel.offset)!!.parentOfType<PbImportName>()?.reference
      reference to reference?.resolve()
    }
    assertTrue(reference is PbImportReference)
    assertEquals(importedFile, resolved)
  }

  @Test
  fun `test resolve import from protobuf folder`() {
    PbProjectSettings.getInstance(myFixture.project).isIncludeProtoDirectories = true

    val importedFile = myFixture.addFileToProject("/proto/imported.proto", "")
    myFixture.configureByText("root.proto", """
      import "impor<caret>ted.proto"
    """.trimIndent())

    val (reference, resolved) = runReadActionBlocking {
      val reference = myFixture.file.findElementAt(myFixture.editor.caretModel.offset)!!.parentOfType<PbImportName>()?.reference
      reference to reference?.resolve()
    }
    assertTrue(reference is PbImportReference)
    assertEquals(importedFile, resolved)
  }

  @Test
  fun `test ambiguous import is not highlighted when index search is enabled`() {
    PbProjectSettings.getInstance(myFixture.project).isIndexBasedResolveEnabled = true

    myFixture.addFileToProject("/testroot/google/protobuf/wrappers.proto", """
      message Test {}
    """.trimIndent())
    val mainFile = myFixture.addFileToProject("main.proto", """
      import "google/protobuf/wrap<caret>pers.proto";
    """.trimIndent())
    myFixture.configureFromExistingVirtualFile(mainFile.virtualFile)
    myFixture.checkHighlighting(true, true, true)
    val reference = myFixture.getReferenceAtCaretPosition()
    assertEquals(2, runReadActionBlocking { (reference as PsiPolyVariantReference).multiResolve(false).size })
  }

  @Test
  fun `test symbol path with ambiguous target can be resolved`() {
    PbProjectSettings.getInstance(myFixture.project).isIndexBasedResolveEnabled = true

    myFixture.addFileToProject("/testroot/google/protobuf/wrappers.proto", """
      package google.protobuf;
      message DoubleValue {}
    """.trimIndent())
    val mainFile = myFixture.addFileToProject("main.proto", """
      syntax = "proto3";
      import "google/protobuf/wrappers.proto";
      
      message Test {
        google.protobuf.Double<caret>Value value = 1;
      }
    """.trimIndent())
    myFixture.configureFromExistingVirtualFile(mainFile.virtualFile)
    myFixture.checkHighlighting(true, true, true)
    val reference = myFixture.getReferenceAtCaretPosition()
    assertEquals(1, runReadActionBlocking { (reference as PsiPolyVariantReference).multiResolve(false).size })
  }
}