// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.jetbrains.lang.dart.breadcrumbs;

import com.intellij.ui.components.breadcrumbs.Crumb;
import com.jetbrains.lang.dart.DartCodeInsightFixtureTestCase;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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

  @NotNull
  private static String getBreadcrumbsAndTooltips(List<Crumb> crumbs) {
    // this intentionally matches other breadcrumb testing support for better readability and maintenance
    return crumbs
      .stream()
      .flatMap(crumb -> Stream.of("Crumb:", crumb.getText(), "Tooltip:", crumb.getTooltip()))
      .collect(Collectors.joining("\n"));
  }

  public void testClass() {
    doTest();
  }

  public void testMixin() {
    doTest();
  }

  public void testEnum() {
    doTest();
  }

  public void testGetter() {
    doTest();
  }

  public void testGetterFatArrow() {
    doTest();
  }

  public void testSetter() {
    doTest();
  }

  public void testSetterFatArrow() {
    doTest();
  }

  public void testMethod() {
    doTest();
  }

  public void testFunction() {
    doTest();
  }
}
