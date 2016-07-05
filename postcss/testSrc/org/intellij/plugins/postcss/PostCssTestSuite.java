package org.intellij.plugins.postcss;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.intellij.plugins.postcss.completion.PostCssKeywordCompletionTest;
import org.intellij.plugins.postcss.fileStructure.PostCssFileStructureTest;
import org.intellij.plugins.postcss.inspections.PostCssNestingInspectionTest;
import org.intellij.plugins.postcss.lexer.PostCssLexerTest;
import org.intellij.plugins.postcss.lexer.highlighting.PostCssHighlightingLexerTest;
import org.intellij.plugins.postcss.parser.PostCssParsingTest;

@SuppressWarnings({"JUnitTestClassNamingConvention", "JUnitTestCaseWithNoTests"})
public class PostCssTestSuite extends TestCase {
  public static Test suite() {
    TestSuite testSuite = new TestSuite("All PostCss");
    testSuite.addTest(WithoutPerformance.suite());
    return testSuite;
  }

  public static class WithoutPerformance extends TestCase {
    public static Test suite() {
      TestSuite suite = new TestSuite("All PostCss without performance");
      suite.addTest(Fast.suite());
      suite.addTest(Inspections.suite());
      suite.addTest(Completion.suite());
      suite.addTest(Other.suite());
      return suite;
    }
  }

  public static class Fast extends TestCase {
    public static Test suite() {
      TestSuite suite = new TestSuite("Fast PostCss");
      suite.addTestSuite(PostCssLexerTest.class);
      suite.addTestSuite(PostCssParsingTest.class);
      suite.addTestSuite(PostCssHighlightingLexerTest.class);
      return suite;
    }
  }

  public static class Inspections extends TestCase {
    public static Test suite() {
      TestSuite suite = new TestSuite("Inspections PostCss");
      suite.addTestSuite(PostCssNestingInspectionTest.class);
      return suite;
    }
  }

  public static class Completion extends TestCase {
    public static Test suite() {
      TestSuite suite = new TestSuite("Completion PostCss");
      suite.addTestSuite(PostCssKeywordCompletionTest.class);
      return suite;
    }
  }

  public static class Other extends TestCase {
    public static Test suite() {
      TestSuite suite = new TestSuite("Other PostCss");
      suite.addTestSuite(PostCssFileStructureTest.class);
      return suite;
    }
  }
}