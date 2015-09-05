package com.intellij.flex.uiDesigner.abc;

import com.intellij.flex.uiDesigner.DesignerTests;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import static com.intellij.flex.uiDesigner.MatcherAssert.assertThat;

public class AbcFilterTest {
  private File out;
  private AbcFilter filter;
  private static final File TEST_LIB_DIR = new File(DesignerTests.getTestDataPath(), "abcTestLib");

  @Before
  public void runBeforeEveryTest() throws Exception {
    out = File.createTempFile("abc_", ".swf");
    filter = new AbcFilter();
  }

  @After
  public void runAfterEveryTest() {
    //noinspection ResultOfMethodCallIgnored
    out.delete();
  }

  @Test
  public void replaceMainClass() throws IOException {
    filter.filter(new File(TEST_LIB_DIR, "libraryWithIncompatibleMxFlexModuleFactory.swf"), out, null);
    assertThat(out.length(), 409003);
  }

  @Test
  public void merge() throws IOException {
    filter.filter(new File(TEST_LIB_DIR, "MinimalComps_0_9_10.swf"), out, null);
    assertThat(out.length(), 252500);
  }

  @SuppressWarnings("IOResourceOpenedButNotSafelyClosed")
  @Test
  public void fxg() throws IOException {
    File fxgFile = new File(DesignerTests.getTestDataPath(), "src/common/star.fxg");
    new FxgTranscoder().transcode(new FileInputStream(fxgFile), fxgFile.length(), out, false);

    //FileUtil.copy(out, new File("/Users/develar/test.swf"));
  }
}