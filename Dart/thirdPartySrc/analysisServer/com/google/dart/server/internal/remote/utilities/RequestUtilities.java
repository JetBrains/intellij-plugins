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
package com.google.dart.server.internal.remote.utilities;

import com.google.common.annotations.VisibleForTesting;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import org.dartlang.analysis.server.protocol.*;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

/**
 * A utilities class for generating the {@link String} analysis server json requests.
 *
 * @coverage dart.server.remote
 */
public class RequestUtilities {
  private static final String CODE = "code";
  private static final String CONTEXT_FILE = "contextFile";
  private static final String CONTEXT_OFFSET = "contextOffset";
  private static final String CONTEXT_ROOT = "contextRoot";
  private static final String EXPRESSIONS = "expressions";
  private static final String FILE = "file";
  private static final String ID = "id";
  private static final String LABEL = "label";
  private static final String LENGTH = "length";
  private static final String LINE_LENGTH = "lineLength";
  private static final String METHOD = "method";
  private static final String OFFSET = "offset";
  private static final String PARAMS = "params";
  private static final String CLIENT_REQUEST_TIME = "clientRequestTime";
  private static final String SELECTION_LENGTH = "selectionLength";
  private static final String SELECTION_OFFSET = "selectionOffset";
  private static final String SUBSCRIPTIONS = "subscriptions";
  private static final String SUPER_ONLY = "superOnly";
  private static final String URI = "uri";
  private static final String VARIABLES = "variables";
  private static final String KEY = "key";
  private static final String ELEMENTS = "elements";
  private static final String VALUE = "value";
  private static final String ACTION = "action";
  private static final String EVENT = "event";
  private static final String MILLIS = "millis";

  // Server domain
  private static final String METHOD_SERVER_GET_VERSION = "server.getVersion";
  private static final String METHOD_SERVER_SHUTDOWN = "server.shutdown";
  private static final String METHOD_SERVER_SET_SUBSCRIPTIONS = "server.setSubscriptions";

  // Analysis domain
  private static final String METHOD_ANALYSIS_GET_ERRORS = "analysis.getErrors";
  private static final String METHOD_ANALYSIS_GET_HOVER = "analysis.getHover";
  private static final String METHOD_ANALYSIS_GET_IMPORTED_ELEMENTS = "analysis.getImportedElements";
  private static final String METHOD_ANALYSIS_GET_LIBRARY_DEPENDENCIES = "analysis.getLibraryDependencies";
  private static final String METHOD_ANALYSIS_GET_NAVIGATION = "analysis.getNavigation";
  private static final String METHOD_ANALYSIS_REANALYZE = "analysis.reanalyze";
  private static final String METHOD_ANALYSIS_SET_GENERAL_SUBSCRIPTIONS = "analysis.setGeneralSubscriptions";
  private static final String METHOD_ANALYSIS_SET_ROOTS = "analysis.setAnalysisRoots";
  private static final String METHOD_ANALYSIS_SET_PRIORITY_FILES = "analysis.setPriorityFiles";
  private static final String METHOD_ANALYSIS_SET_SUBSCRIPTIONS = "analysis.setSubscriptions";
  private static final String METHOD_ANALYSIS_UPDATE_CONTENT = "analysis.updateContent";
  private static final String METHOD_ANALYSIS_UPDATE_OPTIONS = "analysis.updateOptions";

  // Analytics domain
  private static final String METHOD_ANALYTICS_ENABLE = "analytics.enable";
  private static final String METHOD_ANALYTICS_ISENABLED = "analytics.isEnabled";
  private static final String METHOD_ANALYTICS_SEND_EVENT = "analytics.sendEvent";
  private static final String METHOD_ANALYTICS_SEND_TIMING = "analytics.sendTiming";

  // Edit domain
  private static final String METHOD_EDIT_FORMAT = "edit.format";
  private static final String METHOD_EDIT_GET_ASSISTS = "edit.getAssists";
  private static final String METHOD_EDIT_GET_AVAILABLE_REFACTORING = "edit.getAvailableRefactorings";
  private static final String METHOD_EDIT_GET_FIXES = "edit.getFixes";
  private static final String METHOD_EDIT_GET_POSTFIX_COMPLETION = "edit.getPostfixCompletion";
  private static final String METHOD_EDIT_GET_REFACTORING = "edit.getRefactoring";
  private static final String METHOD_EDIT_GET_STATEMENT_COMPLETION = "edit.getStatementCompletion";
  private static final String METHOD_EDIT_IMPORT_ELEMENTS = "edit.importElements";
  private static final String METHOD_EDIT_ORGANIZE_DIRECTIVES = "edit.organizeDirectives";
  private static final String METHOD_EDIT_SORT_MEMBERS = "edit.sortMembers";
  private static final String METHOD_IS_POSTFIX_COMPLETION_APPLICABLE = "edit.isPostfixCompletionApplicable";
  private static final String METHOD_LIST_POSTFIX_COMPLETION_TEMPLATES = "edit.listPostfixCompletionTemplates";

