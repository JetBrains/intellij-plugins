package com.dmarcotte.handlebars.structure;

import com.intellij.ide.structureView.StructureViewBuilder;
import com.intellij.ide.structureView.impl.StructureViewComposite;
import com.intellij.ide.structureView.newStructureView.StructureViewComponent;
import com.intellij.lang.LanguageStructureViewBuilder;
import com.intellij.openapi.fileEditor.FileEditor;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Disposer;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;
import com.intellij.testFramework.EdtTestUtil;
import com.intellij.testFramework.PlatformTestUtil;
import com.intellij.testFramework.fixtures.CodeInsightTestFixture;
import com.intellij.testFramework.junit5.TestApplication;
import com.intellij.testFramework.junit5.fixture.TestFixture;
import com.intellij.util.Consumer;
import org.junit.jupiter.api.Test;

import static com.intellij.platform.testFramework.junit5.codeInsight.fixture.CodeInsightFixtureKt.sharedCodeInsightFixture;
import static com.intellij.platform.testFramework.junit5.codeInsight.fixture.CodeInsightFixtureKt.sharedCodeInsightProjectFixture;
import static com.intellij.testFramework.PlatformTestUtil.assertTreeEqual;

@TestApplication
public class HbStructureViewTest {
  private static final TestFixture<Project> projectFixture = sharedCodeInsightProjectFixture();
  private final TestFixture<CodeInsightTestFixture> fixture = sharedCodeInsightFixture(projectFixture);

  private static final String ourTestFileName = "test.hbs";

  private void doStructureViewTest(String fileText, String expectedTree) {
    CodeInsightTestFixture myFixture = fixture.get();
    myFixture.configureByText(ourTestFileName, fileText);

    EdtTestUtil.runInEdtAndWait(() -> doTestStructureView(myFixture.getFile(), composite -> {
      StructureViewComponent svc = (StructureViewComponent)composite.getSelectedStructureView();
      PlatformTestUtil.waitForPromise(svc.rebuildAndUpdate());
      PlatformTestUtil.expandAll(svc.getTree());
      assertTreeEqual(svc.getTree(), expectedTree + "\n");
    }));
  }

  private void doTestStructureView(PsiFile file, Consumer<StructureViewComposite> consumer) {
    VirtualFile vFile = file.getVirtualFile();
    FileEditor fileEditor = FileEditorManager.getInstance(file.getProject()).getSelectedEditor(vFile);
    StructureViewBuilder builder = LanguageStructureViewBuilder.getInstance().getStructureViewBuilder(file);
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

  @Test
  public void testNestedBlocks() {
    doStructureViewTest(

      """
        {{#foo}}
            {{#bar}}
                {{baz}}<caret>
            {{/bar}}
        {{/foo}}
        """,

      "-" + ourTestFileName + "\n" +
      " -foo\n" +
      "  -bar\n" +
      "   -baz"
    );
  }

  @Test
  public void testUnclosedBlocks() {
    doStructureViewTest(

      "{{#foo}}\n" +
      "{{^bar}}",

      "-" + ourTestFileName + "\n" +
      " -foo\n" +
      "  -bar"
    );
  }

  @Test
  public void testAllConstructs() {
    doStructureViewTest(

      """
        {{#block}}
        {{/block}}
        {{^inverse}}
            {{else}}
        {{/inverse}}
        {{mustache}}
        {{>partial}}
        {{#>partialBlock}}
        {{/partialBlock}}
        {{@data}}
        {{^}}
        {{{unescaped}}
        """,

      "-" + ourTestFileName + "\n" +
      " -block\n" +
      " -inverse\n" +
      "  -else\n" +
      " -mustache\n" +
      " -partial\n" +
      " -partialBlock\n" +
      " -@data\n" +
      " -unescaped"
    );
  }
}
