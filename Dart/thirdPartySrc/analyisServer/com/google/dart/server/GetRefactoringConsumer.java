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

import org.dartlang.analysis.server.protocol.RefactoringFeedback;
import org.dartlang.analysis.server.protocol.RefactoringProblem;
import org.dartlang.analysis.server.protocol.RequestError;
import org.dartlang.analysis.server.protocol.SourceChange;

import java.util.List;

/**
 * The interface {@code GetRefactoringConsumer} defines the behavior of objects that consume the
 * changes required to perform a refactoring.
 * 
 * @coverage dart.server
 */
public interface GetRefactoringConsumer extends Consumer {
  /**
   * The changes required to perform a refactoring.
   * 
   * @param initialProblems The initial status of the refactoring, i.e. problems related to the
   *          context in which the refactoring is requested. The array will be empty if there are no
   *          known problems.
   * @param optionsProblems The options validation status, i.e. problems in the given options, such
   *          as light-weight validation of a new name, flags compatibility, etc. The array will be
   *          empty if there are no known problems.
   * @param finalProblems The final status of the refactoring, i.e. problems identified in the
   *          result of a full, potentially expensive validation and / or change creation. The array
   *          will be empty if there are no known problems.
   * @param feedback Data used to provide feedback to the user. The structure of the data is
   *          dependent on the kind of refactoring being created. The data that is returned is
   *          documented in the section titled Refactorings, labeled as “Feedback”.
   * @param change The changes that are to be applied to affect the refactoring. This field will be
   *          omitted if there are problems that prevent a set of changes from being computed, such
   *          as having no options specified for a refactoring that requires them, or if only
   *          validation was requested.
   * @param potentialEdits The ids of source edits that are not known to be valid. An edit is not
   *          known to be valid if there was insufficient type information for the server to be able
   *          to determine whether or not the code needs to be modified, such as when a member is
   *          being renamed and there is a reference to a member from an unknown type. This field
   *          will be omitted if the change field is omitted or if there are no potential edits for
   *          the refactoring.
   */
  public void computedRefactorings(List<RefactoringProblem> initialProblems,
      List<RefactoringProblem> optionsProblems, List<RefactoringProblem> finalProblems,
      RefactoringFeedback feedback, SourceChange change, List<String> potentialEdits);

  /**
   * If a set of changes cannot be passed back, some {@link RequestError} is passed back instead.
   * 
   * @param requestError the reason why a result was not passed back
   */
  public void onError(RequestError requestError);
}
