package com.google.jstestdriver.idea.assertFramework.jasmine;

import com.google.common.collect.Maps;
import com.google.jstestdriver.idea.assertFramework.Annotation;
import com.google.jstestdriver.idea.assertFramework.CompoundId;
import com.intellij.lang.javascript.psi.JSFile;
import com.intellij.util.ObjectUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


class MarkedJasmineFileStructureBuilder {

  private static final Pattern SUITE_START = Pattern.compile("/\\*suite .+?\\*/");
  private static final Pattern SUITE_END = Pattern.compile("/\\*suiteEnd .+?\\*/");

  private static final Pattern SPEC_START = Pattern.compile("/\\*spec .+?\\*/");
  private static final Pattern SPEC_END = Pattern.compile("/\\*specEnd .+?\\*/");

  private MarkedJasmineFileStructureBuilder() {}

  public static MarkedJasmineFileStructure buildMarkedJasmineFileStructure(@NotNull String fileText, @NotNull JSFile jsFile) {
    MarkedJasmineFileStructure markedJasmineFileStructure = new MarkedJasmineFileStructure();
    handleBeginOfMarkedSuite(markedJasmineFileStructure, fileText);
    handleEndOfMarkedSuite(markedJasmineFileStructure, fileText, jsFile);
    validateMarkedSuites(markedJasmineFileStructure.getInnerSuiteStructures());

    Collection<MarkedJasmineSpecStructure> markedJasmineSpecStructures = buildMarkedJasmineSpecStructures(fileText, jsFile);
    assignSpecsToSuites(markedJasmineFileStructure, markedJasmineSpecStructures);

    return markedJasmineFileStructure;
  }

  private static void assignSpecsToSuites(@NotNull MarkedJasmineFileStructure markedJasmineFileStructure,
                                          @NotNull Collection<MarkedJasmineSpecStructure> markedJasmineSpecStructures) {
    for (MarkedJasmineSpecStructure specStructure : markedJasmineSpecStructures) {
      CompoundId suiteId = specStructure.getSuiteId();
      MarkedJasmineSuiteStructureContainer container = findContainerById(markedJasmineFileStructure, suiteId);
      MarkedJasmineSuiteStructure suiteStructure = ObjectUtils.tryCast(container, MarkedJasmineSuiteStructure.class);
      if (suiteStructure == null) {
        throw new RuntimeException("Suite structure was not found by id '" + suiteId + "'!");
      }
      suiteStructure.addSpecStructure(specStructure);
    }
  }

  private static void validateMarkedSuites(Collection<MarkedJasmineSuiteStructure> suites) {
    for (MarkedJasmineSuiteStructure suite : suites) {
      suite.validate();
    }
  }

  private static void handleBeginOfMarkedSuite(MarkedJasmineFileStructure markedJasmineFileStructure, String fileText) {
    Matcher suiteStartMatcher = SUITE_START.matcher(fileText);
    while (suiteStartMatcher.find()) {
      Annotation startAnnotation = Annotation.fromMatcher(suiteStartMatcher);
      CompoundId suiteId = startAnnotation.getCompoundId();
      MarkedJasmineSuiteStructureContainer container = findContainerById(markedJasmineFileStructure, suiteId.getParentId());
      if (container == null) {
        throw new RuntimeException("Can't find container for suite with " + suiteId);
      }
      MarkedJasmineSuiteStructure markedJasmineSuiteStructure = container.findSuiteStructureById(suiteId);
      if (markedJasmineSuiteStructure != null) {
        throw new RuntimeException("Duplicated suite with id " + suiteId + " found");
      }
      markedJasmineSuiteStructure = new MarkedJasmineSuiteStructure(startAnnotation);
      container.addSuiteStructure(markedJasmineSuiteStructure);
    }
  }

