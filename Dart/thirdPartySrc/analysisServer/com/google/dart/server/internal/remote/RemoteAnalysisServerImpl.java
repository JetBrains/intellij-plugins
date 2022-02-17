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
package com.google.dart.server.internal.remote;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Maps;
import com.google.dart.server.*;
import com.google.dart.server.generated.AnalysisServer;
import com.google.dart.server.internal.BroadcastAnalysisServerListener;
import com.google.dart.server.internal.remote.processor.*;
import com.google.dart.server.internal.remote.utilities.RequestUtilities;
import com.google.dart.server.internal.remote.utilities.ResponseUtilities;
import com.google.dart.server.utilities.general.StringUtilities;
import com.google.dart.server.utilities.instrumentation.Instrumentation;
import com.google.dart.server.utilities.instrumentation.InstrumentationBuilder;
import com.google.dart.server.utilities.logging.Logging;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import org.dartlang.analysis.server.protocol.*;
import org.osgi.framework.Version;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * This {@link AnalysisServer} calls out to the analysis server written in Dart and communicates
 * with the server over standard IO streams.
 *
 * @coverage dart.server.remote
 */
public class RemoteAnalysisServerImpl implements AnalysisServer {
  public static final String SERVER_NOTIFICATION_ERROR = "server.error";

  /**
   * Minimal analysis server version, inclusive.
   */
  private final static Version MIN_SERVER_VERSION = Version.parseVersion("1.9.0");

  /**
   * Maximal analysis server version, exclusive.
   */
  private final static Version MAX_SERVER_VERSION = Version.parseVersion("2.0.0");

  // Server domain
  private static final String SERVER_NOTIFICATION_CONNECTED = "server.connected";
  private static final String SERVER_NOTIFICATION_STATUS = "server.status";

  // Analysis domain
  private static final String ANALYSIS_NOTIFICATION_ANALYZED_FILES = "analysis.analyzedFiles";
  private static final String ANALYSIS_NOTIFICATION_ERRORS = "analysis.errors";
  private static final String ANALYSIS_NOTIFICATION_FLUSH_RESULTS = "analysis.flushResults";
  private static final String ANALYSIS_NOTIFICATION_HIGHTLIGHTS = "analysis.highlights";
  private static final String ANALYSIS_NOTIFICATION_IMPLEMENTED = "analysis.implemented";
  private static final String ANALYSIS_NOTIFICATION_NAVIGATION = "analysis.navigation";
  private static final String ANALYSIS_NOTIFICATION_OCCURRENCES = "analysis.occurrences";
  private static final String ANALYSIS_NOTIFICATION_OUTLINE = "analysis.outline";
  private static final String ANALYSIS_NOTIFICATION_OVERRIDES = "analysis.overrides";
  private static final String ANALYSIS_NOTIFICATION_CLOSING_LABELS = "analysis.closingLabels";

  // Code Completion domain
  private static final String COMPLETION_AVAILABLE_SUGGESTIONS = "completion.availableSuggestions";
  private static final String COMPLETION_EXISTING_IMPORTS = "completion.existingImports";
  private static final String COMPLETION_NOTIFICATION_RESULTS = "completion.results";

  // Search domain
  private static final String SEARCH_NOTIFICATION_RESULTS = "search.results";

  // Execution domain
  private static final String LAUNCH_DATA_NOTIFICATION_RESULTS = "execution.launchData";
  private final AnalysisServerSocket socket;
  private final Object requestSinkLock = new Object();
  private RequestSink requestSink;
  private ResponseStream responseStream;
  private LineReaderStream errorStream;
  private final AtomicLong lastResponseTime = new AtomicLong(0);
  private final AtomicLong lastRequestTime = new AtomicLong(0);

  /**
   * The listener that will receive notification when new analysis results become available.
   */
  private final BroadcastAnalysisServerListener listener = new BroadcastAnalysisServerListener();

  private final List<RequestListener> requestListenerList = new ArrayList<>();

  private final List<ResponseListener> responseListenerList = new ArrayList<>();

  private final List<AnalysisServerStatusListener> statusListenerList = new ArrayList<AnalysisServerStatusListener>();

  /**
   * A mapping between {@link String} ids' and the associated {@link Consumer} that was passed when
   * the request was made.
   */
  private final Map<String, Consumer> consumerMap = Maps.newHashMap();

  /**
   * The object used to synchronize access to {@link #consumerMap}.
   */
  private final Object consumerMapLock = new Object();

  /**
   * The unique ID for the next request.
   */
  private final AtomicInteger nextId = new AtomicInteger();

  /**
   * A mapping between "getRefactoring" request ids and the requested refactoring kinds.
   */
  private final Map<String, String> requestToRefactoringKindMap = Maps.newHashMap();

  /**
   * The thread that restarts an unresponsive server or {@code null} if it has not been started.
   */
  private Thread watcher;

  /**
   * A flag indicating whether the watcher should continue monitoring the remote process.
   */
  private boolean watch;

  /**
   * A flag indicating whether the server shutdown process has been requested.
   */
  private boolean shutdownRequested;

  /**
   * Check server version is {@code true} by default, but can be set to {@code false} to disable the
   * check that the server that has been started is less than {@link #MAX_SERVER_VERSION}.
   */
  private final boolean checkServerVersion;

