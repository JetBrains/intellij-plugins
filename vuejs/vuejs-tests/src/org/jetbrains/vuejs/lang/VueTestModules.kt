// Copyright 2000-2022 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.vuejs.lang

import com.intellij.javascript.nodejs.library.NodeModulesDirectoryManager
import com.intellij.lang.javascript.buildTools.npm.PackageJsonUtil
import com.intellij.openapi.project.Project
import com.intellij.testFramework.fixtures.CodeInsightTestFixture

fun createPackageJsonWithVueDependency(fixture: CodeInsightTestFixture,
                                       additionalDependencies: String = "") {
  fixture.configureByText(PackageJsonUtil.FILE_NAME, """
    {
      "name": "test",
      "version": "0.0.1",
      "dependencies": {
        "vue": "2.5.3" ${if (additionalDependencies.isBlank()) "" else ", $additionalDependencies"}
      }
    }
  """.trimIndent())
}

fun CodeInsightTestFixture.configureVueDependencies(vararg modules: VueTestModule) {
  createPackageJsonWithVueDependency(
    this, modules.asSequence()
    .filter { it.folder != "vue" }
    .flatMap { it.packageNames.asSequence() }
    .joinToString { "\"${it}\": \"*\"" })
  for (module in modules) {
    tempDirFixture.copyAll("${getVueTestDataPath()}/modules/${module.folder}/node_modules", "node_modules")
  }
  forceReloadProjectRoots(project)
}

internal fun forceReloadProjectRoots(project: Project) {
  // TODO - this shouldn't be needed, something's wrong with how roots are set within tests - check RootIndex#myRootInfos
  NodeModulesDirectoryManager.getInstance(project).requestLibrariesUpdate()
}

enum class VueTestModule(val folder: String, vararg packageNames: String) {
  BOOTSTRAP_VUE_2_0_0_RC_11("bootstrap-vue"),
  BUEFY_0_6_2("buefy"),
  COMPOSITION_API_0_4_0("composition-api/0.4.0", "@vue/composition-api"),
  COMPOSITION_API_1_0_0("composition-api/1.0.0", "@vue/composition-api"),
  ELEMENT_UI_2_0_5("element-ui"),
  ELEMENT_PLUS_2_1_11_NO_WEB_TYPES("element-plus/2.1.11-no-web-types", "element-plus"),
  HEADLESS_UI_1_4_1("headlessui", "@headlessui/vue"),
  IVIEW_2_8_0("iview/2.8.0", "iview"),
  IVIEW_3_5_4("iview/3.5.4", "iview"),
  MINT_UI_2_2_3("mint-ui"),
  NAIVE_UI_2_19_11_NO_WEB_TYPES("naive-ui/2.19.11-no-web-types", "naive-ui"),
  NAIVE_UI_2_19_11("naive-ui/2.19.11", "naive-ui"),

  // This version contains updated Web-Types and volar.d.ts included from index.d.ts
  NAIVE_UI_2_33_2_PATCHED("naive-ui/2.33.2-patched", "naive-ui"),
  NUXT_2_8_1("nuxt/2.8.1", "nuxt"),
  NUXT_2_9_2("nuxt/2.9.2", "nuxt", "@nuxt/types"),
  NUXT_2_13_2("nuxt/2.13.2", "nuxt", "@nuxt/types"),
  NUXT_2_15_6("nuxt/2.15.6", "nuxt", "@nuxt/types"),
  PRIMEVUE_3_8_2("primevue/3.8.2", "primevue"),
  PINIA_2_0_22("pinia/2.0.22", "pinia", "vue-demi"),
  QUASAR_2_6_5("quasar/2.6.5", "quasar"),
  SHARDS_VUE_1_0_5("shards-vue", "@shards/vue"),
  VUE_2_5_3("vue/2.5.3", "vue"),
  VUE_2_6_10("vue/2.6.10", "vue"),
  VUE_3_0_0("vue/3.0.0", "vue"),
  VUE_3_1_0("vue/3.1.0", "vue"),
  VUE_3_2_2("vue/3.2.2", "vue"),
  VUE_3_3_0_ALPHA5("vue/3.3.0-alpha", "vue"), // TODO - replace with 3.0.0 once released
  VUELIDATE_0_7_13("vuelidate", "@types/vuelidate", "vuelidate"),
  VUETIFY_0_17_2("vuetify/0.17.2", "vuetify"),
  VUETIFY_1_2_10("vuetify/1.2.10", "vuetify"),
  VUETIFY_1_3_7("vuetify/1.3.7", "vuetify"),
  VUEUSE_9_3_0("vueuse/9.3.0", "@vueuse/core"),
  VUEX_3_1_0("vuex/3.1.0", "vuex"),
  VUEX_4_0_0("vuex/4.0.0", "vuex");

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
