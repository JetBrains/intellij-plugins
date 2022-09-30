// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.vuejs.libraries

import org.jetbrains.vuejs.libraries.cssModules.CssModulesTest
import org.jetbrains.vuejs.libraries.eslint.VueESLintImportCodeStyleTest
import org.jetbrains.vuejs.libraries.i18n.I18nTest
import org.jetbrains.vuejs.libraries.nuxt.NuxtTestSuite
import org.jetbrains.vuejs.libraries.pinia.PiniaTest
import org.jetbrains.vuejs.libraries.templateLoader.TemplateLoaderCompletionTest
import org.jetbrains.vuejs.libraries.vueLoader.VueLoaderTest
import org.jetbrains.vuejs.libraries.vuelidate.VuelidateTest
import org.jetbrains.vuejs.libraries.vuex.VuexTestSuite
import org.junit.runner.RunWith
import org.junit.runners.Suite

@RunWith(Suite::class)
@Suite.SuiteClasses(
  NuxtTestSuite::class,
  PiniaTest::class,
  VuexTestSuite::class,
  VueLoaderTest::class,
  TemplateLoaderCompletionTest::class,
  VuelidateTest::class,
  CssModulesTest::class,
  I18nTest::class,
  VueESLintImportCodeStyleTest::class
)
class LibrariesTestSuite
