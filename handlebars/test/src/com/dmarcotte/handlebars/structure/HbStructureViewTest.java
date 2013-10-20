package com.dmarcotte.handlebars.structure;

import com.intellij.ide.structureView.StructureViewBuilder;
import com.intellij.ide.structureView.impl.StructureViewComposite;
import com.intellij.ide.structureView.newStructureView.StructureViewComponent;
import com.intellij.lang.LanguageStructureViewBuilder;
import com.intellij.openapi.fileEditor.FileEditor;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.util.Disposer;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;
import com.intellij.testFramework.fixtures.LightPlatformCodeInsightFixtureTestCase;
import com.intellij.util.Consumer;

import javax.swing.*;

import static com.intellij.testFramework.PlatformTestUtil.assertTreeEqual;

public class HbStructureViewTest extends LightPlatformCodeInsightFixtureTestCase {

  private static final String ourTestFileName = "test.hbs";

  private void doStructureViewTest(final String fileText, final String expectedTree) {
    myFixture.configureByText(ourTestFileName, fileText);

    testStructureView(myFixture.getFile(), new Consumer<StructureViewComposite>() {
      @Override
      public void consume(StructureViewComposite component) {
        JTree tree = ((StructureViewComponent) component.getSelectedStructureView()).getTree();

        // expand the whole tree
        int rowCount = tree.getRowCount();
        for (int i = 0; i <= rowCount; i++) {
          tree.expandRow(i);
        }

        assertTreeEqual(tree, expectedTree + "\n");
      }
    });
  }

  public void testStructureView(PsiFile file, Consumer<StructureViewComposite> consumer) {
    final VirtualFile vFile = file.getVirtualFile();
    final FileEditor fileEditor = FileEditorManager.getInstance(getProject()).getSelectedEditor(vFile);
    final StructureViewBuilder builder = LanguageStructureViewBuilder.INSTANCE.getStructureViewBuilder(file);
    assert builder != null;

    StructureViewComposite composite = null;
    try {
      composite = (StructureViewComposite) builder.createStructureView(fileEditor, file.getProject());
      consumer.consume(composite);
    }
    finally {
      if (composite != null) Disposer.dispose(composite);
    }
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
      " @data\n" +
      " \n" +
      " unescaped"
    );
  }
}
