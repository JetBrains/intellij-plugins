package com.intellij.lang.javascript.linter.eslint;

import com.intellij.codeInspection.InspectionProfileEntry;
import com.intellij.javascript.nodejs.library.yarn.pnp.YarnPnpNodePackage;
import com.intellij.javascript.nodejs.util.NodePackage;
import com.intellij.javascript.nodejs.util.NodePackageRef;
import com.intellij.lang.javascript.JSTestUtils;
import com.intellij.lang.javascript.JavascriptLanguage;
import com.intellij.lang.javascript.buildTools.npm.PackageJsonUtil;
import com.intellij.lang.javascript.formatter.JSCodeStyleSettings;
import com.intellij.lang.javascript.linter.JSLinterUtil;
import com.intellij.lang.javascript.linter.eslint.importer.EslintImportCodeStyleAction;
import com.intellij.lang.javascript.nodejs.library.yarn.AbstractYarnPnpIntegrationTest;
import com.intellij.notification.ActionCenter;
import com.intellij.notification.NotificationType;
import com.intellij.notification.impl.ApplicationNotificationsModel;
import com.intellij.notification.impl.StatusMessage;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.codeStyle.CommonCodeStyleSettings;
import com.intellij.util.ObjectUtils;
import com.intellij.util.PairConsumer;
import com.intellij.util.ThrowableRunnable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.junit.Assert;

import java.util.Objects;

import static com.intellij.psi.codeStyle.CommonCodeStyleSettings.WRAP_ALWAYS;

public class ESLintImportCodeStyleIntegrationTest extends EslintServiceTestBase {

  @Override
  protected String getBasePath() {
    return EslintTestUtil.ESLINT_TEST_DATA_RELATIVE_PATH + "/linter/eslint/import/";
  }

  public void testCombineTwoConfigs() {
    doImportTestFromDirectory("subdir/.eslintrc.json", (common, custom) -> {
      Assert.assertEquals(WRAP_ALWAYS, custom.OBJECT_LITERAL_WRAP);
      Assert.assertEquals(WRAP_ALWAYS, common.ARRAY_INITIALIZER_WRAP);
    });
  }

  public void testFromPackageJson() {
    doImportTestFromText("""
                           {
                             "eslintConfig": {
                               "rules": {
                                 "semi": ["error", "never"]
                               }
                             }
                           }""",
                         (settings, custom) -> Assert.assertFalse(custom.USE_SEMICOLON_AFTER_STATEMENT),
                         PackageJsonUtil.FILE_NAME);
  }

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
                         (settings, custom) -> Assert.assertFalse(custom.USE_SEMICOLON_AFTER_STATEMENT),
                         EslintUtil.DEFAULT_CONFIG_PREFIX + ".js");
  }


  public void testImportFileWithError() {
    myFixture.configureByText(EslintUtil.DEFAULT_CONFIG_PREFIX + ".json", "{ \"not complete\"");

    // this instantiates EventLog.ProjectTracker, otherwise notification, on which this test relies, is not processed at all.
    ActionCenter.expireNotifications(getProject());

    myFixture.testAction(new EslintImportCodeStyleAction());

    final @Nullable StatusMessage statusMessage = ApplicationNotificationsModel.getStatusMessage(getProject());
    Assert.assertNotNull(statusMessage);
    Assert.assertNotNull(statusMessage.notification());
    Assert.assertEquals(NotificationType.ERROR, statusMessage.notification().getType());
    Assert.assertEquals(JSLinterUtil.NOTIFICATION_GROUP.getDisplayId(), statusMessage.notification().getGroupId());
  }

  public void testImportFromYamlFile() {
    String text = """
      ---
      rules:
        semi:
        - error
        - never
      """;
    doImportTestFromText(text,
                         (common, custom) -> Assert.assertFalse(custom.USE_SEMICOLON_AFTER_STATEMENT),
                         EslintUtil.DEFAULT_CONFIG_PREFIX + ".yaml");
  }

  public void testJsonFileWithExtends() {
    doTestJsonFileWithExtends(null);
  }

  private <T extends Throwable> void doTestJsonFileWithExtends(@Nullable ThrowableRunnable<T> onProjectCopiedRunnable) throws T {
    myFixture.copyDirectoryToProject("jsonFileWithExtends", ".");
    myFixture.configureByFile(".eslintrc.json");
    if (onProjectCopiedRunnable != null) {
      onProjectCopiedRunnable.run();
    }
    performImport((common, custom) -> {
      Assert.assertFalse(custom.USE_SEMICOLON_AFTER_STATEMENT);
      Assert.assertEquals(7, common.getIndentOptions().INDENT_SIZE);
    });
  }

  public void testJsonFileWithExtends_YarnPnp() throws Exception {
    doTestJsonFileWithExtends(() -> {
      VirtualFile root = Objects.requireNonNull(myFixture.findFileInTempDir("."));
      NodePackage yarnPkg = AbstractYarnPnpIntegrationTest.installYarnGlobally(getNodeJsAppRule());
      AbstractYarnPnpIntegrationTest.configureYarnBerryAndRunYarnInstall(getProject(), yarnPkg, getNodeJsAppRule(), root);
      YarnPnpNodePackage yarnEslintPkg = YarnPnpNodePackage.create(getProject(),
                                                                   PackageJsonUtil.findChildPackageJsonFile(root),
                                                                   getPackageName(), false, false);
      assertNotNull(yarnEslintPkg);
      configureLinterForPackage(NodePackageRef.create(yarnEslintPkg));
    });
  }

  private void doImportTestFromText(final @NotNull String text,
                                    final @NotNull PairConsumer<CommonCodeStyleSettings, JSCodeStyleSettings> styleChecker,
                                    @Nullable String fileName) {
    myFixture.configureByText(ObjectUtils.coalesce(fileName, ".eslintrc.json"), text);
    performImport(styleChecker);
  }

  private void doImportTestFromDirectory(String relativeFilePath,
                                         final @NotNull PairConsumer<CommonCodeStyleSettings, JSCodeStyleSettings> test) {
    myFixture.copyDirectoryToProject(getTestName(true), "");
    myFixture.configureByFile(relativeFilePath);

    performImport(test);
  }

  private void performImport(@NotNull PairConsumer<CommonCodeStyleSettings, JSCodeStyleSettings> test) {
    JSTestUtils.testWithTempCodeStyleSettings(getProject(), (settings) -> {
      CommonCodeStyleSettings common = settings.getCommonSettings(JavascriptLanguage.INSTANCE);
      JSCodeStyleSettings custom = settings.getCustomSettings(JSCodeStyleSettings.class);
      myFixture.testAction(new EslintImportCodeStyleAction());
      test.consume(common, custom);
    });
  }

  @Override
  protected @NotNull InspectionProfileEntry getInspection() {
    return new EslintInspection();
  }

  @Override
  protected @NotNull String getPackageName() {
    return EslintUtil.PACKAGE_NAME;
  }
}
