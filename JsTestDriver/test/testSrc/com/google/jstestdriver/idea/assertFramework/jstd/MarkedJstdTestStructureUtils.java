package com.google.jstestdriver.idea.assertFramework.jstd;

import com.google.common.collect.Maps;
import com.intellij.javascript.testFramework.Annotation;
import com.intellij.lang.javascript.psi.JSCallExpression;
import com.intellij.lang.javascript.psi.JSFile;
import com.intellij.openapi.util.TextRange;
import com.intellij.util.ObjectUtils;
import junit.framework.Assert;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

class MarkedJstdTestStructureUtils {

  private static final Pattern TEST_CASE_PATTERN = Pattern.compile("/\\*TestCase (.+?)\\*/");
  private static final Pattern TEST_CASE_END_PATTERN = Pattern.compile("/\\*TestCaseEnd id:(\\d+?)\\*/");

  private static final Pattern TEST_PATTERN = Pattern.compile("/\\*Test (.+?)\\*/");
  private static final Pattern TEST_END_PATTERN = Pattern.compile("/\\*TestEnd (.+?)\\*/");

  private MarkedJstdTestStructureUtils() {}

  public static MarkedJsTestFileStructure buildMarkedJsTestFileStructureByFileText(@NotNull String fileText, @NotNull JSFile jsFile) {
    MarkedJsTestFileStructure markedJsTestFileStructure = new MarkedJsTestFileStructure();
    handleBeginOfMarkedTestCase(markedJsTestFileStructure, fileText);
    handleEndOfMarkedTestCase(markedJsTestFileStructure, fileText, jsFile);
    validateMarkedTestCases(markedJsTestFileStructure.getMarkedTestCaseStructures());

    Collection<MarkedTestStructure> markedTestStructures = buildMarkedTestStructures(fileText, jsFile);
    assignTestsToTestCases(markedJsTestFileStructure, markedTestStructures);

    return markedJsTestFileStructure;
  }

  private static void handleBeginOfMarkedTestCase(MarkedJsTestFileStructure markedJsTestFileStructure, String fileText) {
    Matcher testCaseMatcher = TEST_CASE_PATTERN.matcher(fileText);
    while (testCaseMatcher.find()) {
      Annotation annotation = new Annotation("TestCase", testCaseMatcher.start(), testCaseMatcher.end(), testCaseMatcher.group(1));
      int id = MarkedTestCaseStructure.getIdAndValidate(annotation);
      MarkedTestCaseStructure markedTestCaseStructure = markedJsTestFileStructure.findById(id);
      if (markedTestCaseStructure != null) {
        throw new RuntimeException("Duplicated TestCase with id " + id + " found");
      }
      markedTestCaseStructure = new MarkedTestCaseStructure(annotation);
      markedJsTestFileStructure.addMarkedTestCaseStructure(markedTestCaseStructure);
    }
  }

  private static void handleEndOfMarkedTestCase(MarkedJsTestFileStructure markedJsTestFileStructure, String fileText, JSFile jsFile) {
    Matcher testCaseEndMatcher = TEST_CASE_END_PATTERN.matcher(fileText);
    while (testCaseEndMatcher.find()) {
      String testCaseIdStr = testCaseEndMatcher.group(1);
      int testCaseId = Integer.parseInt(testCaseIdStr);
      MarkedTestCaseStructure markedTestCaseStructure = markedJsTestFileStructure.findById(testCaseId);
      if (markedTestCaseStructure == null) {
        throw new RuntimeException("'" + testCaseEndMatcher.group() + "' references undefined TestCase");
      }
      markedTestCaseStructure.endEncountered(TextRange.create(testCaseEndMatcher.start(), testCaseEndMatcher.end()), jsFile);
    }
  }

  private static void validateMarkedTestCases(List<MarkedTestCaseStructure> markedTestCaseStructures) {
    for (MarkedTestCaseStructure markedTestCaseStructure : markedTestCaseStructures) {
      if (markedTestCaseStructure.getPsiElement() == null) {
        throw new RuntimeException("End was not found for " + markedTestCaseStructure);
      }
      JSCallExpression jsCallExpression = ObjectUtils.tryCast(markedTestCaseStructure.getPsiElement(), JSCallExpression.class);
      if (jsCallExpression == null) {
        Assert.fail("Unable to find underlying " + JSCallExpression.class + " for " + markedTestCaseStructure);
      }
    }
  }

  private static void assignTestsToTestCases(MarkedJsTestFileStructure markedJsTestFileStructure,
                                             Collection<MarkedTestStructure> markedTestStructures) {
    for (MarkedTestStructure markedTestStructure : markedTestStructures) {
      MarkedTestCaseStructure markedTestCaseStructure = markedJsTestFileStructure.findById(markedTestStructure.getTestCaseId());
      markedTestCaseStructure.addTestStructureInfo(markedTestStructure);
    }
  }

  private static Collection<MarkedTestStructure> buildMarkedTestStructures(@NotNull String fileText, @NotNull JSFile jsFile) {
    Map<String, MarkedTestStructure> markedTestStructureMap = Maps.newHashMap();
    handleBeginOfMarkedTest(markedTestStructureMap, fileText);
    handleEndOfMarkedTest(markedTestStructureMap, fileText, jsFile);

    Collection<MarkedTestStructure> markedTestStructures = markedTestStructureMap.values();

    for (MarkedTestStructure markedTestStructure : markedTestStructures) {
      markedTestStructure.validateBuiltTest();
    }

    return markedTestStructures;
  }

  private static void handleBeginOfMarkedTest(Map<String, MarkedTestStructure> markedTestStructureMap, String fileText) {
    Matcher testMatcher = TEST_PATTERN.matcher(fileText);
    while (testMatcher.find()) {
      Annotation annotation = new Annotation("Test", testMatcher.start(), testMatcher.end(), testMatcher.group(1));
      String testId = MarkedTestStructure.getIdAndValidate(annotation);
      MarkedTestStructure markedTestStructure = markedTestStructureMap.get(testId);
      if (markedTestStructure == null) {
        markedTestStructure = new MarkedTestStructure(annotation);
        markedTestStructureMap.put(testId, markedTestStructure);
      }
      markedTestStructure.handleBeginAnnotation(annotation);
    }
  }

  private static void handleEndOfMarkedTest(Map<String, MarkedTestStructure> markedTestStructureMap, String fileText, JSFile jsFile) {
    Matcher endOfTestMatcher = TEST_END_PATTERN.matcher(fileText);
    while (endOfTestMatcher.find()) {
      Annotation annotation = new Annotation("TestEnd", endOfTestMatcher.start(), endOfTestMatcher.end(), endOfTestMatcher.group(1));
      String testId = annotation.getValue("id");
      MarkedTestStructure markedTestStructure = markedTestStructureMap.get(testId);
      if (markedTestStructure == null) {
        throw new RuntimeException("'" + endOfTestMatcher.group() + "' references undefined Test");
      }
      markedTestStructure.handleEndAnnotation(annotation, jsFile);
    }
  }

}
