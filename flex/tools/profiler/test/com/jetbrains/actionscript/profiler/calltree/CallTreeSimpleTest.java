package com.jetbrains.actionscript.profiler.calltree;

import java.io.File;
import java.io.IOException;

public class CallTreeSimpleTest extends CallTreeTest {
  @Override
  protected String getBasePath() {
    return super.getBasePath() + File.separator + "simple";
  }

  public void testSimple() throws IOException {
    doTest("simple.xml", "simple_results.xml");
  }

  public void testSimpleNested() throws IOException {
    doTest("simple_nested.xml", "simple_nested_results.xml");
  }
}
