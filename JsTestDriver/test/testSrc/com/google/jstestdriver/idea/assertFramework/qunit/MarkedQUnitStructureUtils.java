package com.google.jstestdriver.idea.assertFramework.qunit;

import com.google.common.collect.Maps;
import com.google.jstestdriver.idea.assertFramework.Annotation;
import com.intellij.lang.javascript.psi.JSFile;
import com.intellij.openapi.util.TextRange;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MarkedQUnitStructureUtils {

  private static final Pattern MODULE_PATTERN = Pattern.compile("/\\*module (.+?)\\*/");
  private static final Pattern MODULE_END_PATTERN = Pattern.compile("/\\*moduleEnd (.+?)\\*/");

  private static final Pattern TEST_PATTERN = Pattern.compile("/\\*test (.+?)\\*/");
  private static final Pattern TEST_END_PATTERN = Pattern.compile("/\\*testEnd (.+?)\\*/");

  private MarkedQUnitStructureUtils() {
  }

  public static MarkedQUnitFileStructure buildMarkedQUnitFileStructureByFileText(String fileText, JSFile jsFile) {
    MarkedQUnitFileStructure markedQUnitFileStructure = new MarkedQUnitFileStructure();
    markedQUnitFileStructure.addMarkedModuleStructure(MarkedQUnitModuleStructure.newDefaultModule());
    handleBeginOfMarkedModule(markedQUnitFileStructure, fileText);
    handleEndOfMarkedModule(markedQUnitFileStructure, fileText, jsFile);
    validateMarkedModules(markedQUnitFileStructure.getModules());

    Collection<MarkedQUnitTestMethodStructure> markedQUnitTestStructures = buildMarkedQUnitTestStructures(fileText, jsFile);
    assignTestsToModules(markedQUnitFileStructure, markedQUnitTestStructures);

    return markedQUnitFileStructure;
  }

  private static void assignTestsToModules(MarkedQUnitFileStructure markedQUnitFileStructure,
                                           Collection<MarkedQUnitTestMethodStructure> markedQUnitTestStructures) {
    for (MarkedQUnitTestMethodStructure markedQUnitTestStructure : markedQUnitTestStructures) {
      MarkedQUnitModuleStructure markedQUnitModuleStructure = markedQUnitFileStructure.findById(markedQUnitTestStructure.getTestCaseId());
      markedQUnitModuleStructure.addTestStructure(markedQUnitTestStructure);
    }
  }

  private static void handleBeginOfMarkedModule(MarkedQUnitFileStructure markedQUnitFileStructure, String fileText) {
    Matcher moduleMatcher = MODULE_PATTERN.matcher(fileText);
    while (moduleMatcher.find()) {
      Annotation annotation = new Annotation("module", moduleMatcher.start(), moduleMatcher.end(), moduleMatcher.group(1));
      int id = MarkedQUnitModuleStructure.getId(annotation);
      MarkedQUnitModuleStructure markedQUnitModuleStructure = markedQUnitFileStructure.findById(id);
      if (markedQUnitModuleStructure != null) {
        throw new RuntimeException("Duplicated module with id " + id + " found");
      }
      markedQUnitModuleStructure = MarkedQUnitModuleStructure.newRegularModule(annotation);
      markedQUnitFileStructure.addMarkedModuleStructure(markedQUnitModuleStructure);
    }
  }

  private static void handleEndOfMarkedModule(MarkedQUnitFileStructure markedQUnitFileStructure, String fileText, JSFile jsFile) {
    Matcher moduleEndMatcher = MODULE_END_PATTERN.matcher(fileText);
    while (moduleEndMatcher.find()) {
      Annotation endAnnotation = new Annotation("moduleEnd", moduleEndMatcher.start(), moduleEndMatcher.end(), moduleEndMatcher.group(1));
      int moduleId = MarkedQUnitModuleStructure.getId(endAnnotation);
      MarkedQUnitModuleStructure markedQUnitModuleStructure = markedQUnitFileStructure.findById(moduleId);
      if (markedQUnitModuleStructure == null) {
        throw new RuntimeException("'" + moduleEndMatcher.group() + "' references undefined module");
      }
      markedQUnitModuleStructure.endEncountered(TextRange.create(moduleEndMatcher.start(), moduleEndMatcher.end()), jsFile);
    }
  }

  private static void validateMarkedModules(Collection<MarkedQUnitModuleStructure> markedQUnitModuleStructures) {
    for (MarkedQUnitModuleStructure markedQUnitModuleStructure : markedQUnitModuleStructures) {
      markedQUnitModuleStructure.validate();
    }
  }

  private static Collection<MarkedQUnitTestMethodStructure> buildMarkedQUnitTestStructures(String fileText, JSFile jsFile) {
    Map<String, MarkedQUnitTestMethodStructure> markedTestStructureMap = Maps.newHashMap();
    handleBeginOfMarkedQUnitTest(markedTestStructureMap, fileText);
    handleEndOfMarkedQUnitTest(markedTestStructureMap, fileText, jsFile);

    Collection<MarkedQUnitTestMethodStructure> markedQUnitTestStructures = markedTestStructureMap.values();

    for (MarkedQUnitTestMethodStructure markedQUnitTestStructure : markedQUnitTestStructures) {
      markedQUnitTestStructure.validateBuiltStructure();
    }

    return markedQUnitTestStructures;
  }

  private static void handleBeginOfMarkedQUnitTest(Map<String, MarkedQUnitTestMethodStructure> markedQUniTestStructureMap, String fileText) {
    Matcher testMatcher = TEST_PATTERN.matcher(fileText);
    while (testMatcher.find()) {
      Annotation annotation = new Annotation("test", testMatcher.start(), testMatcher.end(), testMatcher.group(1));
      String testId = MarkedQUnitTestMethodStructure.getId(annotation);
      MarkedQUnitTestMethodStructure markedQUnitTestStructure = markedQUniTestStructureMap.get(testId);
      if (markedQUnitTestStructure != null) {
        throw new RuntimeException("Test with id " + testId + " is duplicated");
      }
      markedQUnitTestStructure = new MarkedQUnitTestMethodStructure(annotation);
      markedQUniTestStructureMap.put(testId, markedQUnitTestStructure);
    }
  }

  private static void handleEndOfMarkedQUnitTest(Map<String, MarkedQUnitTestMethodStructure> markedQUnitTestStructureMap, String fileText, JSFile jsFile) {
    Matcher testEndMatcher = TEST_END_PATTERN.matcher(fileText);
    while (testEndMatcher.find()) {
      Annotation annotation = new Annotation("testEnd", testEndMatcher.start(), testEndMatcher.end(), testEndMatcher.group(1));
      String testId = MarkedQUnitTestMethodStructure.getId(annotation);
      MarkedQUnitTestMethodStructure markedQUnitTestStructure = markedQUnitTestStructureMap.get(testId);
      if (markedQUnitTestStructure == null) {
        throw new RuntimeException("Test with id " + testId + " is not found");
      }
      markedQUnitTestStructure.endEncountered(annotation.getTextRange(), jsFile);
    }
  }

  @NotNull
  public static String getRequiredAttributeValue(String attributeKey, Annotation annotation) {
    String value = annotation.getValue(attributeKey);
    if (value == null) {
      throw new RuntimeException("Attribute '" + attributeKey + "' should be specified, " + annotation);
    }
    return value;
  }

}
