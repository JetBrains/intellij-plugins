// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.

@file:Suppress("DEPRECATION")

package org.angular2

import org.angular2.cli.Angular2BlueprintListTest
import org.angular2.cli.Angular2CliContextTest
import org.angular2.cli.Angular2ConfigTest
import org.angular2.codeInsight.*
import org.angular2.codeInsight.deprecated.*
import org.angular2.codeInsight.deprecated.Angular2ComponentDeclarationNavigationTest
import org.angular2.codeInsight.inspections.*
import org.angular2.codeInsight.navigation.Angular2FindUsagesTest
import org.angular2.codeInsight.navigation.Angular2GotoDeclarationTest
import org.angular2.codeInsight.navigation.Angular2GotoRelatedTest
import org.angular2.codeInsight.navigation.Angular2GotoSymbolTest
import org.angular2.codeInsight.refactoring.*
import org.angular2.css.Angular2CssClassTest
import org.angular2.css.Angular2CssCompletionTest
import org.angular2.css.Angular2CssHighlightingTest
import org.angular2.css.Angular2CssInspectionsTest
import org.angular2.css.Angular2CssRenameTest
import org.angular2.css.Angular2CssUsageHighlightingTest
import org.angular2.lang.expr.Angular2LexerSpecTest
import org.angular2.lang.expr.Angular2LexerTest
import org.angular2.lang.expr.Angular2ParserSpecTest
import org.angular2.lang.expr.Angular2ParserTest
import org.angular2.lang.html.*
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
  Angular2ParserSpecTest::class,
  Angular2ParserTest::class,
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
  Angular2HtmlParsingTest::class,
  Angular17HtmlParsingTest::class,
  Angular181HtmlParsingTest::class,
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