  public RemoteAnalysisServerImpl(AnalysisServerSocket socket) {
    this(socket, true);
  }

  public RemoteAnalysisServerImpl(AnalysisServerSocket socket, boolean checkServerVersion) {
    this.socket = socket;
    this.checkServerVersion = checkServerVersion;
  }

  @Override
  public void addAnalysisServerListener(AnalysisServerListener listener) {
    this.listener.addListener(listener);
  }

  @Override
  public void addRequestListener(RequestListener listener) {
    synchronized (requestListenerList) {
      if (!requestListenerList.contains(listener)) {
        requestListenerList.add(listener);
      }
    }
  }

  @Override
  public void addResponseListener(ResponseListener listener) {
    synchronized (responseListenerList) {
      if (!responseListenerList.contains(listener)) {
        responseListenerList.add(listener);
      }
    }
  }

  @Override
  public void addStatusListener(AnalysisServerStatusListener listener) {
    statusListenerList.add(listener);
  }

  @Override
  public void analysis_getErrors(String file, GetErrorsConsumer consumer) {
    String id = generateUniqueId();
    sendRequestToServer(id, RequestUtilities.generateAnalysisGetErrors(id, file), consumer);
  }

  @Override
  public void analysis_getHover(String file, int offset, GetHoverConsumer consumer) {
    String id = generateUniqueId();
    sendRequestToServer(id, RequestUtilities.generateAnalysisGetHover(id, file, offset), consumer);
  }

  @Override
  public void analysis_getImportedElements(String file, int offset, int length, GetImportedElementsConsumer consumer) {
    String id = generateUniqueId();
    sendRequestToServer(id, RequestUtilities.generateAnalysisGetImportedElements(id, file, offset, length), consumer);
  }

  @Override
  public void analysis_getLibraryDependencies(GetLibraryDependenciesConsumer consumer) {
    String id = generateUniqueId();
    sendRequestToServer(id, RequestUtilities.generateAnalysisGetLibraryDependencies(id), consumer);
  }

  @Override
  public void analysis_getNavigation(String file, int offset, int length, GetNavigationConsumer consumer) {
    String id = generateUniqueId();
    sendRequestToServer(id, RequestUtilities.generateAnalysisGetNavigation(id, file, offset, length), consumer);
  }

  @Override
  public void analysis_getReachableSources(String file, GetReachableSourcesConsumer consumer) {
    // TODO(scheglov) implement
  }

  @Override
  public void analysis_getSignature(String file, int offset, GetSignatureConsumer consumer) {
  }

  @Override
  public void analysis_reanalyze() {
    String id = generateUniqueId();
    sendRequestToServer(id, RequestUtilities.generateAnalysisReanalyze(id));
  }

  @Override
  public void analysis_setAnalysisRoots(List<String> includedPaths, List<String> excludedPaths, Map<String, String> packageRoots) {
    String id = generateUniqueId();
    if (includedPaths == null) {
      includedPaths = StringUtilities.EMPTY_LIST;
    }
    if (excludedPaths == null) {
      excludedPaths = StringUtilities.EMPTY_LIST;
    }
    sendRequestToServer(id, RequestUtilities.generateAnalysisSetAnalysisRoots(id, includedPaths, excludedPaths, packageRoots));
  }

  @Override
  public void analysis_setGeneralSubscriptions(List<String> subscriptions) {
    String id = generateUniqueId();
    if (subscriptions == null) {
      subscriptions = StringUtilities.EMPTY_LIST;
    }
    sendRequestToServer(id, RequestUtilities.generateAnalysisSetGeneralSubscriptions(id, subscriptions));
  }

  @Override
  public void analysis_setPriorityFiles(List<String> files) {
    String id = generateUniqueId();
    if (files == null) {
      files = StringUtilities.EMPTY_LIST;
    }
    sendRequestToServer(id, RequestUtilities.generateAnalysisSetPriorityFiles(id, files));
  }

  @Override
  public void analysis_setSubscriptions(Map<String, List<String>> subscriptions) {
    String id = generateUniqueId();
    if (subscriptions == null) {
      subscriptions = Maps.newHashMap();
    }
    sendRequestToServer(id, RequestUtilities.generateAnalysisSetSubscriptions(id, subscriptions));
  }

  @Override
  public void analysis_updateContent(Map<String, Object> files, UpdateContentConsumer consumer) {
    String id = generateUniqueId();
    if (files == null) {
      files = Maps.newHashMap();
    }
    sendRequestToServer(id, RequestUtilities.generateAnalysisUpdateContent(id, files), consumer);
  }

  @Override
  public void analysis_updateOptions(AnalysisOptions options) {
    String id = generateUniqueId();
    sendRequestToServer(id, RequestUtilities.generateAnalysisUpdateOptions(id, options));
  }

  @Override
  public void analytics_enable(boolean value) {
    String id = generateUniqueId();
    sendRequestToServer(id, RequestUtilities.generateAnalyticsEnable(id, value));
  }

  @Override
  public void analytics_isEnabled(IsEnabledConsumer consumer) {
    String id = generateUniqueId();
    sendRequestToServer(id, RequestUtilities.generateAnalyticsIsEnabled(id), consumer);
  }

  @Override
  public void analytics_sendEvent(String action) {
    String id = generateUniqueId();
    sendRequestToServer(id, RequestUtilities.generateAnalyticsSendEvent(id, action));
  }

