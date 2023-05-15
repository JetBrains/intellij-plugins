// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2;

import org.angular2.cli.AngularConfigTest;
import org.angular2.cli.BlueprintListTest;
import org.angular2.codeInsight.*;
import org.angular2.codeInsight.template.Angular2LiveTemplateTest;
import org.angular2.css.CssClassTest;
import org.angular2.css.CssCompletionTest;
import org.angular2.css.CssInspectionsTest;
import org.angular2.editor.Angular2CopyPasteTest;
import org.angular2.editor.Angular2ParameterHintsTest;
import org.angular2.editor.Angular2PipeParameterInfoTest;
import org.angular2.editor.ComponentDeclarationNavigationTest;
import org.angular2.formatting.FormattingTest;
import org.angular2.inspections.*;
import org.angular2.lang.expr.lexer.Angular2LexerSpecTest;
import org.angular2.lang.expr.lexer.Angular2LexerTest;
import org.angular2.lang.expr.parser.Angular2ParserSpecTest;
import org.angular2.lang.expr.parser.Angular2ParserTest;
import org.angular2.lang.html.highlighting.Angular2HtmlHighlightingTest;
import org.angular2.lang.html.index.Angular2HtmlIndexerTest;
import org.angular2.lang.html.lexer.Angular2HtmlLexerSpecTest;
import org.angular2.lang.html.lexer.Angular2HtmlLexerTest;
import org.angular2.lang.html.parser.Angular2HtmlParsingTest;
import org.angular2.lang.selector.Angular2DirectiveSimpleSelectorSpecTest;
import org.angular2.metadata.IvyMetadataTest;
import org.angular2.metadata.JsonMetadataTest;
import org.angular2.navigation.AngularFindUsagesTest;
import org.angular2.navigation.GotoRelatedTest;
import org.angular2.navigation.GotoSymbolTest;
import org.angular2.refactoring.*;
import org.angular2.resharper.Angular2ReSharperTestSuite;
import org.angular2.svg.AngularSvgTest;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({
  BlueprintListTest.class,
  Angular2LexerSpecTest.class,
  Angular2LexerTest.class,
  Angular2ParserSpecTest.class,
  Angular2ParserTest.class,
  Angular2HtmlLexerSpecTest.class,
  Angular2HtmlLexerTest.class,
  Angular2HtmlHighlightingTest.class,
  Angular2HtmlIndexerTest.class,
  Angular2HtmlParsingTest.class,
  Angular2DirectiveSimpleSelectorSpecTest.class,
  Angular2DocumentationTest.class,
  Angular2CompletionTest.class,
  Angular2AttributesTest.class,
  ContextTest.class,
  InjectionsTest.class,
  InspectionsTest.class,
  IntentionsTest.class,
  NgForTest.class,
  NgContentSelectorsTest.class,
  NgTemplateLetTest.class,
  PipesTest.class,
  ScopesTest.class,
  TagsTest.class,
  ModulesTest.class,
  FrameworkHandlerTest.class,
  FormattingTest.class,
  Angular2TemplateInspectionsTest.class,
  Angular2DecoratorInspectionsTest.class,
  Angular2PipeParameterInfoTest.class,
  Angular2ParameterHintsTest.class,
  Angular2ExpressionTypesInspectionTest.class,
  Angular2SuppressionsTest.class,
  Angular2NgModuleImportQuickFixesTest.class,
  AngularChangeSignatureTest.class,
  Angular2InaccessibleMemberAotQuickFixesTest.class,
  Angular2InaccessibleMemberAotInspectionTest.class,
  Angular2ControlFlowTest.class,
  Angular2CopyPasteTest.class,
  AngularFindUsagesTest.class,
  DirectiveRenameTest.class,
  MoveTest.class,
  RenameTest.class,
  Angular2ExtractComponentTest.class,
  ComponentDeclarationNavigationTest.class,
  JsonMetadataTest.class,
  IvyMetadataTest.class,
  NgMaterialTest.class,
  AngularSvgTest.class,
  GotoSymbolTest.class,
  GotoRelatedTest.class,
  CssClassTest.class,
  CssCompletionTest.class,
  CssInspectionsTest.class,
  Angular2ReSharperTestSuite.class,
  AngularConfigTest.class,
  Angular2LiveTemplateTest.class
})
public class Angular2TestSuite {
}
