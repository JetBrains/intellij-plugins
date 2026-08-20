// Copyright 2000-2026 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.angular2.findUsages

import com.intellij.lang.javascript.findUsages.JSSearchCandidateBatcherTestSupport
import com.intellij.lang.javascript.findUsages.JSSearchCandidateBatcherTestSupport.EXPERIMENTAL_CANDIDATES_BATCHING_KEY
import com.intellij.openapi.util.registry.Registry
import org.angular2.Angular2TestCase
import org.angular2.Angular2TestModule
import org.angular2.TestNoService
import org.junit.Test

/**
 * Angular-specific coverage for [com.intellij.lang.javascript.findUsages.JSImportGraphSearchCandidateBatcher].
 *
 * Angular templates (`*.html`) are not part of any TypeScript config import graph — a component references its
 * template via `@Component({ templateUrl })`, which is not a JS import. This test verifies that a template candidate
 * is nevertheless batched by the config of its associated component `.ts` file (resolved through
 * [com.intellij.lang.javascript.config.JSConfigProvider.getTSConfigGraphFile]) instead of being pushed into the
 * config-less unbatched bucket. Covered cases:
 * - templates are grouped by the directly including config of their component (`moduleA`'s two templates share a
 *   bucket; `moduleC`, reachable from `moduleA`'s config graph but preferring its own config, is redirected there
 *   and — since that redirect is resolved fast enough — arrives coalesced with `moduleA`'s batch; `moduleD` needs
 *   its own config graph built from scratch and arrives as a separate, later batch);
 * - every candidate is emitted; none is dropped. `moduleB`, scoped to a config unrelated to the query, is simply
 *   emitted as its own separate batch instead of being excluded.
 *
 * Shares the batching assertions with the plain-TypeScript `JSSearchCandidateBatcherTest` via
 * [JSSearchCandidateBatcherTestSupport].
 */
@TestNoService
class Angular2SearchCandidateBatcherTest : Angular2TestCase("findUsages") {

  @Test
  fun testGroupsTemplateCandidatesByComponentConfig() {
    Registry.get(EXPERIMENTAL_CANDIDATES_BATCHING_KEY).setValue(true, testRootDisposable)
    doConfiguredTest(Angular2TestModule.ANGULAR_CORE_19_2_0, dir = true, dirName = "importGraphBatcher", configureFile = false) {
      val root = tempDirFixture.findOrCreateDir(".")

      // Whether the moduleA/moduleC bucket and the separate moduleB/moduleD batches arrive as one combined pull
      // or several is a genuine race (see assertFlattenedBatches), so only completeness and relative order are
      // asserted here.
      JSSearchCandidateBatcherTestSupport.assertFlattenedBatches(
        project, root,
        queryPaths = listOf("moduleA/component-a1.html"),
        candidatePaths = listOf(
          "moduleA/component-a1.html",
          "moduleA/component-a2.html",
          "moduleB/component-b.html",
          "moduleC/component-c.html",
          "moduleD/component-d.html",
        ),
        expected = listOf(
          "moduleA/component-a1.html",
          "moduleC/component-c.html",
          "moduleA/component-a2.html",
          "moduleB/component-b.html",
          "moduleD/component-d.html",
        ),
      )
    }
  }
}
