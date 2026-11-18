package com.intellij.lang.javascript.linter.eslint.unit;

import com.intellij.lang.javascript.JSTestUtils;
import com.intellij.lang.javascript.linter.eslint.EslintUtil;
import com.intellij.lang.javascript.linter.eslint.importer.EslintConfigWrapper;
import com.intellij.openapi.application.ReadAction;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.psi.PsiFile;
import com.intellij.psi.codeStyle.CodeStyleSettings;
import com.intellij.testFramework.fixtures.CodeInsightTestFixture;
import com.intellij.util.ObjectUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.Objects;
import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public final class ESLintImportCodeStyleTestUtil {
  private ESLintImportCodeStyleTestUtil() {
  }

  public static void doImportTest(@NotNull CodeInsightTestFixture fixture,
                                  final @NotNull String text,
                                  final @Nullable Consumer<CodeStyleSettings> stylePreset,
                                  final @NotNull Consumer<CodeStyleSettings> styleChecker) {
    doImportTest(fixture, text, stylePreset, styleChecker, null);
  }

  public static void doImportTest(@NotNull CodeInsightTestFixture fixture,
                                  final @NotNull String text,
                                  final @Nullable Consumer<CodeStyleSettings> stylePreset,
                                  final @NotNull Consumer<CodeStyleSettings> styleChecker,
                                  @Nullable String fileName) {
    JSTestUtils.testWithTempCodeStyleSettings(fixture.getProject(), (settings) -> {
      if (stylePreset != null) {
        stylePreset.accept(settings);
      }
      performImport(fixture, text, fileName);
      styleChecker.accept(settings);
    });
  }

  public static void doTestNoDataToImport(@NotNull CodeInsightTestFixture fixture, final @NotNull String text, @Nullable String fileName) {
    final PsiFile configPsi = fixture.configureByText(ObjectUtils.coalesce(fileName, EslintUtil.DEFAULT_CONFIG_PREFIX + ".json"), text);
    final EslintConfigWrapper importer = ReadAction.computeBlocking(() -> EslintConfigWrapper.getForFile(configPsi));
    assertNotNull(importer);
    final boolean actualHasData = ReadAction.computeBlocking(() -> importer.hasDataToImport(fixture.getProject()));
    assertFalse(actualHasData, "Expected hasDataToImport to return 'false'");
  }

  public static void performImport(@NotNull CodeInsightTestFixture fixture, final @NotNull String text, @Nullable String fileName) {
    final String name = ObjectUtils.coalesce(fileName, EslintUtil.DEFAULT_CONFIG_PREFIX + ".json");
    final PsiFile configPsi = fixture.configureByText(name, text);
    final EslintConfigWrapper importer = ReadAction.computeBlocking(() -> EslintConfigWrapper.getForFile(configPsi));
    assertNotNull(importer);
    final boolean actualHasData = ReadAction.computeBlocking(() -> importer.hasDataToImport(fixture.getProject()));
    assertTrue(actualHasData, "Expected hasDataToImport to return 'true' before import");
    importer.modifySettings(fixture.getProject());
    assertFalse(ReadAction.computeBlocking(() -> importer.hasDataToImport(fixture.getProject())),
                "Expected hasDataToImport to return 'false' after import");
    WriteCommandAction.runWriteCommandAction(
      fixture.getProject(), () -> {
        try {
          Objects.requireNonNull(fixture.getTempDirFixture().getFile(name)).delete(null);
        }
        catch (IOException e) {
          throw new RuntimeException(e);
        }
      });
  }
}
