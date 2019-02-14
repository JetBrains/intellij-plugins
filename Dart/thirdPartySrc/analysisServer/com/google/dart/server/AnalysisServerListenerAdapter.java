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
package com.google.dart.server;

import org.dartlang.analysis.server.protocol.*;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * This adapter class provides default implementations for the methods described by the
 * {@code AnalysisServerListener} interface.
 */
public class AnalysisServerListenerAdapter implements AnalysisServerListener {

  @Override
  public void computedAnalyzedFiles(List<String> directories) {
  }

  @Override
  public void computedAvailableSuggestions(@NotNull List<AvailableSuggestionSet> changed,
                                           @NotNull int[] removed) {
  }

  @Override
  public void computedCompletion(String completionId,
                                 int replacementOffset,
                                 int replacementLength,
                                 List<CompletionSuggestion> completions,
                                 List<IncludedSuggestionSet> includedSuggestionSets,
                                 List<String> includedSuggestionKinds,
                                 boolean isLast) {
  }

  @Override
  public void computedErrors(String file, List<AnalysisError> errors) {
  }

  @Override
  public void computedHighlights(String file, List<HighlightRegion> highlights) {
  }

  @Override
  public void computedImplemented(String file, List<ImplementedClass> implementedClasses,
                                  List<ImplementedMember> implementedMembers) {
  }

  @Override
  public void computedLaunchData(String file, String kind, String[] referencedFiles) {
  }

  @Override
  public void computedNavigation(String file, List<NavigationRegion> targets) {
  }

  @Override
  public void computedOccurrences(String file, List<Occurrences> occurrencesArray) {
  }

  @Override
  public void computedOutline(String file, Outline outline) {
  }

  @Override
  public void computedOverrides(String file, List<OverrideMember> overrides) {
  }

  @Override
  public void computedClosingLabels(String file, List<ClosingLabel> labels) {
  }

  @Override
  public void computedSearchResults(String searchId, List<SearchResult> results, boolean last) {
  }

  @Override
  public void flushedResults(List<String> files) {
  }

  @Override
  public void requestError(RequestError requestError) {
  }

  @Override
  public void serverConnected(String version) {
  }

  @Override
  public void serverError(boolean isFatal, String message, String stackTrace) {
  }

  @Override
  public void serverIncompatibleVersion(String version) {
  }

  @Override
  public void serverStatus(AnalysisStatus analysisStatus, PubStatus pubStatus) {
  }
}