  // Code Completion domain
  private static final String METHOD_COMPLETION_GET_SUGGESTION_DETAILS = "completion.getSuggestionDetails";
  private static final String METHOD_COMPLETION_GET_SUGGESTIONS = "completion.getSuggestions";
  private static final String METHOD_COMPLETION_SET_SUBSCRIPTIONS = "completion.setSubscriptions";
  private static final String METHOD_COMPLETION_REGISTER_LIBRARY_PATHS = "completion.registerLibraryPaths";

  // Search domain
  private static final String METHOD_SEARCH_FIND_ELEMENT_REFERENCES = "search.findElementReferences";
  private static final String METHOD_SEARCH_FIND_MEMBER_DECLARATIONS = "search.findMemberDeclarations";
  private static final String METHOD_SEARCH_FIND_MEMBER_REFERENCES = "search.findMemberReferences";
  private static final String METHOD_SEARCH_FIND_TOP_LEVEL_DECLARATIONS = "search.findTopLevelDeclarations";
  private static final String METHOD_SEARCH_GET_TYPE_HIERARCHY = "search.getTypeHierarchy";

  // Execution domain
  private static final String METHOD_EXECUTION_CREATE_CONTEXT = "execution.createContext";
  private static final String METHOD_EXECUTION_DELETE_CONTEXT = "execution.deleteContext";
  private static final String METHOD_EXECUTION_GET_SUGGESTIONS = "execution.getSuggestions";
  private static final String METHOD_EXECUTION_MAP_URI = "execution.mapUri";
  private static final String METHOD_EXECUTION_SET_SUBSCRIPTIONS = "execution.setSubscriptions";

  // Diagnostic domain
  private static final String METHOD_DIAGNOSTIC_GET_SERVER_PORT = "diagnostic.getServerPort";

  /**
   * Flag indicating whether requests should include the time at which the request is made.
   */
  private static boolean includeRequestTime = true;

  @VisibleForTesting
  public static JsonElement buildJsonElement(Object object) {
    if (object instanceof Boolean) {
      return new JsonPrimitive((Boolean)object);
    }
    else if (object instanceof Number) {
      return new JsonPrimitive((Number)object);
    }
    else if (object instanceof String) {
      return new JsonPrimitive((String)object);
    }
    else if (object instanceof List<?>) {
      List<?> list = (List<?>)object;
      JsonArray jsonArray = new JsonArray();
      for (Object item : list) {
        JsonElement jsonItem = buildJsonElement(item);
        jsonArray.add(jsonItem);
      }
      return jsonArray;
    }
    else if (object instanceof Map<?, ?>) {
      Map<?, ?> map = (Map<?, ?>)object;
      JsonObject jsonObject = new JsonObject();
      for (Entry<?, ?> entry : map.entrySet()) {
        Object key = entry.getKey();
        // prepare string key
        String keyString;
        if (key instanceof String) {
          keyString = (String)key;
        }
        else {
          throw new IllegalArgumentException("Unable to convert to string: " + getClassName(key));
        }
        // prepare JsonElement value
        Object value = entry.getValue();
        JsonElement valueJson = buildJsonElement(value);
        // put a property into the JSON object
        if (keyString != null && valueJson != null) {
          jsonObject.add(keyString, valueJson);
        }
      }
      return jsonObject;
    }
    else if (object instanceof AnalysisError) {
      return buildJsonObjectAnalysisError((AnalysisError)object);
    }
    else if (object instanceof AddContentOverlay) {
      return ((AddContentOverlay)object).toJson();
    }
    else if (object instanceof ChangeContentOverlay) {
      return ((ChangeContentOverlay)object).toJson();
    }
    else if (object instanceof RemoveContentOverlay) {
      return ((RemoveContentOverlay)object).toJson();
    }
    else if (object instanceof AnalysisOptions) {
      return ((AnalysisOptions)object).toJson();
    }
    else if (object instanceof Location) {
      return buildJsonObjectLocation((Location)object);
    }
    else if (object instanceof ImportedElements) {
      return ((ImportedElements)object).toJson();
    }
    else if (object instanceof LibraryPathSet) {
      return ((LibraryPathSet)object).toJson();
    }
    throw new IllegalArgumentException("Unable to convert to JSON: " + object);
  }

  /**
   * Generate and return a {@value #METHOD_ANALYSIS_GET_ERRORS} request.
   * <p>
   * <pre>
   * request: {
   *   "id": String
   *   "method": "analysis.getErrors"
   *   "params": {
   *     "file": FilePath
   *   }
   * }
   * </pre>
   */
  public static JsonObject generateAnalysisGetErrors(String idValue, String file) {
    JsonObject params = new JsonObject();
    params.addProperty(FILE, file);
    return buildJsonObjectRequest(idValue, METHOD_ANALYSIS_GET_ERRORS, params);
  }

