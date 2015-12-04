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

import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.dart.server.CreateContextConsumer;
import com.google.dart.server.FindElementReferencesConsumer;
import com.google.dart.server.FindMemberDeclarationsConsumer;
import com.google.dart.server.FindMemberReferencesConsumer;
import com.google.dart.server.FindTopLevelDeclarationsConsumer;
import com.google.dart.server.FormatConsumer;
import com.google.dart.server.GetAssistsConsumer;
import com.google.dart.server.GetAvailableRefactoringsConsumer;
import com.google.dart.server.GetErrorsConsumer;
import com.google.dart.server.GetFixesConsumer;
import com.google.dart.server.GetHoverConsumer;
import com.google.dart.server.GetLibraryDependenciesConsumer;
import com.google.dart.server.GetNavigationConsumer;
import com.google.dart.server.GetRefactoringConsumer;
import com.google.dart.server.GetSuggestionsConsumer;
import com.google.dart.server.GetTypeHierarchyConsumer;
import com.google.dart.server.GetVersionConsumer;
import com.google.dart.server.MapUriConsumer;
import com.google.dart.server.OrganizeDirectivesConsumer;
import com.google.dart.server.SortMembersConsumer;
import com.google.dart.server.UpdateContentConsumer;
import com.google.dart.server.internal.AnalysisServerError;
import com.google.dart.server.internal.remote.utilities.RequestUtilities;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import org.dartlang.analysis.server.protocol.AddContentOverlay;
import org.dartlang.analysis.server.protocol.AnalysisError;
import org.dartlang.analysis.server.protocol.AnalysisErrorFixes;
import org.dartlang.analysis.server.protocol.AnalysisErrorSeverity;
import org.dartlang.analysis.server.protocol.AnalysisOptions;
import org.dartlang.analysis.server.protocol.AnalysisService;
import org.dartlang.analysis.server.protocol.AnalysisStatus;
import org.dartlang.analysis.server.protocol.ChangeContentOverlay;
import org.dartlang.analysis.server.protocol.CompletionSuggestion;
import org.dartlang.analysis.server.protocol.CompletionSuggestionKind;
import org.dartlang.analysis.server.protocol.Element;
import org.dartlang.analysis.server.protocol.ElementKind;
import org.dartlang.analysis.server.protocol.ExecutionService;
import org.dartlang.analysis.server.protocol.ExtractLocalVariableFeedback;
import org.dartlang.analysis.server.protocol.ExtractLocalVariableOptions;
import org.dartlang.analysis.server.protocol.ExtractMethodFeedback;
import org.dartlang.analysis.server.protocol.ExtractMethodOptions;
import org.dartlang.analysis.server.protocol.GeneralAnalysisService;
import org.dartlang.analysis.server.protocol.HighlightRegion;
import org.dartlang.analysis.server.protocol.HighlightRegionType;
import org.dartlang.analysis.server.protocol.HoverInformation;
import org.dartlang.analysis.server.protocol.ImplementedClass;
import org.dartlang.analysis.server.protocol.ImplementedMember;
import org.dartlang.analysis.server.protocol.InlineLocalVariableFeedback;
import org.dartlang.analysis.server.protocol.InlineMethodFeedback;
import org.dartlang.analysis.server.protocol.InlineMethodOptions;
import org.dartlang.analysis.server.protocol.Location;
import org.dartlang.analysis.server.protocol.NavigationRegion;
import org.dartlang.analysis.server.protocol.NavigationTarget;
import org.dartlang.analysis.server.protocol.Occurrences;
import org.dartlang.analysis.server.protocol.Outline;
import org.dartlang.analysis.server.protocol.OverriddenMember;
import org.dartlang.analysis.server.protocol.OverrideMember;
import org.dartlang.analysis.server.protocol.RefactoringFeedback;
import org.dartlang.analysis.server.protocol.RefactoringKind;
import org.dartlang.analysis.server.protocol.RefactoringMethodParameter;
import org.dartlang.analysis.server.protocol.RefactoringMethodParameterKind;
import org.dartlang.analysis.server.protocol.RefactoringOptions;
import org.dartlang.analysis.server.protocol.RefactoringProblem;
import org.dartlang.analysis.server.protocol.RefactoringProblemSeverity;
import org.dartlang.analysis.server.protocol.RemoveContentOverlay;
import org.dartlang.analysis.server.protocol.RenameFeedback;
import org.dartlang.analysis.server.protocol.RenameOptions;
import org.dartlang.analysis.server.protocol.RequestError;
import org.dartlang.analysis.server.protocol.SearchResult;
import org.dartlang.analysis.server.protocol.SearchResultKind;
import org.dartlang.analysis.server.protocol.ServerService;
import org.dartlang.analysis.server.protocol.SourceChange;
import org.dartlang.analysis.server.protocol.SourceEdit;
import org.dartlang.analysis.server.protocol.SourceFileEdit;
import org.dartlang.analysis.server.protocol.TypeHierarchyItem;

import static org.fest.assertions.Assertions.assertThat;
import static org.fest.assertions.MapAssert.entry;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Unit tests for {@link RemoteAnalysisServerImpl}, for integration tests which actually uses the
 * remote server, see {@link RemoteAnalysisServerImplIntegrationTest}.
 */
public class RemoteAnalysisServerImplTest extends AbstractRemoteServerTest {

  public void test_analysis_getErrors() throws Exception {
    final AnalysisError[][] errors = {{null}};
    final RequestError[] requestErrorArray = {null};
    server.analysis_getErrors("/fileA.dart", new GetErrorsConsumer() {
      @Override
      public void computedErrors(AnalysisError[] e) {
        errors[0] = e;
      }

      @Override
      public void onError(RequestError requestError) {
        requestErrorArray[0] = requestError;
      }
    });

    List<JsonObject> requests = requestSink.getRequests();
    JsonObject expected = parseJson(//
        "{",
        "  'id': '0',",
        "  'method': 'analysis.getErrors',",
        "  'params': {",
        "    'file': '/fileA.dart'",
        "  }",
        "}");
    assertTrue(requests.contains(expected));

    putResponse(//
        "{",
        "  'id': '0',",
        "  'result': {",
        "    'errors' : [",
        "      {",
        "        'severity': 'ERROR',",
        "        'type': 'SYNTACTIC_ERROR',",
        "        'location': {",
        "          'file': '/fileA.dart',",
        "          'offset': 1,",
        "          'length': 2,",
        "          'startLine': 3,",
        "          'startColumn': 4",
        "        },",
        "        'message': 'message A',",
        "        'correction': 'correction A',",
        "        'hasFix': true",
        "      },",
        "      {",
        "        'severity': 'ERROR',",
        "        'type': 'COMPILE_TIME_ERROR',",
        "        'location': {",
        "          'file': '/fileB.dart',",
        "          'offset': 5,",
        "          'length': 6,",
        "          'startLine': 7,",
        "          'startColumn': 8",
        "        },",
        "        'message': 'message B',",
        "        'correction': 'correction B',",
        "        'hasFix': false",
        "      }",
        "    ]",
        "  }",
        "}");
    responseStream.waitForEmpty();
    server.test_waitForWorkerComplete();

    assertNull(requestErrorArray[0]);
    assertThat(errors[0]).hasSize(2);
    assertEquals(new AnalysisError(AnalysisErrorSeverity.ERROR, "SYNTACTIC_ERROR", new Location(
        "/fileA.dart",
        1,
        2,
        3,
        4), "message A", "correction A", true), errors[0][0]);
    assertEquals(new AnalysisError(AnalysisErrorSeverity.ERROR, "COMPILE_TIME_ERROR", new Location(
        "/fileB.dart",
        5,
        6,
        7,
        8), "message B", "correction B", false), errors[0][1]);
  }

  public void test_analysis_getErrors_error() throws Exception {
    final AnalysisError[][] errors = {{null}};
    final RequestError[] requestErrorArray = {null};
    server.analysis_getErrors("/fileA.dart", new GetErrorsConsumer() {
      @Override
      public void computedErrors(AnalysisError[] e) {
        errors[0] = e;
      }

      @Override
      public void onError(RequestError requestError) {
        requestErrorArray[0] = requestError;
      }
    });

    List<JsonObject> requests = requestSink.getRequests();
    JsonObject expected = parseJson(//
        "{",
        "  'id': '0',",
        "  'method': 'analysis.getErrors',",
        "  'params': {",
        "    'file': '/fileA.dart'",
        "  }",
        "}");
    assertTrue(requests.contains(expected));

    putResponse(//
        "{",
        "  'id': '0',",
        "  'method': 'edit.getFixes',",
        "  'error': {",
        "    'code': 'CONTENT_MODIFIED',",
        "    'message': 'message0',",
        "    'stackTrace': 'stackTrace0'",
        "  }",
        "}");
    responseStream.waitForEmpty();
    server.test_waitForWorkerComplete();

    assertNull(errors[0][0]);
    assertNotNull(requestErrorArray[0]);
    RequestError requestError = requestErrorArray[0];
    assertEquals("CONTENT_MODIFIED", requestError.getCode());
    assertEquals("message0", requestError.getMessage());
    assertEquals("stackTrace0", requestError.getStackTrace());
  }

  public void test_analysis_getErrors_error_responseError() throws Exception {
    final AnalysisError[][] errors = {{null}};
    final RequestError[] requestErrorArray = {null};
    server.analysis_getErrors("/fileA.dart", new GetErrorsConsumer() {
      @Override
      public void computedErrors(AnalysisError[] e) {
        errors[0] = e;
      }

      @Override
      public void onError(RequestError requestError) {
        requestErrorArray[0] = requestError;
      }
    });

    List<JsonObject> requests = requestSink.getRequests();
    JsonObject expected = parseJson(//
        "{",
        "  'id': '0',",
        "  'method': 'analysis.getErrors',",
        "  'params': {",
        "    'file': '/fileA.dart'",
        "  }",
        "}");
    assertTrue(requests.contains(expected));

    putResponse(//
        "{",
        "  'id': '0',",
        "  'result': {",
        "    'errors' : [",
        "      {",
        "        'severity': 'ERROR',",
        // invalid response, type missing
//        "        'type': 'SYNTACTIC_ERROR',",
        "        'location': {",
        "          'file': '/fileA.dart',",
        "          'offset': 1,",
        "          'length': 2,",
        "          'startLine': 3,",
        "          'startColumn': 4",
        "        },",
        "        'message': 'message A',",
        "        'correction': 'correction A'",
        "      },",
        "      {",
        "        'severity': 'ERROR',",
        "        'type': 'COMPILE_TIME_ERROR',",
        "        'location': {",
        "          'file': '/fileB.dart',",
        "          'offset': 5,",
        "          'length': 6,",
        "          'startLine': 7,",
        "          'startColumn': 8",
        "        },",
        "        'message': 'message B',",
        "        'correction': 'correction B'",
        "      }",
        "    ]",
        "  }",
        "}");
    responseStream.waitForEmpty();
    server.test_waitForWorkerComplete();

    assertNull(errors[0][0]);
    assertNotNull(requestErrorArray[0]);
    RequestError requestError = requestErrorArray[0];
    assertEquals("INVALID_SERVER_RESPONSE", requestError.getCode());
  }

  public void test_analysis_getHover() throws Exception {
    final HoverInformation[] hovers = new HoverInformation[1];
    final RequestError[] requestErrorArray = {null};
    server.analysis_getHover("/fileA.dart", 17, new GetHoverConsumer() {
      @Override
      public void computedHovers(HoverInformation[] result) {
        hovers[0] = result[0];
      }

      @Override
      public void onError(RequestError requestError) {
        requestErrorArray[0] = requestError;
      }
    });

    List<JsonObject> requests = requestSink.getRequests();
    JsonObject expected = parseJson(//
        "{",
        "  'id': '0',",
        "  'method': 'analysis.getHover',",
        "  'params': {",
        "    'file': '/fileA.dart',",
        "    'offset': 17",
        "  }",
        "}");
    assertTrue(requests.contains(expected));

    putResponse(//
        "{",
        "  'id': '0',",
        "  'result': {",
        "    'hovers': [",
        "      {",
        "        'offset': '22',",
        "        'length': '5',",
        "        'containingLibraryName': 'myLibrary',",
        "        'containingLibraryPath': '/path/to/lib',",
        "        'dartdoc': 'some dartdoc',",
        "        'elementDescription': 'element description',",
        "        'elementKind': 'element kind',",
        "        'parameter': 'some parameter',",
        "        'propagatedType': 'typeA',",
        "        'staticType': 'typeB'",
        "      }",
        "    ]",
        "  }",
        "}");
    responseStream.waitForEmpty();
    server.test_waitForWorkerComplete();
    assertNull(requestErrorArray[0]);
    assertNotNull(hovers[0]);
    assertEquals(22, hovers[0].getOffset());
    assertEquals(5, hovers[0].getLength());
    assertEquals("myLibrary", hovers[0].getContainingLibraryName());
    assertEquals("/path/to/lib", hovers[0].getContainingLibraryPath());
    assertEquals("some dartdoc", hovers[0].getDartdoc());
    assertEquals("element description", hovers[0].getElementDescription());
    assertEquals("element kind", hovers[0].getElementKind());
    assertEquals("some parameter", hovers[0].getParameter());
    assertEquals("typeA", hovers[0].getPropagatedType());
    assertEquals("typeB", hovers[0].getStaticType());
  }

  public void test_analysis_getHover_error() throws Exception {
    final HoverInformation[] hovers = new HoverInformation[1];
    final RequestError[] requestErrorArray = {null};
    server.analysis_getHover("/fileA.dart", 17, new GetHoverConsumer() {
      @Override
      public void computedHovers(HoverInformation[] result) {
        hovers[0] = result[0];
      }

      @Override
      public void onError(RequestError requestError) {
        requestErrorArray[0] = requestError;
      }
    });

    List<JsonObject> requests = requestSink.getRequests();
    JsonObject expected = parseJson(//
        "{",
        "  'id': '0',",
        "  'method': 'analysis.getHover',",
        "  'params': {",
        "    'file': '/fileA.dart',",
        "    'offset': 17",
        "  }",
        "}");
    assertTrue(requests.contains(expected));

    putResponse(//
        "{",
        "  'id': '0',",
        "  'method': 'edit.getFixes',",
        "  'error': {",
        "    'code': 'CONTENT_MODIFIED',",
        "    'message': 'message0',",
        "    'stackTrace': 'stackTrace0'",
        "  }",
        "}");
    responseStream.waitForEmpty();
    server.test_waitForWorkerComplete();
    assertNull(hovers[0]);
    assertNotNull(requestErrorArray[0]);
    RequestError requestError = requestErrorArray[0];
    assertNotNull(requestError);
    assertEquals("CONTENT_MODIFIED", requestError.getCode());
    assertEquals("message0", requestError.getMessage());
    assertEquals("stackTrace0", requestError.getStackTrace());
  }

  public void test_analysis_getLibraryDependencies() throws Exception {
    final String[][] libraries = {{null}};
    final List<Map<String, Map<String, List<String>>>> packageMap = Lists.newArrayList();
    final RequestError[] requestErrorArray = {null};
    server.analysis_getLibraryDependencies(new GetLibraryDependenciesConsumer() {
      @Override
      public void computedDependencies(String[] libs,
          Map<String, Map<String, List<String>>> packages) {
        libraries[0] = libs;
        packageMap.add(packages);
      }

      @Override
      public void onError(RequestError requestError) {
        requestErrorArray[0] = requestError;
      }
    });

    List<JsonObject> requests = requestSink.getRequests();
    JsonObject expected = parseJson(//
        "{",
        "  'id': '0',",
        "  'method': 'analysis.getLibraryDependencies'",
        "}");
    assertTrue(requests.contains(expected));

    putResponse(//
        "{",
        "  'id': '0',",
        "  'result': {",
        "    'packageMap': {",
        "      '/context1/' : {",
        "        'a' : ['/a/', '/a1/'],",
        "        'b' : ['/b/', '/b1/']",
        "       },",
        "      '/context2/' : {",
        "        'c' : ['/c/', '/c1/']",
        "       }",
        "     },",
        "    'libraries': [",
        "      '/a/b/c.dart',",
        "      '/d/e/f.dart',",
        "      '/g/h/i.dart'",
        "     ]",
        "   }",
        "}");
    responseStream.waitForEmpty();
    server.test_waitForWorkerComplete();

    assertNull(requestErrorArray[0]);
    assertThat(libraries[0]).hasSize(3);
    assertThat(packageMap).hasSize(1);
    assertThat(packageMap.get(0).get("/context1/")).includes(
        entry("a", Arrays.asList("/a/", "/a1/")));

  }

  public void test_analysis_getLibraryDependenciess_error() throws Exception {
    final String[][] libraries = {null};
    final List<Map<String, Map<String, List<String>>>> packageMap = Lists.newArrayList();
    final RequestError[] requestErrorArray = {null};
    server.analysis_getLibraryDependencies(new GetLibraryDependenciesConsumer() {
      @Override
      public void computedDependencies(String[] libs,
          Map<String, Map<String, List<String>>> packages) {
        libraries[0] = libs;
        packageMap.add(packages);
      }

      @Override
      public void onError(RequestError requestError) {
        requestErrorArray[0] = requestError;
      }
    });

    List<JsonObject> requests = requestSink.getRequests();
    JsonObject expected = parseJson(//
        "{",
        "  'id': '0',",
        "  'method': 'analysis.getLibraryDependencies'",
        "}");
    assertTrue(requests.contains(expected));

    putResponse(//
        "{",
        "  'id': '0',",
        "  'method': 'analysis.getLibraryDependencies',",
        "  'error': {",
        "    'code': 'CONTENT_MODIFIED',",
        "    'message': 'message0',",
        "    'stackTrace': 'stackTrace0'",
        "  }",
        "}");
    responseStream.waitForEmpty();
    server.test_waitForWorkerComplete();

    assertNull(libraries[0]);
    assertThat(packageMap).isEmpty();
    assertNotNull(requestErrorArray[0]);
    RequestError requestError = requestErrorArray[0];
    assertEquals("CONTENT_MODIFIED", requestError.getCode());
    assertEquals("message0", requestError.getMessage());
    assertEquals("stackTrace0", requestError.getStackTrace());
  }

