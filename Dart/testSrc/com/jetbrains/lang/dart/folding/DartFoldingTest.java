package com.jetbrains.lang.dart.folding;

import com.intellij.codeInsight.folding.CodeFoldingSettings;
import com.intellij.util.Consumer;
import com.intellij.util.xmlb.XmlSerializerUtil;
import com.jetbrains.lang.dart.DartCodeInsightFixtureTestCase;
import org.jetbrains.annotations.NotNull;

public class DartFoldingTest extends DartCodeInsightFixtureTestCase {

  private void doTest() {
    myFixture.testFoldingWithCollapseStatus(getTestDataPath() + "/folding/" + getTestName(false) + ".dart");
  }

  private void doTestWithSpecificSettings(@NotNull final Consumer<CodeFoldingSettings> settingsConsumer) {
    final CodeFoldingSettings settings = CodeFoldingSettings.getInstance();
    final CodeFoldingSettings originalSettings = XmlSerializerUtil.createCopy(settings);
    try {
      settingsConsumer.consume(settings);
      doTest();
    }
    finally {
      XmlSerializerUtil.copyBean(originalSettings, settings);
    }
  }

  public void testOnlyFileHeaderInFile() throws Exception {
    doTest();
  }

  public void testSingleLineFileHeader() throws Exception {
    doTest();
  }

  public void testFileHeaderBeforeImport() throws Exception {
    doTest();
  }

  public void testCompositeFileHeaderBeforeLibrary() throws Exception {
    doTest();
  }

  public void testExpandedFileHeaderBeforePart() throws Exception {
    doTestWithSpecificSettings(new Consumer<CodeFoldingSettings>() {
      public void consume(final CodeFoldingSettings settings) {
        settings.COLLAPSE_FILE_HEADER = false;
      }
    });
  }

  public void testExpandedFileHeaderBeforePartOf() throws Exception {
    doTest();
  }

  public void testClassDocNoFileHeader() throws Exception {
    doTest();
  }
}
