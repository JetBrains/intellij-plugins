package com.intellij.lang.javascript.linter.eslint;

import com.intellij.codeInspection.InspectionProfileEntry;
import com.intellij.javascript.nodejs.library.yarn.pnp.YarnPnpNodePackage;
import com.intellij.javascript.nodejs.util.NodePackage;
import com.intellij.javascript.nodejs.util.NodePackageRef;
import com.intellij.lang.javascript.buildTools.npm.PackageJsonUtil;
import com.intellij.lang.javascript.nodejs.library.yarn.AbstractYarnPnpIntegrationTest;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.newvfs.impl.VfsRootAccess;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.Objects;

/**
 * Yarn PnP integration: ESLint resolved through a Yarn Berry Plug'n'Play install, which uses a
 * {@code .pnp.cjs} resolver and no {@code node_modules}. The PackageLock (npm {@code package-lock.json})
 * test pattern cannot represent that, so this stays a live yarn-install integration test rather than
 * moving to the {@code stable}/{@code next} tiers. The linted package pins {@code eslint 8.57.0}, so
 * it is not a floating-version risk.
 */
public class EslintYarnPnpTest extends EslintServiceTestBase {
  @Override
  protected String getBasePath() {
    return EslintTestUtil.ESLINT_TEST_DATA_RELATIVE_PATH + "/linter/eslint/highlighting/";
  }

  @Override
  protected @NotNull InspectionProfileEntry getInspection() {
    return new EslintInspection();
  }

  @Override
  protected @NotNull String getPackageName() {
    return "eslint";
  }

  @Override
  protected @NotNull Map<String, String> getGlobalPackageVersionsToInstall() {
    return Map.of("eslint", "8.57.0");
  }

  @Override
  protected @Nullable String getAnnotationText() {
    return EslintTestUtil.getEslintFileLevelAnnotationText(getProject(), myFixture.getFile().getVirtualFile());
  }

  public void testYarnPnpEslintExample() throws Exception {
    doEditorHighlightingTest("app.js", () -> {
      VirtualFile root = Objects.requireNonNull(myFixture.findFileInTempDir("."));
      NodePackage yarnPkg = AbstractYarnPnpIntegrationTest.installYarnGlobally(getNodeJsAppRule());
      VfsRootAccess.allowRootAccess(getTestRootDisposable(), yarnPkg.getSystemIndependentPath());
      AbstractYarnPnpIntegrationTest.configureYarnBerryAndRunYarnInstall(getProject(), yarnPkg, getNodeJsAppRule(), root);
      YarnPnpNodePackage yarnEslintPkg = YarnPnpNodePackage.create(getProject(),
                                                                   PackageJsonUtil.findChildPackageJsonFile(root),
                                                                   getPackageName(), false, false);
      assertNotNull(yarnEslintPkg);
      configureLinterForPackage(NodePackageRef.create(yarnEslintPkg));
    });
  }
}