  public void test_analysis_getNavigation() throws Exception {
    final List<NavigationRegion> results = Lists.newArrayList();
    final RequestError[] requestErrorArray = {null};
    server.analysis_getNavigation("/test.dart", 1, 2, new GetNavigationConsumer() {
      @Override
      public void computedNavigation(List<NavigationRegion> regions) {
        results.addAll(regions);
      }

      @Override
      public void onError(RequestError requestError) {
        requestErrorArray[0] = requestError;
      }
    });

    List<JsonObject> requests = requestSink.getRequests();
    JsonObject expected = parseJson(//
        "{",
        "  'id': '0',",
        "  'method': 'analysis.getNavigation',",
        "  'params': {",
        "    'file': '/test.dart',",
        "    'offset': 1,",
        "    'length': 2",
        "  }",
        "}");
    assertTrue(requests.contains(expected));

    putResponse(//
        "{",
        "  'id': '0',",
        "  'result': {",
        "    'files': ['/test2.dart', '/test3.dart'],",
        "    'targets': [",
        "      {",
        "        'kind': 'COMPILATION_UNIT',",
        "        'fileIndex': 0,",
        "        'offset': 3,",
        "        'length': 4,",
        "        'startLine': 5,",
        "        'startColumn': 6",
        "      },",
        "      {",
        "        'kind': 'CLASS',",
        "        'fileIndex': 1,",
        "        'offset': 7,",
        "        'length': 8,",
        "        'startLine': 9,",
        "        'startColumn': 10",
        "      }",
        "    ],",
        "    'regions' : [",
        "      {",
        "        'offset': 1,",
        "        'length': 2,",
        "        'targets': [0, 1]",
        "      }",
        "    ]",
        "  }",
        "}");
    responseStream.waitForEmpty();
    server.test_waitForWorkerComplete();

    assertNull(requestErrorArray[0]);
    assertThat(results).hasSize(1);
    {
      NavigationRegion region = results.get(0);
      assertEquals(1, region.getOffset());
      assertEquals(2, region.getLength());
      List<NavigationTarget> targets = region.getTargetObjects();
      assertThat(targets).hasSize(2);
      {
        NavigationTarget target = targets.get(0);
        assertEquals(ElementKind.COMPILATION_UNIT, target.getKind());
        assertEquals("/test2.dart", target.getFile());
        assertEquals(3, target.getOffset());
        assertEquals(4, target.getLength());
        assertEquals(5, target.getStartLine());
        assertEquals(6, target.getStartColumn());
      }
      {
        NavigationTarget target = targets.get(1);
        assertEquals(ElementKind.CLASS, target.getKind());
        assertEquals("/test3.dart", target.getFile());
        assertEquals(7, target.getOffset());
        assertEquals(8, target.getLength());
        assertEquals(9, target.getStartLine());
        assertEquals(10, target.getStartColumn());
      }
    }
  }

  public void test_analysis_getNavigation_error() throws Exception {
    final List<NavigationRegion> results = Lists.newArrayList();
    final RequestError[] requestErrorArray = {null};
    server.analysis_getNavigation("/test.dart", 1, 2, new GetNavigationConsumer() {
      @Override
      public void computedNavigation(List<NavigationRegion> regions) {
        results.addAll(regions);
      }

      @Override
      public void onError(RequestError requestError) {
        requestErrorArray[0] = requestError;
      }
    });

    List<JsonObject> requests = requestSink.getRequests();
    JsonObject expected = parseJson(//
        "{",
        "  'id': '0',",
        "  'method': 'analysis.getNavigation',",
        "  'params': {",
        "    'file': '/test.dart',",
        "    'offset': 1,",
        "    'length': 2",
        "  }",
        "}");
    assertTrue(requests.contains(expected));

    putResponse(//
        "{",
        "  'id': '0',",
        "  'error': {",
        "    'code': 'CONTENT_MODIFIED',",
        "    'message': 'message0',",
        "    'stackTrace': 'stackTrace0'",
        "  }",
        "}");
    responseStream.waitForEmpty();
    server.test_waitForWorkerComplete();

    assertThat(results).isEmpty();
    assertNotNull(requestErrorArray[0]);
    RequestError requestError = requestErrorArray[0];
    assertEquals("CONTENT_MODIFIED", requestError.getCode());
    assertEquals("message0", requestError.getMessage());
    assertEquals("stackTrace0", requestError.getStackTrace());
  }

  public void test_analysis_notification_analyzedFiles() throws Exception {
    putResponse(//
        "{",
        "  'event': 'analysis.analyzedFiles',",
        "  'params': {",
        "    'directories': ['/test1.dart','/test2.dart','/test3.dart','/test4.dart']",
        "  }",
        "}");
    responseStream.waitForEmpty();
    server.test_waitForWorkerComplete();
    listener.assertAnalyzedFiles(ImmutableList.of(
        "/test1.dart",
        "/test2.dart",
        "/test3.dart",
        "/test4.dart"));
  }

  public void test_analysis_notification_errors() throws Exception {
    putResponse(//
        "{",
        "  'event': 'analysis.errors',",
        "  'params': {",
        "    'file': '/test.dart',",
        "    'errors' : [",
        "      {",
        "        'severity': 'ERROR',",
        "        'type': 'SYNTACTIC_ERROR',",
        "        'location': {",
        "          'file': '/fileA.dart',",
        "          'offset': 1,",
        "          'length': 2,",
        "          'startLine': 3,",
        "          'startColumn': 4",
        "        },",
        "        'message': 'message A',",
        "        'correction': 'correction A',",
        "        'hasFix': false",
        "      },",
        "      {",
        "        'severity': 'ERROR',",
        "        'type': 'COMPILE_TIME_ERROR',",
        "        'location': {",
        "          'file': '/fileB.dart',",
        "          'offset': 5,",
        "          'length': 6,",
        "          'startLine': 7,",
        "          'startColumn': 8",
        "        },",
        "        'message': 'message B',",
        "        'correction': 'correction B',",
        "        'hasFix': false",
        "      }",
        "    ]",
        "  }",
        "}");
    responseStream.waitForEmpty();
    server.test_waitForWorkerComplete();
    listener.assertErrorsWithAnalysisErrors("/test.dart", new AnalysisError(
        AnalysisErrorSeverity.ERROR,
        "SYNTACTIC_ERROR",
        new Location("/fileA.dart", 1, 2, 3, 4),
        "message A",
        "correction A", false), new AnalysisError(
        AnalysisErrorSeverity.ERROR,
        "COMPILE_TIME_ERROR",
        new Location("/fileB.dart", 5, 6, 7, 8),
        "message B",
        "correction B", false));
  }

  public void test_analysis_notification_flushResults() throws Exception {
    listener.assertFlushedResults(new ArrayList<String>(0));
    putResponse(//
        "{",
        "  'event': 'analysis.flushResults',",
        "  'params': {",
        "    'files': [",
        "      'file1.dart',",
        "      'file2.dart',",
        "      'file3.dart'",
        "    ]",
        "  }",
        "}");
    responseStream.waitForEmpty();
    server.test_waitForWorkerComplete();
    listener.assertFlushedResults(ImmutableList.of("file1.dart", "file2.dart", "file3.dart"));
  }

  public void test_analysis_notification_highlights() throws Exception {
    putResponse(//
        "{",
        "  'event': 'analysis.highlights',",
        "  'params': {",
        "    'file': '/test.dart',",
        "    'regions' : [",
        "      {",
        "        'type': 'CLASS',",
        "         'offset': 1,",
        "        'length': 2",
        "      },",
        "      {",
        "        'type': 'INSTANCE_FIELD_REFERENCE',",
        "        'offset': 10,",
        "        'length': 20",
        "      }",
        "    ]",
        "  }",
        "}");
    responseStream.waitForEmpty();
    server.test_waitForWorkerComplete();
    List<HighlightRegion> regions = listener.getHighlightRegions("/test.dart");
    assertThat(regions).hasSize(2);
    {
      HighlightRegion error = regions.get(0);
      assertEquals(HighlightRegionType.CLASS, error.getType());
      assertEquals(1, error.getOffset());
      assertEquals(2, error.getLength());
    }
    {
      HighlightRegion error = regions.get(1);
      assertEquals(HighlightRegionType.INSTANCE_FIELD_REFERENCE, error.getType());
      assertEquals(10, error.getOffset());
      assertEquals(20, error.getLength());
    }
  }

  public void test_analysis_notification_implemented() throws Exception {
    putResponse(//
        "{",
        "  'event': 'analysis.implemented',",
        "  'params': {",
        "    'file': '/test.dart',",
        "    'classes': [",
        "      {",
        "        'offset': 1,",
        "        'length': 2",
        "      }",
        "    ],",
        "    'members': [",
        "      {",
        "        'offset': 3,",
        "        'length': 4",
        "      }",
        "    ]",
        "  }",
        "}");
    responseStream.waitForEmpty();
    server.test_waitForWorkerComplete();
    {
      List<ImplementedClass> implementedClasses = listener.getImplementedClasses("/test.dart");
      assertThat(implementedClasses).hasSize(1);
      {
        ImplementedClass clazz = implementedClasses.get(0);
        assertEquals(1, clazz.getOffset());
        assertEquals(2, clazz.getLength());
      }
    }
    {
      List<ImplementedMember> implementedMembers = listener.getImplementedMembers("/test.dart");
      assertThat(implementedMembers).hasSize(1);
      {
        ImplementedMember member = implementedMembers.get(0);
        assertEquals(3, member.getOffset());
        assertEquals(4, member.getLength());
      }
    }
  }

  public void test_analysis_notification_navigation() throws Exception {
    putResponse(//
        "{",
        "  'event': 'analysis.navigation',",
        "  'params': {",
        "    'file': '/test.dart',",
        "    'files': ['/test2.dart', '/test3.dart'],",
        "    'targets': [",
        "      {",
        "        'kind': 'COMPILATION_UNIT',",
        "        'fileIndex': 0,",
        "        'offset': 3,",
        "        'length': 4,",
        "        'startLine': 5,",
        "        'startColumn': 6",
        "      },",
        "      {",
        "        'kind': 'CLASS',",
        "        'fileIndex': 1,",
        "        'offset': 7,",
        "        'length': 8,",
        "        'startLine': 9,",
        "        'startColumn': 10",
        "      }",
        "    ],",
        "    'regions' : [",
        "      {",
        "        'offset': 1,",
        "        'length': 2,",
        "        'targets': [0, 1]",
        "      }",
        "    ]",
        "  }",
        "}");
    responseStream.waitForEmpty();
    server.test_waitForWorkerComplete();
    List<NavigationRegion> regions = listener.getNavigationRegions("/test.dart");
    assertThat(regions).hasSize(1);
    {
      NavigationRegion region = regions.get(0);
      assertEquals(1, region.getOffset());
      assertEquals(2, region.getLength());
      List<NavigationTarget> targets = region.getTargetObjects();
      assertThat(targets).hasSize(2);
      {
        NavigationTarget target = targets.get(0);
        assertEquals(ElementKind.COMPILATION_UNIT, target.getKind());
        assertEquals("/test2.dart", target.getFile());
        assertEquals(3, target.getOffset());
        assertEquals(4, target.getLength());
        assertEquals(5, target.getStartLine());
        assertEquals(6, target.getStartColumn());
      }
      {
        NavigationTarget target = targets.get(1);
        assertEquals(ElementKind.CLASS, target.getKind());
        assertEquals("/test3.dart", target.getFile());
        assertEquals(7, target.getOffset());
        assertEquals(8, target.getLength());
        assertEquals(9, target.getStartLine());
        assertEquals(10, target.getStartColumn());
      }
    }
  }

  public void test_analysis_notification_occurences() throws Exception {
    putResponse(//
        "{",
        "  'event': 'analysis.occurrences',",
        "  'params': {",
        "    'file': '/test.dart',",
        "    'occurrences' : [",
        "      {",
        "        'element': {",
        "          'kind': 'CLASS',",
        "          'name': 'name0',",
        "          'location': {",
        "            'file': '/test2.dart',",
        "            'offset': 7,",
        "            'length': 8,",
        "            'startLine': 9,",
        "            'startColumn': 10",
        "          },",
        "          'flags': 63",
        "        },",
        "        'offsets': [1,2,3,4,5],",
        "        'length': 6",
        "      }",
        "    ]",
        "  }",
        "}");
    responseStream.waitForEmpty();
    server.test_waitForWorkerComplete();
    List<Occurrences> occurrencesList = listener.getOccurrences("/test.dart");

    // assertions on occurrences
    assertThat(occurrencesList).hasSize(1);
    Occurrences occurrences = occurrencesList.get(0);
    {
      Element element = occurrences.getElement();
      assertEquals(ElementKind.CLASS, element.getKind());
      assertEquals("name0", element.getName());
      Location location = element.getLocation();
      assertEquals("/test2.dart", location.getFile());
      assertEquals(7, location.getOffset());
      assertEquals(8, location.getLength());
      assertEquals(9, location.getStartLine());
      assertEquals(10, location.getStartColumn());
      assertTrue(element.isAbstract());
      assertTrue(element.isConst());
      assertTrue(element.isDeprecated());
      assertTrue(element.isFinal());
      assertTrue(element.isPrivate());
      assertTrue(element.isTopLevelOrStatic());
      assertNull(element.getParameters());
      assertNull(element.getReturnType());
    }
    assertThat(occurrences.getOffsets()).hasSize(5).contains(1, 2, 3, 4, 5);
    assertEquals(6, occurrences.getLength());
  }

  public void test_analysis_notification_outline() throws Exception {
    putResponse(//
        "{",
        "  'event': 'analysis.outline',",
        "  'params': {",
        "    'file': '/test.dart',",
        "    'outline' : {",
        "      'element': {",
        "        'kind': 'COMPILATION_UNIT',",
        "        'name': 'name0',",
        "        'location': {",
        "          'file': '/test2.dart',",
        "          'offset': 3,",
        "          'length': 4,",
        "          'startLine': 5,",
        "          'startColumn': 6",
        "        },",
        "        'flags': 63,",
        "        'parameters': 'parameters0',",
        "        'returnType': 'returnType0'",
        "      },",
        "      'offset': 1,",
        "      'length': 2,",
        "      'children': [",
        "      {",
        "        'element': {",
        "          'kind': 'CLASS',",
        "          'name': '_name1',",
        "          'location': {",
        "            'file': '/test3.dart',",
        "            'offset': 9,",
        "            'length': 10,",
        "            'startLine': 11,",
        "            'startColumn': 12",
        "          },",
        "          'flags': 0",
        "        },",
        "        'offset': 7,",
        "        'length': 8",
        "      }",
        "    ]",
        "    }",
        "  }",
        "}");
    responseStream.waitForEmpty();
    server.test_waitForWorkerComplete();
    Outline outline = listener.getOutline("/test.dart");

    // assertions on outline
    assertThat(outline.getChildren()).hasSize(1);
    assertEquals(1, outline.getOffset());
    assertEquals(2, outline.getLength());
    Element element = outline.getElement();
    assertEquals(ElementKind.COMPILATION_UNIT, element.getKind());
    assertEquals("name0", element.getName());
    Location location = element.getLocation();
    assertEquals("/test2.dart", location.getFile());
    assertEquals(3, location.getOffset());
    assertEquals(4, location.getLength());
    assertEquals(5, location.getStartLine());
    assertEquals(6, location.getStartColumn());
    assertTrue(element.isAbstract());
    assertTrue(element.isConst());
    assertTrue(element.isDeprecated());
    assertTrue(element.isFinal());
    assertTrue(element.isPrivate());
    assertTrue(element.isTopLevelOrStatic());
    assertEquals("parameters0", element.getParameters());
    assertEquals("returnType0", element.getReturnType());

    // assertions on child
    Outline child = outline.getChildren().get(0);
    assertEquals(7, child.getOffset());
    assertEquals(8, child.getLength());
    assertThat(child.getChildren()).hasSize(0);
    Element childElement = child.getElement();
    assertEquals(ElementKind.CLASS, childElement.getKind());
    assertEquals("_name1", childElement.getName());
    location = childElement.getLocation();
    assertEquals("/test3.dart", location.getFile());
    assertEquals(9, location.getOffset());
    assertEquals(10, location.getLength());
    assertEquals(11, location.getStartLine());
    assertEquals(12, location.getStartColumn());

    assertFalse(childElement.isAbstract());
    assertFalse(childElement.isConst());
    assertFalse(childElement.isDeprecated());
    assertFalse(childElement.isFinal());
    assertFalse(childElement.isPrivate());
    assertFalse(childElement.isTopLevelOrStatic());
    assertNull(childElement.getParameters());
    assertNull(childElement.getReturnType());
  }

  public void test_analysis_notification_overrides() throws Exception {
    putResponse(//
        "{",
        "  'event': 'analysis.overrides',",
        "  'params': {",
        "    'file': '/test.dart',",
        "    'overrides' : [",
        "      {",
        "        'offset': 1,",
        "        'length': 2,",
        "        'superclassMember': {",
        "          'className':'superclassName1',",
        "          'element': {",
        "            'kind': 'CLASS',",
        "            'name': 'name1',",
        "            'location': {",
        "              'file': '/test2.dart',",
        "              'offset': 3,",
        "              'length': 4,",
        "              'startLine': 5,",
        "              'startColumn': 6",
        "            },",
        "            'flags': 0",
        "          }",
        "        }",
        "      },",
        "      {",
        "        'offset': 7,",
        "        'length': 8",
        "      }",
        "    ]",
        "  }",
        "}");
    responseStream.waitForEmpty();
    server.test_waitForWorkerComplete();
    List<OverrideMember> overrides = listener.getOverrides("/test.dart");

    // assertions on overrides
    assertThat(overrides).hasSize(2);
    {
      assertEquals(1, overrides.get(0).getOffset());
      assertEquals(2, overrides.get(0).getLength());
      OverriddenMember superclassMember = overrides.get(0).getSuperclassMember();
      assertNotNull(superclassMember);
      assertEquals("superclassName1", superclassMember.getClassName());
      assertEquals("name1", superclassMember.getElement().getName());
    }
    {
      assertEquals(7, overrides.get(1).getOffset());
      assertEquals(8, overrides.get(1).getLength());
      assertNull(overrides.get(1).getSuperclassMember());
    }
  }

  public void test_analysis_reanalyze() throws Exception {
    server.analysis_reanalyze(null);
    List<JsonObject> requests = requestSink.getRequests();
    JsonObject expected = parseJson(//
        "{",
        "  'id': '0',",
        "  'method': 'analysis.reanalyze'",
        "}");
    assertTrue(requests.contains(expected));
  }

