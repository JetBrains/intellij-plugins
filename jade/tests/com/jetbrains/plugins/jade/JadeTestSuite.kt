// Copyright 2000-2025 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.jetbrains.plugins.jade

import com.jetbrains.plugins.jade.injectedScriptJs.JadeJsHighlightingTest
import com.jetbrains.plugins.jade.injectedScriptJs.JadeJsIntroduceVariableTest
import com.jetbrains.plugins.jade.injectedScriptJs.JadeMetaJsLexerTest
import com.jetbrains.plugins.jade.parser.JadeParsingTest
import org.junit.runner.RunWith
import org.junit.runners.Suite

@RunWith(Suite::class)
@Suite.SuiteClasses(
  JadeLexerTest::class,
  JadeParsingTest::class,
  JadeCompletionTest::class,
  JadeCopyPasteTest::class,
  JadeFindUsagesTest::class,
  JadeFoldingTest::class,
  JadeFormatterTest::class,
  JadeHighlightingLexerTest::class,
  JadeHighlightingTest::class,
  JadeInjectionTest::class,
  JadeQuoteHandlerTest::class,
  JadeRenameTest::class,
  JadeSpellcheckerTest::class,
  JadeTodoTest::class,
  JadeTypingTest::class,
  JadeJsHighlightingTest::class,
  JadeJsIntroduceVariableTest::class,
  JadeMetaJsLexerTest::class,
)
class JadeTestSuite