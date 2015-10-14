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
package com.google.dart.server.internal.remote.processor;

import com.google.dart.server.GetRefactoringConsumer;
import com.google.dart.server.utilities.general.JsonUtilities;
import com.google.gson.JsonObject;

import org.dartlang.analysis.server.protocol.ExtractLocalVariableFeedback;
import org.dartlang.analysis.server.protocol.ExtractMethodFeedback;
import org.dartlang.analysis.server.protocol.InlineLocalVariableFeedback;
import org.dartlang.analysis.server.protocol.InlineMethodFeedback;
import org.dartlang.analysis.server.protocol.RefactoringFeedback;
import org.dartlang.analysis.server.protocol.RefactoringKind;
import org.dartlang.analysis.server.protocol.RefactoringProblem;
import org.dartlang.analysis.server.protocol.RenameFeedback;
import org.dartlang.analysis.server.protocol.RequestError;
import org.dartlang.analysis.server.protocol.SourceChange;

import java.util.List;
import java.util.Map;

/**
 * Instances of {@code GetRefactoringProcessor} translate JSON result objects for a given
 * {@link GetRefactoringConsumer}.
 * 
 * @coverage dart.server.remote
 */
public class GetRefactoringProcessor extends ResultProcessor {
  private final Map<String, String> requestToRefactoringKindMap;
  private final GetRefactoringConsumer consumer;

  public GetRefactoringProcessor(Map<String, String> requestToRefactoringKindMap,
      GetRefactoringConsumer consumer) {
    this.requestToRefactoringKindMap = requestToRefactoringKindMap;
    this.consumer = consumer;
  }

  public void process(String requestId, JsonObject resultObject, RequestError requestError) {
    if (resultObject != null) {
      try {
        // problems
        List<RefactoringProblem> initialProblems = getRefactoringProblems(
            resultObject,
            "initialProblems");
        List<RefactoringProblem> optionsProblems = getRefactoringProblems(
            resultObject,
            "optionsProblems");
        List<RefactoringProblem> finalProblems = getRefactoringProblems(
            resultObject,
            "finalProblems");

        // change
        SourceChange change = null;
        if (resultObject.has("change")) {
          change = SourceChange.fromJson(resultObject.get("change").getAsJsonObject());
        }

        // potential edits
        List<String> potentialEdits = JsonUtilities.decodeStringList(resultObject.get("potentialEdits") != null
            ? resultObject.get("potentialEdits").getAsJsonArray() : null);

        //
        // Compute all refactoring-kind specific "Feedback" and put them into the feedback map
        //
        RefactoringFeedback feedback = null;
        if (resultObject.has("feedback")) {
          JsonObject feedbackObject = resultObject.get("feedback").getAsJsonObject();
          String kind = requestToRefactoringKindMap.remove(requestId);
          if (RefactoringKind.EXTRACT_LOCAL_VARIABLE.equals(kind)) {
            feedback = ExtractLocalVariableFeedback.fromJson(feedbackObject);
          } else if (RefactoringKind.EXTRACT_METHOD.equals(kind)) {
            feedback = ExtractMethodFeedback.fromJson(feedbackObject);
          } else if (RefactoringKind.INLINE_LOCAL_VARIABLE.equals(kind)) {
            feedback = InlineLocalVariableFeedback.fromJson(feedbackObject);
          } else if (RefactoringKind.INLINE_METHOD.equals(kind)) {
            feedback = InlineMethodFeedback.fromJson(feedbackObject);
          } else if (RefactoringKind.RENAME.equals(kind)) {
            feedback = RenameFeedback.fromJson(feedbackObject);
          }
        }
        consumer.computedRefactorings(
            initialProblems,
            optionsProblems,
            finalProblems,
            feedback,
            change,
            potentialEdits);
      } catch (Exception exception) {
        // catch any exceptions in the formatting of this response
        requestError = generateRequestError(exception);
      }
    }
    if (requestError != null) {
      consumer.onError(requestError);
    }
  }

  private List<RefactoringProblem> getRefactoringProblems(JsonObject resultObject, String name) {
    return RefactoringProblem.fromJsonArray(resultObject.get(name).getAsJsonArray());
  }
}
