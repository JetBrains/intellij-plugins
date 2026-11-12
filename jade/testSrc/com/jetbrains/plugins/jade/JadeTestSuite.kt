// Copyright 2000-2025 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.jetbrains.plugins.jade

import com.intellij.idea.IJIgnore
import com.jetbrains.plugins.jade.injectedScriptJs.JadeJsHighlightingTest
import org.junit.platform.suite.api.SelectClasses
import org.junit.platform.suite.api.Suite

@IJIgnore(issue = "Manual only")
@Suite
@SelectClasses(
  JadeCompletionTest::class,
  JadeCopyPasteTest::class,
  JadeFindUsagesTest::class,
  JadeFoldingTest::class,
  JadeFormatterTest::class,
  JadeHighlightingTest::class,
  JadeQuoteHandlerTest::class,
  JadeRenameTest::class,
  JadeSpellcheckerTest::class,
  JadeTodoTest::class,
  JadeTypingTest::class,
  JadeJsHighlightingTest::class,
)
class JadeTestSuite