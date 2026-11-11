// Copyright 2000-2025 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.intellij.flex;

import com.intellij.flex.codeInsight.ActionScriptStatementMoverTest;
import com.intellij.flex.codeInsight.FlexGotoImplementationsTest;
import com.intellij.flex.codeInsight.FlexHierarchyTest;
import com.intellij.flex.codeInsight.FlexNavigationTest;
import com.intellij.flex.completion.ActionScriptCompletionInTextFieldTest;
import com.intellij.flex.completion.FlexCompletionInUmlTextFieldsTest;
import com.intellij.flex.editor.FlexLiveTemplatesTest;
import com.intellij.flex.flashBuilder.FlashBuilderImportTest;
import com.intellij.flex.flexunit.codeInsight.FlexUnitHighlightingTest;
import com.intellij.flex.highlighting.ActionScriptHighlightingInTextFieldTest;
import com.intellij.flex.highlighting.ActionScriptHighlightingTest;
import com.intellij.flex.highlighting.ActionScriptLineMarkersTest;
import com.intellij.flex.highlighting.ActionScriptRegExpHighlightingTest;
import com.intellij.flex.highlighting.ActionScriptStubsTest;
import com.intellij.flex.highlighting.FlexColorAnnotatorTest;
import com.intellij.flex.highlighting.FlexHighlightingPerformanceTest;
import com.intellij.flex.highlighting.FlexHighlightingTest;
import com.intellij.flex.highlighting.FlexLineMarkersTest;
import com.intellij.flex.highlighting.FlexScopeTest;
import com.intellij.flex.highlighting.SwfHighlightingTest;
import com.intellij.flex.imports.FlexAutoImportsTest;
import com.intellij.flex.imports.FlexOptimizeImportsTest;
import com.intellij.flex.parser.FlexImporterTest;
import com.intellij.flex.refactoring.ActionScriptInPlaceIntroduceVariableTest;
import com.intellij.flex.resolver.FlexCssNavigationTest;
import com.intellij.testFramework.TestFrameworkUtil;
import junit.framework.Test;
import junit.framework.TestSuite;
import org.junit.Ignore;

@Ignore("Manual only")
public final class FlexTestSuite {

  public static Test suite() {
    final TestSuite testSuite = new TestSuite(FlexTestSuite.class.getSimpleName());

    // com.intellij.flex.codeInsight.*
    testSuite.addTestSuite(ActionScriptStatementMoverTest.class);
    testSuite.addTestSuite(FlexGotoImplementationsTest.class);
    testSuite.addTestSuite(FlexHierarchyTest.class);
    testSuite.addTestSuite(FlexNavigationTest.class);

    // com.intellij.flex.completion.*
    testSuite.addTestSuite(ActionScriptCompletionInTextFieldTest.class);
    testSuite.addTestSuite(FlexCompletionInUmlTextFieldsTest.class);

    //com.intellij.flex.editor.*
    testSuite.addTestSuite(FlexLiveTemplatesTest.class);

    //com.intellij.flex.flashBuilder.*
    testSuite.addTestSuite(FlashBuilderImportTest.class);

    //com.intellij.flex.flexunit.*
    testSuite.addTestSuite(FlexUnitHighlightingTest.class);

    //com.intellij.flex.formatter.*

    //com.intellij.flex.generate.*

    //com.intellij.flex.highlighting.*
    testSuite.addTestSuite(ActionScriptHighlightingInTextFieldTest.class);
    testSuite.addTestSuite(ActionScriptHighlightingTest.class);
    testSuite.addTestSuite(ActionScriptLineMarkersTest.class);
    testSuite.addTestSuite(ActionScriptRegExpHighlightingTest.class);
    testSuite.addTestSuite(ActionScriptStubsTest.class);
    testSuite.addTestSuite(FlexColorAnnotatorTest.class);
    testSuite.addTestSuite(FlexHighlightingPerformanceTest.class);
    testSuite.addTestSuite(FlexHighlightingTest.class);
    testSuite.addTestSuite(FlexLineMarkersTest.class);
    testSuite.addTestSuite(FlexScopeTest.class);
    testSuite.addTestSuite(SwfHighlightingTest.class);

    //com.intellij.flex.imports.*
    testSuite.addTestSuite(FlexAutoImportsTest.class);
    testSuite.addTestSuite(FlexOptimizeImportsTest.class);

    //com.intellij.flex.intentions.*

    //com.intellij.flex.maven.*
    //testSuite.addTestSuite(Flexmojos3ImporterTest.class);
    //testSuite.addTestSuite(Flexmojos4ImporterTest.class);
    //testSuite.addTestSuite(ImportingNonJavaModulesTest.class);
    //testSuite.addTestSuite(NonJarDependenciesImportingTest.class);

    //com.intellij.flex.parser.*
    testSuite.addTestSuite(FlexImporterTest.class);

    //com.intellij.flex.refactoring.*
    testSuite.addTestSuite(ActionScriptInPlaceIntroduceVariableTest.class);

    //com.intellij.flex.resolver.*
    testSuite.addTestSuite(FlexCssNavigationTest.class);

    return TestFrameworkUtil.flattenSuite(testSuite);
  }
}
