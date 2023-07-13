// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.angular2

import com.intellij.javascript.web.WebFrameworkTestModule
import com.intellij.lang.javascript.JSTestUtils
import com.intellij.lang.javascript.ui.NodeModuleNamesUtil
import com.intellij.lang.resharper.ReSharperTestUtil
import com.intellij.openapi.application.WriteAction
import com.intellij.openapi.util.Disposer
import com.intellij.openapi.util.io.FileUtilRt
import com.intellij.testFramework.PsiTestUtil
import com.intellij.testFramework.fixtures.CodeInsightTestFixture
import one.util.streamex.StreamEx
import org.angularjs.AngularTestUtil

enum class Angular2TestModule(private val myPackageName: String, private val myVersion: String) : WebFrameworkTestModule {
  AGM_CORE_1_0_0_BETA_5("@agm/core", "1.0.0-beta.5"),
  ANGULAR_CDK_14_2_0("@angular/cdk", "14.2.0"),
  ANGULAR_COMMON_4_0_0("@angular/common", "4.0.0"),
  ANGULAR_COMMON_8_2_14("@angular/common", "8.2.14"),
  ANGULAR_COMMON_13_3_5("@angular/common", "13.3.5"),
  ANGULAR_COMMON_15_1_5("@angular/common", "15.1.5"),
  ANGULAR_COMMON_16_0_0_NEXT_4("@angular/common", "16.0.0-next.4"),
  ANGULAR_CORE_4_0_0("@angular/core", "4.0.0"),
  ANGULAR_CORE_8_2_14("@angular/core", "8.2.14"),
  ANGULAR_CORE_9_1_1_MIXED("@angular/core", "9.1.1-mixed"),
  ANGULAR_CORE_13_3_5("@angular/core", "13.3.5"),
  ANGULAR_CORE_15_1_5("@angular/core", "15.1.5"),
  ANGULAR_CORE_16_0_0_NEXT_4("@angular/core", "16.0.0-next.4"),
  ANGULAR_FLEX_LAYOUT_13_0_0("@angular/flex-layout", "13.0.0-beta.36"),
  ANGULAR_FORMS_4_0_0("@angular/forms", "4.0.0"),
  ANGULAR_FORMS_8_2_14("@angular/forms", "8.2.14"),
  ANGULAR_FORMS_16_0_0_NEXT_4("@angular/forms", "16.0.0-next.4"),
  ANGULAR_MATERIAL_7_2_1("@angular/material", "7.2.1"),
  ANGULAR_MATERIAL_8_2_3_MIXED("@angular/material", "8.2.3-mixed"),
  ANGULAR_MATERIAL_14_2_5_MIXED("@angular/material", "14.2.5"),
  ANGULAR_MATERIAL_16_0_0_NEXT_6("@angular/material", "16.0.0-next.6"),
  ANGULAR_PLATFORM_BROWSER_4_0_0("@angular/platform-browser", "4.0.0"),
  ANGULAR_ROUTER_4_0_0("@angular/router", "4.0.0"),
  ANGULAR_ROUTER_16_0_0_NEXT_4("@angular/router", "16.0.0-next.4"),
  ANGULAR_L10N_4_2_0("angular-l10n", "4.2.0"),
  EVO_UI_KIT_1_17_0("@evo/ui-kit", "1.17.0"),
  IONIC_ANGULAR_3_0_1("ionic-angular", "3.0.1"),
  IONIC_ANGULAR_4_1_1("@ionic/angular", "4.1.1"),
  IONIC_ANGULAR_4_11_4_IVY("@ionic/angular", "4.11.4-ivy"),
  NGNEAT_TRANSLOCO_2_6_0_IVY("@ngneat/transloco", "2.6.0-ivy"),
  NGXS_STORE_3_6_2("@ngxs/store", "3.6.2"),
  NGXS_STORE_3_6_2_MIXED("@ngxs/store", "3.6.2-mixed"),
  NG_ZORRO_ANTD_8_5_0_IVY("ng-zorro-antd", "8.5.0-ivy"),
  RXJS_6_4_0("rxjs", "6.4.0");

  override val packageNames: List<String> = listOf(myPackageName)
  override val folder: String = myPackageName.replace('/', '#') + "/" + myVersion + "/node_modules"

  val location: String
    get() = DATA_DIR + myPackageName.replace('/', '#') + "/" + myVersion + "/"

  companion object {
    private val DATA_DIR = FileUtilRt.toCanonicalPath(
      AngularTestUtil.getBaseTestDataPath() + "node_modules/", '/', false)

    @JvmStatic
    fun configureCopy(fixture: CodeInsightTestFixture, vararg modules: Angular2TestModule) {
      configure(fixture, false, modules = modules)
    }

    @JvmStatic
    fun configureLink(fixture: CodeInsightTestFixture, vararg modules: Angular2TestModule) {
      configure(fixture, true, modules = modules)
    }

    @JvmStatic
    fun configure(fixture: CodeInsightTestFixture,
                  linkSourceRoot: Boolean,
                  vararg modules: Angular2TestModule) {
      if (modules.isNotEmpty()) {
        if (linkSourceRoot) {
          WriteAction.runAndWait<RuntimeException> {
            for (module in modules) {
              val nodeModules = ReSharperTestUtil.fetchVirtualFile("", module.location + "node_modules",
                                                                   fixture.testRootDisposable)
              PsiTestUtil.addSourceContentToRoots(fixture.getModule(), nodeModules)
              Disposer.register(fixture.testRootDisposable
              ) { PsiTestUtil.removeContentEntry(fixture.getModule(), nodeModules) }
            }
          }
        }
        else {
          for (module in modules) {
            fixture.getTempDirFixture().copyAll(module.location, ".")
          }
        }
      }
      if (fixture.getTempDirFixture().getFile(NodeModuleNamesUtil.PACKAGE_JSON) == null) {
        JSTestUtils.addPackageJson(fixture, *StreamEx.of(*modules).map { module: Angular2TestModule -> module.myPackageName }
          .append("@angular/core").distinct().toArray(String::class.java))
      }
    }
  }
}
