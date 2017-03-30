package org.angularjs;

import junit.framework.Test;
import junit.framework.TestSuite;
import org.angularjs.codeInsight.*;
import org.angularjs.codeInsight.messageFormat.AngularMessageFormatAnnotatorTest;
import org.angularjs.editor.AngularTypedHandlerTest;
import org.angularjs.findUsages.FindUsagesTest;
import org.angularjs.html.Angular2HTMLParserTest;
import org.angularjs.index.AngularDirectiveCommentParsingTest;
import org.angularjs.inspections.EmptyEventHandlerInspectionTest;
import org.angularjs.lang.lexer.AngularJSLexerTest;
import org.angularjs.refactoring.DirectiveRenameTest;

public class AngularJSTestSuite {

  public static Test suite() {
    final TestSuite testSuite = new TestSuite(AngularJSTestSuite.class.getSimpleName());

    testSuite.addTestSuite(AngularJSLexerTest.class);
    //testSuite.addTestSuite(AngularJSParserTest.class); seems it can't handle @Test annotation
    testSuite.addTestSuite(Angular2HTMLParserTest.class);
    testSuite.addTestSuite(AttributesTest.class);
    testSuite.addTestSuite(DependencyInjectionTest.class);
    testSuite.addTestSuite(DocumentationTest.class);
    testSuite.addTestSuite(FiltersTest.class);
    testSuite.addTestSuite(InjectionsTest.class);
    testSuite.addTestSuite(NgRepeatTest.class);
    testSuite.addTestSuite(RoutingTest.class);
    testSuite.addTestSuite(TagsTest.class);
    testSuite.addTestSuite(AngularTypedHandlerTest.class);
    testSuite.addTestSuite(FindUsagesTest.class);
    testSuite.addTestSuite(AngularDirectiveCommentParsingTest.class);
    testSuite.addTestSuite(AngularJSInjectorMatchingEndFinderTest.class);
    testSuite.addTestSuite(AngularMessageFormatAnnotatorTest.class);
    testSuite.addTestSuite(EmptyEventHandlerInspectionTest.class);
    testSuite.addTestSuite(DirectiveRenameTest.class);
    testSuite.addTestSuite(ContextTest.class);
    testSuite.addTestSuite(AngularUiRouterTest.class);

    return testSuite;
  }
}