  @Override
  public void analytics_sendTiming(String event, int millis) {
    String id = generateUniqueId();
    sendRequestToServer(id, RequestUtilities.generateAnalyticsSendTiming(id, event, millis));
  }

  @Override
  public void completion_registerLibraryPaths(List<LibraryPathSet> paths) {
    // this call is now deprecated in the Analysis Server, a future syncs with the protocol will remove this method
  }

  @Override
  public void completion_getSuggestionDetails(String file, int id, String label, int offset, GetSuggestionDetailsConsumer consumer) {
    String requestId = generateUniqueId();
    sendRequestToServer(requestId, RequestUtilities.generateCompletionGetSuggestionDetails(requestId, file, id, label, offset), consumer);
  }

  @Override
  public void completion_getSuggestionDetails2(String file,
                                               int offset,
                                               String completion,
                                               String libraryUri,
                                               GetSuggestionDetailsConsumer2 consumer) {
    String requestId = generateUniqueId();
    sendRequestToServer(requestId,
                        RequestUtilities.generateCompletionGetSuggestionDetails2(requestId, file, offset, completion, libraryUri),
                        consumer);
  }

  @Override
  public void completion_getSuggestions(String file, int offset, GetSuggestionsConsumer consumer) {
    String id = generateUniqueId();
    sendRequestToServer(id, RequestUtilities.generateCompletionGetSuggestions(id, file, offset), consumer);
  }

  @Override
  public void completion_getSuggestions2(String file,
                                         int offset,
                                         int maxResults,
                                         String completionCaseMatchingMode,
                                         String completionMode,
                                         int invocationCount,
                                         int timeout,
                                         GetSuggestionsConsumer2 consumer) {
    String id = generateUniqueId();
    sendRequestToServer(id, RequestUtilities.generateCompletionGetSuggestions2(id, file, offset, maxResults, completionCaseMatchingMode, completionMode, invocationCount, timeout), consumer);
  }

  @Override
  public void completion_setSubscriptions(List<String> subscriptions) {
    String id = generateUniqueId();
    if (subscriptions == null) {
      subscriptions = StringUtilities.EMPTY_LIST;
    }
    sendRequestToServer(id, RequestUtilities.generateCompletionSetSubscriptions(id, subscriptions));
  }


  @Override
  public void diagnostic_getDiagnostics(GetDiagnosticsConsumer consumer) {
    // TODO(scheglov) implement
  }

  public void diagnostic_getServerPort(GetServerPortConsumer consumer) {
    String id = generateUniqueId();
    sendRequestToServer(id, RequestUtilities.generateDiagnosticGetServerPort(id), consumer);
  }

  @Override
  public void edit_bulkFixes(List<String> included, boolean inTestMode, BulkFixesConsumer consumer) { }

  @Override
  public void edit_format(String file, int selectionOffset, int selectionLength, int lineLength, FormatConsumer consumer) {
    String id = generateUniqueId();
    sendRequestToServer(id, RequestUtilities.generateEditFormat(id, file, selectionOffset, selectionLength, lineLength), consumer);
  }

  @Override
  public void edit_getAssists(String file, int offset, int length, GetAssistsConsumer consumer) {
    String id = generateUniqueId();
    sendRequestToServer(id, RequestUtilities.generateEditGetAssists(id, file, offset, length), consumer);
  }

  @Override
  public void edit_getAvailableRefactorings(String file, int offset, int length, GetAvailableRefactoringsConsumer consumer) {
    String id = generateUniqueId();
    sendRequestToServer(id, RequestUtilities.generateEditGetAvaliableRefactorings(id, file, offset, length), consumer);
  }

  @Override
  public void edit_getFixes(String file, int offset, GetFixesConsumer consumer) {
    String id = generateUniqueId();
    sendRequestToServer(id, RequestUtilities.generateEditGetFixes(id, file, offset), consumer);
  }

  public void edit_isPostfixCompletionApplicable(String path, String key, int offset, IsPostfixCompletionApplicableConsumer consumer) {
    String id = generateUniqueId();
    sendRequestToServer(id, RequestUtilities.generateIsPostfixCompletionApplicable(id, path, offset, key), consumer);
  }

  public void edit_listPostfixCompletionTemplates(ListPostfixCompletionTemplatesConsumer consumer) {
    String id = generateUniqueId();
    sendRequestToServer(id, RequestUtilities.generateListPostfixCompletionTeamplates(id), consumer);
  }

  public void edit_getPostfixCompletion(String file, String key, int offset, GetPostfixCompletionConsumer consumer) {
    String id = generateUniqueId();
    sendRequestToServer(id, RequestUtilities.generateEditPostfixCompletion(id, file, offset, key), consumer);
  }

  @Override
  public void edit_getRefactoring(String kindId,
                                  String file,
                                  int offset,
                                  int length,
                                  boolean validateOnly,
                                  RefactoringOptions options,
                                  GetRefactoringConsumer consumer) {
    String id = generateUniqueId();
    requestToRefactoringKindMap.put(id, kindId);
    sendRequestToServer(id, RequestUtilities.generateEditGetRefactoring(id, kindId, file, offset, length, validateOnly, options), consumer);
  }