  @Nullable
  private static MarkedJasmineSuiteStructureContainer findContainerById(MarkedJasmineSuiteStructureContainer rootContainer, CompoundId id) {
    if (!id.hasParent()) {
      return rootContainer;
    }
    MarkedJasmineSuiteStructureContainer parentContainer = findContainerById(rootContainer, id.getParentId());
    if (parentContainer == null) {
      return null;
    }
    return parentContainer.findSuiteStructureById(id);
  }

  private static void handleEndOfMarkedSuite(@NotNull MarkedJasmineFileStructure markedJasmineFileStructure,
                                             @NotNull String fileText,
                                             @NotNull JSFile jsFile) {
    Matcher suiteEndMatcher = SUITE_END.matcher(fileText);
    while (suiteEndMatcher.find()) {
      Annotation endAnnotation = Annotation.fromMatcher(suiteEndMatcher);
      CompoundId suiteStructureId = endAnnotation.getCompoundId();
      MarkedJasmineSuiteStructureContainer suiteStructureContainer = findContainerById(markedJasmineFileStructure, suiteStructureId);
      if (!(suiteStructureContainer instanceof MarkedJasmineSuiteStructure)) {
        throw new RuntimeException(suiteEndMatcher.group() + " references undefined suite!");
      }
      MarkedJasmineSuiteStructure suiteStructure = (MarkedJasmineSuiteStructure)suiteStructureContainer;
      suiteStructure.endAnnotationEncountered(endAnnotation.getTextRange(), jsFile);
    }
  }

  private static Collection<MarkedJasmineSpecStructure> buildMarkedJasmineSpecStructures(String fileText, JSFile jsFile) {
    Map<CompoundId, MarkedJasmineSpecStructure> markedSpecStructureMap = Maps.newHashMap();
    handleBeginOfMarkedJasmineSpec(markedSpecStructureMap, fileText);
    handleEndOfMarkedJasmineSpec(markedSpecStructureMap, fileText, jsFile);

    Collection<MarkedJasmineSpecStructure> markedSpecStructures = markedSpecStructureMap.values();

    for (MarkedJasmineSpecStructure markedJasmineSpecStructure : markedSpecStructures) {
      markedJasmineSpecStructure.validate();
    }

    return markedSpecStructures;
  }

  private static void handleBeginOfMarkedJasmineSpec(@NotNull Map<CompoundId, MarkedJasmineSpecStructure> markedSpecStructureMap,
                                                     @NotNull String fileText) {
    Matcher specStartMatcher = SPEC_START.matcher(fileText);
    while (specStartMatcher.find()) {
      Annotation startAnnotation = Annotation.fromMatcher(specStartMatcher);
      CompoundId specId = startAnnotation.getCompoundId();
      MarkedJasmineSpecStructure markedJasmineSpecStructure = markedSpecStructureMap.get(specId);
      if (markedJasmineSpecStructure != null) {
        throw new RuntimeException("Duplicated spec with id '" + specId + "'");
      }
      markedJasmineSpecStructure = new MarkedJasmineSpecStructure(startAnnotation);
      markedSpecStructureMap.put(markedJasmineSpecStructure.getId(), markedJasmineSpecStructure);
    }
  }

  private static void handleEndOfMarkedJasmineSpec(@NotNull Map<CompoundId, MarkedJasmineSpecStructure> markedSpecStructureMap,
                                                   @NotNull String fileText,
                                                   @NotNull JSFile jsFile) {
    Matcher specEndMatcher = SPEC_END.matcher(fileText);
    while (specEndMatcher.find()) {
      Annotation endAnnotation = Annotation.fromMatcher(specEndMatcher);
      CompoundId specId = endAnnotation.getCompoundId();
      MarkedJasmineSpecStructure markedJasmineSpecStructure = markedSpecStructureMap.get(specId);
      if (markedJasmineSpecStructure == null) {
        throw new RuntimeException(specEndMatcher.group() + " references undefined spec!");
      }
      markedJasmineSpecStructure.endAnnotationEncountered(endAnnotation.getTextRange(), jsFile);
    }
  }
}