  /**
   * Generate and return a {@value #METHOD_ANALYSIS_GET_HOVER} request.
   * <p>
   * <pre>
   * request: {
   *   "id": String
   *   "method": "analysis.getHover"
   *   "params": {
   *     "file": FilePath
   *     "offset": int
   *   }
   * }
   * </pre>
   */
  public static JsonObject generateAnalysisGetHover(String idValue, String file, int offset) {
    JsonObject params = new JsonObject();
    params.addProperty(FILE, file);
    params.addProperty(OFFSET, offset);
    return buildJsonObjectRequest(idValue, METHOD_ANALYSIS_GET_HOVER, params);
  }

  public static JsonObject generateAnalysisGetImportedElements(String id, String file, int offset, int length) {
    JsonObject params = new JsonObject();
    params.addProperty(FILE, file);
    params.addProperty(OFFSET, offset);
    params.addProperty(LENGTH, length);
    return buildJsonObjectRequest(id, METHOD_ANALYSIS_GET_IMPORTED_ELEMENTS, params);
  }

  /**
   * Generate and return a {@value #METHOD_ANALYSIS_GET_LIBRARY_DEPENDENCIES} request.
   * <p>
   * <pre>
   * request: {
   *   "id": String
   *   "method": "analysis.getLibraryDependencies"
   * }
   * </pre>
   */
  public static JsonObject generateAnalysisGetLibraryDependencies(String id) {
    return buildJsonObjectRequest(id, METHOD_ANALYSIS_GET_LIBRARY_DEPENDENCIES);
  }

  /**
   * Generate and return a {@value #METHOD_ANALYSIS_GET_NAVIGATION} request.
   * <p>
   * <pre>
   * request: {
   *   "id": String
   *   "method": "analysis.getNavigation"
   *   "params": {
   *     "file": FilePath
   *     "offset": int
   *     "length": int
   *   }
   * }
   * </pre>
   */
  public static JsonObject generateAnalysisGetNavigation(String idValue, String file, int offset,
                                                         int length) {
    JsonObject params = new JsonObject();
    params.addProperty(FILE, file);
    params.addProperty(OFFSET, offset);
    params.addProperty(LENGTH, length);
    return buildJsonObjectRequest(idValue, METHOD_ANALYSIS_GET_NAVIGATION, params);
  }

  /**
   * Generate and return a {@value #METHOD_ANALYSIS_REANALYZE} request.
   * <p>
   * <pre>
   * request: {
   *   "id": String
   *   "method": "analysis.reanalyze"
   * }
   * </pre>
   */
  public static JsonObject generateAnalysisReanalyze(String id) {
    return buildJsonObjectRequest(id, METHOD_ANALYSIS_REANALYZE);
  }

  /**
   * Generate and return a {@value #METHOD_ANALYSIS_SET_ROOTS} request.
   * <p>
   * <pre>
   * request: {
   *   "id": String
   *   "method": "analysis.setAnalysisRoots"
   *   "params": {
   *     "included": List&lt;FilePath&gt;
   *     "excluded": List&lt;FilePath&gt;
   *     "packageRoots": optional Map&lt;FilePath, FilePath&gt;
   *   }
   * }
   * </pre>
   */
  public static JsonObject generateAnalysisSetAnalysisRoots(String id, List<String> included,
                                                            List<String> excluded, Map<String, String> packageRoots) {
    JsonObject params = new JsonObject();
    params.add("included", buildJsonElement(included));
    params.add("excluded", buildJsonElement(excluded));
    if (packageRoots != null) {
      params.add("packageRoots", buildJsonElement(packageRoots));
    }
    return buildJsonObjectRequest(id, METHOD_ANALYSIS_SET_ROOTS, params);
  }

  /**
   * Generate and return a {@value #METHOD_ANALYSIS_SET_GENERAL_SUBSCRIPTIONS} request.
   * <p>
   * <pre>
   * request: {
   *   "id": String
   *   "method": "analysis.setGeneralSubscriptions"
   *   "params": {
   *     "subscriptions": List&lt;GeneralAnalysisService&gt;
   *   }
   * }
   * </pre>
   */
  public static JsonObject generateAnalysisSetGeneralSubscriptions(String idValue,
                                                                   List<String> subscriptions) {
    JsonObject params = new JsonObject();
    params.add(SUBSCRIPTIONS, buildJsonElement(subscriptions));
    return buildJsonObjectRequest(idValue, METHOD_ANALYSIS_SET_GENERAL_SUBSCRIPTIONS, params);
  }

  /**
   * Generate and return a {@value #METHOD_ANALYSIS_SET_PRIORITY_FILES} request.
   * <p>
   * <pre>
   * request: {
   *   "id": String
   *   "method": "analysis.setPriorityFiles"
   *   "params": {
   *     "files": List&lt;FilePath&gt;
   *   }
   * }
   * </pre>
   */
  public static JsonObject generateAnalysisSetPriorityFiles(String id, List<String> files) {
    JsonObject params = new JsonObject();
    params.add("files", buildJsonElement(files));
    return buildJsonObjectRequest(id, METHOD_ANALYSIS_SET_PRIORITY_FILES, params);
  }

