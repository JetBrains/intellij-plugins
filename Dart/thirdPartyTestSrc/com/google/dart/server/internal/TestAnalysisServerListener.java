/*
 * Copyright (c) 2014, the Dart project authors.
 * 
 * Licensed under the Eclipse Public License v1.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 * 
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

package com.google.dart.server.internal;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.dart.server.AnalysisServerListener;
import junit.framework.Assert;
import junit.framework.AssertionFailedError;
import org.dartlang.analysis.server.protocol.*;

import java.util.List;
import java.util.Map;

import static org.fest.assertions.Assertions.assertThat;

public class TestAnalysisServerListener implements AnalysisServerListener {

  private final Map<String, CompletionResult> completionsMap = Maps.newHashMap();
  private final List<String> flushedResults = Lists.newArrayList();
  private final Map<String, List<SearchResult>> searchResultsMap = Maps.newHashMap();
  private final List<AnalysisServerError> serverErrors = Lists.newArrayList();
  private final Map<String, List<AnalysisError>> sourcesErrors = Maps.newHashMap();
  private final List<String> analyzedFiles = Lists.newArrayList();
  private final Map<String, List<HighlightRegion>> highlightsMap = Maps.newHashMap();
  private final Map<String, List<ImplementedClass>> implementedClassesMap = Maps.newHashMap();
  private final Map<String, List<ImplementedMember>> implementedMembersMap = Maps.newHashMap();
  private final Map<String, List<NavigationRegion>> navigationMap = Maps.newHashMap();
  private final Map<String, List<Occurrences>> occurrencesMap = Maps.newHashMap();
  private final Map<String, Outline> outlineMap = Maps.newHashMap();
  private final Map<String, List<OverrideMember>> overridesMap = Maps.newHashMap();
  private boolean serverConnected = false;
  private AnalysisStatus analysisStatus = null;

  public synchronized void assertAnalysisStatus(AnalysisStatus expectedStatus) {
    Assert.assertEquals(expectedStatus.isAnalyzing(), analysisStatus.isAnalyzing());
    Assert.assertEquals(expectedStatus.getAnalysisTarget(), analysisStatus.getAnalysisTarget());
  }

  /**
   * Assert that the passed set of analyzed files equals the last set passed to the server.
   *
   * @param files the list of expected analyzed file paths
   * @throws AssertionFailedError if a different set of strings is passed than was expected
   */
  public synchronized void assertAnalyzedFiles(List<String> files) {
    assertThat(getAnalyzedFiles()).isEqualTo(files);
  }

  /**
   * Assert that the number of errors that have been gathered matches the number of errors that are
   * given and that they have the expected error codes. The order in which the errors were gathered
   * is ignored.
   *
   * @param file               the file to check errors for
   * @param expectedErrorCodes the error codes of the errors that should have been gathered
   * @throws AssertionFailedError if a different number of errors have been gathered than were
   *                              expected
   */
  public synchronized void assertErrorsWithAnalysisErrors(String file, AnalysisError... expectedErrors) {
    List<AnalysisError> errors = getErrors(file);
    assertErrorsWithAnalysisErrors(errors, expectedErrors);
  }

  /**
   * Asserts the list of flushed results.
   */
  public synchronized void assertFlushedResults(List<String> expectedFlushedResults) {
    assertThat(expectedFlushedResults).isEqualTo(flushedResults);
  }

  /**
   * Asserts that there was no {@link AnalysisServerError} reported.
   */
  public synchronized void assertNoServerErrors() {
    assertThat(serverErrors).isEmpty();
  }

  public synchronized void assertServerConnected(boolean expectedConnected) {
    Assert.assertEquals(expectedConnected, serverConnected);
  }

  public void assertServerErrors(List<AnalysisServerError> expectedErrors) {
    Assert.assertEquals(expectedErrors.size(), serverErrors.size());
    for (int i = 0; i < expectedErrors.size(); i++) {
      Assert.assertTrue(expectedErrors.get(i).equals(serverErrors.get(i)));
    }
  }

  /**
   * Removes all of reported {@link NavigationRegion}s.
   */
  public synchronized void clearNavigationRegions() {
    navigationMap.clear();
  }

  /**
   * Removes all of reported {@link Occurrences}s.
   */
  public synchronized void clearOccurrences() {
    occurrencesMap.clear();
  }

  /**
   * Removes all of reported {@link OverrideMember}.
   */
  public synchronized void clearOverrides() {
    overridesMap.clear();
  }

  @Override
  public void computedAnalyzedFiles(List<String> directories) {
    analyzedFiles.clear();
    analyzedFiles.addAll(directories);
  }

  @Override
  public synchronized void computedCompletion(String completionId,
                                              int replacementOffset,
                                              int replacementLength,
                                              List<CompletionSuggestion> suggestions,
                                              boolean isLast) {
    // computed completion results are aggregate, replacing any prior results
    completionsMap.put(completionId, new CompletionResult(replacementOffset, replacementLength, suggestions, isLast));
  }

  @Override
  public synchronized void computedErrors(String file, List<AnalysisError> errors) {
    sourcesErrors.put(file, errors);
  }

  @Override
  public synchronized void computedHighlights(String file, List<HighlightRegion> highlights) {
    highlightsMap.put(file, highlights);
  }

  @Override
  public void computedImplemented(String file, List<ImplementedClass> implementedClasses, List<ImplementedMember> implementedMembers) {
    implementedClassesMap.put(file, implementedClasses);
    implementedMembersMap.put(file, implementedMembers);
  }

  @Override
  public synchronized void computedLaunchData(String file, String kind, String[] referencedFiles) {
    // TODO(brianwilkerson) Add tests for this notification and implement this method appropriately
  }

  @Override
  public synchronized void computedNavigation(String file, List<NavigationRegion> targets) {
    navigationMap.put(file, targets);
  }

  @Override
  public void computedOccurrences(String file, List<Occurrences> occurrencesArray) {
    occurrencesMap.put(file, occurrencesArray);
  }

  @Override
  public synchronized void computedOutline(String file, Outline outline) {
    outlineMap.put(file, outline);
  }

  @Override
  public void computedOverrides(String file, List<OverrideMember> overrides) {
    overridesMap.put(file, overrides);
  }

  @Override
  public void computedSearchResults(String searchId, List<SearchResult> results, boolean last) {
    searchResultsMap.put(searchId, results);
  }

  /**
   * Returns a navigation {@link NavigationTarget} at the given position.
   */
  public synchronized NavigationTarget findNavigationElement(String file, int offset) {
    List<NavigationRegion> regions = getNavigationRegions(file);
    if (regions != null) {
      for (NavigationRegion navigationRegion : regions) {
        if (navigationRegion.containsInclusive(offset)) {
          return navigationRegion.getTargetObjects().get(0);
        }
      }
    }
    return null;
  }

  @Override
  public synchronized void flushedResults(List<String> files) {
    flushedResults.addAll(files);
  }

  /**
   * Returns the last set of analyzed files sent from the server.
   */
  public synchronized List<String> getAnalyzedFiles() {
    return analyzedFiles;
  }

  public boolean getCompletionIsLast(String completionId) {
    CompletionResult result = completionsMap.get(completionId);
    if (result == null) {
      throw new AssertionFailedError("Expected completion response: " + completionId);
    }
    return result.last;
  }

  public int getCompletionReplacementLength(String completionId) {
    CompletionResult result = completionsMap.get(completionId);
    if (result == null) {
      throw new AssertionFailedError("Expected completion response: " + completionId);
    }
    return result.replacementLength;
  }

  public int getCompletionReplacementOffset(String completionId) {
    CompletionResult result = completionsMap.get(completionId);
    if (result == null) {
      throw new AssertionFailedError("Expected completion response: " + completionId);
    }
    return result.replacementOffset;
  }

  /**
   * Returns list of {@link CompletionSuggestion} for the given completion id, maybe {@code null} if
   * have not been ever notified.
   */
  public synchronized List<CompletionSuggestion> getCompletions(String completionId) {
    CompletionResult result = completionsMap.get(completionId);
    return result != null ? result.suggestions : null;
  }

  /**
   * Returns {@link AnalysisError} for the given file, may be empty, but not {@code null}.
   */
  public synchronized List<AnalysisError> getErrors(String file) {
    List<AnalysisError> errors = sourcesErrors.get(file);
    if (errors == null) {
      return AnalysisError.EMPTY_LIST;
    }
    return errors;
  }

  /**
   * Returns {@link HighlightRegion}s for the given file, maybe {@code null} if have not been ever
   * notified.
   */
  public synchronized List<HighlightRegion> getHighlightRegions(String file) {
    return highlightsMap.get(file);
  }

  /**
   * Returns {@link ImplementedClass}s for the given file, maybe {@code null} if have not been ever
   * notified.
   */
  public synchronized List<ImplementedClass> getImplementedClasses(String file) {
    return implementedClassesMap.get(file);
  }

  /**
   * Returns {@link ImplementedMember}s for the given file, maybe {@code null} if have not been ever
   * notified.
   */
  public synchronized List<ImplementedMember> getImplementedMembers(String file) {
    return implementedMembersMap.get(file);
  }

  /**
   * Returns {@link NavigationRegion}s for the given file, maybe {@code null} if have not been ever
   * notified.
   */
  public synchronized List<NavigationRegion> getNavigationRegions(String file) {
    return navigationMap.get(file);
  }

  /**
   * Returns {@link Occurrences}s for the given file, maybe {@code null} if have not been ever
   * notified.
   */
  public synchronized List<Occurrences> getOccurrences(String file) {
    return occurrencesMap.get(file);
  }

  /**
   * Returns {@link Outline} for the given {@link Source}, maybe {@code null} if have not been ever
   * notified.
   */
  public synchronized Outline getOutline(String file) {
    return outlineMap.get(file);
  }

  /**
   * Returns {@link OverrideMember}s for the given file, maybe {@code null} if have not been ever
   * notified.
   */
  public synchronized List<OverrideMember> getOverrides(String file) {
    return overridesMap.get(file);
  }

  /**
   * Returns {@link SearchResult[]} for the given search id, maybe {@code null} if have not been
   * ever notified.
   */
  public List<SearchResult> getSearchResults(String searchId) {
    return searchResultsMap.get(searchId);
  }

  @Override
  public void requestError(RequestError requestError) {
  }

  @Override
  public synchronized void serverConnected(String version) {
    serverConnected = true;
  }

  @Override
  public synchronized void serverError(boolean isFatal, String message, String stackTrace) {
    serverErrors.add(new AnalysisServerError(isFatal, message, stackTrace));
  }

  @Override
  public void serverIncompatibleVersion(String version) {
  }

  @Override
  public synchronized void serverStatus(AnalysisStatus analysisStatus, PubStatus pubStatus) {
    this.analysisStatus = analysisStatus;
  }

  /**
   * Assert that the array of actual {@link AnalysisError}s match the array of expected
   * {@link AnalysisError}s.
   *
   * @param actualErrors   the actual set of errors that were created for some analysis
   * @param expectedErrors the expected array of errors
   */
  private void assertErrorsWithAnalysisErrors(List<AnalysisError> actualErrors, AnalysisError[] expectedErrors) {
    if (actualErrors == null && expectedErrors == null) {
      return;
    }

    // assert that the arrays have the same length
    Assert.assertEquals(expectedErrors.length, actualErrors.size());

    AnalysisError[] actualErrorsArray = actualErrors.toArray(new AnalysisError[actualErrors.size()]);

    // assert that the actualErrors contains all of the expected errors
    for (AnalysisError expectedError : expectedErrors) {
      // individual calls to assert each error are made for better messaging when there is a failure
      assertThat(actualErrorsArray).contains(expectedError);
    }
  }

  private final static class CompletionResult {
    final int replacementOffset;
    final int replacementLength;
    final List<CompletionSuggestion> suggestions;
    final boolean last;

    public CompletionResult(int replacementOffset, int replacementLength, List<CompletionSuggestion> suggestions, boolean last) {
      this.replacementOffset = replacementOffset;
      this.replacementLength = replacementLength;
      this.suggestions = suggestions;
      this.last = last;
    }
  }
}
