// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.css;

import com.intellij.codeInspection.InspectionProfileEntry;
import com.intellij.psi.PsiFile;
import com.intellij.psi.css.inspections.CssUnknownPropertyInspection;
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

  public void testUnknownProperty() {
    doTest(new CssUnknownPropertyInspection(), () -> {
      myFixture.configureByFiles("unknownCssProperty.html", "package.json");
      myFixture.checkHighlighting();
    });
  }

  public void testPreprocessorIncludePaths() {
    doTest(new CssUnknownTargetInspection(), () -> {
      myFixture.addFileToProject(".angular-cli.json",
                                 """
                                   {"projects": {"foo": { "root": "foo"}, "sassy3": { "root": "",
                                         "architect": {"build": {"builder": "z", "options": {"outputPath": "dist/sassy3",
                                               "stylePreprocessorOptions": {"includePaths": ["src/assets/styles"]}}}}}}}
                                   """);
      myFixture.addFileToProject("src/assets/styles/moofoo.sass", "");
      myFixture.configureByText("main.scss", """
        @import "moofoo";
        @import "moofoo.sass";
        @import '<error>incorrect</error>';
        """);
      myFixture.checkHighlighting();
    });
  }

  public void testRelativeToAngularCliFolder() {
    doTest(new CssUnknownTargetInspection(), () -> {
      myFixture.addFileToProject("package.json", "");
      myFixture.addFileToProject(".angular-cli.json", "{\"projects\": {\"foo\": { \"root\": \"foo\"}}}\n");
      myFixture.addFileToProject("foo/bar/_inFooBar.scss", "");
      PsiFile file = myFixture.addFileToProject("foo/src/main.scss", """
        @import 'foo/bar/inFooBar';
        @import 'foo/bar/inFooBar.scss';
        @import 'foo/bar/_inFooBar';
        @import 'foo/bar/_inFooBar.scss';
        @import '<error>bar</error>/<error>_inFooBar.scss</error>';
        @import '<error>_inFooBar.scss</error>';
        """);
      myFixture.openFileInEditor(file.getVirtualFile());
      myFixture.checkHighlighting();
    });
  }

  public void testBaseURLPriority() {
    doTest(new CssUnknownTargetInspection(), () -> {
      myFixture.addFileToProject("package.json", "");
      myFixture.addFileToProject("tsconfig.json", "{\"compilerOptions\": {\"baseUrl\": \"./\"}}");
      myFixture.addFileToProject(".angular-cli.json",
                                 """
                                   {"projects": {"foo": { "root": "foo",
                                         "architect": {"build": {"builder": "z", "options": {"outputPath": "dist/sassy3",
                                               "tsConfig":"tsconfig.json","stylePreprocessorOptions": {"includePaths": ["foo/bar"]}}}}}}}
                                   """);
      myFixture.addFileToProject("baz/inBaz.scss", "");
      myFixture.addFileToProject("foo/bar/_inFooBar.scss", "");
      myFixture.addFileToProject("foo/foo/bar/_inFooFooBar.scss", "");
      PsiFile file = myFixture.addFileToProject("foo/src/main.scss", """
        @import 'inFooBar';
        @import '_inFooBar.scss';
        @import '<error>~bar</error>/<error>_inFooBar.scss</error>';
        @import '~baz/inBaz.scss';
        @import 'baz/inBaz';
        @import '<error>inBaz.scss</error>';
        @import '<error>inFooFooBar</error>';
        """);
      myFixture.openFileInEditor(file.getVirtualFile());
      myFixture.checkHighlighting();
    });
  }

  public void testIncludedPathsRelativeToCliFolder() {
    doTest(new CssUnknownTargetInspection(), () -> {
      myFixture.addFileToProject("package.json", "");
      myFixture.addFileToProject(".angular-cli.json",
                                 """
                                   {"projects": {"foo": { "root": "foo",
                                         "architect": {"build": {"builder": "z", "options": {"outputPath": "dist/sassy3",
                                               "stylePreprocessorOptions": {"includePaths": ["foo/bar"]}}}}}}}
                                   """);
      myFixture.addFileToProject("baz/inBaz.scss", "");
      myFixture.addFileToProject("foo/bar/_inFooBar.scss", "");
      myFixture.addFileToProject("foo/foo/bar/_inFooFooBar.scss", "");
      PsiFile file = myFixture.addFileToProject("foo/src/main.scss", """
        @import 'inFooBar';
        @import 'inFooBar.scss';
        @import '_inFooBar';
        @import '_inFooBar.scss';
        @import '~bar/_inFooBar.scss';
        @import '<error>~baz</error>/<error>inBaz.scss</error>';
        @import 'baz/inBaz';
        @import 'baz/inBaz.scss';
        @import '<error>inBaz.scss</error>';
        @import '<error>inFooFooBar</error>';
        @import '<error>inFooFooBar.scss</error>';
        @import '<error>_inFooFooBar</error>';
        @import '<error>_inFooFooBar.scss</error>';
        """);
      myFixture.openFileInEditor(file.getVirtualFile());
      myFixture.checkHighlighting();
    });
  }

  public void testLegacyPreprocessorIncludePaths() {
    doTest(new CssUnknownTargetInspection(), () -> {
      myFixture.addFileToProject(".angular-cli.json",
                                 """
                                   { "project": {"name": "scss-imports"},
                                             "apps": [
                                                { "root": "", "appRoot": "src", "assets": ["assets"]},
                                                { "stylePreprocessorOptions": {"includePaths": ["baz/qux"]}, "root": "foo/bar"}
                                   ]}""");
      myFixture.addFileToProject("src/sass/_var1.scss", "");
      myFixture.addFileToProject("foo/bar/baz/qux/_var2.scss", "");
      myFixture.addFileToProject("foo/bar/baz/qux/quux/_var3.scss", "");
      myFixture.configureByText("main.scss", """
        @import '<error>~sass</error>/<error>var1</error>';
        @import '<error>sass</error>/<error>var1</error>';
        @import '<error>~var1</error>';
        @import '~baz/qux/var2';
        @import '<error>~qux</error>/<error>var2</error>';
        @import 'quux/var3';
        @import '<error>~quux</error>/<error>var3</error>';
        """);
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
