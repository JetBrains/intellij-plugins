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
import com.google.dart.server.AnalysisServerListener;
import org.dartlang.analysis.server.protocol.*;

import java.util.List;

/**
 * The class {@code BroadcastAnalysisServerListener} implements {@link AnalysisServerListener} that
 * broadcasts events to other listeners.
 * 
 * @coverage dart.server
 */
public class BroadcastAnalysisServerListener implements AnalysisServerListener {
  private final List<AnalysisServerListener> listeners = Lists.newArrayList();

  /**
   * Add the given listener to the list of listeners that will receive notification when new
   * analysis results become available.
   * 
   * @param listener the listener to be added
   */
  public void addListener(AnalysisServerListener listener) {
    synchronized (listeners) {
      if (listeners.contains(listener)) {
        return;
      }
      listeners.add(listener);
    }
  }

  @Override
  public void computedAnalyzedFiles(List<String> directories) {
    for (AnalysisServerListener listener : getListeners()) {
      listener.computedAnalyzedFiles(directories);
    }
  }

  @Override
  public void computedCompletion(String completionId, int replacementOffset, int replacementLength,
      List<CompletionSuggestion> completions, boolean isLast) {
    for (AnalysisServerListener listener : getListeners()) {
      listener.computedCompletion(
          completionId,
          replacementOffset,
          replacementLength,
          completions,
          isLast);
    }
  }

  @Override
  public void computedErrors(String file, List<AnalysisError> errors) {
    for (AnalysisServerListener listener : getListeners()) {
      listener.computedErrors(file, errors);
    }
  }

  @Override
  public void computedHighlights(String file, List<HighlightRegion> highlights) {
    for (AnalysisServerListener listener : getListeners()) {
      listener.computedHighlights(file, highlights);
    }
  }

  @Override
  public void computedImplemented(String file, List<ImplementedClass> implementedClasses,
      List<ImplementedMember> implementedMembers) {
    for (AnalysisServerListener listener : getListeners()) {
      listener.computedImplemented(file, implementedClasses, implementedMembers);
    }
  }

  @Override
  public void computedLaunchData(String file, String kind, String[] referencedFiles) {
    for (AnalysisServerListener listener : getListeners()) {
      listener.computedLaunchData(file, kind, referencedFiles);
    }
  }

  @Override
  public void computedNavigation(String file, List<NavigationRegion> targets) {
    for (AnalysisServerListener listener : getListeners()) {
      listener.computedNavigation(file, targets);
    }
  }

  @Override
  public void computedOccurrences(String file, List<Occurrences> occurrencesArray) {
    for (AnalysisServerListener listener : getListeners()) {
      listener.computedOccurrences(file, occurrencesArray);
    }
  }

  @Override
  public void computedOutline(String file, Outline outline) {
    for (AnalysisServerListener listener : getListeners()) {
      listener.computedOutline(file, outline);
    }
  }

  @Override
  public void computedOverrides(String file, List<OverrideMember> overrides) {
    for (AnalysisServerListener listener : getListeners()) {
      listener.computedOverrides(file, overrides);
    }
  }

  @Override
  public void computedClosingLabels(String file, List<ClosingLabel> labels) {
    for (AnalysisServerListener listener : getListeners()) {
      listener.computedClosingLabels(file, labels);
    }
  }

  @Override
  public void computedSearchResults(String searchId, List<SearchResult> results, boolean last) {
    for (AnalysisServerListener listener : getListeners()) {
      listener.computedSearchResults(searchId, results, last);
    }
  }

  @Override
  public void flushedResults(List<String> files) {
    for (AnalysisServerListener listener : getListeners()) {
      listener.flushedResults(files);
    }
  }

  /**
   * Remove the given listener from the list of listeners that will receive notification when new
   * analysis results become available.
   * 
   * @param listener the listener to be removed
   */
  public void removeListener(AnalysisServerListener listener) {
    synchronized (listeners) {
      listeners.remove(listener);
    }
  }

  @Override
  public void requestError(RequestError requestError) {
    for (AnalysisServerListener listener : getListeners()) {
      listener.requestError(requestError);
    }
  }

  @Override
  public void serverConnected(String version) {
    for (AnalysisServerListener listener : getListeners()) {
      listener.serverConnected(version);
    }
  }

  @Override
  public void serverError(boolean isFatal, String message, String stackTrace) {
    for (AnalysisServerListener listener : getListeners()) {
      listener.serverError(isFatal, message, stackTrace);
    }
  }

  @Override
  public void serverIncompatibleVersion(String version) {
    for (AnalysisServerListener listener : getListeners()) {
      listener.serverIncompatibleVersion(version);
    }
  }

  @Override
  public void serverStatus(AnalysisStatus analysisStatus, PubStatus pubStatus) {
    for (AnalysisServerListener listener : getListeners()) {
      listener.serverStatus(analysisStatus, pubStatus);
    }
  }

  /**
   * Returns an immutable copy of {@link #listeners}.
   */
  private List<AnalysisServerListener> getListeners() {
    synchronized (listeners) {
      return Lists.newArrayList(listeners);
    }
  }
}
