package org.intellij.plugins.postcss;

import com.intellij.openapi.util.io.FileUtil;
import com.intellij.testFramework.fixtures.LightPlatformCodeInsightFixtureTestCase;
import org.jetbrains.annotations.NotNull;

public abstract class PostCssFixtureTestCase extends LightPlatformCodeInsightFixtureTestCase {
  @Override
  protected String getTestDataPath() {
    return FileUtil.join(PostCssTestUtils.getFullTestDataPath(getClass()), getTestDataSubdir());
  }

  @NotNull
  protected String getTestDataSubdir() {
    return "";
  }
}