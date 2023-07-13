// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2

import org.angular2.cli.Angular2ConfigTest
import org.angular2.cli.Angular2BlueprintListTest
import org.angular2.cli.Angular2ContextTest
import org.angular2.codeInsight.*
import org.angular2.css.Angular2CssClassTest
import org.angular2.css.Angular2CssCompletionTest
import org.angular2.css.Angular2CssInspectionsTest
import org.angular2.editor.*
import org.angular2.formatting.Angular2FormattingTest
import org.angular2.inspections.*
import org.angular2.lang.expr.Angular2LexerSpecTest
import org.angular2.lang.expr.Angular2LexerTest
import org.angular2.lang.expr.Angular2ParserSpecTest
import org.angular2.lang.expr.Angular2ParserTest
import org.angular2.lang.html.*
import org.angular2.lang.selector.Angular2DirectiveSimpleSelectorSpecTest
import org.angular2.linters.tslint.Angular2TslintHighlightingTest
import org.angular2.metadata.Angular2IvyMetadataTest
import org.angular2.metadata.Angular2JsonMetadataTest
import org.angular2.navigation.Angular2FindUsagesTest
import org.angular2.navigation.Angular2GotoRelatedTest
import org.angular2.navigation.Angular2GotoSymbolTest
import org.angular2.refactoring.*
import org.angular2.resharper.Angular2ReSharperTestSuite
import org.angular2.svg.Angular2SvgTest
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
  Angular2HtmlParsingTest::class,
  Angular2DirectiveSimpleSelectorSpecTest::class,
  Angular2DocumentationTest::class,
  Angular2CompletionTest::class,
  Angular2AttributesTest::class,
  Angular2ContextTest::class,
  Angular2InjectionsTest::class,
  Angular2InspectionsTest::class,
  Angular2IntentionsTest::class,
  Angular2NgForTest::class,
  Angular2NgContentSelectorsTest::class,
  Angular2NgTemplateLetTest::class,
  Angular2PipesTest::class,
  Angular2ScopesTest::class,
  Angular2TagsTest::class,
  Angular2ModulesTest::class,
  Angular2FrameworkHandlerTest::class,
  Angular2FormattingTest::class,
  Angular2TemplateInspectionsTest::class,
  Angular2DecoratorInspectionsTest::class,
  Angular2PipeParameterInfoTest::class,
  Angular2ParameterHintsTest::class,
  Angular2ExpressionTypesInspectionTest::class,
  Angular2SuppressionsTest::class,
  Angular2NgModuleImportQuickFixesTest::class,
  Angular2ChangeSignatureTest::class,
  Angular2InaccessibleMemberAotQuickFixesTest::class,
  Angular2InaccessibleMemberAotInspectionTest::class,
  Angular2ControlFlowTest::class,
  Angular2MultiFileIntentionsTest::class,
  Angular2CopyPasteTest::class,
  Angular2FindUsagesTest::class,
  Angular2DirectiveRenameTest::class,
  Angular2MoveTest::class,
  Angular2RenameTest::class,
  Angular2ExtractComponentTest::class,
  Angular2ComponentDeclarationNavigationTest::class,
  Angular2JsonMetadataTest::class,
  Angular2IvyMetadataTest::class,
  Angular2NgMaterialTest::class,
  Angular2SvgTest::class,
  Angular2ComponentStructureViewTest::class,
  Angular2GotoSymbolTest::class,
  Angular2GotoRelatedTest::class,
  Angular2CssClassTest::class,
  Angular2CssCompletionTest::class,
  Angular2CssInspectionsTest::class,
  Angular2ReSharperTestSuite::class,
  Angular2ConfigTest::class,
  Angular2ContextTest::class,
  Angular2LiveTemplateTest::class,
)
class Angular2TestSuite
