// Copyright 2000-2018 JetBrains s.r.o.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
package org.jetbrains.vuejs.lang

import org.jetbrains.vuejs.lang.VueWebTypesDocumentationTest
import org.jetbrains.vuejs.lang.expr.VueJSParserTest
import org.jetbrains.vuejs.lang.html.VueHighlightingLexerTest
import org.jetbrains.vuejs.lang.html.VueIndexerTest
import org.jetbrains.vuejs.lang.html.VueLexerTest
import org.jetbrains.vuejs.lang.html.VueParserTest
import org.jetbrains.vuejs.libraries.LibrariesTestSuite
import org.jetbrains.vuejs.pug.PugTemplateTest
import org.jetbrains.vuejs.pug.VuePugFoldingTest
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
  VueTypedHandlerTest::class,
  VueAttributeNameParserTest::class,
  VueResolveTest::class,
  VueFindUsagesTest::class,
  VueRenameTest::class,
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
  VueTypeScriptHighlightingTest::class
)
class VueTestSuite 