  /**
   * Generate and return a {@value #METHOD_ANALYSIS_SET_SUBSCRIPTIONS} request.
   * <p>
   * <pre>
   * request: {
   *   "id": String
   *   "method": "analysis.setSubscriptions"
   *   "params": {
   *     "subscriptions": Map&gt;AnalysisService, List&lt;FilePath&gt;&gt;
   *   }
   * }
   * </pre>
   */
  public static JsonObject generateAnalysisSetSubscriptions(String id,
                                                            Map<String, List<String>> subscriptions) {
    JsonObject params = new JsonObject();
    params.add(SUBSCRIPTIONS, buildJsonElement(subscriptions));
    return buildJsonObjectRequest(id, METHOD_ANALYSIS_SET_SUBSCRIPTIONS, params);
  }

  /**
   * Generate and return a {@value #METHOD_ANALYSIS_UPDATE_CONTENT} request.
   * <p>
   * <pre>
   * request: {
   *   "id": String
   *   "method": "analysis.updateContent"
   *   "params": {
   *     "files": Map&lt;FilePath, ContentChange&gt;
   *   }
   * }
   * </pre>
   */
  public static JsonObject generateAnalysisUpdateContent(String idValue, Map<String, Object> files) {
    JsonObject params = new JsonObject();
    params.add("files", buildJsonElement(files));
    return buildJsonObjectRequest(idValue, METHOD_ANALYSIS_UPDATE_CONTENT, params);
  }

  /**
   * Generate and return a {@value #METHOD_ANALYSIS_UPDATE_OPTIONS} request.
   * <p>
   * <pre>
   * request: {
   *   "id": String
   *   "method": "analysis.updateOptions"
   *   "params": {
   *     "options": AnalysisOptions
   *   }
   * }
   * </pre>
   */
  public static JsonObject generateAnalysisUpdateOptions(String idValue, AnalysisOptions options) {
    JsonObject params = new JsonObject();
    params.add("options", buildJsonElement(options));
    return buildJsonObjectRequest(idValue, METHOD_ANALYSIS_UPDATE_OPTIONS, params);
  }

  /**
   * Generate and return a {@value #METHOD_COMPLETION_REGISTER_LIBRARY_PATHS} request.
   * <p>
   * <pre>
   * request: {
   *   "id": string",
   *   "method": "completion.registerLibraryPaths",
   *   "params": {
   *     "paths": List&lt;LibraryPathSet&gt;
   *   }
   * }
   * </pre>
   * </p>
   */
  public static JsonObject generateCompletionRegisterLibraryPaths(String idValue, List<LibraryPathSet> paths) {
    JsonObject params = new JsonObject();
    params.add("paths", buildJsonElement(paths));
    return buildJsonObjectRequest(idValue, METHOD_COMPLETION_REGISTER_LIBRARY_PATHS, params);
  }

  /**
   * Generate and return a {@value #METHOD_COMPLETION_GET_SUGGESTION_DETAILS} request.
   * <p>
   * <pre>
   * request: {
   *   "id": String
   *   "method": "completion.getSuggestionDetails"
   *   "params": {
   *     "file": FilePath
   *     "id": int
   *     "label": String
   *   }
   * }
   * </pre>
   */
  public static JsonObject generateCompletionGetSuggestionDetails(String idValue, String file, int id, String label, int offset) {
    JsonObject params = new JsonObject();
    params.addProperty(FILE, file);
    params.addProperty(ID, id);
    params.addProperty(LABEL, label);
    params.addProperty(OFFSET, offset);
    return buildJsonObjectRequest(idValue, METHOD_COMPLETION_GET_SUGGESTION_DETAILS, params);
  }

  /**
   * Generate and return a {@value #METHOD_COMPLETION_GET_SUGGESTIONS} request.
   * <p>
   * <pre>
   * request: {
   *   "id": String
   *   "method": "completion.getSuggestions"
   *   "params": {
   *     "file": FilePath
   *     "offset": int
   *   }
   * }
   * </pre>
   */
  public static JsonObject generateCompletionGetSuggestions(String idValue, String file, int offset) {
    JsonObject params = new JsonObject();
    params.addProperty(FILE, file);
    params.addProperty(OFFSET, offset);
    return buildJsonObjectRequest(idValue, METHOD_COMPLETION_GET_SUGGESTIONS, params);
  }

  /**
   * Generate and return a {@value #METHOD_COMPLETION_SET_SUBSCRIPTIONS} request.
   * <p>
   * <pre>
   * request: {
   *   "id": String
   *   "method": "completion.setSubscriptions"
   *   "params": {
   *     "subscriptions": List&lt;CompletionService&gt;
   *   }
   * }
   * </pre>
   */
  public static JsonObject generateCompletionSetSubscriptions(String idValue,
                                                              List<String> subscriptions) {
    JsonObject params = new JsonObject();
    params.add(SUBSCRIPTIONS, buildJsonElement(subscriptions));
    return buildJsonObjectRequest(idValue, METHOD_COMPLETION_SET_SUBSCRIPTIONS, params);
  }

