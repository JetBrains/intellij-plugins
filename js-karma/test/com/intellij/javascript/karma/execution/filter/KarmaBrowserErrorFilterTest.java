package com.intellij.javascript.karma.execution.filter;

import com.intellij.execution.filters.FileHyperlinkRawData;
import com.intellij.javascript.karma.filter.KarmaBrowserErrorFilter;
import com.intellij.util.containers.ContainerUtil;
import junit.framework.TestCase;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class KarmaBrowserErrorFilterTest extends TestCase {

  public void testBasePath() {
    String s = "at http://localhost:9876/base/spec/personSpec.js?1368878723000:22";
    doTest(s, new FileHyperlinkRawData("spec/personSpec.js", 21, -1, 3, s.length()));
  }

  public void testAbsolutePath() {
    String s = "at http://localhost:9876/absolute/home/karma-chai-sample/test/test.js?1378466989000:1";
    doTest(s, new FileHyperlinkRawData("/home/karma-chai-sample/test/test.js", 0, -1, 3, s.length()));
  }

  public void testAbsolutePathWithoutPath() {
    String s = "at http://localhost:9876/absoluteC:/Users/User/AppData/Roaming/npm/node_modules/karma-commonjs/client/commonjs_bridge.js?1392838273000:21";
    FileHyperlinkRawData expected = new FileHyperlinkRawData(
      "C:/Users/User/AppData/Roaming/npm/node_modules/karma-commonjs/client/commonjs_bridge.js",
      20, -1, 3, s.length()
    );
    doTest(s, expected);
  }

  private static void doTest(@NotNull String line, @Nullable FileHyperlinkRawData expected) {
    List<FileHyperlinkRawData> actualList = KarmaBrowserErrorFilter.FINDER.find(line);
    List<FileHyperlinkRawData> expectedList = ContainerUtil.createMaybeSingletonList(expected);
    assertEquals(expectedList, actualList);
  }
}
