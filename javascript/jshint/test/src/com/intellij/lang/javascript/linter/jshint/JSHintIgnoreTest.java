package com.intellij.lang.javascript.linter.jshint;

import com.intellij.lang.javascript.linter.jshint.config.JSHintConfigFileUtil;
import com.intellij.openapi.application.ReadAction;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;
import com.intellij.testFramework.fixtures.CodeInsightTestFixture;
import com.intellij.testFramework.junit5.TestApplication;
import com.intellij.testFramework.junit5.fixture.TestFixture;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;

import static com.intellij.platform.testFramework.junit5.codeInsight.fixture.CodeInsightFixtureKt.sharedCodeInsightFixture;
import static com.intellij.platform.testFramework.junit5.codeInsight.fixture.CodeInsightFixtureKt.sharedCodeInsightProjectFixture;
import static org.junit.jupiter.api.Assertions.assertEquals;

@TestApplication
public class JSHintIgnoreTest {
  private static final TestFixture<Project> projectFixture = sharedCodeInsightProjectFixture();
  private final TestFixture<CodeInsightTestFixture> codeInsightFixture = sharedCodeInsightFixture(projectFixture);

  @Test
  public void testTrailingDoubleAsterisk() {
    codeInsightFixture.get().addFileToProject(".jshintignore", "build/**");
    check("a.js", false);
    check("dir/a.js", false);
    check("build/a.js", true);
    check("build/sub/a.js", true);
  }

  @Test
  public void testMisc() {
    String[] lines = {"node_modules", "dist/**/", "reports/**/*", "tests/*.spec.js", "concrete.js", "dir/**/my-*.js"};
    codeInsightFixture.get().addFileToProject(".jshintignore", StringUtil.join(lines, "\n"));

    check("node_modules/a.js", true);
    check("node_modules/express/index.js", true);

    check("dist/a.js", true);
    check("dist/temp/a.js", true);

    check("reports/a.js", true);
    check("reports/temp/a.js", true);

    check("tests/a.js", false);
    check("tests/a.spec.js", true);
    check("tests/t/a.spec.js", false);

    check("concrete.js", true);
    check("concrete2.js", false);

    check("dir/my.js", false);
    check("dir/my-.js", true);
    check("dir/t/p/my-a.js", true);
    check("dir/t/p/my-a.json", false);
  }

  private void check(@NotNull String path, boolean ignoredExpected) {
    CodeInsightTestFixture myFixture = codeInsightFixture.get();
    PsiFile psiFile = myFixture.addFileToProject(path, "");
    VirtualFile vFile = psiFile.getVirtualFile();
    assert vFile != null;
    Project project = myFixture.getProject();
    boolean ignoredActual = ReadAction.computeBlocking(() -> JSHintConfigFileUtil.isIgnored(project, vFile));
    assertEquals(ignoredExpected, ignoredActual);
  }
}
