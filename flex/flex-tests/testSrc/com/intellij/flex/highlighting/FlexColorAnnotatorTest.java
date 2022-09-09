// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.intellij.flex.highlighting;

import com.intellij.codeInsight.daemon.GutterMark;
import com.intellij.flex.FlexTestOption;
import com.intellij.flex.FlexTestOptions;
import com.intellij.flex.util.FlexModuleFixtureBuilder;
import com.intellij.flex.util.FlexModuleFixtureBuilderImpl;
import com.intellij.flex.util.FlexTestUtils;
import com.intellij.javascript.flex.mxml.FlexMxmlColorAnnotator;
import com.intellij.testFramework.fixtures.CodeInsightFixtureTestCase;
import com.intellij.testFramework.fixtures.IdeaTestFixtureFactory;
import com.intellij.util.ui.ColorIcon;
import com.intellij.util.ui.EmptyIcon;
import org.jetbrains.annotations.NotNull;

public class FlexColorAnnotatorTest extends CodeInsightFixtureTestCase<FlexModuleFixtureBuilder> {
  @Override
  protected Class<FlexModuleFixtureBuilder> getModuleBuilderClass() {
    return FlexModuleFixtureBuilder.class;
  }

  @Override
  protected void setUp() throws Exception {
    FlexTestUtils.allowFlexVfsRootsFor(getTestRootDisposable(), "");

    IdeaTestFixtureFactory.getFixtureFactory().registerFixtureBuilder(FlexModuleFixtureBuilder.class, FlexModuleFixtureBuilderImpl.class);
    super.setUp();
    FlexTestUtils.setupFlexSdk(myModule, getTestName(false), getClass(), myFixture.getProjectDisposable());
    myFixture.setTestDataPath(FlexTestUtils.getTestDataPath("flex_color_gutter"));
  }

  @FlexTestOptions(FlexTestOption.WithFlexSdk)
  public void testHighlighting() {
    myFixture.configureByFile(getTestName(false) + ".mxml");
    myFixture.checkHighlighting();
  }

  @FlexTestOptions(FlexTestOption.WithFlexSdk)
  public void testGutter1() {
    FlexMxmlColorAnnotator.MyRenderer renderer = findAppropriateRenderer("mxml");
    assertInstanceOf(renderer.getIcon(), ColorIcon.class);
  }

  @FlexTestOptions(FlexTestOption.WithFlexSdk)
  public void testGutter2() {
    FlexMxmlColorAnnotator.MyRenderer renderer = findAppropriateRenderer("mxml");
    assertInstanceOf(renderer.getIcon(), ColorIcon.class);
  }

  @FlexTestOptions(FlexTestOption.WithFlexSdk)
  public void testGutter3() {
    FlexMxmlColorAnnotator.MyRenderer renderer = findAppropriateRenderer("mxml");
    assertInstanceOf(renderer.getIcon(), EmptyIcon.class);
  }

  @FlexTestOptions(FlexTestOption.WithFlexFacet)
  public void testGutter4() {
    GutterMark r = myFixture.findGutter(getTestName(false) + '.' + "css");
    assertNotNull(r);
    assertInstanceOf(r.getIcon(), ColorIcon.class);
  }

  @FlexTestOptions(FlexTestOption.WithFlexFacet)
  public void testGutter5() {
    GutterMark r = myFixture.findGutter(getTestName(false) + '.' + "css");
    assertNotNull(r);
    assertInstanceOf(r.getIcon(), ColorIcon.class);
  }

  @FlexTestOptions(FlexTestOption.WithFlexSdk)
  public void testGutter6() {
    FlexMxmlColorAnnotator.MyRenderer renderer = findAppropriateRenderer("mxml");
    assertInstanceOf(renderer.getIcon(), ColorIcon.class);
  }

  @FlexTestOptions(FlexTestOption.WithFlexSdk)
  public void testIdea94474() {
    GutterMark r = myFixture.findGutter(getTestName(false) + '.' + "css");
    assertNull(r);
  }

  @NotNull
  private FlexMxmlColorAnnotator.MyRenderer findAppropriateRenderer(String extension) {
    GutterMark r = myFixture.findGutter(getTestName(false) + '.' + extension);
    assertNotNull(r);
    assertInstanceOf(r, FlexMxmlColorAnnotator.MyRenderer.class);
    return (FlexMxmlColorAnnotator.MyRenderer)r;
  }
}
