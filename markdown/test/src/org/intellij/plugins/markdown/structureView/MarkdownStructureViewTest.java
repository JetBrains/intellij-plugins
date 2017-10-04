package org.intellij.plugins.markdown.structureView;

import com.intellij.openapi.ui.Queryable;
import com.intellij.testFramework.PlatformTestUtil;
import com.intellij.testFramework.fixtures.LightPlatformCodeInsightFixtureTestCase;
import com.intellij.util.ui.tree.TreeUtil;
import org.intellij.plugins.markdown.MarkdownTestingUtil;

import javax.swing.*;

public class MarkdownStructureViewTest extends LightPlatformCodeInsightFixtureTestCase {

  @Override
  protected String getTestDataPath() {
    return MarkdownTestingUtil.TEST_DATA_PATH + "/structureView/";
  }

  public void doTest() {
    myFixture.configureByFile(getTestName(true) + ".md");
    myFixture.testStructureView(svc -> {
      svc.select(svc.getTreeModel().getCurrentEditorElement(), false);
      JTree tree = svc.getTree();
      TreeUtil.expandAll(tree);
      assertSameLinesWithFile(
        getTestDataPath() + '/' + getTestName(true) + ".txt",
        PlatformTestUtil.print(tree, tree.getModel().getRoot(), new Queryable.PrintInfo(null, new String[]{"location"}), true));
    });
  }

  public void testOneParagraph() {
    doTest();
  }

  public void testTwoParagraphs() {
    doTest();
  }

  public void testNormalATXDocument() {
    doTest();
  }

  public void testNormalSetextDocument() {
    doTest();
  }

  public void testHeadersLadder() {
    doTest();
  }

  public void testHeadersUnderBlockquotesAndLists() {
    doTest();
  }

  public void testPuppetlabsCoreTypes() {
    doTest();
  }

}
