// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.intellij.terraform.config;

import com.intellij.lang.ParserDefinition;
import com.intellij.openapi.application.ReadAction;
import com.intellij.openapi.fileEditor.impl.LoadTextUtil;
import com.intellij.openapi.project.Project;
import com.intellij.platform.testFramework.junit5.codeInsight.fixture.ParsingTestFixture;
import com.intellij.psi.PsiFile;
import com.intellij.testFramework.LightVirtualFile;
import com.intellij.testFramework.ParsingTestUtil;
import com.intellij.testFramework.TestDataPath;
import com.intellij.testFramework.junit5.TestApplication;
import com.intellij.testFramework.junit5.fixture.TestFixture;
import org.intellij.terraform.TfTestUtils;
import org.intellij.terraform.hcl.HCLParserDefinition;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;

import static com.intellij.platform.testFramework.junit5.codeInsight.fixture.ParsingTestFixtureKt.parsingFixture;
import static com.intellij.testFramework.junit5.fixture.FixturesKt.projectFixture;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@TestApplication
@TestDataPath("$CONTENT_ROOT/../test-data/psi/")
public class HCLParserTest {
  private static final TestFixture<Project> projectFixture = projectFixture();

  protected final TestFixture<ParsingTestFixture> parsingFixture =
    parsingFixture(projectFixture, TfTestUtils.getTestDataPath() + "/psi", "hcl", false, createParserDefinitions());

  protected ParserDefinition @NotNull [] createParserDefinitions() {
    return new ParserDefinition[]{new HCLParserDefinition()};
  }

  protected void doTest() {
    parsingFixture.get().doTest(true);
  }

  protected void doCodeTest(@NotNull String code) {
    parsingFixture.get().doCodeTest(code);
  }


  @Test
  public void testEmpty() throws Exception {
    doTest();
  }

  @Test
  public void testComment_Single() throws Exception {
    doTest();
  }

  @Test
  public void testComment_Multiline() throws Exception {
    doTest();
  }

  @Test
  public void testComment_Complex() throws Exception {
    doTest();
  }

  @Test
  public void testSimple_Types() throws Exception {
    doTest();
  }

  @Test
  public void testList_Simple() throws Exception {
    doTest();
  }

  @Test
  public void testList_Tailing_Comma() throws Exception {
    doTest();
  }

  @Test
  public void testMultiple_Properties() throws Exception {
    doTest();
  }

  @Test
  public void testHereDoc() throws Exception {
    doTest();
  }
  @Test
  public void testHereDoc_Empty() throws Exception {
    doTest();
  }

  @Test
  public void testHereDoc_EmptyLines() throws Exception {
    doTest();
  }

  @Test
  public void testHereDoc_Incomplete() throws Exception {
    doTest();
  }

  @Test
  public void testHereDoc_Missing_Marker() throws Exception {
    doTest();
  }

  @Test
  public void testHereDoc_Indented() throws Exception {
    doTest();
  }

  @Test
  public void testHereDoc_Indented_End() throws Exception {
    doTest();
  }

  @Test
  public void testIdentifiers() throws Exception {
    doTest();
  }

  @Test
  public void testBlock_Empty() throws Exception {
    doTest();
  }

  @Test
  public void testBlock() throws Exception {
    doTest();
  }

  @Test
  public void testComplex() throws Exception {
    doTest();
  }

  @Test
  public void testArrayTailComment() throws Exception {
    doTest();
  }

  @Test
  public void testIncomplete_Block() throws Exception {
    doTest();
  }

  @Test
  public void testIncomplete_Property_Or_Block() throws Exception {
    doTest();
  }

  @Test
  public void testExtraQuote() throws Exception {
    doTest();
  }

  @Test
  public void testExtraQuoteNoNewLine() throws Exception {
    doTest();
  }

  @Test
  public void testIAM_Role_Policy() throws Exception {
    doTest();
  }

  @Test
  public void testAssign_Deep() throws Exception {
    doTest();
  }

  @Test
  public void testList_Of_Maps() throws Exception {
    doTest();
  }

  @Test
  public void testList_Of_Lists() throws Exception {
    doTest();
  }

  @Test
  public void testObject_List_Comma() throws Exception {
    doTest();
  }

  @Test
  public void testMultiline_Literal() throws Exception {
    doTest();
  }

  @Test
  public void testList_With_Identifier() throws Exception {
    doTest();
  }

  @Test
  public void testUnfinished_String() throws Exception {
    doTest();
  }

  @Test
  public void testUnfinished_Block_Name() throws Exception {
    doTest();
  }

  @Test
  public void testMixUnaryBinary() throws Exception {
    doCodeTest("a=(-a+b) * (-1+1)");
  }

  @Test
  public void testMultipleIndexes() throws Exception {
    doCodeTest("a=foo[0][1][2][3]");
  }

  @Test
  public void testGreedyIndexes() throws Exception {
    doCodeTest("a=aws_instance.web[1].list[2]");
  }

  @Test
  public void testUnaryOverIndexSelect() throws Exception {
    doCodeTest("a=!var.list[0]");
  }

  @Test
  public void testKeywordAsPropertyName() throws Exception {
    doTest();
  }

  @Test
  public void testUnaryOverMethodCall() throws Exception {
    doCodeTest("a=!tobool(\"false\")");
  }

  @Test
  public void testImportBlock() {
    doTest();
  }

  // From several 'panic' issues in HCL itself
  @Test
  public void testBrokenInput() throws Exception {
    doTestNoException("{\"\\0"); // #194
    doTestNoException("wÔΩø\u00dc<<070005000\n"); // #130
    doTestNoException("t\"\\400n\"{}"); // #129
    doTestNoException("{:{"); // #128
    doTestNoException("\"\\0"); // #127
  }

  private void doTestNoException(String text) {
    ParsingTestFixture fixture = parsingFixture.get();
    PsiFile file = fixture.createPsiFile("a", text);
    ReadAction.runBlocking(() -> {
      ParsingTestUtil.ensureParsed(file);
      assertEquals(text, ((LightVirtualFile)file.getVirtualFile()).getContent().toString(), "light virtual file text mismatch");
      assertEquals(text, LoadTextUtil.loadText(file.getVirtualFile()).toString(), "virtual file text mismatch");
      var document = file.getViewProvider().getDocument();
      assertNotNull(document);
      assertEquals(text, document.getText(), "doc text mismatch");
      assertEquals(text, file.getText(), "psi text mismatch");
      ParsingTestFixture.toParseTreeText(file, fixture.getSkipSpaces(), fixture.getIncludeRanges());
    });
  }
}