  /**
   * Generate and return a {@value #METHOD_EDIT_FORMAT} request.
   * <p>
   * <pre>
   * request: {
   *   "id": String
   *   "method": "edit.format"
   *   "params": {
   *     "file": FilePath
   *     "selectionOffset": int
   *     "selectionLength": int
   *     "lineLength": optional int
   *   }
   * }
   * </pre>
   */
  public static JsonObject generateEditFormat(String idValue, String file, int offset, int length,
                                              int lineLength) {
    JsonObject params = new JsonObject();
    params.addProperty(FILE, file);
    params.addProperty(SELECTION_OFFSET, offset);
    params.addProperty(SELECTION_LENGTH, length);
    if (lineLength != -1) {
      params.addProperty(LINE_LENGTH, lineLength);
    }
    return buildJsonObjectRequest(idValue, METHOD_EDIT_FORMAT, params);
  }

  /**
   * Generate and return a {@value #METHOD_EDIT_GET_ASSISTS} request.
   * <p>
   * <pre>
   * request: {
   *   "id": String
   *   "method": "edit.getAssists"
   *   "params": {
   *     "file": FilePath
   *     "offset": int
   *     "length": int
   *   }
   * }
   * </pre>
   */
  public static JsonObject generateEditGetAssists(String idValue, String file, int offset,
                                                  int length) {
    JsonObject params = new JsonObject();
    params.addProperty(FILE, file);
    params.addProperty(OFFSET, offset);
    params.addProperty(LENGTH, length);
    return buildJsonObjectRequest(idValue, METHOD_EDIT_GET_ASSISTS, params);
  }

  /**
   * Generate and return a {@value #METHOD_EDIT_GET_AVAILABLE_REFACTORING} request.
   * <p>
   * <pre>
   * request: {
   *   "id": String
   *   "method": "edit.getAvailableRefactorings"
   *   "params": {
   *     "file": FilePath
   *     "offset": int
   *     "length": int
   *   }
   * }
   * </pre>
   */
  public static JsonObject generateEditGetAvaliableRefactorings(String idValue, String file,
                                                                int offset, int length) {
    JsonObject params = new JsonObject();
    params.addProperty(FILE, file);
    params.addProperty(OFFSET, offset);
    params.addProperty(LENGTH, length);
    return buildJsonObjectRequest(idValue, METHOD_EDIT_GET_AVAILABLE_REFACTORING, params);
  }

  /**
   * Generate and return a {@value #METHOD_EDIT_GET_FIXES} request.
   * <p>
   * <pre>
   * request: {
   *   "id": String
   *   "method": "edit.getFixes"
   *   "params": {
   *     "file": FilePath
   *     "offset": int
   *   }
   * }
   * </pre>
   */
  public static JsonObject generateEditGetFixes(String idValue, String file, int offset) {
    JsonObject params = new JsonObject();
    params.addProperty(FILE, file);
    params.addProperty(OFFSET, offset);
    return buildJsonObjectRequest(idValue, METHOD_EDIT_GET_FIXES, params);
  }

  public static JsonObject generateIsPostfixCompletionApplicable(String idValue, String file, int offset, String key) {
    JsonObject params = new JsonObject();
    params.addProperty(FILE, file);
    params.addProperty(OFFSET, offset);
    params.addProperty(KEY, key);
    return buildJsonObjectRequest(idValue, METHOD_IS_POSTFIX_COMPLETION_APPLICABLE, params);
  }

  public static JsonObject generateListPostfixCompletionTeamplates(String idValue) {
    JsonObject params = new JsonObject();
    return buildJsonObjectRequest(idValue, METHOD_LIST_POSTFIX_COMPLETION_TEMPLATES);
  }

  public static JsonObject generateEditPostfixCompletion(String idValue, String file, int offset, String key) {
    JsonObject params = new JsonObject();
    params.addProperty(FILE, file);
    params.addProperty(OFFSET, offset);
    params.addProperty(KEY, key);
    return buildJsonObjectRequest(idValue, METHOD_EDIT_GET_POSTFIX_COMPLETION, params);
  }

  /**
   * Generate and return a {@value #METHOD_REFACTORING} request.
   * <p>
   * <pre>
   * request: {
   *   "id": String
   *   "method": "edit.getRefactoring"
   *   "params": {
   *     "kind": RefactoringKind
   *     "file": FilePath
   *     "offset": int
   *     "length": int
   *     "validateOnly": bool
   *     "options": optional object
   *   }
   * }
   * </pre>
   */
  public static JsonObject generateEditGetRefactoring(String idValue, String kind, String file,
                                                      int offset, int length, boolean validateOnly, RefactoringOptions options) {
    JsonObject params = new JsonObject();
    params.addProperty("kind", kind);
    params.addProperty(FILE, file);
    params.addProperty(OFFSET, offset);
    params.addProperty(LENGTH, length);
    params.addProperty("validateOnly", validateOnly);
    if (options != null) {
      params.add("options", options.toJson());
    }
    return buildJsonObjectRequest(idValue, METHOD_EDIT_GET_REFACTORING, params);
  }