  public void test_analysis_setAnalysisRoots() throws Exception {
    server.analysis_setAnalysisRoots(
        ImmutableList.of("/fileA.dart", "/fileB.dart"),
        ImmutableList.of("/fileC.dart", "/fileD.dart"),
        null);
    List<JsonObject> requests = requestSink.getRequests();
    JsonObject expected = parseJson(//
        "{",
        "  'id': '0',",
        "  'method': 'analysis.setAnalysisRoots',",
        "  'params': {",
        "    'included': ['/fileA.dart', '/fileB.dart'],",
        "    'excluded': ['/fileC.dart', '/fileD.dart']",
        "  }",
        "}");
    assertTrue(requests.contains(expected));
  }

  public void test_analysis_setAnalysisRoots_emptyLists() throws Exception {
    server.analysis_setAnalysisRoots(new ArrayList<String>(0), new ArrayList<String>(0), null);
    List<JsonObject> requests = requestSink.getRequests();
    JsonObject expected = parseJson(//
        "{",
        "  'id': '0',",
        "  'method': 'analysis.setAnalysisRoots',",
        "  'params': {",
        "    'included': [],",
        "    'excluded': []",
        "  }",
        "}");
    assertTrue(requests.contains(expected));
  }

  public void test_analysis_setAnalysisRoots_nullLists() throws Exception {
    server.analysis_setAnalysisRoots(null, null, null);
    List<JsonObject> requests = requestSink.getRequests();
    JsonObject expected = parseJson(//
        "{",
        "  'id': '0',",
        "  'method': 'analysis.setAnalysisRoots',",
        "  'params': {",
        "    'included': [],",
        "    'excluded': []",
        "  }",
        "}");
    assertTrue(requests.contains(expected));
  }

  public void test_analysis_setAnalysisRoots_packageRoots() throws Exception {
    server.analysis_setAnalysisRoots(
        null,
        null,
        ImmutableMap.of("/path1", "/path2", "/path3", "/path4"));
    List<JsonObject> requests = requestSink.getRequests();
    JsonObject expected = parseJson(//
        "{",
        "  'id': '0',",
        "  'method': 'analysis.setAnalysisRoots',",
        "  'params': {",
        "    'included': [],",
        "    'excluded': [],",
        "    'packageRoots': {",
        "      '/path1': '/path2',",
        "      '/path3': '/path4'",
        "    }",
        "  }",
        "}");
    assertTrue(requests.contains(expected));
  }

  public void test_analysis_setGeneralSubscriptions() throws Exception {
    List<String> subscriptions = Lists.newArrayList();
    subscriptions.add(GeneralAnalysisService.ANALYZED_FILES);
    server.analysis_setGeneralSubscriptions(subscriptions);
    List<JsonObject> requests = requestSink.getRequests();
    JsonObject expected = parseJson(//
        "{",
        "  'id': '0',",
        "  'method': 'analysis.setGeneralSubscriptions',",
        "  'params': {",
        "    'subscriptions': [",
        "      ANALYZED_FILES",
        "    ]",
        "  }",
        "}");
    assertTrue(requests.contains(expected));
  }

  public void test_analysis_setPriorityFiles() throws Exception {
    server.analysis_setPriorityFiles(ImmutableList.of("/fileA.dart", "/fileB.dart"));
    List<JsonObject> requests = requestSink.getRequests();
    JsonObject expected = parseJson(//
        "{",
        "  'id': '0',",
        "  'method': 'analysis.setPriorityFiles',",
        "  'params': {",
        "    'files': ['/fileA.dart', '/fileB.dart']",
        "  }",
        "}");
    assertTrue(requests.contains(expected));
  }

  public void test_analysis_setPriorityFiles_emptyList() throws Exception {
    server.analysis_setPriorityFiles(new ArrayList<String>(0));
    List<JsonObject> requests = requestSink.getRequests();
    JsonObject expected = parseJson(//
        "{",
        "  'id': '0',",
        "  'method': 'analysis.setPriorityFiles',",
        "  'params': {",
        "    'files': []",
        "  }",
        "}");
    assertTrue(requests.contains(expected));
  }

  public void test_analysis_setPriorityFiles_nullList() throws Exception {
    server.analysis_setPriorityFiles(null);
    List<JsonObject> requests = requestSink.getRequests();
    JsonObject expected = parseJson(//
        "{",
        "  'id': '0',",
        "  'method': 'analysis.setPriorityFiles',",
        "  'params': {",
        "    'files': []",
        "  }",
        "}");
    assertTrue(requests.contains(expected));
  }

  public void test_analysis_setSubscriptions() throws Exception {
    Map<String, List<String>> subscriptionsMap = new LinkedHashMap<String, List<String>>();
    subscriptionsMap.put(AnalysisService.FOLDING, new ArrayList<String>(0));
    subscriptionsMap.put(AnalysisService.HIGHLIGHTS, ImmutableList.of("/fileA.dart"));
    subscriptionsMap.put(AnalysisService.NAVIGATION, ImmutableList.of("/fileB.dart", "/fileC.dart"));
    subscriptionsMap.put(
        AnalysisService.OCCURRENCES,
        ImmutableList.of("/fileD.dart", "/fileE.dart", "/fileF.dart"));

    server.analysis_setSubscriptions(subscriptionsMap);
    List<JsonObject> requests = requestSink.getRequests();
    JsonObject expected = parseJson(//
        "{",
        "  'id': '0',",
        "  'method': 'analysis.setSubscriptions',",
        "  'params': {",
        "    'subscriptions': {",
        "      FOLDING: [],",
        "      HIGHLIGHTS: ['/fileA.dart'],",
        "      NAVIGATION: ['/fileB.dart', '/fileC.dart'],",
        "      OCCURRENCES: ['/fileD.dart', '/fileE.dart', '/fileF.dart']",
        "    }",
        "  }",
        "}");
    assertTrue(requests.contains(expected));
  }

  public void test_analysis_setSubscriptions_emptyMap() throws Exception {
    server.analysis_setSubscriptions(new HashMap<String, List<String>>(0));
    List<JsonObject> requests = requestSink.getRequests();
    JsonObject expected = parseJson(//
        "{",
        "  'id': '0',",
        "  'method': 'analysis.setSubscriptions',",
        "  'params': {",
        "    'subscriptions': {}",
        "  }",
        "}");
    assertTrue(requests.contains(expected));
  }

  public void test_analysis_setSubscriptions_nullMap() throws Exception {
    server.analysis_setSubscriptions(null);
    List<JsonObject> requests = requestSink.getRequests();
    JsonObject expected = parseJson(//
        "{",
        "  'id': '0',",
        "  'method': 'analysis.setSubscriptions',",
        "  'params': {",
        "    'subscriptions': {}",
        "  }",
        "}");
    assertTrue(requests.contains(expected));
  }

  public void test_analysis_updateContent_AddContentOverlay() throws Exception {
    Map<String, Object> files = new HashMap<String, Object>(2);
    files.put("/fileA.dart", new AddContentOverlay("content for A"));
    files.put("/fileB.dart", new AddContentOverlay("content for B"));
    server.analysis_updateContent(files, new UpdateContentConsumer() {
      @Override
      public void onResponse() {
      }
    });
    List<JsonObject> requests = requestSink.getRequests();
    JsonObject expected = parseJson(//
        "{",
        "  'id': '0',",
        "  'method': 'analysis.updateContent',",
        "  'params': {",
        "    'files': {",
        "      '/fileA.dart': {",
        "        'type': 'add',",
        "        'content': 'content for A'",
        "        },",
        "      '/fileB.dart': {",
        "        'type': 'add',",
        "        'content': 'content for B'",
        "      }",
        "    }",
        "  }",
        "}");
    assertTrue(requests.contains(expected));
  }

  public void test_analysis_updateContent_ChangeContentOverlay() throws Exception {
    Map<String, Object> files = new HashMap<String, Object>(2);
    List<SourceEdit> sourceEdit1 = new ArrayList<SourceEdit>(1);
    sourceEdit1.add(new SourceEdit(0, 1, "replacement1", null));
    List<SourceEdit> sourceEdit2 = new ArrayList<SourceEdit>(1);
    sourceEdit2.add(new SourceEdit(2, 3, "replacement2", "sourceEditId2"));
    files.put("/fileA.dart", new ChangeContentOverlay(sourceEdit1));
    files.put("/fileB.dart", new ChangeContentOverlay(sourceEdit2));
    server.analysis_updateContent(files, new UpdateContentConsumer() {
      @Override
      public void onResponse() {
      }
    });
    List<JsonObject> requests = requestSink.getRequests();
    JsonObject expected = parseJson(//
        "{",
        "  'id': '0',",
        "  'method': 'analysis.updateContent',",
        "  'params': {",
        "    'files': {",
        "      '/fileA.dart': {",
        "        'type': 'change',",
        "        'edits': [",
        "          {",
        "            'offset': 0,",
        "            'length': 1,",
        "            'replacement': 'replacement1'",
        "          }",
        "        ]",
        "      },",
        "      '/fileB.dart': {",
        "        'type': 'change',",
        "        'edits': [",
        "          {",
        "            'offset': 2,",
        "            'length': 3,",
        "            'replacement': 'replacement2',",
        "            'id': 'sourceEditId2'",
        "          }",
        "        ]",
        "      }",
        "    }",
        "  }",
        "}");
    assertTrue(requests.contains(expected));
  }

  public void test_analysis_updateContent_emptyList() throws Exception {
    Map<String, Object> files = new HashMap<String, Object>(0);
    server.analysis_updateContent(files, new UpdateContentConsumer() {
      @Override
      public void onResponse() {
      }
    });
    List<JsonObject> requests = requestSink.getRequests();
    JsonObject expected = parseJson(//
        "{",
        "  'id': '0',",
        "  'method': 'analysis.updateContent',",
        "  'params': {",
        "    'files': {",
        "    }",
        "  }",
        "}");
    assertTrue(requests.contains(expected));
  }

  public void test_analysis_updateContent_nullList() throws Exception {
    server.analysis_updateContent(null, new UpdateContentConsumer() {
      @Override
      public void onResponse() {
      }
    });
    List<JsonObject> requests = requestSink.getRequests();
    JsonObject expected = parseJson(//
        "{",
        "  'id': '0',",
        "  'method': 'analysis.updateContent',",
        "  'params': {",
        "    'files': {",
        "    }",
        "  }",
        "}");
    assertTrue(requests.contains(expected));
  }

  public void test_analysis_updateContent_RemoveContentOverlay() throws Exception {
    Map<String, Object> files = new HashMap<String, Object>(2);
    files.put("/fileA.dart", new RemoveContentOverlay());
    files.put("/fileB.dart", new RemoveContentOverlay());
    server.analysis_updateContent(files, new UpdateContentConsumer() {
      @Override
      public void onResponse() {
      }
    });
    List<JsonObject> requests = requestSink.getRequests();
    JsonObject expected = parseJson(//
        "{",
        "  'id': '0',",
        "  'method': 'analysis.updateContent',",
        "  'params': {",
        "    'files': {",
        "      '/fileA.dart': {",
        "        'type': 'remove'",
        "        },",
        "      '/fileB.dart': {",
        "        'type': 'remove'",
        "      }",
        "    }",
        "  }",
        "}");
    assertTrue(requests.contains(expected));
  }

  public void test_analysis_updateOptions_all_false() throws Exception {
    AnalysisOptions options = new AnalysisOptions(
        false,
        false,
        false,
        false,
        false,
        false,
        false,
        false);
    server.analysis_updateOptions(options);
    List<JsonObject> requests = requestSink.getRequests();
    JsonObject expected = parseJson(//
        "{",
        "  'id': '0',",
        "  'method': 'analysis.updateOptions',",
        "  'params': {",
        "    'options': {",
        "      'enableAsync': false,",
        "      'enableDeferredLoading': false,",
        "      'enableEnums': false,",
        "      'enableNullAwareOperators': false,",
        "      'enableSuperMixins': false,",
        "      'generateDart2jsHints': false,",
        "      'generateHints': false,",
        "      'generateLints': false",
        "    }",
        "  }",
        "}");
    assertTrue(requests.contains(expected));
  }

  public void test_analysis_updateOptions_all_true() throws Exception {
    AnalysisOptions options = new AnalysisOptions(true, true, true, true, true, true, true, true);
    server.analysis_updateOptions(options);
    List<JsonObject> requests = requestSink.getRequests();
    JsonObject expected = parseJson(//
        "{",
        "  'id': '0',",
        "  'method': 'analysis.updateOptions',",
        "  'params': {",
        "    'options': {",
        "      'enableAsync': true,",
        "      'enableDeferredLoading': true,",
        "      'enableEnums': true,",
        "      'enableNullAwareOperators': true,",
        "      'enableSuperMixins': true,",
        "      'generateDart2jsHints': true,",
        "      'generateHints': true,",
        "      'generateLints': true",
        "    }",
        "  }",
        "}");
    assertTrue(requests.contains(expected));
  }

  public void test_analysis_updateOptions_subset1() throws Exception {
    AnalysisOptions options = new AnalysisOptions(true, null, null, null, null, null, null, null);
    server.analysis_updateOptions(options);
    List<JsonObject> requests = requestSink.getRequests();
    JsonObject expected = parseJson(//
        "{",
        "  'id': '0',",
        "  'method': 'analysis.updateOptions',",
        "  'params': {",
        "    'options': {",
        "      'enableAsync': true",
        "    }",
        "  }",
        "}");
    assertTrue(requests.contains(expected));
  }

  public void test_analysis_updateOptions_subset2() throws Exception {
    AnalysisOptions options = new AnalysisOptions(false, true, null, null, null, null, null, null);
    server.analysis_updateOptions(options);
    List<JsonObject> requests = requestSink.getRequests();
    JsonObject expected = parseJson(//
        "{",
        "  'id': '0',",
        "  'method': 'analysis.updateOptions',",
        "  'params': {",
        "    'options': {",
        "      'enableAsync': false,",
        "      'enableDeferredLoading': true",
        "    }",
        "  }",
        "}");
    assertTrue(requests.contains(expected));
  }

  public void test_completion_getSuggestions() throws Exception {
    final String[] completionIdPtr = {null};
    final RequestError[] requestErrorArray = {null};
    server.completion_getSuggestions("/fileA.dart", 0, new GetSuggestionsConsumer() {
      @Override
      public void computedCompletionId(String completionId) {
        completionIdPtr[0] = completionId;
      }

      @Override
      public void onError(RequestError requestError) {
        requestErrorArray[0] = requestError;
      }
    });
    List<JsonObject> requests = requestSink.getRequests();
    JsonObject expected = parseJson(//
        "{",
        "  'id': '0',",
        "  'method': 'completion.getSuggestions',",
        "  'params': {",
        "    'file': '/fileA.dart',",
        "    'offset': 0",
        "  }",
        "}");
    assertTrue(requests.contains(expected));

    putResponse(//
        "{",
        "  'id': '0',",
        "  'result': {",
        "    'id': 'completionId0'",
        "  }",
        "}");
    responseStream.waitForEmpty();
    server.test_waitForWorkerComplete();
    assertNull(requestErrorArray[0]);
    assertEquals("completionId0", completionIdPtr[0]);
  }

  public void test_completion_getSuggestions_error() throws Exception {
    final String[] completionIdPtr = {null};
    final RequestError[] requestErrorArray = {null};
    server.completion_getSuggestions("/fileA.dart", 0, new GetSuggestionsConsumer() {
      @Override
      public void computedCompletionId(String completionId) {
        completionIdPtr[0] = completionId;
      }

      @Override
      public void onError(RequestError requestError) {
        requestErrorArray[0] = requestError;
      }
    });
    List<JsonObject> requests = requestSink.getRequests();
    JsonObject expected = parseJson(//
        "{",
        "  'id': '0',",
        "  'method': 'completion.getSuggestions',",
        "  'params': {",
        "    'file': '/fileA.dart',",
        "    'offset': 0",
        "  }",
        "}");
    assertTrue(requests.contains(expected));

    putResponse(//
        "{",
        "  'id': '0',",
        "  'method': 'edit.getFixes',",
        "  'error': {",
        "    'code': 'CONTENT_MODIFIED',",
        "    'message': 'message0',",
        "    'stackTrace': 'stackTrace0'",
        "  }",
        "}");
    responseStream.waitForEmpty();
    server.test_waitForWorkerComplete();

    assertNull(completionIdPtr[0]);
    RequestError requestError = requestErrorArray[0];
    assertNotNull(requestError);
    assertEquals("CONTENT_MODIFIED", requestError.getCode());
    assertEquals("message0", requestError.getMessage());
    assertEquals("stackTrace0", requestError.getStackTrace());
  }

