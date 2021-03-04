// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.vuejs.lang

import com.intellij.openapi.application.runWriteAction
import com.intellij.openapi.roots.ex.ProjectRootManagerEx
import com.intellij.openapi.util.EmptyRunnable
import com.intellij.testFramework.fixtures.CodeInsightTestFixture
import com.intellij.testFramework.runInEdtAndWait

fun CodeInsightTestFixture.configureVueDependencies(vararg modules: VueTestModule) {
  createPackageJsonWithVueDependency(
    this, modules.asSequence()
    .filter { it.folder != "vue" }
    .flatMap { it.packageNames.asSequence() }
    .joinToString { "\"${it}\": \"0.0.0\"" })
  for (module in modules) {
    tempDirFixture.copyAll("${getVueTestDataPath()}/modules/${module.folder}/node_modules", "node_modules")
  }
  // TODO - this shouldn't be needed, something's wrong with how roots are set within tests - check RootIndex#myRootInfos
  runInEdtAndWait {
    runWriteAction {
      ProjectRootManagerEx.getInstanceEx(project)
        .makeRootsChange(EmptyRunnable.getInstance(), false, true)
    }
  }
}

enum class VueTestModule(val folder: String, vararg packageNames: String) {
  BOOTSTRAP_VUE_2_0_0_RC_11("bootstrap-vue"),
  BUEFY_0_6_2("buefy"),
  COMPOSITION_API_0_4_0("composition-api", "@vue/composition-api"),
  ELEMENT_UI_2_0_5("element-ui"),
  IVIEW_2_8_0("iview"),
  MINT_UI_2_2_3("mint-ui"),
  NUXT_2_8_1("nuxt/2.8.1", "nuxt"),
  NUXT_2_9_2("nuxt/2.9.2", "nuxt", "@nuxt/types"),
  NUXT_2_13_2("nuxt/2.13.2", "nuxt", "@nuxt/types"),
  SHARDS_VUE_1_0_5("shards-vue", "@shards/vue"),
  VUE_2_5_3("vue/2.5.3", "vue"),
  VUE_2_6_10("vue/2.6.10", "vue"),
  VUE_3_0_0("vue/3.0.0", "vue"),
  VUELIDATE_0_7_13("vuelidate", "@types/vuelidate", "vuelidate"),
  VUETIFY_0_17_2("vuetify/0.17.2", "vuetify"),
  VUETIFY_1_2_10("vuetify/1.2.10", "vuetify"),
  VUETIFY_1_3_7("vuetify/1.3.7", "vuetify"),
  VUEX_3_1_0("vuex");

  val packageNames: List<String>

  init {
    if (packageNames.isEmpty()) {
      this.packageNames = listOf(folder)
    }
    else {
      this.packageNames = packageNames.toList()
    }
  }
}