  public static JsonObject generateEditStatementCompletion(String idValue, String file, int offset) {
    JsonObject params = new JsonObject();
    params.addProperty(FILE, file);
    params.addProperty(OFFSET, offset);
    return buildJsonObjectRequest(idValue, METHOD_EDIT_GET_STATEMENT_COMPLETION, params);
  }

  public static JsonObject generateEditImportElements(String id, String file, List<ImportedElements> elements, int offset) {
    JsonObject params = new JsonObject();
    params.addProperty(FILE, file);
    params.add(ELEMENTS, buildJsonElement(elements));
    params.addProperty(OFFSET, offset);
    return buildJsonObjectRequest(id, METHOD_EDIT_IMPORT_ELEMENTS, params);
  }

  /**
   * Generate and return a {@value #METHOD_EDIT_ORGANIZE_DIRECTIVES} request.
   * <p>
   * <pre>
   * request: {
   *   "id": String
   *   "method": "edit.organizeDirectives"
   *   "params": {
   *     "file": FilePath
   *   }
   * }
   * </pre>
   */
  public static JsonObject generateEditOrganizeDirectives(String idValue, String file) {
    JsonObject params = new JsonObject();
    params.addProperty(FILE, file);
    return buildJsonObjectRequest(idValue, METHOD_EDIT_ORGANIZE_DIRECTIVES, params);
  }

  /**
   * Generate and return a {@value #METHOD_EDIT_SORT_MEMBERS} request.
   * <p>
   * <pre>
   * request: {
   *   "id": String
   *   "method": "edit.sortMembers"
   *   "params": {
   *     "file": FilePath
   *   }
   * }
   * </pre>
   */
  public static JsonObject generateEditSortMembers(String idValue, String file) {
    JsonObject params = new JsonObject();
    params.addProperty(FILE, file);
    return buildJsonObjectRequest(idValue, METHOD_EDIT_SORT_MEMBERS, params);
  }

  /**
   * Generate and return a {@value #METHOD_EXECUTION_CREATE_CONTEXT} request.
   * <p>
   * <pre>
   * request: {
   *   "id": String
   *   "method": "execution.createContext"
   *   "params": {
   *     "contextRoot": FilePath
   *   }
   * }
   * </pre>
   */
  public static JsonObject generateExecutionCreateContext(String idValue, String contextRoot) {
    JsonObject params = new JsonObject();
    params.addProperty(CONTEXT_ROOT, contextRoot);
    return buildJsonObjectRequest(idValue, METHOD_EXECUTION_CREATE_CONTEXT, params);
  }

  /**
   * Generate and return a {@value #METHOD_EXECUTION_DELETE_CONTEXT} request.
   * <p>
   * <pre>
   * request: {
   *   "id": String
   *   "method": "execution.deleteContext"
   *   "params": {
   *     "id": ExecutionContextId
   *   }
   * }
   * </pre>
   */
  public static JsonObject generateExecutionDeleteContext(String idValue, String contextId) {
    JsonObject params = new JsonObject();
    params.addProperty(ID, contextId);
    return buildJsonObjectRequest(idValue, METHOD_EXECUTION_DELETE_CONTEXT, params);
  }

  /**
   * Generate and return a {@value #METHOD_EXECUTION_GET_SUGGESTIONS} request.
   * <p>
   * <pre>
   * request: {
   *   "id": String
   *   "method": "execution.getSuggestions"
   *   "params": {
   *     "code": String
   *     "offset": int
   *     "contextFile": FilePath
   *     "contextOffset": int
   *     "variables": List<RuntimeCompletionVariable>
   *     "expressions": optional List<RuntimeCompletionExpression>
   *   }
   * }
   * </pre>
   */
  public static JsonObject generateExecutionGetSuggestions(String idValue,
                                                           String code,
                                                           int offset,
                                                           String contextFile,
                                                           int contextOffset,
                                                           List<RuntimeCompletionVariable> variables,
                                                           List<RuntimeCompletionExpression> expressions) {
    JsonObject params = new JsonObject();
    params.addProperty(CODE, code);
    params.addProperty(OFFSET, offset);
    params.addProperty(CONTEXT_FILE, contextFile);
    params.addProperty(CONTEXT_OFFSET, contextOffset);
    params.add(VARIABLES, buildJsonElement(variables));
    params.add(EXPRESSIONS, buildJsonElement(expressions));
    return buildJsonObjectRequest(idValue, METHOD_EXECUTION_GET_SUGGESTIONS, params);
  }

  /**
   * Generate and return a {@value #METHOD_EXECUTION_MAP_URI} request.
   * <p>
   * <pre>
   * request: {
   *   "id": String
   *   "method": "execution.mapUri"
   *   "params": {
   *     "id": ExecutionContextId
   *     "file": FilePath
   *     "uri": String
   *   }
   * }
   * </pre>
   */
  public static JsonObject generateExecutionMapUri(String idValue, String contextId, String file,
                                                   String uri) {
    JsonObject params = new JsonObject();
    params.addProperty(ID, contextId);
    if (file == null) {
      if (uri == null) {
        throw new IllegalArgumentException("Exactly one of 'file' and 'uri' must be non-null");
      }
      params.addProperty(URI, uri);
    }
    else {
      if (uri != null) {
        throw new IllegalArgumentException("Exactly one of 'file' and 'uri' must be non-null");
      }
      params.addProperty(FILE, file);
    }
    return buildJsonObjectRequest(idValue, METHOD_EXECUTION_MAP_URI, params);
  }