  @Override
  public void edit_getStatementCompletion(String file, int offset, GetStatementCompletionConsumer consumer) {
    String id = generateUniqueId();
    sendRequestToServer(id, RequestUtilities.generateEditStatementCompletion(id, file, offset), consumer);
  }

  @Override
  public void edit_importElements(String file, List<ImportedElements> elements, int offset, ImportElementsConsumer consumer) {
    String id = generateUniqueId();
    sendRequestToServer(id, RequestUtilities.generateEditImportElements(id, file, elements, offset), consumer);
  }

  @Override
  public void edit_organizeDirectives(String file, OrganizeDirectivesConsumer consumer) {
    String id = generateUniqueId();
    sendRequestToServer(id, RequestUtilities.generateEditOrganizeDirectives(id, file), consumer);
  }

  @Override
  public void edit_sortMembers(String file, SortMembersConsumer consumer) {
    String id = generateUniqueId();
    sendRequestToServer(id, RequestUtilities.generateEditSortMembers(id, file), consumer);
  }

  @Override
  public void execution_createContext(String contextRoot, CreateContextConsumer consumer) {
    String id = generateUniqueId();
    sendRequestToServer(id, RequestUtilities.generateExecutionCreateContext(id, contextRoot), consumer);
  }

  @Override
  public void execution_deleteContext(String contextId) {
    String id = generateUniqueId();
    sendRequestToServer(id, RequestUtilities.generateExecutionDeleteContext(id, contextId));
  }

  @Override
  public void execution_getSuggestions(String code,
                                       int offset,
                                       String contextFile,
                                       int contextOffset,
                                       List<RuntimeCompletionVariable> variables,
                                       List<RuntimeCompletionExpression> expressions,
                                       GetRuntimeCompletionConsumer consumer) {
    String id = generateUniqueId();
    sendRequestToServer(id,
                        RequestUtilities.generateExecutionGetSuggestions(
                          id,
                          code, offset,
                          contextFile, contextOffset,
                          variables, expressions),
                        consumer);
  }

  @Override
  public void execution_mapUri(String contextId, String file, String uri, MapUriConsumer consumer) {
    String id = generateUniqueId();
    sendRequestToServer(id, RequestUtilities.generateExecutionMapUri(id, contextId, file, uri), consumer);
  }

  @Override
  public void execution_setSubscriptions(List<String> subscriptions) {
    String id = generateUniqueId();
    if (subscriptions == null) {
      subscriptions = StringUtilities.EMPTY_LIST;
    }
    sendRequestToServer(id, RequestUtilities.generateExecutionSetSubscriptions(id, subscriptions));
  }

  @Override
  public void flutter_getWidgetDescription(String file, int offset, GetWidgetDescriptionConsumer consumer) { }

  @Override
  public void flutter_setSubscriptions(Map<String, List<String>> subscriptions) { }

  public void flutter_setWidgetPropertyValue(int id, FlutterWidgetPropertyValue value, SetWidgetPropertyValueConsumer consumer) { }

  @Override
  public boolean isSocketOpen() {
    return socket.isOpen();
  }

  @Override
  public void kythe_getKytheEntries(String file, GetKytheEntriesConsumer consumer) {
  }

  @Override
  public void removeAnalysisServerListener(AnalysisServerListener listener) {
    this.listener.removeListener(listener);
  }

  @Override
  public void removeRequestListener(RequestListener listener) {
    synchronized (requestListenerList) {
      requestListenerList.remove(listener);
    }
  }

  @Override
  public void removeResponseListener(ResponseListener listener) {
    synchronized (responseListenerList) {
      responseListenerList.remove(listener);
    }
  }

  @Override
  public void search_findElementReferences(String file, int offset, boolean includePotential, FindElementReferencesConsumer consumer) {
    String id = generateUniqueId();
    sendRequestToServer(id, RequestUtilities.generateSearchFindElementReferences(id, file, offset, includePotential), consumer);
  }

  @Override
  public void search_findMemberDeclarations(String name, FindMemberDeclarationsConsumer consumer) {
    String id = generateUniqueId();
    sendRequestToServer(id, RequestUtilities.generateSearchFindMemberDeclarations(id, name), consumer);
  }

  @Override
  public void search_findMemberReferences(String name, FindMemberReferencesConsumer consumer) {
    String id = generateUniqueId();
    sendRequestToServer(id, RequestUtilities.generateSearchFindMemberReferences(id, name), consumer);
  }

  @Override
  public void search_findTopLevelDeclarations(String pattern, FindTopLevelDeclarationsConsumer consumer) {
    String id = generateUniqueId();
    sendRequestToServer(id, RequestUtilities.generateSearchFindTopLevelDeclarations(id, pattern), consumer);
  }

  @Override
  public void search_getElementDeclarations(String file, String pattern, int maxResults, GetElementDeclarationsConsumer consumer) {
  }

  @Override
  public void search_getTypeHierarchy(String file, int offset, boolean superOnly, GetTypeHierarchyConsumer consumer) {
    String id = generateUniqueId();
    sendRequestToServer(id, RequestUtilities.generateSearchGetTypeHierarchy(id, file, offset, superOnly), consumer);
  }

  @Override
  public void server_cancelRequest(String id) { }

  @Override
  public void server_getVersion(GetVersionConsumer consumer) {
    String id = generateUniqueId();
    sendRequestToServer(id, RequestUtilities.generateServerGetVersion(id), consumer);
  }

