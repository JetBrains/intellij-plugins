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

import com.google.dart.server.generated.AnalysisServer;
import com.google.dart.server.internal.remote.utilities.ResponseUtilities;
import org.dartlang.analysis.server.protocol.*;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * The interface {@code AnalysisServerListener} defines the behavior of objects that listen for
 * results from an analysis server.
 *
 * @coverage dart.server
 */
public interface AnalysisServerListener {
  /**
   * Reports the paths of the files that are being analyzed.
   * <p>
   * This notification is not subscribed to by default. Clients can subscribe by including the value
   * "ANALYZED_FILES" in the list of services passed in an analysis.setGeneralSubscriptions request.
   *
   * @param directories a list of the paths of the files that are being analyzed
   */
  public void computedAnalyzedFiles(List<String> directories);

  public void computedAvailableSuggestions(@NotNull final List<AvailableSuggestionSet> changed,
                                           @NotNull final int[] removed);

  /**
   * A new collection of completions have been computed for the given completion id.
   *
   * @param completionId      the id associated with the completion
   * @param replacementOffset The offset of the start of the text to be replaced. This will be
   *                          different than the offset used to request the completion suggestions if there was a
   *                          portion of an identifier before the original offset. In particular, the
   *                          replacementOffset will be the offset of the beginning of said identifier.
   * @param replacementLength The length of the text to be replaced if the remainder of the
   *                          identifier containing the cursor is to be replaced when the suggestion is applied
   *                          (that is, the number of characters in the existing identifier).
   * @param completions       the completion suggestions being reported
   * @param isLast            {@code true} if this is the last set of results that will be returned for the
   *                          indicated completion
   */
  public void computedCompletion(String completionId,
                                 int replacementOffset,
                                 int replacementLength,
                                 List<CompletionSuggestion> completions,
                                 List<IncludedSuggestionSet> includedSuggestionSets,
                                 List<String> includedSuggestionKinds,
                                 boolean isLast);

  /**
   * Reports the errors associated with a given file.
   *
   * @param file   the file containing the errors
   * @param errors the errors contained in the file
   */
  public void computedErrors(String file, List<AnalysisError> errors);

  /**
   * A new collection of highlight regions has been computed for the given file. Each highlight
   * region represents a particular syntactic or semantic meaning associated with some range. Note
   * that the highlight regions that are returned can overlap other highlight regions if there is
   * more than one meaning associated with a particular region.
   *
   * @param file       the file containing the highlight regions
   * @param highlights the highlight regions contained in the file
   */
  public void computedHighlights(String file, List<HighlightRegion> highlights);

  /**
   * New collections of implemented classes and class members have been computed for the given file.
   *
   * @param file               the file with which the implementations are associated.
   * @param implementedClasses the classes defined in the file that are implemented or extended.
   * @param implementedMembers the member defined in the file that are implemented or overridden.
   */
  public void computedImplemented(String file, List<ImplementedClass> implementedClasses,
                                  List<ImplementedMember> implementedMembers);

  /**
   * New launch data has been computed.
   *
   * @param file            the file for which launch data is being provided
   * @param kind            the kind of the executable file, or {@code null} for non-Dart files
   * @param referencedFiles a list of the Dart files that are referenced by the file, or
   *                        {@code null} for non-HTML files
   */
  public void computedLaunchData(String file, String kind, String[] referencedFiles);

  /**
   * A new collection of navigation regions has been computed for the given file. Each navigation
   * region represents a list of targets associated with some range. The lists will usually contain
   * a single target, but can contain more in the case of a part that is included in multiple
   * libraries or an Dart code that is compiled against multiple versions of a package. Note that
   * the navigation regions that are returned do not overlap other navigation regions.
   *
   * @param file       the file containing the navigation regions
   * @param highlights the highlight regions associated with the source
   */
  public void computedNavigation(String file, List<NavigationRegion> targets);

  /**
   * A new collection of occurrences that been computed for the given file. Each occurrences object
   * represents a list of occurrences for some element in the file.
   *
   * @param file             the file containing the occurrences
   * @param occurrencesArray the array of occurrences in the passed file
   */
  public void computedOccurrences(String file, List<Occurrences> occurrencesArray);

  /**
   * A new outline has been computed for the given file.
   *
   * @param file    the file with which the outline is associated
   * @param outline the outline associated with the file
   */
  public void computedOutline(String file, Outline outline);

  /**
   * A new collection of overrides that have been computed for a given file. Each override array
   * represents a list of overrides for some file.
   *
   * @param file      the file with which the outline is associated
   * @param overrides the overrides associated with the file
   */
  public void computedOverrides(String file, List<OverrideMember> overrides);

  public void computedClosingLabels(String file, List<ClosingLabel> labels);

  /**
   * A new collection of search results have been computed for the given completion id.
   *
   * @param searchId the id associated with the search
   * @param results  the search results being reported
   * @param last     {@code true} if this is the last set of results that will be returned for the
   *                 indicated search
   */
  public void computedSearchResults(String searchId, List<SearchResult> results, boolean last);

  /**
   * Reports that any analysis results that were previously associated with the given files should
   * be considered to be invalid because those files are no longer being analyzed, either because
   * the analysis root that contained it is no longer being analyzed or because the file no longer
   * exists.
   * <p>
   * If a file is included in this notification and at some later time a notification with results
   * for the file is received, clients should assume that the file is once again being analyzed and
   * the information should be processed.
   * <p>
   * It is not possible to subscribe to or unsubscribe from this notification.
   */
  public void flushedResults(List<String> files);

  /**

   */
  public void requestError(RequestError requestError);

  /**
   * Reports that the server is running. This notification is issued once after the server has
   * started running to let the client know that it started correctly.
   *
   * @param version the version of the server that is running
   */
  public void serverConnected(String version);

  /**
   * An error happened in the {@link AnalysisServer}.
   *
   * @param isFatal    {@code true} if the error is a fatal error, meaning that the server will
   *                   shutdown automatically after sending this notification
   * @param message    the error message indicating what kind of error was encountered
   * @param stackTrace the stack trace associated with the generation of the error, used for
   *                   debugging the server
   */
  public void serverError(boolean isFatal, String message, String stackTrace);

  /**
   * Reports that the server version is not compatible with the client version. All the requests
   * will fail with {@link ResponseUtilities#INCOMPATIBLE_SERVER_VERSION} error.
   *
   * @param version is the actual version of the server if not {@code null}, or {@code null} if an
   *                error received as a version.
   */
  public void serverIncompatibleVersion(String version);

  /**
   * Reports the current status of the server.
   *
   * @param analysisStatus the current analysis status of the server, or {@code null} if there is no
   *                       analysis status
   * @param pubStatus      the current pub status of the server, or {@code null} if there is no pub
   *                       status
   */
  public void serverStatus(AnalysisStatus analysisStatus, PubStatus pubStatus);
}