  /**
   * Generate and return a {@value #METHOD_EXECUTION_SET_SUBSCRIPTIONS} request.
   * <p>
   * <pre>
   * request: {
   *   "id": String
   *   "method": "execution.setSubscriptions"
   *   "params": {
   *     "subscriptions": List<ExecutionService>
   *   }
   * }
   * </pre>
   */
  public static JsonObject generateExecutionSetSubscriptions(String idValue,
                                                             List<String> subscriptions) {
    JsonObject params = new JsonObject();
    params.add(SUBSCRIPTIONS, buildJsonElement(subscriptions));
    return buildJsonObjectRequest(idValue, METHOD_EXECUTION_SET_SUBSCRIPTIONS, params);
  }

  /**
   * Generate and return a {@value #METHOD_SEARCH_FIND_ELEMENT_REFERENCES} request.
   * <p>
   * <pre>
   * request: {
   *   "id": String
   *   "method": "search.findElementReferences"
   *   "params": {
   *     "file": FilePath
   *     "offset": int
   *     "includePotential": boolean
   *   }
   * }
   * </pre>
   */
  public static JsonObject generateSearchFindElementReferences(String idValue, String file,
                                                               int offset, boolean includePotential) {
    JsonObject params = new JsonObject();
    params.addProperty(FILE, file);
    params.addProperty(OFFSET, offset);
    params.addProperty("includePotential", includePotential);
    return buildJsonObjectRequest(idValue, METHOD_SEARCH_FIND_ELEMENT_REFERENCES, params);
  }

  /**
   * Generate and return a {@value #METHOD_SEARCH_FIND_MEMBER_DECLARATIONS} request.
   * <p>
   * <pre>
   * request: {
   *   "id": String
   *   "method": "search.findMemberDeclarations"
   *   "params": {
   *     "name": String
   *   }
   * }
   * </pre>
   */
  public static JsonObject generateSearchFindMemberDeclarations(String idValue, String name) {
    JsonObject params = new JsonObject();
    params.addProperty("name", name);
    return buildJsonObjectRequest(idValue, METHOD_SEARCH_FIND_MEMBER_DECLARATIONS, params);
  }

  /**
   * Generate and return a {@value #METHOD_SEARCH_FIND_MEMBER_REFERENCES} request.
   * <p>
   * <pre>
   * request: {
   *   "id": String
   *   "method": "search.findMemberReferences"
   *   "params": {
   *     "name": String
   *   }
   * }
   * </pre>
   */
  public static JsonObject generateSearchFindMemberReferences(String idValue, String name) {
    JsonObject params = new JsonObject();
    params.addProperty("name", name);
    return buildJsonObjectRequest(idValue, METHOD_SEARCH_FIND_MEMBER_REFERENCES, params);
  }

  /**
   * Generate and return a {@value #METHOD_SEARCH_FIND_TOP_LEVEL_DECLARATIONS} request.
   * <p>
   * <pre>
   * request: {
   *   "id": String
   *   "method": "search.findTopLevelDeclarations"
   *   "params": {
   *     "name": String
   *   }
   * }
   * </pre>
   */
  public static JsonObject generateSearchFindTopLevelDeclarations(String idValue, String pattern) {
    JsonObject params = new JsonObject();
    params.addProperty("pattern", pattern);
    return buildJsonObjectRequest(idValue, METHOD_SEARCH_FIND_TOP_LEVEL_DECLARATIONS, params);
  }

  /**
   * Generate and return a {@value #METHOD_SEARCH_GET_TYPE_HIERARCHY} request.
   * <p>
   * <pre>
   * request: {
   *   "id": String
   *   "method": "search.getTypeHierarchy"
   *   "params": {
   *     "file": FilePath
   *     "offset": int
   *     "superOnly": optional bool
   *   }
   * }
   * </pre>
   */
  public static JsonObject generateSearchGetTypeHierarchy(String id, String file, int offset,
                                                          boolean superOnly) {
    JsonObject params = new JsonObject();
    params.addProperty(FILE, file);
    params.addProperty(OFFSET, offset);
    params.addProperty(SUPER_ONLY, superOnly);
    return buildJsonObjectRequest(id, METHOD_SEARCH_GET_TYPE_HIERARCHY, params);
  }

  /**
   * Generate and return a {@value #METHOD_SERVER_GET_VERSION} request.
   * <p>
   * <pre>
   * request: {
   *   "id": String
   *   "method": "server.getVersion"
   * }
   * </pre>
   */
  public static JsonObject generateServerGetVersion(String idValue) {
    return buildJsonObjectRequest(idValue, METHOD_SERVER_GET_VERSION);
  }

