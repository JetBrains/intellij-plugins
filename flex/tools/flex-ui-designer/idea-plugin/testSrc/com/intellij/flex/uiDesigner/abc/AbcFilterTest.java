package com.intellij.flex.uiDesigner.abc;

import com.intellij.flex.uiDesigner.FlexUIDesignerBaseTestCase;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static com.intellij.flex.uiDesigner.MatcherAssert.assertThat;

import java.io.File;
import java.io.IOException;

public class AbcFilterTest {
  private File out;
  private AbcFilter filter;
  private static final File TEST_LIB_DIR = new File(FlexUIDesignerBaseTestCase.testDataPath(), "abcTestLib");

  @Before
  public void setUp() throws Exception {
    out = File.createTempFile("abc_", ".swf");
    filter = new AbcFilter(null);
  }

  @After
  public void runAfterEveryTest() {
    //noinspection ResultOfMethodCallIgnored
    out.delete();
  }

  @Test
  public void replaceMainClass() throws IOException {
    filter.filter(new File(TEST_LIB_DIR, "libraryWithIncompatibleMxFlexModuleFactory.swf"), out, null);
    assertThat(out.length(), 413958);
  }

  @Test
  public void replaceExportsToSymbolClass() throws IOException {
    filter.filter(new File(TEST_LIB_DIR, "MinimalComps_0_9_10.swf"), out, null);
    assertThat(out.length(), 257126);
  }
}