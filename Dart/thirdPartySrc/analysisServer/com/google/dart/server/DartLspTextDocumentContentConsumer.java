/*
 * Copyright (c) 2024, the Dart project authors.
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

public interface DartLspTextDocumentContentConsumer extends Consumer {

  public void computedDocumentContents(String contents);

  /**
   * If the file contents can't be sent back, some {@link RequestError} is passed back instead.
   *
   * @param requestError the reason why a result was not passed back
   */
  public void onError(RequestError requestError);
}