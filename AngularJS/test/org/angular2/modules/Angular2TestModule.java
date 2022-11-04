// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.modules;

import com.intellij.lang.javascript.JSTestUtils;
import com.intellij.lang.resharper.ReSharperTestUtil;
import com.intellij.openapi.application.WriteAction;
import com.intellij.openapi.util.Disposer;
import com.intellij.openapi.util.io.FileUtilRt;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.testFramework.PsiTestUtil;
import com.intellij.testFramework.fixtures.CodeInsightTestFixture;
import one.util.streamex.StreamEx;
import org.angularjs.AngularTestUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static com.intellij.lang.javascript.ui.NodeModuleNamesUtil.PACKAGE_JSON;

public enum Angular2TestModule {

  AGM_CORE_1_0_0_BETA_5("@agm/core", "1.0.0-beta.5"),
  ANGULAR_CDK_14_2_0("@angular/cdk", "14.2.0"),
  ANGULAR_COMMON_4_0_0("@angular/common", "4.0.0"),
  ANGULAR_COMMON_8_2_14("@angular/common", "8.2.14"),
  ANGULAR_COMMON_13_3_5("@angular/common", "13.3.5"),
  ANGULAR_CORE_4_0_0("@angular/core", "4.0.0"),
  ANGULAR_CORE_8_2_14("@angular/core", "8.2.14"),
  ANGULAR_CORE_9_1_1_MIXED("@angular/core", "9.1.1-mixed"),
  ANGULAR_CORE_13_3_5("@angular/core", "13.3.5"),
  ANGULAR_FLEX_LAYOUT_13_0_0("@angular/flex-layout", "13.0.0-beta.36"),
  ANGULAR_FORMS_4_0_0("@angular/forms", "4.0.0"),
  ANGULAR_FORMS_8_2_14("@angular/forms", "8.2.14"),
  ANGULAR_MATERIAL_7_2_1("@angular/material", "7.2.1"),
  ANGULAR_MATERIAL_8_2_3_MIXED("@angular/material", "8.2.3-mixed"),
  ANGULAR_MATERIAL_14_2_5_MIXED("@angular/material", "14.2.5"),
  ANGULAR_PLATFORM_BROWSER_4_0_0("@angular/platform-browser", "4.0.0"),
  ANGULAR_ROUTER_4_0_0("@angular/router", "4.0.0"),
  ANGULAR_L10N_4_2_0("angular-l10n", "4.2.0"),
  EVO_UI_KIT_1_17_0("@evo/ui-kit", "1.17.0"),
  IONIC_ANGULAR_3_0_1("ionic-angular", "3.0.1"),
  IONIC_ANGULAR_4_1_1("@ionic/angular", "4.1.1"),
  IONIC_ANGULAR_4_11_4_IVY("@ionic/angular", "4.11.4-ivy"),
  NGNEAT_TRANSLOCO_2_6_0_IVY("@ngneat/transloco", "2.6.0-ivy"),
  NGXS_STORE_3_6_2("@ngxs/store", "3.6.2"),
  NGXS_STORE_3_6_2_MIXED("@ngxs/store", "3.6.2-mixed"),
  NG_ZORRO_ANTD_8_5_0_IVY("ng-zorro-antd", "8.5.0-ivy"),
  RXJS_6_4_0("rxjs", "6.4.0"),
  ;
  private static final String DATA_DIR = FileUtilRt.toCanonicalPath(
    AngularTestUtil.getBaseTestDataPath(Angular2TestModule.class) + "../node_modules/", '/', false);

  private final String myPackageName;
  private final String myVersion;

  Angular2TestModule(String packageName, String version) {
    myPackageName = packageName;
    myVersion = version;
  }

  public String getLocation() {
    return DATA_DIR + myPackageName.replace('/', '#') + "/" + myVersion + "/";
  }

  public static void configureCopy(@NotNull CodeInsightTestFixture fixture, @NotNull Angular2TestModule @NotNull ... modules) {
    configure(fixture, false, null, modules);
  }

  public static void configureLink(@NotNull CodeInsightTestFixture fixture, @NotNull Angular2TestModule @NotNull ... modules) {
    configure(fixture, true, null, modules);
  }

  public static void configure(@NotNull CodeInsightTestFixture fixture,
                               boolean linkSourceRoot,
                               @Nullable String targetRoot,
                               @NotNull Angular2TestModule @NotNull ... modules) {
    if (targetRoot == null) {
      targetRoot = ".";
    }
    if (modules.length > 0) {
      if (linkSourceRoot) {
        WriteAction.runAndWait(() -> {
          for (Angular2TestModule module : modules) {
            VirtualFile nodeModules = ReSharperTestUtil.fetchVirtualFile("", module.getLocation() + "node_modules",
                                                                         fixture.getTestRootDisposable());
            PsiTestUtil.addSourceContentToRoots(fixture.getModule(), nodeModules);
            Disposer.register(fixture.getTestRootDisposable(),
                              () -> PsiTestUtil.removeContentEntry(fixture.getModule(), nodeModules));
          }
        });
      }
      else {
        for (Angular2TestModule module : modules) {
          fixture.getTempDirFixture().copyAll(module.getLocation(), targetRoot);
        }
      }
    }
    if (fixture.getTempDirFixture().getFile(PACKAGE_JSON) == null) {
      JSTestUtils.addPackageJson(fixture, StreamEx.of(modules).map(module -> module.myPackageName)
        .append("@angular/core").distinct().toArray(String.class));
    }
  }
}