  public void test_completion_notification_results() throws Exception {
    putResponse(//
        "{",
        "  'event': 'completion.results',",
        "  'params': {",
        "    'id': 'completion0',",
        "    'replacementOffset': 107,",
        "    'replacementLength': 108,",
        "    'results' : [",
        "      {",
        "        'kind': 'INVOCATION',",
        "        'relevance': 2000,",
        "        'completion': 'completion0',",
        "        'selectionOffset': 4,",
        "        'selectionLength': 5,",
        "        'isDeprecated': true,",
        "        'isPotential': true,",
        "        'docSummary': 'docSummary0',",
        "        'docComplete': 'docComplete0',",
        "        'declaringType': 'declaringType0',",
        "        'returnType': 'returnType0',",
        "        'parameterNames': ['param0', 'param1'],",
        "        'parameterTypes': ['paramType0', 'paramType1'],",
        "        'requiredParameterCount': 2,",
        "        'hasNamedParameters': false,",
        "        'parameterName': 'param2',",
        "        'parameterType': 'paramType2',",
        "        'element': {",
        "          'kind': 'METHOD',",
        "          'name': 'completion0',",
        "          'flags': 0,",
        "          'parameters': '(paramType0 param0, paramType1 param1)'",
        "        }",
        "      },",
        "      {",
        "        'kind': 'IDENTIFIER',",
        "        'relevance': 1000,",
        "        'completion': 'completion1',",
        "        'selectionOffset': 10,",
        "        'selectionLength': 11,",
        "        'isDeprecated': true,",
        "        'isPotential': true,",
        "        'element': {",
        "          'kind': 'CLASS',",
        "          'name': 'completion1',",
        "          'flags': 1",
        "        }",
        "      }",
        "    ],",
        "    'isLast': true",
        "  }",
        "}");
    responseStream.waitForEmpty();
    server.test_waitForWorkerComplete();
    assertThat(listener.getCompletionReplacementOffset("completion0")).isEqualTo(107);
    assertThat(listener.getCompletionReplacementLength("completion0")).isEqualTo(108);
    List<CompletionSuggestion> suggestions = listener.getCompletions("completion0");
    assertThat(suggestions).hasSize(2);
    assertThat(listener.getCompletionIsLast("completion0")).isEqualTo(true);
    {
      CompletionSuggestion suggestion = suggestions.get(0);
      assertEquals(CompletionSuggestionKind.INVOCATION, suggestion.getKind());
      assertEquals(2000, suggestion.getRelevance());
      assertEquals(suggestion.getCompletion(), "completion0");
      assertEquals(4, suggestion.getSelectionOffset());
      assertEquals(5, suggestion.getSelectionLength());
      assertTrue(suggestion.isDeprecated());
      assertTrue(suggestion.isPotential());
      assertEquals(suggestion.getDocSummary(), "docSummary0");
      assertEquals(suggestion.getDocComplete(), "docComplete0");
      assertEquals(suggestion.getDeclaringType(), "declaringType0");
      assertEquals(suggestion.getReturnType(), "returnType0");
      List<String> parameterNames = suggestion.getParameterNames();
      assertThat(parameterNames).hasSize(2);
      assertThat(parameterNames).contains("param0");
      assertThat(parameterNames).contains("param1");
      List<String> parameterTypes = suggestion.getParameterTypes();
      assertThat(parameterTypes).hasSize(2);
      assertThat(parameterTypes).contains("paramType0");
      assertThat(parameterTypes).contains("paramType1");
      assertEquals(suggestion.getRequiredParameterCount(), new Integer(2));
      assertEquals(suggestion.getHasNamedParameters(), new Boolean(false));
      assertEquals(suggestion.getParameterName(), "param2");
      assertEquals(suggestion.getParameterType(), "paramType2");
      {
        Element element = suggestion.getElement();
        assertEquals(element.getName(), "completion0");
        assertEquals(element.getKind(), ElementKind.METHOD);
        assertEquals(element.getFlags(), 0);
        assertEquals(element.getParameters(), "(paramType0 param0, paramType1 param1)");
      }
    }
    {
      CompletionSuggestion suggestion = suggestions.get(1);
      assertEquals(CompletionSuggestionKind.IDENTIFIER, suggestion.getKind());
      assertEquals(1000, suggestion.getRelevance());
      assertEquals(suggestion.getCompletion(), "completion1");
      assertEquals(10, suggestion.getSelectionOffset());
      assertEquals(11, suggestion.getSelectionLength());
      assertTrue(suggestion.isDeprecated());
      assertTrue(suggestion.isPotential());
      assertNull(suggestion.getDocSummary());
      assertNull(suggestion.getDocComplete());
      assertNull(suggestion.getDeclaringType());
      assertNull(suggestion.getReturnType());
      assertNull(suggestion.getParameterNames());
      assertNull(suggestion.getParameterTypes());
      assertNull(suggestion.getRequiredParameterCount());
      assertNull(suggestion.getHasNamedParameters());
      assertNull(suggestion.getParameterName());
      assertNull(suggestion.getParameterType());
      {
        Element element = suggestion.getElement();
        assertEquals(element.getName(), "completion1");
        assertEquals(element.getKind(), ElementKind.CLASS);
        assertEquals(element.getFlags(), 1);
      }
    }
  }

  public void test_edit_format() throws Exception {
    final Object[] editsArray = {null};
    final int[] selectionOffsetArray = {-1};
    final int[] selectionLengthArray = {-1};
    final RequestError[] requestErrorArray = {null};
    server.edit_format("/fileA.dart", 1, 2, -1, new FormatConsumer() {
      @Override
      public void computedFormat(List<SourceEdit> edits, int selectionOffset, int selectionLength) {
        editsArray[0] = edits;
        selectionOffsetArray[0] = selectionOffset;
        selectionLengthArray[0] = selectionLength;
      }

      @Override
      public void onError(RequestError requestError) {
        requestErrorArray[0] = requestError;
      }
    });
    List<JsonObject> requests = requestSink.getRequests();
    JsonObject expected = parseJson(//
        "{",
        "  'id': '0',",
        "  'method': 'edit.format',",
        "  'params': {",
        "    'file': '/fileA.dart',",
        "    'selectionOffset': 1,",
        "    'selectionLength': 2",
        "  }",
        "}");
    if (!requests.contains(expected)) {
      fail("Expected '" + expected + "' found '" + requests + "'");
    }

    putResponse(//
        "{",
        "  'id': '0',",
        "  'result': {",
        "    'edits': [",
        "      {",
        "        'offset': 1,",
        "        'length': 2,",
        "        'replacement': 'replacement'",
        "      }",
        "    ],",
        "    'selectionOffset': 3,",
        "    'selectionLength': 4",
        "  }",
        "}");
    responseStream.waitForEmpty();
    server.test_waitForWorkerComplete();

    // assertion on requestErrorArray
    assertNull(requestErrorArray[0]);
    // assertions on 'assists' (List<SourceEdit>)
    @SuppressWarnings("unchecked")
    List<SourceEdit> edits = (List<SourceEdit>) editsArray[0];
    assertNotNull(edits);
    assertThat(edits).hasSize(1);
    assertEquals(3, selectionOffsetArray[0]);
    assertEquals(4, selectionLengthArray[0]);
    // other assertions would would test the generated fromJson methods
  }

  public void test_edit_format_error() throws Exception {
    final Object[] editsArray = {null};
    final int[] selectionOffsetArray = {-1};
    final int[] selectionLengthArray = {-1};
    final RequestError[] requestErrorArray = {null};
    server.edit_format("/fileA.dart", 1, 2, -1, new FormatConsumer() {
      @Override
      public void computedFormat(List<SourceEdit> edits, int selectionOffset, int selectionLength) {
        editsArray[0] = edits;
        selectionOffsetArray[0] = selectionOffset;
        selectionLengthArray[0] = selectionLength;
      }

      @Override
      public void onError(RequestError requestError) {
        requestErrorArray[0] = requestError;
      }
    });
    List<JsonObject> requests = requestSink.getRequests();
    JsonObject expected = parseJson(//
        "{",
        "  'id': '0',",
        "  'method': 'edit.format',",
        "  'params': {",
        "    'file': '/fileA.dart',",
        "    'selectionOffset': 1,",
        "    'selectionLength': 2",
        "  }",
        "}");
    if (!requests.contains(expected)) {
      fail("Expected '" + expected + "' found '" + requests + "'");
    }

    putResponse(//
        "{",
        "  'id': '0',",
        "  'error': {",
        "    'code': 'CONTENT_MODIFIED',",
        "    'message': 'message0',",
        "    'stackTrace': 'stackTrace0'",
        "  }",
        "}");
    responseStream.waitForEmpty();
    server.test_waitForWorkerComplete();

    assertNull(editsArray[0]);
    assertNotNull(requestErrorArray[0]);
    RequestError requestError = requestErrorArray[0];
    assertEquals("CONTENT_MODIFIED", requestError.getCode());
    assertEquals("message0", requestError.getMessage());
    assertEquals("stackTrace0", requestError.getStackTrace());
  }

  public void test_edit_getAssists() throws Exception {
    final Object[] sourceChangesArray = {null};
    final RequestError[] requestErrorArray = {null};
    server.edit_getAssists("/fileA.dart", 1, 2, new GetAssistsConsumer() {
      @Override
      public void computedSourceChanges(List<SourceChange> sourceChanges) {
        sourceChangesArray[0] = sourceChanges;
      }

      @Override
      public void onError(RequestError requestError) {
        requestErrorArray[0] = requestError;
      }
    });
    List<JsonObject> requests = requestSink.getRequests();
    JsonObject expected = parseJson(//
        "{",
        "  'id': '0',",
        "  'method': 'edit.getAssists',",
        "  'params': {",
        "    'file': '/fileA.dart',",
        "    'offset': 1,",
        "    'length': 2",
        "  }",
        "}");
    assertTrue(requests.contains(expected));

    putResponse(//
        "{",
        "  'id': '0',",
        "  'result': {",
        "    'assists': [",
        "      {",
        "        'message': 'message1',",
        "        'edits': [",
        "          {",
        "            'file':'file1.dart',",
        "            'fileStamp': 101,",
        "            'edits': [",
        "              {",
        "                'offset': 1,",
        "                'length': 2,",
        "                'replacement': 'replacement1',",
        "                'id': 'id1'",
        "              }",
        "            ]",
        "          }",
        "        ],",
        "        'linkedEditGroups': [",
        "          {",
        "            'positions': [",
        "              {",
        "                'file': 'file2.dart',",
        "                'offset': 3",
        "              }",
        "            ],",
        "            'length': 4,",
        "            'suggestions': [",
        "              {",
        "                'value': 'value1',",
        "                'kind': 'METHOD'",
        "              }",
        "            ]",
        "          }",
        "        ],",
        "        'selection': {",
        "          'file': 'file3.dart',",
        "          'offset': 5",
        "        }",
        "      },",
        "      {",
        "        'message': 'message2',",
        "        'edits': [",
        "          {",
        "            'file':'someFile3.dart',",
        "            'fileStamp': 102,",
        "            'edits': [",
        "              {",
        "                'offset': 6,",
        "                'length': 7,",
        "                'replacement': 'replacement2'",
        "             },",
        "             {",
        "                'offset': 8,",
        "                'length': 9,",
        "                'replacement': 'replacement2'",
        "             }",
        "            ]",
        "          }",
        "        ],",
        "        'linkedEditGroups': [",
        "          {",
        "            'positions': [",
        "              {",
        "                'file': 'file4.dart',",
        "                'offset': 10",
        "              }",
        "            ],",
        "            'length': 12,",
        "            'suggestions': [",
        "              {",
        "                'value': 'value2',",
        "                'kind': 'PARAMETER'",
        "              }",
        "            ]",
        "          }",
        "        ]",
        "      }",
        "    ]",
        "  }",
        "}");
    responseStream.waitForEmpty();
    server.test_waitForWorkerComplete();

    // assertion on requestErrorArray
    assertNull(requestErrorArray[0]);
    // assertions on 'assists' (List<SourceChange>)
    @SuppressWarnings("unchecked")
    List<SourceChange> sourceChanges = (List<SourceChange>) sourceChangesArray[0];
    assertThat(sourceChanges).hasSize(2);
    // other assertions would would test the generated fromJson methods
  }

  public void test_edit_getAssists_error() throws Exception {
    final Object[] sourceChangesArray = {null};
    final RequestError[] requestErrorArray = {null};
    server.edit_getAssists("/fileA.dart", 1, 2, new GetAssistsConsumer() {
      @Override
      public void computedSourceChanges(List<SourceChange> sourceChanges) {
        sourceChangesArray[0] = sourceChanges;
      }

      @Override
      public void onError(RequestError requestError) {
        requestErrorArray[0] = requestError;
      }
    });
    List<JsonObject> requests = requestSink.getRequests();
    JsonObject expected = parseJson(//
        "{",
        "  'id': '0',",
        "  'method': 'edit.getAssists',",
        "  'params': {",
        "    'file': '/fileA.dart',",
        "    'offset': 1,",
        "    'length': 2",
        "  }",
        "}");
    assertTrue(requests.contains(expected));

    putResponse(//
        "{",
        "  'id': '0',",
        "  'method': 'edit.getFixes',",
        "  'error': {",
        "    'code': 'CONTENT_MODIFIED',",
        "    'message': 'message0',",
        "    'stackTrace': 'stackTrace0'",
        "  }",
        "}");
    responseStream.waitForEmpty();
    server.test_waitForWorkerComplete();

    assertNull(sourceChangesArray[0]);
    assertNotNull(requestErrorArray[0]);
    RequestError requestError = requestErrorArray[0];
    assertEquals("CONTENT_MODIFIED", requestError.getCode());
    assertEquals("message0", requestError.getMessage());
    assertEquals("stackTrace0", requestError.getStackTrace());
  }

  public void test_edit_getAssists_error_responseError() throws Exception {
    final Object[] sourceChangesArray = {null};
    final RequestError[] requestErrorArray = {null};
    server.edit_getAssists("/fileA.dart", 1, 2, new GetAssistsConsumer() {
      @Override
      public void computedSourceChanges(List<SourceChange> sourceChanges) {
        sourceChangesArray[0] = sourceChanges;
      }

      @Override
      public void onError(RequestError requestError) {
        requestErrorArray[0] = requestError;
      }
    });
    List<JsonObject> requests = requestSink.getRequests();
    JsonObject expected = parseJson(//
        "{",
        "  'id': '0',",
        "  'method': 'edit.getAssists',",
        "  'params': {",
        "    'file': '/fileA.dart',",
        "    'offset': 1,",
        "    'length': 2",
        "  }",
        "}");
    assertTrue(requests.contains(expected));

    putResponse(//
        "{",
        "  'id': '0',",
        "  'result': {",
        "    'assists': [",
        "      {",
        "        'message': 'message1',",
        "        'edits': [",
        "          {",
        "            'file':'file1.dart',",
        // invalid response
//        "            'fileStamp': 101,",
        "            'edits': [",
        "              {",
        "                'offset': 1,",
        "                'length': 2,",
        "                'replacement': 'replacement1',",
        "                'id': 'id1'",
        "             }",
        "            ]",
        "          }",
        "        ],",
        "        'linkedEditGroups': [",
        "          {",
        "            'positions': [",
        "              {",
        "                'file': 'file2.dart',",
        "                'offset': 3",
        "              }",
        "            ],",
        "            'length': 4,",
        "            'suggestions': [",
        "              {",
        "                'value': 'value1',",
        "                'kind': 'METHOD'",
        "              }",
        "            ]",
        "          }",
        "        ],",
        "        'selection': {",
        "          'file': 'file3.dart',",
        "          'offset': 5",
        "        }",
        "      },",
        "      {",
        "        'message': 'message2',",
        "        'edits': [",
        "          {",
        "            'file':'someFile3.dart',",
        "            'fileStamp': 102,",
        "            'edits': [",
        "              {",
        "                'offset': 6,",
        "                'length': 7,",
        "                'replacement': 'replacement2'",
        "             },",
        "             {",
        "                'offset': 8,",
        "                'length': 9,",
        "                'replacement': 'replacement2'",
        "             }",
        "            ]",
        "          }",
        "        ],",
        "        'linkedEditGroups': [",
        "          {",
        "            'positions': [",
        "              {",
        "                'file': 'file4.dart',",
        "                'offset': 10",
        "              }",
        "            ],",
        "            'length': 12,",
        "            'suggestions': [",
        "              {",
        "                'value': 'value2',",
        "                'kind': 'PARAMETER'",
        "              }",
        "            ]",
        "          }",
        "        ]",
        "      }",
        "    ]",
        "  }",
        "}");

    responseStream.waitForEmpty();
    server.test_waitForWorkerComplete();

    assertNull(sourceChangesArray[0]);
    assertNotNull(requestErrorArray[0]);
    RequestError requestError = requestErrorArray[0];
    assertEquals("INVALID_SERVER_RESPONSE", requestError.getCode());
  }

  public void test_edit_getAvailableRefactorings() throws Exception {
    final Object[] refactoringKindsArray = {null};
    final RequestError[] requestErrorArray = {null};
    server.edit_getAvailableRefactorings(
        "/fileA.dart",
        1,
        2,
        new GetAvailableRefactoringsConsumer() {
          @Override
          public void computedRefactoringKinds(List<String> refactoringKinds) {
            refactoringKindsArray[0] = refactoringKinds;
          }

          @Override
          public void onError(RequestError requestError) {
            requestErrorArray[0] = requestError;
          }
        });
    List<JsonObject> requests = requestSink.getRequests();
    JsonObject expected = parseJson(//
        "{",
        "  'id': '0',",
        "  'method': 'edit.getAvailableRefactorings',",
        "  'params': {",
        "    'file': '/fileA.dart',",
        "    'offset': 1,",
        "    'length': 2",
        "  }",
        "}");
    assertTrue(requests.contains(expected));

    putResponse(//
        "{",
        "  'id': '0',",
        "  'result': {",
        "    'kinds': ['CONVERT_GETTER_TO_METHOD','CONVERT_METHOD_TO_GETTER','EXTRACT_LOCAL_VARIABLE']",
        "  }",
        "}");
    responseStream.waitForEmpty();
    server.test_waitForWorkerComplete();

    // assertions on 'kinds' (List<RefactoringKind>)
    @SuppressWarnings("unchecked")
    List<String> refactoringKinds = (List<String>) refactoringKindsArray[0];
    assertThat(refactoringKinds).hasSize(3);
    assertThat(refactoringKinds).contains(
        "CONVERT_GETTER_TO_METHOD",
        "CONVERT_METHOD_TO_GETTER",
        "EXTRACT_LOCAL_VARIABLE");

    // request errors is null
    assertNull(requestErrorArray[0]);
  }

  public void test_edit_getAvailableRefactorings_emptyKindsList() throws Exception {
    final Object[] refactoringKindsArray = {null};
    final RequestError[] requestErrorArray = {null};
    server.edit_getAvailableRefactorings(
        "/fileA.dart",
        1,
        2,
        new GetAvailableRefactoringsConsumer() {
          @Override
          public void computedRefactoringKinds(List<String> refactoringKinds) {
            refactoringKindsArray[0] = refactoringKinds;
          }

          @Override
          public void onError(RequestError requestError) {
            requestErrorArray[0] = requestError;
          }
        });

    putResponse(//
        "{",
        "  'id': '0',",
        "  'result': {",
        "    'kinds': []",
        "  }",
        "}");
    responseStream.waitForEmpty();
    server.test_waitForWorkerComplete();

    // assertions on 'kinds' (List<RefactoringKind>)
    @SuppressWarnings("unchecked")
    List<String> refactoringKinds = (List<String>) refactoringKindsArray[0];
    assertThat(refactoringKinds).hasSize(0);

    // request errors is null
    assertNull(requestErrorArray[0]);
  }