  /**
   * Generate and return a {@value #METHOD_SERVER_SET_SUBSCRIPTIONS} request.
   * <p>
   * <pre>
   * request: {
   *   "id": String
   *   "method": "server.setSubscriptions"
   *   "params": {
   *     "subscriptions": List&lt;ServerService&gt;
   *   }
   * }
   * </pre>
   */
  public static JsonObject generateServerSetSubscriptions(String idValue, List<String> subscriptions) {
    JsonObject params = new JsonObject();
    params.add(SUBSCRIPTIONS, buildJsonElement(subscriptions));
    return buildJsonObjectRequest(idValue, METHOD_SERVER_SET_SUBSCRIPTIONS, params);
  }

  /**
   * Generate and return a {@value #METHOD_SERVER_SHUTDOWN} request.
   * <p>
   * <pre>
   * request: {
   *   "id": String
   *   "method": "server.shutdown"
   * }
   * </pre>
   */
  public static JsonObject generateServerShutdown(String idValue) {
    return buildJsonObjectRequest(idValue, METHOD_SERVER_SHUTDOWN);
  }

  public static JsonObject generateDiagnosticGetServerPort(String idValue) {
    return buildJsonObjectRequest(idValue, METHOD_DIAGNOSTIC_GET_SERVER_PORT);
  }

  public static JsonObject generateAnalyticsEnable(String idValue, boolean value) {
    JsonObject params = new JsonObject();
    params.add(VALUE, buildJsonElement(value));
    return buildJsonObjectRequest(idValue, METHOD_ANALYTICS_ENABLE, params);
  }

  public static JsonObject generateAnalyticsIsEnabled(String idValue) {
    return buildJsonObjectRequest(idValue, METHOD_ANALYTICS_ISENABLED);
  }

  public static JsonObject generateAnalyticsSendEvent(String idValue, String action) {
    JsonObject params = new JsonObject();
    params.add(ACTION, buildJsonElement(action));
    return buildJsonObjectRequest(idValue, METHOD_ANALYTICS_SEND_EVENT, params);
  }

  public static JsonObject generateAnalyticsSendTiming(String idValue, String event, int millis) {
    JsonObject params = new JsonObject();
    params.add(EVENT, buildJsonElement(event));
    params.add(MILLIS, buildJsonElement(millis));
    return buildJsonObjectRequest(idValue, METHOD_ANALYTICS_SEND_TIMING, params);
  }

  /**
   * Return the ID of the given request.
   */
  public static String getId(JsonObject request) {
    return request.getAsJsonPrimitive(ID).getAsString();
  }

  /**
   * Return {@code true} if the given request is a version request.
   */
  public static boolean isVersionRequest(JsonObject request) {
    String method = getRequestMethod(request);
    return METHOD_SERVER_GET_VERSION.equals(method);
  }

  /**
   * Set whether the request time is included in the request itself.
   */
  public static void setIncludeRequestTime(boolean includeRequestTime) {
    RequestUtilities.includeRequestTime = includeRequestTime;
  }

  private static JsonObject buildJsonObjectAnalysisError(AnalysisError error) {
    JsonObject errorJsonObject = new JsonObject();
    errorJsonObject.addProperty("severity", error.getSeverity());
    errorJsonObject.addProperty("type", error.getType());
    errorJsonObject.add("location", buildJsonObjectLocation(error.getLocation()));
    errorJsonObject.addProperty("message", error.getMessage());
    String correction = error.getCorrection();
    if (correction != null) {
      errorJsonObject.addProperty("correction", correction);
    }
    return errorJsonObject;
  }

  private static JsonObject buildJsonObjectLocation(Location location) {
    JsonObject locationJsonObject = new JsonObject();
    locationJsonObject.addProperty(FILE, location.getFile());
    locationJsonObject.addProperty(OFFSET, location.getOffset());
    locationJsonObject.addProperty(LENGTH, location.getLength());
    locationJsonObject.addProperty("startLine", location.getStartLine());
    locationJsonObject.addProperty("startColumn", location.getStartColumn());
    return locationJsonObject;
  }

  private static JsonObject buildJsonObjectRequest(String idValue, String methodValue) {
    return buildJsonObjectRequest(idValue, methodValue, null);
  }

  private static JsonObject buildJsonObjectRequest(String idValue, String methodValue,
                                                   JsonObject params) {
    JsonObject jsonObject = new JsonObject();
    jsonObject.addProperty(ID, idValue);
    jsonObject.addProperty(METHOD, methodValue);
    if (params != null) {
      jsonObject.add(PARAMS, params);
    }
    if (includeRequestTime) {
      jsonObject.addProperty(CLIENT_REQUEST_TIME, System.currentTimeMillis());
    }
    return jsonObject;
  }

  /**
   * Return the name of the given object, may be {@code "null"} string.
   */
  private static String getClassName(Object object) {
    return object != null ? object.getClass().getName() : "null";
  }

  /**
   * Returns the request method, or {@code null}.
   */
  private static String getRequestMethod(JsonObject request) {
    JsonElement child = request.get(METHOD);
    if (child instanceof JsonPrimitive) {
      return child.getAsString();
    }
    return null;
  }

  private RequestUtilities() {
  }
}
