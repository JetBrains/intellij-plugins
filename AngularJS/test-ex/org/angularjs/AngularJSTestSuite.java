// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angularjs;

import org.angular2.Angular2TestSuite;
import org.angularjs.codeInsight.*;
import org.angularjs.codeInsight.messageFormat.AngularMessageFormatAnnotatorTest;
import org.angularjs.diagrams.DiagramsTest;
import org.angularjs.editor.AngularTypedHandlerTest;
import org.angularjs.findUsages.FindUsagesTest;
import org.angularjs.index.AngularDirectiveCommentParsingTest;
import org.angularjs.lang.lexer.AngularJSLexerTest;
import org.angularjs.lang.parser.AngularJSParserTest;
import org.angularjs.refactoring.DirectiveRenameTest;
import org.angularjs.resharper.AngularJSReSharperTestSuite;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;


@RunWith(Suite.class)
@Suite.SuiteClasses({
  AngularJSLexerTest.class,
  AngularJSParserTest.class,
  AttributesTest.class,
  DependencyInjectionTest.class,
  DocumentationTest.class,
  FiltersTest.class,
  InjectionsTest.class,
  NgRepeatTest.class,
  RoutingTest.class,
  TagsTest.class,
  DirectivesTest.class,
  AngularTypedHandlerTest.class,
  FindUsagesTest.class,
  AngularDirectiveCommentParsingTest.class,
  AngularJSInjectorMatchingEndFinderTest.class,
  AngularMessageFormatAnnotatorTest.class,
  DirectiveRenameTest.class,
  AngularUiRouterTest.class,
  ComponentsTest.class,
  NgDocTest.class,
  DiagramsTest.class,
  AngularJSReSharperTestSuite.class,
  Angular2TestSuite.class
})
public class AngularJSTestSuite {

}
