// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.vuejs.libraries

import org.jetbrains.vuejs.libraries.nuxt.NuxtTestSuite
import org.jetbrains.vuejs.libraries.templateLoader.TemplateLoaderCompletionTest
import org.jetbrains.vuejs.libraries.vuelidate.VuelidateTest
import org.jetbrains.vuejs.libraries.vuex.VuexTestSuite
import org.junit.runner.RunWith
import org.junit.runners.Suite

@RunWith(Suite::class)
@Suite.SuiteClasses(
  NuxtTestSuite::class,
  VuexTestSuite::class,
  TemplateLoaderCompletionTest::class,
  VuelidateTest::class
)
class LibrariesTestSuite