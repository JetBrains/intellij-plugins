package com.intellij.lang.javascript.linter.eslint;

import com.intellij.javascript.nodejs.library.yarn.pnp.YarnPnpNodePackage;
import com.intellij.javascript.nodejs.util.NodePackage;
import com.intellij.javascript.nodejs.util.NodePackageRef;
import com.intellij.lang.javascript.JSTestUtils;
import com.intellij.lang.javascript.JavascriptLanguage;
import com.intellij.lang.javascript.buildTools.npm.PackageJsonUtil;
import com.intellij.lang.javascript.formatter.JSCodeStyleSettings;
import com.intellij.lang.javascript.linter.JSLinterUtil;
import com.intellij.lang.javascript.linter.eslint.importer.EslintImportCodeStyleAction;
import com.intellij.lang.javascript.modules.JSTempDirFixtureOptions;
import com.intellij.lang.javascript.nodejs.library.yarn.YarnPnpIntegrationFixture;
import com.intellij.notification.ActionCenter;
import com.intellij.notification.NotificationType;
import com.intellij.notification.impl.ApplicationNotificationsModel;
import com.intellij.notification.impl.StatusMessage;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.codeStyle.CommonCodeStyleSettings;
import com.intellij.testFramework.EdtTestUtil;
import com.intellij.testFramework.fixtures.CodeInsightTestFixture;
import com.intellij.testFramework.junit5.TestApplication;
import com.intellij.testFramework.junit5.fixture.TestFixture;
import com.intellij.util.ObjectUtils;
import com.intellij.util.PairConsumer;
import com.intellij.util.ThrowableRunnable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.Objects;

import static com.intellij.lang.javascript.linter.eslint.EslintServiceFixture.eslintServiceFixture;
import static com.intellij.psi.codeStyle.CommonCodeStyleSettings.WRAP_ALWAYS;

@TestApplication
public class ESLintImportCodeStyleIntegrationTest {
  private final TestFixture<EslintServiceFixture> eslint = eslintServiceFixture(
    JSTempDirFixtureOptions.forBasePath(EslintTestUtil.ESLINT_TEST_DATA_RELATIVE_PATH + "/linter/eslint/import/")
      .withMainPackage(EslintUtil.PACKAGE_NAME)
      .withGlobalPackages(Map.of("eslint", "8.57.0")),
    () -> new EslintInspection());

  @Test
  public void testCombineTwoConfigs() {
    doImportTestFromDirectory("subdir/.eslintrc.json", (common, custom) -> {
      Assertions.assertEquals(WRAP_ALWAYS, custom.OBJECT_LITERAL_WRAP);
      Assertions.assertEquals(WRAP_ALWAYS, common.ARRAY_INITIALIZER_WRAP);
    });
  }

  @Test
  public void testFromPackageJson() {
    doImportTestFromText("""
                           {
                             "eslintConfig": {
                               "rules": {
                                 "semi": ["error", "never"]
                               }
                             }
                           }""",
                         (settings, custom) -> Assertions.assertFalse(custom.USE_SEMICOLON_AFTER_STATEMENT),
                         PackageJsonUtil.FILE_NAME);
  }

  @Test
  public void testImportFromJsFile() {
    doImportTestFromText("""
                           module.exports = {
                             rules: {
                               semi: [
                                 "error",
                                 "never"
                               ]
                             }
                           };
                           """,
                         (settings, custom) -> Assertions.assertFalse(custom.USE_SEMICOLON_AFTER_STATEMENT),
                         EslintUtil.DEFAULT_CONFIG_PREFIX + ".js");
  }


  @Test
  public void testImportFileWithError() {
    CodeInsightTestFixture myFixture = eslint.get().getFixture();
    myFixture.configureByText(EslintUtil.DEFAULT_CONFIG_PREFIX + ".json", "{ \"not complete\"");

    // this instantiates EventLog.ProjectTracker, otherwise notification, on which this test relies, is not processed at all.
    ActionCenter.expireNotifications(myFixture.getProject());

    // The action needs the EDT.
    EdtTestUtil.runInEdtAndWait(() -> myFixture.testAction(new EslintImportCodeStyleAction()));

    final @Nullable StatusMessage statusMessage = ApplicationNotificationsModel.getStatusMessage(myFixture.getProject());
    Assertions.assertNotNull(statusMessage);
    Assertions.assertNotNull(statusMessage.notification());
    Assertions.assertEquals(NotificationType.ERROR, statusMessage.notification().getType());
    Assertions.assertEquals(JSLinterUtil.NOTIFICATION_GROUP.getDisplayId(), statusMessage.notification().getGroupId());
  }