  public void test_edit_getAvailableRefactorings_error() throws Exception {
    final Object[] refactoringKindsArray = {null};
    final RequestError[] requestErrorArray = {null};
    server.edit_getAvailableRefactorings(
        "/fileA.dart",
        1,
        2,
        new GetAvailableRefactoringsConsumer() {
          @Override
          public void computedRefactoringKinds(List<String> refactoringKinds) {
            refactoringKindsArray[0] = refactoringKinds;
          }

          @Override
          public void onError(RequestError requestError) {
            requestErrorArray[0] = requestError;
          }
        });

    putResponse(//
        "{",
        "  'id': '0',",
        "  'method': 'edit.getFixes',",
        "  'error': {",
        "    'code': 'CONTENT_MODIFIED',",
        "    'message': 'message0',",
        "    'stackTrace': 'stackTrace0'",
        "  }",
        "}");
    responseStream.waitForEmpty();
    server.test_waitForWorkerComplete();

    assertNull(refactoringKindsArray[0]);

    assertNotNull(requestErrorArray[0]);
    RequestError requestError = requestErrorArray[0];
    assertEquals("CONTENT_MODIFIED", requestError.getCode());
    assertEquals("message0", requestError.getMessage());
    assertEquals("stackTrace0", requestError.getStackTrace());
  }

  public void test_edit_getFixes() throws Exception {
    final Object[] errorFixesArray = {null};
    final RequestError[] requestErrorArray = {null};
    server.edit_getFixes("/fileA.dart", 1, new GetFixesConsumer() {
      @Override
      public void computedFixes(List<AnalysisErrorFixes> e) {
        errorFixesArray[0] = e;
      }

      @Override
      public void onError(RequestError requestError) {
        requestErrorArray[0] = requestError;
      }
    });
    List<JsonObject> requests = requestSink.getRequests();
    JsonObject expected = parseJson(//
        "{",
        "  'id': '0',",
        "  'method': 'edit.getFixes',",
        "  'params': {",
        "    'file': '/fileA.dart',",
        "    'offset': 1",
        "  }",
        "}");
    assertTrue(requests.contains(expected));

    putResponse(//
        "{",
        "  'id': '0',",
        "  'result': {",
        "    'fixes': [",
        "      {",
        "        'error': {",
        "          'severity': 'ERROR',",
        "          'type': 'SYNTACTIC_ERROR',",
        "          'location': {",
        "            'file': '/fileA.dart',",
        "            'offset': 1,",
        "            'length': 2,",
        "            'startLine': 3,",
        "            'startColumn': 4",
        "          },",
        "          'message': 'message A',",
        "          'correction': 'correction A'",
        "        },",
        "        'fixes': [",
        "          {",
        "            'message': 'message1',",
        "            'edits': [",
        "              {",
        "                'file':'file1.dart',",
        "                'fileStamp': 101,",
        "                'edits': [",
        "                  {",
        "                    'offset': 1,",
        "                    'length': 2,",
        "                    'replacement': 'replacement1',",
        "                    'id': 'id1'",
        "                  }",
        "                ]",
        "              }",
        "            ],",
        "            'linkedEditGroups': [",
        "              {",
        "                'positions': [",
        "                  {",
        "                    'file': 'file2.dart',",
        "                    'offset': 3",
        "                  }",
        "                ],",
        "                'length': 4,",
        "                'suggestions': [",
        "                  {",
        "                    'value': 'value1',",
        "                    'kind': 'METHOD'",
        "                  }",
        "                ]",
        "              }",
        "            ],",
        "            'selection': {",
        "              'file': 'file3.dart',",
        "              'offset': 5",
        "            }",
        "          }",
        "        ]",
        "      }",
        "    ]",
        "  }",
        "}");
    responseStream.waitForEmpty();
    server.test_waitForWorkerComplete();

    // assertions on 'fixes' (List<ErrorFixes>)
    @SuppressWarnings("unchecked")
    List<AnalysisErrorFixes> errorFixes = (List<AnalysisErrorFixes>) errorFixesArray[0];
    assertThat(errorFixes).hasSize(1);
    assertNull(requestErrorArray[0]);
    // other assertions would would test the generated fromJson methods
  }

  public void test_edit_getFixes_error() throws Exception {
    final Object[] errorFixesArray = {null};
    final RequestError[] requestErrorArray = {null};
    server.edit_getFixes("/fileA.dart", 1, new GetFixesConsumer() {
      @Override
      public void computedFixes(List<AnalysisErrorFixes> e) {
        errorFixesArray[0] = e;
      }

      @Override
      public void onError(RequestError requestError) {
        requestErrorArray[0] = requestError;
      }
    });
    List<JsonObject> requests = requestSink.getRequests();
    JsonObject expected = parseJson(//
        "{",
        "  'id': '0',",
        "  'method': 'edit.getFixes',",
        "  'params': {",
        "    'file': '/fileA.dart',",
        "    'offset': 1",
        "  }",
        "}");
    assertTrue(requests.contains(expected));

    putResponse(//
        "{",
        "  'id': '0',",
        "  'method': 'edit.getFixes',",
        "  'error': {",
        "    'code': 'CONTENT_MODIFIED',",
        "    'message': 'message0',",
        "    'stackTrace': 'stackTrace0'",
        "  }",
        "}");
    responseStream.waitForEmpty();
    server.test_waitForWorkerComplete();

    assertNull(errorFixesArray[0]);
    assertNotNull(requestErrorArray[0]);
    RequestError requestError = requestErrorArray[0];
    assertEquals("CONTENT_MODIFIED", requestError.getCode());
    assertEquals("message0", requestError.getMessage());
    assertEquals("stackTrace0", requestError.getStackTrace());
  }

  public void test_edit_getFixes_error_responseError() throws Exception {
    final Object[] errorFixesArray = {null};
    final RequestError[] requestErrorArray = {null};
    server.edit_getFixes("/fileA.dart", 1, new GetFixesConsumer() {
      @Override
      public void computedFixes(List<AnalysisErrorFixes> e) {
        errorFixesArray[0] = e;
      }

      @Override
      public void onError(RequestError requestError) {
        requestErrorArray[0] = requestError;
      }
    });
    List<JsonObject> requests = requestSink.getRequests();
    JsonObject expected = parseJson(//
        "{",
        "  'id': '0',",
        "  'method': 'edit.getFixes',",
        "  'params': {",
        "    'file': '/fileA.dart',",
        "    'offset': 1",
        "  }",
        "}");
    assertTrue(requests.contains(expected));

    putResponse(//
        "{",
        "  'id': '0',",
        "  'result': {",
        "    'fixes': [",
        "      {",
        "        'error': {",
        "          'severity': 'ERROR',",
        // bad response, no type:
//        "          'type': 'SYNTACTIC_ERROR',",
        "          'location': {",
        "            'file': '/fileA.dart',",
        "            'offset': 1,",
        "            'length': 2,",
        "            'startLine': 3,",
        "            'startColumn': 4",
        "          },",
        "          'message': 'message A',",
        "          'correction': 'correction A'",
        "        },",
        "        'fixes': [",
        "          {",
        "            'message': 'message1',",
        "            'edits': [",
        "              {",
        "                'file':'file1.dart',",
        "                'fileStamp': 101,",
        "                'edits': [",
        "                  {",
        "                    'offset': 1,",
        "                    'length': 2,",
        "                    'replacement': 'replacement1',",
        "                    'id': 'id1'",
        "                  }",
        "                ]",
        "              }",
        "            ],",
        "            'linkedEditGroups': [",
        "              {",
        "                'positions': [",
        "                  {",
        "                    'file': 'file2.dart',",
        "                    'offset': 3",
        "                  }",
        "                ],",
        "                'length': 4,",
        "                'suggestions': [",
        "                  {",
        "                    'value': 'value1',",
        "                    'kind': 'METHOD'",
        "                  }",
        "                ]",
        "              }",
        "            ],",
        "            'selection': {",
        "              'file': 'file3.dart',",
        "              'offset': 5",
        "            }",
        "          }",
        "        ]",
        "      }",
        "    ]",
        "  }",
        "}");
    responseStream.waitForEmpty();
    server.test_waitForWorkerComplete();

    assertNull(errorFixesArray[0]);
    assertNotNull(requestErrorArray[0]);
    RequestError requestError = requestErrorArray[0];
    assertEquals("INVALID_SERVER_RESPONSE", requestError.getCode());
  }

  public void test_edit_getRefactoring_request_options_extractLocalVariable() throws Exception {
    final RequestError[] requestErrorArray = {null};
    RefactoringOptions options = new ExtractLocalVariableOptions("name1", true);
    server.edit_getRefactoring(
        RefactoringKind.EXTRACT_LOCAL_VARIABLE,
        "file1.dart",
        1,
        2,
        false,
        options,
        new GetRefactoringConsumer() {
          @Override
          public void computedRefactorings(List<RefactoringProblem> initialProblems,
              List<RefactoringProblem> optionsProblems, List<RefactoringProblem> finalProblems,
              RefactoringFeedback feedback, SourceChange change, List<String> potentialEdits) {
          }

          @Override
          public void onError(RequestError requestError) {
            requestErrorArray[0] = requestError;
          }
        });
    List<JsonObject> requests = requestSink.getRequests();
    JsonObject expected = parseJson(//
        "{",
        "  'id': '0',",
        "  'method': 'edit.getRefactoring',",
        "  'params': {",
        "    'kind': 'EXTRACT_LOCAL_VARIABLE',",
        "    'file': 'file1.dart',",
        "    'offset': 1,",
        "    'length': 2,",
        "    'validateOnly': false,",
        "    'options': {",
        "      'name': 'name1',",
        "      'extractAll': true",
        "    }",
        "  }",
        "}");
    assertTrue(requests.contains(expected));
    assertNull(requestErrorArray[0]);
  }

  public void test_edit_getRefactoring_request_options_extractMethod() throws Exception {
    final RequestError[] requestErrorArray = {null};
    RefactoringMethodParameter p1 = new RefactoringMethodParameter(
        "id1",
        RefactoringMethodParameterKind.REQUIRED,
        "type1",
        "name1",
        "parameters1");
    RefactoringMethodParameter p2 = new RefactoringMethodParameter(
        "id2",
        RefactoringMethodParameterKind.POSITIONAL,
        "type2",
        "name2",
        null);
    RefactoringOptions options = new ExtractMethodOptions(
        "returnType1",
        true,
        "name1",
        Lists.newArrayList(p1, p2),
        true);
    server.edit_getRefactoring(
        RefactoringKind.EXTRACT_METHOD,
        "file1.dart",
        1,
        2,
        false,
        options,
        new GetRefactoringConsumer() {
          @Override
          public void computedRefactorings(List<RefactoringProblem> initialProblems,
              List<RefactoringProblem> optionsProblems, List<RefactoringProblem> finalProblems,
              RefactoringFeedback feedback, SourceChange change, List<String> potentialEdits) {
          }

          @Override
          public void onError(RequestError requestError) {
            requestErrorArray[0] = requestError;
          }
        });
    List<JsonObject> requests = requestSink.getRequests();
    JsonObject expected = parseJson(//
        "{",
        "  'id': '0',",
        "  'method': 'edit.getRefactoring',",
        "  'params': {",
        "    'kind': 'EXTRACT_METHOD',",
        "    'file': 'file1.dart',",
        "    'offset': 1,",
        "    'length': 2,",
        "    'validateOnly': false,",
        "    'options': {",
        "      'returnType': 'returnType1',",
        "      'createGetter': true,",
        "      'name': 'name1',",
        "      'parameters': [",
        "        {",
        "          'id': 'id1',",
        "          'kind': 'REQUIRED',",
        "          'type': 'type1',",
        "          'name': 'name1',",
        "          'parameters': 'parameters1'",
        "        },",
        "        {",
        "          'id': 'id2',",
        "          'kind': 'POSITIONAL',",
        "          'type': 'type2',",
        "          'name': 'name2'",
        "        }",
        "      ],",
        "      'extractAll': true",
        "    }",
        "  }",
        "}");
    assertTrue(requests.contains(expected));
    assertNull(requestErrorArray[0]);
  }

  public void test_edit_getRefactoring_request_options_extractMethod_noParameters()
      throws Exception {
    final RequestError[] requestErrorArray = {null};
    RefactoringOptions options = new ExtractMethodOptions(
        "returnType1",
        true,
        "name1",
        Lists.<RefactoringMethodParameter> newArrayList(),
        true);
    server.edit_getRefactoring(
        RefactoringKind.EXTRACT_METHOD,
        "file1.dart",
        1,
        2,
        false,
        options,
        new GetRefactoringConsumer() {
          @Override
          public void computedRefactorings(List<RefactoringProblem> initialProblems,
              List<RefactoringProblem> optionsProblems, List<RefactoringProblem> finalProblems,
              RefactoringFeedback feedback, SourceChange change, List<String> potentialEdits) {
          }

          @Override
          public void onError(RequestError requestError) {
            requestErrorArray[0] = requestError;
          }
        });
    List<JsonObject> requests = requestSink.getRequests();
    JsonObject expected = parseJson(//
        "{",
        "  'id': '0',",
        "  'method': 'edit.getRefactoring',",
        "  'params': {",
        "    'kind': 'EXTRACT_METHOD',",
        "    'file': 'file1.dart',",
        "    'offset': 1,",
        "    'length': 2,",
        "    'validateOnly': false,",
        "    'options': {",
        "      'returnType': 'returnType1',",
        "      'createGetter': true,",
        "      'name': 'name1',",
        "      'parameters': [],",
        "      'extractAll': true",
        "    }",
        "  }",
        "}");
    assertTrue(requests.contains(expected));
    assertNull(requestErrorArray[0]);
  }

  public void test_edit_getRefactoring_request_options_inlineMethod() throws Exception {
    final RequestError[] requestErrorArray = {null};
    RefactoringOptions options = new InlineMethodOptions(true, true);
    server.edit_getRefactoring(
        RefactoringKind.INLINE_METHOD,
        "file1.dart",
        1,
        2,
        false,
        options,
        new GetRefactoringConsumer() {
          @Override
          public void computedRefactorings(List<RefactoringProblem> initialProblems,
              List<RefactoringProblem> optionsProblems, List<RefactoringProblem> finalProblems,
              RefactoringFeedback feedback, SourceChange change, List<String> potentialEdits) {
          }

          @Override
          public void onError(RequestError requestError) {
            requestErrorArray[0] = requestError;
          }
        });
    List<JsonObject> requests = requestSink.getRequests();
    JsonObject expected = parseJson(//
        "{",
        "  'id': '0',",
        "  'method': 'edit.getRefactoring',",
        "  'params': {",
        "    'kind': 'INLINE_METHOD',",
        "    'file': 'file1.dart',",
        "    'offset': 1,",
        "    'length': 2,",
        "    'validateOnly': false,",
        "    'options': {",
        "      'deleteSource': true,",
        "      'inlineAll': true",
        "    }",
        "  }",
        "}");
    assertTrue(requests.contains(expected));
    assertNull(requestErrorArray[0]);
  }

  public void test_edit_getRefactoring_request_options_rename() throws Exception {
    final RequestError[] requestErrorArray = {null};
    RefactoringOptions options = new RenameOptions("newName1");
    server.edit_getRefactoring(
        RefactoringKind.RENAME,
        "file1.dart",
        1,
        2,
        false,
        options,
        new GetRefactoringConsumer() {
          @Override
          public void computedRefactorings(List<RefactoringProblem> initialProblems,
              List<RefactoringProblem> optionsProblems, List<RefactoringProblem> finalProblems,
              RefactoringFeedback feedback, SourceChange change, List<String> potentialEdits) {
          }

          @Override
          public void onError(RequestError requestError) {
            requestErrorArray[0] = requestError;
          }
        });
    List<JsonObject> requests = requestSink.getRequests();
    JsonObject expected = parseJson(//
        "{",
        "  'id': '0',",
        "  'method': 'edit.getRefactoring',",
        "  'params': {",
        "    'kind': 'RENAME',",
        "    'file': 'file1.dart',",
        "    'offset': 1,",
        "    'length': 2,",
        "    'validateOnly': false,",
        "    'options': {",
        "      'newName': 'newName1'",
        "    }",
        "  }",
        "}");
    assertTrue(requests.contains(expected));
    assertNull(requestErrorArray[0]);
  }

  public void test_edit_getRefactoring_response() throws Exception {
    final RequestError[] requestErrorArray = {null};
    final Object[] initialProblemsArray = {null};
    final Object[] optionsProblemsArray = {null};
    final Object[] finalProblemsArray = {null};
    final RefactoringFeedback[] feedbackArray = {null};
    final SourceChange[] changeArray = {null};
    final Object[] potentialEditsArray = {null};
    RefactoringOptions options = null;
    server.edit_getRefactoring(
        RefactoringKind.RENAME,
        "file1.dart",
        1,
        2,
        false,
        options,
        new GetRefactoringConsumer() {
          @Override
          public void computedRefactorings(List<RefactoringProblem> initialProblems,
              List<RefactoringProblem> optionsProblems, List<RefactoringProblem> finalProblems,
              RefactoringFeedback feedback, SourceChange change, List<String> potentialEdits) {
            initialProblemsArray[0] = initialProblems;
            optionsProblemsArray[0] = optionsProblems;
            finalProblemsArray[0] = finalProblems;
            feedbackArray[0] = feedback;
            changeArray[0] = change;
            potentialEditsArray[0] = potentialEdits;
          }

          @Override
          public void onError(RequestError requestError) {
            requestErrorArray[0] = requestError;
          }
        });

    putResponse(//
        "{",
        "  'id': '0',",
        "  'result': {",
        "    'initialProblems': [",
        "      {",
        "        'severity': 'INFO',",
        "        'message': 'message1'",
        "      }",
        "    ],",
        "    'optionsProblems': [",
        "      {",
        "        'severity': 'WARNING',",
        "        'message': 'message2'",
        "      }",
        "    ],",
        "    'finalProblems': [",
        "      {",
        "        'severity': 'FATAL',",
        "        'message': 'message3'",
        "      }",
        "    ],",
        "    'feedback': {",
        "      offset: 1,",
        "      length: 2,",
        "      elementKindName: 'class',",
        "      oldName: 'oldName'",
        "    },",
        "    'change': " + getSourceChangeJson() + ",",
        "    'potentialEdits': ['one']",
        "  }",
        "}");
    responseStream.waitForEmpty();
    server.test_waitForWorkerComplete();

    // assertions on 'problems' (List<ErrorFixes>)
    {
      @SuppressWarnings("unchecked")
      List<RefactoringProblem> problem = (List<RefactoringProblem>) initialProblemsArray[0];
      assertThat(problem).hasSize(1);
      assertEquals(problem.get(0).getSeverity(), RefactoringProblemSeverity.INFO);
      assertEquals(problem.get(0).getMessage(), "message1");
    }
    {
      @SuppressWarnings("unchecked")
      List<RefactoringProblem> problem = (List<RefactoringProblem>) optionsProblemsArray[0];
      assertThat(problem).hasSize(1);
      assertEquals(problem.get(0).getSeverity(), RefactoringProblemSeverity.WARNING);
      assertEquals(problem.get(0).getMessage(), "message2");
    }
    {
      @SuppressWarnings("unchecked")
      List<RefactoringProblem> problem = (List<RefactoringProblem>) finalProblemsArray[0];
      assertThat(problem).hasSize(1);
      assertEquals(problem.get(0).getSeverity(), RefactoringProblemSeverity.FATAL);
      assertEquals(problem.get(0).getMessage(), "message3");
    }

    // assertions on 'feedback'
    RenameFeedback feedback = (RenameFeedback) feedbackArray[0];
    assertEquals(1, feedback.getOffset());
    assertEquals(2, feedback.getLength());
    assertEquals("class", feedback.getElementKindName());
    assertEquals("oldName", feedback.getOldName());

    // assertions on 'potentialEdits' (List<String>)
    @SuppressWarnings("unchecked")
    List<String> potentialEdits = (List<String>) potentialEditsArray[0];
    assertThat(potentialEdits).hasSize(1);
    assertEquals("one", potentialEdits.get(0));

    assertNull(requestErrorArray[0]);

    // other assertions would would test the generated fromJson methods
  }

