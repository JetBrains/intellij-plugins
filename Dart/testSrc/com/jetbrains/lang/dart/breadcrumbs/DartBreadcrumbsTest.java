// Copyright 2000-2022 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.jetbrains.lang.dart.breadcrumbs;

import com.intellij.ui.components.breadcrumbs.Crumb;
import com.jetbrains.lang.dart.DartCodeInsightFixtureTestCase;

import java.util.List;
import java.util.stream.Collectors;

public class DartBreadcrumbsTest extends DartCodeInsightFixtureTestCase {
  @Override
  protected String getTestDataPath() {
    return super.getTestDataPath() + "/breadcrumbs";
  }

  private void doTest() {
    final String testName = getTestName(true);
    myFixture.configureByFile(testName + ".dart");
    final String breadcrumbsAndTooltips = getBreadcrumbsAndTooltips(myFixture.getBreadcrumbsAtCaret());
    assertSameLinesWithFile(getTestDataPath() + "/" + testName + "_crumbs.txt", breadcrumbsAndTooltips);
  }

  private static String getBreadcrumbsAndTooltips(List<Crumb> crumbs) {
    return crumbs.stream().map(Crumb::getText).collect(Collectors.joining("\n"));
  }

  public void testClass() {
    doTest();
  }

  public void testClassVariable() {
    doTest();
  }

  public void testEnum() {
    doTest();
  }

  public void testFileVariable() {
    doTest();
  }

  public void testFunction() {
    doTest();
  }

  public void testGetter() {
    doTest();
  }

  public void testGetterFatArrow() {
    doTest();
  }

  public void testLocalVariable() {
    doTest();
  }

  public void testMethod() {
    doTest();
  }

  public void testMixin() {
    doTest();
  }

  public void testSetter() {
    doTest();
  }

  public void testSetterFatArrow() {
    doTest();
  }

  public void testUnitTest() {
    doTest();
  }
}
