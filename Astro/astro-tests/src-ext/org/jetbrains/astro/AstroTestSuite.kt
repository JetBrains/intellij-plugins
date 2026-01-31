// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.astro

import org.jetbrains.astro.codeInsight.AstroCommenterTest
import org.jetbrains.astro.codeInsight.AstroCompletionTest
import org.jetbrains.astro.codeInsight.AstroCompletionTypingTest
import org.jetbrains.astro.codeInsight.AstroCopyPasteTest
import org.jetbrains.astro.codeInsight.AstroDocumentationTest
import org.jetbrains.astro.codeInsight.AstroFindUsagesTest
import org.jetbrains.astro.codeInsight.AstroFormattingTest
import org.jetbrains.astro.codeInsight.AstroGotoDeclarationTest
import org.jetbrains.astro.codeInsight.AstroHtmlFormatterTest
import org.jetbrains.astro.codeInsight.AstroInspectionsTest
import org.jetbrains.astro.codeInsight.AstroRenameTest
import org.jetbrains.astro.codeInsight.AstroTypingTest
import org.jetbrains.astro.codeInsight.highlighting.AstroHighlightingTest
import org.jetbrains.astro.codeInsight.highlighting.AstroQuickFixHighlightingTest
import org.jetbrains.astro.codeInsight.highlighting.AstroSuppressedInspectionsHighlightingTest
import org.jetbrains.astro.lang.AstroHighlightingLexerTest
import org.jetbrains.astro.lang.AstroLexerTest
import org.jetbrains.astro.lang.AstroParserTest
import org.junit.runner.RunWith
import org.junit.runners.Suite

@RunWith(Suite::class)
@Suite.SuiteClasses(
  AstroLexerTest::class,
  AstroHighlightingLexerTest::class,
  AstroParserTest::class,
  AstroCompletionTest::class,
  AstroHighlightingTest::class,
  AstroSuppressedInspectionsHighlightingTest::class,
  AstroDocumentationTest::class,
  AstroInspectionsTest::class,
  AstroTypingTest::class,
  AstroFormattingTest::class,
  AstroCopyPasteTest::class,
  AstroHtmlFormatterTest::class,
  AstroCommenterTest::class,
  AstroGotoDeclarationTest::class,
  AstroFindUsagesTest::class,
  AstroRenameTest::class,
  AstroQuickFixHighlightingTest::class,
  AstroCompletionTypingTest::class,
)
class AstroTestSuite
