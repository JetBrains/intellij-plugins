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

import org.dartlang.analysis.server.protocol.RefactoringKind;
import org.dartlang.analysis.server.protocol.RequestError;

import java.util.List;

/**
 * The interface {@code GetAvailableRefactoringsConsumer} defines the behavior of objects that
 * consume a get refactoring response.
 * 
 * @coverage dart.server
 */
public interface GetAvailableRefactoringsConsumer extends Consumer {
  /**
   * The refactoring kinds that have been computed for file location.
   * 
   * @param refactoringKinds the kinds of refactorings that are valid for the given selection
   * @see RefactoringKind
   */
  void computedRefactoringKinds(List<String> refactoringKinds);

  /**
   * If a set of refactoring kinds cannot be passed back, some {@link RequestError} is passed back
   * instead.
   * 
   * @param requestError the reason why a result was not passed back
   */
  void onError(RequestError requestError);
}