  public void test_edit_getRefactoring_response_error() throws Exception {
    final RequestError[] requestErrorArray = {null};
    final Object[] initialProblemsArray = {null};
    final Object[] optionsProblemsArray = {null};
    final Object[] finalProblemsArray = {null};
    final RefactoringFeedback[] feedbackArray = {null};
    final SourceChange[] changeArray = {null};
    final Object[] potentialEditsArray = {null};
    RefactoringOptions options = null;
    server.edit_getRefactoring(
        RefactoringKind.RENAME,
        "file1.dart",
        1,
        2,
        false,
        options,
        new GetRefactoringConsumer() {
          @Override
          public void computedRefactorings(List<RefactoringProblem> initialProblems,
              List<RefactoringProblem> optionsProblems, List<RefactoringProblem> finalProblems,
              RefactoringFeedback feedback, SourceChange change, List<String> potentialEdits) {
            initialProblemsArray[0] = initialProblems;
            optionsProblemsArray[0] = optionsProblems;
            finalProblemsArray[0] = finalProblems;
            feedbackArray[0] = feedback;
            changeArray[0] = change;
            potentialEditsArray[0] = potentialEdits;
          }

          @Override
          public void onError(RequestError requestError) {
            requestErrorArray[0] = requestError;
          }
        });

    putResponse(//
        "{",
        "  'id': '0',",
        "  'method': 'edit.getFixes',",
        "  'error': {",
        "    'code': 'CONTENT_MODIFIED',",
        "    'message': 'message0',",
        "    'stackTrace': 'stackTrace0'",
        "  }",
        "}");
    responseStream.waitForEmpty();
    server.test_waitForWorkerComplete();

    assertNull(initialProblemsArray[0]);
    assertNull(optionsProblemsArray[0]);
    assertNull(finalProblemsArray[0]);
    assertNull(feedbackArray[0]);
    assertNull(changeArray[0]);
    assertNull(potentialEditsArray[0]);

    assertNotNull(requestErrorArray[0]);
    RequestError requestError = requestErrorArray[0];
    assertEquals("CONTENT_MODIFIED", requestError.getCode());
    assertEquals("message0", requestError.getMessage());
    assertEquals("stackTrace0", requestError.getStackTrace());
  }

  public void test_edit_getRefactoring_response_feedback_extractLocalVariable() throws Exception {
    final RequestError[] requestErrorArray = {null};
    final RefactoringFeedback[] feedbackArray = {null};
    RefactoringOptions options = null;
    server.edit_getRefactoring(
        RefactoringKind.EXTRACT_LOCAL_VARIABLE,
        "file1.dart",
        1,
        2,
        false,
        options,
        new GetRefactoringConsumer() {
          @Override
          public void computedRefactorings(List<RefactoringProblem> initialProblems,
              List<RefactoringProblem> optionsProblems, List<RefactoringProblem> finalProblems,
              RefactoringFeedback feedback, SourceChange change, List<String> potentialEdits) {
            feedbackArray[0] = feedback;
          }

          @Override
          public void onError(RequestError requestError) {
            requestErrorArray[0] = requestError;
          }
        });

    putResponse(//
        "{",
        "  'id': '0',",
        "  'result': {",
        "    'initialProblems': [],",
        "    'optionsProblems': [],",
        "    'finalProblems': [],",
        "    'feedback': {",
        "      'names': ['one', 'two'],",
        "      'offsets': [1, 2],",
        "      'lengths': [3, 4, 5]",
        "    },",
        "    'change': " + getSourceChangeJson() + ",",
        "    'potentialEdits': ['one']",
        "  }",
        "}");
    responseStream.waitForEmpty();
    server.test_waitForWorkerComplete();

    // assertions on 'feedback'
    ExtractLocalVariableFeedback feedback = (ExtractLocalVariableFeedback) feedbackArray[0];
    assertEquals(feedback.getNames(), Lists.newArrayList("one", "two"));
    assertThat(feedback.getOffsets()).hasSize(2).contains(1, 2);
    assertThat(feedback.getLengths()).hasSize(3).contains(3, 4, 5);
    assertNull(requestErrorArray[0]);
  }

  public void test_edit_getRefactoring_response_feedback_extractMethod() throws Exception {
    final RequestError[] requestErrorArray = {null};
    final RefactoringFeedback[] feedbackArray = {null};
    RefactoringOptions options = null;
    server.edit_getRefactoring(
        RefactoringKind.EXTRACT_METHOD,
        "file1.dart",
        1,
        2,
        false,
        options,
        new GetRefactoringConsumer() {
          @Override
          public void computedRefactorings(List<RefactoringProblem> initialProblems,
              List<RefactoringProblem> optionsProblems, List<RefactoringProblem> finalProblems,
              RefactoringFeedback feedback, SourceChange change, List<String> potentialEdits) {
            feedbackArray[0] = feedback;
          }

          @Override
          public void onError(RequestError requestError) {
            requestErrorArray[0] = requestError;
          }
        });

    putResponse(//
        "{",
        "  'id': '0',",
        "  'result': {",
        "    'initialProblems': [],",
        "    'optionsProblems': [],",
        "    'finalProblems': [],",
        "    'feedback': {",
        "      'offset': 1,",
        "      'length': 2,",
        "      'returnType': 'returnType1',",
        "      'names': ['one', 'two'],",
        "      'canCreateGetter': true,",
        "      'parameters': [],",
        "      'offsets': [3, 4, 5],",
        "      'lengths': [6, 7, 8, 9]",
        "    },",
        "    'change': " + getSourceChangeJson() + ",",
        "    'potentialEdits': ['one']",
        "  }",
        "}");
    responseStream.waitForEmpty();
    server.test_waitForWorkerComplete();

    // assertions on 'feedback'
    ExtractMethodFeedback feedback = (ExtractMethodFeedback) feedbackArray[0];
    assertEquals(1, feedback.getOffset());
    assertEquals(2, feedback.getLength());
    assertEquals("returnType1", feedback.getReturnType());
    assertEquals(Lists.newArrayList("one", "two"), feedback.getNames());
    assertEquals(true, feedback.canCreateGetter());
    assertThat(feedback.getParameters()).hasSize(0);
    assertThat(feedback.getOffsets()).hasSize(3).contains(3, 4, 5);
    assertThat(feedback.getLengths()).hasSize(4).contains(6, 7, 8, 9);
    assertNull(requestErrorArray[0]);
  }

  public void test_edit_getRefactoring_response_feedback_inlineLocalVariable() throws Exception {
    final RequestError[] requestErrorArray = {null};
    final RefactoringFeedback[] feedbackArray = {null};
    RefactoringOptions options = null;
    server.edit_getRefactoring(
        RefactoringKind.INLINE_LOCAL_VARIABLE,
        "file1.dart",
        1,
        2,
        false,
        options,
        new GetRefactoringConsumer() {
          @Override
          public void computedRefactorings(List<RefactoringProblem> initialProblems,
              List<RefactoringProblem> optionsProblems, List<RefactoringProblem> finalProblems,
              RefactoringFeedback feedback, SourceChange change, List<String> potentialEdits) {
            feedbackArray[0] = feedback;
          }

          @Override
          public void onError(RequestError requestError) {
            requestErrorArray[0] = requestError;
          }
        });

    putResponse(//
        "{",
        "  'id': '0',",
        "  'result': {",
        "    'initialProblems': [],",
        "    'optionsProblems': [],",
        "    'finalProblems': [],",
        "    'feedback': {",
        "      'name': 'myVar',",
        "      'occurrences': 3",
        "    },",
        "    'change': " + getSourceChangeJson() + ",",
        "    'potentialEdits': ['one']",
        "  }",
        "}");
    responseStream.waitForEmpty();
    server.test_waitForWorkerComplete();

    // assertions on 'feedback'
    InlineLocalVariableFeedback feedback = (InlineLocalVariableFeedback) feedbackArray[0];
    assertEquals("myVar", feedback.getName());
    assertEquals(3, feedback.getOccurrences());
    assertNull(requestErrorArray[0]);
  }

  public void test_edit_getRefactoring_response_feedback_inlineMethod() throws Exception {
    final RequestError[] requestErrorArray = {null};
    final RefactoringFeedback[] feedbackArray = {null};
    RefactoringOptions options = null;
    server.edit_getRefactoring(
        RefactoringKind.INLINE_METHOD,
        "file1.dart",
        1,
        2,
        false,
        options,
        new GetRefactoringConsumer() {
          @Override
          public void computedRefactorings(List<RefactoringProblem> initialProblems,
              List<RefactoringProblem> optionsProblems, List<RefactoringProblem> finalProblems,
              RefactoringFeedback feedback, SourceChange change, List<String> potentialEdits) {
            feedbackArray[0] = feedback;
          }

          @Override
          public void onError(RequestError requestError) {
            requestErrorArray[0] = requestError;
          }
        });

    putResponse(//
        "{",
        "  'id': '0',",
        "  'result': {",
        "    'initialProblems': [],",
        "    'optionsProblems': [],",
        "    'finalProblems': [],",
        "    'feedback': {",
        "      'className': 'myClassName',",
        "      'methodName': 'myMethodName',",
        "      'isDeclaration': true",
        "    },",
        "    'change': " + getSourceChangeJson() + ",",
        "    'potentialEdits': ['one']",
        "  }",
        "}");
    responseStream.waitForEmpty();
    server.test_waitForWorkerComplete();

    // assertions on 'feedback'
    InlineMethodFeedback feedback = (InlineMethodFeedback) feedbackArray[0];
    assertEquals("myClassName", feedback.getClassName());
    assertEquals("myMethodName", feedback.getMethodName());
    assertEquals(true, feedback.isDeclaration());
    assertNull(requestErrorArray[0]);
  }

  public void test_edit_getRefactoring_response_feedback_rename() throws Exception {
    final RequestError[] requestErrorArray = {null};
    final Object[] feedbackArray = {null};
    RefactoringOptions options = null;
    server.edit_getRefactoring(
        RefactoringKind.RENAME,
        "file1.dart",
        1,
        2,
        false,
        options,
        new GetRefactoringConsumer() {
          @Override
          public void computedRefactorings(List<RefactoringProblem> initialProblems,
              List<RefactoringProblem> optionsProblems, List<RefactoringProblem> finalProblems,
              RefactoringFeedback feedback, SourceChange change, List<String> potentialEdits) {
            feedbackArray[0] = feedback;
          }

          @Override
          public void onError(RequestError requestError) {
            requestErrorArray[0] = requestError;
          }
        });

    putResponse(//
        "{",
        "  'id': '0',",
        "  'result': {",
        "    'initialProblems': [],",
        "    'optionsProblems': [],",
        "    'finalProblems': [],",
        "    'feedback': {",
        "      offset: 1,",
        "      length: 2,",
        "      elementKindName: 'class',",
        "      oldName: 'oldName'",
        "    },",
        "    'change': " + getSourceChangeJson() + ",",
        "    'potentialEdits': ['one']",
        "  }",
        "}");
    responseStream.waitForEmpty();
    server.test_waitForWorkerComplete();

    // assertions on 'feedback'
    RenameFeedback feedback = (RenameFeedback) feedbackArray[0];
    assertEquals(1, feedback.getOffset());
    assertEquals(2, feedback.getLength());
    assertEquals("class", feedback.getElementKindName());
    assertEquals("oldName", feedback.getOldName());
    assertNull(requestErrorArray[0]);
  }

  public void test_edit_organizeDirectives() throws Exception {
    final SourceFileEdit[] fileEditArray = {null};
    server.edit_organizeDirectives("/file.dart", new OrganizeDirectivesConsumer() {
      @Override
      public void computedEdit(SourceFileEdit fileEdit) {
        fileEditArray[0] = fileEdit;
      }

      @Override
      public void onError(RequestError requestError) {
      }
    });
    List<JsonObject> requests = requestSink.getRequests();
    JsonObject expected = parseJson(//
        "{",
        "  'id': '0',",
        "  'method': 'edit.organizeDirectives',",
        "  'params': {",
        "    'file': '/file.dart'",
        "  }",
        "}");
    assertTrue(requests.contains(expected));

    putResponse(//
        "{",
        "  'id': '0',",
        "  'result': {",
        "    'edit': {",
        "      'file':'file1.dart',",
        "      'fileStamp': 101,",
        "      'edits': [",
        "        {",
        "          'offset': 1,",
        "          'length': 2,",
        "          'replacement': 'replacement1'",
        "        },",
        "        {",
        "          'offset': 3,",
        "          'length': 4,",
        "          'replacement': 'replacement2'",
        "        }",
        "      ]",
        "    }",
        "  }",
        "}");
    responseStream.waitForEmpty();
    server.test_waitForWorkerComplete();

    // assertions on 'edit'
    SourceFileEdit fileEdit = fileEditArray[0];
    List<SourceEdit> edits = fileEdit.getEdits();
    assertThat(edits).hasSize(2);
    {
      SourceEdit edit = edits.get(0);
      assertEquals(1, edit.getOffset());
      assertEquals(2, edit.getLength());
      assertEquals("replacement1", edit.getReplacement());
    }
    {
      SourceEdit edit = edits.get(1);
      assertEquals(3, edit.getOffset());
      assertEquals(4, edit.getLength());
      assertEquals("replacement2", edit.getReplacement());
    }
  }

  public void test_edit_organizeDirectives_error() throws Exception {
    final SourceFileEdit[] fileEditArray = {null};
    final RequestError[] requestErrorArray = {null};
    server.edit_organizeDirectives("/file.dart", new OrganizeDirectivesConsumer() {
      @Override
      public void computedEdit(SourceFileEdit fileEdit) {
        fileEditArray[0] = fileEdit;
      }

      @Override
      public void onError(RequestError requestError) {
        requestErrorArray[0] = requestError;
      }
    });
    List<JsonObject> requests = requestSink.getRequests();
    JsonObject expected = parseJson(//
        "{",
        "  'id': '0',",
        "  'method': 'edit.organizeDirectives',",
        "  'params': {",
        "    'file': '/file.dart'",
        "  }",
        "}");
    assertTrue(requests.contains(expected));

    putResponse(//
        "{",
        "  'id': '0',",
        "  'error': {",
        "    'code': 'ORGANIZE_DIRECTIVES_ERROR',",
        "    'message': 'File has 1 scan/parse errors.'",
        "  }",
        "}");
    responseStream.waitForEmpty();
    server.test_waitForWorkerComplete();

    assertNull(fileEditArray[0]);
    assertNotNull(requestErrorArray[0]);
    RequestError requestError = requestErrorArray[0];
    assertEquals("ORGANIZE_DIRECTIVES_ERROR", requestError.getCode());
    assertEquals("File has 1 scan/parse errors.", requestError.getMessage());
  }

  public void test_edit_sortMembers() throws Exception {
    final SourceFileEdit[] fileEditArray = {null};
    server.edit_sortMembers("/file.dart", new SortMembersConsumer() {
      @Override
      public void computedEdit(SourceFileEdit fileEdit) {
        fileEditArray[0] = fileEdit;
      }

      @Override
      public void onError(RequestError requestError) {
      }
    });
    List<JsonObject> requests = requestSink.getRequests();
    JsonObject expected = parseJson(//
        "{",
        "  'id': '0',",
        "  'method': 'edit.sortMembers',",
        "  'params': {",
        "    'file': '/file.dart'",
        "  }",
        "}");
    assertTrue(requests.contains(expected));

    putResponse(//
        "{",
        "  'id': '0',",
        "  'result': {",
        "    'edit': {",
        "      'file':'file1.dart',",
        "      'fileStamp': 101,",
        "      'edits': [",
        "        {",
        "          'offset': 1,",
        "          'length': 2,",
        "          'replacement': 'replacement1'",
        "        },",
        "        {",
        "          'offset': 3,",
        "          'length': 4,",
        "          'replacement': 'replacement2'",
        "        }",
        "      ]",
        "    }",
        "  }",
        "}");
    responseStream.waitForEmpty();
    server.test_waitForWorkerComplete();

    // assertions on 'edit'
    SourceFileEdit fileEdit = fileEditArray[0];
    List<SourceEdit> edits = fileEdit.getEdits();
    assertThat(edits).hasSize(2);
    {
      SourceEdit edit = edits.get(0);
      assertEquals(1, edit.getOffset());
      assertEquals(2, edit.getLength());
      assertEquals("replacement1", edit.getReplacement());
    }
    {
      SourceEdit edit = edits.get(1);
      assertEquals(3, edit.getOffset());
      assertEquals(4, edit.getLength());
      assertEquals("replacement2", edit.getReplacement());
    }
  }

