package com.intellij.javascript.karma.execution.filter;

import com.intellij.execution.filters.FileHyperlinkRawData;
import com.intellij.javascript.karma.filter.KarmaSourceMapStacktraceFilter;
import com.intellij.util.containers.ContainerUtil;
import junit.framework.TestCase;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class KarmaSourceMapStacktraceFilterTest extends TestCase {
  public void testChrome() throws Exception {
    doTest("	    at Object.<anonymous> (test/test-multiply.coffee:3:27 <- test/test-multiply.js:3:35)", ContainerUtil.newArrayList(
      new FileHyperlinkRawData("test/test-multiply.coffee", 2, 26, 28, 58),
      new FileHyperlinkRawData("test/test-multiply.js", 2, 34, 62, 88)
    ));
  }

  public void testPhantomJS() throws Exception {
    doTest("	test/test-multiply.coffee:3:32 <- test/test-multiply.js:3:39", ContainerUtil.newArrayList(
      new FileHyperlinkRawData("test/test-multiply.coffee", 2, 31, 1, 31),
      new FileHyperlinkRawData("test/test-multiply.js", 2, 38, 35, 61)
    ));
  }

  public void testFirefox() throws Exception {
    doTest("	@test/test-multiply.coffee:3:4 <- test/test-multiply.js:3:12", ContainerUtil.newArrayList(
      new FileHyperlinkRawData("test/test-multiply.coffee", 2, 3, 2, 31),
      new FileHyperlinkRawData("test/test-multiply.js", 2, 11, 35, 61)
    ));
  }

  private static void doTest(@NotNull String line, @NotNull List<FileHyperlinkRawData> expectedList) {
    List<FileHyperlinkRawData> actualList = KarmaSourceMapStacktraceFilter.FINDER.find(line);
    assertEquals(expectedList, actualList);
  }
}