  @Override
  public void server_setSubscriptions(List<String> subscriptions) {
    String id = generateUniqueId();
    if (subscriptions == null) {
      subscriptions = StringUtilities.EMPTY_LIST;
    }
    sendRequestToServer(id, RequestUtilities.generateServerSetSubscriptions(id, subscriptions));
  }

  @Override
  public void server_shutdown() {
    shutdownRequested = true;
    stopWatcher();
    String id = generateUniqueId();
    sendRequestToServer(id, RequestUtilities.generateServerShutdown(id), new BasicConsumer() {
      @Override
      public void received() {
        // Close communication channels once response has been received
        requestSink.close();
        for (AnalysisServerStatusListener listener : statusListenerList) {
          listener.isAliveServer(false);
        }
      }
    });
    stopServer();
  }

  /**
   * Starts the analysis server.
   *
   * @throws ServerVersionMismatchException if the underlying analysis server doesn't match to a
   *                                        server version that this Java API can communicate with.
   */
  @Override
  public void start() throws Exception {
    startServer();
    startWatcher(5000);
  }

  @VisibleForTesting
  public void test_waitForWorkerComplete() {
    while (!consumerMap.isEmpty()) {
      Thread.yield();
    }
  }

  /**
   * Generate and return a unique {@link String} id to be used in the requests sent to the analysis
   * server.
   *
   * @return a unique {@link String} id to be used in the requests sent to the analysis server
   */
  public String generateUniqueId() {
    return Integer.toString(nextId.getAndIncrement());
  }

  /**
   * Attempts to handle the given {@link JsonObject} as a notification. Return {@code true} if it
   * was handled, otherwise {@code false} is returned.
   *
   * @return {@code true} if it was handled, otherwise {@code false} is returned
   */
  private boolean processNotification(JsonObject response) throws Exception {
    // prepare notification kind
    JsonElement eventElement = response.get("event");
    if (eventElement == null || !eventElement.isJsonPrimitive()) {
      return false;
    }
    String event = eventElement.getAsString();
    // handle each supported notification kind
    if (event.equals(ANALYSIS_NOTIFICATION_ERRORS)) {
      // analysis.errors
      new NotificationAnalysisErrorsProcessor(listener).process(response);
    }
    else if (event.equals(ANALYSIS_NOTIFICATION_FLUSH_RESULTS)) {
      // analysis.flushResults
      new NotificationAnalysisFlushResultsProcessor(listener).process(response);
    }
    else if (event.equals(ANALYSIS_NOTIFICATION_HIGHTLIGHTS)) {
      // analysis.highlights
      new NotificationAnalysisHighlightsProcessor(listener).process(response);
    }
    else if (event.equals(ANALYSIS_NOTIFICATION_IMPLEMENTED)) {
      // analysis.implemented
      new NotificationAnalysisImplementedProcessor(listener).process(response);
    }
    else if (event.equals(ANALYSIS_NOTIFICATION_NAVIGATION)) {
      // analysis.navigation
      new NotificationAnalysisNavigationProcessor(listener).process(response);
    }
    else if (event.equals(ANALYSIS_NOTIFICATION_OCCURRENCES)) {
      // analysis.occurrences
      new NotificationAnalysisOccurrencesProcessor(listener).process(response);
    }
    else if (event.equals(ANALYSIS_NOTIFICATION_OUTLINE)) {
      // analysis.outline
      new NotificationAnalysisOutlineProcessor(listener).process(response);
    }
    else if (event.equals(ANALYSIS_NOTIFICATION_OVERRIDES)) {
      // analysis.overrides
      new NotificationAnalysisOverridesProcessor(listener).process(response);
    }
    else if (event.equals(ANALYSIS_NOTIFICATION_CLOSING_LABELS)) {
      // analysis.closingLabels
      new NotificationAnalysisClosingLabelsProcessor(listener).process(response);
    }
    else if (event.equals(ANALYSIS_NOTIFICATION_ANALYZED_FILES)) {
      // analysis.errors
      new NotificationAnalysisAnalyzedFilesProcessor(listener).process(response);
    }
    else if (event.equals(COMPLETION_AVAILABLE_SUGGESTIONS)) {
      // completion.results
      new NotificationCompletionAvailableSuggestionsProcessor(listener).process(response);
    }
    else if (event.equals(COMPLETION_EXISTING_IMPORTS)) {
      // completion.existingImports
      new NotificationCompletionExistingImportsProcessor(listener).process(response);
    }
    else if (event.equals(COMPLETION_NOTIFICATION_RESULTS)) {
      // completion.results
      new NotificationCompletionResultsProcessor(listener).process(response);
    }
    else if (event.equals(SEARCH_NOTIFICATION_RESULTS)) {
      // search.results
      new NotificationSearchResultsProcessor(listener).process(response);
    }
    else if (event.equals(SERVER_NOTIFICATION_STATUS)) {
      // server.status
      new NotificationServerStatusProcessor(listener).process(response);
    }
    else if (event.equals(SERVER_NOTIFICATION_ERROR)) {
      // server.error
      new NotificationServerErrorProcessor(listener).process(response);
    }
    else if (event.equals(SERVER_NOTIFICATION_CONNECTED)) {
      // server.connected
      new NotificationServerConnectedProcessor(listener).process(response);
    }
    else if (event.equals(LAUNCH_DATA_NOTIFICATION_RESULTS)) {
      new NotificationExecutionLaunchDataProcessor(listener).process(response);
    }
    // it is a notification, even if we did not handle it
    return true;
  }

