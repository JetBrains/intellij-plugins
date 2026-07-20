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
 * config-less fallback batch. Covered cases:
 * - templates whose component is part of the query's config graph land in the query's bucket (the query's own module
 *   `moduleA`, and `moduleC` whose component is imported into `moduleA`);
 * - `moduleD`, whose config imports `moduleA`'s component (so its graph includes the query) but is not imported back,
 *   forms its own relevant bucket;
 * - `moduleB`, scoped to an unrelated config, is excluded entirely.
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

      JSSearchCandidateBatcherTestSupport.assertBatches(
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
          // Query bucket: moduleA's own templates and moduleC's template (its component is imported into moduleA, so
          // it is part of moduleA's config graph).
          listOf("moduleA/component-a1.html", "moduleA/component-a2.html", "moduleC/component-c.html"),
          // Separate relevant bucket: moduleD's config graph includes the query's component (moduleD imports it),
          // even though moduleA does not import moduleD.
          listOf("moduleD/component-d.html"),
        )
        // moduleB is scoped to an unrelated config and is excluded entirely.
      )
    }
  }
}
