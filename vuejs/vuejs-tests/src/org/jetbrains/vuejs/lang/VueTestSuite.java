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
package org.jetbrains.vuejs.lang;

import org.jetbrains.vuejs.lang.parser.VueHighlightingLexerTest;
import org.jetbrains.vuejs.lang.parser.VueLexerTest;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({
  VueTypedHandlerTest.class,
  VueTypeScriptHighlightingTest.class,
  VueResolveTest.class,
  VueRenameTest.class,
  VueParameterInfoTest.class,
  VueOptimizeImportTest.class,
  VueLiveTemplatesTest.class,
  VueIntentionsTest.class,
  VueInjectionTest.class,
  VueHighlightingTest.class,
  VueFormatterTest.class,
  VueExtractComponentTest.class,
  VueDocumentationTest.class,
  VueCreateTsVariableTest.class,
  VueCopyrightTest.class,
  VueCompletionTest.class,
  VueAutoPopupTest.class,
  VueHighlightingLexerTest.class,
  VueLexerTest.class,
  VueEmmetTest.class,
  VueModuleImportTest.class
})
public class VueTestSuite {
}
