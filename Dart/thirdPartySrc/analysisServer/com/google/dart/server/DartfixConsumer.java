/*
 * Copyright (c) 2019, the Dart project authors.
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

import org.dartlang.analysis.server.protocol.DartFixSuggestion;
import org.dartlang.analysis.server.protocol.RequestError;
import org.dartlang.analysis.server.protocol.SourceFileEdit;

import java.util.List;

/**
 * The interface {@code DartfixConsumer} defines the behavior of objects that consume a dartfix request.
 *
 * @coverage dart.server
 */
public interface DartfixConsumer extends Consumer {
  /**
   * @param suggestions      A list of recommended changes that can be automatically made by applying the 'edits' included in this response.
   * @param otherSuggestions A list of recommended changes that could not be automatically made.
   * @param hasErrors        True if the analyzed source contains errors that might impact the correctness of the recommended changes that
   *                         can be automatically applied.
   * @param edits            A list of source edits to apply the recommended changes.
   */
  public void computedDartfix(List<DartFixSuggestion> suggestions,
                              List<DartFixSuggestion> otherSuggestions,
                              boolean hasErrors,
                              List<SourceFileEdit> edits);

  /**
   * If a transitive closure cannot be passed back, some {@link RequestError} is passed back
   * instead.
   *
   * @param requestError the reason why a result was not passed back
   */
  public void onError(RequestError requestError);
}