  public void test_edit_sortMembers_error() throws Exception {
    final SourceFileEdit[] fileEditArray = {null};
    final RequestError[] requestErrorArray = {null};
    server.edit_sortMembers("/file.dart", new SortMembersConsumer() {
      @Override
      public void computedEdit(SourceFileEdit fileEdit) {
        fileEditArray[0] = fileEdit;
      }

      @Override
      public void onError(RequestError requestError) {
        requestErrorArray[0] = requestError;
      }
    });
    List<JsonObject> requests = requestSink.getRequests();
    JsonObject expected = parseJson(//
        "{",
        "  'id': '0',",
        "  'method': 'edit.sortMembers',",
        "  'params': {",
        "    'file': '/file.dart'",
        "  }",
        "}");
    assertTrue(requests.contains(expected));

    putResponse(//
        "{",
        "  'id': '0',",
        "  'method': 'edit.getFixes',",
        "  'error': {",
        "    'code': 'SORT_MEMBERS_PARSE_ERRORS',",
        "    'message': 'Error during `edit.sortMembers`: file has 1 scan/parse errors.'",
        "  }",
        "}");
    responseStream.waitForEmpty();
    server.test_waitForWorkerComplete();

    assertNull(fileEditArray[0]);
    assertNotNull(requestErrorArray[0]);
    RequestError requestError = requestErrorArray[0];
    assertEquals("SORT_MEMBERS_PARSE_ERRORS", requestError.getCode());
    assertEquals(
        "Error during `edit.sortMembers`: file has 1 scan/parse errors.",
        requestError.getMessage());
  }

  public void test_execution_createContext() throws Exception {
    final String[] contextIds = {null};
    final RequestError[] requestErrorArray = {null};
    server.execution_createContext("/a/b", new CreateContextConsumer() {
      @Override
      public void computedExecutionContext(String contextId) {
        contextIds[0] = contextId;
      }

      @Override
      public void onError(RequestError requestError) {
        requestErrorArray[0] = requestError;
      }
    });
    List<JsonObject> requests = requestSink.getRequests();
    JsonObject expected = parseJson(//
        "{",
        "  'id': '0',",
        "  'method': 'execution.createContext',",
        "  'params': {",
        "    'contextRoot': '/a/b'",
        "  }",
        "}");
    assertTrue(requests.contains(expected));

    putResponse(//
        "{",
        "  'id': '0',",
        "  'result': {",
        "    'id' : '23'",
        "  }",
        "}");
    responseStream.waitForEmpty();
    server.test_waitForWorkerComplete();
    assertEquals("23", contextIds[0]);
    assertNull(requestErrorArray[0]);
  }

  public void test_execution_createContext_error() throws Exception {
    final String[] contextIds = {null};
    final RequestError[] requestErrorArray = {null};
    server.execution_createContext("/a/b", new CreateContextConsumer() {
      @Override
      public void computedExecutionContext(String contextId) {
        contextIds[0] = contextId;
      }

      @Override
      public void onError(RequestError requestError) {
        requestErrorArray[0] = requestError;
      }
    });
    List<JsonObject> requests = requestSink.getRequests();
    JsonObject expected = parseJson(//
        "{",
        "  'id': '0',",
        "  'method': 'execution.createContext',",
        "  'params': {",
        "    'contextRoot': '/a/b'",
        "  }",
        "}");
    assertTrue(requests.contains(expected));

    putResponse(//
        "{",
        "  'id': '0',",
        "  'method': 'edit.getFixes',",
        "  'error': {",
        "    'code': 'CODE',",
        "    'message': 'MESSAGE'",
        "  }",
        "}");
    responseStream.waitForEmpty();
    server.test_waitForWorkerComplete();

    assertNull(contextIds[0]);
    assertNotNull(requestErrorArray[0]);
    RequestError requestError = requestErrorArray[0];
    assertEquals("CODE", requestError.getCode());
    assertEquals("MESSAGE", requestError.getMessage());
  }

  public void test_execution_deleteContext() throws Exception {
    server.execution_deleteContext("23");
    List<JsonObject> requests = requestSink.getRequests();
    JsonObject expected = parseJson(//
        "{",
        "  'id': '0',",
        "  'method': 'execution.deleteContext',",
        "  'params': {",
        "    'id': '23'",
        "  }",
        "}");
    assertTrue(requests.contains(expected));
  }

  public void test_execution_mapUri_error() throws Exception {
    final String[] files = {null};
    final String[] uris = {null};
    final RequestError[] requestErrorArray = {null};
    server.execution_mapUri("23", null, "package:/foo/foo.dart", new MapUriConsumer() {
      @Override
      public void computedFileOrUri(String file, String uri) {
        files[0] = file;
        uris[0] = uri;
      }

      @Override
      public void onError(RequestError requestError) {
        requestErrorArray[0] = requestError;
      }
    });
    List<JsonObject> requests = requestSink.getRequests();
    JsonObject expected = parseJson(//
        "{",
        "  'id': '0',",
        "  'method': 'execution.mapUri',",
        "  'params': {",
        "    'id': '23',",
        "    'uri' : 'package:/foo/foo.dart'",
        "  }",
        "}");
    assertTrue(requests.contains(expected));

    putResponse(//
        "{",
        "  'id': '0',",
        "  'method': 'execution.mapUri',",
        "  'error': {",
        "    'code': 'CODE',",
        "    'message': 'MESSAGE'",
        "  }",
        "}");
    responseStream.waitForEmpty();
    server.test_waitForWorkerComplete();
    assertNull(files[0]);
    assertNull(uris[0]);
    assertNotNull(requestErrorArray[0]);
    RequestError requestError = requestErrorArray[0];
    assertEquals("CODE", requestError.getCode());
    assertEquals("MESSAGE", requestError.getMessage());
  }

  public void test_execution_mapUri_file() throws Exception {
    final String[] files = {null};
    final String[] uris = {null};
    final RequestError[] requestErrorArray = {null};
    server.execution_mapUri("23", "/a/b", null, new MapUriConsumer() {
      @Override
      public void computedFileOrUri(String file, String uri) {
        files[0] = file;
        uris[0] = uri;
      }

      @Override
      public void onError(RequestError requestError) {
        requestErrorArray[0] = requestError;
      }
    });
    List<JsonObject> requests = requestSink.getRequests();
    JsonObject expected = parseJson(//
        "{",
        "  'id': '0',",
        "  'method': 'execution.mapUri',",
        "  'params': {",
        "    'id': '23',",
        "    'file': '/a/b'",
        "  }",
        "}");
    assertTrue(requests.contains(expected));

    putResponse(//
        "{",
        "  'id': '0',",
        "  'result': {",
        "    'uri' : 'package:/foo/foo.dart'",
        "  }",
        "}");
    responseStream.waitForEmpty();
    server.test_waitForWorkerComplete();
    assertNull(files[0]);
    assertEquals("package:/foo/foo.dart", uris[0]);
    assertNull(requestErrorArray[0]);
  }

  public void test_execution_mapUri_uri() throws Exception {
    final String[] files = {null};
    final String[] uris = {null};
    final RequestError[] requestErrorArray = {null};
    server.execution_mapUri("23", null, "package:/foo/foo.dart", new MapUriConsumer() {
      @Override
      public void computedFileOrUri(String file, String uri) {
        files[0] = file;
        uris[0] = uri;
      }

      @Override
      public void onError(RequestError requestError) {
        requestErrorArray[0] = requestError;
      }
    });
    List<JsonObject> requests = requestSink.getRequests();
    JsonObject expected = parseJson(//
        "{",
        "  'id': '0',",
        "  'method': 'execution.mapUri',",
        "  'params': {",
        "    'id': '23',",
        "    'uri' : 'package:/foo/foo.dart'",
        "  }",
        "}");
    assertTrue(requests.contains(expected));

    putResponse(//
        "{",
        "  'id': '0',",
        "  'result': {",
        "    'file': '/a/b'",
        "  }",
        "}");
    responseStream.waitForEmpty();
    server.test_waitForWorkerComplete();
    assertEquals("/a/b", files[0]);
    assertNull(uris[0]);
    assertNull(requestErrorArray[0]);
  }

  public void test_execution_notification_launchData() throws Exception {
    putResponse(//
        "{",
        "  'event': 'execution.launchData',",
        "  'params': {",
        "    'file': '/a.dart',",
        "    'kind': 'CLIENT'",
        "  }",
        "}");
    responseStream.waitForEmpty();
    server.test_waitForWorkerComplete();
  }

  public void test_execution_setSubscriptions_emptyList() throws Exception {
    server.execution_setSubscriptions(new ArrayList<String>(0));
    List<JsonObject> requests = requestSink.getRequests();
    JsonObject expected = parseJson(//
        "{",
        "  'id': '0',",
        "  'method': 'execution.setSubscriptions',",
        "  'params': {",
        "    'subscriptions': []",
        "  }",
        "}");
    assertTrue(requests.contains(expected));
  }

  public void test_execution_setSubscriptions_launchData() throws Exception {
    ArrayList<String> subscriptions = new ArrayList<String>();
    subscriptions.add(ExecutionService.LAUNCH_DATA);
    server.execution_setSubscriptions(subscriptions);
    List<JsonObject> requests = requestSink.getRequests();
    JsonObject expected = parseJson(//
        "{",
        "  'id': '0',",
        "  'method': 'execution.setSubscriptions',",
        "  'params': {",
        "    'subscriptions': [LAUNCH_DATA]",
        "  }",
        "}");
    assertTrue(requests.contains(expected));
  }

  public void test_execution_setSubscriptions_nullList() throws Exception {
    server.execution_setSubscriptions(null);
    List<JsonObject> requests = requestSink.getRequests();
    JsonObject expected = parseJson(//
        "{",
        "  'id': '0',",
        "  'method': 'execution.setSubscriptions',",
        "  'params': {",
        "    'subscriptions': []",
        "  }",
        "}");
    assertTrue(requests.contains(expected));
  }

  public void test_search_findElementReferences() throws Exception {
    final String[] searchIdArray = new String[] {null};
    final Element[] elementArray = new Element[] {null};
    final RequestError[] requestErrorArray = {null};
    server.search_findElementReferences(
        "/fileA.dart",
        17,
        false,
        new FindElementReferencesConsumer() {
          @Override
          public void computedElementReferences(String searchId, Element element) {
            searchIdArray[0] = searchId;
            elementArray[0] = element;
          }

          @Override
          public void onError(RequestError requestError) {
            requestErrorArray[0] = requestError;
          }
        });
    List<JsonObject> requests = requestSink.getRequests();
    JsonObject expected = parseJson(//
        "{",
        "  'id': '0',",
        "  'method': 'search.findElementReferences',",
        "  'params': {",
        "    'file': '/fileA.dart',",
        "    'offset': 17,",
        "    'includePotential': false",
        "  }",
        "}");
    assertTrue(requests.contains(expected));

    putResponse(//
        "{",
        "  'id': '0',",
        "  'result': {",
        "    'id': 'searchId0',",
        "    'element': {",
        "        'kind': 'CLASS',",
        "        'name': 'name1',",
        "        'location': {",
        "          'file': '/test1.dart',",
        "          'offset': 1,",
        "          'length': 2,",
        "          'startLine': 3,",
        "          'startColumn': 4",
        "        },",
        "        'flags': 63",
        "      }",
        "  }",
        "}");
    responseStream.waitForEmpty();
    server.test_waitForWorkerComplete();
    assertEquals("searchId0", searchIdArray[0]);
    assertNotNull(elementArray[0]);
    assertNull(requestErrorArray[0]);
  }

  public void test_search_findElementReferences_error() throws Exception {
    final String[] searchIdArray = new String[] {null};
    final Element[] elementArray = new Element[] {null};
    final RequestError[] requestErrorArray = {null};
    server.search_findElementReferences(
        "/fileA.dart",
        17,
        false,
        new FindElementReferencesConsumer() {
          @Override
          public void computedElementReferences(String searchId, Element element) {
            searchIdArray[0] = searchId;
            elementArray[0] = element;
          }

          @Override
          public void onError(RequestError requestError) {
            requestErrorArray[0] = requestError;
          }
        });
    List<JsonObject> requests = requestSink.getRequests();
    JsonObject expected = parseJson(//
        "{",
        "  'id': '0',",
        "  'method': 'search.findElementReferences',",
        "  'params': {",
        "    'file': '/fileA.dart',",
        "    'offset': 17,",
        "    'includePotential': false",
        "  }",
        "}");
    assertTrue(requests.contains(expected));

    putResponse(//
        "{",
        "  'id': '0',",
        "  'error': {",
        "    'code': 'CODE',",
        "    'message': 'MESSAGE'",
        "  }",
        "}");
    responseStream.waitForEmpty();
    server.test_waitForWorkerComplete();
    assertNull(searchIdArray[0]);
    assertNull(elementArray[0]);
    assertNotNull(requestErrorArray[0]);
    RequestError requestError = requestErrorArray[0];
    assertEquals("CODE", requestError.getCode());
    assertEquals("MESSAGE", requestError.getMessage());
  }

  public void test_search_findElementReferences_noElement() throws Exception {
    final String[] searchIdArray = new String[] {null};
    final Element[] elementArray = new Element[] {null};
    final RequestError[] requestErrorArray = {null};
    server.search_findElementReferences(
        "/fileA.dart",
        17,
        false,
        new FindElementReferencesConsumer() {
          @Override
          public void computedElementReferences(String searchId, Element element) {
            searchIdArray[0] = searchId;
            elementArray[0] = element;
          }

          @Override
          public void onError(RequestError requestError) {
            requestErrorArray[0] = requestError;
          }
        });
    List<JsonObject> requests = requestSink.getRequests();
    JsonObject expected = parseJson(//
        "{",
        "  'id': '0',",
        "  'method': 'search.findElementReferences',",
        "  'params': {",
        "    'file': '/fileA.dart',",
        "    'offset': 17,",
        "    'includePotential': false",
        "  }",
        "}");
    assertTrue(requests.contains(expected));

    putResponse(//
        "{",
        "  'id': '0',",
        "  'result': {}",
        "}");
    responseStream.waitForEmpty();
    server.test_waitForWorkerComplete();
    assertNull(searchIdArray[0]);
    assertNull(elementArray[0]);
    assertNull(requestErrorArray[0]);
  }

  public void test_search_findMemberDeclarations() throws Exception {
    final String[] searchIdArray = new String[1];
    final RequestError[] requestErrorArray = {null};
    server.search_findMemberDeclarations("mydeclaration", new FindMemberDeclarationsConsumer() {
      @Override
      public void computedSearchId(String searchId) {
        searchIdArray[0] = searchId;
      }

      @Override
      public void onError(RequestError requestError) {
        requestErrorArray[0] = requestError;
      }
    });
    List<JsonObject> requests = requestSink.getRequests();
    JsonObject expected = parseJson(//
        "{",
        "  'id': '0',",
        "  'method': 'search.findMemberDeclarations',",
        "  'params': {",
        "    'name': 'mydeclaration'",
        "  }",
        "}");
    assertTrue(requests.contains(expected));

    putResponse(//
        "{",
        "  'id': '0',",
        "  'result': {",
        "    'id': 'searchId1'",
        "  }",
        "}");
    responseStream.waitForEmpty();
    server.test_waitForWorkerComplete();
    assertEquals("searchId1", searchIdArray[0]);
    assertNull(requestErrorArray[0]);
  }

  public void test_search_findMemberDeclarations_error() throws Exception {
    final String[] searchIdArray = new String[1];
    final RequestError[] requestErrorArray = {null};
    server.search_findMemberDeclarations("mydeclaration", new FindMemberDeclarationsConsumer() {
      @Override
      public void computedSearchId(String searchId) {
        searchIdArray[0] = searchId;
      }

      @Override
      public void onError(RequestError requestError) {
        requestErrorArray[0] = requestError;
      }
    });
    List<JsonObject> requests = requestSink.getRequests();
    JsonObject expected = parseJson(//
        "{",
        "  'id': '0',",
        "  'method': 'search.findMemberDeclarations',",
        "  'params': {",
        "    'name': 'mydeclaration'",
        "  }",
        "}");
    assertTrue(requests.contains(expected));

    putResponse(//
        "{",
        "  'id': '0',",
        "  'error': {",
        "    'code': 'CODE',",
        "    'message': 'MESSAGE'",
        "  }",
        "}");
    responseStream.waitForEmpty();
    server.test_waitForWorkerComplete();
    assertNull(searchIdArray[0]);
    assertNotNull(requestErrorArray[0]);
    RequestError requestError = requestErrorArray[0];
    assertEquals("CODE", requestError.getCode());
    assertEquals("MESSAGE", requestError.getMessage());
  }

  public void test_search_findMemberReferences() throws Exception {
    final String[] searchIdArray = new String[1];
    final RequestError[] requestErrorArray = {null};
    server.search_findMemberReferences("mydeclaration", new FindMemberReferencesConsumer() {
      @Override
      public void computedSearchId(String searchId) {
        searchIdArray[0] = searchId;
      }

      @Override
      public void onError(RequestError requestError) {
        requestErrorArray[0] = requestError;
      }
    });
    List<JsonObject> requests = requestSink.getRequests();
    JsonObject expected = parseJson(//
        "{",
        "  'id': '0',",
        "  'method': 'search.findMemberReferences',",
        "  'params': {",
        "    'name': 'mydeclaration'",
        "  }",
        "}");
    assertTrue(requests.contains(expected));

    putResponse(//
        "{",
        "  'id': '0',",
        "  'result': {",
        "    'id': 'searchId2'",
        "  }",
        "}");
    responseStream.waitForEmpty();
    server.test_waitForWorkerComplete();
    assertEquals("searchId2", searchIdArray[0]);
    assertNull(requestErrorArray[0]);
  }

  public void test_search_findMemberReferences_error() throws Exception {
    final String[] searchIdArray = new String[1];
    final RequestError[] requestErrorArray = {null};
    server.search_findMemberReferences("mydeclaration", new FindMemberReferencesConsumer() {
      @Override
      public void computedSearchId(String searchId) {
        searchIdArray[0] = searchId;
      }

      @Override
      public void onError(RequestError requestError) {
        requestErrorArray[0] = requestError;
      }
    });
    List<JsonObject> requests = requestSink.getRequests();
    JsonObject expected = parseJson(//
        "{",
        "  'id': '0',",
        "  'method': 'search.findMemberReferences',",
        "  'params': {",
        "    'name': 'mydeclaration'",
        "  }",
        "}");
    assertTrue(requests.contains(expected));

    putResponse(//
        "{",
        "  'id': '0',",
        "  'error': {",
        "    'code': 'CODE',",
        "    'message': 'MESSAGE'",
        "  }",
        "}");
    responseStream.waitForEmpty();
    server.test_waitForWorkerComplete();
    assertNull(searchIdArray[0]);
    assertNotNull(requestErrorArray[0]);
    RequestError requestError = requestErrorArray[0];
    assertEquals("CODE", requestError.getCode());
    assertEquals("MESSAGE", requestError.getMessage());
  }

