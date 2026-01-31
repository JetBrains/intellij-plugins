package org.intellij.plugins.postcss;

import junit.framework.Test;
import junit.framework.TestSuite;
import org.intellij.plugins.postcss.completion.PostCssCustomMediaCompletionTest;
import org.intellij.plugins.postcss.completion.PostCssCustomPropertyCompletionTest;
import org.intellij.plugins.postcss.completion.PostCssCustomSelectorCompletionTest;
import org.intellij.plugins.postcss.completion.PostCssNestCompletionTest;
import org.intellij.plugins.postcss.completion.PostCssNestingCompletionTest;
import org.intellij.plugins.postcss.completion.PostCssOtherCompletionTest;
import org.intellij.plugins.postcss.editor.PostCssCommenterTest;
import org.intellij.plugins.postcss.editor.PostCssCopyrightTest;
import org.intellij.plugins.postcss.editor.breadcrumbs.PostCssBreadcrumbsTest;
import org.intellij.plugins.postcss.fileStructure.PostCssFileStructureTest;
import org.intellij.plugins.postcss.formatter.PostCssFormatterTest;
import org.intellij.plugins.postcss.inspections.PostCssCssInspectionTest;
import org.intellij.plugins.postcss.inspections.PostCssCustomMediaInspectionTest;
import org.intellij.plugins.postcss.inspections.PostCssCustomMediaQuickFixTest;
import org.intellij.plugins.postcss.inspections.PostCssCustomSelectorInspectionTest;
import org.intellij.plugins.postcss.inspections.PostCssCustomSelectorQuickFixTest;
import org.intellij.plugins.postcss.inspections.PostCssHighlightingTest;
import org.intellij.plugins.postcss.inspections.PostCssMediaRangeInspectionTest;
import org.intellij.plugins.postcss.inspections.PostCssNestingInspectionTest;
import org.intellij.plugins.postcss.inspections.PostCssNestingQuickFixTest;
import org.intellij.plugins.postcss.lexer.PostCssLexerTest;
import org.intellij.plugins.postcss.lexer.highlighting.PostCssHighlightingLexerTest;
import org.intellij.plugins.postcss.parser.PostCssIncrementalParserTest;
import org.intellij.plugins.postcss.parser.PostCssParsingCustomMediaTest;
import org.intellij.plugins.postcss.parser.PostCssParsingCustomSelectorTest;
import org.intellij.plugins.postcss.parser.PostCssParsingMediaRangesTest;
import org.intellij.plugins.postcss.parser.PostCssParsingNestingTest;
import org.intellij.plugins.postcss.parser.PostCssParsingOtherTest;
import org.intellij.plugins.postcss.rename.PostCssRenameTest;
import org.intellij.plugins.postcss.resolve.PostCssCustomMediaResolveTest;
import org.intellij.plugins.postcss.resolve.PostCssCustomSelectorResolveTest;
import org.intellij.plugins.postcss.resolve.PostCssSimpleVarsTest;
import org.intellij.plugins.postcss.smartEnter.PostCssSmartEnterTest;
import org.intellij.plugins.postcss.usages.PostCssFindUsagesTest;

@SuppressWarnings("JUnitTestClassNamingConvention")
public final class PostCssTestSuite {
  public static Test suite() {
    TestSuite testSuite = new TestSuite("All PostCSS");
    testSuite.addTest(WithoutPerformance.suite());
    return testSuite;
  }

  public static final class WithoutPerformance {
    public static Test suite() {
      TestSuite suite = new TestSuite("All PostCSS without performance");
      suite.addTest(Fast.suite());
      suite.addTest(Inspections.suite());
      suite.addTest(Completion.suite());
      suite.addTest(Resolving.suite());
      suite.addTest(Refactoring.suite());
      suite.addTest(Other.suite());
      return suite;
    }
  }

  public static final class Fast {
    public static Test suite() {
      TestSuite suite = new TestSuite("Fast PostCSS");
      suite.addTestSuite(PostCssLexerTest.class);
      suite.addTestSuite(PostCssParsingNestingTest.class);
      suite.addTestSuite(PostCssParsingCustomSelectorTest.class);
      suite.addTestSuite(PostCssParsingCustomMediaTest.class);
      suite.addTestSuite(PostCssParsingMediaRangesTest.class);
      suite.addTestSuite(PostCssParsingOtherTest.class);
      suite.addTestSuite(PostCssHighlightingLexerTest.class);
      return suite;
    }
  }

  public static final class Inspections {
    public static Test suite() {
      TestSuite suite = new TestSuite("Inspections PostCSS");
      suite.addTestSuite(PostCssCssInspectionTest.class);
      suite.addTestSuite(PostCssNestingInspectionTest.class);
      suite.addTestSuite(PostCssCustomSelectorInspectionTest.class);
      suite.addTestSuite(PostCssNestingQuickFixTest.class);
      suite.addTestSuite(PostCssCustomSelectorQuickFixTest.class);
      suite.addTestSuite(PostCssMediaRangeInspectionTest.class);
      suite.addTestSuite(PostCssCustomMediaInspectionTest.class);
      suite.addTestSuite(PostCssCustomMediaQuickFixTest.class);

      suite.addTestSuite(PostCssHighlightingTest.class);
      return suite;
    }
  }

  public static final class Completion {
    public static Test suite() {
      TestSuite suite = new TestSuite("Completion PostCSS");
      suite.addTestSuite(PostCssNestCompletionTest.class);
      suite.addTestSuite(PostCssNestingCompletionTest.class);
      suite.addTestSuite(PostCssCustomSelectorCompletionTest.class);
      suite.addTestSuite(PostCssCustomMediaCompletionTest.class);
      suite.addTestSuite(PostCssCustomPropertyCompletionTest.class);
      suite.addTestSuite(PostCssOtherCompletionTest.class);
      return suite;
    }
  }

  public static final class Resolving {
    public static Test suite() {
      TestSuite suite = new TestSuite("Resolving PostCSS");
      suite.addTestSuite(PostCssCustomSelectorResolveTest.class);
      suite.addTestSuite(PostCssCustomMediaResolveTest.class);
      suite.addTestSuite(PostCssFindUsagesTest.class);
      suite.addTestSuite(PostCssSimpleVarsTest.class);
      return suite;
    }
  }

  public static final class Refactoring {
    public static Test suite() {
      TestSuite suite = new TestSuite("Refactoring PostCSS");
      suite.addTestSuite(PostCssRenameTest.class);
      return suite;
    }
  }

  public static final class Other {
    public static Test suite() {
      TestSuite suite = new TestSuite("Other PostCSS");
      suite.addTestSuite(PostCssFormatterTest.class);
      suite.addTestSuite(PostCssBreadcrumbsTest.class);
      suite.addTestSuite(PostCssFileStructureTest.class);
      suite.addTestSuite(PostCssSmartEnterTest.class);
      suite.addTestSuite(PostCssIncrementalParserTest.class);
      suite.addTestSuite(PostCssGotoSymbolTest.class);
      suite.addTestSuite(PostCssCommenterTest.class);
      suite.addTestSuite(PostCssCopyrightTest.class);
      return suite;
    }
  }
}