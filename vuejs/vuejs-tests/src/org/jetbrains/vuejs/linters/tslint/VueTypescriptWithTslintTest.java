// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.vuejs.linters.tslint;

import com.intellij.lang.javascript.linter.tslint.TypescriptServiceWithTslintTestBase;

import static org.jetbrains.vuejs.language.VueTestUtilKt.vueRelativeTestDataPath;

public class VueTypescriptWithTslintTest extends TypescriptServiceWithTslintTestBase {
  @Override
  protected String getBasePath() {
    return vueRelativeTestDataPath() + "/linters/tslint/";
  }

  public void testFilterWhitespaceErrorsByScriptTag() {
    doHighlightingTest("main", "vue");
  }

  public void testFixAllErrorsWithWhitespaceRules() {
    doFixTest("main", "vue", "Fix all auto-fixable tslint failures");
  }
}