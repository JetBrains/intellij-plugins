/*
 * Copyright (c) 2015, the Dart project authors.
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

import org.dartlang.analysis.server.protocol.RequestError;
import org.dartlang.analysis.server.protocol.SourceEdit;

import java.util.List;

/**
 * The interface {@code FormatConsumer} defines the behavior of objects that consume the results of
 * an {@code edit.format} request.
 */
public interface FormatConsumer extends Consumer {
  /**
   * The result of formatting code has been computed.
   * 
   * @param edits the edit(s) to be applied in order to format the code. The list will be empty if
   *          the code was already formatted (there are no changes).
   * @param selectionOffset the offset of the selection after formatting the code
   * @param selectionLength the length of the selection after formatting the code
   */
  public void computedFormat(List<SourceEdit> edits, int selectionOffset, int selectionLength);

  /**
   * If a formatting edits cannot be passed back, some {@link RequestError} is passed back instead.
   * 
   * @param requestError the reason why a result was not passed back
   */
  public void onError(RequestError requestError);
}
