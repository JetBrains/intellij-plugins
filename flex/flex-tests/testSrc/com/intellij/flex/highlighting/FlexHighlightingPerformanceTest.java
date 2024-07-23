package com.intellij.flex.highlighting;

import com.intellij.flex.FlexTestOption;
import com.intellij.flex.FlexTestOptions;
import com.intellij.flex.util.ActionScriptDaemonAnalyzerTestCase;
import com.intellij.flex.util.FlexTestUtils;
import com.intellij.lang.javascript.flex.FlexModuleType;
import com.intellij.openapi.module.ModuleType;
import com.intellij.tools.ide.metrics.benchmark.Benchmark;
import com.jetbrains.performancePlugin.yourkit.YourKitProfilerHandler;
import org.jetbrains.annotations.NotNull;

public class FlexHighlightingPerformanceTest extends ActionScriptDaemonAnalyzerTestCase {
  @Override
  protected void setUpJdk() {
    FlexTestUtils.setupFlexSdk(myModule, getTestName(false), getClass(), getTestRootDisposable());
  }

  @Override
  protected String getBasePath() {
    return "";
  }

  @NotNull
  @Override
  protected String getTestDataPath() {
    return FlexTestUtils.getTestDataPath(FlexHighlightingTest.BASE_PATH);
  }

  @Override
  protected String getExtension() {
    return "as";
  }

  private static final boolean doProfiling = false;
  
  @FlexTestOptions(FlexTestOption.WithFlexFacet)
  public void testPerformance() {
    if (doProfiling) YourKitProfilerHandler.startCPUProfiling();
    try {
      Benchmark.newBenchmark("Mxml highlighting",
                             () -> doTestFor(true, getTestName(false) + ".mxml", "UsingSwcStubs2.swc"))
        .start();
    }
    finally {
      if (doProfiling) YourKitProfilerHandler.captureCPUSnapshot();
    }
  }

  @NotNull
  @Override
  protected ModuleType getModuleType() {
    return FlexModuleType.getInstance();
  }
}
