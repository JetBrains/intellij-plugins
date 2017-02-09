package com.jetbrains.dart.analysisServer;

import com.intellij.openapi.roots.OrderRootType;
import com.intellij.openapi.roots.impl.libraries.ProjectLibraryTable;
import com.intellij.testFramework.fixtures.CodeInsightFixtureTestCase;
import com.intellij.util.ArrayUtil;
import com.jetbrains.lang.dart.sdk.DartSdk;
import com.jetbrains.lang.dart.sdk.DartSdkLibUtil;
import com.jetbrains.lang.dart.util.DartTestUtils;

public class DartSdkConfigurationTest extends CodeInsightFixtureTestCase {
  @Override
  public void setUp() throws Exception {
    super.setUp();
    DartTestUtils.configureDartSdk(myModule, getTestRootDisposable(), true);
  }

  private static void checkSdkRoots(String sdkHomePath, String[] actualRoots) {
    final String[] expectedRoots = {
      "file://" + sdkHomePath + "/lib/async",
      "file://" + sdkHomePath + "/lib/collection",
      "file://" + sdkHomePath + "/lib/convert",
      "file://" + sdkHomePath + "/lib/core",
      "file://" + sdkHomePath + "/lib/developer",
      "file://" + sdkHomePath + "/lib/html",
      "file://" + sdkHomePath + "/lib/indexed_db",
      "file://" + sdkHomePath + "/lib/io",
      "file://" + sdkHomePath + "/lib/isolate",
      "file://" + sdkHomePath + "/lib/js",
      "file://" + sdkHomePath + "/lib/js_util",
      "file://" + sdkHomePath + "/lib/math",
      "file://" + sdkHomePath + "/lib/mirrors",
      "file://" + sdkHomePath + "/lib/svg",
      "file://" + sdkHomePath + "/lib/typed_data",
      "file://" + sdkHomePath + "/lib/web_audio",
      "file://" + sdkHomePath + "/lib/web_gl",
      "file://" + sdkHomePath + "/lib/web_sql",
    };

    assertOrderedEquals(actualRoots, expectedRoots);
  }

  public void testSdkRoots() throws Exception {
    final DartSdk sdk = DartSdk.getDartSdk(getProject());
    assertNotNull(sdk);
    final String[] actualRoots =
      ProjectLibraryTable.getInstance(getProject()).getLibraries()[0].getRootProvider().getUrls(OrderRootType.CLASSES);
    checkSdkRoots(sdk.getHomePath(), actualRoots);
  }

  public void testSdkRootsFromLibrariesFile() throws Exception {
    final DartSdk sdk = DartSdk.getDartSdk(getProject());
    assertNotNull(sdk);
    final String[] actualRoots = ArrayUtil.toStringArray(DartSdkLibUtil.getRootUrlsFromLibrariesFile(getProject(), sdk.getHomePath()));
    checkSdkRoots(sdk.getHomePath(), actualRoots);
  }

  public void testSdkRootsUsingBlacklist() throws Exception {
    final DartSdk sdk = DartSdk.getDartSdk(getProject());
    assertNotNull(sdk);
    final String[] actualRoots = ArrayUtil.toStringArray(DartSdkLibUtil.getRootUrlsFailover(sdk.getHomePath()));
    checkSdkRoots(sdk.getHomePath(), actualRoots);
  }
}
