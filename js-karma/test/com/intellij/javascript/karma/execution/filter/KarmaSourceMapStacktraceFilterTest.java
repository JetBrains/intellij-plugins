package com.intellij.javascript.karma.execution.filter;

import com.intellij.execution.filters.AbstractFileHyperlinkFilter;
import com.intellij.execution.filters.FileHyperlinkRawData;
import com.intellij.javascript.karma.filter.KarmaSourceMapStacktraceFilter;
import com.intellij.javascript.testFramework.util.BrowserStacktraceFilters;
import com.intellij.openapi.project.Project;
import com.intellij.testFramework.junit5.TestApplication;
import com.intellij.testFramework.junit5.fixture.TestFixture;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;

import java.util.List;

import static com.intellij.testFramework.junit5.fixture.FixturesKt.projectFixture;
import static org.junit.jupiter.api.Assertions.assertEquals;

@TestApplication
public class KarmaSourceMapStacktraceFilterTest {
  private static final TestFixture<Project> projectFixture = projectFixture();

  @Test
  public void testChrome() {
    KarmaSourceMapStacktraceFilter filter = new KarmaSourceMapStacktraceFilter(
      projectFixture.get(),
      null,
      BrowserStacktraceFilters.createFilter(BrowserStacktraceFilters.CHROME, projectFixture.get(), null)
    );
    doTest("	    at Object.<anonymous> (test/test-multiply.coffee:3:27 <- test/test-multiply.js:3:35)", filter,
           new FileHyperlinkRawData("test/test-multiply.coffee", 2, 26, 28, 58));
  }

  @Test
  public void testPhantomJS() {
    KarmaSourceMapStacktraceFilter filter = new KarmaSourceMapStacktraceFilter(
      projectFixture.get(),
      null,
      BrowserStacktraceFilters.createFilter(BrowserStacktraceFilters.PHANTOM_JS, projectFixture.get(), null)
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

  @Test
  public void testFirefox() {
    KarmaSourceMapStacktraceFilter filter = new KarmaSourceMapStacktraceFilter(
      projectFixture.get(),
      null,
      BrowserStacktraceFilters.createFilter("firefox", projectFixture.get(), null)
    );
    doTest("	@test/test-multiply.coffee:3:4 <- test/test-multiply.js:3:12", filter,
           new FileHyperlinkRawData("test/test-multiply.coffee", 2, 3, 2, 31));
  }

  @Test
  public void testWebpack() {
    KarmaSourceMapStacktraceFilter filter = new KarmaSourceMapStacktraceFilter(
      projectFixture.get(),
      null,
      BrowserStacktraceFilters.createFilter("firefox", projectFixture.get(), null)
    );
    doTest("	ZoneAwareError@webpack:///~/zone.js/dist/zone.js:923:0 <- src/polyfills.ts:3571:28", filter,
           new FileHyperlinkRawData("node_modules/zone.js/dist/zone.js", 922, -1, 16, 55));
  }

  @Test
  public void testFirefoxScopedPackage() {
    KarmaSourceMapStacktraceFilter filter = new KarmaSourceMapStacktraceFilter(
      projectFixture.get(),
      null,
      BrowserStacktraceFilters.createFilter("firefox", projectFixture.get(), null)
    );
    doTest("async/<@webpack:///~/@angular/core/@angular/core/testing.es5.js:49:0 <- src/test.ts:14768:13 [ProxyZone]",
           filter,
           new FileHyperlinkRawData("node_modules/@angular/core/@angular/core/testing.es5.js", 48, -1, 8, 68));
  }

  @Test
  public void testHttpLink() {
    KarmaSourceMapStacktraceFilter filter = new KarmaSourceMapStacktraceFilter(
      projectFixture.get(),
      null,
      BrowserStacktraceFilters.createFilter("chrome", projectFixture.get(), null)
    );
    doTest("\t    at UserContext.<anonymous> (http://localhost:9876/src/app/app.component.spec.ts?:14:17)\n",
           filter,
           new FileHyperlinkRawData("src/app/app.component.spec.ts", 13, 16, 33, 91));
    doTest("\t    at UserContext.<anonymous> (http://localhost:9876/base/src/app/app.component.spec.ts?:14:17)\n",
           filter,
           new FileHyperlinkRawData("src/app/app.component.spec.ts", 13, 16, 33, 96));
    doTest("\t    at UserContext.<anonymous> (http://localhost:9876/absolute/src/app/app.component.spec.ts?:14:17)\n",
           filter,
           new FileHyperlinkRawData("/src/app/app.component.spec.ts", 13, 16, 33, 100));
  }

  private static void doTest(@NotNull String line,
                             @NotNull AbstractFileHyperlinkFilter filter,
                             @NotNull FileHyperlinkRawData expected) {
    List<FileHyperlinkRawData> actualList = filter.parse(line);
    assertEquals(1, actualList.size());
    assertEquals(expected, actualList.get(0));
  }
}
