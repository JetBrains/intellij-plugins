// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.intellij.flex;

import com.intellij.flex.bc.FlexCompilerConfigTest;
import com.intellij.flex.bc.FlexProjectConfigTest;
import com.intellij.flex.codeInsight.*;
import com.intellij.flex.completion.*;
import com.intellij.flex.editor.FlexEditorTest;
import com.intellij.flex.editor.FlexExtendSelectionTest;
import com.intellij.flex.editor.FlexLiveTemplatesTest;
import com.intellij.flex.flashBuilder.FlashBuilderImportTest;
import com.intellij.flex.flexunit.codeInsight.FlexUnitCompletionTest;
import com.intellij.flex.flexunit.codeInsight.FlexUnitConfigurationTest;
import com.intellij.flex.flexunit.codeInsight.FlexUnitHighlightingTest;
import com.intellij.flex.formatter.ActionScriptFormatterTest;
import com.intellij.flex.generate.ActionScriptGenerateTest;
import com.intellij.flex.highlighting.*;
import com.intellij.flex.imports.FlexAutoImportsTest;
import com.intellij.flex.imports.FlexOptimizeImportsTest;
import com.intellij.flex.intentions.*;
import com.intellij.flex.parser.ActionScriptParsingTest;
import com.intellij.flex.parser.FlexImporterTest;
import com.intellij.flex.projectView.FlexProjectViewTest;
import com.intellij.flex.refactoring.*;
import com.intellij.flex.resolver.ActionScriptResolveTest;
import com.intellij.flex.resolver.FlexCssNavigationTest;
import com.intellij.flex.uml.FlashUmlTest;
import junit.framework.Test;
import junit.framework.TestSuite;

public final class FlexTestSuite {

  public static Test suite() {
    final TestSuite testSuite = new TestSuite(FlexTestSuite.class.getSimpleName());

    // com.intellij.flex.bc.*
    testSuite.addTestSuite(FlexCompilerConfigTest.class);
    testSuite.addTestSuite(FlexProjectConfigTest.class);

    // com.intellij.flex.codeInsight.*
    testSuite.addTestSuite(ActionScriptRearrangerTest.class);
    testSuite.addTestSuite(ActionScriptStatementMoverTest.class);
    testSuite.addTestSuite(FlexDocumentationTest.class);
    testSuite.addTestSuite(FlexFindUsagesTest.class);
    testSuite.addTestSuite(FlexGotoImplementationsTest.class);
    testSuite.addTestSuite(FlexHierarchyTest.class);
    testSuite.addTestSuite(FlexNavigationTest.class);
    testSuite.addTestSuite(FlexStructureViewTest.class);

    // com.intellij.flex.completion.*
    testSuite.addTestSuite(ActionScriptCompletionInTextFieldTest.class);
    testSuite.addTestSuite(ActionScriptCompletionTest.class);
    testSuite.addTestSuite(FlexAutoPopupTest.class);
    testSuite.addTestSuite(FlexCompletionInUmlTextFieldsTest.class);
    testSuite.addTestSuite(FlexCompletionTest.class);
    testSuite.addTestSuite(FlexCssCompletionTest.class);

    //com.intellij.flex.editor.*
    testSuite.addTestSuite(FlexEditorTest.class);
    testSuite.addTestSuite(FlexExtendSelectionTest.class);
    testSuite.addTestSuite(FlexLiveTemplatesTest.class);

    //com.intellij.flex.flashBuilder.*
    testSuite.addTestSuite(FlashBuilderImportTest.class);

    //com.intellij.flex.flexunit.*
    testSuite.addTestSuite(FlexUnitConfigurationTest.class);
    testSuite.addTestSuite(FlexUnitHighlightingTest.class);
    testSuite.addTestSuite(FlexUnitCompletionTest.class);

    //com.intellij.flex.formatter.*
    testSuite.addTestSuite(ActionScriptFormatterTest.class);

    //com.intellij.flex.generate.*
    testSuite.addTestSuite(ActionScriptGenerateTest.class);

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
    testSuite.addTestSuite(GlobalFlexHighlightingTest.class);
    testSuite.addTestSuite(SwfHighlightingTest.class);

    //com.intellij.flex.imports.*
    testSuite.addTestSuite(FlexAutoImportsTest.class);
    testSuite.addTestSuite(FlexOptimizeImportsTest.class);

    //com.intellij.flex.intentions.*
    testSuite.addTestSuite(ActionScriptIntentionActionTest.class);
    testSuite.addTestSuite(CreateASFunctionIntentionTest.class);
    testSuite.addTestSuite(CreateASVariableIntentionTest.class);
    testSuite.addTestSuite(FlexConvertToLocalTest.class);
    testSuite.addTestSuite(ImportJSClassIntentionTest.class);

    //com.intellij.flex.maven.*
    //testSuite.addTestSuite(Flexmojos3ImporterTest.class);
    //testSuite.addTestSuite(Flexmojos4ImporterTest.class);
    //testSuite.addTestSuite(ImportingNonJavaModulesTest.class);
    //testSuite.addTestSuite(NonJarDependenciesImportingTest.class);

    //com.intellij.flex.parser.*
    testSuite.addTestSuite(ActionScriptParsingTest.class);
    testSuite.addTestSuite(FlexImporterTest.class);

    //com.intellij.flex.projectView.*
    testSuite.addTestSuite(FlexProjectViewTest.class);

    //com.intellij.flex.refactoring.*
    testSuite.addTestSuite(ActionScriptIntroduceVariableTest.class);
    testSuite.addTestSuite(ActionScriptInPlaceIntroduceVariableTest.class);
    testSuite.addTestSuite(FlexChangeSignatureTest.class);
    testSuite.addTestSuite(FlexExtractFunctionTest.class);
    testSuite.addTestSuite(FlexExtractSuperTest.class);
    testSuite.addTestSuite(FlexInlineFunctionTest.class);
    testSuite.addTestSuite(FlexInlineVariableTest.class);
    testSuite.addTestSuite(FlexIntroduceConstantTest.class);
    testSuite.addTestSuite(FlexIntroduceFieldTest.class);
    testSuite.addTestSuite(FlexMoveInnerClassTest.class);
    testSuite.addTestSuite(FlexMoveMembersTest.class);
    testSuite.addTestSuite(FlexMoveTest.class);
    testSuite.addTestSuite(FlexPullUpTest.class);
    testSuite.addTestSuite(FlexPushDownTest.class);
    testSuite.addTestSuite(FlexRenameTest.class);

    //com.intellij.flex.resolver.*
    testSuite.addTestSuite(ActionScriptResolveTest.class);
    testSuite.addTestSuite(FlexCssNavigationTest.class);

    //com.intellij.flex.uml.*
    testSuite.addTestSuite(FlashUmlTest.class);

    return testSuite;
  }
}
