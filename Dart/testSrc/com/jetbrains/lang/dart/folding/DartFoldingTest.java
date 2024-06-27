// Copyright 2000-2022 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.jetbrains.lang.dart.folding;

import com.intellij.codeInsight.folding.CodeFoldingSettings;
import com.intellij.util.Consumer;
import com.intellij.util.xmlb.XmlSerializerUtil;
import com.jetbrains.lang.dart.DartCodeInsightFixtureTestCase;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class DartFoldingTest extends DartCodeInsightFixtureTestCase {

  private void doTest() {
    myFixture.testFoldingWithCollapseStatus(getTestDataPath() + "/folding/" + getTestName(false) + ".dart");
  }

  private void doTestWithSpecificSettings(@NotNull final Consumer<CodeFoldingSettings> commonSettingsConsumer) {
    doTestWithSpecificSettings(commonSettingsConsumer, null);
  }

  private void doTestWithSpecificSettings(@Nullable final Consumer<CodeFoldingSettings> commonSettingsConsumer,
                                          @Nullable final Consumer<DartCodeFoldingSettings> dartCodeFoldingSettingsConsumer) {
    CodeFoldingSettings commonSettings = null;
    CodeFoldingSettings commonOriginalSettings = null;

    DartCodeFoldingSettings dartSettings = null;
    DartCodeFoldingSettings dartOriginalSettings = null;

    if (commonSettingsConsumer != null) {
      commonSettings = CodeFoldingSettings.getInstance();
      commonOriginalSettings = XmlSerializerUtil.createCopy(commonSettings);
    }

    if (dartCodeFoldingSettingsConsumer != null) {
      dartSettings = DartCodeFoldingSettings.getInstance();
      dartOriginalSettings = XmlSerializerUtil.createCopy(dartSettings);
    }

    try {
      if (commonSettingsConsumer != null) {
        commonSettingsConsumer.consume(commonSettings);
      }

      if (dartCodeFoldingSettingsConsumer != null) {
        dartCodeFoldingSettingsConsumer.consume(dartSettings);
      }

      doTest();
    }
    finally {
      if (commonSettingsConsumer != null) {
        XmlSerializerUtil.copyBean(commonOriginalSettings, commonSettings);
      }

      if (dartCodeFoldingSettingsConsumer != null) {
        XmlSerializerUtil.copyBean(dartOriginalSettings, dartSettings);
      }
    }
  }

  public void testOnlyFileHeaderInFile() {
    doTest();
  }

  public void testSingleLineFileHeader() {
    doTest();
  }

  public void testFileHeaderBeforeSingleImport() {
    doTest();
  }

  public void testCompositeFileHeaderAndExpandedImports() {
    doTestWithSpecificSettings(settings -> settings.COLLAPSE_IMPORTS = false);
  }

  public void testExpandedFileHeaderAndFoldedImports() {
    doTestWithSpecificSettings(settings -> settings.COLLAPSE_FILE_HEADER = false);
  }

  public void testFileHeaderBeforePartOf() {
    doTest();
  }

  public void testClassDocNoFileHeader() {
    doTest();
  }

  public void testClassEnumExtensionBodies() {
    doTest();
  }

  public void testCommentsFolding() {
    doTest();
  }

  public void testDocCommentsCollapsed() {
    doTestWithSpecificSettings(settings -> settings.COLLAPSE_DOC_COMMENTS = true);
  }

  public void testFunctionBody() {
    doTest();
  }

  public void testFunctionExpressionBody() {
    doTest();
  }

  public void testFunctionBodyCollapsedByDefault() {
    doTestWithSpecificSettings(settings -> settings.COLLAPSE_METHODS = true);
  }

  public void testCustomRegionsOverlappingWithCommentFoldings() {
    doTest();
  }

  public void testTypeArguments() {
    doTest();
  }

  public void testTypeArgumentsByDefault() {
    doTestWithSpecificSettings(null, settings -> settings.setCollapseGenericParameters(true));
  }

  public void testParts() {
    doTestWithSpecificSettings(null, settings -> settings.setCollapseParts(false));
  }

  public void testPartsByDefault() {
    doTest();
  }

  public void testMultilineStrings() {
    doTest();
  }

  public void testNewExpression() {
    doTest();
  }

  public void testAssertStatements() {
    doTest();
  }

  public void testIfStatements() {
    doTest();
  }

  public void testLoopStatements() {
    doTest();
  }

  public void testLiterals() {
    doTest();
  }

  public void testCustomRegions() {
    doTest();
  }

  public void testDartFormalParameterList() { doTest(); }
}
