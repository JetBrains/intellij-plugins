// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.

@file:Suppress("DEPRECATION")

package org.angular2

import org.angular2.cli.Angular2BlueprintListTest
import org.angular2.cli.Angular2CliContextTest
import org.angular2.cli.Angular2ConfigTest
import org.angular2.codeInsight.Angular2CompletionTest
import org.angular2.codeInsight.Angular2ControlFlowTest
import org.angular2.codeInsight.Angular2CopyPasteTest
import org.angular2.codeInsight.Angular2DocumentationTest
import org.angular2.codeInsight.Angular2EditorTest
import org.angular2.codeInsight.Angular2FormattingTest
import org.angular2.codeInsight.Angular2HighlightingTest
import org.angular2.codeInsight.Angular2IntentionsAndQuickFixesTest
import org.angular2.codeInsight.Angular2LiveTemplateTest
import org.angular2.codeInsight.Angular2ModelStructureTest
import org.angular2.codeInsight.Angular2ParameterHintsTest
import org.angular2.codeInsight.Angular2ParameterInfoTest
import org.angular2.codeInsight.Angular2RearrangerTest
import org.angular2.codeInsight.Angular2ServiceInlayHintsTest
import org.angular2.codeInsight.Angular2StructureViewTest
import org.angular2.codeInsight.Angular2TemplateTranspilerTest
import org.angular2.codeInsight.Angular2UsageHighlightingTest
import org.angular2.codeInsight.deprecated.Angular2AttributesTest
import org.angular2.codeInsight.deprecated.Angular2ComponentDeclarationNavigationTest
import org.angular2.codeInsight.deprecated.Angular2ContextTest
import org.angular2.codeInsight.deprecated.Angular2FrameworkHandlerTest
import org.angular2.codeInsight.deprecated.Angular2InjectionsTest
import org.angular2.codeInsight.deprecated.Angular2IvyModelTest
import org.angular2.codeInsight.deprecated.Angular2JsonModelTest
import org.angular2.codeInsight.deprecated.Angular2NgContentSelectorsTest
import org.angular2.codeInsight.deprecated.Angular2NgForTest
import org.angular2.codeInsight.deprecated.Angular2NgMaterialTest
import org.angular2.codeInsight.deprecated.Angular2NgTemplateLetTest
import org.angular2.codeInsight.deprecated.Angular2PipesTest
import org.angular2.codeInsight.deprecated.Angular2ScopesTest
import org.angular2.codeInsight.deprecated.Angular2SvgTest
import org.angular2.codeInsight.deprecated.Angular2TagsTest
import org.angular2.codeInsight.inspections.Angular2BlockInspectionsTest
import org.angular2.codeInsight.inspections.Angular2CompilerFlagsTest
import org.angular2.codeInsight.inspections.Angular2DecoratorInspectionsTest
import org.angular2.codeInsight.inspections.Angular2ExpressionTypesInspectionTest
import org.angular2.codeInsight.inspections.Angular2ExpressionTypesInspectionWithoutServiceTest
import org.angular2.codeInsight.inspections.Angular2InaccessibleMemberAotQuickFixesTest
import org.angular2.codeInsight.inspections.Angular2NgModuleImportQuickFixesTest
import org.angular2.codeInsight.inspections.Angular2OptimizedImageDirectiveInspectionTest
import org.angular2.codeInsight.inspections.Angular2SuppressionsTest
import org.angular2.codeInsight.inspections.Angular2TemplateInspectionsTest
import org.angular2.codeInsight.inspections.Angular2TsInspectionsTest
import org.angular2.codeInsight.navigation.Angular2FindUsagesTest
import org.angular2.codeInsight.navigation.Angular2GotoDeclarationTest
import org.angular2.codeInsight.navigation.Angular2GotoRelatedTest
import org.angular2.codeInsight.navigation.Angular2GotoSymbolTest
import org.angular2.codeInsight.navigation.Angular2JumpToSourceTest
import org.angular2.codeInsight.navigation.Angular2TscGotoDeclarationTest
import org.angular2.codeInsight.refactoring.Angular2ChangeSignatureTest
import org.angular2.codeInsight.refactoring.Angular2ExtractComponentTest
import org.angular2.codeInsight.refactoring.Angular2InlineTest
import org.angular2.codeInsight.refactoring.Angular2MoveTest
import org.angular2.codeInsight.refactoring.Angular2RenameTest
import org.angular2.codeInsight.refactoring.Angular2TscRenameTest
import org.angular2.css.Angular2CssClassTest
import org.angular2.css.Angular2CssCompletionTest
import org.angular2.css.Angular2CssHighlightingTest
import org.angular2.css.Angular2CssInspectionsTest
import org.angular2.css.Angular2CssRenameTest
import org.angular2.css.Angular2CssUsageHighlightingTest
import org.angular2.lang.expr.Angular20LexerTest
import org.angular2.lang.expr.Angular20ParserTest
import org.angular2.lang.expr.Angular2LexerSpecTest
import org.angular2.lang.expr.Angular2LexerTest
import org.angular2.lang.expr.Angular2ParserSpecTest
import org.angular2.lang.expr.Angular2ParserTest
import org.angular2.lang.html.Angular17HtmlHighlightingLexerTest
import org.angular2.lang.html.Angular17HtmlIndexerTest
import org.angular2.lang.html.Angular17HtmlLexerTest
import org.angular2.lang.html.Angular17HtmlParsingTest
import org.angular2.lang.html.Angular181HtmlHighlightingLexerTest
import org.angular2.lang.html.Angular181HtmlIndexerTest
import org.angular2.lang.html.Angular181HtmlLexerTest
import org.angular2.lang.html.Angular181HtmlParsingTest
import org.angular2.lang.html.Angular20HtmlHighlightingLexerTest
import org.angular2.lang.html.Angular20HtmlIndexerTest
import org.angular2.lang.html.Angular20HtmlLexerTest
import org.angular2.lang.html.Angular20HtmlParsingTest
import org.angular2.lang.html.Angular2HtmlHighlightingLexerTest
import org.angular2.lang.html.Angular2HtmlIndexerTest
import org.angular2.lang.html.Angular2HtmlLexerSpecTest
import org.angular2.lang.html.Angular2HtmlLexerTest
import org.angular2.lang.html.Angular2HtmlParsingTest
import org.angular2.lang.html.Angular2SemanticHighlightingTest
import org.angular2.lang.selector.Angular2DirectiveSimpleSelectorSpecTest
import org.angular2.library.forms.Angular2FormsTestSuite
import org.angular2.resharper.Angular2ReSharperTestSuite
import org.junit.runner.RunWith
import org.junit.runners.Suite

