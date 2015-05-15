package org.intellij.plugins.markdown.parser;

import com.intellij.testFramework.ParsingTestCase;
import org.intellij.plugins.markdown.highlighting.MarkdownColorSettingsPage;
import org.intellij.plugins.markdown.lang.parser.MarkdownParserDefinition;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;

public class MarkdownParserTest extends ParsingTestCase {
  @Nullable
  private String myTestDataPath = null;

  public MarkdownParserTest() {
    super("parser", "md", true, new MarkdownParserDefinition());
  }

  @Override
  protected String getTestDataPath() {
    if (myTestDataPath == null) {
      myTestDataPath = new File("test/data").getAbsolutePath();
    }
    return myTestDataPath;
  }

  public void testColorsAndFontsSample() throws IOException {
    final String demoText = new MarkdownColorSettingsPage().getDemoText();
    doCodeTest(demoText);
  }

  public void testCodeBlock() {
    doTest(true);
  }
}
