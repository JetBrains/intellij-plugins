package com.dmarcotte.handlebars.structure;

import com.intellij.ide.structureView.newStructureView.StructureViewComponent;
import com.intellij.testFramework.fixtures.LightPlatformCodeInsightFixtureTestCase;
import com.intellij.util.Consumer;

import javax.swing.*;

import static com.intellij.testFramework.PlatformTestUtil.assertTreeEqual;

public class HbStructureViewTest extends LightPlatformCodeInsightFixtureTestCase {

  private static final String ourTestFileName = "test.hbs";

  private void doStructureViewTest(final String fileText, final String expectedTree) {
    myFixture.configureByText(ourTestFileName, fileText);

    myFixture.testStructureView(new Consumer<StructureViewComponent>() {
      @Override
      public void consume(StructureViewComponent component) {
        JTree tree = component.getTree();

        // expand the whole tree
        int rowCount = tree.getRowCount();
        for (int i = 0; i <= rowCount; i++) {
          tree.expandRow(i);
        }

        assertTreeEqual(tree, expectedTree + "\n");
      }
    });
  }

  public void testNestedBlocks() throws Exception {
    doStructureViewTest(

      "{{#foo}}\n" +
      "    {{#bar}}\n" +
      "        {{baz}}<caret>\n" +
      "    {{/bar}}\n" +
      "{{/foo}}\n",

      "-" + ourTestFileName + "\n" +
      " -foo\n" +
      "  -bar\n" +
      "   baz"
    );
  }

  public void testUnclosedBlocks() throws Exception {
    doStructureViewTest(

      "{{#foo}}\n" +
      "{{^bar}}",

      "-" + ourTestFileName + "\n" +
      " -foo\n" +
      "  bar"
    );
  }

  public void testAllConstructs() throws Exception {
    doStructureViewTest(

      "{{#block}}\n" +
      "{{/block}}\n" +
      "{{^inverse}}\n" +
      "    {{else}}\n" +
      "{{/inverse}}\n" +
      "{{mustache}}\n" +
      "{{>partial}}\n" +
      "{{@data}}\n" +
      "{{^}}\n" +
      "{{{unescaped}}\n",

      "-" + ourTestFileName + "\n" +
      " block\n" +
      " -inverse\n" +
      "  else\n" +
      " mustache\n" +
      " partial\n" +
      " data\n" +
      " \n" +
      " unescaped"
    );
  }
}
