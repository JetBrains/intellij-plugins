// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.vuejs.lang

import com.intellij.testFramework.fixtures.CodeInsightTestFixture

fun CodeInsightTestFixture.configureDependencies(vararg modules: VueTestModule) {
  createPackageJsonWithVueDependency(
    this, modules.asSequence().filter { it.folder != "vue" }.joinToString { "\"${it.packageName}\": \"0.0.0\"" })
  for (module in modules) {
    tempDirFixture.copyAll("${getVueTestDataPath()}/modules/${module.folder}/node_modules", "node_modules")
  }
}

enum class VueTestModule(val folder: String, val packageName: String = folder) {
  BOOTSTRAP_VUE_2_0_0_RC_11("bootstrap-vue"),
  BUEFY_0_6_2("buefy"),
  COMPOSITION_API_0_4_0("composition-api", "@vue/composition-api"),
  ELEMENT_UI_2_0_5("element-ui"),
  IVIEW_2_8_0("iview"),
  MINT_UI_2_2_3("mint-ui"),
  SHARDS_VUE_1_0_5("shards-vue", "@shards/vue"),
  VUE_2_5_3("vue/2.5.3", "vue"),
  VUE_2_6_10("vue/2.6.10", "vue"),
  VUE_3_0_0_BETA_9("vue/3.0.0-beta.9", "vue"),
  VUETIFY_0_17_2("vuetify/0.17.2", "vuetify"),
  VUETIFY_1_2_10("vuetify/1.2.10", "vuetify"),
  VUETIFY_1_3_7("vuetify/1.3.7", "vuetify"),
  VUEX_3_1_0("vuex")
}