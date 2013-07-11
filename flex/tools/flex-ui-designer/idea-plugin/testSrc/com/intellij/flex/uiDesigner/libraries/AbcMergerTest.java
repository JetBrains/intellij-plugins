package com.intellij.flex.uiDesigner.libraries;

import com.intellij.flex.uiDesigner.DesignerTests;
import com.intellij.openapi.util.Condition;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Set;

public class AbcMergerTest {
  private File out;

  @Before
  public void runBeforeEveryTest() throws Exception {
    out = File.createTempFile("abc_", ".swf");
  }

  @After
  public void runAfterEveryTest() {
    Assert.assertTrue(out.delete());
  }

  @Test
  public void merge() throws IOException {
    doTest(createLibrary("MinimalComps_0_9_10.swc"));
  }

  @Test
  public void merge2() throws IOException {
    doTest(createLibrary("as3commons-logging-1.1.1.swc"),
           createLibrary("as3corelib-0.93.swc"),
           createLibrary("swiz-framework-v1.4.0.swc"),
           createLibrary("ds_release.swc"));
  }

  private static LightLibrary createLibrary(String path) throws IOException {
    return new LightLibrary((new File(DesignerTests.getTestDataPath(), "lib/" + path)));
  }

  @Test
  public void mergeAwareOfDefineButton2() throws IOException {
    doTest(createLibrary("IDEA-104608/IpgVideoPlayerAssets.swc"),
           createLibrary("IDEA-104608/IpgVptLoaderAssets.swc"));
  }

  private void doTest(Library... libraries) throws IOException {
    final Set<CharSequence> globalDefinitions = LibraryUtil.getDefinitions(LibraryUtil.getTestGlobalLibrary(false));
    LibrarySorter librarySorter = new LibrarySorter();
    librarySorter.sort(Arrays.asList(libraries), out, new Condition<String>() {
      @Override
      public boolean value(String name) {
        return globalDefinitions.contains(name);
      }
    }, false);
  }
}