  private void processResponse(JsonObject response) throws Exception {
    notifyResponseListeners(response);
    // handle notification
    if (processNotification(response)) {
      return;
    }
    // prepare ID
    JsonPrimitive idJsonPrimitive = (JsonPrimitive)response.get("id");
    if (idJsonPrimitive == null) {
      return;
    }
    String idString = idJsonPrimitive.getAsString();
    // prepare consumer
    Consumer consumer;
    synchronized (consumerMapLock) {
      consumer = consumerMap.get(idString);
    }
    JsonObject errorObject = (JsonObject)response.get("error");
    RequestError requestError = null;
    if (errorObject != null) {
      requestError = processErrorResponse(errorObject);
      listener.requestError(requestError);
    }

    // handle result
    JsonObject resultObject = (JsonObject)response.get("result");

    //
    // Analysis Domain
    //
    if (consumer instanceof UpdateContentConsumer) {
      ((UpdateContentConsumer)consumer).onResponse();
    }
    //
    // Completion Domain
    //
    else if (consumer instanceof GetSuggestionDetailsConsumer) {
      new GetSuggestionDetailsProcessor((GetSuggestionDetailsConsumer)consumer).process(resultObject, requestError);
    }
    else if (consumer instanceof GetSuggestionsConsumer) {
      new CompletionIdProcessor((GetSuggestionsConsumer)consumer).process(resultObject, requestError);
    }
    else if (consumer instanceof GetSuggestionDetailsConsumer2) {
      new GetSuggestionDetailsProcessor2((GetSuggestionDetailsConsumer2)consumer).process(resultObject, requestError);
    }
    else if (consumer instanceof GetSuggestionsConsumer2) {
      new CompletionIdProcessor2((GetSuggestionsConsumer2)consumer).process(resultObject, requestError);
    }
    //
    // Search Domain
    //
    else if (consumer instanceof FindElementReferencesConsumer) {
      new FindElementReferencesProcessor((FindElementReferencesConsumer)consumer).process(resultObject, requestError);
    }
    else if (consumer instanceof FindMemberDeclarationsConsumer) {
      new FindMemberDeclarationsProcessor((FindMemberDeclarationsConsumer)consumer).process(resultObject, requestError);
    }
    else if (consumer instanceof FindMemberReferencesConsumer) {
      new FindMemberReferencesProcessor((FindMemberReferencesConsumer)consumer).process(resultObject, requestError);
    }
    else if (consumer instanceof FindTopLevelDeclarationsConsumer) {
      new FindTopLevelDeclarationsProcessor((FindTopLevelDeclarationsConsumer)consumer).process(resultObject, requestError);
    }
    else if (consumer instanceof GetTypeHierarchyConsumer) {
      new TypeHierarchyProcessor((GetTypeHierarchyConsumer)consumer).process(resultObject, requestError);
    }
    //
    // Edit Domain
    //
    else if (consumer instanceof FormatConsumer) {
      new FormatProcessor((FormatConsumer)consumer).process(resultObject, requestError);
    }
    else if (consumer instanceof GetHoverConsumer) {
      new HoverProcessor((GetHoverConsumer)consumer).process(resultObject, requestError);
    }
    else if (consumer instanceof GetRefactoringConsumer) {
      new GetRefactoringProcessor(requestToRefactoringKindMap, (GetRefactoringConsumer)consumer)
        .process(idString, resultObject, requestError);
    }
    else if (consumer instanceof GetAssistsConsumer) {
      new AssistsProcessor((GetAssistsConsumer)consumer).process(resultObject, requestError);
    }
    else if (consumer instanceof GetFixesConsumer) {
      new FixesProcessor((GetFixesConsumer)consumer).process(resultObject, requestError);
    }
    else if (consumer instanceof GetStatementCompletionConsumer) {
      new StatementCompletionProcessor((GetStatementCompletionConsumer)consumer).process(resultObject, requestError);
    }
    else if (consumer instanceof GetPostfixCompletionConsumer) {
      new PostfixCompletionProcessor((GetPostfixCompletionConsumer)consumer).process(resultObject, requestError);
    }
    else if (consumer instanceof IsPostfixCompletionApplicableConsumer) {
      new IsPostfixCompletionApplicableProcessor((IsPostfixCompletionApplicableConsumer)consumer).process(resultObject, requestError);
    }
    else if (consumer instanceof ListPostfixCompletionTemplatesConsumer) {
      new ListPostfixCompletionTemplatesProcessor((ListPostfixCompletionTemplatesConsumer)consumer).process(resultObject, requestError);
    }
    else if (consumer instanceof GetImportedElementsConsumer) {
      new GetImportedElementsProcessor((GetImportedElementsConsumer)consumer).process(resultObject, requestError);
    }
    else if (consumer instanceof GetLibraryDependenciesConsumer) {
      new LibraryDependenciesProcessor((GetLibraryDependenciesConsumer)consumer).process(resultObject, requestError);
    }
    else if (consumer instanceof GetNavigationConsumer) {
      new GetNavigationProcessor((GetNavigationConsumer)consumer).process(resultObject, requestError);
    }
    else if (consumer instanceof GetAvailableRefactoringsConsumer) {
      new RefactoringGetAvailableProcessor((GetAvailableRefactoringsConsumer)consumer).process(resultObject, requestError);
    }
    else if (consumer instanceof GetErrorsConsumer) {
      new AnalysisErrorsProcessor((GetErrorsConsumer)consumer).process(resultObject, requestError);
    }
    else if (consumer instanceof ImportElementsConsumer) {
      new ImportElementsProcessor((ImportElementsConsumer)consumer).process(resultObject, requestError);
    }
    else if (consumer instanceof OrganizeDirectivesConsumer) {
      new OrganizeDirectivesProcessor((OrganizeDirectivesConsumer)consumer).process(resultObject, requestError);
    }
    else if (consumer instanceof SortMembersConsumer) {
      new SortMembersProcessor((SortMembersConsumer)consumer).process(resultObject, requestError);
    }
    //
    // Execution Domain
    //
    else if (consumer instanceof CreateContextConsumer) {
      new CreateContextProcessor((CreateContextConsumer)consumer).process(resultObject, requestError);
    }
    else if (consumer instanceof GetRuntimeCompletionConsumer) {
      new GetRuntimeCompletionProcessor((GetRuntimeCompletionConsumer)consumer).process(resultObject, requestError);
    }
    else if (consumer instanceof MapUriConsumer) {
      new MapUriProcessor((MapUriConsumer)consumer).process(resultObject, requestError);
    }
    //
    // Diagnostic Domain
    //
    else if (consumer instanceof GetServerPortConsumer) {
      new GetServerPortProcessor((GetServerPortConsumer)consumer).process(resultObject, requestError);
    }
    //
    // Server Domain
    //
    else if (consumer instanceof GetVersionConsumer) {
      new VersionProcessor((GetVersionConsumer)consumer).process(resultObject, requestError);
    }
    else if (consumer instanceof BasicConsumer) {
      ((BasicConsumer)consumer).received();
    }
    else if (consumer instanceof JsonConsumer) {
      ((JsonConsumer)consumer).onResponse(resultObject, requestError);
    }

    synchronized (consumerMapLock) {
      consumerMap.remove(idString);
    }
  }

