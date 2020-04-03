// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.css;

import com.intellij.codeInspection.InspectionProfileEntry;
import com.intellij.psi.PsiFile;
import com.intellij.psi.css.inspections.CssUnusedSymbolInspection;
import com.intellij.psi.css.inspections.invalid.CssUnknownTargetInspection;
import org.angular2.Angular2CodeInsightFixtureTestCase;
import org.angularjs.AngularTestUtil;

public class CssInspectionsTest extends Angular2CodeInsightFixtureTestCase {

  @Override
  protected String getTestDataPath() {
    return AngularTestUtil.getBaseTestDataPath(getClass()) + "inspections";
  }

  public void testLocalStylesheet() {
    doTest(new CssUnusedSymbolInspection(), () -> {
      myFixture.configureByFiles("local-stylesheet.ts", "package.json");
      myFixture.checkHighlighting();
    });
  }

  public void testLocalStylesheetExtUsage() {
    doTest(new CssUnusedSymbolInspection(), () -> {
      myFixture.configureByFiles("local-stylesheet-ext.ts", "local-stylesheet-ext.html", "local-stylesheet-ext.css", "package.json");
      myFixture.checkHighlighting();
    });
  }

  public void testPreprocessorIncludePaths() {
    doTest(new CssUnknownTargetInspection(), () -> {
      myFixture.addFileToProject(".angular-cli.json",
                                 "{\"projects\": {\"foo\": { \"root\": \"foo\"}, \"sassy3\": { \"root\": \"\",\n" +
                                 "      \"architect\": {\"build\": {\"builder\": \"z\", \"options\": {\"outputPath\": \"dist/sassy3\",\n" +
                                 "            \"stylePreprocessorOptions\": {\"includePaths\": [\"src/assets/styles\"]}}}}}}}\n");
      myFixture.addFileToProject("src/assets/styles/moofoo.sass", "");
      myFixture.configureByText("main.scss", "@import \"moofoo\";\n" +
                                             "@import \"moofoo.sass\";\n" +
                                             "@import '<error>incorrect</error>';\n");
      myFixture.checkHighlighting();
    });
  }

  public void testRelativeToAngularCliFolder() {
    doTest(new CssUnknownTargetInspection(), () -> {
      myFixture.addFileToProject("package.json", "");
      myFixture.addFileToProject(".angular-cli.json", "{\"projects\": {\"foo\": { \"root\": \"foo\"}}}\n");
      myFixture.addFileToProject("foo/bar/_inFooBar.scss", "");
      PsiFile file = myFixture.addFileToProject("foo/src/main.scss", "@import 'foo/bar/inFooBar';\n" +
                                                                     "@import 'foo/bar/inFooBar.scss';\n" +
                                                                     "@import 'foo/bar/_inFooBar';\n" +
                                                                     "@import 'foo/bar/_inFooBar.scss';\n" +
                                                                     "@import '<error>bar</error>/<error>_inFooBar.scss</error>';\n" +
                                                                     "@import '<error>_inFooBar.scss</error>';\n");
      myFixture.openFileInEditor(file.getVirtualFile());
      myFixture.checkHighlighting();
    });
  }

  public void testBaseURLPriority() {
    doTest(new CssUnknownTargetInspection(), () -> {
      myFixture.addFileToProject("package.json", "");
      myFixture.addFileToProject("tsconfig.json", "{\"compilerOptions\": {\"baseUrl\": \"./\"}}");
      myFixture.addFileToProject(".angular-cli.json",
                                 "{\"projects\": {\"foo\": { \"root\": \"foo\",\n" +
                                 "      \"architect\": {\"build\": {\"builder\": \"z\", \"options\": {\"outputPath\": \"dist/sassy3\",\n" +
                                 "            \"tsConfig\":\"tsconfig.json\",\"stylePreprocessorOptions\": {\"includePaths\": [\"foo/bar\"]}}}}}}}\n");
      myFixture.addFileToProject("baz/inBaz.scss", "");
      myFixture.addFileToProject("foo/bar/_inFooBar.scss", "");
      myFixture.addFileToProject("foo/foo/bar/_inFooFooBar.scss", "");
      PsiFile file = myFixture.addFileToProject("foo/src/main.scss", "@import 'inFooBar';\n" +
                                                                     "@import '_inFooBar.scss';\n" +
                                                                     "@import '<error>~bar</error>/<error>_inFooBar.scss</error>';\n" +
                                                                     "@import '~baz/inBaz.scss';\n" +
                                                                     "@import 'baz/inBaz';\n" +
                                                                     "@import '<error>inBaz.scss</error>';\n" +
                                                                     "@import '<error>inFooFooBar</error>';\n");
      myFixture.openFileInEditor(file.getVirtualFile());
      myFixture.checkHighlighting();
    });
  }

