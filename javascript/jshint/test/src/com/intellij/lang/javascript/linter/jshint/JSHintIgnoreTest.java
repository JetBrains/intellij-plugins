package com.intellij.lang.javascript.linter.jshint;

import com.intellij.lang.javascript.linter.jshint.config.JSHintConfigFileUtil;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;
import com.intellij.testFramework.fixtures.BasePlatformTestCase;
import org.jetbrains.annotations.NotNull;

public class JSHintIgnoreTest extends BasePlatformTestCase {

  public void testTrailingDoubleAsterisk() {
    myFixture.addFileToProject(".jshintignore", "build/**");
    check("a.js", false);
    check("dir/a.js", false);
    check("build/a.js", true);
    check("build/sub/a.js", true);
  }

  public void testMisc() {
    String[] lines = {"node_modules", "dist/**/", "reports/**/*", "tests/*.spec.js", "concrete.js", "dir/**/my-*.js"};
    myFixture.addFileToProject(".jshintignore", StringUtil.join(lines, "\n"));

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
    PsiFile psiFile = myFixture.addFileToProject(path, "");
    VirtualFile vFile = psiFile.getVirtualFile();
    assert vFile != null;
    Project project = getProject();
    boolean ignoredActual = JSHintConfigFileUtil.isIgnored(project, vFile);
    assertEquals(ignoredExpected, ignoredActual);
  }
}
