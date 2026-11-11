package com.intellij.lang.javascript.linter.eslint;

import com.intellij.javascript.nodejs.library.yarn.pnp.YarnPnpNodePackage;
import com.intellij.javascript.nodejs.util.NodePackage;
import com.intellij.javascript.nodejs.util.NodePackageRef;
import com.intellij.lang.javascript.buildTools.npm.PackageJsonUtil;
import com.intellij.lang.javascript.modules.JSTempDirFixtureOptions;
import com.intellij.lang.javascript.nodejs.library.yarn.YarnPnpIntegrationFixture;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.newvfs.impl.VfsRootAccess;
import com.intellij.testFramework.junit5.TestApplication;
import com.intellij.testFramework.junit5.fixture.TestFixture;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.Objects;

import static com.intellij.lang.javascript.linter.eslint.EslintServiceFixture.eslintServiceFixture;

/**
 * Yarn PnP integration: ESLint resolved through a Yarn Berry Plug'n'Play install, which uses a
 * {@code .pnp.cjs} resolver and no {@code node_modules}. The PackageLock (npm {@code package-lock.json})
 * test pattern cannot represent that, so this stays a live yarn-install integration test rather than
 * moving to the {@code stable}/{@code next} tiers. The linted package pins {@code eslint 8.57.0}, so
 * it is not a floating-version risk.
 */
@TestApplication
public class EslintYarnPnpTest {
  private final TestFixture<EslintServiceFixture> eslint = eslintServiceFixture(
    JSTempDirFixtureOptions.forBasePath(EslintTestUtil.ESLINT_TEST_DATA_RELATIVE_PATH + "/linter/eslint/highlighting/")
      .withMainPackage("eslint")
      .withGlobalPackages(Map.of("eslint", "8.57.0")),
    () -> new EslintInspection(),
    true);

  @Test
  public void testYarnPnpEslintExample() {
    EslintServiceFixture fixture = eslint.get();
    fixture.doEditorHighlightingTest("app.js", () -> {
      VirtualFile root = Objects.requireNonNull(fixture.getFixture().findFileInTempDir("."));
      NodePackage yarnPkg = YarnPnpIntegrationFixture.installYarnGlobally(fixture.getNodeJsAppRule());
      VfsRootAccess.allowRootAccess(fixture.getTestRootDisposable(), yarnPkg.getSystemIndependentPath());
      YarnPnpIntegrationFixture.configureYarnBerryAndRunYarnInstall(fixture.getProject(), yarnPkg, fixture.getNodeJsAppRule(), root);
      YarnPnpNodePackage yarnEslintPkg = YarnPnpNodePackage.create(fixture.getProject(),
                                                                   PackageJsonUtil.findChildPackageJsonFile(root),
                                                                   fixture.getPackageName(), false, false);
      Assertions.assertNotNull(yarnEslintPkg);
      fixture.configureLinterForPackage(NodePackageRef.create(yarnEslintPkg));
    });
  }
}
