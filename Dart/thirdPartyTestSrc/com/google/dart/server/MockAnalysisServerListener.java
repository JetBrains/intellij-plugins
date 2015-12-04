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

import org.dartlang.analysis.server.protocol.AnalysisError;
import org.dartlang.analysis.server.protocol.AnalysisStatus;
import org.dartlang.analysis.server.protocol.CompletionSuggestion;
import org.dartlang.analysis.server.protocol.HighlightRegion;
import org.dartlang.analysis.server.protocol.ImplementedClass;
import org.dartlang.analysis.server.protocol.ImplementedMember;
import org.dartlang.analysis.server.protocol.NavigationRegion;
import org.dartlang.analysis.server.protocol.Occurrences;
import org.dartlang.analysis.server.protocol.Outline;
import org.dartlang.analysis.server.protocol.OverrideMember;
import org.dartlang.analysis.server.protocol.PubStatus;
import org.dartlang.analysis.server.protocol.RequestError;
import org.dartlang.analysis.server.protocol.SearchResult;

import java.util.List;

public class MockAnalysisServerListener implements AnalysisServerListener {

  @Override
  public void computedAnalyzedFiles(List<String> directories) {
    throw new UnsupportedOperationException();
  }

  @Override
  public void computedCompletion(String completionId, int replacementOffset, int replacementLength,
      List<CompletionSuggestion> completions, boolean isLast) {
    throw new UnsupportedOperationException();
  }

  @Override
  public void computedErrors(String file, List<AnalysisError> errors) {
    throw new UnsupportedOperationException();
  }

  @Override
  public void computedHighlights(String file, List<HighlightRegion> highlights) {
    throw new UnsupportedOperationException();
  }

  @Override
  public void computedImplemented(String file, List<ImplementedClass> implementedClasses,
      List<ImplementedMember> implementedMembers) {
    throw new UnsupportedOperationException();
  }

  @Override
  public void computedLaunchData(String file, String kind, String[] referencedFiles) {
    throw new UnsupportedOperationException();
  }

  @Override
  public void computedNavigation(String file, List<NavigationRegion> targets) {
    throw new UnsupportedOperationException();
  }

  @Override
  public void computedOccurrences(String file, List<Occurrences> occurrencesArray) {
    throw new UnsupportedOperationException();
  }

  @Override
  public void computedOutline(String file, Outline outline) {
    throw new UnsupportedOperationException();
  }

  @Override
  public void computedOverrides(String file, List<OverrideMember> overrides) {
    throw new UnsupportedOperationException();
  }

  @Override
  public void computedSearchResults(String searchId, List<SearchResult> results, boolean last) {
    throw new UnsupportedOperationException();
  }

  @Override
  public void flushedResults(List<String> files) {
    throw new UnsupportedOperationException();
  }

  @Override
  public void requestError(RequestError requestError) {
    throw new UnsupportedOperationException();
  }

  @Override
  public void serverConnected(String version) {
    throw new UnsupportedOperationException();
  }

  @Override
  public void serverError(boolean isFatal, String message, String stackTrace) {
    throw new UnsupportedOperationException();
  }

  @Override
  public void serverIncompatibleVersion(String version) {
    throw new UnsupportedOperationException();
  }

  @Override
  public void serverStatus(AnalysisStatus analysisStatus, PubStatus pubStatus) {
    throw new UnsupportedOperationException();
  }
}