  public void testIncludedPathsRelativeToCliFolder() {
    doTest(new CssUnknownTargetInspection(), () -> {
      myFixture.addFileToProject("package.json", "");
      myFixture.addFileToProject(".angular-cli.json",
                                 "{\"projects\": {\"foo\": { \"root\": \"foo\",\n" +
                                 "      \"architect\": {\"build\": {\"builder\": \"z\", \"options\": {\"outputPath\": \"dist/sassy3\",\n" +
                                 "            \"stylePreprocessorOptions\": {\"includePaths\": [\"foo/bar\"]}}}}}}}\n");
      myFixture.addFileToProject("baz/inBaz.scss", "");
      myFixture.addFileToProject("foo/bar/_inFooBar.scss", "");
      myFixture.addFileToProject("foo/foo/bar/_inFooFooBar.scss", "");
      PsiFile file = myFixture.addFileToProject("foo/src/main.scss", "@import 'inFooBar';\n" +
                                                                     "@import 'inFooBar.scss';\n" +
                                                                     "@import '_inFooBar';\n" +
                                                                     "@import '_inFooBar.scss';\n" +
                                                                     "@import '~bar/_inFooBar.scss';\n" +
                                                                     "@import '<error>~baz</error>/<error>inBaz.scss</error>';\n" +
                                                                     "@import 'baz/inBaz';\n" +
                                                                     "@import 'baz/inBaz.scss';\n" +
                                                                     "@import '<error>inBaz.scss</error>';\n" +
                                                                     "@import '<error>inFooFooBar</error>';\n" +
                                                                     "@import '<error>inFooFooBar.scss</error>';\n" +
                                                                     "@import '<error>_inFooFooBar</error>';\n" +
                                                                     "@import '<error>_inFooFooBar.scss</error>';\n");
      myFixture.openFileInEditor(file.getVirtualFile());
      myFixture.checkHighlighting();
    });
  }

  public void testLegacyPreprocessorIncludePaths() {
    doTest(new CssUnknownTargetInspection(), () -> {
      myFixture.addFileToProject(".angular-cli.json",
                                 "{ \"project\": {\"name\": \"scss-imports\"},\n" +
                                 "          \"apps\": [\n" +
                                 "             { \"root\": \"\", \"appRoot\": \"src\", \"assets\": [\"assets\"]},\n" +
                                 "             { \"stylePreprocessorOptions\": {\"includePaths\": [\"baz/qux\"]}, \"root\": \"foo/bar\"}\n" +
                                 "]}");
      myFixture.addFileToProject("src/sass/_var1.scss", "");
      myFixture.addFileToProject("foo/bar/baz/qux/_var2.scss", "");
      myFixture.addFileToProject("foo/bar/baz/qux/quux/_var3.scss", "");
      myFixture.configureByText("main.scss", "@import '<error>~sass</error>/<error>var1</error>';\n" +
                                             "@import '<error>sass</error>/<error>var1</error>';\n" +
                                             "@import '<error>~var1</error>';\n" +
                                             "@import '~baz/qux/var2';\n" +
                                             "@import '<error>~qux</error>/<error>var2</error>';\n" +
                                             "@import 'quux/var3';\n" +
                                             "@import '<error>~quux</error>/<error>var3</error>';\n" +
                                             "");
      myFixture.checkHighlighting();
    });
  }

  private void doTest(InspectionProfileEntry inspection, Runnable testRunnable) {
    doTest(new InspectionProfileEntry[]{inspection}, testRunnable);
  }

  private void doTest(InspectionProfileEntry[] inspections, Runnable testRunnable) {
    myFixture.enableInspections(inspections);
    try {
      testRunnable.run();
    }
    finally {
      try {
        myFixture.disableInspections(inspections);
      }
      catch (Exception e) {
        addSuppressedException(e);
      }
    }
  }
}
