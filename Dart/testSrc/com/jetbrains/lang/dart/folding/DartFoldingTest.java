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

  private void doTestWithSpecificSettings(@NotNull final Consumer<CodeFoldingSettings> settingsConsumer) {
    doTestWithSpecificSettings(settingsConsumer, null);
  }

  private void doTestWithSpecificSettings(@NotNull final Consumer<CodeFoldingSettings> settingsConsumer, @Nullable final Consumer<DartCodeFoldingSettings> dartCodeFoldingSettingsConsumer) {
    final CodeFoldingSettings settings = CodeFoldingSettings.getInstance();
    final CodeFoldingSettings originalSettings = XmlSerializerUtil.createCopy(settings);
    DartCodeFoldingSettings dartSettings = null;
    DartCodeFoldingSettings dartOriginalSettings = null;
    if(dartCodeFoldingSettingsConsumer != null) {
      dartSettings = DartCodeFoldingSettings.getInstance();
      dartOriginalSettings = XmlSerializerUtil.createCopy(dartSettings);
    }
    try {
      settingsConsumer.consume(settings);
      if(dartCodeFoldingSettingsConsumer != null) {
        dartCodeFoldingSettingsConsumer.consume(dartSettings);
      }
      doTest();
    }
    finally {
      XmlSerializerUtil.copyBean(originalSettings, settings);
      if(dartCodeFoldingSettingsConsumer != null) {
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
    doTestWithSpecificSettings(new Consumer<CodeFoldingSettings>() {
      public void consume(final CodeFoldingSettings settings) {
        settings.COLLAPSE_IMPORTS = false;
      }
    });
  }

  public void testExpandedFileHeaderAndFoldedImports() throws Exception {
    doTestWithSpecificSettings(new Consumer<CodeFoldingSettings>() {
      public void consume(final CodeFoldingSettings settings) {
        settings.COLLAPSE_FILE_HEADER = false;
      }
    });
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
    doTestWithSpecificSettings(new Consumer<CodeFoldingSettings>() {
      public void consume(final CodeFoldingSettings settings) {
        settings.COLLAPSE_DOC_COMMENTS = true;
      }
    });
  }

  public void testFunctionBody() throws Exception {
    doTest();
  }

  public void testFunctionBodyCollapsedByDefault() throws Exception {
    doTestWithSpecificSettings(new Consumer<CodeFoldingSettings>() {
      public void consume(final CodeFoldingSettings settings) {
        settings.COLLAPSE_METHODS = true;
      }
    });
  }

  public void testCustomRegionsOverlappingWithCommentFoldings() throws Exception {
    doTest();
  }

  public void testTypeArguments() throws Exception {
    doTest();
  }

  public void testTypeArgumentsByDefault() throws Exception {
    doTestWithSpecificSettings(new Consumer<CodeFoldingSettings>() {
      @Override
      public void consume(final CodeFoldingSettings settings) {}
    }, new Consumer<DartCodeFoldingSettings>() {
      public void consume(final DartCodeFoldingSettings settings) {
        settings.COLLAPSE_GENERIC_PARAMETERS = true;
      }
    });
  }
}
