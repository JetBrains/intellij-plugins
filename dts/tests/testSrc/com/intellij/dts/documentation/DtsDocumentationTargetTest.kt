package com.intellij.dts.documentation

import com.intellij.dts.dtsFixture
import com.intellij.dts.dtsTimeoutRunBlocking
import com.intellij.lang.documentation.ide.IdeDocumentationTargetProvider
import com.intellij.openapi.application.readAction
import com.intellij.testFramework.junit5.TestApplication
import com.intellij.testFramework.junit5.fixture.projectFixture
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

@TestApplication
class DtsDocumentationTargetTest {
  companion object {
    private val projectFixture = projectFixture(openAfterCreation = true)
  }

  private val dts by dtsFixture(projectFixture)

  private val myFixture get() = dts.codeInsight

  @Test
  fun `test start of node name`() = dtsTimeoutRunBlocking {
    doTest("""
       <caret>node {}; 
    """)
  }

  @Test
  fun `test end of node name`() = dtsTimeoutRunBlocking {
    doTest("""
       node<caret> {}; 
    """)
  }

  @Test
  fun `test start of property name`() = dtsTimeoutRunBlocking {
    doTest("""
       <caret>prop = "value"; 
    """)
  }

  @Test
  fun `test end of property name`() = dtsTimeoutRunBlocking {
    doTest("""
       prop<caret> = "value"; 
    """)
  }

  @Test
  fun `test start of ref`() = dtsTimeoutRunBlocking {
    doTest("""
       prop = <caret>&label; 
       label: node {};
    """)
  }

  @Test
  fun `test end of ref`() = dtsTimeoutRunBlocking {
    doTest("""
       prop = &label<caret>; 
       label: node {};
    """)
  }

  @Test
  fun `test lookup label ref`() = dtsTimeoutRunBlocking {
    doLookupTest("""
      prop = &<caret>;
      label1: node {}; 
    """)
  }

  @Test
  fun `test lookup path ref`() = dtsTimeoutRunBlocking {
    doLookupTest("""
      / { node1 {}; node2 {}; };
      &{/<caret>} {};
    """)
  }

  private suspend fun assertOneDocumentationTarget() {
    val provider = IdeDocumentationTargetProvider.getInstance(dts.project)

    val targets = readAction {
      provider.documentationTargets(myFixture.editor, myFixture.file, myFixture.caretOffset)
    }

    assertThat(targets).hasSize(1)
  }

  private suspend fun doTest(text: String) {
    dts.configureByText(text)

    assertOneDocumentationTarget()
  }

  private suspend fun doLookupTest(text: String) {
    dts.configureByText(text)

    val completion = myFixture.completeBasic().toList()
    assertThat(completion).isNotEmpty()

    assertOneDocumentationTarget()
  }
}