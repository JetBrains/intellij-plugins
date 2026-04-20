// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.vuejs.lang

import org.junit.runner.RunWith
import org.junit.runners.Suite

@RunWith(Suite::class)
@Suite.SuiteClasses(
  VueAutoPopupTest::class,
  VueCompletionTest::class,
  VueDocumentationTest::class,
  VueExtractComponentTest::class,
  VueFindUsagesTest::class,
  VueGotoDeclarationTest::class,
  VueHighlightingTest::class,
  VueNewComponentTest::class,
  VueRenameTest::class,
)
class VuePluginTestSuite

@RunWith(Suite::class)
@Suite.SuiteClasses(
  VueAutoPopupTest.WithLegacyPluginTest::class,
  VueCompletionTest.WithLegacyPluginTest::class,
  VueDocumentationTest.WithLegacyPluginTest::class,
  VueExtractComponentTest.WithLegacyPluginTest::class,
  VueFindUsagesTest.WithLegacyPluginTest::class,
  VueGotoDeclarationTest.WithLegacyPluginTest::class,
  VueHighlightingTest.WithLegacyPluginTest::class,
  VueNewComponentTest.WithLegacyPluginTest::class,
  VueRenameTest.WithLegacyPluginTest::class,
)
class VueLegacyPluginTestSuite

@RunWith(Suite::class)
@Suite.SuiteClasses(
  VueAutoPopupTest.WithoutServiceTest::class,
  VueCompletionTest.WithoutServiceTest::class,
  VueDocumentationTest.WithoutServiceTest::class,
  VueExtractComponentTest.WithoutServiceTest::class,
  VueFindUsagesTest.WithoutServiceTest::class,
  VueGotoDeclarationTest.WithoutServiceTest::class,
  VueHighlightingTest.WithoutServiceTest::class,
  VueNewComponentTest.WithoutServiceTest::class,
  VueRenameTest.WithoutServiceTest::class,
)
class VueWithoutServiceTestSuite