  private void notifyRequestListeners(JsonObject request) {
    synchronized (requestListenerList) {
      List<RequestListener> listeners = ImmutableList.copyOf(requestListenerList);
      for (RequestListener listener : listeners) {
        listener.onRequest(request.toString());
      }
    }
  }

  private void notifyResponseListeners(JsonObject response) {
    synchronized (responseListenerList) {
      List<ResponseListener> listeners = ImmutableList.copyOf(responseListenerList);
      for (ResponseListener listener : listeners) {
        listener.onResponse(response.toString());
      }
    }
  }

  /**
   * Sends the request, and associates the request with a {@link LocalConsumer}, a simple consumer
   * which only holds onto the the request {@link JsonObject}, for the purposes of error reporting.
   *
   * @param id      the identifier of the request
   * @param request the request to send
   */
  public void sendRequestToServer(String id, JsonObject request) {
    sendRequestToServer(id, request, new LocalConsumer(request));
  }

  /**
   * Sends the request and associates the request with the passed {@link Consumer}.
   *
   * @param id       the identifier of the request
   * @param request  the request to send
   * @param consumer the {@link Consumer} to process a response
   */
  public void sendRequestToServer(String id, JsonObject request, Consumer consumer) {
    notifyRequestListeners(request);
    synchronized (consumerMapLock) {
      consumerMap.put(id, consumer);
    }
    lastRequestTime.set(System.currentTimeMillis());
    synchronized (requestSinkLock) {
      requestSink.add(request);
    }
  }

  private void startServer() throws Exception {
    socket.start();
    consumerMap.clear();
    requestSink = socket.getRequestSink();
    responseStream = socket.getResponseStream();
    errorStream = socket.getErrorStream();
    new ServerResponseReaderThread(responseStream).start();
    if (errorStream != null) {
      new ServerErrorReaderThread(errorStream, listener).start();
    }
    if (checkServerVersion) {
      final BlockingRequestSink blockRequestSink = new BlockingRequestSink(requestSink);
      requestSink = blockRequestSink;
      server_getVersion(new GetVersionConsumer() {
        @Override
        public void computedVersion(String versionStr) {
          String message = null;
          // parse version
          Version version = null;
          try {
            version = Version.parseVersion(versionStr);
          }
          catch (Throwable e) {
            message = "Unable to parse version: " + versionStr;
          }
          // invalid version
          if (version != null) {
            if (version.compareTo(MIN_SERVER_VERSION) < 0 || version.compareTo(MAX_SERVER_VERSION) >= 0) {
              message = "This version of the com.google.dart.server project can communicate only " +
                        "with server versions between " +
                        MIN_SERVER_VERSION +
                        " and " +
                        MAX_SERVER_VERSION +
                        ", but the version read from the server is " +
                        version +
                        ".";
            }
          }
          // OK
          if (message == null) {
            synchronized (requestSinkLock) {
              requestSink = blockRequestSink.toPassthroughSink();
              return;
            }
          }
          // report error
          Logging.getLogger().logError(message);
          listener.serverIncompatibleVersion(versionStr);
          sendErrorForEveryRequest(versionStr);
        }

        @Override
        public void onError(RequestError requestError) {
          Logging.getLogger().logError("No version received from the server.");
          listener.serverIncompatibleVersion(null);
          sendErrorForEveryRequest(null);
        }

        private void sendErrorForEveryRequest(String version) {
          String message = "Incompatible server version: " + version;
          synchronized (requestSinkLock) {
            requestSink = blockRequestSink.toErrorSink(new ResponseSink() {
              @Override
              public void add(JsonObject response) throws Exception {
                processResponse(response);
              }
            }, ResponseUtilities.INCOMPATIBLE_SERVER_VERSION, message);
          }
          server_shutdown();
        }
      });
    }
  }

