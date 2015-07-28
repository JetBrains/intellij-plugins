package com.intellij.lang.javascript;

import com.intellij.lang.javascript.flex.FlexModuleType;
import com.intellij.openapi.module.ModuleType;
import com.intellij.testFramework.PlatformTestUtil;
import com.intellij.util.ProfilingUtil;
import com.intellij.util.ThrowableRunnable;
import org.jetbrains.annotations.NonNls;

public class FlexHighlightingPerformanceTest extends JSDaemonAnalyzerTestCase {
  @NonNls private static final String BASE_PATH = "/flex_highlighting";

  @Override
  protected void setUpJdk() {
    JSTestUtils.setupFlexSdk(myModule, getTestName(false), getClass());
  }

  @Override
  protected void setUpModule() {
    super.setUpModule();
  }

  @Override
  protected String getBasePath() {
    return BASE_PATH;
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
      }).cpuBound().usesAllCPUCores().assertTiming();
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
