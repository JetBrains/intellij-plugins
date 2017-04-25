package com.intellij.flex.highlighting;

import com.intellij.codeInsight.daemon.GutterMark;
import com.intellij.flex.util.FlexModuleFixtureBuilder;
import com.intellij.flex.util.FlexModuleFixtureBuilderImpl;
import com.intellij.flex.util.FlexTestUtils;
import com.intellij.javascript.flex.css.FlexStylesIndexableSetContributor;
import com.intellij.javascript.flex.mxml.FlexMxmlColorAnnotator;
import com.intellij.javascript.flex.mxml.schema.FlexSchemaHandler;
import com.intellij.lang.javascript.JSTestOption;
import com.intellij.lang.javascript.JSTestOptions;
import com.intellij.openapi.vfs.newvfs.impl.VfsRootAccess;
import com.intellij.testFramework.fixtures.CodeInsightFixtureTestCase;
import com.intellij.testFramework.fixtures.IdeaTestFixtureFactory;
import com.intellij.util.ui.EmptyIcon;
import com.intellij.xml.util.ColorIconCache;
import org.jetbrains.annotations.NotNull;

import static com.intellij.openapi.vfs.VfsUtilCore.convertFromUrl;
import static com.intellij.openapi.vfs.VfsUtilCore.urlToPath;

public class FlexColorAnnotatorTest extends CodeInsightFixtureTestCase<FlexModuleFixtureBuilder> {
  @Override
  protected Class<FlexModuleFixtureBuilder> getModuleBuilderClass() {
    return FlexModuleFixtureBuilder.class;
  }

  @Override
  protected void setUp() throws Exception {
    VfsRootAccess.allowRootAccess(getTestRootDisposable(),
                                  urlToPath(convertFromUrl(FlexSchemaHandler.class.getResource("z.xsd"))),
                                  urlToPath(convertFromUrl(FlexStylesIndexableSetContributor.class.getResource("FlexStyles.as"))));

    IdeaTestFixtureFactory.getFixtureFactory().registerFixtureBuilder(FlexModuleFixtureBuilder.class, FlexModuleFixtureBuilderImpl.class);
    super.setUp();
    FlexTestUtils.setupFlexSdk(myModule, getTestName(false), getClass());
    myFixture.setTestDataPath(FlexTestUtils.getTestDataPath("flex_color_gutter"));
  }

  @JSTestOptions(JSTestOption.WithFlexSdk)
  public void testHighlighting() throws Exception {
    myFixture.configureByFile(getTestName(false) + ".mxml");
    myFixture.checkHighlighting();
  }

  @JSTestOptions(JSTestOption.WithFlexSdk)
  public void testGutter1() throws Exception {
    FlexMxmlColorAnnotator.MyRenderer renderer = findAppropriateRenderer("mxml");
    assertInstanceOf(renderer.getIcon(), ColorIconCache.ColorIcon.class);
  }

  @JSTestOptions(JSTestOption.WithFlexSdk)
  public void testGutter2() throws Exception {
    FlexMxmlColorAnnotator.MyRenderer renderer = findAppropriateRenderer("mxml");
    assertInstanceOf(renderer.getIcon(), ColorIconCache.ColorIcon.class);
  }

  @JSTestOptions(JSTestOption.WithFlexSdk)
  public void testGutter3() throws Exception {
    FlexMxmlColorAnnotator.MyRenderer renderer = findAppropriateRenderer("mxml");
    assertInstanceOf(renderer.getIcon(), EmptyIcon.class);
  }

  @JSTestOptions({JSTestOption.WithFlexFacet, JSTestOption.WithCssSupportLoader})
  public void testGutter4() throws Exception {
    GutterMark r = myFixture.findGutter(getTestName(false) + '.' + "css");
    assertNotNull(r);
    assertInstanceOf(r.getIcon(), ColorIconCache.ColorIcon.class);
  }

  @JSTestOptions({JSTestOption.WithFlexFacet, JSTestOption.WithCssSupportLoader})
  public void testGutter5() throws Exception {
    GutterMark r = myFixture.findGutter(getTestName(false) + '.' + "css");
    assertNotNull(r);
    assertInstanceOf(r.getIcon(), ColorIconCache.ColorIcon.class);
  }

  @JSTestOptions(JSTestOption.WithFlexSdk)
  public void testGutter6() throws Exception {
    FlexMxmlColorAnnotator.MyRenderer renderer = findAppropriateRenderer("mxml");
    assertInstanceOf(renderer.getIcon(), ColorIconCache.ColorIcon.class);
  }

  @JSTestOptions(JSTestOption.WithFlexSdk)
  public void testIdea94474() throws Exception {
    GutterMark r = myFixture.findGutter(getTestName(false) + '.' + "css");
    assertNull(r);
  }

  @NotNull
  private FlexMxmlColorAnnotator.MyRenderer findAppropriateRenderer(String extension) throws Exception {
    GutterMark r = myFixture.findGutter(getTestName(false) + '.' + extension);
    assertNotNull(r);
    assertInstanceOf(r, FlexMxmlColorAnnotator.MyRenderer.class);
    return (FlexMxmlColorAnnotator.MyRenderer)r;
  }
}