@RunWith(Suite::class)
@Suite.SuiteClasses(
  Angular2BlueprintListTest::class,
  Angular2LexerSpecTest::class,
  Angular2LexerTest::class,
  Angular20LexerTest::class,
  Angular2ParserSpecTest::class,
  Angular2ParserTest::class,
  Angular20ParserTest::class,
  Angular2HtmlLexerSpecTest::class,
  Angular2HtmlLexerTest::class,
  Angular2HtmlHighlightingLexerTest::class,
  Angular2HtmlIndexerTest::class,
  Angular17HtmlLexerTest::class,
  Angular17HtmlHighlightingLexerTest::class,
  Angular17HtmlIndexerTest::class,
  Angular181HtmlLexerTest::class,
  Angular181HtmlHighlightingLexerTest::class,
  Angular181HtmlIndexerTest::class,
  Angular20HtmlLexerTest::class,
  Angular20HtmlHighlightingLexerTest::class,
  Angular20HtmlIndexerTest::class,
  Angular2HtmlParsingTest::class,
  Angular17HtmlParsingTest::class,
  Angular181HtmlParsingTest::class,
  Angular20HtmlParsingTest::class,
  Angular2SemanticHighlightingTest::class,
  Angular2DirectiveSimpleSelectorSpecTest::class,
  Angular2TemplateTranspilerTest::class,
  Angular2DocumentationTest::class,
  Angular2HighlightingTest::class,
  Angular2CompletionTest::class,
  Angular2ContextTest::class,
  Angular2TsInspectionsTest::class,
  Angular2ModelStructureTest::class,
  Angular2FormattingTest::class,
  Angular2TemplateInspectionsTest::class,
  Angular2DecoratorInspectionsTest::class,
  Angular2ParameterInfoTest::class,
  Angular2ParameterHintsTest::class,
  Angular2ExpressionTypesInspectionTest::class,
  Angular2ExpressionTypesInspectionWithoutServiceTest::class,
  Angular2BlockInspectionsTest::class,
  Angular2OptimizedImageDirectiveInspectionTest::class,
  Angular2CompilerFlagsTest::class,
  Angular2SuppressionsTest::class,
  Angular2NgModuleImportQuickFixesTest::class,
  Angular2ChangeSignatureTest::class,
  Angular2InaccessibleMemberAotQuickFixesTest::class,
  Angular2ControlFlowTest::class,
  Angular2CopyPasteTest::class,
  Angular2FindUsagesTest::class,
  Angular2UsageHighlightingTest::class,
  Angular2IntentionsAndQuickFixesTest::class,
  Angular2ServiceInlayHintsTest::class,
  Angular2MoveTest::class,
  Angular2RenameTest::class,
  Angular2TscRenameTest::class,
  Angular2InlineTest::class,
  Angular2ConfigTest::class,
  Angular2EditorTest::class,
  Angular2CliContextTest::class,
  Angular2LiveTemplateTest::class,
  Angular2ExtractComponentTest::class,
  Angular2StructureViewTest::class,
  Angular2GotoSymbolTest::class,
  Angular2GotoRelatedTest::class,
  Angular2GotoDeclarationTest::class,
  Angular2TscGotoDeclarationTest::class,
  Angular2RearrangerTest::class,
  Angular2JumpToSourceTest::class,
  Angular2CssClassTest::class,
  Angular2CssCompletionTest::class,
  Angular2CssInspectionsTest::class,
  Angular2CssRenameTest::class,
  Angular2ReSharperTestSuite::class,
  Angular2CssHighlightingTest::class,
  Angular2CssUsageHighlightingTest::class,

  // Library suites follow
  Angular2FormsTestSuite::class,

  // Deprecated tests follow
  Angular2AttributesTest::class,
  Angular2InjectionsTest::class,
  Angular2NgForTest::class,
  Angular2NgContentSelectorsTest::class,
  Angular2NgTemplateLetTest::class,
  Angular2PipesTest::class,
  Angular2ScopesTest::class,
  Angular2TagsTest::class,
  Angular2FrameworkHandlerTest::class,
  Angular2ComponentDeclarationNavigationTest::class,
  Angular2JsonModelTest::class,
  Angular2IvyModelTest::class,
  Angular2NgMaterialTest::class,
  Angular2SvgTest::class,
)
class Angular2TestSuite
