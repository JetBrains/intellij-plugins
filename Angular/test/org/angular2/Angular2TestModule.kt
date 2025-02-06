// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.angular2

import com.intellij.javascript.testFramework.web.WebFrameworkTestModule
import com.intellij.javascript.testFramework.web.configureDependencies
import com.intellij.testFramework.fixtures.CodeInsightTestFixture

enum class Angular2TestModule(myPackageName: String, myVersion: String) : WebFrameworkTestModule {
  AGM_CORE_1_0_0_BETA_5("@agm/core", "1.0.0-beta.5"),

  ANGULAR_CDK_14_2_0("@angular/cdk", "14.2.0"),
  ANGULAR_CDK_17_1_0_RC_0("@angular/cdk", "17.1.0-rc.0"),

  ANGULAR_COMMON_4_0_0("@angular/common", "4.0.0"),
  ANGULAR_COMMON_8_2_14("@angular/common", "8.2.14"),
  ANGULAR_COMMON_13_3_5("@angular/common", "13.3.5"),
  ANGULAR_COMMON_15_1_5("@angular/common", "15.1.5"),
  ANGULAR_COMMON_16_2_8("@angular/common", "16.2.8"),
  ANGULAR_COMMON_17_3_0("@angular/common", "17.3.0"),
  ANGULAR_COMMON_18_2_1("@angular/common", "18.2.1"),

  ANGULAR_CORE_4_0_0("@angular/core", "4.0.0"),
  ANGULAR_CORE_8_2_14("@angular/core", "8.2.14"),
  ANGULAR_CORE_9_1_1_MIXED("@angular/core", "9.1.1-mixed"),
  ANGULAR_CORE_13_3_5("@angular/core", "13.3.5"),
  ANGULAR_CORE_15_1_5("@angular/core", "15.1.5"),
  ANGULAR_CORE_16_2_8("@angular/core", "16.2.8"),
  ANGULAR_CORE_17_3_0("@angular/core", "17.3.0"),
  ANGULAR_CORE_18_2_1("@angular/core", "18.2.1"),
  ANGULAR_CORE_19_0_0_NEXT_4("@angular/core", "19.0.0-next.4"),

  ANGULAR_FLEX_LAYOUT_13_0_0("@angular/flex-layout", "13.0.0-beta.36"),

  ANGULAR_FORMS_4_0_0("@angular/forms", "4.0.0"),
  ANGULAR_FORMS_8_2_14("@angular/forms", "8.2.14"),
  ANGULAR_FORMS_16_2_8("@angular/forms", "16.2.8"),
  ANGULAR_FORMS_17_3_0("@angular/forms", "17.3.0"),

  ANGULAR_MATERIAL_7_2_1("@angular/material", "7.2.1"),
  ANGULAR_MATERIAL_8_2_3_MIXED("@angular/material", "8.2.3-mixed"),
  ANGULAR_MATERIAL_14_2_5_MIXED("@angular/material", "14.2.5"),
  ANGULAR_MATERIAL_16_2_8("@angular/material", "16.2.8"),
  ANGULAR_MATERIAL_17_3_0("@angular/material", "17.3.0"),

  ANGULAR_PLATFORM_BROWSER_4_0_0("@angular/platform-browser", "4.0.0"),

  ANGULAR_ROUTER_4_0_0("@angular/router", "4.0.0"),
  ANGULAR_ROUTER_16_2_8("@angular/router", "16.2.8"),

  ANGULAR_L10N_4_2_0("angular-l10n", "4.2.0"),
  EVO_UI_KIT_1_17_0("@evo/ui-kit", "1.17.0"),
  IONIC_ANGULAR_3_0_1("ionic-angular", "3.0.1"),
  IONIC_ANGULAR_4_1_1("@ionic/angular", "4.1.1"),
  IONIC_ANGULAR_4_11_4_IVY("@ionic/angular", "4.11.4-ivy"),
  IONIC_ANGULAR_7_7_3("@ionic/angular", "7.7.3"),
  NGNEAT_TRANSLOCO_2_6_0_IVY("@ngneat/transloco", "2.6.0-ivy"),
  NGXS_STORE_3_6_2("@ngxs/store", "3.6.2"),
  NGXS_STORE_3_6_2_MIXED("@ngxs/store", "3.6.2-mixed"),
  NG_ZORRO_ANTD_8_5_0_IVY("ng-zorro-antd", "8.5.0-ivy"),
  RXJS_6_4_0("rxjs", "6.4.0"),
  RXJS_7_8_1("rxjs", "7.8.1"),
  TS_LIB("tslib", "2.6.2"),
  ;

  override val packageNames: List<String> = listOf(myPackageName)
  override val folder: String = myPackageName.replace('/', '#') + "/" + myVersion + "/node_modules"

  companion object {
    private val testDataRoot = Angular2TestUtil.getBaseTestDataPath()
    private val defaultDependencies = mapOf("@angular/core" to "*")

    @JvmStatic
    fun CodeInsightTestFixture.configureDependencies(
      vararg modules: Angular2TestModule,
    ) {
      configureDependencies(testDataRoot, defaultDependencies, *modules)
    }
  }
}