  @Test
  public void testImportFromYamlFile() {
    String text = """
      ---
      rules:
        semi:
        - error
        - never
      """;
    doImportTestFromText(text,
                         (common, custom) -> Assertions.assertFalse(custom.USE_SEMICOLON_AFTER_STATEMENT),
                         EslintUtil.DEFAULT_CONFIG_PREFIX + ".yaml");
  }

  @Test
  public void testJsonFileWithExtends() {
    doTestJsonFileWithExtends(null);
  }

  private <T extends Throwable> void doTestJsonFileWithExtends(@Nullable ThrowableRunnable<T> onProjectCopiedRunnable) throws T {
    CodeInsightTestFixture myFixture = eslint.get().getFixture();
    myFixture.copyDirectoryToProject("jsonFileWithExtends", ".");
    myFixture.configureByFile(".eslintrc.json");
    if (onProjectCopiedRunnable != null) {
      onProjectCopiedRunnable.run();
    }
    performImport((common, custom) -> {
      Assertions.assertFalse(custom.USE_SEMICOLON_AFTER_STATEMENT);
      Assertions.assertEquals(7, common.getIndentOptions().INDENT_SIZE);
    });
  }

  @Test
  public void testJsonFileWithExtends_YarnPnp() throws Exception {
    EslintServiceFixture fixture = eslint.get();
    doTestJsonFileWithExtends(() -> {
      VirtualFile root = Objects.requireNonNull(fixture.getFixture().findFileInTempDir("."));
      NodePackage yarnPkg = YarnPnpIntegrationFixture.installYarnGlobally(fixture.getNodeJsAppRule());
      YarnPnpIntegrationFixture.configureYarnBerryAndRunYarnInstall(fixture.getProject(), yarnPkg, fixture.getNodeJsAppRule(), root);
      YarnPnpNodePackage yarnEslintPkg = YarnPnpNodePackage.create(fixture.getProject(),
                                                                   PackageJsonUtil.findChildPackageJsonFile(root),
                                                                   fixture.getPackageName(), false, false);
      Assertions.assertNotNull(yarnEslintPkg);
      fixture.configureLinterForPackage(NodePackageRef.create(yarnEslintPkg));
    });
  }

  private void doImportTestFromText(final @NotNull String text,
                                    final @NotNull PairConsumer<CommonCodeStyleSettings, JSCodeStyleSettings> styleChecker,
                                    @Nullable String fileName) {
    eslint.get().getFixture().configureByText(ObjectUtils.coalesce(fileName, ".eslintrc.json"), text);
    performImport(styleChecker);
  }

  private void doImportTestFromDirectory(String relativeFilePath,
                                         final @NotNull PairConsumer<CommonCodeStyleSettings, JSCodeStyleSettings> test) {
    EslintServiceFixture fixture = eslint.get();
    fixture.getFixture().copyDirectoryToProject(fixture.getTestName(true), "");
    fixture.getFixture().configureByFile(relativeFilePath);

    performImport(test);
  }

  private void performImport(@NotNull PairConsumer<CommonCodeStyleSettings, JSCodeStyleSettings> test) {
    CodeInsightTestFixture myFixture = eslint.get().getFixture();
    JSTestUtils.testWithTempCodeStyleSettings(myFixture.getProject(), (settings) -> {
      CommonCodeStyleSettings common = settings.getCommonSettings(JavascriptLanguage.INSTANCE);
      JSCodeStyleSettings custom = settings.getCustomSettings(JSCodeStyleSettings.class);
      // The action needs the EDT.
      EdtTestUtil.runInEdtAndWait(() -> myFixture.testAction(new EslintImportCodeStyleAction()));
      test.consume(common, custom);
    });
  }
}
