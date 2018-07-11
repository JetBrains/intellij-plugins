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
package org.jetbrains.vuejs.language;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.jetbrains.vuejs.language.parser.VueHighlightingLexerTest;
import org.jetbrains.vuejs.language.parser.VueLexerTest;

public class VueTestSuite extends TestCase {
  public static Test suite() {
    final TestSuite testSuite = new TestSuite(VueTestSuite.class.getSimpleName());
    testSuite.addTestSuite(VueTypedHandlerTest.class);
    testSuite.addTestSuite(VueTypeScriptHighlightingTest.class);
    testSuite.addTestSuite(VueResolveTest.class);
    testSuite.addTestSuite(VueRenameTest.class);
    testSuite.addTestSuite(VueParameterInfoTest.class);
    testSuite.addTestSuite(VueOptimizeImportTest.class);
    testSuite.addTestSuite(VueLiveTemplatesTest.class);
    testSuite.addTestSuite(VueIntentionsTest.class);
    testSuite.addTestSuite(VueInjectionTest.class);
    testSuite.addTestSuite(VueHighlightingTest.class);
    testSuite.addTestSuite(VueFormatterTest.class);
    testSuite.addTestSuite(VueExtractComponentTest.class);
    testSuite.addTestSuite(VueDocumentationTest.class);
    testSuite.addTestSuite(VueCreateTsVariableTest.class);
    testSuite.addTestSuite(VueCopyrightTest.class);
    testSuite.addTestSuite(VueCompletionTest.class);
    testSuite.addTestSuite(VueAutoPopupTest.class);
    testSuite.addTestSuite(VueHighlightingLexerTest.class);
    testSuite.addTestSuite(VueLexerTest.class);
    testSuite.addTestSuite(VueEmmetTest.class);

    return testSuite;
  }
}
