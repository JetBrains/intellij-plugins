// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.vuejs.libraries.nuxt

import org.junit.runner.RunWith
import org.junit.runners.Suite

@RunWith(Suite::class)
@Suite.SuiteClasses(
  NuxtConfigTest::class,
  NuxtCompletionTest::class,
  NuxtResolveTest::class,
  NuxtHighlightingTest::class,
  NuxtNavigationTest::class,
  NuxtFindUsagesTest::class,
  NuxtRenameTest::class,
)
class NuxtTestSuite