// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2;

import org.angular2.cli.BlueprintListTest;
import org.angular2.codeInsight.*;
import org.angular2.editor.ComponentDeclarationNavigationTest;
import org.angular2.formatting.FormattingTest;
import org.angular2.inspections.EmptyEventHandlerInspectionTest;
import org.angular2.lang.expr.lexer.Angular2LexerSpecTest;
import org.angular2.lang.expr.lexer.Angular2LexerTest;
import org.angular2.lang.expr.parser.Angular2ParserSpecTest;
import org.angular2.lang.expr.parser.Angular2ParserTest;
import org.angular2.lang.html.highlighting.Angular2HtmlHighlightingTest;
import org.angular2.lang.html.lexer.Angular2HtmlLexerSpecTest;
import org.angular2.lang.html.lexer.Angular2HtmlLexerTest;
import org.angular2.lang.html.parser.Angular2HtmlParsingTest;
import org.angular2.lang.selector.Angular2DirectiveSimpleSelectorSpecTest;
import org.angular2.metadata.MetadataTest;
import org.angular2.navigation.GotoSymbolTest;
import org.angular2.refactoring.AngularChangeSignatureTest;
import org.angular2.refactoring.DirectiveRenameTest;
import org.angular2.refactoring.RenameTest;
import org.angular2.resharper.Angular2ReSharperTestSuite;
import org.angular2.service.Angular2ServiceTest;
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
  Angular2HtmlParsingTest.class,
  Angular2DirectiveSimpleSelectorSpecTest.class,
  AttributesTest.class,
  ContextTest.class,
  InjectionsTest.class,
  InspectionsTest.class,
  IntentionsTest.class,
  NgForTest.class,
  PipesTest.class,
  ScopesTest.class,
  TagsTest.class,
  FormattingTest.class,
  EmptyEventHandlerInspectionTest.class,
  AngularChangeSignatureTest.class,
  DirectiveRenameTest.class,
  RenameTest.class,
  ComponentDeclarationNavigationTest.class,
  MetadataTest.class,
  NgMaterialTest.class,
  Angular2ServiceTest.class,
  GotoSymbolTest.class,
  Angular2ReSharperTestSuite.class,
})
public class Angular2TestSuite {
}
