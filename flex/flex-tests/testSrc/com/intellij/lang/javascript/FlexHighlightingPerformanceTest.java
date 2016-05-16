package com.intellij.lang.javascript;

import com.intellij.flex.FlexTestUtils;
import com.intellij.lang.javascript.flex.FlexModuleType;
import com.intellij.openapi.module.ModuleType;
import com.intellij.testFramework.PlatformTestUtil;
import com.intellij.util.ProfilingUtil;
import com.intellij.util.ThrowableRunnable;

public class FlexHighlightingPerformanceTest extends ActionScriptDaemonAnalyzerTestCase {
  @Override
  protected void setUpJdk() {
    FlexTestUtils.setupFlexSdk(myModule, getTestName(false), getClass());
  }

  @Override
  protected String getBasePath() {
    return "";
  }

  @Override
  protected String getTestDataPath() {
    return FlexTestUtils.getTestDataPath(FlexHighlightingTest.BASE_PATH);
  }

  @Override
  protected String getExtension() {
    return "as";
  }

  @JSTestOptions({JSTestOption.WithLoadingAndSavingCaches, JSTestOption.WithFlexFacet, JSTestOption.WithJsSupportLoader})
  public void testPerformance() throws Exception {
    final boolean doProfiling = false;
    if (doProfiling) ProfilingUtil.startCPUProfiling();
    try {
      PlatformTestUtil.startPerformanceTest("Mxml should be highlighted fast!",9000, new ThrowableRunnable() {
        @Override
        public void run() throws Throwable {
          doTestFor(true, getTestName(false) + ".mxml", "UsingSwcStubs2.swc");
        }
      }).cpuBound().usesAllCPUCores().useLegacyScaling().assertTiming();
    }
    finally {
      if (doProfiling) ProfilingUtil.captureCPUSnapshot();
    }
  }

  @Override
  protected ModuleType getModuleType() {
    return FlexModuleType.getInstance();
  }
}
