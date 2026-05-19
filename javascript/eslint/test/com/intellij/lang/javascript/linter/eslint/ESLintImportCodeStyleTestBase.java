package com.intellij.lang.javascript.linter.eslint;

import com.intellij.lang.javascript.JSTestUtils;
import com.intellij.lang.javascript.linter.eslint.EslintUtil;
import com.intellij.lang.javascript.linter.eslint.importer.EslintConfigWrapper;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.psi.PsiFile;
import com.intellij.psi.codeStyle.CodeStyleSettings;
import com.intellij.testFramework.fixtures.CodeInsightFixtureTestCase;
import com.intellij.util.ObjectUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.junit.Assert;

import java.io.IOException;
import java.util.Objects;
import java.util.function.Consumer;

public abstract class ESLintImportCodeStyleTestBase extends CodeInsightFixtureTestCase {

  protected void doImportTest(final @NotNull String text,
                              final @Nullable Consumer<CodeStyleSettings> stylePreset,
                              final @NotNull Consumer<CodeStyleSettings> styleChecker) {
    doImportTest(text, stylePreset, styleChecker, null);
  }


  protected void doImportTest(final @NotNull String text,
                              final @Nullable Consumer<CodeStyleSettings> stylePreset,
                              final @NotNull Consumer<CodeStyleSettings> styleChecker,
                              @Nullable String fileName) {
    JSTestUtils.testWithTempCodeStyleSettings(getProject(), (settings) -> {
      if (stylePreset != null) {
        stylePreset.accept(settings);
      }
      performImport(text, fileName);
      styleChecker.accept(settings);
    });
  }

  protected void doTestNoDataToImport(final @NotNull String text, @Nullable String fileName) {
    final PsiFile configPsi = myFixture.configureByText(ObjectUtils.coalesce(fileName, EslintUtil.DEFAULT_CONFIG_PREFIX + ".json"), text);
    final EslintConfigWrapper importer = EslintConfigWrapper.getForFile(configPsi);
    assertNotNull(importer);
    final boolean actualHasData = importer.hasDataToImport(getProject());
    Assert.assertFalse("Expected hasDataToImport to return 'false'", actualHasData);
  }

  protected void performImport(final @NotNull String text, @Nullable String fileName) {
    final String name = ObjectUtils.coalesce(fileName, EslintUtil.DEFAULT_CONFIG_PREFIX + ".json");
    final PsiFile configPsi = myFixture.configureByText(name, text);
    final EslintConfigWrapper importer = EslintConfigWrapper.getForFile(configPsi);
    assertNotNull(importer);
    final boolean actualHasData = importer.hasDataToImport(getProject());
    Assert.assertTrue("Expected hasDataToImport to return 'true' before import", actualHasData);
    importer.modifySettings(getProject());
    Assert.assertFalse("Expected hasDataToImport to return 'false' after import", importer.hasDataToImport(getProject()));
    WriteCommandAction.runWriteCommandAction(
      myFixture.getProject(), () -> {
        try {
          Objects.requireNonNull(myFixture.getTempDirFixture().getFile(name)).delete(null);
        }
        catch (IOException e) {
          throw new RuntimeException(e);
        }
      });
  }
}
