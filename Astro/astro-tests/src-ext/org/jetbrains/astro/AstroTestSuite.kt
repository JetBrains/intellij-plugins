// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.astro

import org.jetbrains.astro.codeInsight.*
import org.jetbrains.astro.codeInsight.highlighting.AstroHighlightingTest
import org.jetbrains.astro.codeInsight.highlighting.AstroSuppressedInspectionsHighlightingTest
import org.jetbrains.astro.lang.AstroHighlightingLexerTest
import org.jetbrains.astro.lang.AstroLexerTest
import org.jetbrains.astro.lang.AstroParserTest
import org.jetbrains.astro.service.AstroTypeScriptServiceGotoDeclarationTest
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
  AstroTypeScriptServiceGotoDeclarationTest::class,
)
class AstroTestSuite
