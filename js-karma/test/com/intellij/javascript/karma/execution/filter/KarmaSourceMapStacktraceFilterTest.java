package com.intellij.javascript.karma.execution.filter;

import com.intellij.execution.filters.AbstractFileHyperlinkFilter;
import com.intellij.execution.filters.FileHyperlinkRawData;
import com.intellij.javascript.karma.filter.KarmaSourceMapStacktraceFilter;
import com.intellij.javascript.testFramework.util.BrowserStacktraceFilters;
import com.intellij.testFramework.fixtures.CodeInsightFixtureTestCase;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class KarmaSourceMapStacktraceFilterTest extends CodeInsightFixtureTestCase {
  public void testChrome() {
    KarmaSourceMapStacktraceFilter filter = new KarmaSourceMapStacktraceFilter(
      getProject(),
      null,
      BrowserStacktraceFilters.createFilter(BrowserStacktraceFilters.CHROME, getProject(), null)
    );
    doTest("	    at Object.<anonymous> (test/test-multiply.coffee:3:27 <- test/test-multiply.js:3:35)", filter,
           new FileHyperlinkRawData("test/test-multiply.coffee", 2, 26, 28, 58));
  }

  public void testPhantomJS() {
    KarmaSourceMapStacktraceFilter filter = new KarmaSourceMapStacktraceFilter(
      getProject(),
      null,
      BrowserStacktraceFilters.createFilter(BrowserStacktraceFilters.PHANTOM_JS, getProject(), null)
    );
    doTest("	test/test-multiply.coffee:3:32 <- test/test-multiply.js:3:39", filter,
           new FileHyperlinkRawData("test/test-multiply.coffee", 2, 31, 1, 31));
    doTest("\tinvokeTask@webpack:///~/zone.js/dist/zone.js:421:0 <- src/polyfills.ts:3254:36\n",
           filter,
           new FileHyperlinkRawData("node_modules/zone.js/dist/zone.js", 420, -1, 12, 51));
    doTest("webpack:///~/@angular/core/@angular/core/testing.es5.js:49:0 <- src/test.ts:14768:26",
           filter,
           new FileHyperlinkRawData("node_modules/@angular/core/@angular/core/testing.es5.js", 48, -1, 0, 60));
    doTest("\trunInTestZone@webpack:///~/@angular/core/@angular/core/testing.es5.js:106:0 <- src/test.ts:16096:35\n",
           filter,
           new FileHyperlinkRawData("node_modules/@angular/core/@angular/core/testing.es5.js", 105, -1, 15, 76));
  }

  public void testFirefox() {
    KarmaSourceMapStacktraceFilter filter = new KarmaSourceMapStacktraceFilter(
      getProject(),
      null,
      BrowserStacktraceFilters.createFilter("firefox", getProject(), null)
    );
    doTest("	@test/test-multiply.coffee:3:4 <- test/test-multiply.js:3:12", filter,
           new FileHyperlinkRawData("test/test-multiply.coffee", 2, 3, 2, 31));
  }

  public void testWebpack() {
    KarmaSourceMapStacktraceFilter filter = new KarmaSourceMapStacktraceFilter(
      getProject(),
      null,
      BrowserStacktraceFilters.createFilter("firefox", getProject(), null)
    );
    doTest("	ZoneAwareError@webpack:///~/zone.js/dist/zone.js:923:0 <- src/polyfills.ts:3571:28", filter,
           new FileHyperlinkRawData("node_modules/zone.js/dist/zone.js", 922, -1, 16, 55));
  }

  public void testFirefoxScopedPackage() {
    KarmaSourceMapStacktraceFilter filter = new KarmaSourceMapStacktraceFilter(
      getProject(),
      null,
      BrowserStacktraceFilters.createFilter("firefox", getProject(), null)
    );
    doTest("async/<@webpack:///~/@angular/core/@angular/core/testing.es5.js:49:0 <- src/test.ts:14768:13 [ProxyZone]",
           filter,
           new FileHyperlinkRawData("node_modules/@angular/core/@angular/core/testing.es5.js", 48, -1, 8, 68));
  }

  private static void doTest(@NotNull String line,
                             @NotNull AbstractFileHyperlinkFilter filter,
                             @NotNull FileHyperlinkRawData expected) {
    List<FileHyperlinkRawData> actualList = filter.parse(line);
    assertEquals(1, actualList.size());
    assertEquals(expected, actualList.get(0));
  }
}