  public void test_search_findTopLevelDeclarations() throws Exception {
    final String[] searchIdArray = new String[1];
    final RequestError[] requestErrorArray = {null};
    server.search_findTopLevelDeclarations("some-pattern", new FindTopLevelDeclarationsConsumer() {
      @Override
      public void computedSearchId(String searchId) {
        searchIdArray[0] = searchId;
      }

      @Override
      public void onError(RequestError requestError) {
        requestErrorArray[0] = requestError;
      }
    });
    List<JsonObject> requests = requestSink.getRequests();
    JsonObject expected = parseJson(//
        "{",
        "  'id': '0',",
        "  'method': 'search.findTopLevelDeclarations',",
        "  'params': {",
        "    'pattern': 'some-pattern'",
        "  }",
        "}");
    assertTrue(requests.contains(expected));

    putResponse(//
        "{",
        "  'id': '0',",
        "  'result': {",
        "    'id': 'searchId3'",
        "  }",
        "}");
    responseStream.waitForEmpty();
    server.test_waitForWorkerComplete();
    assertEquals("searchId3", searchIdArray[0]);
    assertNull(requestErrorArray[0]);
  }

  public void test_search_findTopLevelDeclarations_error() throws Exception {
    final String[] searchIdArray = new String[1];
    final RequestError[] requestErrorArray = {null};
    server.search_findTopLevelDeclarations("some-pattern", new FindTopLevelDeclarationsConsumer() {
      @Override
      public void computedSearchId(String searchId) {
        searchIdArray[0] = searchId;
      }

      @Override
      public void onError(RequestError requestError) {
        requestErrorArray[0] = requestError;
      }
    });
    List<JsonObject> requests = requestSink.getRequests();
    JsonObject expected = parseJson(//
        "{",
        "  'id': '0',",
        "  'method': 'search.findTopLevelDeclarations',",
        "  'params': {",
        "    'pattern': 'some-pattern'",
        "  }",
        "}");
    assertTrue(requests.contains(expected));

    putResponse(//
        "{",
        "  'id': '0',",
        "  'error': {",
        "    'code': 'CODE',",
        "    'message': 'MESSAGE'",
        "  }",
        "}");
    responseStream.waitForEmpty();
    server.test_waitForWorkerComplete();
    assertNull(searchIdArray[0]);
    assertNotNull(requestErrorArray[0]);
    RequestError requestError = requestErrorArray[0];
    assertEquals("CODE", requestError.getCode());
    assertEquals("MESSAGE", requestError.getMessage());
  }

  public void test_search_getTypeHierarchy() throws Exception {
    final Object[] itemsArray = {null};
    final RequestError[] requestErrorArray = {null};
    server.search_getTypeHierarchy("/fileA.dart", 1, true, new GetTypeHierarchyConsumer() {
      @Override
      public void computedHierarchy(List<TypeHierarchyItem> hierarchyItems) {
        itemsArray[0] = hierarchyItems;
      }

      @Override
      public void onError(RequestError requestError) {
        requestErrorArray[0] = requestError;
      }
    });
    List<JsonObject> requests = requestSink.getRequests();
    JsonObject expected = parseJson(//
        "{",
        "  'id': '0',",
        "  'method': 'search.getTypeHierarchy',",
        "  'params': {",
        "    'file': '/fileA.dart',",
        "    'offset': 1,",
        "    'superOnly': true",
        "  }",
        "}");
    assertTrue(requests.contains(expected));

    putResponse(//
        "{",
        "  'id': '0',",
        "  'result': {",
        "    'hierarchyItems': [{",
        "      'classElement': {",
        "        'kind': 'CLASS',",
        "        'name': 'name1',",
        "        'location': {",
        "          'file': '/test1.dart',",
        "          'offset': 1,",
        "          'length': 2,",
        "          'startLine': 3,",
        "          'startColumn': 4",
        "        },",
        "        'flags': 63",
        "      },",
        "      'displayName': 'displayName1',",
        "      'memberElement': {",
        "        'kind': 'CLASS',",
        "        'name': 'name2',",
        "        'location': {",
        "          'file': '/test2.dart',",
        "          'offset': 5,",
        "          'length': 6,",
        "          'startLine': 7,",
        "          'startColumn': 8",
        "        },",
        "        'flags': 0",
        "      },",
        "      'superclass': 1,",
        "      'interfaces': [2, 3],",
        "      'mixins': [4, 5],",
        "      'subclasses': [6, 7]",
        "    }]",
        "  }",
        "}");
    responseStream.waitForEmpty();
    server.test_waitForWorkerComplete();
    @SuppressWarnings("unchecked")
    List<TypeHierarchyItem> items = (List<TypeHierarchyItem>) itemsArray[0];
    assertThat(items).hasSize(1);
    assertNull(requestErrorArray[0]);
  }

  public void test_search_getTypeHierarchy_error() throws Exception {
    final Object[] itemsArray = {null};
    final RequestError[] requestErrorArray = {null};
    server.search_getTypeHierarchy("/fileA.dart", 1, false, new GetTypeHierarchyConsumer() {
      @Override
      public void computedHierarchy(List<TypeHierarchyItem> hierarchyItems) {
        itemsArray[0] = hierarchyItems;
      }

      @Override
      public void onError(RequestError requestError) {
        requestErrorArray[0] = requestError;
      }
    });
    List<JsonObject> requests = requestSink.getRequests();
    JsonObject expected = parseJson(//
        "{",
        "  'id': '0',",
        "  'method': 'search.getTypeHierarchy',",
        "  'params': {",
        "    'file': '/fileA.dart',",
        "    'offset': 1,",
        "    'superOnly': false",
        "  }",
        "}");
    assertTrue(requests.contains(expected));

    putResponse(//
        "{",
        "  'id': '0',",
        "  'error': {",
        "    'code': 'CODE',",
        "    'message': 'MESSAGE'",
        "  }",
        "}");
    responseStream.waitForEmpty();
    server.test_waitForWorkerComplete();
    assertNull(itemsArray[0]);
    assertNotNull(requestErrorArray[0]);
  }

  public void test_search_notification_results() throws Exception {
    putResponse(//
        "{",
        "  'event': 'search.results',",
        "  'params': {",
        "    'id': 'searchId7',",
        "    'results' : [",
        "      {",
        "        'location': {",
        "          'file': 'someFile.dart',",
        "          'offset': 9,",
        "          'length': 10,",
        "          'startLine': 11,",
        "          'startColumn': 12",
        "        },",
        "        'kind': 'DECLARATION',",
        "        'isPotential': true,",
        "        'path': [",
        "          {",
        "            'kind': 'FUNCTION',",
        "            'name': 'foo',",
        "            'location': {",
        "              'file': 'fileA.dart',",
        "              'offset': 13,",
        "              'length': 14,",
        "              'startLine': 15,",
        "              'startColumn': 16",
        "            },",
        "            'flags': 42,",
        "            'parameters': '(a, b, c)',",
        "            'returnType': 'anotherType'",
        "          },",
        "          {",
        "            'kind': 'CLASS',",
        "            'name': 'myClass',",
        "            'location': {",
        "              'file': 'fileB.dart',",
        "              'offset': 17,",
        "              'length': 18,",
        "              'startLine': 19,",
        "              'startColumn': 20",
        "            },",
        "            'flags': 21",
        "          }",
        "        ]",
        "      }",
        "    ],",
        "    'isLast': true",
        "  }",
        "}");
    responseStream.waitForEmpty();
    server.test_waitForWorkerComplete();
    List<SearchResult> results = listener.getSearchResults("searchId7");
    assertThat(results).hasSize(1);
    {
      SearchResult result = results.get(0);
      assertLocation(result.getLocation(), "someFile.dart", 9, 10, 11, 12);
      assertEquals(SearchResultKind.DECLARATION, result.getKind());
      assertEquals(true, result.isPotential());
      {
        List<Element> path = result.getPath();
        assertThat(path).hasSize(2);
        {
          Element element = path.get(0);
          assertEquals(ElementKind.FUNCTION, element.getKind());
          assertEquals("foo", element.getName());
          assertLocation(element.getLocation(), "fileA.dart", 13, 14, 15, 16);
          assertFalse(element.isAbstract());
          assertTrue(element.isConst());
          assertFalse(element.isFinal());
          assertTrue(element.isTopLevelOrStatic());
          assertFalse(element.isPrivate());
          assertTrue(element.isDeprecated());
          assertEquals("(a, b, c)", element.getParameters());
          assertEquals("anotherType", element.getReturnType());
        }
        {
          Element element = path.get(1);
          assertEquals(ElementKind.CLASS, element.getKind());
          assertEquals("myClass", element.getName());
          assertLocation(element.getLocation(), "fileB.dart", 17, 18, 19, 20);
          assertTrue(element.isAbstract());
          assertFalse(element.isConst());
          assertTrue(element.isFinal());
          assertFalse(element.isTopLevelOrStatic());
          assertTrue(element.isPrivate());
          assertFalse(element.isDeprecated());
          assertNull(element.getParameters());
          assertNull(element.getReturnType());
        }
      }
    }
  }

  public void test_server_getVersion() throws Exception {
    final String[] versionPtr = {null};
    final RequestError[] requestErrorArray = {null};
    server.server_getVersion(new GetVersionConsumer() {
      @Override
      public void computedVersion(String version) {
        versionPtr[0] = version;
      }

      @Override
      public void onError(RequestError requestError) {
        requestErrorArray[0] = requestError;
      }
    });
    List<JsonObject> requests = requestSink.getRequests();
    JsonObject expected = parseJson(//
        "{",
        "  'id': '0',",
        "  'method': 'server.getVersion'",
        "}");
    assertTrue(requests.contains(expected));

    putResponse(//
        "{",
        "  'id': '0',",
        "  'result': {",
        "    'version': '0.0.1'",
        "  }",
        "}");
    responseStream.waitForEmpty();
    server.test_waitForWorkerComplete();

    assertEquals("0.0.1", versionPtr[0]);
    assertNull(requestErrorArray[0]);
  }

  public void test_server_getVersion_error() throws Exception {
    final String[] versionPtr = {null};
    final RequestError[] requestErrorArray = {null};
    server.server_getVersion(new GetVersionConsumer() {
      @Override
      public void computedVersion(String version) {
        versionPtr[0] = version;
      }

      @Override
      public void onError(RequestError requestError) {
        requestErrorArray[0] = requestError;
      }
    });
    List<JsonObject> requests = requestSink.getRequests();
    JsonObject expected = parseJson(//
        "{",
        "  'id': '0',",
        "  'method': 'server.getVersion'",
        "}");
    assertTrue(requests.contains(expected));

    putResponse(//
        "{",
        "  'id': '0',",
        "  'error': {",
        "    'code': 'CODE',",
        "    'message': 'message0',",
        "    'stackTrace': 'stackTrace0'",
        "  }",
        "}");
    responseStream.waitForEmpty();
    server.test_waitForWorkerComplete();

    assertNull(versionPtr[0]);
    RequestError requestError = requestErrorArray[0];
    assertNotNull(requestError);
    assertEquals("CODE", requestError.getCode());
    assertEquals("message0", requestError.getMessage());
    assertEquals("stackTrace0", requestError.getStackTrace());
  }

  public void test_server_notification_connected() throws Exception {
    listener.assertServerConnected(false);
    putResponse(//
        "{",
        "  'event': 'server.connected'",
        "}");
    responseStream.waitForEmpty();
    server.test_waitForWorkerComplete();
    listener.assertServerConnected(true);
  }

  public void test_server_notification_error() throws Exception {
    putResponse(//
        "{",
        "  'event': 'server.error',",
        "  'params': {",
        "    'isFatal': false,",
        "    'message': 'message0',",
        "    'stackTrace': 'stackTrace0'",
        "  }",
        "}");
    responseStream.waitForEmpty();
    server.test_waitForWorkerComplete();
    List<AnalysisServerError> errors = Lists.newArrayList();
    errors.add(new AnalysisServerError(false, "message0", "stackTrace0"));
    listener.assertServerErrors(errors);
  }

  public void test_server_notification_error2() throws Exception {
    putResponse(//
        "{",
        "  'event': 'server.error',",
        "  'params': {",
        "    'isFatal': false,",
        "    'message': 'message0',",
        "    'stackTrace': 'stackTrace0'",
        "  }",
        "}");
    putResponse(//
        "{",
        "  'event': 'server.error',",
        "  'params': {",
        "    'isFatal': true,",
        "    'message': 'message1',",
        "    'stackTrace': 'stackTrace1'",
        "  }",
        "}");
    responseStream.waitForEmpty();
    server.test_waitForWorkerComplete();
    List<AnalysisServerError> errors = Lists.newArrayList();
    errors.add(new AnalysisServerError(false, "message0", "stackTrace0"));
    errors.add(new AnalysisServerError(true, "message1", "stackTrace1"));
    listener.assertServerErrors(errors);
  }

  public void test_server_notification_status_false() throws Exception {
    putResponse(//
        "{",
        "  'event': 'server.status',",
        "  'params': {",
        "    'analysis': {",
        "      'isAnalyzing': false",
        "    }",
        "  }",
        "}");
    responseStream.waitForEmpty();
    server.test_waitForWorkerComplete();
    listener.assertAnalysisStatus(new AnalysisStatus(false, null));
  }

  public void test_server_notification_status_true() throws Exception {
    putResponse(//
        "{",
        "  'event': 'server.status',",
        "  'params': {",
        "    'analysis': {",
        "      'isAnalyzing': true,",
        "      'analysisTarget': 'target0'",
        "    }",
        "  }",
        "}");
    responseStream.waitForEmpty();
    server.test_waitForWorkerComplete();
    listener.assertAnalysisStatus(new AnalysisStatus(true, "target0"));
  }

  public void test_server_setSubscriptions_emptyList() throws Exception {
    server.server_setSubscriptions(new ArrayList<String>(0));
    List<JsonObject> requests = requestSink.getRequests();
    JsonObject expected = parseJson(//
        "{",
        "  'id': '0',",
        "  'method': 'server.setSubscriptions',",
        "  'params': {",
        "    'subscriptions': []",
        "  }",
        "}");
    assertTrue(requests.contains(expected));
  }

  public void test_server_setSubscriptions_nullList() throws Exception {
    server.server_setSubscriptions(null);
    List<JsonObject> requests = requestSink.getRequests();
    JsonObject expected = parseJson(//
        "{",
        "  'id': '0',",
        "  'method': 'server.setSubscriptions',",
        "  'params': {",
        "    'subscriptions': []",
        "  }",
        "}");
    assertTrue(requests.contains(expected));
  }

  public void test_server_setSubscriptions_status() throws Exception {
    ArrayList<String> subscriptions = new ArrayList<String>();
    subscriptions.add(ServerService.STATUS);
    server.server_setSubscriptions(subscriptions);
    List<JsonObject> requests = requestSink.getRequests();
    JsonObject expected = parseJson(//
        "{",
        "  'id': '0',",
        "  'method': 'server.setSubscriptions',",
        "  'params': {",
        "    'subscriptions': [STATUS]",
        "  }",
        "}");
    assertTrue(requests.contains(expected));
  }

  public void test_server_shutdown() throws Exception {
    server.server_shutdown();
    JsonObject expected = parseJson(//
        "{",
        "  'id': '0',",
        "  'method': 'server.shutdown'",
        "}");
    assertTrue(requestSink.getRequests().contains(expected));
    assertFalse(requestSink.isClosed());
    putResponse(//
        "{",
        "  'id': '0'",
        "}");
    responseStream.waitForEmpty();
    server.test_waitForWorkerComplete();
    assertTrue(requestSink.isClosed());
    assertTrue(socket.isStopped());
  }

  public void test_server_startup() throws Exception {
    server.start();
    // Simulate a response
    putResponse(//
        "{",
        "  'id': '0'",
        "}");
    assertTrue(socket.isStarted());
    server.server_shutdown();
  }

  @Override
  protected void setUp() throws Exception {
    super.setUp();
    RequestUtilities.setIncludeRequestTime(false);
  }

  @Override
  protected void tearDown() throws Exception {
    RequestUtilities.setIncludeRequestTime(true);
    super.tearDown();
  }

  private void assertLocation(Location location, String file, int offset, int length,
      int startLine, int startColumn) {
    assertEquals(file, location.getFile());
    assertEquals(offset, location.getOffset());
    assertEquals(length, location.getLength());
    assertEquals(startLine, location.getStartLine());
    assertEquals(startColumn, location.getStartColumn());
  }

  private String getSourceChangeJson() {
    return Joiner.on('\n').join(
        "          {",
        "            'message': 'message1',",
        "            'edits': [",
        "              {",
        "                'file':'file1.dart',",
        "                'fileStamp': 101,",
        "                'edits': [",
        "                  {",
        "                    'offset': 1,",
        "                    'length': 2,",
        "                    'replacement': 'replacement1',",
        "                    'id': 'id1'",
        "                  }",
        "                ]",
        "              }",
        "            ],",
        "            'linkedEditGroups': [",
        "              {",
        "                'positions': [",
        "                  {",
        "                    'file': 'file2.dart',",
        "                    'offset': 3",
        "                  }",
        "                ],",
        "                'length': 4,",
        "                'suggestions': [",
        "                  {",
        "                    'value': 'value1',",
        "                    'kind': 'METHOD'",
        "                  }",
        "                ]",
        "              }",
        "            ],",
        "            'selection': {",
        "              'file': 'file3.dart',",
        "              'offset': 5",
        "            }",
        "          }");
  }

  /**
   * Builds a JSON string from the given lines.
   */
  private JsonObject parseJson(String... lines) {
    String json = Joiner.on('\n').join(lines);
    json = json.replace('\'', '"');
    return (JsonObject)new JsonParser().parse(json);
  }
}