  private void startWatcher(final long millisToRestart) {
    if (millisToRestart <= 0 || watcher != null) {
      return;
    }
    watch = true;
    watcher = new Thread(getClass().getSimpleName() + " watcher") {
      @Override
      public void run() {
        watch(millisToRestart);
      }
    };
    watcher.setDaemon(true);
    watcher.start();
  }

  private void stopServer() {
    socket.stop();
  }

  private void stopWatcher() {
    if (watcher == null) {
      return;
    }
    watch = false;
    watcher.interrupt();
    try {
      watcher.join(5000);
    }
    catch (InterruptedException e) {
      //$FALL-THROUGH$
    }
    watcher = null;
  }

  private void watch(long millisToRestart) {
//    long restartTime = System.currentTimeMillis();
//    int restartCount = 0;
    while (watch) {
      if (isSocketOpen()) {
        sleep(millisToRestart / 2);
      }
      else {
        // If still no response from server then restart the server
        InstrumentationBuilder instrumentation = Instrumentation.builder("RemoteAnalysisServerImpl.serverNotRunning");
        for (AnalysisServerStatusListener listener : statusListenerList) {
          listener.isAliveServer(false);
        }
        instrumentation.log();
        watch = false;
//        try {
//          stopServer();
//
//          // If the analysis server has been restarted several times in a 5 minute period, then give up
//          long now = System.currentTimeMillis();
//          if (now - restartTime < 5 * 60 * 1000) {
//            if (++restartCount > 3) {
//              Logging.getLogger().logError(
//                  "Restarted analysis server several times in a short period of time. Giving up.");
//              instrumentation.metric("restartedAnalysisServer", false);
//              break;
//            }
//          } else {
//            restartTime = now;
//            restartCount = 0;
//          }
//
//          startServer();
//        } catch (Exception e) {
//          // Bail out if cannot restart the server
//          Logging.getLogger().logError("Failed to restart analysis server", e);
//          instrumentation.record(e);
//          break;
//        } finally {
//          instrumentation.log();
//        }
//        sentRequest = false;
//        sleep(millisToRestart);
      }
    }
  }

  public long getLastRequestMillis() {
    return lastRequestTime.get();
  }

  public long getLastResponseMillis() {
    long millis = lastResponseTime.get();
    if (millis == 0L) {
      // If the first request comes before the first response set the time to give the UI meaningful data in next request.
      lastResponseTime.set(System.currentTimeMillis());
    }
    return millis;
  }

  private static RequestError processErrorResponse(JsonObject errorObject) throws Exception {
    String errorCode = errorObject.get("code").getAsString();
    String errorMessage = errorObject.get("message").getAsString();
    String errorStackTrace = errorObject.get("stackTrace") != null ? errorObject.get("stackTrace").getAsString() : null;
    return new RequestError(errorCode, errorMessage, errorStackTrace);
  }

  private static void sleep(long millisToSleep) {
    try {
      Thread.sleep(millisToSleep);
    }
    catch (InterruptedException e) {
      //$FALL-THROUGH$
    }
  }

  /**
   * For requests that do not have a {@link Consumer}, this object is created as a place holder so
   * that if an error occurs after the request, an error can be reported.
   */
  public static class LocalConsumer implements Consumer {
    private final JsonObject request;

    public LocalConsumer(JsonObject request) {
      this.request = request;
    }

    @SuppressWarnings("unused")
    private JsonObject getRequest() {
      return request;
    }
  }

  /**
   * A thread which reads responses from the {@link ResponseStream} and calls the associated
   * {@link Consumer}s from {@link RemoteAnalysisServerImpl#consumerMap}.
   */
  public class ServerResponseReaderThread extends Thread {

    private ResponseStream stream;

    public ServerResponseReaderThread(ResponseStream stream) {
      setDaemon(true);
      setName("ServerResponseReaderThread");
      this.stream = stream;
    }

    @Override
    public void run() {
      while (true) {
        try {
          JsonObject response = stream.take();
          if (response == null) {
            return;
          }
          lastResponseTime.set(System.currentTimeMillis());
          try {
            processResponse(response);
          }
          finally {
            stream.lastRequestProcessed();
          }
        }
        catch (Throwable e) {
          // Ignore exceptions during shutdown
          if (shutdownRequested) {
            return;
          }
          if (e instanceof IOException) {
            String message = e.getMessage();
            if (message != null && message.contains("closed")) {
              Logging.getLogger().logError("AnalysisServer stream unexpected closed", e);
              return;
            }
          }
          Logging.getLogger().logError(e.getMessage(), e);
        }
      }
    }
  }
}
