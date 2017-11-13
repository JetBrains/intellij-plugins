package org.intellij.plugins.markdown.html;

import com.intellij.openapi.application.PathManager;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;
import com.intellij.testFramework.fixtures.LightPlatformCodeInsightFixtureTestCase;
import com.intellij.util.containers.ContainerUtil;
import org.intellij.markdown.IElementType;
import org.intellij.markdown.ast.ASTNode;
import org.intellij.markdown.html.GeneratingProvider;
import org.intellij.markdown.html.HtmlGenerator;
import org.intellij.markdown.parser.LinkMap;
import org.intellij.markdown.parser.MarkdownParser;
import org.intellij.plugins.markdown.MarkdownTestingUtil;
import org.intellij.plugins.markdown.lang.parser.MarkdownParserManager;
import org.intellij.plugins.markdown.ui.preview.MarkdownCodeFencePluginCacheProvider;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.net.URI;
import java.util.Map;

public class MarkdownHtmlGenerationTest extends LightPlatformCodeInsightFixtureTestCase {
  @NotNull
  @Override
  protected String getTestDataPath() {
    return MarkdownTestingUtil.TEST_DATA_PATH + "/html";
  }

  private void doTest(@NotNull String htmlText) {
    PsiFile mdFile = myFixture.configureByFile(getTestName(true) + ".md");

    VirtualFile virtualFile = mdFile.getVirtualFile();
    VirtualFile parent = virtualFile.getParent();
    URI baseUri = parent != null ? new File(parent.getPath()).toURI() : null;
    String text = mdFile.getText();

    final ASTNode parsedTree = new MarkdownParser(MarkdownParserManager.FLAVOUR).buildMarkdownTreeFromString(text);
    MarkdownCodeFencePluginCacheProvider codeFencePluginCache = new MarkdownCodeFencePluginCacheProvider(virtualFile);

    Map<IElementType, GeneratingProvider> map = ContainerUtil
      .newHashMap(MarkdownParserManager.FLAVOUR.createHtmlGeneratingProviders(LinkMap.Builder.buildLinkMap(parsedTree, text), baseUri));
    map.putAll(MarkdownParserManager.CODE_FENCE_PLUGIN_FLAVOUR.createHtmlGeneratingProviders(codeFencePluginCache));

    assertTrue(new HtmlGenerator(text, parsedTree, map, true).generateHtml().contains(htmlText));
  }

  public void testCodeFenceWithLang() {
    doTestByHtmlFile();
  }

  public void testCodeFenceWithoutLang() {
    doTestByHtmlFile();
  }

  public void testPlantUML1() {
    doTestPlantUML();
  }

  public void testPlantUML2() {
    doTestPlantUML();
  }

  public void testPuml() {
    doTestPlantUML();
  }

  void doTestPlantUML() {
    doTest("<img src=\"file:" + PathManager.getSystemPath());
  }

  void doTestByHtmlFile() {
    doTest(myFixture.configureByFile(getTestName(true) + ".html").getText());
  }
}
