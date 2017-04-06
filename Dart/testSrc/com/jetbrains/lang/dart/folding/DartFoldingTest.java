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

  public void testOnlyFileHeaderInFile() throws Exception {
    doTest();
  }

  public void testSingleLineFileHeader() throws Exception {
    doTest();
  }

  public void testFileHeaderBeforeSingleImport() throws Exception {
    doTest();
  }

  public void testCompositeFileHeaderAndExpandedImports() throws Exception {
    doTestWithSpecificSettings(settings -> settings.setCollapseImports(false));
  }

  public void testExpandedFileHeaderAndFoldedImports() throws Exception {
    doTestWithSpecificSettings(settings -> settings.setCollapseFileHeader(false));
  }

  public void testFileHeaderBeforePartOf() throws Exception {
    doTest();
  }

  public void testClassDocNoFileHeader() throws Exception {
    doTest();
  }

  public void testCommentsFolding() throws Exception {
    doTest();
  }

  public void testDocCommentsCollapsed() throws Exception {
    doTestWithSpecificSettings(settings -> settings.setCollapseDocComments(true));
  }

  public void testFunctionBody() throws Exception {
    doTest();
  }

  public void testFunctionBodyCollapsedByDefault() throws Exception {
    doTestWithSpecificSettings(settings -> settings.setCollapseMethods(true));
  }

  public void testCustomRegionsOverlappingWithCommentFoldings() throws Exception {
    doTest();
  }

  public void testTypeArguments() throws Exception {
    doTest();
  }

  public void testTypeArgumentsByDefault() throws Exception {
    doTestWithSpecificSettings(null, settings -> settings.setCollapseGenericParameters(true));
  }

  public void testParts() throws Exception {
    doTestWithSpecificSettings(null, settings -> settings.setCollapseParts(false));
  }

  public void testPartsByDefault() throws Exception {
    doTest();
  }

  public void testMultilineStrings() throws Exception {
    doTest();
  }
}
