// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.astro

import com.intellij.idea.IJIgnore
import org.jetbrains.astro.lang.AstroHighlightingLexerTest
import org.jetbrains.astro.lang.AstroLexerTest
import org.junit.platform.suite.api.SelectClasses
import org.junit.platform.suite.api.Suite

@IJIgnore(issue = "Manual only")
@Suite
@SelectClasses(
  AstroLexerTest::class,
  AstroHighlightingLexerTest::class,
)
class AstroTestSuite
