package com.intellij.javascript.karma.execution.filter;

import com.intellij.execution.filters.FileHyperlinkRawData;
import com.intellij.javascript.karma.filter.KarmaSourceMapStacktraceFilter;
import junit.framework.TestCase;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class KarmaSourceMapStacktraceFilterTest extends TestCase {
  public void testChrome() {
    doTest("	    at Object.<anonymous> (test/test-multiply.coffee:3:27 <- test/test-multiply.js:3:35)",
           new FileHyperlinkRawData("test/test-multiply.coffee", 2, 26, 28, 58));
  }

  public void testPhantomJS() {
    doTest("	test/test-multiply.coffee:3:32 <- test/test-multiply.js:3:39",
           new FileHyperlinkRawData("test/test-multiply.coffee", 2, 31, 1, 31));
  }

  public void testFirefox() {
    doTest("	@test/test-multiply.coffee:3:4 <- test/test-multiply.js:3:12",
           new FileHyperlinkRawData("test/test-multiply.coffee", 2, 3, 2, 31));
  }

  public void testWebpack() {
    doTest("	ZoneAwareError@webpack:///~/zone.js/dist/zone.js:923:0 <- src/polyfills.ts:3571:28",
           new FileHyperlinkRawData("node_modules/zone.js/dist/zone.js", 922, -1, 16, 55));
  }

  public void testScopedPackage() {
    doTest("async/<@webpack:///~/@angular/core/@angular/core/testing.es5.js:49:0 <- src/test.ts:14768:13 [ProxyZone]",
           new FileHyperlinkRawData("node_modules/@angular/core/@angular/core/testing.es5.js", 48, -1, 8, 68));
    doTest("webpack:///~/@angular/core/@angular/core/testing.es5.js:49:0 <- src/test.ts:14768:26",
           new FileHyperlinkRawData("node_modules/@angular/core/@angular/core/testing.es5.js", 48, -1, 0, 60));
  }

  private static void doTest(@NotNull String line, @NotNull FileHyperlinkRawData expected) {
    List<FileHyperlinkRawData> actualList = KarmaSourceMapStacktraceFilter.FINDER.find(line);
    assertEquals(1, actualList.size());
    assertEquals(expected, actualList.get(0));
  }
}
