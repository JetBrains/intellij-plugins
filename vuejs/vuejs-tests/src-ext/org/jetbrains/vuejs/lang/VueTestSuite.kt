// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.vuejs.lang

import org.jetbrains.vuejs.lang.expr.VueJSParserTest
import org.jetbrains.vuejs.lang.html.VueHighlightingLexerTest
import org.jetbrains.vuejs.lang.html.VueIndexerTest
import org.jetbrains.vuejs.lang.html.VueLexerTest
import org.jetbrains.vuejs.lang.html.VueParserTest
import org.jetbrains.vuejs.libraries.LibrariesTestSuite
import org.jetbrains.vuejs.linters.tslint.VueESLintHighlightingTest
import org.jetbrains.vuejs.linters.tslint.VueTypeScriptWithTslintTest
import org.jetbrains.vuejs.pug.PugTemplateTest
import org.jetbrains.vuejs.pug.VuePugFoldingTest
import org.jetbrains.vuejs.service.VueTypeScriptServiceTest
import org.junit.runner.RunWith
import org.junit.runners.Suite

@RunWith(Suite::class)
@Suite.SuiteClasses(
  VueTestCommons::class,
  VueJSParserTest::class,
  VueLexerTest::class,
  VueHighlightingLexerTest::class,
  VueIndexerTest::class,
  VueParserTest::class,
  VueCompletionTest::class,
  VueCommenterTest::class,
  VueHighlightingTest::class,
  VueControlFlowTest::class,
  VueComponentTest::class,
  VueTypedHandlerTest::class,
  VueAttributeNameParserTest::class,
  VueResolveTest::class,
  VueFindUsagesTest::class,
  VueRenameTest::class,
  VueCssClassTest::class,
  VueParameterInfoTest::class,
  VueOptimizeImportTest::class,
  VueLiveTemplatesTest::class,
  VueIntentionsTest::class,
  VueInjectionTest::class,
  VueFormatterTest::class,
  VueRearrangerTest::class,
  VueExternalFilesLinkingTest::class,
  VueExtractComponentTest::class,
  VueDocumentationTest::class,
  VueWebTypesDocumentationTest::class,
  VueCreateTsVariableTest::class,
  VueCopyrightTest::class,
  VueAutoPopupTest::class,
  VueEmmetTest::class,
  VueTypeResolveTest::class,
  LibrariesTestSuite::class,
  PugTemplateTest::class,
  VuePugFoldingTest::class,
  VueModuleImportTest::class,
  VueCopyPasteTest::class,
  VueMoveModuleMemberTest::class,
  VueTypeScriptDuplicateTest::class,
  VueIntroduceVariableTest::class,
  VueTypeScriptLineMarkersTest::class,
  VueESLintHighlightingTest::class,
  VueTypeScriptServiceTest::class,
  VueMoveTest::class,
  VueTypeScriptWithTslintTest::class,
  VueTypeScriptHighlightingTest::class,
)
class VueTestSuite
